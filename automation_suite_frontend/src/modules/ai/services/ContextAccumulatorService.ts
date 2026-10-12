export interface ConversationTurnRecord {
  turnNumber: number;
  userQuery: string;
  targetHost?: string;
  feature?: string;
  commandExecuted?: string;
  keyFindings?: string;
  finalOutcome?: string;
  timestamp: number;
}

const STOPWORDS = new Set([
  'the', 'is', 'a', 'an', 'and', 'or', 'of', 'in', 'to', 'for', 'with', 'on', 'at', 'by', 'from',
  'please', 'could', 'you', 'can', 'find', 'check', 'do', 'it', 'my', 'me', 'there', 'i', 'we',
  'am', 'are', 'was', 'were', 'be', 'been', 'being', 'have', 'has', 'had', 'would', 'should',
  'will', 'shall', 'about', 'above', 'after', 'again', 'all', 'any', 'both', 'each', 'few',
  'more', 'most', 'other', 'some', 'such', 'no', 'nor', 'not', 'only', 'own', 'same', 'so',
  'than', 'too', 'very', 'what', 'which', 'who', 'whom', 'this', 'that', 'these', 'those'
]);

export class ContextAccumulatorService {
  /**
   * Filters out grammatical stopwords, keeping only critical keywords, paths, and identifiers.
   */
  public static extractKeywords(text: string): string {
    if (!text) return '';
    // Preserve paths, identifiers, and remove generic words
    const tokens = text
      .replace(/[^\w\s\-\.\:\\\\\/]/g, ' ')
      .split(/\s+/)
      .filter((t) => t.length > 1 && !STOPWORDS.has(t.toLowerCase()));
    return tokens.join(' ');
  }

  /**
   * Formats the ground-truth turn records into a 2-tier rolling window context:
   * - Turns 1 to N-2: Compressed keyword-only summary (grammar stripped)
   * - Turns N-1 and N: Full fidelity as-is
   */
  public static formatRollingContext(turns: ConversationTurnRecord[]): string {
    if (!turns || turns.length === 0) {
      return 'None';
    }

    const total = turns.length;
    const parts: string[] = [];

    // Older turns: compressed into compact keyword points
    if (total > 2) {
      const olderTurns = turns.slice(0, total - 2);
      const summaryLines: string[] = [];

      olderTurns.forEach((turn) => {
        const queryKw = this.extractKeywords(turn.userQuery);
        const hostStr = turn.targetHost ? `Host: ${turn.targetHost}` : 'Host: Unset';
        const actionStr = turn.commandExecuted
          ? `Cmd: ${this.extractKeywords(turn.commandExecuted)}`
          : (turn.feature ? `Feature: ${turn.feature}` : '');
        const findingKw = turn.keyFindings ? this.extractKeywords(turn.keyFindings) : (turn.finalOutcome ? this.extractKeywords(turn.finalOutcome) : 'completed');

        const segments = [hostStr, queryKw, actionStr, findingKw].filter(Boolean);
        summaryLines.push(`• T${turn.turnNumber} | ${segments.join(' | ')}`);
      });

      parts.push(`[Historical Context Summary (Keywords)]:\n${summaryLines.join('\n')}`);
    }

    // Last 2 turns: full detail as-is
    const recentTurns = total > 2 ? turns.slice(total - 2) : turns;
    const recentLines: string[] = [];

    recentTurns.forEach((turn) => {
      const turnLines: string[] = [`Turn ${turn.turnNumber}:`];
      turnLines.push(`- User Query: '${turn.userQuery.replace(/'/g, "\\'")}'`);
      if (turn.targetHost) turnLines.push(`- Target Host: '${turn.targetHost.replace(/'/g, "\\'")}'`);
      if (turn.feature) turnLines.push(`- Feature: '${turn.feature.replace(/'/g, "\\'")}'`);
      if (turn.commandExecuted) turnLines.push(`- Command Executed: '${turn.commandExecuted.replace(/'/g, "\\'")}'`);
      if (turn.keyFindings) turnLines.push(`- Key Findings: '${turn.keyFindings.replace(/'/g, "\\'")}'`);
      if (turn.finalOutcome) turnLines.push(`- Outcome: '${turn.finalOutcome.replace(/'/g, "\\'")}'`);
      recentLines.push(turnLines.join('\n'));
    });

    parts.push(`[Recent Context (Last 2 Interactions)]:\n${recentLines.join('\n\n')}`);

    return parts.join('\n\n');
  }

  /**
   * Ensures context is never overwritten by "None", "null", or empty strings.
   */
  public static sanitizeContext(
    aiContext: string | null | undefined,
    groundTruthContext: string
  ): string {
    if (!aiContext || typeof aiContext !== 'string') {
      return groundTruthContext;
    }
    const trimmed = aiContext.trim();
    if (trimmed === '' || /^none$/i.test(trimmed) || /^null$/i.test(trimmed)) {
      return groundTruthContext;
    }
    return trimmed;
  }
}
