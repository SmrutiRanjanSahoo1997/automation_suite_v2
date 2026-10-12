package com.automation.aiagent.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Chrome Monitor & Minimization Service.
 * Periodically checks Chrome process and window state using Win32 API via PowerShell.
 * Returns JSON {"running": true/false, "minimized": true/false}.
 */
public class ChromeMonitorService {

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private volatile boolean minimizable = false;
    private volatile boolean taskExecuting = false;
    private volatile String currentChromeState = "UNKNOWN";

    private static final String SCRIPT_STATE_CHECK =
            "if (-not ([System.Management.Automation.PSTypeName]'WinState').Type) {\n" +
            "    Add-Type 'using System; using System.Runtime.InteropServices; public class WinState { [DllImport(\"user32.dll\")] public static extern bool IsIconic(IntPtr hWnd); }';\n" +
            "}\n" +
            "$procs = Get-Process chrome -ErrorAction SilentlyContinue;\n" +
            "if (-not $procs) {\n" +
            "    '{\"running\":false,\"minimized\":false}'\n" +
            "} else {\n" +
            "    $minimized = $false;\n" +
            "    $hasWindow = $false;\n" +
            "    foreach ($p in $procs) {\n" +
            "        if ($p.MainWindowHandle -ne [IntPtr]::Zero) {\n" +
            "            $hasWindow = $true;\n" +
            "            if ([WinState]::IsIconic($p.MainWindowHandle)) {\n" +
            "                $minimized = $true;\n" +
            "            } else {\n" +
            "                $minimized = $false;\n" +
            "                break;\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "    if (-not $hasWindow) {\n" +
            "        '{\"running\":true,\"minimized\":false}'\n" +
            "    } elseif ($minimized) {\n" +
            "        '{\"running\":true,\"minimized\":true}'\n" +
            "    } else {\n" +
            "        '{\"running\":true,\"minimized\":false}'\n" +
            "    }\n" +
            "}\n";

    private static final String SCRIPT_MINIMIZE =
            "Add-Type '[DllImport(\"user32.dll\")] public static extern bool ShowWindowAsync(IntPtr hWnd, int nCmdShow);' -Name Win32Min -Namespace NativeMin -ErrorAction SilentlyContinue;\n" +
            "Get-Process chrome -ErrorAction SilentlyContinue | Where-Object {$_.MainWindowHandle -ne 0} | ForEach-Object {[NativeMin.Win32Min]::ShowWindowAsync($_.MainWindowHandle, 6)}\n";

    private static final String SCRIPT_RESTORE =
            "Add-Type 'using System; using System.Runtime.InteropServices; public class Win32Res { " +
            "[DllImport(\"user32.dll\")] public static extern bool ShowWindowAsync(IntPtr hWnd, int nCmdShow); " +
            "[DllImport(\"user32.dll\")] public static extern bool SetForegroundWindow(IntPtr hWnd); " +
            "[DllImport(\"user32.dll\")] public static extern bool BringWindowToTop(IntPtr hWnd); " +
            "[DllImport(\"user32.dll\")] public static extern void keybd_event(byte bVk, byte bScan, uint dwFlags, UIntPtr dwExtraInfo); " +
            "}' -ErrorAction SilentlyContinue;\n" +
            "[Win32Res]::keybd_event(0, 0, 0, [UIntPtr]::Zero);\n" +
            "[Win32Res]::keybd_event(0, 0, 2, [UIntPtr]::Zero);\n" +
            "Get-Process chrome -ErrorAction SilentlyContinue | Where-Object {$_.MainWindowHandle -ne 0} | ForEach-Object {\n" +
            "    [Win32Res]::ShowWindowAsync($_.MainWindowHandle, 9);\n" +
            "    [Win32Res]::SetForegroundWindow($_.MainWindowHandle);\n" +
            "    [Win32Res]::BringWindowToTop($_.MainWindowHandle);\n" +
            "}\n";

    public void start() {
        executor.scheduleAtFixedRate(this::checkAndUpdateChromeState, 0, 2, TimeUnit.SECONDS);
        System.out.println("[ChromeMonitorService] Chrome state monitor started.");
    }

    public void checkAndUpdateChromeState() {
        try {
            String jsonOutput = executePowerShellScript(SCRIPT_STATE_CHECK);
            if (jsonOutput == null || jsonOutput.isEmpty()) return;

            boolean running = jsonOutput.contains("\"running\":true");
            boolean minimized = jsonOutput.contains("\"minimized\":true");

            if (!running) {
                currentChromeState = "NOT RUNNING";
                System.out.println("[ChromeMonitorService] Chrome is NOT RUNNING. Auto-launching Chrome...");
                launchChrome();
            } else if (minimized) {
                currentChromeState = "RUNNING - MINIMIZED";
            } else {
                currentChromeState = "RUNNING - NOT MINIMIZED";
                if (minimizable && !taskExecuting) {
                    System.out.println("[ChromeMonitorService] Chrome running not minimized. Minimizing per minimizable=true...");
                    minimizeChrome();
                }
            }
        } catch (Exception e) {
            System.err.println("[ChromeMonitorService] Error during Chrome monitor cycle: " + e.getMessage());
        }
    }

    public void setTaskExecuting(boolean taskExecuting) {
        this.taskExecuting = taskExecuting;
        System.out.println("[ChromeMonitorService] Task executing flag set to: " + taskExecuting);
    }

    public boolean isTaskExecuting() {
        return taskExecuting;
    }

    public void launchChrome() {
        try {
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "start", "chrome.exe", "https://copilot.com/chat");
            pb.start();
            System.out.println("[ChromeMonitorService] Launched chrome.exe to https://copilot.com/chat");
        } catch (Exception e) {
            System.err.println("[ChromeMonitorService] Could not launch Chrome: " + e.getMessage());
        }
    }

    public void minimizeChrome() {
        try {
            executePowerShellScript(SCRIPT_MINIMIZE);
        } catch (Exception e) {
            System.err.println("[ChromeMonitorService] Error minimizing Chrome: " + e.getMessage());
        }
    }

    public void restoreChrome() {
        try {
            executePowerShellScript(SCRIPT_RESTORE);
        } catch (Exception e) {
            System.err.println("[ChromeMonitorService] Error restoring Chrome: " + e.getMessage());
        }
    }

    public void setMinimizable(boolean minimizable) {
        this.minimizable = minimizable;
        System.out.println("[ChromeMonitorService] Updated minimizable flag to: " + minimizable);
        if (!minimizable) {
            restoreChrome();
        } else {
            minimizeChrome();
        }
    }

    public boolean isMinimizable() {
        return minimizable;
    }

    public String getCurrentChromeState() {
        return currentChromeState;
    }

    private String executePowerShellScript(String script) {
        try {
            byte[] bytes = script.getBytes(StandardCharsets.UTF_16LE);
            String encoded = Base64.getEncoder().encodeToString(bytes);

            ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-EncodedCommand", encoded);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                        return trimmed;
                    }
                    sb.append(line).append("\n");
                }
            }
            process.waitFor();
            return sb.toString().trim();
        } catch (Exception e) {
            return null;
        }
    }

    public void stop() {
        executor.shutdown();
    }
}
