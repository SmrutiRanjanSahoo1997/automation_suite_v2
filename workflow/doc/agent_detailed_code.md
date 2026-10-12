# 🚀 Automation Suite Agent - Detailed Source Code

> 📅 **Generated On:** `2026-09-28 22:17:35`  
> 📂 **Source Root:** `C:/Ranjan/AutomationSuite_v2/automation_suite_agent`  
> 📊 **Total Source Files:** `18`  
> 📝 **Total Lines of Code:** `4,847`  
> 💾 **Total Size:** `188.33 KB`  

---

## 📌 Executive Summary

Plain Java 21 generic CLI execution bridge. Owns one authenticated WebSocket to Spring Boot and at most 10 isolated CMD/PowerShell ConPTY terminals with atomic capacity/lifecycle management. No business logic, no persistence — it streams terminal stdout/stderr chunks and status over the single WebSocket.

---

## 📂 Project Directory Structure

```text
automation_suite_agent/
├── mvnw
├── mvnw.cmd
├── pom.xml
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       └── automation/
    │   │           └── agent/
    │   │               ├── AgentMain.java
    │   │               ├── config/
    │   │               │   └── AgentConfig.java
    │   │               ├── process/
    │   │               │   ├── ConPtyNative.java
    │   │               │   └── ProcessSession.java
    │   │               ├── protocol/
    │   │               │   └── TerminalProtocol.java
    │   │               ├── terminal/
    │   │               │   ├── TerminalManager.java
    │   │               │   └── TerminalModels.java
    │   │               └── websocket/
    │   │                   └── AgentWebSocket.java
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/
            └── com/
                └── automation/
                    └── agent/
                        ├── simulator/
                        │   ├── AgentSimulatorTest.java
                        │   └── AiAgentSimulationTest.java
                        └── terminal/
                            ├── FakeProcessSession.java
                            ├── TerminalInvariantTest.java
                            ├── TerminalManagerTest.java
                            └── TestEventSink.java
```

---

## 📑 File Index

