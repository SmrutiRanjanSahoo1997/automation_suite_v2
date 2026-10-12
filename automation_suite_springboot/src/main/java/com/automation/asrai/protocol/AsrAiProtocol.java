package com.automation.asrai.protocol;

import java.util.List;

import com.automation.constants.GlobalEventConstants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class AsrAiProtocol {

    private AsrAiProtocol() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiHealthStatusEvent {
        @JsonProperty("event-type")
        public String eventType = GlobalEventConstants.AI_HEALTH_STATUS;
        @JsonProperty("status")
        public String status; // "OK" or "NOT_OK"
        @JsonProperty("extsnConnected")
        public boolean extsnConnected;
        @JsonProperty("activeTasks")
        public int activeTasks;
        @JsonProperty("queuedTasks")
        public int queuedTasks;
        @JsonProperty("timestamp")
        public long timestamp;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiExecuteTaskRequest {
        @JsonProperty("event-type")
        public String eventType = GlobalEventConstants.AI_EXECUTE_TASK;
        @JsonProperty("taskId")
        public String taskId;
        @JsonProperty("conversationId")
        public String conversationId;
        @JsonProperty("prompt")
        public String prompt;
        @JsonProperty("filesPath")
        public List<String> filesPath;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiTaskResultResponse {
        @JsonProperty("event-type")
        public String eventType = GlobalEventConstants.AI_TASK_RESULT;
        @JsonProperty("taskId")
        public String taskId;
        @JsonProperty("conversationId")
        public String conversationId;
        @JsonProperty("status")
        public String status; // "RESPONSE" or "FAILED"
        @JsonProperty("rawResponse")
        public String rawResponse;
        @JsonProperty("error")
        public String error;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiHostResolveRequest {
        @JsonProperty("event-type")
        public String eventType = GlobalEventConstants.AI_HOST_RESOLVE_REQ;
        @JsonProperty("taskId")
        public String taskId;
        @JsonProperty("conversationId")
        public String conversationId;
        @JsonProperty("query")
        public String query;
        @JsonProperty("knownHosts")
        public List<String> knownHosts;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiHostResolveResponse {
        @JsonProperty("event-type")
        public String eventType = GlobalEventConstants.AI_HOST_RESOLVE_RESP;
        @JsonProperty("taskId")
        public String taskId;
        @JsonProperty("conversationId")
        public String conversationId;
        @JsonProperty("pre_response")
        public String preResponse;
        @JsonProperty("target_hosts")
        public List<String> targetHosts;
        @JsonProperty("review_required")
        public boolean reviewRequired;
        @JsonProperty("review_title")
        public String reviewTitle;
        @JsonProperty("review_description")
        public String reviewDescription;
        @JsonProperty("final_outcome")
        public String finalOutcome;
        @JsonProperty("error")
        public String error;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiRemediateRequest {
        @JsonProperty("event-type")
        public String eventType = GlobalEventConstants.AI_REMEDIATE_REQ;
        @JsonProperty("taskId")
        public String taskId;
        @JsonProperty("conversationId")
        public String conversationId;
        @JsonProperty("query")
        public String query;
        @JsonProperty("host")
        public String host;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiRemediateResponse {
        @JsonProperty("event-type")
        public String eventType = GlobalEventConstants.AI_REMEDIATE_RESP;
        @JsonProperty("taskId")
        public String taskId;
        @JsonProperty("conversationId")
        public String conversationId;
        @JsonProperty("host")
        public String host;
        @JsonProperty("pre_response")
        public String preResponse;
        @JsonProperty("command")
        public String command;
        @JsonProperty("platform")
        public String platform;
        @JsonProperty("review_needed")
        public boolean reviewNeeded;
        @JsonProperty("review_title")
        public String reviewTitle;
        @JsonProperty("review_description")
        public String reviewDescription;
        @JsonProperty("criticality_level")
        public int criticalityLevel;
        @JsonProperty("file_needed")
        public List<String> fileNeeded;
        @JsonProperty("final_outcome")
        public String finalOutcome;
        @JsonProperty("error")
        public String error;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiExecuteCmdRequest {
        @JsonProperty("event-type")
        public String eventType = GlobalEventConstants.AI_EXECUTE_CMD_REQ;
        @JsonProperty("conversationId")
        public String conversationId;
        @JsonProperty("host")
        public String host;
        @JsonProperty("command")
        public String command;
        @JsonProperty("platform")
        public String platform;
        @JsonProperty("userId")
        public String userId;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiExecuteCmdResponse {
        @JsonProperty("event-type")
        public String eventType = GlobalEventConstants.AI_EXECUTE_CMD_RESP;
        @JsonProperty("conversationId")
        public String conversationId;
        @JsonProperty("terminalId")
        public String terminalId;
        @JsonProperty("host")
        public String host;
        @JsonProperty("status")
        public String status;
        @JsonProperty("error")
        public String error;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiExecuteSqlRequest {
        @JsonProperty("eventType")
        public String eventType = "MSSQL_EXECUTE_QUERY";
        @JsonProperty("taskId")
        public String taskId;
        @JsonProperty("userId")
        public String userId;
        @JsonProperty("environment")
        public String environment; // "PROD" or "TEST"
        @JsonProperty("sql")
        public String sql;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiExecuteSqlResponse {
        @JsonProperty("eventType")
        public String eventType;
        @JsonProperty("taskId")
        public String taskId;
        @JsonProperty("type")
        public String type; // "connecting", "connected", "querySent", "output", "closed"
        @JsonProperty("data")
        public List<Object> data;
        @JsonProperty("rowCount")
        public Integer rowCount;
        @JsonProperty("durationMs")
        public Long durationMs;
        @JsonProperty("end")
        public Boolean end;
        @JsonProperty("success")
        public Boolean success;
        @JsonProperty("error")
        public String error;
    }
}

