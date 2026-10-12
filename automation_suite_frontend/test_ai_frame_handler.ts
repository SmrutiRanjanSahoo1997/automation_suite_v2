import { buildHostSelectionPrompt } from './src/modules/ai/services/promptTemplates.ts';

console.log("==================================================");
console.log(" Frontend AI Frame & State Diagnostic Test");
console.log("==================================================");

// 1. Test Frame Parsing with various Copilot response outputs
const testFrames = [
  {
    "event-type": "AI_TASK_RESULT",
    "taskId": "task_1790598270_xyz",
    "conversationId": "7de31de1-7c5d-4d67-9670-71ea263214f3",
    "status": "RESPONSE",
    "rawResponse": JSON.stringify({
      "pre_response": "Analyzing server inventory...",
      "target_hosts": [],
      "review_required": true,
      "final_outcome": "Please specify target server environment."
    })
  },
  {
    "event-type": "AI_TASK_RESULT",
    "taskId": "task_1790598270_xyz2",
    "conversationId": "7de31de1-7c5d-4d67-9670-71ea263214f3",
    "status": "RESPONSE",
    "rawResponse": "Hello! How can I help you today?" // Raw non-JSON text response
  }
];

testFrames.forEach((frame, idx) => {
  console.log(`\n--- Test Case ${idx + 1} ---`);
  let jsonPayload: any = null;
  if (frame.rawResponse) {
    try {
      jsonPayload = JSON.parse(frame.rawResponse);
    } catch (e) {
      console.warn("rawResponse is not valid JSON string:", frame.rawResponse);
    }
  }
  if (!jsonPayload) {
    jsonPayload = frame;
  }

  const isHostRes = jsonPayload.target_hosts !== undefined || jsonPayload.review_required !== undefined;
  const isRemediation = jsonPayload.Command !== undefined || jsonPayload.command !== undefined || jsonPayload.review_needed !== undefined;
  const isGeneralOutcome = Boolean(jsonPayload.final_outcome || jsonPayload.pre_response);

  console.log("Matches Host Resolution (Step A):", isHostRes);
  console.log("Matches Remediation (Step B):", isRemediation);
  console.log("Matches General Outcome Frame:", isGeneralOutcome);

  if (!isHostRes && !isRemediation && !isGeneralOutcome) {
    console.error("⚠️ CRITICAL BUG FOUND: Incoming response frame matched NO rendering condition! UI will freeze loading!");
  } else {
    console.log("✅ Frame correctly matches a rendering handler.");
  }
});
