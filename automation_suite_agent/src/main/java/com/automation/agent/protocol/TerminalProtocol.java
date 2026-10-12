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