package com.automation.agent.process;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Self-contained Win32 layer backend for the ConPTY sessions created by
 * {@link ProcessSession}. Holds the raw kernel32 bindings, the input/output
 * pipe stream adapters and the constants/logging for the per-terminal
 * pseudo-console. Creation and lifecycle orchestration live in
 * {@code ProcessSession}; Ctrl+C is a 0x03 byte written into the owning
 * session's own ConPTY input pipe (never a global
 * {@code GenerateConsoleCtrlEvent(CTRL_C_EVENT, 0)}).
 *
 * <p>All Win32 access is self-contained (no jna-platform dependency). Any
 * failure is reported to the caller, which surfaces PROCESS_START_FAILED —
 * there is no fallback mechanism by design.
 */
public final class ConPtyNative {

    private ConPtyNative() {
    }

    static final int EXTENDED_STARTUPINFO_PRESENT = 0x00080000;
    static final int STARTF_USESTDHANDLES = 0x00000100;
    static final long PROC_THREAD_ATTRIBUTE_PSEUDOCONSOLE = 0x00020016L;

    public interface Shell32 extends Library {
        Shell32 INSTANCE = Native.load("shell32", Shell32.class);

        boolean IsUserAnAdmin();
    }

    /** Minimal kernel32 mapping needed for ConPTY + handle I/O. */
    public interface K32 extends Library {
        K32 INSTANCE = Native.load("kernel32", K32.class);

        boolean CreatePipe(PointerByReference hRead, PointerByReference hWrite, SECURITY_ATTRIBUTES sa, int size);

        int CreatePseudoConsole(COORD.ByValue size, Pointer hIn, Pointer hOut, int flags, PointerByReference phPC);

        int ResizePseudoConsole(Pointer hPC, COORD.ByValue size);

        void ClosePseudoConsole(Pointer hPC);

        // PSIZE_T is pointer-width (8 bytes on Win64): pass an 8-byte Memory, not IntByReference.
        boolean InitializeProcThreadAttributeList(Pointer list, int attrCount, int flags, Pointer size);

        boolean UpdateProcThreadAttribute(Pointer list, int flags, long attr, Pointer value, long size,
                Pointer prev, Pointer prevSize);

        void DeleteProcThreadAttributeList(Pointer list);

        boolean CreateProcessW(WString app, Pointer cmdLine, Pointer procSec, Pointer threadSec,
                boolean inherit, int flags, Pointer env, WString cwd, STARTUPINFOEX si, PROCESS_INFORMATION pi);

        boolean ReadFile(Pointer h, byte[] buf, int toRead, IntByReference read, Pointer overlapped);

        boolean WriteFile(Pointer h, byte[] buf, int toRead, IntByReference written, Pointer overlapped);

        boolean FlushFileBuffers(Pointer h);

        boolean CloseHandle(Pointer h);

        boolean SetHandleInformation(Pointer hObject, int dwMask, int dwFlags);

        boolean TerminateProcess(Pointer hProcess, int code);

        int WaitForSingleObject(Pointer h, int ms);

        boolean GetExitCodeProcess(Pointer hProcess, IntByReference code);

        Pointer GetConsoleWindow();
    }

    @Structure.FieldOrder({"X", "Y"})
    public static class COORD extends Structure {
        public short X;
        public short Y;

        /** CreatePseudoConsole takes COORD by value — JNA needs the ByValue marker. */
        public static class ByValue extends COORD implements Structure.ByValue {
        }
    }

    @Structure.FieldOrder({"nLength", "lpSecurityDescriptor", "bInheritHandle"})
    public static class SECURITY_ATTRIBUTES extends Structure {
        public int nLength = size();
        public Pointer lpSecurityDescriptor = null;
        public boolean bInheritHandle = true;
    }

