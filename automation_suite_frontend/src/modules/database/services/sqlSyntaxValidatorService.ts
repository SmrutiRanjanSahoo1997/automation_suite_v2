export type SqlTokenType =
  | 'keyword'
  | 'function'
  | 'variable'
  | 'string'
  | 'number'
  | 'comment'
  | 'bracket'
  | 'punctuation'
  | 'identifier'
  | 'typo'
  | 'whitespace';

export interface SqlToken {
  text: string;
  type: SqlTokenType;
  suggestion?: string;
}

export const SQL_KEYWORDS = new Set([
  'SELECT', 'FROM', 'WHERE', 'JOIN', 'INNER', 'LEFT', 'RIGHT', 'FULL', 'CROSS',
  'OUTER', 'ON', 'GROUP', 'BY', 'ORDER', 'HAVING', 'TOP', 'DISTINCT', 'AS',
  'AND', 'OR', 'NOT', 'IN', 'LIKE', 'IS', 'NULL', 'INSERT', 'INTO', 'VALUES',
  'UPDATE', 'SET', 'DELETE', 'USE', 'EXEC', 'EXECUTE', 'CREATE', 'DROP',
  'ALTER', 'TABLE', 'VIEW', 'INDEX', 'PROCEDURE', 'DATABASE', 'UNION', 'ALL',
  'CASE', 'WHEN', 'THEN', 'ELSE', 'END', 'EXISTS', 'BETWEEN', 'WITH', 'NOLOCK',
  'ASC', 'DESC', 'DEFAULT', 'CHECK', 'CONSTRAINT', 'PRIMARY', 'KEY', 'FOREIGN',
  'REFERENCES', 'TRUNCATE', 'BEGIN', 'COMMIT', 'ROLLBACK', 'TRANSACTION',
  'MERGE', 'MATCHED', 'DECLARE', 'PRINT', 'GO'
]);

export const SQL_FUNCTIONS = new Set([
  'COUNT', 'SUM', 'AVG', 'MIN', 'MAX', 'GETDATE', 'DATEADD', 'DATEDIFF',
  'CONVERT', 'CAST', 'COALESCE', 'ISNULL', 'ROW_NUMBER', 'RANK', 'DENSE_RANK',
  'LEN', 'SUBSTRING', 'LOWER', 'UPPER', 'LTRIM', 'RTRIM', 'TRIM', 'REPLACE',
  'CHARINDEX', 'STUFF', 'ROUND', 'CEILING', 'FLOOR', 'ABS', 'OVER', 'PARTITION'
]);

/**
 * Calculates Levenshtein distance between two strings
 */
function levenshteinDistance(a: string, b: string): number {
  const m = a.length;
  const n = b.length;
  const dp: number[][] = [];

  for (let i = 0; i <= m; i++) {
    dp[i] = [i];
  }
  for (let j = 0; j <= n; j++) {
    dp[0][j] = j;
  }

  for (let i = 1; i <= m; i++) {
    for (let j = 1; j <= n; j++) {
      if (a[i - 1] === b[j - 1]) {
        dp[i][j] = dp[i - 1][j - 1];
      } else {
        dp[i][j] = Math.min(
          dp[i - 1][j] + 1,     // deletion
          dp[i][j - 1] + 1,     // insertion
          dp[i - 1][j - 1] + 1  // substitution
        );
      }
    }
  }

  return dp[m][n];
}

/**
 * Checks if a word is likely a misspelled keyword/function
 */
export function checkKeywordTypo(word: string): string | null {
  const upper = word.toUpperCase();
  if (SQL_KEYWORDS.has(upper) || SQL_FUNCTIONS.has(upper)) {
    return null;
  }

  // Only check words between 3 and 15 characters
  if (upper.length < 3 || upper.length > 15) {
    return null;
  }

  let closest: string | null = null;
  let minDistance = 999;

  for (const kw of SQL_KEYWORDS) {
    const dist = levenshteinDistance(upper, kw);
    // Allow distance 1 for length 3-5, distance 2 for length >= 6
    const threshold = kw.length >= 6 ? 2 : 1;
    if (dist <= threshold && dist < minDistance) {
      minDistance = dist;
      closest = kw;
    }
  }

  for (const fn of SQL_FUNCTIONS) {
    const dist = levenshteinDistance(upper, fn);
    const threshold = fn.length >= 6 ? 2 : 1;
    if (dist <= threshold && dist < minDistance) {
      minDistance = dist;
      closest = fn;
    }
  }

  return closest;
}

