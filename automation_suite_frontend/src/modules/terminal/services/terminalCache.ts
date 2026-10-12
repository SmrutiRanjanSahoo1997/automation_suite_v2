const cacheMap = new Map<string, string[]>();

export const splitLines = (text: string): string[] => {
  if (!text) return [];
  const parts = text.replace(/\r\n/g, '\n').replace(/\r/g, '\n').split('\n');
  if (parts.length > 1 && parts[parts.length - 1] === '') {
    parts.pop();
  }
  return parts;
};

export const getTerminalCache = (terminalId: string): string[] | undefined => {
  return cacheMap.get(terminalId);
};

export const setTerminalCache = (terminalId: string, lines: string[]): void => {
  const flattened: string[] = [];
  lines.forEach((item) => {
    if (item === undefined || item === null) return;
    if (typeof item === 'string' && (item.includes('\n') || item.includes('\r'))) {
      flattened.push(...splitLines(item));
    } else {
      flattened.push(String(item));
    }
  });
  cacheMap.set(terminalId, flattened);
};

export const appendTerminalCache = (terminalId: string, chunk: string): void => {
  const existing = cacheMap.get(terminalId) || [];
  if (!chunk) return;
  const newLines = splitLines(chunk);
  cacheMap.set(terminalId, [...existing, ...newLines]);
};

export const clearTerminalCache = (terminalId: string): void => {
  cacheMap.delete(terminalId);
};

