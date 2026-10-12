import sys
import time
import ctypes
import pyautogui

pyautogui.FAILSAFE = False

user32 = ctypes.windll.user32

def bring_chrome_to_foreground():
    """Finds Chrome/Copilot top-level window and brings it to foreground."""
    target_hwnds = []

    def enum_cb(hwnd, lparam):
        if user32.IsWindowVisible(hwnd):
            length = user32.GetWindowTextLengthW(hwnd)
            if length > 0:
                buff = ctypes.create_unicode_buffer(length + 1)
                user32.GetWindowTextW(hwnd, buff, length + 1)
                title = buff.value.lower()
                if "copilot" in title or "chrome" in title:
                    target_hwnds.append(hwnd)
        return True

    ENUMPROC = ctypes.WINFUNCTYPE(ctypes.c_bool, ctypes.c_void_p, ctypes.c_void_p)
    user32.EnumWindows(ENUMPROC(enum_cb), 0)

    for hwnd in target_hwnds:
        try:
            # Win32 unlock trick
            user32.keybd_event(0, 0, 0, 0)
            user32.keybd_event(0, 0, 2, 0)
            user32.ShowWindowAsync(hwnd, 9)  # SW_RESTORE
            user32.ShowWindowAsync(hwnd, 3)  # SW_MAXIMIZE
            user32.BringWindowToTop(hwnd)
            user32.SetForegroundWindow(hwnd)
            time.sleep(0.15)
            return True
        except Exception as e:
            print(f"[click_verification] Warning restoring hwnd {hwnd}: {e}", file=sys.stderr)

    return False

def click_verification_box(target_x=None, target_y=None):
    # Step 1: Ensure Chrome is foregrounded
    brought = bring_chrome_to_foreground()
    time.sleep(0.2)

    # Step 2: Determine target coordinates
    screen_w, screen_h = pyautogui.size()
    if target_x is not None and target_y is not None and target_x > 0 and target_y > 0:
        click_x = int(target_x)
        click_y = int(target_y)
    else:
        # Default fallback: centered modal checkbox position in 1920x1080 (around 42% X, 53% Y)
        click_x = int(screen_w * 0.42)
        click_y = int(screen_h * 0.53)

    # Step 3: Human-like mouse movement and click
    # Move to nearby offset first
    pyautogui.moveTo(click_x - 40, click_y - 25, duration=0.2)
    time.sleep(0.05)
    # Move smoothly to exact checkbox
    pyautogui.moveTo(click_x, click_y, duration=0.15)
    time.sleep(0.1)
    # Physical OS hardware mouse click (isTrusted = true)
    pyautogui.click(click_x, click_y)
    time.sleep(0.1)

    print(f'{{"status":"OK","clickedX":{click_x},"clickedY":{click_y},"foregrounded":{str(brought).lower()}}}')

if __name__ == "__main__":
    tx = None
    ty = None
    if len(sys.argv) >= 3:
        try:
            tx = float(sys.argv[1])
            ty = float(sys.argv[2])
        except ValueError:
            pass

    click_verification_box(tx, ty)
