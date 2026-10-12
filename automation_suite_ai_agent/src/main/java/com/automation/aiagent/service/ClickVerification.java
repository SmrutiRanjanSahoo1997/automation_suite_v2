package com.automation.aiagent.service;

import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Native Java implementation of the verification clicker service.
 * Replaces scripts/click_verification.py with exact same flow and timing using java.awt.Robot.
 */
public class ClickVerification {

    private static final String SCRIPT_FOREGROUND =
            "if (-not ([System.Management.Automation.PSTypeName]'Win32Fg').Type) {\n" +
            "    Add-Type 'using System; using System.Runtime.InteropServices; public class Win32Fg {\n" +
            "        [DllImport(\"user32.dll\")] public static extern bool ShowWindowAsync(IntPtr hWnd, int nCmdShow);\n" +
            "        [DllImport(\"user32.dll\")] public static extern bool SetForegroundWindow(IntPtr hWnd);\n" +
            "        [DllImport(\"user32.dll\")] public static extern bool BringWindowToTop(IntPtr hWnd);\n" +
            "        [DllImport(\"user32.dll\")] public static extern void keybd_event(byte bVk, byte bScan, uint dwFlags, UIntPtr dwExtraInfo);\n" +
            "    }'\n" +
            "}\n" +
            "[Win32Fg]::keybd_event(0, 0, 0, [UIntPtr]::Zero)\n" +
            "[Win32Fg]::keybd_event(0, 0, 2, [UIntPtr]::Zero)\n" +
            "$found = $false\n" +
            "Get-Process | Where-Object { $_.MainWindowHandle -ne 0 -and ($_.MainWindowTitle -match 'chrome|copilot' -or $_.ProcessName -match 'chrome') } | ForEach-Object {\n" +
            "    [Win32Fg]::ShowWindowAsync($_.MainWindowHandle, 9) | Out-Null\n" +
            "    [Win32Fg]::ShowWindowAsync($_.MainWindowHandle, 3) | Out-Null\n" +
            "    [Win32Fg]::BringWindowToTop($_.MainWindowHandle) | Out-Null\n" +
            "    [Win32Fg]::SetForegroundWindow($_.MainWindowHandle) | Out-Null\n" +
            "    $found = $true\n" +
            "}\n" +
            "if ($found) { 'true' } else { 'false' }\n";

    /**
     * Step 1: Finds Chrome/Copilot top-level window and brings it to foreground.
     */
    public static boolean bringChromeToForeground() {
        try {
            byte[] bytes = SCRIPT_FOREGROUND.getBytes(StandardCharsets.UTF_16LE);
            String encoded = Base64.getEncoder().encodeToString(bytes);

            ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-EncodedCommand", encoded);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            boolean found = false;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().equalsIgnoreCase("true")) {
                        found = true;
                    }
                }
            }
            process.waitFor();
            Thread.sleep(150);
            return found;
        } catch (Exception e) {
            System.err.println("[ClickVerification] Warning restoring window: " + e.getMessage());
            return false;
        }
    }

    /**
     * Smooth human-like mouse movement interpolating between current location and destination.
     */
    private static void smoothMouseMove(Robot robot, int targetX, int targetY, int durationMs) {
        try {
            Point cur = MouseInfo.getPointerInfo().getLocation();
            int startX = cur.x;
            int startY = cur.y;
            int steps = Math.max(1, durationMs / 10);

            for (int i = 1; i <= steps; i++) {
                double progress = (double) i / steps;
                double ease = (1 - Math.cos(progress * Math.PI)) / 2.0; // ease in-out
                int intermediateX = (int) (startX + (targetX - startX) * ease);
                int intermediateY = (int) (startY + (targetY - startY) * ease);
                robot.mouseMove(intermediateX, intermediateY);
                Thread.sleep(10);
            }
            robot.mouseMove(targetX, targetY);
        } catch (Exception e) {
            robot.mouseMove(targetX, targetY);
        }
    }

    /**
     * Main verification clicker reproducing exact 3-step flow:
     * 1. Foreground Chrome
     * 2. Determine target coordinates
     * 3. Human-like mouse movement and click
     */
    public static void clickVerificationBox(double targetX, double targetY) {
        try {
            // Step 1: Ensure Chrome is foregrounded
            boolean brought = bringChromeToForeground();
            Thread.sleep(200);

            // Step 2: Determine target coordinates
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int screenW = screenSize.width;
            int screenH = screenSize.height;

            int clickX;
            int clickY;
            if (targetX > 0 && targetY > 0) {
                clickX = (int) targetX;
                clickY = (int) targetY;
            } else {
                // Default fallback: centered modal checkbox position in 1920x1080 (around 42% X, 53% Y)
                clickX = (int) (screenW * 0.42);
                clickY = (int) (screenH * 0.53);
            }

            // Step 3: Human-like mouse movement and click
            Robot robot = new Robot();
            robot.setAutoDelay(10);

            // Move to nearby offset first (duration ~ 200ms)
            smoothMouseMove(robot, clickX - 40, clickY - 25, 200);
            Thread.sleep(50);

            // Move smoothly to exact checkbox (duration ~ 150ms)
            smoothMouseMove(robot, clickX, clickY, 150);
            Thread.sleep(100);

            // Physical OS hardware mouse click (isTrusted = true)
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            Thread.sleep(50);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            Thread.sleep(100);

            System.out.println(String.format("{\"status\":\"OK\",\"clickedX\":%d,\"clickedY\":%d,\"foregrounded\":%s}",
                    clickX, clickY, String.valueOf(brought).toLowerCase()));

        } catch (Exception e) {
            System.err.println("[ClickVerification] Error executing clicker: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        double tx = 0.0;
        double ty = 0.0;
        if (args.length >= 2) {
            try {
                tx = Double.parseDouble(args[0]);
                ty = Double.parseDouble(args[1]);
            } catch (NumberFormatException ignored) {}
        }
        clickVerificationBox(tx, ty);
    }
}
