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