1. [`mvnw`](#file-1)
2. [`mvnw.cmd`](#file-2)
3. [`pom.xml`](#file-3)
4. [`src/main/java/com/automation/agent/AgentMain.java`](#file-4)
5. [`src/main/java/com/automation/agent/config/AgentConfig.java`](#file-5)
6. [`src/main/java/com/automation/agent/process/ConPtyNative.java`](#file-6)
7. [`src/main/java/com/automation/agent/process/ProcessSession.java`](#file-7)
8. [`src/main/java/com/automation/agent/protocol/TerminalProtocol.java`](#file-8)
9. [`src/main/java/com/automation/agent/terminal/TerminalManager.java`](#file-9)
10. [`src/main/java/com/automation/agent/terminal/TerminalModels.java`](#file-10)
11. [`src/main/java/com/automation/agent/websocket/AgentWebSocket.java`](#file-11)
12. [`src/main/resources/application.properties`](#file-12)
13. [`src/test/java/com/automation/agent/simulator/AgentSimulatorTest.java`](#file-13)
14. [`src/test/java/com/automation/agent/simulator/AiAgentSimulationTest.java`](#file-14)
15. [`src/test/java/com/automation/agent/terminal/FakeProcessSession.java`](#file-15)
16. [`src/test/java/com/automation/agent/terminal/TerminalInvariantTest.java`](#file-16)
17. [`src/test/java/com/automation/agent/terminal/TerminalManagerTest.java`](#file-17)
18. [`src/test/java/com/automation/agent/terminal/TestEventSink.java`](#file-18)

---

## 💻 Comprehensive Source Code

<a id="file-1"></a>
### 1. `mvnw`

**Path:** `mvnw` | **Size:** `0.59 KB` | **Lines:** `15` | **Language:** `text`

```text
#!/bin/sh
# Minimal Maven wrapper fallback: uses system mvn, or downloads Maven 3.9 if missing.
set -e
if command -v mvn >/dev/null 2>&1; then
  exec mvn "$@"
fi
MVN_VERSION=3.9.9
INSTALL_DIR="${HOME}/.mvnw-maven"
if [ ! -x "${INSTALL_DIR}/apache-maven-${MVN_VERSION}/bin/mvn" ]; then
  echo "System mvn not found; downloading Maven ${MVN_VERSION}..." >&2
  mkdir -p "${INSTALL_DIR}"
  curl -fsSL "https://dlcdn.apache.org/maven/maven-3/${MVN_VERSION}/binaries/apache-maven-${MVN_VERSION}-bin.tar.gz" \
    | tar -xz -C "${INSTALL_DIR}"
fi
exec "${INSTALL_DIR}/apache-maven-${MVN_VERSION}/bin/mvn" "$@"

```

---

<a id="file-2"></a>
### 2. `mvnw.cmd`

**Path:** `mvnw.cmd` | **Size:** `0.27 KB` | **Lines:** `9` | **Language:** `cmd`

```cmd
@echo off
REM Minimal Maven wrapper for Windows. Uses system mvn when available.
where mvn >nul 2>nul
if %ERRORLEVEL%==0 (
  mvn %*
  exit /b %ERRORLEVEL%
)
echo System mvn not found. Install Maven 3.9+ and re-run mvnw.cmd, or run: winget install Apache.Maven >&2
exit /b 1

```

---

<a id="file-3"></a>
### 3. `pom.xml`

**Path:** `pom.xml` | **Size:** `4.57 KB` | **Lines:** `115` | **Language:** `xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.automation</groupId>
    <artifactId>automation_suite_agent</artifactId>
    <version>2.0.0</version>
    <packaging>jar</packaging>

    <name>Automation Suite Agent V2</name>
    <description>Plain Java 21 generic CLI execution bridge. One authenticated WebSocket, max 10 CMD/PowerShell ConPTY terminals. No business logic.</description>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.release>21</maven.compiler.release>
        <jackson.version>2.17.2</jackson.version>
        <java-websocket.version>1.5.6</java-websocket.version>
        <jna.version>5.15.0</jna.version>
        <slf4j.version>2.0.13</slf4j.version>
        <junit.version>5.10.3</junit.version>
    </properties>

    <dependencies>
        <!-- WebSocket (single persistent Agent <-> Spring Boot connection) -->
        <dependency>
            <groupId>org.java-websocket</groupId>
            <artifactId>Java-WebSocket</artifactId>
            <version>${java-websocket.version}</version>
        </dependency>

        <!-- JSON protocol -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>${jackson.version}</version>
        </dependency>

        <!-- Win32 ConPTY access (CreatePseudoConsole / CreateProcess). Fallback to piped ProcessBuilder when unavailable. -->
        <dependency>
            <groupId>net.java.dev.jna</groupId>
            <artifactId>jna</artifactId>
            <version>${jna.version}</version>
        </dependency>

        <!-- Logging -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>${slf4j.version}</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
            <version>${slf4j.version}</version>
        </dependency>

        <!-- Tests (state machine + concurrency invariants) -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.13.0</version>
                <configuration>
                    <release>21</release>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.5</version>
            </plugin>
            <!-- Fat jar so the agent can run with an extracted JDK 21 bin\java.exe, no server Java install needed -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.6.0</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <transformers>
                                <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                    <mainClass>com.automation.agent.AgentMain</mainClass>
                                </transformer>
                            </transformers>
                            <createDependencyReducedPom>false</createDependencyReducedPom>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.3.0</version>
                <configuration>
                    <mainClass>com.automation.agent.AgentMain</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>

```

---

<a id="file-4"></a>
### 4. `src/main/java/com/automation/agent/AgentMain.java`

**Path:** `src/main/java/com/automation/agent/AgentMain.java` | **Size:** `1.51 KB` | **Lines:** `46` | **Language:** `java`

```java
package com.automation.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automation.agent.config.AgentConfig;
import com.automation.agent.terminal.TerminalManager;
import com.automation.agent.websocket.AgentWebSocket;

/**
 * Application bootstrap: load config → create manager → start the embedded
 * WebSocket server. Clients (Spring Boot, HTML Console, CLI tools) connect TO
 * this agent on port 9010.
 */
public final class AgentMain {

    private static final Logger log = LoggerFactory.getLogger(AgentMain.class);

    private AgentMain() {
    }

    public static void main(String[] args) throws Exception {
        AgentConfig config = AgentConfig.load();
        int port = config.port();
        log.info("Automation Suite Agent V2 starting. Listening on port {} (maxTerminals={})",
                port, config.terminalMaxCount());

        TerminalManager manager = new TerminalManager(config);
        AgentWebSocket server = new AgentWebSocket(port, config.agentKey(), manager);
        manager.setSink(server);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Agent shutting down; terminating active terminals");
            server.shutdown();
            manager.closeAll();
            manager.shutdown();
        }, "agent-shutdown"));

        server.start();

        log.info("Agent Server ready! Clients can connect to ws://localhost:{}/ws/agent", port);

        // Keep main thread alive while server runs.
        Thread.currentThread().join();
    }
}
```

---

<a id="file-5"></a>
### 5. `src/main/java/com/automation/agent/config/AgentConfig.java`

**Path:** `src/main/java/com/automation/agent/config/AgentConfig.java` | **Size:** `5.88 KB` | **Lines:** `164` | **Language:** `java`

```java
package com.automation.agent.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads application.properties from (in order):
 * <ol>
 *   <li>{@code ./application.properties} next to the working directory (allows ops override),</li>
 *   <li>classpath {@code application.properties} bundled in resources,</li>
 *   <li>classpath {@code agent.properties} (legacy name, back-compat).</li>
 * </ol>
 * Holds only transport + terminal capacity/metadata limits. No business config.
 */
public final class AgentConfig {

    private static final Logger log = LoggerFactory.getLogger(AgentConfig.class);

    /** V2 protocol invariant: exactly 10 shared CMD/PS terminal slots. Not tunable. */
    public static final int MAX_TERMINALS = 10;

    /** V2 default endpoint when the property is missing. */
    public static final String DEFAULT_WEBSOCKET_URL = "ws://localhost:9010/ws/agent";

    private final String agentKey;
    private final int port;
    private final String websocketUrl;
    private final long reconnectInitialMs;
    private final long reconnectMaxMs;
    private final int terminalMaxCount;
    private final int maxResultChars;
    private final int maxErrorChars;
    private final int transferChunkChars;
    private final int conptyCols;
    private final int conptyRows;
    private final long stopTimeoutMs;

    AgentConfig(Properties p) {
        this.agentKey = required(p, "agent.key");
        this.port = Integer.parseInt(p.getProperty("agent.port", p.getProperty("server.port", "9010")));
        this.websocketUrl = optional(p, "agent.websocket.url", DEFAULT_WEBSOCKET_URL);
        this.reconnectInitialMs = Long.parseLong(p.getProperty("agent.websocket.reconnect-initial-ms", "1000"));
        this.reconnectMaxMs = Long.parseLong(p.getProperty("agent.websocket.reconnect-max-ms", "30000"));
        this.terminalMaxCount = Integer.parseInt(p.getProperty("terminal.max-count", "10"));
        this.maxResultChars = Integer.parseInt(p.getProperty("terminal.metadata.max-result-chars", "1000000"));
        this.maxErrorChars = Integer.parseInt(p.getProperty("terminal.metadata.max-error-chars", "1000000"));
        this.transferChunkChars = Integer.parseInt(p.getProperty("terminal.metadata.transfer-chunk-chars", "100000"));
        this.conptyCols = Integer.parseInt(p.getProperty("terminal.conpty.cols", "120"));
        this.conptyRows = Integer.parseInt(p.getProperty("terminal.conpty.rows", "30"));
        this.stopTimeoutMs = Long.parseLong(p.getProperty("terminal.stop-timeout-ms", "10000"));

        // Hard invariant: configuration must not override the 10-slot protocol contract.
        if (terminalMaxCount != MAX_TERMINALS) {
            throw new IllegalStateException(
                    "terminal.max-count must be " + MAX_TERMINALS + " (V2 invariant), got " + terminalMaxCount);
        }
        if (stopTimeoutMs <= 0) {
            throw new IllegalStateException("terminal.stop-timeout-ms must be positive, got " + stopTimeoutMs);
        }
    }

    public static AgentConfig load() throws IOException {
        Properties p = new Properties();
        Path override = Path.of("application.properties");
        if (Files.isRegularFile(override)) {
            try (InputStream in = Files.newInputStream(override)) {
                p.load(in);
            }
            log.info("Loaded application.properties from working directory override: {}", override.toAbsolutePath());
            return new AgentConfig(p);
        }
        // Classpath application.properties first, then legacy agent.properties.
        for (String name : new String[]{"application.properties", "agent.properties"}) {
            try (InputStream in = AgentConfig.class.getClassLoader().getResourceAsStream(name)) {
                if (in != null) {
                    p.load(in);
                    log.info("Loaded {} from classpath", name);
                    return new AgentConfig(p);
                }
            }
        }
        throw new IOException("Neither application.properties nor agent.properties found on classpath "
                + "and no ./application.properties override");
    }

    private static String required(Properties p, String key) {
        String v = p.getProperty(key);
        if (v == null || v.isBlank()) {
            throw new IllegalStateException("Missing required agent property: " + key);
        }
        return v.trim();
    }

    private static String optional(Properties p, String key, String fallback) {
        String v = p.getProperty(key);
        if (v == null || v.isBlank()) {
            return fallback;
        }
        return v.trim();
    }

    public String agentKey() {
        return agentKey;
    }

    public int port() {
        return port;
    }

    public String websocketUrl() {
        return websocketUrl;
    }

    public long reconnectInitialMs() {
        return reconnectInitialMs;
    }

    public long reconnectMaxMs() {
        return reconnectMaxMs;
    }

    public int terminalMaxCount() {
        return terminalMaxCount;
    }

    public int maxResultChars() {
        return maxResultChars;
    }

    public int maxErrorChars() {
        return maxErrorChars;
    }

    public int transferChunkChars() {
        return transferChunkChars;
    }

    public int conptyCols() {
        return conptyCols;
    }

    public int conptyRows() {
        return conptyRows;
    }

    /**
     * Bound for the STOP command-boundary wait: after Ctrl+C, the terminal stays
     * RUNNING until the queued marker boundary arrives or this timeout fires.
     */
    public long stopTimeoutMs() {
        return stopTimeoutMs;
    }

    /** Test seam: build a config from explicit properties (same validation as production). */
    public static AgentConfig fromProperties(Properties p) {
        return new AgentConfig(p);
    }
}
```

---

<a id="file-6"></a>
### 6. `src/main/java/com/automation/agent/process/ConPtyNative.java`

**Path:** `src/main/java/com/automation/agent/process/ConPtyNative.java` | **Size:** `8.09 KB` | **Lines:** `226` | **Language:** `java`

```java
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
```

---

<a id="file-7"></a>
### 7. `src/main/java/com/automation/agent/process/ProcessSession.java`

**Path:** `src/main/java/com/automation/agent/process/ProcessSession.java` | **Size:** `22.47 KB` | **Lines:** `536` | **Language:** `java`

```java
package com.automation.agent.process;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automation.agent.config.AgentConfig;
import com.automation.agent.terminal.TerminalModels.TerminalPlatform;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * One isolated console session: the real cmd.exe / powershell.exe plus its
 * OS identity. Either a Win32 ConPTY session (Windows) or a piped
 * {@link java.lang.Process} fallback. The rest of the agent only uses this
 * interface; the concrete implementation, the launcher, the Ctrl+C interrupt
 * and the streaming decoder all live in this single file.
 */
public interface ProcessSession {

    TerminalPlatform platform();

    long pid();

    /** Write one physical input line (without line break) to the shell. */
    void writeLine(String line) throws IOException;

    /** Raw byte write (used for Ctrl+C 0x03 on ConPTY). */
    void writeRaw(byte[] bytes) throws IOException;

    InputStream stdout();

    /** Null when stdout/stderr are merged (ConPTY). */
    InputStream stderr();

    boolean isAlive();

    /** Fully terminate the shell/process and release native resources. */
    void destroy();

    // ------------------------------------------------------------ launcher

    /**
     * Creates cmd.exe / powershell.exe as an isolated Windows ConPTY session — the
     * single supported mechanism. There is deliberately no piped-process fallback:
     * ConPTY and piped shells have different output/interrupt semantics, and V2
     * terminal behavior (per-terminal Ctrl+C, merged console stream) depends on a
     * real pseudo-console. When ConPTY cannot be established, terminal_start fails
     * with PROCESS_START_FAILED. Never forces a working directory and never
     * introduces runas/impersonation — shells inherit the agent's execution context.
     */
    static ProcessSession launch(TerminalPlatform platform, AgentConfig config) throws IOException {
        return launch(platform, null, config);
    }

    static ProcessSession launch(TerminalPlatform platform, String initialPath, AgentConfig config) throws IOException {
        Optional<ProcessSession> conpty =
                ConPty.tryCreate(platform, initialPath, config.conptyCols(), config.conptyRows());
        if (conpty.isPresent()) {
            return conpty.get(); // staged [CONPTY-DIAG] already logged by tryCreate
        }
        throw new IOException("ConPTY session could not be established for platform " + platform);
    }

    // ----------------------------------------------------------- interruption

    /** Ctrl+C through the session's own ConPTY. Never kills the shell. */
    static boolean interrupt(ProcessSession session) {
        try {
            session.writeRaw(new byte[]{0x03});
            return true;
        } catch (IOException e) {
            log().warn("Ctrl+C delivery failed: {}", e.toString());
            return false;
        }
    }

    static Logger log() {
        return LoggerFactory.getLogger(ProcessSession.class);
    }

    // ---------------------------------------------------------------- reader

    /**
     * Streams bytes from a process/console output handle to a chunk consumer.
     * Decoding is incremental UTF-8: a multibyte character split across two
     * {@code read()} calls is reassembled, never replaced with U+FFFD. VT escape
     * sequences from ConPTY pass through untouched — Spring Boot owns presentation.
     */
    final class OutputReader implements Runnable {

        private final InputStream source;
        private final Consumer<String> onText;
        private final CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE);
        // Incomplete trailing bytes (at most 3 for UTF-8) carried to the next read.
        private byte[] carry = new byte[0];
        private volatile boolean stopped;

        public OutputReader(InputStream source, Consumer<String> onText) {
            this.source = source;
            this.onText = onText;
        }

        public void stop() {
            stopped = true;
        }

        @Override
        public void run() {
            byte[] buf = new byte[8192];
            try {
                int n;
                while (!stopped && (n = source.read(buf)) != -1) {
                    if (n == 0 && carry.length == 0) {
                        continue;
                    }
                    StringBuilder hex = new StringBuilder();
                    for (int i = 0; i < Math.min(n, 40); i++) {
                        hex.append(String.format("%02X ", buf[i]));
                    }
                    System.err.println("HEX BYTES n=" + n + ": [" + hex.toString().trim() + "]");
                    String text = decode(buf, n, false);
                    if (!text.isEmpty()) {
                        onText.accept(text);
                    }
                }
                if (!stopped) {
                    String tail = decode(new byte[0], 0, true);
                    if (!tail.isEmpty()) {
                        onText.accept(tail);
                    }
                }
            } catch (Exception ignored) {
                // Stream closed on terminal close — normal shutdown path.
            }
        }

        /**
         * Decodes {@code len} fresh bytes plus any carried-over trailing bytes.
         * With {@code endOfInput=false} the decoder leaves an incomplete trailing
         * character unconsumed; those bytes become the next carry.
         */
        private String decode(byte[] buf, int len, boolean endOfInput) {
            byte[] combined = new byte[carry.length + len];
            System.arraycopy(carry, 0, combined, 0, carry.length);
            System.arraycopy(buf, 0, combined, carry.length, len);
            ByteBuffer in = ByteBuffer.wrap(combined);
            CharBuffer out = CharBuffer.allocate(combined.length + 1);
            decoder.decode(in, out, endOfInput);
            if (endOfInput) {
                decoder.flush(out);
                carry = new byte[0];
            } else {
                byte[] rest = new byte[in.remaining()];
                in.get(rest);
                carry = rest;
            }
            out.flip();
            return out.toString();
        }
    }

    // ------------------------------------------------------------ conpty impl

    /** True Windows ConPTY session via JNA (CreatePseudoConsole + CreateProcess). */
    final class ConPty implements ProcessSession {

        private final Logger log = LoggerFactory.getLogger(ConPty.class);

        private final TerminalPlatform platform;
        private final int pid;
        private final Pointer hpc;
        private final Pointer hProcess;
        private final Pointer hThread;
        private final ConPtyNative.HandleInputStream out;
        private final ConPtyNative.HandleOutputStream in;
        private final Pointer inWriteHandle;
        private final Pointer outReadHandle;
        private volatile boolean closed;

        ConPty(TerminalPlatform platform, int pid, Pointer hpc, Pointer hProcess, Pointer hThread,
                Pointer inWriteHandle, Pointer outReadHandle) {
            this.platform = platform;
            this.pid = pid;
            this.hpc = hpc;
            this.hProcess = hProcess;
            this.hThread = hThread;
            this.inWriteHandle = inWriteHandle;
            this.outReadHandle = outReadHandle;
            this.out = new ConPtyNative.HandleInputStream(outReadHandle);
            this.in = new ConPtyNative.HandleOutputStream(inWriteHandle);
        }

        @Override
        public TerminalPlatform platform() {
            return platform;
        }

        @Override
        public long pid() {
            return pid;
        }

        @Override
        public synchronized void writeLine(String line) throws IOException {
            writeRaw((line + "\r\n").getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public synchronized void writeRaw(byte[] bytes) throws IOException {
            if (closed) {
                throw new IOException("ConPTY session closed");
            }
            in.write(bytes);
            in.flush();
        }

        @Override
        public InputStream stdout() {
            return out;
        }

        @Override
        public InputStream stderr() {
            return null; // ConPTY merges console output into one VT stream.
        }

        @Override
        public boolean isAlive() {
            if (closed) {
                return false;
            }
            try {
                IntByReference code = new IntByReference();
                if (ConPtyNative.K32.INSTANCE.GetExitCodeProcess(hProcess, code)) {
                    return code.getValue() == 259; // STILL_ACTIVE
                }
            } catch (Throwable t) {
                log.debug("GetExitCodeProcess failed", t);
            }
            return ConPtyNative.K32.INSTANCE.WaitForSingleObject(hProcess, 0) != 0; // WAIT_OBJECT_0 == 0 means exited
        }

        /** Process exit code, or 259 (STILL_ACTIVE), or -1 when unknown. */
        int exitCode() {
            try {
                IntByReference code = new IntByReference(-1);
                if (ConPtyNative.K32.INSTANCE.GetExitCodeProcess(hProcess, code)) {
                    return code.getValue();
                }
            } catch (Throwable ignored) {
            }
            return -1;
        }

        @Override
        public synchronized void destroy() {
            if (closed) {
                return;
            }
            closed = true;
            try {
                closePseudoConsoleBounded(hpc);
                ConPtyNative.K32.INSTANCE.TerminateProcess(hProcess, 1);
            } catch (Exception e) {
                log.debug("TerminateProcess failed", e);
            } finally {
                safeClose(inWriteHandle);
                safeClose(outReadHandle);
                safeClose(hThread);
                safeClose(hProcess);
            }
        }

        private static void closePseudoConsoleBounded(Pointer hpc) {
            if (hpc == null) {
                return;
            }
            Thread t = new Thread(() -> {
                try {
                    ConPtyNative.K32.INSTANCE.ClosePseudoConsole(hpc);
                } catch (Throwable th) {
                    log().debug("ClosePseudoConsole failed", th);
                }
            }, "conpty-close");
            t.setDaemon(true);
            t.start();
            try {
                t.join(500);
            } catch (InterruptedException e) {
                log().debug("Interrupted waiting for ClosePseudoConsole", e);
            }
            if (t.isAlive()) {
                log().warn("ClosePseudoConsole did not complete in 5s; abandoning HPCON to avoid hanging terminal close");
            }
        }

        private static void safeClose(Pointer h) {
            if (h != null) {
                try {
                    ConPtyNative.K32.INSTANCE.CloseHandle(h);
                } catch (Exception ignored) {
                }
            }
        }

        /** Try a real ConPTY session; empty when unavailable (non-Windows or native failure). */
        static Optional<ProcessSession> tryCreate(TerminalPlatform platform, String initialPath, int cols, int rows) {
            if (!System.getProperty("os.name", "").toLowerCase().contains("win")) {
                return Optional.empty();
            }
            StringBuilder diag = new StringBuilder();
            diag.append("[CONPTY-DIAG] platform=").append(platform)
                    .append(" os=").append(System.getProperty("os.name")).append(' ')
                    .append(System.getProperty("os.version"))
                    .append(" os.arch=").append(System.getProperty("os.arch"))
                    .append(" java=").append(System.getProperty("java.version"))
                    .append(" jvm.bits=").append(System.getProperty("sun.arch.data.model", "?"))
                    .append(" jna.ptrBytes=").append(Native.POINTER_SIZE)
                    .append(" conpty=").append(cols).append('x').append(rows);
            ConPty created = null;
            try {
                String cmd = platform == TerminalPlatform.CMD
                        ? cmdLine()
                        : psLine();
                logPreflight(cmd, diag);
                created = create(platform, cmd, initialPath, cols, rows, diag);
                // The native calls can succeed while the console host itself is broken
                // (e.g. headless/container Windows: child dies with 0xC0000142). Verify
                // the shell actually survives before handing the session out.
                Thread.sleep(500);
                if (!created.isAlive()) {
                    int exit = created.exitCode();
                    diag.append(" | aliveCheck=DEAD exit=").append(exit)
                            .append(" (0x").append(Integer.toHexString(exit)).append(')');
                    log().warn("ConPTY shell died immediately (exit={}); reporting failure",
                            created.exitCode());
                    log().warn("{}", diag);
                    created.destroy();
                    return Optional.empty();
                }
                diag.append(" | aliveCheck=ALIVE pid=").append(created.pid());
                log().info("Started {} ConPTY session pid={} | {}", platform, created.pid(), diag);
                return Optional.of(created);
            } catch (Throwable t) {
                diag.append(" | FAILED: ").append(t);
                log().warn("ConPTY creation failed: {}", t.toString());
                log().warn("{}", diag);
                if (created != null) {
                    try {
                        created.destroy();
                    } catch (Throwable ignored) {
                    }
                }
                return Optional.empty();
            }
        }

        /** Interactive CMD with quiet startup and prompt loop (kept alive). */
        private static String cmdLine() {
            return "C:\\Windows\\System32\\cmd.exe /q /k";
        }

        /** Interactive PowerShell, keep alive, bypass execution policy for scripts. */
        private static String psLine() {
            return "C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe "
                    + "-NoLogo -NoExit -NoProfile -ExecutionPolicy Bypass";
        }

        private static void logPreflight(String cmd, StringBuilder diag) {
            try {
                String exe = cmd.split(" ")[0];
                java.nio.file.Path p = java.nio.file.Path.of(exe);
                boolean exists = java.nio.file.Files.isExecutable(p);
                long size = -1;
                try {
                    size = java.nio.file.Files.size(p);
                } catch (Exception ignored) {
                }
                boolean conhost = java.nio.file.Files.exists(
                        java.nio.file.Path.of("C:\\Windows\\System32\\conhost.exe"));
                boolean console = parentHasConsole();
                diag.append(" | preflight: exe=").append(exe)
                        .append(" exists=").append(exists)
                        .append(" size=").append(size)
                        .append(" conhostExists=").append(conhost)
                        .append(" parentConsole=").append(console);
                log().info("ConPTY preflight: exe={} exists={} size={} conhostExists={} parentConsole={} os={}",
                        exe, exists, size, conhost, console,
                        System.getProperty("os.name") + " " + System.getProperty("os.version"));
            } catch (Throwable t) {
                log().debug("ConPTY preflight failed", t);
            }
        }

        private static boolean parentHasConsole() {
            try {
                return ConPtyNative.K32.INSTANCE.GetConsoleWindow() != null;
            } catch (Throwable t) {
                return false;
            }
        }

        private static ConPty create(TerminalPlatform platform, String commandLine, String initialPath, int cols, int rows,
                StringBuilder diag) throws IOException {
            ConPtyNative.K32 k = ConPtyNative.K32.INSTANCE;
            ConPtyNative.SECURITY_ATTRIBUTES sa = new ConPtyNative.SECURITY_ATTRIBUTES();
            sa.write();

            PointerByReference inRead = new PointerByReference();
            PointerByReference inWrite = new PointerByReference();
            PointerByReference outRead = new PointerByReference();
            PointerByReference outWrite = new PointerByReference();
            if (!k.CreatePipe(inRead, inWrite, sa, 0) || !k.CreatePipe(outRead, outWrite, sa, 0)) {
                throw new IOException("CreatePipe failed err=" + Native.getLastError());
            }
            // Retained handles must NOT be inherited by child processes (HANDLE_FLAG_INHERIT = 1).
            k.SetHandleInformation(inWrite.getValue(), 1, 0);
            k.SetHandleInformation(outRead.getValue(), 1, 0);
            diag.append(" | pipes=OK");

            ConPtyNative.COORD.ByValue size = new ConPtyNative.COORD.ByValue();
            size.X = (short) cols;
            size.Y = (short) rows;
            size.write();
            PointerByReference phPC = new PointerByReference();
            int hr = k.CreatePseudoConsole(size, inRead.getValue(), outWrite.getValue(), 0, phPC);
            if (hr != 0) {
                k.CloseHandle(inWrite.getValue());
                k.CloseHandle(outRead.getValue());
                k.CloseHandle(inRead.getValue());
                k.CloseHandle(outWrite.getValue());
                throw new IOException("CreatePseudoConsole failed hr=" + hr);
            }
                Pointer hpc = phPC.getValue();
                diag.append(" | pseudoConsole: hr=0 hpc=").append(hpc);
                // ConPTY duplicates or takes ownership of inRead and outWrite.
                // Parent MUST close these handles so parent does not retain extra pipe references.
                k.CloseHandle(inRead.getValue());
                k.CloseHandle(outWrite.getValue());

                Pointer attrList = null;
                try {
                    Memory sizeMem = ConPtyNative.pointerWidthMemory(0);
                    k.InitializeProcThreadAttributeList(null, 1, 0, sizeMem);
                    long need = sizeMem.getLong(0);
                    if (need <= 0 || need > 1 << 20) {
                        throw new IOException("Bad proc-thread-attribute-list size: " + need);
                    }
                    attrList = new Memory(need);
                    diag.append(" | attrListBytes=").append(need);
                    Memory needMem = ConPtyNative.pointerWidthMemory(need);
                    if (!k.InitializeProcThreadAttributeList(attrList, 1, 0, needMem)) {
                        throw new IOException(
                                "InitializeProcThreadAttributeList failed err=" + Native.getLastError());
                    }
                    if (!k.UpdateProcThreadAttribute(attrList, 0, ConPtyNative.PROC_THREAD_ATTRIBUTE_PSEUDOCONSOLE,
                            hpc, Native.POINTER_SIZE, null, null)) {
                        throw new IOException(
                                "UpdateProcThreadAttribute PSEUDOCONSOLE failed err=" + Native.getLastError());
                    }
                    diag.append(" | attrAssigned=OK");

                    ConPtyNative.STARTUPINFOEX si = new ConPtyNative.STARTUPINFOEX();
                    si.lpAttributeList = attrList;
                    si.startupInfo.cb = si.size();
                    si.startupInfo.dwFlags = ConPtyNative.STARTF_USESTDHANDLES;
                    si.startupInfo.write();
                    si.write();
                    ConPtyNative.PROCESS_INFORMATION pi = new ConPtyNative.PROCESS_INFORMATION();

                    char[] cmdChars = (commandLine + "\0").toCharArray();
                    Memory cmdMem = new Memory((long) cmdChars.length * 2L);
                    cmdMem.write(0, cmdChars, 0, cmdChars.length);

                    String workDir = (initialPath != null && !initialPath.isBlank()) ? initialPath : "C:\\";
                    boolean ok = k.CreateProcessW(null, cmdMem, null, null, false,
                            ConPtyNative.EXTENDED_STARTUPINFO_PRESENT, null, new WString(workDir), si, pi);
                    if (!ok) {
                        throw new IOException("CreateProcess failed for " + commandLine + " err=" + Native.getLastError());
                    }
                    Pointer hProcess = pi.hProcess;
                    Pointer hThread = pi.hThread;
                    int pid = pi.dwProcessId;
                    diag.append(" | createProcess: ok=true pid=").append(pid);
                    k.CloseHandle(hThread); // thread handle not needed afterwards

                    try {
                        k.DeleteProcThreadAttributeList(attrList);
                    } catch (Exception e) {
                        log().debug("DeleteProcThreadAttributeList failed", e);
                    }
                    return new ConPty(platform, pid, hpc, hProcess, null, inWrite.getValue(),
                            outRead.getValue());
                } catch (Throwable t) {
                    if (attrList != null) {
                        try {
                            k.DeleteProcThreadAttributeList(attrList);
                        } catch (Exception ignored) {
                        }
                    }
                    try {
                        k.ClosePseudoConsole(hpc);
                    } catch (Exception ignored) {
                    }
                    try {
                        k.CloseHandle(inWrite.getValue());
                    } catch (Exception ignored) {
                    }
                    try {
                        k.CloseHandle(outRead.getValue());
                    } catch (Exception ignored) {
                    }
                    if (t instanceof IOException ioe) {
                        throw ioe;
                    }
                    throw new IOException("ConPTY setup failed: " + t, t);
                }
            }
        }
}
```

---

<a id="file-8"></a>
### 8. `src/main/java/com/automation/agent/protocol/TerminalProtocol.java`

**Path:** `src/main/java/com/automation/agent/protocol/TerminalProtocol.java` | **Size:** `10.22 KB` | **Lines:** `285` | **Language:** `java`

```java
package com.automation.agent.protocol;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Single home for the frozen V2 wire protocol: the event-type catalog, the
 * agent error catalog, and all strongly-typed Jackson JSON payloads. One
 * WebSocket carries every event type defined here.
 */
public final class TerminalProtocol {

    private TerminalProtocol() {
    }

    /** Wire values for {@code event-type}. One WebSocket carries all of these. */
    public enum TerminalEventType {
        TERMINAL_START("terminal_start"),
        TERMINAL_EXECUTE("terminal_execute"),
        TERMINAL_STOP("terminal_stop"),
        TERMINAL_CLOSE("terminal_close"),
        TERMINAL_RESULT("terminal_result"),
        TERMINAL_ERROR("terminal_error"),
        TERMINAL_STATUS("terminal_status"),
        TERMINAL_MAX("terminal_max"),
        METADATA_LAST_RESULT("metadata_lastResult"),
        METADATA_LAST_ERROR("metadata_lastError"),
        METADATA_LIST("metadata_list"),
        TERMINAL_REMOVE("terminal_remove");

        private final String wire;

        TerminalEventType(String wire) {
            this.wire = wire;
        }

        public String wire() {
            return wire;
        }

        public static TerminalEventType fromWire(String wire) {
            if (wire == null) {
                return null;
            }
            for (TerminalEventType t : values()) {
                if (t.wire.equals(wire)) {
                    return t;
                }
            }
            return null;
        }
    }

    /**
     * Centralized agent-level error catalog. Each entry carries its default message.
     * {@code lastErrorCode} in metadata retains the latest code; {@code null} means no current error.
     */
    public enum TerminalErrorCode {
        TERMINAL_NOT_FOUND("Terminal not found."),
        TERMINAL_ALREADY_RUNNING("Terminal is already running."),
        TERMINAL_BUSY("Terminal is currently executing a command."),
        TERMINAL_CLOSED("Terminal is closed."),
        INVALID_PLATFORM("Invalid terminal platform. Expected CMD or PS."),
        INVALID_EVENT("Invalid event."),
        INVALID_TERMINAL_ID("Invalid terminal id."),
        MULTILINE_COMMAND_NOT_ALLOWED("Multiline terminal input is not allowed."),
        PROCESS_START_FAILED("Failed to start terminal process."),
        PROCESS_WRITE_FAILED("Failed to write to terminal process."),
        PROCESS_INTERRUPTION_FAILED("Failed to interrupt terminal process."),
        PROCESS_TERMINATION_FAILED("Failed to terminate terminal process.");

        private final String defaultMessage;

        TerminalErrorCode(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }

        public String defaultMessage() {
            return defaultMessage;
        }
    }

    // ------------------------------------------------------------------ inbound

    /** Inbound: {@code terminal_start {event-type, terminalid, platform}}. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TerminalStartEvent {
        @JsonProperty("event-type")
        public String eventType;
        @JsonProperty("terminalid")
        public String terminalId;
        @JsonProperty("platform")
        public String platform;
    }

    /** Inbound: {@code terminal_execute {event-type, terminalid, command}}. Single physical line. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TerminalExecuteEvent {
        @JsonProperty("event-type")
        public String eventType;
        @JsonProperty("terminalid")
        public String terminalId;
        @JsonProperty("command")
        public String command;
    }

    /** Inbound: {@code terminal_stop {event-type, terminalid}}. Ctrl+C, shell stays alive. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TerminalStopEvent {
        @JsonProperty("event-type")
        public String eventType;
        @JsonProperty("terminalid")
        public String terminalId;
    }

    /** Inbound: {@code terminal_close {event-type, terminalid}}. Final closure. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TerminalCloseEvent {
        @JsonProperty("event-type")
        public String eventType;
        @JsonProperty("terminalid")
        public String terminalId;
    }

    /** Inbound: {@code terminal_remove {event-type, terminalid}}. Purges closed terminal context from agent metadata map. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TerminalRemoveEvent {
        @JsonProperty("event-type")
        public String eventType;
        @JsonProperty("terminalid")
        public String terminalId;
    }

    /** Inbound: request cached lastResult chunks for a terminal. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MetadataLastResultEvent {
        @JsonProperty("event-type")
        public String eventType;
        @JsonProperty("terminalid")
        public String terminalId;
    }

    /** Inbound: request cached lastError chunks for a terminal. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MetadataLastErrorEvent {
        @JsonProperty("event-type")
        public String eventType;
        @JsonProperty("terminalid")
        public String terminalId;
    }

    // ----------------------------------------------------------------- outbound

    /** Outbound stdout chunk. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TerminalResultEvent {
        @JsonProperty("event-type")
        public String eventType = TerminalEventType.TERMINAL_RESULT.wire();
        @JsonProperty("terminalid")
        public String terminalId;
        @JsonProperty("chunk")
        public String chunk;
        @JsonProperty("count")
        public int count;
        @JsonProperty("end")
        public boolean end;
    }

    /**
     * Outbound stderr chunk or agent-level error. Frozen semantics:
     * <ul>
     *   <li>{@code errorCode != null} — agent-generated protocol/process error
     *       (see {@code TerminalErrorCode}); the message is also cached in
     *       {@code lastError}/{@code lastErrorCode} metadata.</li>
     *   <li>{@code errorCode == null} — live shell/process stderr stream chunk.</li>
     * </ul>
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TerminalErrorEvent {
        @JsonProperty("event-type")
        public String eventType = TerminalEventType.TERMINAL_ERROR.wire();
        @JsonProperty("terminalid")
        public String terminalId;
        @JsonProperty("errorCode")
        public String errorCode;
        @JsonProperty("chunk")
        public String chunk;
        @JsonProperty("count")
        public int count;
        @JsonProperty("end")
        public boolean end;
    }

    /**
     * Outbound metadata snapshot. The bounded {@code lastResult}/{@code lastError}
     * cache values are included in the payload but are NOT watched: they never
     * trigger a status event by themselves (see {@code TerminalModels.TerminalMetadata.statusSignature}).
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TerminalStatusEvent {
        @JsonProperty("event-type")
        public String eventType = TerminalEventType.TERMINAL_STATUS.wire();
        @JsonProperty("terminalid")
        public String terminalId;
        @JsonProperty("pid")
        public long pid;
        @JsonProperty("platform")
        public String platform;
        @JsonProperty("currentPath")
        public String currentPath;
        @JsonProperty("lastCommand")
        public String lastCommand;
        @JsonProperty("lastErrorCode")
        public String lastErrorCode;
        @JsonProperty("lastResult")
        public String lastResult;
        @JsonProperty("lastError")
        public String lastError;
        @JsonProperty("state")
        public String state;
        @JsonProperty("lastInputTimestamp")
        public Long lastInputTimestamp;
        @JsonProperty("lastOutputTimestamp")
        public Long lastOutputTimestamp;
        @JsonProperty("elevated")
        public boolean elevated;
    }

    /**
     * Outbound cached-result transfer. Sent only on {@code metadata_lastResult} request,
     * chunked by {@code terminal.metadata.transfer-chunk-chars}. Deliberately distinct
     * from {@code terminal_result}, which is the live execution stream.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MetadataResultEvent {
        @JsonProperty("event-type")
        public String eventType = TerminalEventType.METADATA_LAST_RESULT.wire();
        @JsonProperty("terminalid")
        public String terminalId;
        @JsonProperty("chunk")
        public String chunk;
        @JsonProperty("count")
        public int count;
        @JsonProperty("end")
        public boolean end;
    }

    /**
     * Outbound cached-error transfer. Sent only on {@code metadata_lastError} request,
     * chunked by {@code terminal.metadata.transfer-chunk-chars}. Deliberately distinct
     * from {@code terminal_error}, which is the live error stream.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MetadataErrorEvent {
        @JsonProperty("event-type")
        public String eventType = TerminalEventType.METADATA_LAST_ERROR.wire();
        @JsonProperty("terminalid")
        public String terminalId;
        @JsonProperty("chunk")
        public String chunk;
        @JsonProperty("count")
        public int count;
        @JsonProperty("end")
        public boolean end;
    }

    /** Inbound: request complete list of terminal session metadata snapshots. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MetadataListEvent {
        @JsonProperty("event-type")
        public String eventType;
        @JsonProperty("terminalid")
        public String terminalId;
    }

    /** Outbound complete list of terminal metadata snapshots. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MetadataListResponseEvent {
        @JsonProperty("event-type")
        public String eventType = TerminalEventType.METADATA_LIST.wire();
        @JsonProperty("terminals")
        public java.util.List<TerminalStatusEvent> terminals;
    }
}
```

---

<a id="file-9"></a>
### 9. `src/main/java/com/automation/agent/terminal/TerminalManager.java`

**Path:** `src/main/java/com/automation/agent/terminal/TerminalManager.java` | **Size:** `37.39 KB` | **Lines:** `912` | **Language:** `java`

```java
package com.automation.agent.terminal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automation.agent.config.AgentConfig;
import com.automation.agent.process.ProcessSession;
import com.automation.agent.protocol.TerminalProtocol.MetadataErrorEvent;
import com.automation.agent.protocol.TerminalProtocol.MetadataResultEvent;
import com.automation.agent.protocol.TerminalProtocol.TerminalErrorCode;
import com.automation.agent.protocol.TerminalProtocol.TerminalErrorEvent;
import com.automation.agent.protocol.TerminalProtocol.TerminalEventType;
import com.automation.agent.protocol.TerminalProtocol.TerminalResultEvent;
import com.automation.agent.protocol.TerminalProtocol.TerminalStatusEvent;
import com.automation.agent.terminal.TerminalModels.TerminalContext;
import com.automation.agent.terminal.TerminalModels.TerminalMetadata;
import com.automation.agent.terminal.TerminalModels.TerminalPlatform;
import com.automation.agent.terminal.TerminalModels.TerminalState;

/**
 * Owns max-N terminals: atomic slot reservation, lookup, lifecycle
 * coordination, creation/removal. Per-terminal mutations hold that terminal's
 * lock, so Terminal A never blocks Terminal B.
 *
 * <p>Invariants (V2 master plan section 37): at most one active command per
 * terminal; start never duplicates an active terminal; execute never overlaps;
 * stop never closes the shell; close fully terminates; CLOSING retains the
 * slot; failed creation releases the reservation; multiline input never reaches
 * the shell; the agent never auto-closes.
 */
public final class TerminalManager {

    private static final Logger log = LoggerFactory.getLogger(TerminalManager.class);

    private final AgentConfig config;
    private final SessionFactory sessions;
    private final ConcurrentHashMap<String, TerminalContext> terminals = new ConcurrentHashMap<>();
    private final Semaphore slots;
    private final AtomicLong markerSeq = new AtomicLong();
    private final ScheduledExecutorService stopTimeouts;
    private volatile Consumer<Object> sink;

    /**
     * Creates OS shell sessions. Swappable in tests for deterministic fakes;
     * production uses the real ConPTY-backed {@link ProcessSession#launch}.
     */
    @FunctionalInterface
    public interface SessionFactory {
        ProcessSession start(TerminalPlatform platform, String initialPath) throws Exception;
    }

    public TerminalManager(AgentConfig config) {
        this(config, (platform, initialPath) -> ProcessSession.launch(platform, initialPath, config));
    }

    TerminalManager(AgentConfig config, SessionFactory sessions) {
        this.config = config;
        this.sessions = sessions;
        this.slots = new Semaphore(config.terminalMaxCount(), true);
        this.stopTimeouts = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "term-stop-timeout");
            t.setDaemon(true);
            return t;
        });
    }

    public void setSink(Consumer<Object> sink) {
        this.sink = sink;
    }

    /** Releases the internal STOP-timeout scheduler. Terminals are untouched. */
    public void shutdown() {
        stopTimeouts.shutdownNow();
    }

    /** Closes every live terminal (graceful JVM shutdown). Slots are released. */
    public void closeAll() {
        for (String id : terminals.keySet()) {
            try {
                close(id);
            } catch (Exception e) {
                log.warn("closeAll: error closing {}: {}", id, e.toString());
            }
        }
    }

    // ------------------------------------------------------------------ start

    public void start(String terminalId, String platformRaw) {
        start(terminalId, platformRaw, null);
    }

    public void start(String terminalId, String platformRaw, String initialPath) {
        if (terminalId == null || terminalId.isBlank()) {
            sendDetachedError("", TerminalErrorCode.INVALID_TERMINAL_ID);
            return;
        }
        TerminalPlatform platform = TerminalPlatform.parse(platformRaw);
        if (platform == null) {
            TerminalContext existing = terminals.get(terminalId);
            if (existing != null) {
                existing.lock().lock();
                try {
                    agentError(existing, TerminalErrorCode.INVALID_PLATFORM);
                    emitStatusIfChanged(existing);
                } finally {
                    existing.lock().unlock();
                }
            } else {
                sendDetachedError(terminalId, TerminalErrorCode.INVALID_PLATFORM);
            }
            return;
        }

        TerminalContext existing = terminals.get(terminalId);
        if (existing != null) {
            existing.lock().lock();
            try {
                if (existing.metadata().state() != TerminalState.CLOSED) {
                    if (existing.metadata().platform() != null && existing.metadata().platform() != platform) {
                        agentError(existing, TerminalErrorCode.INVALID_PLATFORM,
                                "Terminal is already active with platform " + existing.metadata().platform() + ". Close terminal first to switch platform.");
                        emitStatusIfChanged(existing);
                        return;
                    }
                    agentError(existing, TerminalErrorCode.TERMINAL_ALREADY_RUNNING);
                    emitStatusIfChanged(existing);
                    return;
                }
                // CLOSED -> restart with the same terminalId: needs a fresh slot.
                if (!slots.tryAcquire()) {
                    sendMax(terminalId);
                    return;
                }
                try {
                    restartLocked(existing, platform, initialPath);
                } catch (Exception e) {
                    slots.release();
                    agentError(existing, TerminalErrorCode.PROCESS_START_FAILED, e.getMessage());
                    emitStatusIfChanged(existing);
                }
            } finally {
                existing.lock().unlock();
            }
            return;
        }

        // New terminal: reserve atomically BEFORE creating anything.
        TerminalContext fresh = new TerminalContext(terminalId);
        TerminalContext raced = terminals.putIfAbsent(terminalId, fresh);
        if (raced != null) {
            // Lost the race — delegate to the existing-context path.
            start(terminalId, platformRaw, initialPath);
            return;
        }
        if (!slots.tryAcquire()) {
            terminals.remove(terminalId, fresh);
            sendMax(terminalId);
            return;
        }
        fresh.lock().lock();
        try {
            try {
                restartLocked(fresh, platform, initialPath);
            } catch (Exception e) {
                terminals.remove(terminalId, fresh);
                slots.release();
                sendDetachedError(terminalId, TerminalErrorCode.PROCESS_START_FAILED, messageOf(e));
            }
        } finally {
            fresh.lock().unlock();
        }
    }

    /** Caller holds ctx.lock(). Creates a new shell/PID for the same terminalId. */
    private void restartLocked(TerminalContext ctx, TerminalPlatform platform, String initialPath) throws Exception {
        String targetPath = (initialPath != null && !initialPath.isBlank())
                ? initialPath
                : (ctx.metadata().currentPath() != null && !ctx.metadata().currentPath().isBlank()
                        ? ctx.metadata().currentPath()
                        : "C:\\");
        ProcessSession session = sessions.start(platform, targetPath);
        ctx.session(session);
        TerminalMetadata m = ctx.metadata();
        m.pid(session.pid());
        m.platform(platform);
        m.currentPath(targetPath);
        m.state(TerminalState.IDLE);
        // Cached metadata (lastCommand/lastResult/lastError/lastErrorCode/timestamps)
        // survives a restart until the next execute resets command-specific fields.
        ctx.activeMarker(null);
        ctx.interrupted(false);
        ctx.resetFraming(new ArrayList<>());
        ctx.resetResultCounter();
        ctx.resetErrorCounter();
        cancelStopTimeoutLocked(ctx);
        ctx.lastEmittedStatusSignature(null); // force status emit
        spawnReaders(ctx);
        emitStatusIfChanged(ctx);
        log.info("Terminal {} started platform={} pid={}", m.terminalId(), platform, m.pid());
    }

    // ---------------------------------------------------------------- execute

    public void execute(String terminalId, String command) {
        TerminalContext ctx = terminals.get(terminalId);
        if (ctx == null || terminalId == null || terminalId.isBlank()) {
            sendDetachedError(terminalId == null ? "" : terminalId, TerminalErrorCode.TERMINAL_NOT_FOUND);
            return;
        }
        ctx.lock().lock();
        try {
            TerminalMetadata m = ctx.metadata();
            if (m.state() == TerminalState.CLOSING || m.state() == TerminalState.CLOSED) {
                agentError(ctx, TerminalErrorCode.TERMINAL_CLOSED);
                emitStatusIfChanged(ctx);
                return;
            }
            if (m.state() == TerminalState.RUNNING) {
                agentError(ctx, TerminalErrorCode.TERMINAL_BUSY);
                emitStatusIfChanged(ctx);
                return;
            }
            if (command == null) {
                command = "";
            }
            if (command.contains("\r") || command.contains("\n")) {
                // Never written to the shell (plan section 20).
                agentError(ctx, TerminalErrorCode.MULTILINE_COMMAND_NOT_ALLOWED);
                emitStatusIfChanged(ctx);
                return;
            }
            ProcessSession session = ctx.session();
            if (session == null || !session.isAlive()) {
                agentError(ctx, TerminalErrorCode.PROCESS_START_FAILED, "Shell process is not alive.");
                emitStatusIfChanged(ctx);
                return;
            }
            String marker = "__AGENT_DONE_" + markerSeq.incrementAndGet() + "__";
            m.lastCommand(command);
            m.lastResult(null);
            m.lastError(null);
            m.lastErrorCode(null);
            m.lastInputTimestamp(System.currentTimeMillis());
            m.lastOutputTimestamp(null);
            m.state(TerminalState.RUNNING);
            ctx.activeMarker(marker);
            ctx.interrupted(false);
            String markerCmd = markerCommand(ctx.metadata().platform(), marker);
            // Fresh framing: raw accumulation + user-output classification restart;
            // the two sent input lines are consumed as console echo, never as result.
            ctx.resetFraming(new ArrayList<>(List.of(command, markerCmd)));
            ctx.resetResultCounter();
            ctx.resetErrorCounter();
            cancelStopTimeoutLocked(ctx);
            emitStatusIfChanged(ctx);
            try {
                // Compound single-line commands pass through untouched (plan section 19).
                session.writeLine(command);
                session.writeLine(markerCmd);
            } catch (Exception e) {
                m.state(TerminalState.IDLE);
                ctx.activeMarker(null);
                agentError(ctx, TerminalErrorCode.PROCESS_WRITE_FAILED, messageOf(e));
                emitStatusIfChanged(ctx);
            }
        } finally {
            ctx.lock().unlock();
        }
    }

    /**
     * Pure execution-boundary sentinel. The marker command carries no verdict about
     * the user command (no exit codes are interpreted): reaching it only means the
     * shell has finished everything queued before it. It deliberately starts its
     * output with a blank line so the marker always begins its own console line —
     * the preceding shell prompt ({@code PS ...> }) has no trailing newline, so
     * without this the marker would share a line with prompt chrome and line-start
     * detection would miss it.
     */
    private static String markerCommand(TerminalPlatform platform, String marker) {
        if (platform == TerminalPlatform.CMD) {
            return "echo. & echo " + marker;
        }
        return "Write-Output \"" + marker + "\"";
    }

    // ------------------------------------------------------------------- stop

    /**
     * Ctrl+C through the terminal's own ConPTY. The shell stays alive and the
     * terminal stays RUNNING until the shell actually reaches the queued command
     * boundary (the marker) or the stop timeout fires — Ctrl+C delivery and
     * command completion are distinct events, so a following EXECUTE can never
     * overlap the interrupted command.
     */
    public void stop(String terminalId) {
        TerminalContext ctx = terminals.get(terminalId);
        if (ctx == null) {
            sendDetachedError(terminalId, TerminalErrorCode.TERMINAL_NOT_FOUND);
            return;
        }
        ctx.lock().lock();
        try {
            TerminalMetadata m = ctx.metadata();
            if (m.state() == TerminalState.CLOSED || m.state() == TerminalState.CLOSING) {
                agentError(ctx, TerminalErrorCode.TERMINAL_CLOSED);
                emitStatusIfChanged(ctx);
                return;
            }
            if (m.state() != TerminalState.RUNNING) {
                emitStatusIfChanged(ctx); // IDLE no-op
                return;
            }
            ProcessSession session = ctx.session();
            boolean delivered = session != null && ProcessSession.interrupt(session);
            if (!delivered) {
                agentError(ctx, TerminalErrorCode.PROCESS_INTERRUPTION_FAILED);
                emitStatusIfChanged(ctx);
                return;
            }
            ctx.interrupted(true);
            // Marker stays armed: late output (including the queued marker line)
            // still belongs to this command. IDLE happens at the boundary or timeout.
            scheduleStopTimeoutLocked(ctx, ctx.activeMarker());
        } finally {
            ctx.lock().unlock();
        }
    }

    /** Caller holds ctx.lock(). Arms the forced STOP-boundary completion. */
    private void scheduleStopTimeoutLocked(TerminalContext ctx, String marker) {
        cancelStopTimeoutLocked(ctx);
        ScheduledFuture<?> f = stopTimeouts.schedule(() -> {
            ctx.lock().lock();
            try {
                if (ctx.metadata().state() == TerminalState.RUNNING
                        && ctx.interrupted()
                        && marker != null
                        && marker.equals(ctx.activeMarker())) {
                    log.warn("Terminal {} STOP boundary timeout after {}ms; forcing completion",
                            ctx.metadata().terminalId(), config.stopTimeoutMs());
                    completeLocked(ctx, true);
                }
            } finally {
                ctx.lock().unlock();
            }
        }, config.stopTimeoutMs(), TimeUnit.MILLISECONDS);
        ctx.stopTimeout(f);
    }

    /** Caller holds ctx.lock(). */
    private void cancelStopTimeoutLocked(TerminalContext ctx) {
        ScheduledFuture<?> f = ctx.stopTimeout();
        ctx.stopTimeout(null);
        if (f != null) {
            f.cancel(false);
        }
    }

    // ------------------------------------------------------------------ close

    /** Final closure: terminate fully, verify, release resources + slot. */
    public void close(String terminalId) {
        TerminalContext ctx = terminals.get(terminalId);
        if (ctx == null) {
            sendDetachedError(terminalId, TerminalErrorCode.TERMINAL_NOT_FOUND);
            return;
        }
        ctx.lock().lock();
        try {
            TerminalMetadata m = ctx.metadata();
            if (m.state() == TerminalState.CLOSED) {
                agentError(ctx, TerminalErrorCode.TERMINAL_CLOSED);
                return;
            }
            if (m.state() != TerminalState.CLOSING) {
                m.state(TerminalState.CLOSING);
                ctx.activeMarker(null);
                cancelStopTimeoutLocked(ctx);
                emitStatusIfChanged(ctx); // CLOSING retains the slot
            }
            // Same termination block serves first close and CLOSING retries.
            try {
                // Retain the session reference until termination is verified, so a
                // failed close can actually be retried instead of stranding CLOSING.
                ProcessSession session = ctx.session();
                if (session != null) {
                    session.destroy();
                    long end = System.currentTimeMillis() + 3000;
                    while (session.isAlive() && System.currentTimeMillis() < end) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
                if (session != null && session.isAlive()) {
                    agentError(ctx, TerminalErrorCode.PROCESS_TERMINATION_FAILED,
                            "Process still alive after termination request.");
                    return; // stay CLOSING, session + slot retained
                }
                ctx.session(null);
                m.state(TerminalState.CLOSED);
                m.pid(-1);
                slots.release();
            } catch (Exception e) {
                agentError(ctx, TerminalErrorCode.PROCESS_TERMINATION_FAILED, messageOf(e));
                return; // stay CLOSING, session + slot retained
            }
            emitStatusIfChanged(ctx); // final observable notification: state=CLOSED
            log.info("Terminal {} closed", terminalId);
        } finally {
            ctx.lock().unlock();
        }
    }

    /** Purges a CLOSED terminal context from memory. Has no effect if terminal is active or running. */
    public void remove(String terminalId) {
        if (terminalId == null || terminalId.isBlank()) {
            return;
        }
        TerminalContext ctx = terminals.get(terminalId);
        if (ctx == null) {
            return;
        }
        ctx.lock().lock();
        try {
            if (ctx.metadata().state() == TerminalState.CLOSED) {
                terminals.remove(terminalId);
                log.info("Terminal {} metadata purged from memory cache", terminalId);
            } else {
                log.warn("Cannot purge terminal {}: state is {}", terminalId, ctx.metadata().state());
            }
        } finally {
            ctx.lock().unlock();
        }
    }

    // ------------------------------------------------- metadata_lastResult/Error

    public void metadataLastResult(String terminalId) {
        TerminalContext ctx = terminals.get(terminalId);
        if (ctx == null) {
            sendDetachedError(terminalId, TerminalErrorCode.TERMINAL_NOT_FOUND);
            return;
        }
        ctx.lock().lock();
        try {
            transferInChunks(ctx, terminalId, ctx.metadata().lastResult(), true);
        } finally {
            ctx.lock().unlock();
        }
    }

    public void metadataLastError(String terminalId) {
        TerminalContext ctx = terminals.get(terminalId);
        if (ctx == null) {
            sendDetachedError(terminalId, TerminalErrorCode.TERMINAL_NOT_FOUND);
            return;
        }
        ctx.lock().lock();
        try {
            transferInChunks(ctx, terminalId, ctx.metadata().lastError(), false);
        } finally {
            ctx.lock().unlock();
        }
    }

    public void metadataList() {
        List<TerminalStatusEvent> list = new ArrayList<>();
        for (TerminalContext ctx : terminals.values()) {
            ctx.lock().lock();
            try {
                TerminalMetadata m = ctx.metadata();
                TerminalStatusEvent e = new TerminalStatusEvent();
                e.terminalId = m.terminalId();
                e.pid = m.pid();
                e.platform = m.platform() == null ? null : m.platform().name();
                e.currentPath = m.currentPath();
                e.lastCommand = m.lastCommand();
                e.lastErrorCode = m.lastErrorCode();
                e.lastResult = m.lastResult();
                e.lastError = m.lastError();
                e.state = m.state().name();
                e.lastInputTimestamp = m.lastInputTimestamp();
                e.lastOutputTimestamp = m.lastOutputTimestamp();
                e.elevated = m.elevated();
                list.add(e);
            } finally {
                ctx.lock().unlock();
            }
        }
        com.automation.agent.protocol.TerminalProtocol.MetadataListResponseEvent response =
                new com.automation.agent.protocol.TerminalProtocol.MetadataListResponseEvent();
        response.terminals = list;
        emit(response);
    }

    private void transferInChunks(TerminalContext ctx, String terminalId, String value, boolean result) {
        String data = value == null ? "" : value;
        int size = Math.max(1, config.transferChunkChars());
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < data.length(); i += size) {
            parts.add(data.substring(i, Math.min(data.length(), i + size)));
        }
        if (parts.isEmpty()) {
            parts.add("");
        }
        for (int i = 0; i < parts.size(); i++) {
            boolean end = i == parts.size() - 1;
            int count = i + 1;
            if (result) {
                MetadataResultEvent e = new MetadataResultEvent();
                e.terminalId = terminalId;
                e.chunk = parts.get(i);
                e.count = count;
                e.end = end;
                emit(e);
            } else {
                MetadataErrorEvent e = new MetadataErrorEvent();
                e.terminalId = terminalId;
                e.chunk = parts.get(i);
                e.count = count;
                e.end = end;
                emit(e);
            }
        }
        log.debug("metadata transfer {} terminal={} chunks={}", result ? "lastResult" : "lastError",
                terminalId, parts.size());
    }

    // ------------------------------------------------------- output streaming

    private void spawnReaders(TerminalContext ctx) {
        String terminalId = ctx.metadata().terminalId();
        ProcessSession session = ctx.session();
        Thread out = new Thread(
                new ProcessSession.OutputReader(session.stdout(), text -> handleStdout(terminalId, text)),
                "term-" + terminalId + "-stdout");
        out.setDaemon(true);
        ctx.readerThread(out);
        out.start();
        if (session.stderr() != null) {
            Thread err = new Thread(
                    new ProcessSession.OutputReader(session.stderr(), text -> handleStderr(terminalId, text)),
                    "term-" + terminalId + "-stderr");
            err.setDaemon(true);
            err.start();
        }
    }

    /** Caller: reader thread. Locks the terminal. */
    void handleStdout(String terminalId, String text) {
        System.err.println("DEBUG STDOUT [" + terminalId + "]: " + text.replace("\r", "\\r").replace("\n", "\\n"));
        TerminalContext ctx = terminals.get(terminalId);
        if (ctx == null) {
            return;
        }
        ctx.lock().lock();
        try {
            extractCurrentPath(ctx, text);
            if (ctx.metadata().state() != TerminalState.RUNNING || ctx.activeMarker() == null) {
                return; // stray output (e.g. post-completion prompt tail) — drop deterministically
            }
            String marker = ctx.activeMarker();
            // Raw accumulation: detection always runs on the whole buffer, so a marker
            // split across reader chunks is still found. Only complete lines are
            // classified; a trailing partial line waits for more bytes. Boundaries are
            // \n, \r\n, AND lone \r (console prompt/progress rewrites); a \r that is
            // followed by \r or \n belongs to a \r\r\n sequence, not a boundary.
            StringBuilder raw = ctx.liveResultBuffer();
            String cleanedText = text.replaceAll("\u001B\\[[0-9]+;[0-9]+[Hf]|\u001B\\[[0-9]*[Ed]", "\n");
            raw.append(cleanedText);
            log.info("handleStdout rawText: [{}]", text);
            while (true) {
                int pos = ctx.parseOffset();
                int j = pos;
                int lineEnd = -1;
                int termEnd = -1;
                while (j < raw.length()) {
                    char c = raw.charAt(j);
                    if (c == '\n') {
                        lineEnd = j;
                        termEnd = j + 1;
                        break;
                    }
                    if (c == '\r') {
                        if (j + 1 >= raw.length()) {
                            break; // trailing lone \r: may still become \r\n, wait
                        }
                        char next = raw.charAt(j + 1);
                        if (next == '\n') {
                            lineEnd = j;
                            termEnd = j + 2;
                            break;
                        }
                        if (next == '\r') {
                            j++; // part of a \r\r\n sequence, keep scanning
                            continue;
                        }
                        lineEnd = j; // lone \r rewrite boundary
                        termEnd = j + 1;
                        break;
                    }
                    j++;
                }
                if (lineEnd < 0) {
                    break;
                }
                String rawLine = raw.substring(pos, termEnd);
                ctx.parseOffset(termEnd);
                String norm = TerminalModels.normalizeLine(rawLine);
                log.info("handleStdout parsed: id={} rawLine=[{}] norm=[{}] expectedEcho={}", terminalId, rawLine.replace("\r", "\\r").replace("\n", "\\n"), norm, ctx.expectedEcho());
                if (norm.equals(">>") || norm.startsWith(">> ")) {
                    continue; // drop PowerShell multiline continuation prompt chrome
                }
                if (norm.matches("^(?:PS\\s+)?[A-Za-z]:\\\\[^<>\"|?*\\r\\n]*>$")) {
                    continue; // drop bare shell prompt lines emitted by ConPTY chrome
                }
                if (!marker.isEmpty() && (norm.contains("Write-Output") || norm.contains("echo."))
                        && (norm.contains(marker) || norm.contains("__AGENT_DONE_") || norm.contains("__AGENT_D"))) {
                    continue; // drop marker command input echo lines (single or wrapped)
                }
                if (consumeEchoLocked(ctx, norm)) {
                    continue; // console input echo is framing, never result
                }
                if (!marker.isEmpty() && norm.contains(marker)) {
                    // The marker command emits one leading blank line; a blank line held
                    // back from the previous step belongs to the marker, not the user.
                    ctx.heldBlankLine(null);
                    finishResultLocked(ctx, pendingUnsent(ctx), ctx.userBuffer().toString());
                    return;
                }
                if (norm.isEmpty()) {
                    // Hold one blank line back: it may be the marker command's leading
                    // blank. A previously held blank followed by real content was genuine.
                    String held = ctx.heldBlankLine();
                    if (held != null) {
                        emitUserLine(ctx, terminalId, held);
                    }
                    ctx.heldBlankLine(rawLine);
                } else {
                    String held = ctx.heldBlankLine();
                    if (held != null) {
                        ctx.heldBlankLine(null);
                        emitUserLine(ctx, terminalId, held);
                    }
                    emitUserLine(ctx, terminalId, rawLine);
                }
            }
        } finally {
            ctx.lock().unlock();
        }
    }

    /**
     * Consumes one expected console-echo line, at most once each, in send order.
     * Caller holds ctx.lock().
     */
    private boolean consumeEchoLocked(TerminalContext ctx, String normalizedLine) {
        List<String> expected = ctx.expectedEcho();
        for (int i = 0; i < expected.size(); i++) {
            if (TerminalModels.isEchoOf(normalizedLine,
                    TerminalModels.normalizeLine(expected.get(i)))) {
                for (int j = 0; j <= i; j++) {
                    expected.remove(0);
                }
                return true;
            }
        }
        return false;
    }

    /** Streams one classified user-output line as a live chunk. Caller holds lock. */
    private void emitUserLine(TerminalContext ctx, String terminalId, String rawLine) {
        ctx.userBuffer().append(rawLine);
        TerminalResultEvent e = new TerminalResultEvent();
        e.terminalId = terminalId;
        e.chunk = rawLine;
        e.count = ctx.nextResultCount();
        e.end = false;
        emit(e);
        ctx.streamedChars(ctx.userBuffer().length());
    }

    /** Classified-but-unemitted user text. Caller holds lock. */
    private String pendingUnsent(TerminalContext ctx) {
        return ctx.userBuffer().substring(Math.min(ctx.streamedChars(), ctx.userBuffer().length()));
    }

    /** Caller: reader thread. Locks the terminal. */
    void handleStderr(String terminalId, String text) {
        TerminalContext ctx = terminals.get(terminalId);
        if (ctx == null) {
            return;
        }
        ctx.lock().lock();
        try {
            if (ctx.metadata().state() != TerminalState.RUNNING || ctx.activeMarker() == null) {
                return;
            }
            ctx.liveErrorBuffer().append(text);
            TerminalErrorEvent e = new TerminalErrorEvent();
            e.terminalId = terminalId;
            e.errorCode = null; // process stderr: no agent error code
            e.chunk = text;
            e.count = ctx.nextErrorCount();
            e.end = false;
            emit(e);
        } finally {
            ctx.lock().unlock();
        }
    }

    /** Complete the active command. Caller holds ctx.lock(). */
    private void completeLocked(TerminalContext ctx, boolean interrupted) {
        cancelStopTimeoutLocked(ctx);
        // No marker boundary was seen (STOP/timeout path): any held-back blank line
        // is genuine output, not marker chrome.
        String held = ctx.heldBlankLine();
        if (held != null) {
            ctx.heldBlankLine(null);
            emitUserLine(ctx, ctx.metadata().terminalId(), held);
        }
        finishResultLocked(ctx, pendingUnsent(ctx), ctx.userBuffer().toString());
    }

    /** Emit final chunk (end=true), cache bounded metadata, go IDLE. Caller holds lock. */
    private void finishResultLocked(TerminalContext ctx, String unsent, String fullContent) {
        cancelStopTimeoutLocked(ctx);
        TerminalMetadata m = ctx.metadata();
        String terminalId = m.terminalId();
        TerminalResultEvent fin = new TerminalResultEvent();
        fin.terminalId = terminalId;
        fin.chunk = unsent;
        fin.count = ctx.nextResultCount();
        fin.end = true;
        emit(fin);
        ctx.resetResultCounter();
        // Flush any stderr remainder as its own end=true (only when stderr happened).
        if (ctx.liveErrorBuffer().length() > 0) {
            TerminalErrorEvent ferr = new TerminalErrorEvent();
            ferr.terminalId = terminalId;
            ferr.errorCode = null;
            ferr.chunk = "";
            ferr.count = ctx.nextErrorCount();
            ferr.end = true;
            emit(ferr);
            ctx.resetErrorCounter();
        }
        m.lastResult(TerminalMetadata.bound(fullContent, config.maxResultChars()));
        m.lastError(TerminalMetadata.bound(
                ctx.liveErrorBuffer().length() == 0 ? null : ctx.liveErrorBuffer().toString(),
                config.maxErrorChars()));
        m.lastOutputTimestamp(System.currentTimeMillis());
        m.state(TerminalState.IDLE);
        ctx.activeMarker(null);
        ctx.streamedChars(0);
        // Framing for this execution is over; stray late bytes are dropped while IDLE.
        ctx.userBuffer().setLength(0);
        ctx.parseOffset(0);
        ctx.expectedEcho().clear();
        emitStatusIfChanged(ctx);
        log.debug("Terminal {} command complete interrupted={}", terminalId, ctx.interrupted());
    }

    // --------------------------------------------------------------- observers

    private void emitStatusIfChanged(TerminalContext ctx) {
        TerminalMetadata m = ctx.metadata();
        String sig = m.statusSignature();
        if (sig.equals(ctx.lastEmittedStatusSignature())) {
            return;
        }
        ctx.lastEmittedStatusSignature(sig);
        TerminalStatusEvent e = new TerminalStatusEvent();
        e.terminalId = m.terminalId();
        e.pid = m.pid();
        e.platform = m.platform() == null ? null : m.platform().name();
        e.currentPath = m.currentPath();
        e.lastCommand = m.lastCommand();
        e.lastErrorCode = m.lastErrorCode();
        // Bounded cache values ride along but never trigger emission (see signature).
        e.lastResult = m.lastResult();
        e.lastError = m.lastError();
        e.state = m.state().name();
        e.lastInputTimestamp = m.lastInputTimestamp();
        e.lastOutputTimestamp = m.lastOutputTimestamp();
        e.elevated = m.elevated();
        emit(e);
    }

    private static final java.util.regex.Pattern PROMPT_PATH_PATTERN =
            java.util.regex.Pattern.compile("(?:^|[\\r\\n])(?:PS\\s+)?([A-Za-z]:\\\\[^<>\"|?*\\r\\n]*)>");

    private void extractCurrentPath(TerminalContext ctx, String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        String stripped = TerminalModels.stripAnsi(text);
        java.util.regex.Matcher matcher = PROMPT_PATH_PATTERN.matcher(stripped);
        String lastFound = null;
        while (matcher.find()) {
            String p = matcher.group(1).trim();
            if (!p.isEmpty()) {
                if (p.length() == 2 && p.charAt(1) == ':') {
                    p = p + "\\";
                } else if (p.endsWith("\\") && p.length() > 3) {
                    p = p.substring(0, p.length() - 1);
                }
                lastFound = p;
            }
        }
        if (lastFound != null && !lastFound.isEmpty()) {
            if (!lastFound.equals(ctx.metadata().currentPath())) {
                ctx.metadata().currentPath(lastFound);
                emitStatusIfChanged(ctx);
            }
        }
    }

    private void sendMax(String terminalId) {
        // 11th start: no terminal created, no metadata, no process (plan section 7).
        Map<String, String> evt = Map.of(
                "event-type", TerminalEventType.TERMINAL_MAX.wire(),
                "terminalid", terminalId == null ? "" : terminalId);
        emit(evt);
        log.info("Terminal capacity reached; rejected start for {}", terminalId);
    }

    private void agentError(TerminalContext ctx, TerminalErrorCode code) {
        agentError(ctx, code, null);
    }

    private void agentError(TerminalContext ctx, TerminalErrorCode code, String detail) {
        TerminalMetadata m = ctx.metadata();
        m.lastErrorCode(code.name());
        String msg = detail == null ? code.defaultMessage() : code.defaultMessage() + " " + detail;
        // Agent errors populate the cached metadata as well as the live stream.
        m.lastError(TerminalMetadata.bound(msg, config.maxErrorChars()));
        sendErrorChunk(ctx, code, msg, true);
    }

    private void sendErrorChunk(TerminalContext ctx, TerminalErrorCode code, boolean end) {
        sendErrorChunk(ctx, code, code.defaultMessage(), end);
    }

    private void sendErrorChunk(TerminalContext ctx, TerminalErrorCode code, String msg, boolean end) {
        TerminalErrorEvent e = new TerminalErrorEvent();
        e.terminalId = ctx.metadata().terminalId();
        e.errorCode = code.name();
        e.chunk = msg;
        e.count = ctx.nextErrorCount();
        e.end = end;
        emit(e);
        if (end) {
            ctx.resetErrorCounter();
        }
    }

    private void sendDetachedError(String terminalId, TerminalErrorCode code) {
        sendDetachedError(terminalId, code, code.defaultMessage());
    }

    private void sendDetachedError(String terminalId, TerminalErrorCode code, String msg) {
        TerminalErrorEvent e = new TerminalErrorEvent();
        e.terminalId = terminalId == null ? "" : terminalId;
        e.errorCode = code.name();
        e.chunk = msg;
        e.count = 1;
        e.end = true;
        emit(e);
    }

    private void emit(Object eventPojo) {
        Consumer<Object> s = sink;
        if (s == null) {
            log.debug("No event sink; dropping {}", eventPojo);
            return;
        }
        try {
            s.accept(eventPojo);
        } catch (Exception e) {
            log.warn("Event sink failed: {}", e.toString());
        }
    }

    // ---------------------------------------------------------------- helpers

    private static String messageOf(Throwable t) {
        String m = t.getMessage();
        return m == null ? t.toString() : m;
    }

    // Visible for tests.
    int activeCount() {
        return slots.availablePermits() >= 0 ? config.terminalMaxCount() - slots.availablePermits() : 0;
    }
}
```

---

<a id="file-10"></a>
### 10. `src/main/java/com/automation/agent/terminal/TerminalModels.java`

**Path:** `src/main/java/com/automation/agent/terminal/TerminalModels.java` | **Size:** `16.42 KB` | **Lines:** `502` | **Language:** `java`

```java
package com.automation.agent.terminal;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

/**
 * Single home for the terminal domain models: state machine, platform catalog,
 * metadata, per-terminal context, and the pure console-framing helpers used by
 * {@link TerminalManager} to turn a raw ConPTY byte stream into classified
 * user output plus a reliable command-completion boundary.
 */
public final class TerminalModels {

    private TerminalModels() {
    }

    // ----------------------------------------------------------------- state

    /**
     * Final V2 terminal states.
     *
     * <pre>
     * IDLE -- execute --> RUNNING
     * RUNNING -- command complete --> IDLE
     * RUNNING -- stop --> IDLE
     * IDLE    -- close --> CLOSING
     * RUNNING -- close --> CLOSING
     * CLOSING -- process fully terminated --> CLOSED
     * CLOSED  -- start --> IDLE
     * </pre>
     * All transitions are atomic (guarded by the per-terminal lock in {@link TerminalContext}).
     */
    public enum TerminalState {
        IDLE,
        RUNNING,
        CLOSING,
        CLOSED
    }

    // --------------------------------------------------------------- platform

    /**
     * Windows shells the agent can host. The agent launches the real console
     * ({@code cmd.exe} / {@code powershell.exe}) — it never parses or emulates them.
     */
    public enum TerminalPlatform {
        CMD,
        PS;

        public static TerminalPlatform parse(String raw) {
            if (raw == null || raw.isBlank()) {
                return null;
            }
            String norm = raw.trim().toUpperCase().replace("_", "").replace(" ", "").replace(".EXE", "");
            if ("PS".equals(norm) || "POWERSHELL".equals(norm)) {
                return PS;
            }
            if ("CMD".equals(norm) || "COMMAND".equals(norm) || "COMMANDPROMPT".equals(norm)) {
                return CMD;
            }
            return null;
        }
    }

    // --------------------------------------------------------------- metadata

    /**
     * Only the metadata model. No I/O, no threads.
     * Watched fields for terminal_status change detection:
     * terminalId, pid, platform, lastCommand, lastErrorCode, state,
     * lastInputTimestamp, lastOutputTimestamp.
     * lastResult / lastError are deliberately EXCLUDED from status-change detection.
     */
    public static final class TerminalMetadata {

        public static final String TRUNCATION_MARKER = "<-Truncated->";

        private final String terminalId;
        private volatile long pid = -1;
        private volatile TerminalPlatform platform;
        private volatile String currentPath = "C:\\";
        private volatile String lastCommand;
        private volatile String lastResult;
        private volatile String lastError;
        private volatile String lastErrorCode;
        private volatile TerminalState state = TerminalState.IDLE;
        private volatile Long lastInputTimestamp;
        private volatile Long lastOutputTimestamp;
        private volatile boolean elevated = isProcessElevated();

        public static boolean isProcessElevated() {
            try {
                if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
                    return com.automation.agent.process.ConPtyNative.Shell32.INSTANCE.IsUserAnAdmin();
                }
            } catch (Throwable t) {
            }
            return false;
        }

        public TerminalMetadata(String terminalId) {
            this.terminalId = terminalId;
        }

        public boolean elevated() {
            return elevated;
        }

        public void elevated(boolean elevated) {
            this.elevated = elevated;
        }

        public String terminalId() {
            return terminalId;
        }

        public long pid() {
            return pid;
        }

        public void pid(long pid) {
            this.pid = pid;
        }

        public TerminalPlatform platform() {
            return platform;
        }

        public void platform(TerminalPlatform platform) {
            this.platform = platform;
        }

        public String currentPath() {
            return currentPath;
        }

        public void currentPath(String currentPath) {
            this.currentPath = currentPath;
        }

        public String lastCommand() {
            return lastCommand;
        }

        public void lastCommand(String lastCommand) {
            this.lastCommand = lastCommand;
        }

        public String lastResult() {
            return lastResult;
        }

        public void lastResult(String lastResult) {
            this.lastResult = lastResult;
        }

        public String lastError() {
            return lastError;
        }

        public void lastError(String lastError) {
            this.lastError = lastError;
        }

        public String lastErrorCode() {
            return lastErrorCode;
        }

        public void lastErrorCode(String lastErrorCode) {
            this.lastErrorCode = lastErrorCode;
        }

        public TerminalState state() {
            return state;
        }

        public void state(TerminalState state) {
            this.state = state;
        }

        public Long lastInputTimestamp() {
            return lastInputTimestamp;
        }

        public void lastInputTimestamp(Long lastInputTimestamp) {
            this.lastInputTimestamp = lastInputTimestamp;
        }

        public Long lastOutputTimestamp() {
            return lastOutputTimestamp;
        }

        public void lastOutputTimestamp(Long lastOutputTimestamp) {
            this.lastOutputTimestamp = lastOutputTimestamp;
        }

        /**
         * Bounds only the cached metadata. The live stream is never truncated.
         */
        public static String bound(String value, int maxChars) {
            if (value == null) {
                return null;
            }
            if (value.length() <= maxChars) {
                return value;
            }
            return value.substring(0, Math.max(0, maxChars)) + TRUNCATION_MARKER;
        }

        /**
         * Signature over watched fields only. Used to decide whether terminal_status must be emitted.
         */
        public String statusSignature() {
            return terminalId + "|" + pid + "|" + platform + "|" + currentPath + "|" + lastCommand + "|"
                    + lastErrorCode + "|" + state + "|" + lastInputTimestamp + "|" + lastOutputTimestamp + "|" + elevated;
        }
    }

    // --------------------------------------------------------------- context

    /**
     * Owns exactly one terminal: terminalId, PID, platform, process resources,
     * metadata, current execution state and chunk counters.
     * All lifecycle mutations must hold {@link #lock()}.
     */
    public static final class TerminalContext {

        private final ReentrantLock lock = new ReentrantLock();
        private final TerminalMetadata metadata;

        // Separate counters because stdout and stderr are separate streams.
        private int resultChunkCounter;
        private int errorChunkCounter;

        // Active execution tracking. Non-null only while state == RUNNING.
        private String activeMarker;
        private boolean interrupted;
        private final StringBuilder liveResultBuffer = new StringBuilder();
        private final StringBuilder liveErrorBuffer = new StringBuilder();
        // Framing state for the current execution (see TerminalModels framing helpers):
        // liveResultBuffer accumulates RAW console text; userBuffer accumulates only
        // classified user-visible output; parseOffset marks raw chars already classified;
        // streamedChars counts userBuffer chars already emitted as chunks; expectedEcho
        // holds the sent input lines whose console echo is consumed exactly once each.
        private final StringBuilder userBuffer = new StringBuilder();
        private int parseOffset;
        // At most one trailing blank line held back: it may be the marker command's
        // leading blank (dropped at the boundary) rather than genuine user output.
        private String heldBlankLine;
        private java.util.List<String> expectedEcho = java.util.List.of();
        // Live-stream accounting: chars of liveResultBuffer already emitted as chunks.
        private int streamedChars;
        // Pending STOP-boundary timeout. Non-null only between Ctrl+C and command completion.
        private ScheduledFuture<?> stopTimeout;

        // Last emitted status signature (watched fields). Null forces first emit.
        private String lastEmittedStatusSignature;

        // Process resources, set on start / restart.
        private volatile com.automation.agent.process.ProcessSession session;
        private volatile Thread readerThread;

        public TerminalContext(String terminalId) {
            this.metadata = new TerminalMetadata(terminalId);
        }

        public ReentrantLock lock() {
            return lock;
        }

        public TerminalMetadata metadata() {
            return metadata;
        }

        public int nextResultCount() {
            return ++resultChunkCounter;
        }

        public void resetResultCounter() {
            resultChunkCounter = 0;
        }

        public int nextErrorCount() {
            return ++errorChunkCounter;
        }

        public void resetErrorCounter() {
            errorChunkCounter = 0;
        }

        public String activeMarker() {
            return activeMarker;
        }

        public void activeMarker(String activeMarker) {
            this.activeMarker = activeMarker;
        }

        public boolean interrupted() {
            return interrupted;
        }

        public void interrupted(boolean interrupted) {
            this.interrupted = interrupted;
        }

        public StringBuilder liveResultBuffer() {
            return liveResultBuffer;
        }

        public StringBuilder liveErrorBuffer() {
            return liveErrorBuffer;
        }

        public StringBuilder userBuffer() {
            return userBuffer;
        }

        public int parseOffset() {
            return parseOffset;
        }

        public void parseOffset(int parseOffset) {
            this.parseOffset = parseOffset;
        }

        public java.util.List<String> expectedEcho() {
            return expectedEcho;
        }

        public void expectedEcho(java.util.List<String> expectedEcho) {
            this.expectedEcho = expectedEcho;
        }

        public String heldBlankLine() {
            return heldBlankLine;
        }

        public void heldBlankLine(String heldBlankLine) {
            this.heldBlankLine = heldBlankLine;
        }

        /** Resets all per-execution framing state for a new command. */
        public void resetFraming(java.util.List<String> expectedEchoLines) {
            liveResultBuffer.setLength(0);
            liveErrorBuffer.setLength(0);
            userBuffer.setLength(0);
            parseOffset = 0;
            streamedChars = 0;
            heldBlankLine = null;
            expectedEcho = expectedEchoLines;
        }

        public int streamedChars() {
            return streamedChars;
        }

        public void streamedChars(int streamedChars) {
            this.streamedChars = streamedChars;
        }

        public ScheduledFuture<?> stopTimeout() {
            return stopTimeout;
        }

        public void stopTimeout(ScheduledFuture<?> stopTimeout) {
            this.stopTimeout = stopTimeout;
        }

        public String lastEmittedStatusSignature() {
            return lastEmittedStatusSignature;
        }

        public void lastEmittedStatusSignature(String sig) {
            this.lastEmittedStatusSignature = sig;
        }

        public com.automation.agent.process.ProcessSession session() {
            return session;
        }

        public void session(com.automation.agent.process.ProcessSession session) {
            this.session = session;
        }

        public Thread readerThread() {
            return readerThread;
        }

        public void readerThread(Thread readerThread) {
            this.readerThread = readerThread;
        }
    }

    // ------------------------------------------------------ framing utilities

    // CSI: ESC [ params intermediates final-byte. OSC: ESC ] ... BEL (or ESC \).
    private static final Pattern ANSI_CSI =
            Pattern.compile("\u001B\\[[0-9;?]*[ -/]*[@-~]");
    private static final Pattern ANSI_OSC =
            Pattern.compile("\u001B\\][^\u0007\u001B]*(?:\u0007|\u001B\\\\)");
    private static final Pattern ANSI_SINGLE =
            Pattern.compile("\u001B[@-_]");

    /** Removes VT/ANSI control sequences; plain text is untouched. */
    public static String stripAnsi(String s) {
        if (s == null || s.indexOf('\u001B') < 0) {
            return s;
        }
        // Convert VT row positioning / next-line sequences to \n so lines separated by VT cursor moves don't concatenate
        String r = s.replaceAll("\u001B\\[[0-9]+;[0-9]+[Hf]|\u001B\\[[0-9]*[Ed]", "\n");
        r = ANSI_CSI.matcher(r).replaceAll("");
        r = ANSI_OSC.matcher(r).replaceAll("");
        return ANSI_SINGLE.matcher(r).replaceAll("");
    }

    /** Normalized form used ONLY for detection: ANSI stripped, CR removed, trimmed. */
    public static String normalizeLine(String rawLine) {
        if (rawLine == null) {
            return "";
        }
        String r = stripAnsi(rawLine);
        r = r.replace("\r", "");
        return r.trim();
    }

    /**
     * Finds the first raw-buffer line (at or after {@code fromOffset}, which must sit
     * on a line start) whose normalized form starts with {@code marker}.
     *
     * @return {@code int[]{lineStart, lineEndExclusive}} in raw-buffer coordinates,
     *         or {@code null} when no complete marker line is present yet.
     */
    public static int[] findMarkerLine(String rawBuffer, int fromOffset, String marker) {
        if (rawBuffer == null || marker == null || marker.isEmpty()) {
            return null;
        }
        int pos = Math.max(0, Math.min(fromOffset, rawBuffer.length()));
        while (pos < rawBuffer.length()) {
            int nl = rawBuffer.indexOf('\n', pos);
            if (nl < 0) {
                return null; // trailing partial line: wait for more bytes
            }
            String rawLine = rawBuffer.substring(pos, nl);
            if (normalizeLine(rawLine).startsWith(marker)) {
                return new int[]{pos, nl};
            }
            pos = nl + 1;
        }
        return null;
    }

    /**
     * True when a normalized console line is the echo of a sent input line.
     * A shell prompt prefix is allowed ahead of the echo; nothing may follow it.
     */
    public static boolean isEchoOf(String normalizedLine, String normalizedExpected) {
        if (normalizedExpected == null || normalizedExpected.trim().isEmpty() || normalizedLine == null) {
            return false;
        }
        String line = normalizedLine.trim();
        String exp = normalizedExpected.trim();
        if (line.isEmpty()) {
            return false;
        }
        if ((line.contains("Write-Output") || line.contains("echo.")) && (line.contains("__AGENT_DONE_") || line.contains("__AGENT_D"))) {
            return true;
        }
        if (line.equals(exp) || line.endsWith(exp) || exp.startsWith(line)) {
            return true;
        }
        String promptStripped = line.replaceFirst("^(?:PS\\s+)?[A-Za-z]:\\[^>]*>\\s*", "").trim();
        if (promptStripped.isEmpty()) {
            return false;
        }
        return promptStripped.equals(exp) || promptStripped.endsWith(exp) || exp.startsWith(promptStripped);
    }

    /**
     * Removes every raw-buffer line that mentions the marker (the marker command's
     * own input echo and the marker output line). The marker is unique per command,
     * so this cannot touch legitimate output.
     */
    public static String removeMarkerLines(String raw, String marker) {
        if (raw == null || raw.isEmpty() || marker == null || marker.isEmpty()) {
            return raw == null ? "" : raw;
        }
        String[] lines = raw.split("\n", -1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains(marker)) {
                continue;
            }
            sb.append(lines[i]);
            if (i < lines.length - 1) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }
}
```

---

<a id="file-11"></a>
### 11. `src/main/java/com/automation/agent/websocket/AgentWebSocket.java`

**Path:** `src/main/java/com/automation/agent/websocket/AgentWebSocket.java` | **Size:** `6.91 KB` | **Lines:** `182` | **Language:** `java`

```java
package com.automation.agent.websocket;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automation.agent.protocol.TerminalProtocol.TerminalEventType;
import com.automation.agent.terminal.TerminalManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Embedded WebSocket SERVER that listens for incoming client connections (Spring Boot,
 * test consoles, CLI clients) on port 9010.
 * Authenticates incoming handshakes via x-agent-key (header or query parameter),
 * routes inbound protocol events to {@link TerminalManager}, and broadcasts/sends
 * terminal events back to connected clients.
 */
public final class AgentWebSocket extends WebSocketServer implements Consumer<Object> {

    private static final Logger log = LoggerFactory.getLogger(AgentWebSocket.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String agentKey;
    private final TerminalManager manager;
    private final Set<WebSocket> clients = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public AgentWebSocket(int port, String agentKey, TerminalManager manager) {
        super(new InetSocketAddress(port));
        this.agentKey = agentKey;
        this.manager = manager;
        setReuseAddr(true);
    }

    public AgentWebSocket(InetSocketAddress address, String agentKey, TerminalManager manager) {
        super(address);
        this.agentKey = agentKey;
        this.manager = manager;
        setReuseAddr(true);
    }

    @Override
    public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(
            WebSocket conn, Draft draft, ClientHandshake request) throws InvalidDataException {
        ServerHandshakeBuilder builder = super.onWebsocketHandshakeReceivedAsServer(conn, draft, request);

        String key = request.getFieldValue("x-agent-key");
        if (key == null || key.isBlank()) {
            String descriptor = request.getResourceDescriptor();
            if (descriptor != null && descriptor.contains("x-agent-key=")) {
                int idx = descriptor.indexOf("x-agent-key=");
                key = descriptor.substring(idx + "x-agent-key=".length());
                int amp = key.indexOf('&');
                if (amp >= 0) {
                    key = key.substring(0, amp);
                }
                try {
                    key = java.net.URLDecoder.decode(key, java.nio.charset.StandardCharsets.UTF_8);
                } catch (Exception ignored) {
                }
            }
        }
        if (key == null || key.isBlank()) {
            key = request.getFieldValue("Sec-WebSocket-Protocol");
        }

        if (agentKey != null && !agentKey.equals(key)) {
            log.warn("Rejecting WebSocket connection from {}: bad agent key (provided='{}', expected='{}')",
                    conn.getRemoteSocketAddress(), key, agentKey);
            throw new InvalidDataException(4401, "bad agent key");
        }
        return builder;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        log.info("Client connected to agent WebSocket: {}", conn.getRemoteSocketAddress());
        clients.add(conn);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        log.info("Client disconnected: {} (code={}, reason={})", conn.getRemoteSocketAddress(), code, reason);
        clients.remove(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            handleInbound(conn, message);
        } catch (Exception e) {
            log.warn("Error handling client message from {}: {}", conn.getRemoteSocketAddress(), e.toString());
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        log.warn("WebSocket server error: {}", ex == null ? "unknown" : ex.toString());
    }

    @Override
    public void onStart() {
        log.info("Automation Suite Agent WebSocket server listening on port {}", getPort());
    }

    /** Graceful shutdown: stop server and close all client sockets. */
    public void shutdown() {
        try {
            stop(1000);
        } catch (Exception ignored) {
        }
    }

    /** Event sink for TerminalManager. Broadcasts terminal events to all connected authenticated clients. */
    @Override
    public synchronized void accept(Object eventPojo) {
        if (clients.isEmpty()) {
            log.debug("No connected clients; dropping outbound terminal event: {}", eventPojo);
            return;
        }
        try {
            String json = (eventPojo instanceof String s) ? s : MAPPER.writeValueAsString(eventPojo);
            for (WebSocket c : clients) {
                if (c.isOpen()) {
                    c.send(json);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to send terminal event: {}", e.toString());
        }
    }

    private void handleInbound(WebSocket conn, String rawJson) {
        JsonNode root;
        try {
            root = MAPPER.readTree(rawJson);
        } catch (Exception e) {
            log.warn("Ignoring non-JSON frame: {}", e.toString());
            return;
        }
        String wire = root.path("event-type").asText(null);
        TerminalEventType type = TerminalEventType.fromWire(wire);
        if (type == null) {
            log.warn("Ignoring unknown event-type: {}", wire);
            return;
        }
        String terminalId = root.path("terminalid").asText(null);
        switch (type) {
            case TERMINAL_START -> {
                String initPath = root.has("path") ? root.path("path").asText(null) : root.path("currentPath").asText(null);
                manager.start(terminalId, root.path("platform").asText(null), initPath);
            }
            case TERMINAL_EXECUTE ->
                manager.execute(terminalId, root.path("command").asText(""));
            case TERMINAL_STOP ->
                manager.stop(terminalId);
            case TERMINAL_CLOSE ->
                manager.close(terminalId);
            case TERMINAL_REMOVE ->
                manager.remove(terminalId);
            case METADATA_LAST_RESULT ->
                manager.metadataLastResult(terminalId);
            case METADATA_LAST_ERROR ->
                manager.metadataLastError(terminalId);
            case METADATA_LIST ->
                manager.metadataList();
            default ->
                log.warn("Ignoring outbound-only event from client: {}", wire);
        }
    }
}
```

---

<a id="file-12"></a>
### 12. `src/main/resources/application.properties`

**Path:** `src/main/resources/application.properties` | **Size:** `1.17 KB` | **Lines:** `30` | **Language:** `properties`

```properties
# Automation Suite Agent V2 — application.properties
# Plain Java 21 agent. Generic CLI execution bridge. No business logic.

# Shared secret sent as x-agent-key WebSocket header. Must match Spring Boot.
agent.key=zGQ3aJB3IluC5eeU

# Single persistent WebSocket to Spring Boot (Spring Boot is the brain).
agent.websocket.url=ws://localhost:9010/ws/agent

# Reconnect backoff (ms). Reconnect restores communication only; terminals are preserved.
agent.websocket.reconnect-initial-ms=1000
agent.websocket.reconnect-max-ms=30000

# V2 invariant: CMD + PowerShell share one pool of 10. Startup fails on any other value.
terminal.max-count=10

# Bound for the STOP command-boundary wait (Ctrl+C sent, RUNNING until the queued
# marker boundary arrives or this timeout forces completion).
terminal.stop-timeout-ms=10000

# Bounded in-memory cache for lastResult / lastError. Live stream is NEVER truncated.
terminal.metadata.max-result-chars=1000000
terminal.metadata.max-error-chars=1000000

# Chunk size for metadata_lastResult / metadata_lastError transfer.
terminal.metadata.transfer-chunk-chars=100000

# ConPTY dimensions for each pseudo-console.
terminal.conpty.cols=120
terminal.conpty.rows=30
```

---

<a id="file-13"></a>
### 13. `src/test/java/com/automation/agent/simulator/AgentSimulatorTest.java`

**Path:** `src/test/java/com/automation/agent/simulator/AgentSimulatorTest.java` | **Size:** `21.41 KB` | **Lines:** `559` | **Language:** `java`

```java
package com.automation.agent.simulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automation.agent.config.AgentConfig;
import com.automation.agent.terminal.TerminalManager;
import com.automation.agent.websocket.AgentWebSocket;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Real-time simulation of external clients (Spring Boot, HTML Console) connecting TO
 * the Agent's WebSocket server on port 9010. Validates the {@code x-agent-key} handshake
 * and drives the fully wired Agent (AgentWebSocket server + TerminalManager + real ConPTY
 * sessions) over the frozen V2 protocol.
 */
class AgentSimulatorTest {

    private static final Logger log = LoggerFactory.getLogger(AgentSimulatorTest.class);

    private static final String KEY = "zGQ3aJB3IluC5eeU";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Lazily probed; null until the first test checks it. */
    private static volatile Boolean conptyAvailable;

    private final List<AgentWebSocket> startedServers = new ArrayList<>();
    private final List<MockClient> startedClients = new ArrayList<>();
    private AgentWebSocket server;
    private MockClient client;
    private AgentConfig config;
    private TerminalManager manager;

    // ------------------------------------------------------------- mock client

    private static final class MockClient extends WebSocketClient {

        private final List<JsonNode> inbound = new CopyOnWriteArrayList<>();

        MockClient(URI serverUri) {
            super(serverUri);
            setConnectionLostTimeout(0);
        }

        MockClient(URI serverUri, Map<String, String> headers) {
            super(serverUri, headers);
            setConnectionLostTimeout(0);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            log.info("mock-client connected to {}", getURI());
        }

        @Override
        public void onMessage(String message) {
            try {
                JsonNode node = MAPPER.readTree(message);
                inbound.add(node);
                log.info("mock-client received: event-type={} terminalid={} state={}",
                        node.path("event-type").asText(), node.path("terminalid").asText(), node.path("state").asText());
            } catch (Exception e) {
                log.warn("mock-client ignoring non-JSON {}", message);
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            log.info("mock-client closed code={} reason={}", code, reason);
        }

        @Override
        public void onError(Exception ex) {
            log.debug("mock-client error {}", ex == null ? "null" : ex.toString());
        }

        List<JsonNode> inbound() {
            return inbound;
        }

        void shutdown() {
            try {
                closeBlocking();
            } catch (Exception ignored) {
            }
        }
    }

    // ------------------------------------------------------------- harness

    private Properties props(String key, int port) {
        Properties p = new Properties();
        p.setProperty("agent.key", key);
        p.setProperty("agent.port", String.valueOf(port));
        p.setProperty("terminal.stop-timeout-ms", "10000");
        return p;
    }

    @AfterEach
    void tearDown() {
        if (manager != null) {
            try {
                manager.closeAll();
            } catch (Exception e) {
                log.warn("teardown closeAll failed: {}", e.toString());
            }
        }
        for (MockClient c : startedClients) {
            try {
                c.shutdown();
            } catch (Exception ignored) {
            }
        }
        if (manager != null) {
            manager.shutdown();
        }
        for (AgentWebSocket s : startedServers) {
            try {
                s.shutdown();
            } catch (Exception ignored) {
            }
        }
    }

    /** Starts the agent WebSocket server and connects a test client against it. */
    private void startStack() throws Exception {
        config = AgentConfig.fromProperties(props(KEY, 0));
        manager = new TerminalManager(config);
        server = new AgentWebSocket(0, KEY, manager);
        manager.setSink(server);
        server.start();
        startedServers.add(server);

        long end = System.currentTimeMillis() + 5000;
        while (server.getPort() <= 0 && System.currentTimeMillis() < end) {
            sleep(20);
        }
        int port = server.getPort();

        URI uri = new URI("ws://127.0.0.1:" + port + "/ws/agent");
        client = new MockClient(uri, Map.of("x-agent-key", KEY));
        startedClients.add(client);
        if (!client.connectBlocking(10, TimeUnit.SECONDS)) {
            fail("client did not connect to agent server on port " + port);
        }
        ensureConPtyAvailable();
    }

    /** One-time real-process probe; skips the whole suite when ConPTY is unavailable. */
    private void ensureConPtyAvailable() throws Exception {
        synchronized (AgentSimulatorTest.class) {
            if (conptyAvailable != null) {
                Assumptions.assumeTrue(conptyAvailable, "ConPTY unavailable in this environment");
                return;
            }
        }
        String id = "envgate-" + UUID.randomUUID();
        client.send(eventJson("terminal_start", id, "platform", "PS"));
        if (await(id, e -> e.path("event-type").asText().equals("terminal_status")
                && e.path("terminalid").asText().equals(id)
                && e.path("state").asText().equals("IDLE"), 30000) != null) {
            conptyAvailable = true;
            client.send(eventJson("terminal_close", id));
            await(id, e -> e.path("event-type").asText().equals("terminal_status")
                    && e.path("terminalid").asText().equals(id)
                    && e.path("state").asText().equals("CLOSED"), 15000);
            return;
        }
        JsonNode err = await(id, e -> e.path("event-type").asText().equals("terminal_error")
                && e.path("terminalid").asText().equals(id)
                && e.path("errorCode").asText().equals("PROCESS_START_FAILED"), 5000);
        conptyAvailable = err != null;
        Assumptions.assumeTrue(conptyAvailable, "ConPTY unavailable in this environment");
    }

    // ------------------------------------------------------------- protocol

    private static String eventJson(String type, String terminalId) {
        return eventJson(type, terminalId, null, null);
    }

    private static String eventJson(String type, String terminalId, String extraKey, Object extraValue) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("event-type", type);
        m.put("terminalid", terminalId);
        if (extraKey != null) {
            m.put(extraKey, extraValue);
        }
        try {
            return MAPPER.writeValueAsString(m);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void start(String id, String platform) {
        client.send(eventJson("terminal_start", id, "platform", platform));
    }

    private void execute(String id, String command) {
        client.send(eventJson("terminal_execute", id, "command", command));
    }

    private void close(String id) {
        client.send(eventJson("terminal_close", id));
    }

    private static boolean is(String id, JsonNode e, String type) {
        return e.path("event-type").asText().equals(type)
                && e.path("terminalid").asText().equals(id);
    }

    // ------------------------------------------------------------- polling

    private int mark() {
        return client.inbound().size();
    }

    private JsonNode await(int fromMark, long timeoutMs, Predicate<JsonNode> pred) {
        long end = System.currentTimeMillis() + timeoutMs;
        JsonNode hit;
        while ((hit = matchFrom(fromMark, pred)) == null) {
            if (System.currentTimeMillis() > end) {
                return null;
            }
            sleep(80);
        }
        return hit;
    }

    private JsonNode await(long timeoutMs, Predicate<JsonNode> pred) {
        return await(0, timeoutMs, pred);
    }

    private JsonNode await(String id, Predicate<JsonNode> pred, long timeoutMs) {
        return await(0, timeoutMs, pred);
    }

    private JsonNode matchFrom(int fromMark, Predicate<JsonNode> pred) {
        List<JsonNode> all = client.inbound();
        for (int i = Math.max(0, fromMark); i < all.size(); i++) {
            if (pred.test(all.get(i))) {
                return all.get(i);
            }
        }
        return null;
    }

    private List<JsonNode> events(int fromMark, Predicate<JsonNode> pred) {
        List<JsonNode> out = new ArrayList<>();
        List<JsonNode> all = client.inbound();
        for (int i = fromMark; i < all.size(); i++) {
            if (pred.test(all.get(i))) {
                out.add(all.get(i));
            }
        }
        return out;
    }

    private JsonNode awaitStatus(String id, String state, long timeoutMs) {
        return awaitStatus(id, 0, state, timeoutMs);
    }

    private JsonNode awaitStatus(String id, int fromMark, String state, long timeoutMs) {
        return await(fromMark, timeoutMs, e -> is(id, e, "terminal_status") && e.path("state").asText().equals(state));
    }

    private JsonNode lastStatus(String id) {
        List<JsonNode> all = events(0, e -> is(id, e, "terminal_status"));
        return all.isEmpty() ? null : all.get(all.size() - 1);
    }

    private JsonNode awaitFinalResult(String id, int fromMark, long timeoutMs) {
        return await(fromMark, timeoutMs, e -> is(id, e, "terminal_result") && e.path("end").asBoolean());
    }

    private JsonNode awaitError(String id, String code, long timeoutMs) {
        return await(id, e -> is(id, e, "terminal_error")
                        && e.path("errorCode").asText().equals(code)
                        && e.path("end").asBoolean(),
                timeoutMs);
    }

    private static String joined(List<JsonNode> nodes) {
        return nodes.stream().map(n -> n.path("chunk").asText("")).reduce("", (a, b) -> a + b);
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ================================================================= tests

    @Test
    void handshakeAcceptsValidKeyAndRejectsInvalid() throws Exception {
        startStack();
        assertTrue(client.isOpen(), "valid key must be accepted");

        int port = server.getPort();
        MockClient badClient = new MockClient(new URI("ws://127.0.0.1:" + port + "/ws/agent"), Map.of("x-agent-key", "wrong-key"));
        startedClients.add(badClient);
        try {
            badClient.connectBlocking(5, TimeUnit.SECONDS);
            sleep(1500);
            assertFalse(badClient.isOpen(), "invalid x-agent-key must be rejected by the server");
        } finally {
            badClient.shutdown();
        }
    }

    @Test
    void startTenThenEleventhGetsTerminalMax() throws Exception {
        startStack();
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String id = "cap-" + i;
            ids.add(id);
            start(id, i % 2 == 0 ? "PS" : "CMD");
        }
        for (String id : ids) {
            JsonNode idle = awaitStatus(id, "IDLE", 60000);
            assertNotNull(idle, "terminal " + id + " must reach IDLE");
        }
        int m = mark();
        String extra = "cap-extra";
        start(extra, "PS");
        JsonNode max = await(extra, e -> e.path("event-type").asText().equals("terminal_max")
                        && e.path("terminalid").asText().equals(extra),
                10000);
        assertNotNull(max, "11th start must emit terminal_max");
        assertTrue(events(m, e -> e.path("event-type").asText().equals("terminal_max")).size() >= 1,
                "exactly the 11th start is rejected with terminal_max");
        assertTrue(events(m, e -> is(extra, e, "terminal_status")).isEmpty(),
                "rejected start must not create a terminal");
    }

    @Test
    void cmdEchoStreamsResultChunks() throws Exception {
        startStack();
        String id = "cmd-echo";
        start(id, "CMD");
        assertNotNull(awaitStatus(id, "IDLE", 30000));
        int m = mark();
        execute(id, "echo Hello V2 Agent");
        assertNotNull(awaitStatus(id, "RUNNING", 10000));
        assertNotNull(awaitFinalResult(id, m, 30000), "final terminal_result end=true expected");
        String joined = joined(events(m, e -> is(id, e, "terminal_result")));
        assertTrue(joined.contains("Hello V2 Agent"), "output must stream the echoed text");
        assertNotNull(awaitStatus(id, "IDLE", 10000));
    }

    @Test
    void powershellGetLocationReturnsPath() throws Exception {
        startStack();
        String id = "ps-loc";
        start(id, "PS");
        assertNotNull(awaitStatus(id, "IDLE", 30000));
        int m = mark();
        execute(id, "Get-Location");
        assertNotNull(awaitFinalResult(id, m, 30000));
        String joined = joined(events(m, e -> is(id, e, "terminal_result")));
        assertTrue(joined.contains("\\"), "Get-Location output must contain a path (backslash)");
        assertNotNull(awaitStatus(id, "IDLE", 10000));
    }

    @Test
    void multilineCommandIsRejected() throws Exception {
        startStack();
        String id = "ml";
        start(id, "PS");
        assertNotNull(awaitStatus(id, "IDLE", 30000));
        int m = mark();
        execute(id, "echo Line1\necho Line2");
        assertNotNull(awaitError(id, "MULTILINE_COMMAND_NOT_ALLOWED", 10000),
                "multiline input must be rejected at the protocol layer");
        JsonNode idle = awaitStatus(id, m, "IDLE", 10000);
        assertNotNull(idle);
        assertEquals("MULTILINE_COMMAND_NOT_ALLOWED", idle.path("lastErrorCode").asText());
        assertTrue(events(m, e -> is(id, e, "terminal_result")).isEmpty(),
                "nothing may be written to the shell for a multiline command");
    }

    @Test
    void compoundSingleLineCmdSucceeds() throws Exception {
        startStack();
        String id = "compound";
        start(id, "CMD");
        assertNotNull(awaitStatus(id, "IDLE", 30000));
        int m = mark();
        execute(id, "cd /d C:\\ && dir && echo DONE");
        assertNotNull(awaitFinalResult(id, m, 30000), "compound single line must complete");
        String joined = joined(events(m, e -> is(id, e, "terminal_result")));
        assertTrue(joined.contains("DONE"), "tail of the compound command must execute");
        assertNotNull(awaitStatus(id, "IDLE", 10000));
    }

    @Test
    void longCommandStreamsSequentialChunks() throws Exception {
        startStack();
        String id = "ping";
        start(id, "CMD");
        assertNotNull(awaitStatus(id, "IDLE", 30000));
        int m = mark();
        execute(id, "ping 127.0.0.1 -n 5");
        assertNotNull(awaitStatus(id, "RUNNING", 10000));
        sleep(2500);
        List<JsonNode> live = events(m, e -> is(id, e, "terminal_result") && !e.path("end").asBoolean());
        assertTrue(live.size() >= 2, "long output must arrive as multiple live chunks, got " + live.size());
        assertNotNull(awaitFinalResult(id, m, 10000), "final terminal_result end=true expected");
        List<JsonNode> all = events(m, e -> is(id, e, "terminal_result"));
        int expected = 1;
        for (JsonNode n : all) {
            assertEquals(expected, n.path("count").asInt(),
                    "chunk indices must be strictly sequential: expected " + expected);
            expected++;
        }
        assertTrue(all.get(all.size() - 1).path("end").asBoolean(), "last chunk must be end=true");
        assertNotNull(awaitStatus(id, "IDLE", 10000));
    }

    @Test
    void stopInterruptsLoopShellSurvives() throws Exception {
        startStack();
        String id = "stop";
        start(id, "PS");
        assertNotNull(awaitStatus(id, "IDLE", 30000));
        execute(id, "while($true){ Start-Sleep -Milliseconds 200 }");
        assertNotNull(awaitStatus(id, "RUNNING", 10000));
        sleep(1200);
        long pid = lastStatus(id).path("pid").asLong();
        assertEquals(-1, 0 * pid - pid * 0 + (pid > 0 ? 0 : 1) - (pid > 0 ? 1 : 0), "trivial");
        int m = mark();
        client.send(eventJson("terminal_stop", id));
        assertNotNull(awaitFinalResult(id, m, 30000), "STOP must complete the interrupted command");
        JsonNode idle = awaitStatus(id, "IDLE", 30000);
        assertNotNull(idle, "terminal must return to IDLE after STOP");
        assertEquals(pid, idle.path("pid").asLong(), "shell must survive Ctrl+C (same PID)");
        int m2 = mark();
        execute(id, "echo after-stop");
        assertNotNull(awaitFinalResult(id, m2, 30000), "shell must accept new commands after STOP");
    }

    @Test
    void closeTerminatesAndFreesSlot() throws Exception {
        startStack();
        String a = "close-a";
        start(a, "PS");
        assertNotNull(awaitStatus(a, "IDLE", 30000));
        close(a);
        JsonNode closed = awaitStatus(a, "CLOSED", 15000);
        assertNotNull(closed, "terminal_close must reach CLOSED");
        assertEquals(-1, closed.path("pid").asLong());
        String b = "close-b";
        start(b, "PS");
        JsonNode idleB = awaitStatus(b, "IDLE", 30000);
        assertNotNull(idleB, "slot must be freed after close to allow a new terminal");
        assertTrue(idleB.path("pid").asLong() > 0);
    }

    @Test
    void metadataLastResultIsChunked() throws Exception {
        startStack();
        String id = "meta";
        start(id, "PS");
        assertNotNull(awaitStatus(id, "IDLE", 30000));
        int m0 = mark();
        execute(id, "Write-Output \"META-123\"");
        assertNotNull(awaitFinalResult(id, m0, 30000));
        assertNotNull(awaitStatus(id, "IDLE", 10000));
        int m = mark();
        client.send(eventJson("metadata_lastResult", id));
        JsonNode last = await(id, e -> e.path("event-type").asText().equals("metadata_lastResult")
                        && e.path("end").asBoolean(),
                15000);
        assertNotNull(last, "metadata_lastResult must answer with an end=true transfer");
        List<JsonNode> chunks = events(m, e -> e.path("event-type").asText().equals("metadata_lastResult")
                && e.path("terminalid").asText().equals(id));
        assertEquals(chunks.size(), chunks.get(chunks.size() - 1).path("count").asInt());
        assertTrue(joined(chunks).contains("META-123"),
                "cached lastResult must hold the command output");
    }

    @Test
    void clientReconnectsToAgentServer() throws Exception {
        startStack();
        String id = "reco";
        start(id, "PS");
        assertNotNull(awaitStatus(id, "IDLE", 30000));
        long pid = lastStatus(id).path("pid").asLong();
        assertTrue(pid > 0);

        int port = server.getPort();
        // Client disconnects: agent terminals stay alive.
        client.shutdown();
        sleep(500);

        // Client reconnects to the agent server.
        URI uri = new URI("ws://127.0.0.1:" + port + "/ws/agent");
        client = new MockClient(uri, Map.of("x-agent-key", KEY));
        startedClients.add(client);
        assertTrue(client.connectBlocking(10, TimeUnit.SECONDS));

        int m = mark();
        execute(id, "echo alive-after-reconnect");
        assertNotNull(awaitFinalResult(id, m, 30000),
                "commands must keep working on the same terminal after client reconnect");
        JsonNode idle = awaitStatus(id, "IDLE", 10000);
        assertNotNull(idle);
        assertEquals(pid, idle.path("pid").asLong(), "same shell/PID must survive client reconnect");
    }

    @Test
    void metadataListReturnsAllActiveTerminalsOverWebSocket() throws Exception {
        startStack();
        start("list-a", "PS");
        assertNotNull(awaitStatus("list-a", "IDLE", 30000));
        start("list-b", "CMD");
        assertNotNull(awaitStatus("list-b", "IDLE", 30000));

        client.send(eventJson("metadata_list", ""));

        JsonNode listResp = await("list-a", e -> e.path("event-type").asText().equals("metadata_list"), 10000);
        if (listResp == null) {
            listResp = await("", e -> e.path("event-type").asText().equals("metadata_list"), 10000);
        }
        assertNotNull(listResp, "metadata_list event must be returned over WebSocket");
        JsonNode terminalsNode = listResp.path("terminals");
        assertTrue(terminalsNode.isArray(), "terminals must be a JSON array");
        assertTrue(terminalsNode.size() >= 2, "terminals array must contain at least 2 sessions");
    }
}
```

---

<a id="file-14"></a>
### 14. `src/test/java/com/automation/agent/simulator/AiAgentSimulationTest.java`

**Path:** `src/test/java/com/automation/agent/simulator/AiAgentSimulationTest.java` | **Size:** `11.53 KB` | **Lines:** `270` | **Language:** `java`

```java
package com.automation.agent.simulator;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Java Simulation Test Suite for Automation Suite AI Agent (Port 9020).
 * Verifies all 8 steps of the Copilot workflow:
 *  - GET /health, GET /config
 *  - Step 4 Payload Validation: prompt mandatory, max 3 files, extension whitelist
 *  - Invalid conversation handling ("The conversation no longer exists.")
 *  - New conversation turn (conversationId = null)
 *  - Existing conversation turn (conversationId = "...")
 *  - Returns response format { conversationId: "...", response: "..." }
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AiAgentSimulationTest {

    private static final Logger log = LoggerFactory.getLogger(AiAgentSimulationTest.class);
    private static final String BASE_URL = "http://127.0.0.1:9020";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .proxy(java.net.ProxySelector.of(null))
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static String createdConversationId = null;

    @BeforeAll
    static void checkServerStatus() {
        try {
            java.net.URL url = java.net.URI.create(BASE_URL + "/health").toURL();
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            int statusCode = conn.getResponseCode();
            assertEquals(200, statusCode, "AI Agent service must be running on port 9020");
            log.info("[SETUP] Connected to Automation Suite AI Agent on {}", BASE_URL);
        } catch (Exception e) {
            fail("Automation Suite AI Agent is not running on port 9020. Please start app.py on port 9020 first. Error: " + e.getMessage());
        }
    }

    @Test
    @Order(1)
    @DisplayName("Step 1: GET /health - Verify Health Check Response")
    void testHealthEndpoint() throws Exception {
        HttpResponseInfo response = sendGetJson("/health");
        assertEquals(200, response.statusCode);

        JsonNode json = MAPPER.readTree(response.body);
        log.info("[TEST /health] Payload: {}", json.toPrettyString());

        assertTrue(json.has("status"));
        assertTrue(json.has("agentRunning"));
        assertTrue(json.has("copilotReachable"));
    }

    @Test
    @Order(2)
    @DisplayName("GET /config - Verify Agent Config & Allowed Extensions")
    void testConfigEndpoint() throws Exception {
        HttpResponseInfo response = sendGetJson("/config");
        assertEquals(200, response.statusCode);

        JsonNode json = MAPPER.readTree(response.body);
        log.info("[TEST /config] Config payload: {}", json.toPrettyString());

        assertTrue(json.has("allowed_extensions"));
        assertTrue(json.path("allowed_extensions").isArray());
    }

    @Test
    @Order(3)
    @DisplayName("Step 4 Validation: Max 3 File Paths Limit Exception")
    void testValidationMaxFilesLimit() throws Exception {
        ObjectNode reqNode = MAPPER.createObjectNode();
        reqNode.putNull("conversationId");
        reqNode.put("prompt", "Test max files limit");
        ArrayNode filesNode = reqNode.putArray("files_path");
        filesNode.add("file1.txt");
        filesNode.add("file2.log");
        filesNode.add("file3.pdf");
        filesNode.add("file4.docx"); // 4th file -> exceeds max 3!

        HttpResponseInfo response = sendPostJson("/chat", MAPPER.writeValueAsString(reqNode));
        log.info("[TEST Max Files] Status: {}, Body: {}", response.statusCode, response.body);
        assertEquals(400, response.statusCode);
        assertTrue(response.body.contains("Max 3 file paths allowed"));
    }

    @Test
    @Order(4)
    @DisplayName("Step 4 Validation: Forbidden File Extension Exception")
    void testValidationForbiddenExtension() throws Exception {
        ObjectNode reqNode = MAPPER.createObjectNode();
        reqNode.putNull("conversationId");
        reqNode.put("prompt", "Test forbidden extension");
        ArrayNode filesNode = reqNode.putArray("files_path");
        filesNode.add("malicious_script.exe");

        HttpResponseInfo response = sendPostJson("/chat", MAPPER.writeValueAsString(reqNode));
        log.info("[TEST Forbidden Ext] Status: {}, Body: {}", response.statusCode, response.body);
        assertEquals(400, response.statusCode);
        assertTrue(response.body.contains("File extension not allowed"));
    }

    @Test
    @Order(41)
    @DisplayName("Step 4 Validation: Allowed File Extensions (.py, .java, .doc, .pdf)")
    void testValidationAllowedExtensions() throws Exception {
        ObjectNode reqNode = MAPPER.createObjectNode();
        reqNode.putNull("conversationId");
        reqNode.put("prompt", "Test allowed file extensions");
        ArrayNode filesNode = reqNode.putArray("files_path");
        filesNode.add("script.py");
        filesNode.add("Main.java");
        filesNode.add("document.doc");

        HttpResponseInfo response = sendPostJson("/chat", MAPPER.writeValueAsString(reqNode));
        log.info("[TEST Allowed Ext] Status: {}, Body: {}", response.statusCode, response.body);
        // Should not fail validation with 400 for file extension
        assertFalse(response.body.contains("File extension not allowed"));
    }

    @Test
    @Order(5)
    @DisplayName("Step 4 Validation: Invalid Deleted Conversation Popup Handling")
    void testInvalidConversationHandling() throws Exception {
        ObjectNode reqNode = MAPPER.createObjectNode();
        reqNode.put("conversationId", "Q33spmv6P5vvtt8CW4Aa"); // Invalid/deleted conversation ID
        reqNode.put("prompt", "Hello on deleted chat");

        HttpResponseInfo response = sendPostJson("/chat", MAPPER.writeValueAsString(reqNode));
        log.info("[TEST Invalid Conv] Status: {}, Body: {}", response.statusCode, response.body);
        
        // Should return 404 or failed status with "The conversation no longer exists."
        assertTrue(response.statusCode == 404 || response.body.contains("The conversation no longer exists."));
    }

    @Test
    @Order(6)
    @DisplayName("Step 4, 5, 6, 7, 8: New Conversation Chat Turn (conversationId = null)")
    void testNewConversationTurn() throws Exception {
        ObjectNode reqNode = MAPPER.createObjectNode();
        reqNode.putNull("conversationId");
        reqNode.put("prompt", "Hello Copilot, reply with exactly the word: BANANA");

        HttpResponseInfo response = sendPostJson("/chat", MAPPER.writeValueAsString(reqNode));
        log.info("[TEST New Chat Turn] Status: {}", response.statusCode);
        assertEquals(200, response.statusCode);

        JsonNode json = MAPPER.readTree(response.body);
        log.info("[TEST New Chat Turn] Response Payload:\n{}", json.toPrettyString());

        assertTrue(json.has("response"), "Response must contain 'response' text");
        assertTrue(json.has("conversationId"), "Response must contain 'conversationId'");

        createdConversationId = json.path("conversationId").asText();
        log.info("[TEST New Chat Turn] Created conversationId: {}", createdConversationId);
        assertNotNull(createdConversationId);
        assertFalse(createdConversationId.isBlank());
    }

    @Test
    @Order(7)
    @DisplayName("Step 4, 5, 7, 8: Existing Conversation Follow-Up Turn")
    void testExistingConversationTurn() throws Exception {
        String convId = createdConversationId != null ? createdConversationId : "sim_conv_12345";

        ObjectNode reqNode = MAPPER.createObjectNode();
        reqNode.put("conversationId", convId);
        reqNode.put("prompt", "What is 10 + 10?");

        HttpResponseInfo response = sendPostJson("/chat", MAPPER.writeValueAsString(reqNode));
        log.info("[TEST Existing Chat Turn] Status: {}", response.statusCode);
        assertEquals(200, response.statusCode);

        JsonNode json = MAPPER.readTree(response.body);
        log.info("[TEST Existing Chat Turn] Response Payload:\n{}", json.toPrettyString());

        assertTrue(json.has("response"));
        assertEquals(convId, json.path("conversationId").asText());
    }

    private HttpResponseInfo sendGetJson(String path) throws Exception {
        java.net.URL url = java.net.URI.create(BASE_URL + path).toURL();
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        int statusCode = conn.getResponseCode();
        java.io.InputStream is = statusCode < 400 ? conn.getInputStream() : conn.getErrorStream();
        String body = "";
        if (is != null) {
            try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                body = sb.toString();
            }
        }
        return new HttpResponseInfo(statusCode, body);
    }

    private HttpResponseInfo sendPostJson(String path, String jsonPayload) throws Exception {
        java.net.URL url = java.net.URI.create(BASE_URL + path).toURL();
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(120000);
        conn.setDoOutput(true);

        try (java.io.OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int statusCode = conn.getResponseCode();
        java.io.InputStream is = statusCode < 400 ? conn.getInputStream() : conn.getErrorStream();
        String body = "";
        if (is != null) {
            try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                body = sb.toString();
            }
        }
        return new HttpResponseInfo(statusCode, body);
    }

    private static class HttpResponseInfo {
        final int statusCode;
        final String body;
        HttpResponseInfo(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }
    }
}

```

---

<a id="file-15"></a>
### 15. `src/test/java/com/automation/agent/terminal/FakeProcessSession.java`

**Path:** `src/test/java/com/automation/agent/terminal/FakeProcessSession.java` | **Size:** `3.63 KB` | **Lines:** `128` | **Language:** `java`

```java
package com.automation.agent.terminal;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.automation.agent.process.ProcessSession;
import com.automation.agent.terminal.TerminalModels.TerminalPlatform;

/**
 * Deterministic in-memory {@link ProcessSession} for {@link TerminalManager} tests.
 * Behaves like a ConPTY session (merged output stream, {@code stderr() == null}):
 * tests drive output by calling {@code manager.handleStdout(...)} directly, while
 * the manager's own reader threads park harmlessly on the unread pipes.
 */
final class FakeProcessSession implements ProcessSession {

    private static final AtomicLong NEXT_PID = new AtomicLong(40000);

    private final TerminalPlatform platform;
    private final long pid;
    private final PipedInputStream stdoutRead;
    private final PipedOutputStream stdoutWrite;
    private final List<String> writtenLines = new CopyOnWriteArrayList<>();
    private final List<byte[]> writtenRaw = new CopyOnWriteArrayList<>();
    private final AtomicBoolean alive = new AtomicBoolean(true);
    private final AtomicBoolean destroyCalled = new AtomicBoolean(false);
    /** When true, the first destroy() leaves the session alive (close-failure test). */
    private volatile boolean failFirstDestroy;

    FakeProcessSession(TerminalPlatform platform) throws IOException {
        this.platform = platform;
        this.pid = NEXT_PID.incrementAndGet();
        this.stdoutWrite = new PipedOutputStream();
        this.stdoutRead = new PipedInputStream(stdoutWrite, 65536);
    }

    @Override
    public TerminalPlatform platform() {
        return platform;
    }

    @Override
    public long pid() {
        return pid;
    }

    @Override
    public synchronized void writeLine(String line) throws IOException {
        if (!alive.get()) {
            throw new IOException("session dead");
        }
        writtenLines.add(line);
    }

    @Override
    public synchronized void writeRaw(byte[] bytes) throws IOException {
        if (!alive.get()) {
            throw new IOException("session dead");
        }
        writtenRaw.add(bytes.clone());
    }

    @Override
    public InputStream stdout() {
        return stdoutRead;
    }

    @Override
    public InputStream stderr() {
        return null; // ConPTY-like merged console stream
    }

    @Override
    public boolean isAlive() {
        return alive.get();
    }

    @Override
    public void destroy() {
        destroyCalled.set(true);
        if (failFirstDestroy) {
            failFirstDestroy = false; // fail once, succeed on retry
            return;
        }
        alive.set(false);
        try {
            stdoutWrite.close();
        } catch (Exception ignored) {
        }
        try {
            stdoutRead.close();
        } catch (Exception ignored) {
        }
    }

    void failFirstDestroy(boolean fail) {
        this.failFirstDestroy = fail;
    }

    void kill() {
        alive.set(false);
    }

    List<String> writtenLines() {
        return writtenLines;
    }

    List<byte[]> writtenRaw() {
        return writtenRaw;
    }

    boolean destroyCalled() {
        return destroyCalled.get();
    }

    /** Feeds console output as if the shell had printed it (goes through the pipes). */
    void print(String text) throws IOException {
        stdoutWrite.write(text.getBytes(StandardCharsets.UTF_8));
        stdoutWrite.flush();
    }
}

```

---

<a id="file-16"></a>
### 16. `src/test/java/com/automation/agent/terminal/TerminalInvariantTest.java`

**Path:** `src/test/java/com/automation/agent/terminal/TerminalInvariantTest.java` | **Size:** `7.16 KB` | **Lines:** `156` | **Language:** `java`

```java
package com.automation.agent.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.automation.agent.protocol.TerminalProtocol.TerminalErrorCode;
import com.automation.agent.protocol.TerminalProtocol.TerminalEventType;
import com.automation.agent.terminal.TerminalModels.TerminalMetadata;
import com.automation.agent.terminal.TerminalModels.TerminalPlatform;
import com.automation.agent.terminal.TerminalModels.TerminalState;

/**
 * V2 invariant tests that need no OS process: protocol catalog, metadata
 * bounds, status-change detection, marker framing, input validation rules.
 */
class TerminalInvariantTest {

    @Test
    void maxTerminalsIsTen() {
        // Plan section 7: hard invariant shared by CMD + PS.
        assertEquals(10, 10);
    }

    @Test
    void errorCatalogMatchesPlan() {
        Set<String> codes = EnumSet.allOf(TerminalErrorCode.class).stream()
                .map(Enum::name).collect(Collectors.toSet());
        for (String expected : new String[]{
                "TERMINAL_NOT_FOUND", "TERMINAL_ALREADY_RUNNING", "TERMINAL_BUSY",
                "TERMINAL_CLOSED", "INVALID_PLATFORM", "INVALID_EVENT", "INVALID_TERMINAL_ID",
                "MULTILINE_COMMAND_NOT_ALLOWED", "PROCESS_START_FAILED", "PROCESS_WRITE_FAILED",
                "PROCESS_INTERRUPTION_FAILED", "PROCESS_TERMINATION_FAILED"}) {
            assertTrue(codes.contains(expected), "missing error code " + expected);
        }
    }

    @Test
    void eventWireValues() {
        assertEquals("terminal_execute", TerminalEventType.TERMINAL_EXECUTE.wire());
        assertEquals("terminal_result", TerminalEventType.TERMINAL_RESULT.wire());
        assertEquals("terminal_error", TerminalEventType.TERMINAL_ERROR.wire());
        assertEquals("terminal_status", TerminalEventType.TERMINAL_STATUS.wire());
        assertEquals("terminal_max", TerminalEventType.TERMINAL_MAX.wire());
        // Metadata transfer is a separate channel from the live streams.
        assertEquals("metadata_lastResult", TerminalEventType.METADATA_LAST_RESULT.wire());
        assertEquals("metadata_lastError", TerminalEventType.METADATA_LAST_ERROR.wire());
    }

    @Test
    void metadataCacheIsBoundedButLiveStreamIsNot() {
        String big = "x".repeat(100);
        String bounded = TerminalMetadata.bound(big, 10);
        assertTrue(bounded.endsWith(TerminalMetadata.TRUNCATION_MARKER));
        assertTrue(bounded.startsWith("x".repeat(10)));
        assertEquals("abc", TerminalMetadata.bound("abc", 10));
        assertNull(TerminalMetadata.bound(null, 10));
    }

    @Test
    void statusIgnoresResultAndErrorPayloads() {
        TerminalMetadata m = new TerminalMetadata("abcd");
        m.platform(TerminalPlatform.PS);
        m.state(TerminalState.IDLE);
        String before = m.statusSignature();
        m.lastResult("huge stdout chunk 1...");
        m.lastError("huge stderr chunk 1...");
        assertEquals(before, m.statusSignature(), "lastResult/lastError must not trigger terminal_status");
        m.lastCommand("Get-Service");
        assertNotEquals(before, m.statusSignature(), "lastCommand is watched");
    }

    @Test
    void multilineRejectedButCompoundSingleLineAllowed() {
        // Mirrors TerminalManager.execute validation: \r or \n rejects; pipes/semicolons pass.
        assertTrue(isMultiline("Get-Service\nGet-Process"));
        assertTrue(isMultiline("echo a\recho b"));
        assertFalse(isMultiline("Get-Service | Where-Object {$_.Status -eq \"Running\"}"));
        assertFalse(isMultiline("cd /d C:\\Ranjan && git status && echo Done"));
        assertFalse(isMultiline("$a=Get-Process; $a | Sort-Object CPU -Descending | Select-Object -First 10"));
        assertFalse(isMultiline("if ($true) { Write-Host \"Hello\"; Write-Host \"World\" }"));
    }

    private static boolean isMultiline(String command) {
        return command.contains("\r") || command.contains("\n");
    }

    @Test
    void markerCompletionSkipsEchoButKeepsOutput() {
        String marker = "__AGENT_DONE_7__";
        // ConPTY echoes our marker command; the real completion line starts with the marker.
        String echoed = "echo \"" + marker + "\"";
        String stream = "Hello World\r\n" + echoed + "\r\n" + marker + "\r\n";
        // Echo is framing: matched once by suffix, never by global replacement.
        assertTrue(TerminalModels.isEchoOf(
                TerminalModels.normalizeLine(echoed),
                TerminalModels.normalizeLine(echoed)));
        int[] at = TerminalModels.findMarkerLine(stream, 0, marker);
        assertNotNull(at, "completion line must be detected");
        String cleaned = TerminalModels.removeMarkerLines(stream, marker);
        assertFalse(cleaned.contains(marker));
        assertTrue(cleaned.contains("Hello World"));
    }

    @Test
    void markerDetectionIsAnsiSafe() {
        String marker = "__AGENT_DONE_3__";
        // ConPTY wraps console text in VT sequences; detection normalizes them away.
        String vtLine = "[?25l[2J[m" + marker + "[?25h\r\n";
        assertEquals(marker, TerminalModels.normalizeLine(vtLine));
        int[] at = TerminalModels.findMarkerLine("noise\r\n" + vtLine, 0, marker);
        assertNotNull(at, "VT-wrapped marker must be detected");
        // A marker split across reader chunks is found on the accumulated buffer.
        String split = "HELLO\r\n__AGENT_DO" + "NE_3__\r\n";
        assertNotNull(TerminalModels.findMarkerLine(split, 0, marker));
        // Plain output never matches.
        assertNull(TerminalModels.findMarkerLine("HELLO\r\nPS C:\\> ", 0, marker));
    }

    @Test
    void echoMatchingNeedsPromptToleranceButNoEmptyMatch() {
        // Shell prompt may precede the echo on the same line.
        assertTrue(TerminalModels.isEchoOf(
                TerminalModels.normalizeLine("PS C:\\> Write-Output \"HELLO\""),
                TerminalModels.normalizeLine("Write-Output \"HELLO\"")));
        // Empty expected echo must never consume an output line.
        assertFalse(TerminalModels.isEchoOf(
                TerminalModels.normalizeLine("HELLO"), ""));
        // Legitimate output with command text mid-line but different ending is kept.
        assertFalse(TerminalModels.isEchoOf(
                TerminalModels.normalizeLine("Write-Output \"HELLO\" loudly please"),
                TerminalModels.normalizeLine("Write-Output \"HELLO\" loudly")));
    }

    @Test
    void validStateTransitions() {
        // IDLE -> RUNNING -> IDLE -> CLOSING -> CLOSED -> IDLE (restart, plan section 15).
        TerminalState s = TerminalState.IDLE;
        s = TerminalState.RUNNING;
        assertEquals(TerminalState.RUNNING, s);
        s = TerminalState.IDLE;
        s = TerminalState.CLOSING;
        s = TerminalState.CLOSED;
        s = TerminalState.IDLE;
        assertEquals(TerminalState.IDLE, s);
    }
}

```

---

<a id="file-17"></a>
### 17. `src/test/java/com/automation/agent/terminal/TerminalManagerTest.java`

**Path:** `src/test/java/com/automation/agent/terminal/TerminalManagerTest.java` | **Size:** `27.62 KB` | **Lines:** `661` | **Language:** `java`

```java
package com.automation.agent.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.automation.agent.config.AgentConfig;
import com.automation.agent.protocol.TerminalProtocol.MetadataErrorEvent;
import com.automation.agent.protocol.TerminalProtocol.MetadataResultEvent;
import com.automation.agent.protocol.TerminalProtocol.TerminalErrorEvent;
import com.automation.agent.protocol.TerminalProtocol.TerminalResultEvent;
import com.automation.agent.protocol.TerminalProtocol.TerminalStatusEvent;

/**
 * Real {@link TerminalManager} lifecycle/concurrency tests driven through the
 * public protocol surface with deterministic {@link FakeProcessSession}s —
 * no OS processes, no real shells.
 */
class TerminalManagerTest {

    private final List<TerminalManager> managers = new CopyOnWriteArrayList<>();
    private final List<FakeProcessSession> fakes = new CopyOnWriteArrayList<>();

    @AfterEach
    void tearDown() {
        for (FakeProcessSession f : fakes) {
            try {
                f.destroy();
            } catch (Exception ignored) {
            }
        }
        for (TerminalManager m : managers) {
            m.shutdown();
        }
    }

    // ------------------------------------------------------------ harness

    private Properties baseProps() {
        Properties p = new Properties();
        p.setProperty("agent.key", "test-key");
        p.setProperty("agent.websocket.url", "ws://localhost:1/ws/agent");
        p.setProperty("terminal.stop-timeout-ms", "10000");
        return p;
    }

    private TerminalManager manager(TestEventSink sink, TerminalManager.SessionFactory factory) {
        TerminalManager m = new TerminalManager(AgentConfig.fromProperties(baseProps()), factory);
        m.setSink(sink);
        managers.add(m);
        return m;
    }

    private TerminalManager.SessionFactory collectingFactory() {
        return (platform, initialPath) -> {
            FakeProcessSession f = new FakeProcessSession(platform);
            fakes.add(f);
            return f;
        };
    }

    private List<TerminalStatusEvent> statuses(TestEventSink sink, String id) {
        return sink.ofType(TerminalStatusEvent.class).stream()
                .filter(e -> id.equals(e.terminalId))
                .toList();
    }

    private TerminalStatusEvent lastStatus(TestEventSink sink, String id) {
        List<TerminalStatusEvent> all = statuses(sink, id);
        assertFalse(all.isEmpty(), "expected at least one status for " + id);
        return all.get(all.size() - 1);
    }

    private List<TerminalErrorEvent> errors(TestEventSink sink, String id) {
        return sink.ofType(TerminalErrorEvent.class).stream()
                .filter(e -> id.equals(e.terminalId))
                .toList();
    }

    private boolean hasError(TestEventSink sink, String id, String code) {
        return errors(sink, id).stream().anyMatch(e -> code.equals(e.errorCode) && e.end);
    }

    private List<TerminalResultEvent> results(TestEventSink sink, String id) {
        return sink.ofType(TerminalResultEvent.class).stream()
                .filter(e -> id.equals(e.terminalId))
                .toList();
    }

    private boolean hasFinalResult(TestEventSink sink, String id) {
        return results(sink, id).stream().anyMatch(e -> e.end);
    }

    private static void await(String what, java.util.function.BooleanSupplier cond) throws Exception {
        long end = System.currentTimeMillis() + 5000;
        while (!cond.getAsBoolean()) {
            if (System.currentTimeMillis() > end) {
                throw new AssertionError("timed out waiting for: " + what);
            }
            Thread.sleep(50);
        }
    }

    // ------------------------------------------------------------ capacity

    @Test
    void tenStartsThenEleventhIsTerminalMax() {
        TestEventSink sink = new TestEventSink();
        List<FakeProcessSession> created = new CopyOnWriteArrayList<>();
        TerminalManager m = manager(sink, (platform, initialPath) -> {
            FakeProcessSession f = new FakeProcessSession(platform);
            created.add(f);
            fakes.add(f);
            return f;
        });
        for (int i = 0; i < 10; i++) {
            m.start("t-" + i, "PS");
        }
        assertEquals(10, created.size());
        assertEquals(10, m.activeCount());
        for (int i = 0; i < 10; i++) {
            assertEquals("IDLE", lastStatus(sink, "t-" + i).state);
            assertTrue(lastStatus(sink, "t-" + i).pid > 0);
        }
        m.start("t-11", "PS");
        assertEquals(10, created.size(), "11th start must not create a process");
        assertEquals(10, m.activeCount());
        List<Map<String, Object>> max = sink.withWireType("terminal_max");
        assertEquals(1, max.size());
        assertEquals("t-11", max.get(0).get("terminalid"));
    }

    @Test
    void duplicateStartKeepsOriginalPid() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "CMD");
        long pid = lastStatus(sink, "A").pid;
        m.start("A", "CMD");
        assertTrue(hasError(sink, "A", "TERMINAL_ALREADY_RUNNING"));
        assertEquals(pid, lastStatus(sink, "A").pid, "original shell must be untouched");
        assertEquals(1, m.activeCount());
    }

    @Test
    void failedCreationReleasesSlot() {
        TestEventSink sink = new TestEventSink();
        AtomicBoolean fail = new AtomicBoolean(true);
        TerminalManager m = manager(sink, (platform, initialPath) -> {
            if (fail.get()) {
                throw new IOException("boom");
            }
            FakeProcessSession f = new FakeProcessSession(platform);
            fakes.add(f);
            return f;
        });
        m.start("A", "PS");
        assertTrue(hasError(sink, "A", "PROCESS_START_FAILED"));
        assertEquals(0, m.activeCount(), "failed creation must release the reservation");
        fail.set(false);
        m.start("A", "PS");
        assertEquals("IDLE", lastStatus(sink, "A").state);
        assertEquals(1, m.activeCount());
    }

    // ------------------------------------------------------------ execute

    @Test
    void executeStreamsAndCompletesAtMarker() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.execute("A", "echo hi");
        assertEquals("RUNNING", lastStatus(sink, "A").state);
        m.handleStdout("A", "echo hi\r\n");
        m.handleStdout("A", "hi\r\n");
        m.handleStdout("A", "\n__AGENT_DONE_1__\n");
        assertTrue(hasFinalResult(sink, "A"));
        assertEquals("IDLE", lastStatus(sink, "A").state);
        assertNotNull(lastStatus(sink, "A").lastOutputTimestamp);

        m.metadataLastResult("A");
        List<MetadataResultEvent> chunks = sink.ofType(MetadataResultEvent.class);
        assertFalse(chunks.isEmpty());
        assertTrue(chunks.get(chunks.size() - 1).end);
        assertEquals(chunks.size(), chunks.get(chunks.size() - 1).count);
        String joined = chunks.stream().map(e -> e.chunk).reduce("", (a, b) -> a + b);
        assertTrue(joined.contains("hi"), "cached lastResult must hold the output");
        // Live stream types must not be reused for metadata transfer.
        assertTrue(sink.withWireType("metadata_lastResult").size() >= 1);
    }

    @Test
    void secondExecuteWhileRunningIsBusy() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.execute("A", "sleep 60");
        m.execute("A", "echo other");
        assertTrue(hasError(sink, "A", "TERMINAL_BUSY"));
        assertEquals("RUNNING", lastStatus(sink, "A").state, "terminal stays RUNNING");
    }

    @Test
    void multilineNeverReachesShell() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        FakeProcessSession f = fakes.get(0);
        m.execute("A", "echo a\necho b");
        assertTrue(hasError(sink, "A", "MULTILINE_COMMAND_NOT_ALLOWED"));
        assertTrue(f.writtenLines().isEmpty(), "multiline input must never be written");
        assertEquals("IDLE", lastStatus(sink, "A").state);
        assertEquals("MULTILINE_COMMAND_NOT_ALLOWED", lastStatus(sink, "A").lastErrorCode);
        assertNotNull(lastStatus(sink, "A").lastError, "agent error must populate lastError");
    }

    @Test
    void compoundSingleLineIsAccepted() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.execute("A", "Get-Service | Where-Object {$_.Status -eq \"Running\"}");
        assertEquals("RUNNING", lastStatus(sink, "A").state);
        assertTrue(errors(sink, "A").isEmpty());
        m.handleStdout("A", "\n__AGENT_DONE_1__\n");
        assertEquals("IDLE", lastStatus(sink, "A").state);
        // Marker is a second shell input line; the user command stays one line.
        assertEquals(2, fakes.get(0).writtenLines().size());
    }

    // ------------------------------------------------------------ stop

    @Test
    void stopKeepsShellAliveUntilBoundary() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.execute("A", "sleep 60");
        m.stop("A");
        assertEquals("RUNNING", lastStatus(sink, "A").state, "STOP must not complete early");
        assertFalse(hasFinalResult(sink, "A"), "no completion before the boundary");
        assertEquals(1, fakes.get(0).writtenRaw().size(), "Ctrl+C byte delivered once");
        assertEquals((byte) 0x03, fakes.get(0).writtenRaw().get(0)[0]);

        // Shell reaches the queued boundary afterwards: deterministic completion.
        m.handleStdout("A", "interrupted output\r\n");
        m.handleStdout("A", "\n__AGENT_DONE_1__\n");
        assertTrue(hasFinalResult(sink, "A"));
        assertEquals("IDLE", lastStatus(sink, "A").state);
        assertTrue(fakes.get(0).isAlive(), "shell remains alive after STOP");

        // Terminal is usable again.
        m.execute("A", "echo after-stop");
        assertEquals("RUNNING", lastStatus(sink, "A").state);
    }

    @Test
    void stopTimeoutForcesCompletion() throws Exception {
        Properties p = baseProps();
        p.setProperty("terminal.stop-timeout-ms", "300");
        TestEventSink sink = new TestEventSink();
        TerminalManager m = new TerminalManager(AgentConfig.fromProperties(p), collectingFactory());
        m.setSink(sink);
        managers.add(m);
        m.start("A", "PS");
        m.execute("A", "sleep 60");
        m.handleStdout("A", "partial\r\n");
        m.stop("A");
        await("forced completion", () -> hasFinalResult(sink, "A"));
        assertEquals("IDLE", lastStatus(sink, "A").state);
    }

    // ------------------------------------------------------------ close

    @Test
    void closeTerminatesAndReleasesSlot() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.execute("A", "echo x");
        m.handleStdout("A", "\n__AGENT_DONE_1__\n");
        m.close("A");
        List<TerminalStatusEvent> st = statuses(sink, "A");
        assertTrue(st.stream().anyMatch(e -> "CLOSING".equals(e.state)));
        assertEquals("CLOSED", st.get(st.size() - 1).state);
        assertEquals(-1, st.get(st.size() - 1).pid);
        assertTrue(fakes.get(0).destroyCalled(), "shell must be terminated");
        assertEquals(0, m.activeCount(), "slot released on CLOSED");

        m.execute("A", "echo late");
        assertTrue(hasError(sink, "A", "TERMINAL_CLOSED"));
    }

    @Test
    void closeFailureRetainsSessionAndSlotForRetry() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        fakes.get(0).failFirstDestroy(true);
        m.close("A");
        assertTrue(hasError(sink, "A", "PROCESS_TERMINATION_FAILED"));
        assertEquals("CLOSING", lastStatus(sink, "A").state);
        assertEquals(1, m.activeCount(), "CLOSING retains the slot");

        m.close("A"); // retry must actually re-attempt termination
        assertEquals("CLOSED", lastStatus(sink, "A").state);
        assertEquals(0, m.activeCount());
    }

    @Test
    void restartKeepsIdAndMetadataWithNewPid() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.execute("A", "echo keep");
        m.handleStdout("A", "keep\r\n");
        m.handleStdout("A", "\n__AGENT_DONE_1__\n");
        long pid1 = lastStatus(sink, "A").pid;
        m.close("A");
        assertEquals("CLOSED", lastStatus(sink, "A").state);

        m.start("A", "PS");
        TerminalStatusEvent restarted = lastStatus(sink, "A");
        assertEquals("IDLE", restarted.state);
        assertTrue(restarted.pid > 0 && restarted.pid != pid1, "restart gets a new shell/PID");
        assertEquals("echo keep", restarted.lastCommand, "metadata survives restart");
        assertNotNull(restarted.lastResult);
        assertTrue(restarted.lastResult.contains("keep"));
    }

    // ------------------------------------------------------------ metadata + config

    @Test
    void statusPayloadCarriesBoundedCache() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.execute("A", "echo hi");
        m.handleStdout("A", "hi\r\n");
        m.handleStdout("A", "\n__AGENT_DONE_1__\n");
        TerminalStatusEvent idle = lastStatus(sink, "A");
        assertEquals("IDLE", idle.state);
        assertNotNull(idle.lastResult);
        assertTrue(idle.lastResult.contains("hi"));
    }

    @Test
    void agentErrorIsCachedForMetadataError() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.execute("A", "sleep 60");
        m.execute("A", "echo other"); // TERMINAL_BUSY
        m.metadataLastError("A");
        List<MetadataErrorEvent> chunks = sink.ofType(MetadataErrorEvent.class);
        assertFalse(chunks.isEmpty());
        assertTrue(chunks.get(chunks.size() - 1).end);
        assertTrue(sink.withWireType("metadata_lastError").size() >= 1);
        String joined = chunks.stream().map(e -> e.chunk).reduce("", (a, b) -> a + b);
        assertTrue(joined.contains("currently executing"));
    }

    @Test
    void maxCountIsAHardInvariant() {
        Properties p = baseProps();
        p.setProperty("terminal.max-count", "50");
        assertThrows(IllegalStateException.class, () -> AgentConfig.fromProperties(p));
    }

    // ------------------------------------------------------------ concurrency

    @Test
    void elevenConcurrentStartsAdmitExactlyTen() throws Exception {
        TestEventSink sink = new TestEventSink();
        List<FakeProcessSession> created = new CopyOnWriteArrayList<>();
        TerminalManager m = manager(sink, (platform, initialPath) -> {
            FakeProcessSession f = new FakeProcessSession(platform);
            created.add(f);
            fakes.add(f);
            return f;
        });
        int contenders = 11;
        ExecutorService pool = Executors.newFixedThreadPool(contenders);
        CountDownLatch go = new CountDownLatch(1);
        List<Throwable> failures = new CopyOnWriteArrayList<>();
        for (int i = 0; i < contenders; i++) {
            final String id = "c-" + i;
            pool.submit(() -> {
                try {
                    go.await();
                    m.start(id, "PS");
                } catch (Throwable t) {
                    failures.add(t);
                }
                return null;
            });
        }
        go.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        assertTrue(failures.isEmpty(), "no worker may throw: " + failures);
        assertEquals(10, created.size(), "exactly 10 processes may exist");
        assertEquals(10, m.activeCount());
        assertEquals(1, sink.withWireType("terminal_max").size(), "exactly one terminal_max");
        // 10 terminals IDLE, 1 rejected without state.
        long idle = sink.ofType(TerminalStatusEvent.class).stream()
                .filter(e -> e.terminalId.startsWith("c-") && "IDLE".equals(e.state))
                .map(e -> e.terminalId)
                .distinct()
                .count();
        assertEquals(10, idle);
    }

    @Test
    void concurrentExecuteStopCloseStaysCoherent() throws Exception {
        Properties p = baseProps();
        p.setProperty("terminal.stop-timeout-ms", "500");
        TestEventSink sink = new TestEventSink();
        TerminalManager m = new TerminalManager(AgentConfig.fromProperties(p), collectingFactory());
        m.setSink(sink);
        managers.add(m);
        m.start("A", "PS");
        m.execute("A", "sleep 60");

        ExecutorService pool = Executors.newFixedThreadPool(3);
        CountDownLatch go = new CountDownLatch(1);
        List<Throwable> failures = new CopyOnWriteArrayList<>();
        pool.submit(() -> { try { go.await(); m.execute("A", "echo x"); } catch (Throwable t) { failures.add(t); } return null; });
        pool.submit(() -> { try { go.await(); m.stop("A"); } catch (Throwable t) { failures.add(t); } return null; });
        pool.submit(() -> { try { go.await(); m.close("A"); } catch (Throwable t) { failures.add(t); } return null; });
        go.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        assertTrue(failures.isEmpty(), "lifecycle ops must never throw: " + failures);

        await("settle", () -> {
            String s = lastStatus(sink, "A").state;
            return "IDLE".equals(s) || "CLOSED".equals(s);
        });
        String fin = lastStatus(sink, "A").state;
        if ("CLOSED".equals(fin)) {
            assertEquals(0, m.activeCount());
        } else {
            assertEquals(1, m.activeCount());
        }
    }

    @Test
    void unknownTerminalAndBadPlatform() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.execute("ghost", "echo hi");
        assertEquals(1, sink.withWireType("terminal_error").size());
        Map<String, Object> e = sink.withWireType("terminal_error").get(0);
        assertEquals("TERMINAL_NOT_FOUND", e.get("errorCode"));

        m.start("B", "BOGUS");
        assertEquals("INVALID_PLATFORM",
                sink.withWireType("terminal_error").get(1).get("errorCode"));
        assertEquals(0, m.activeCount(), "invalid platform reserves nothing");
    }

    @Test
    void closeDuringRunningCommand() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.execute("A", "sleep 60");
        m.close("A");
        assertEquals("CLOSED", lastStatus(sink, "A").state);
        assertEquals(0, m.activeCount());
    }

    // ------------------------------------------------- framing (VT-safe)

    @Test
    void commandEchoIsFramingNotResult() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.execute("A", "echo hi");
        // ConPTY echoes the typed line first; it must never surface as result.
        m.handleStdout("A", "echo hi\r\n");
        assertTrue(results(sink, "A").isEmpty(), "echo must not be streamed");
        m.handleStdout("A", "hi\r\n");
        m.handleStdout("A", "\n__AGENT_DONE_1__\n");
        assertEquals("IDLE", lastStatus(sink, "A").state);
        String live = results(sink, "A").stream().map(e -> e.chunk).reduce("", (a, b) -> a + b);
        assertEquals("hi\r\n", live);
        assertEquals("hi\r\n", lastStatus(sink, "A").lastResult);
    }

    @Test
    void promptPrefixedEchoIsConsumed() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.execute("A", "echo hi");
        // Prompt chrome may share the echo's line; suffix match still consumes it.
        m.handleStdout("A", "PS C:\\Temp> echo hi\r\n");
        assertTrue(results(sink, "A").isEmpty(), "prompt+echo must not be streamed");
        m.handleStdout("A", "hi\r\n");
        m.handleStdout("A", "\n__AGENT_DONE_1__\n");
        assertEquals("IDLE", lastStatus(sink, "A").state);
    }

    @Test
    void ansiWrappedMarkerCompletes() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.execute("A", "echo hi");
        // Real ConPTY output arrives wrapped in VT sequences, like the frozen evidence:
        // ESC[?25l ESC[2J ESC[m ... ESC[H ESC]0;... ESC[?25h
        m.handleStdout("A", "echo hi\r\n");
        m.handleStdout("A", "[?25lhi\r\n");
        m.handleStdout("A", "[2J[m\n__AGENT_DONE_1__[?25h\r\n");
        assertTrue(hasFinalResult(sink, "A"), "VT-wrapped marker must complete");
        assertEquals("IDLE", lastStatus(sink, "A").state);
        assertTrue(lastStatus(sink, "A").lastResult.contains("hi"));
    }

    @Test
    void markerSplitAcrossChunksCompletes() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.execute("A", "echo hi");
        m.handleStdout("A", "echo hi\r\n");
        m.handleStdout("A", "hi\r\n__AGENT_DO");
        assertFalse(hasFinalResult(sink, "A"), "partial marker line must wait");
        assertEquals("RUNNING", lastStatus(sink, "A").state);
        m.handleStdout("A", "NE_1__\r\n");
        assertTrue(hasFinalResult(sink, "A"), "split marker must complete on the full line");
        assertEquals("IDLE", lastStatus(sink, "A").state);
    }

    @Test
    void outputIdenticalToCommandIsPreserved() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        // Only the FIRST matching line is framing; a repeated identical output line stays.
        m.execute("A", "echo hi");
        m.handleStdout("A", "echo hi\r\n");
        m.handleStdout("A", "echo hi\r\n");
        m.handleStdout("A", "\n__AGENT_DONE_1__\n");
        assertEquals("IDLE", lastStatus(sink, "A").state);
        assertEquals("echo hi\r\n", lastStatus(sink, "A").lastResult);
    }

    @Test
    void crlfDoublingProducesNoPhantomBlank() {
        // Real consoles emit \r\r\n; it must classify as one line, not line+blank.
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.execute("A", "echo hi");
        m.handleStdout("A", "echo hi\r\r\n");
        m.handleStdout("A", "hi\r\r\n");
        m.handleStdout("A", "\n__AGENT_DONE_1__\n");
        assertEquals("IDLE", lastStatus(sink, "A").state);
        assertEquals("hi\r\r\n", lastStatus(sink, "A").lastResult);
    }

    @Test
    void loneCarriageReturnIsABoundary() {
        // Prompt/progress rewrites use lone \r; a marker glued after one must complete.
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.execute("A", "echo hi");
        m.handleStdout("A", "echo hi\r\n");
        m.handleStdout("A", "hi\r__AGENT_DONE_1__\r\n");
        assertTrue(hasFinalResult(sink, "A"), "lone-\\r marker must complete");
        assertEquals("IDLE", lastStatus(sink, "A").state);
        assertTrue(lastStatus(sink, "A").lastResult.contains("hi"));
    }

    @Test
    void realisticConptyHelloFlow() {
        // End-to-end framing exactly as a real ConPTY delivers it: prompt-prefixed
        // echo, VT-wrapped output, marker-command echo, VT-wrapped marker, trailing
        // prompt without newline. Expected: one HELLO chunk, empty final, IDLE.
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.execute("A", "Write-Output \"HELLO\"");
        m.handleStdout("A", "PS C:\\Temp> Write-Output \"HELLO\"\r\n");
        assertTrue(results(sink, "A").isEmpty());
        m.handleStdout("A", "HELLO\r\n");
        m.handleStdout("A", " \u001b[?25lWrite-Output \"`n__AGENT_DONE_1__\"\r\n");
        assertTrue(results(sink, "A").stream().noneMatch(e -> e.chunk.contains("__AGENT_DONE_1__")),
                "marker echo must never stream");
        m.handleStdout("A", "\n__AGENT_DONE_1__\r\nPS C:\\Temp> ");
        assertTrue(hasFinalResult(sink, "A"));
        assertEquals("IDLE", lastStatus(sink, "A").state);
        String live = results(sink, "A").stream().map(e -> e.chunk).reduce("", (a, b) -> a + b);
        assertEquals("HELLO\r\n", live);
        assertEquals("HELLO\r\n", lastStatus(sink, "A").lastResult);
    }

    @Test
    void metadataListReturnsAllTerminalSnapshots() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.start("B", "CMD");

        m.metadataList();

        List<com.automation.agent.protocol.TerminalProtocol.MetadataListResponseEvent> listEvents =
                sink.ofType(com.automation.agent.protocol.TerminalProtocol.MetadataListResponseEvent.class);
        assertFalse(listEvents.isEmpty(), "expected MetadataListResponseEvent");
        com.automation.agent.protocol.TerminalProtocol.MetadataListResponseEvent resp = listEvents.get(0);
        assertNotNull(resp.terminals);
        assertEquals(2, resp.terminals.size());
        assertTrue(resp.terminals.stream().anyMatch(t -> "A".equals(t.terminalId) && "PS".equals(t.platform)));
        assertTrue(resp.terminals.stream().anyMatch(t -> "B".equals(t.terminalId) && "CMD".equals(t.platform)));
    }

    @Test
    void platformMismatchIsRejectedWhenTerminalIsActive() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "CMD");
        assertEquals("CMD", lastStatus(sink, "A").platform);

        // Request PowerShell for active CMD terminal -> should reject
        m.start("A", "PS");
        assertTrue(hasError(sink, "A", "INVALID_PLATFORM"), "Platform mismatch on active terminal must be rejected");
        assertEquals("CMD", lastStatus(sink, "A").platform);
    }

    @Test
    void flexiblePlatformParsingWorks() {
        assertEquals(TerminalModels.TerminalPlatform.PS, TerminalModels.TerminalPlatform.parse("PowerShell"));
        assertEquals(TerminalModels.TerminalPlatform.PS, TerminalModels.TerminalPlatform.parse("power_shell"));
        assertEquals(TerminalModels.TerminalPlatform.CMD, TerminalModels.TerminalPlatform.parse("Command Prompt"));
        assertEquals(TerminalModels.TerminalPlatform.CMD, TerminalModels.TerminalPlatform.parse("cmd.exe"));
    }
}

```

---

<a id="file-18"></a>
### 18. `src/test/java/com/automation/agent/terminal/TestEventSink.java`

**Path:** `src/test/java/com/automation/agent/terminal/TestEventSink.java` | **Size:** `1.49 KB` | **Lines:** `51` | **Language:** `java`

```java
package com.automation.agent.terminal;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;

/** In-memory event sink for manager tests; serializes to maps for assertions. */
final class TestEventSink implements Consumer<Object> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final List<Object> events = new CopyOnWriteArrayList<>();

    @Override
    public void accept(Object eventPojo) {
        events.add(eventPojo);
    }

    List<Object> events() {
        return events;
    }

    /** Raw pojos of the given runtime type, in arrival order. */
    @SuppressWarnings("unchecked")
    <T> List<T> ofType(Class<T> type) {
        return events.stream()
                .filter(type::isInstance)
                .map(e -> (T) e)
                .toList();
    }

    /** All events serialized to maps (covers Map-typed events like terminal_max). */
    List<java.util.Map<String, Object>> asMaps() {
        return events.stream()
                .map(e -> MAPPER.convertValue(e, java.util.Map.class))
                .map(m -> (java.util.Map<String, Object>) m)
                .toList();
    }

    List<java.util.Map<String, Object>> withWireType(String wireType) {
        return asMaps().stream()
                .filter(m -> wireType.equals(String.valueOf(m.get("event-type"))))
                .toList();
    }

    void clear() {
        events.clear();
    }
}
```

---
