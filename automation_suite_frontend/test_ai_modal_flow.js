const { buildHostSelectionPrompt, buildDiagnosticsRemediationPrompt, formatServerInventoryJson } = require('./src/modules/ai/services/promptTemplates.ts');

console.log("==================================================");
console.log(" Testing Frontend Prompt Template Generators");
console.log("==================================================");

try {
  const query = "find me the latest resume inside document folder.";
  const servers = [
    { host: "localhost", displayName: "Localhost Agent", category: "INSPIRE_AUTOMATION", environment: "DEV" },
    { host: "H1191", displayName: "IA Prod 01", category: "INSPIRE_AUTOMATION", environment: "PROD" }
  ];

  const hostPrompt = buildHostSelectionPrompt(query, servers, null);
  console.log("[SUCCESS] Host Selection Prompt built successfully. Length:", hostPrompt.length);
  console.log("Sample Prompt Output:\n", hostPrompt.substring(0, 300) + "...\n");

  const diagPrompt = buildDiagnosticsRemediationPrompt(query, "H1191", "Previous context summary");
  console.log("[SUCCESS] Diagnostics Remediation Prompt built successfully. Length:", diagPrompt.length);
  console.log("Sample Prompt Output:\n", diagPrompt.substring(0, 300) + "...\n");

} catch (err) {
  System.err.println("[ERROR] Prompt template generator threw exception:", err);
}