    @Structure.FieldOrder({"cb", "lpReserved", "lpDesktop", "lpTitle", "dwX", "dwY", "dwXSize", "dwYSize",
            "dwXCountChars", "dwYCountChars", "dwFillAttribute", "dwFlags", "wShowWindow", "cbReserved2",
            "lpReserved2", "hStdInput", "hStdOutput", "hStdError"})
    public static class STARTUPINFO extends Structure {
        public int cb = size();
        public Pointer lpReserved = null;
        public Pointer lpDesktop = null;
        public Pointer lpTitle = null;
        public int dwX;
        public int dwY;
        public int dwXSize;
        public int dwYSize;
        public int dwXCountChars;
        public int dwYCountChars;
        public int dwFillAttribute;
        public int dwFlags;
        public short wShowWindow;
        public short cbReserved2;
        public Pointer lpReserved2 = null;
        public Pointer hStdInput = null;
        public Pointer hStdOutput = null;
        public Pointer hStdError = null;
    }

    @Structure.FieldOrder({"startupInfo", "lpAttributeList"})
    public static class STARTUPINFOEX extends Structure {
        public STARTUPINFO startupInfo = new STARTUPINFO();
        public Pointer lpAttributeList = null;
    }

    @Structure.FieldOrder({"hProcess", "hThread", "dwProcessId", "dwThreadId"})
    public static class PROCESS_INFORMATION extends Structure {
        public Pointer hProcess = null;
        public Pointer hThread = null;
        public int dwProcessId;
        public int dwThreadId;
    }

    static final class HandleInputStream extends InputStream {
        private final Pointer handle;

        HandleInputStream(Pointer handle) {
            this.handle = handle;
        }

        @Override
        public int read() throws IOException {
            byte[] one = new byte[1];
            int n = read(one, 0, 1);
            return n <= 0 ? -1 : (one[0] & 0xFF);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (len == 0) {
                return 0;
            }
            byte[] tmp = new byte[Math.min(len, 8192)];
            IntByReference got = new IntByReference();
            boolean ok = K32.INSTANCE.ReadFile(handle, tmp, tmp.length, got, null);
            if (!ok) {
                int err = Native.getLastError();
                // 109 = ERROR_BROKEN_PIPE (pipe closed by child exit), 38 = ERROR_HANDLE_EOF
                if (err == 109 || err == 38 || err == 6) { // 6 = ERROR_INVALID_HANDLE
                    return -1;
                }
                return -1;
            }
            int count = got.getValue();
            if (count <= 0) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {
                }
                return 0;
            }
            System.arraycopy(tmp, 0, b, off, count);
            return count;
        }
    }

    static final class HandleOutputStream extends OutputStream {
        private final Pointer handle;

        HandleOutputStream(Pointer handle) {
            this.handle = handle;
        }

        @Override
        public synchronized void write(byte[] b, int off, int len) throws IOException {
            int writtenTotal = 0;
            while (writtenTotal < len) {
                byte[] tmp = new byte[len - writtenTotal];
                System.arraycopy(b, off + writtenTotal, tmp, 0, tmp.length);
                IntByReference wrote = new IntByReference();
                if (!K32.INSTANCE.WriteFile(handle, tmp, tmp.length, wrote, null)) {
                    throw new IOException("WriteFile to ConPTY failed err=" + com.sun.jna.Native.getLastError());
                }
                int n = wrote.getValue();
                if (n <= 0) {
                    throw new IOException("WriteFile wrote 0 bytes to ConPTY pipe");
                }
                writtenTotal += n;
            }
        }

        @Override
        public void write(int b) throws IOException {
            write(new byte[]{(byte) b}, 0, 1);
        }

        @Override
        public synchronized void flush() throws IOException {
            K32.INSTANCE.FlushFileBuffers(handle);
        }
    }

    /** Allocates a pointer-width SIZE_T buffer at value {@code v}. */
    static Memory pointerWidthMemory(long v) {
        Memory m = new Memory(Native.POINTER_SIZE);
        m.setLong(0, v);
        return m;
    }
}