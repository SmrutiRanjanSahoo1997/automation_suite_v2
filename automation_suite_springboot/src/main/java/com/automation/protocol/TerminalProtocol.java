package com.automation.protocol;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class TerminalProtocol {

    private TerminalProtocol() {
    }

    public enum TerminalEventType {
        TERMINAL_START("terminal_start"),
        TERMINAL_EXECUTE("terminal_execute"),
        TERMINAL_STOP("terminal_stop"),
        TERMINAL_CLOSE("terminal_close"),
        TERMINAL_REMOVE("terminal_remove"),
        TERMINAL_RESULT("terminal_result"),
        TERMINAL_ERROR("terminal_error"),
        TERMINAL_STATUS("terminal_status"),
        TERMINAL_MAX("terminal_max"),
        METADATA_LAST_RESULT("metadata_lastResult"),
        METADATA_LAST_ERROR("metadata_lastError"),
        METADATA_LIST("metadata_list"),
        TERMINAL_HISTORY("terminal_history");

        private final String wire;

        TerminalEventType(String wire) {
            this.wire = wire;
        }

        public String wire() {
            return wire;
        }

        public static TerminalEventType fromWire(String wire) {
            if (wire == null) return null;
            for (TerminalEventType t : values()) {
                if (t.wire.equalsIgnoreCase(wire)) return t;
            }
            return null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TerminalStartEvent {
        @JsonProperty("event-type")
        public String eventType = TerminalEventType.TERMINAL_START.wire();
        @JsonProperty("terminalid")
        public String terminalId;
        @JsonProperty("platform")
        public String platform;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TerminalExecuteEvent {
        @JsonProperty("event-type")
        public String eventType = TerminalEventType.TERMINAL_EXECUTE.wire();
        @JsonProperty("terminalid")
        public String terminalId;
        @JsonProperty("command")
        public String command;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TerminalStopEvent {
        @JsonProperty("event-type")
        public String eventType = TerminalEventType.TERMINAL_STOP.wire();
        @JsonProperty("terminalid")
        public String terminalId;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TerminalCloseEvent {
        @JsonProperty("event-type")
        public String eventType = TerminalEventType.TERMINAL_CLOSE.wire();
        @JsonProperty("terminalid")
        public String terminalId;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TerminalRemoveEvent {
        @JsonProperty("event-type")
        public String eventType = TerminalEventType.TERMINAL_REMOVE.wire();
        @JsonProperty("terminalid")
        public String terminalId;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TerminalResultEvent {
        @JsonProperty("event-type")
        public String eventType;
        @JsonProperty("terminalid")
        public String terminalId;
        @JsonProperty("chunk")
        public String chunk;
        @JsonProperty("count")
        public int count;
        @JsonProperty("end")
        public boolean end;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TerminalErrorEvent {
        @JsonProperty("event-type")
        public String eventType;
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TerminalStatusEvent {
        @JsonProperty("event-type")
        public String eventType;
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
        public Boolean elevated;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MetadataListResponseEvent {
        @JsonProperty("event-type")
        public String eventType;
        @JsonProperty("terminals")
        public List<TerminalStatusEvent> terminals;
    }
}