/**
 * Tokenizes SQL code with syntax and typo classification
 */
export function tokenizeSql(
  sql: string,
  knownTables: string[] = [],
  knownDatabases: string[] = []
): SqlToken[] {
  const tokens: SqlToken[] = [];
  const len = sql.length;
  let i = 0;

  const upperTables = new Set(knownTables.map((t) => t.toUpperCase()));
  const upperDbs = new Set(knownDatabases.map((d) => d.toUpperCase()));

  while (i < len) {
    const char = sql[i];

    // 1. Whitespace
    if (/\s/.test(char)) {
      let ws = '';
      while (i < len && /\s/.test(sql[i])) {
        ws += sql[i];
        i++;
      }
      tokens.push({ text: ws, type: 'whitespace' });
      continue;
    }

    // 2. Line comment --
    if (char === '-' && sql[i + 1] === '-') {
      let comment = '';
      while (i < len && sql[i] !== '\n') {
        comment += sql[i];
        i++;
      }
      tokens.push({ text: comment, type: 'comment' });
      continue;
    }

    // 3. Block comment /* */
    if (char === '/' && sql[i + 1] === '*') {
      let comment = '/*';
      i += 2;
      while (i < len && !(sql[i] === '*' && sql[i + 1] === '/')) {
        comment += sql[i];
        i++;
      }
      if (i < len) {
        comment += '*/';
        i += 2;
      }
      tokens.push({ text: comment, type: 'comment' });
      continue;
    }

    // 4. Strings '...' or N'...'
    if (char === "'" || ((char === 'N' || char === 'n') && sql[i + 1] === "'")) {
      let str = '';
      if (char === 'N' || char === 'n') {
        str += char;
        i++;
      }
      str += "'";
      i++;
      while (i < len) {
        if (sql[i] === "'") {
          str += "'";
          i++;
          if (sql[i] === "'") { // Escaped single quote
            str += "'";
            i++;
          } else {
            break;
          }
        } else {
          str += sql[i];
          i++;
        }
      }
      tokens.push({ text: str, type: 'string' });
      continue;
    }

    // 5. Bracketed identifier [schema].[table]
    if (char === '[') {
      let bracketed = '[';
      i++;
      while (i < len && sql[i] !== ']') {
        bracketed += sql[i];
        i++;
      }
      if (i < len && sql[i] === ']') {
        bracketed += ']';
        i++;
      }
      tokens.push({ text: bracketed, type: 'bracket' });
      continue;
    }

    // 6. Variables @param or @@IDENTITY
    if (char === '@') {
      let v = '@';
      i++;
      if (i < len && sql[i] === '@') {
        v += '@';
        i++;
      }
      while (i < len && /[a-zA-Z0-9_]/.test(sql[i])) {
        v += sql[i];
        i++;
      }
      tokens.push({ text: v, type: 'variable' });
      continue;
    }

    // 7. Numbers
    if (/\d/.test(char)) {
      let num = '';
      while (i < len && /[\d.]/.test(sql[i])) {
        num += sql[i];
        i++;
      }
      tokens.push({ text: num, type: 'number' });
      continue;
    }

    // 8. Word / Identifier / Keyword / Typo
    if (/[a-zA-Z_#]/.test(char)) {
      let word = '';
      while (i < len && /[a-zA-Z0-9_#]/.test(sql[i])) {
        word += sql[i];
        i++;
      }

      const upper = word.toUpperCase();

      if (SQL_KEYWORDS.has(upper)) {
        tokens.push({ text: word, type: 'keyword' });
      } else if (SQL_FUNCTIONS.has(upper)) {
        tokens.push({ text: word, type: 'function' });
      } else if (upperTables.has(upper) || upperDbs.has(upper)) {
        tokens.push({ text: word, type: 'identifier' });
      } else {
        // Typo intelligence check
        const typoSuggestion = checkKeywordTypo(word);
        if (typoSuggestion) {
          tokens.push({ text: word, type: 'typo', suggestion: typoSuggestion });
        } else {
          tokens.push({ text: word, type: 'identifier' });
        }
      }
      continue;
    }

    // 9. Punctuation / Operators
    tokens.push({ text: char, type: 'punctuation' });
    i++;
  }

  return tokens;
}
