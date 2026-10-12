import { logger } from './logger';

export interface JsonParseResult<T = any> {
  success: boolean;
  data?: T;
  error?: string;
  cleanedRaw?: string;
  isFallback?: boolean;
}

/**
 * Robust JSON parser and repair utility for AI LLM outputs.
 * Handles markdown fences, surrounding text, unescaped backslashes in Windows paths,
 * unescaped quotes inside string values, unescaped newlines/tabs, trailing commas,
 * and schema-aware fallback key extraction.
 */
export function safeParseAiJson<T = any>(rawInput: any): JsonParseResult<T> {
  if (rawInput === null || rawInput === undefined) {
    return { success: false, error: 'Empty or null raw response' };
  }

  // If already an object (e.g. parsed upstream by websocket)
  if (typeof rawInput === 'object') {
    return { success: true, data: rawInput };
  }

  if (typeof rawInput !== 'string') {
    return { success: false, error: 'Raw response is not a string or object' };
  }

  let cleaned = rawInput.trim();
  if (!cleaned) {
    return { success: false, error: 'Empty raw response string' };
  }

  // 1. Strip markdown code fences (```json ... ``` or ``` ... ```)
  cleaned = cleaned.replace(/^```(?:json)?\s*/i, '').replace(/\s*```$/i, '').trim();

  // 2. Extract content between the first '{' and the last '}'
  const firstBrace = cleaned.indexOf('{');
  const lastBrace = cleaned.lastIndexOf('}');
  if (firstBrace !== -1 && lastBrace !== -1 && lastBrace > firstBrace) {
    cleaned = cleaned.substring(firstBrace, lastBrace + 1).trim();
  } else {
    // Check if it's an array '[ ... ]'
    const firstBracket = cleaned.indexOf('[');
    const lastBracket = cleaned.lastIndexOf(']');
    if (firstBracket !== -1 && lastBracket !== -1 && lastBracket > firstBracket) {
      cleaned = cleaned.substring(firstBracket, lastBracket + 1).trim();
    } else {
      return { success: false, error: 'No JSON object or array structure found in response' };
    }
  }

  // 3. Attempt 1: Direct JSON.parse
  try {
    const parsed = JSON.parse(cleaned);
    return { success: true, data: parsed, cleanedRaw: cleaned };
  } catch (_) {
    // Continue to repair attempts
  }

  // 4. Attempt 2: Fix unescaped control characters inside string literals (raw newlines and tabs)
  let step2 = cleaned.replace(/[\r\n]+/g, '\\n').replace(/\t/g, '\\t');
  try {
    const parsed = JSON.parse(step2);
    return { success: true, data: parsed, cleanedRaw: step2 };
  } catch (_) {
    // Continue to repair attempts
  }

  // 5. Attempt 3: Repair unescaped Windows paths and trailing commas
  let step3 = step2
    .replace(/\\(?!["\\/bfnrt]|u[0-9a-fA-F]{4})/g, '\\\\')
    .replace(/,\s*([\]}])/g, '$1');
  try {
    const parsed = JSON.parse(step3);
    logger.info('jsonRepair', 'safeParseAiJson', 'Parsed successfully after path/escape repair');
    return { success: true, data: parsed, cleanedRaw: step3 };
  } catch (_) {
    // Continue to repair attempts
  }

  // 6. Attempt 4: Schema-aware Fallback Field Extractor
  // When LLM includes unescaped quotes or invalid escapes inside string values,
  // we reliably extract all fields using schema key positioning.
  try {
    const fallbackData = fallbackExtractFields(cleaned);
    if (fallbackData && Object.keys(fallbackData).length > 0) {
      logger.info('jsonRepair', 'safeParseAiJson', 'Parsed successfully using fallback schema extractor', {
        keysFound: Object.keys(fallbackData),
      });
      return { success: true, data: fallbackData as T, cleanedRaw: cleaned, isFallback: true };
    }
  } catch (fallbackErr) {
    logger.warn('jsonRepair', 'safeParseAiJson', 'Fallback schema extraction encountered an error', fallbackErr);
  }

  return { success: false, error: 'Failed to parse AI JSON response after all repair attempts', cleanedRaw: cleaned };
}

/**
 * Robust schema-aware key-value extractor that tolerates unescaped internal quotes and newlines.
 */
function fallbackExtractFields(jsonStr: string): Record<string, any> | null {
  const result: Record<string, any> = {};

  const knownKeys = [
    'feature_id', 'featureId',
    'pre_response', 'preResponse', 'thought', 'reasoning',
    'accumulated_context_data', 'accumulatedContextData',
    'final_outcome', 'finalOutcome',
    'target_hosts', 'targetHosts',
    'review_title', 'reviewTitle',
    'review_description', 'reviewDescription',
    'Command', 'command', 'cmd',
    'platform',
    'review_needed', 'reviewNeeded',
    'CriticalityLevele', 'criticality_level', 'criticalityLevel',
    'file_needed', 'fileNeeded',
    'error', 'status', 'message'
  ];

  // Match positions of all known keys in the string
  const keyPositions: Array<{ key: string; index: number; valStartIndex: number }> = [];

  for (const key of knownKeys) {
    const regex = new RegExp(`"${key}"\\s*:`, 'g');
    let match: RegExpExecArray | null;
    while ((match = regex.exec(jsonStr)) !== null) {
      keyPositions.push({
        key,
        index: match.index,
        valStartIndex: match.index + match[0].length,
      });
    }
  }

  if (keyPositions.length === 0) {
    return null;
  }

  // Sort by index ascending to determine value boundaries
  keyPositions.sort((a, b) => a.index - b.index);

  for (let i = 0; i < keyPositions.length; i++) {
    const current = keyPositions[i];
    const valStart = current.valStartIndex;
    const valEnd = i + 1 < keyPositions.length
      ? keyPositions[i + 1].index
      : jsonStr.lastIndexOf('}');

    let rawValSegment = jsonStr.substring(valStart, valEnd).trim();

    // Remove trailing comma if present at boundary
    if (rawValSegment.endsWith(',')) {
      rawValSegment = rawValSegment.substring(0, rawValSegment.length - 1).trim();
    }

    if (rawValSegment === 'null') {
      result[current.key] = null;
    } else if (rawValSegment === 'true') {
      result[current.key] = true;
    } else if (rawValSegment === 'false') {
      result[current.key] = false;
    } else if (!isNaN(Number(rawValSegment)) && rawValSegment !== '') {
      result[current.key] = Number(rawValSegment);
    } else if (rawValSegment.startsWith('[') && rawValSegment.endsWith(']')) {
      try {
        result[current.key] = JSON.parse(rawValSegment);
      } catch (_) {
        const matches = rawValSegment.match(/"([^"]+)"/g);
        result[current.key] = matches ? matches.map((s) => s.replace(/^"|"$/g, '')) : [];
      }
    } else if (rawValSegment.startsWith('"')) {
      let strVal = rawValSegment;
      if (strVal.startsWith('"')) strVal = strVal.substring(1);
      if (strVal.endsWith('"')) strVal = strVal.substring(0, strVal.length - 1);
      // Unescape standard escapes
      strVal = strVal
        .replace(/\\"/g, '"')
        .replace(/\\n/g, '\n')
        .replace(/\\r/g, '')
        .replace(/\\t/g, '\t')
        .replace(/\\\\/g, '\\');
      result[current.key] = strVal;
    } else {
      result[current.key] = rawValSegment;
    }
  }

  return Object.keys(result).length > 0 ? result : null;
}
