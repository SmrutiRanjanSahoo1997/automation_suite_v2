/// <reference types="vite/client" />

/**
 * Custom Diagnostic Logger Utility.
 * Checks import.meta.env.DEV internally before printing.
 * Format: Timestamp  FileName  MethodName -> Message
 * Info/Debug: White | Warn: Orange | Error: Red
 */

// By default, logs only print in development mode (import.meta.env.DEV).
// In production builds (npm run build), DEV is false and all logs are silenced.
// To silence logs even while running in dev mode, set VITE_DISABLE_LOGS=true in .env
const IS_DEV = Boolean((import.meta as any).env?.DEV) && (import.meta as any).env?.VITE_DISABLE_LOGS !== 'true';


function getTimestamp(): string {
  const now = new Date();
  return now.toISOString().substring(11, 23);
}

export const logger = {
  debug: (fileName: string, methodName: string, message: string, data?: any) => {
    if (!IS_DEV) return;
    const header = `%c[${getTimestamp()}] %c${fileName} %c${methodName}`;
    const styleTimestamp = 'color: #94a3b8; font-weight: bold;';
    const styleFile = 'color: #ffffff; font-weight: bold;';
    const styleMethod = 'color: #ffffff; font-style: italic;';
    const styleMsg = 'color: #ffffff;';

    if (data !== undefined) {
      console.debug(`${header}\n%c${message}`, styleTimestamp, styleFile, styleMethod, styleMsg, data);
    } else {
      console.debug(`${header}\n%c${message}`, styleTimestamp, styleFile, styleMethod, styleMsg);
    }
  },

  info: (fileName: string, methodName: string, message: string, data?: any) => {
    if (!IS_DEV) return;
    const header = `%c[${getTimestamp()}] %c${fileName} %c${methodName}`;
    const styleTimestamp = 'color: #94a3b8; font-weight: bold;';
    const styleFile = 'color: #ffffff; font-weight: bold;';
    const styleMethod = 'color: #ffffff; font-style: italic;';
    const styleMsg = 'color: #ffffff;';

    if (data !== undefined) {
      console.info(`${header}\n%c${message}`, styleTimestamp, styleFile, styleMethod, styleMsg, data);
    } else {
      console.info(`${header}\n%c${message}`, styleTimestamp, styleFile, styleMethod, styleMsg);
    }
  },

  warn: (fileName: string, methodName: string, message: string, data?: any) => {
    if (!IS_DEV) return;
    const header = `%c[${getTimestamp()}] %c${fileName} %c${methodName}`;
    const styleTimestamp = 'color: #94a3b8; font-weight: bold;';
    const styleFile = 'color: #f97316; font-weight: bold;';
    const styleMethod = 'color: #f97316; font-style: italic;';
    const styleMsg = 'color: #f97316;';

    if (data !== undefined) {
      console.warn(`${header}\n%c${message}`, styleTimestamp, styleFile, styleMethod, styleMsg, data);
    } else {
      console.warn(`${header}\n%c${message}`, styleTimestamp, styleFile, styleMethod, styleMsg);
    }
  },

  error: (fileName: string, methodName: string, message: string, error?: any) => {
    if (!IS_DEV) return;
    const header = `%c[${getTimestamp()}] %c${fileName} %c${methodName}`;
    const styleTimestamp = 'color: #94a3b8; font-weight: bold;';
    const styleFile = 'color: #ef4444; font-weight: bold;';
    const styleMethod = 'color: #ef4444; font-style: italic;';
    const styleMsg = 'color: #ef4444;';

    if (error !== undefined) {
      console.error(`${header}\n%c${message}`, styleTimestamp, styleFile, styleMethod, styleMsg, error);
    } else {
      console.error(`${header}\n%c${message}`, styleTimestamp, styleFile, styleMethod, styleMsg);
    }
  },
};
