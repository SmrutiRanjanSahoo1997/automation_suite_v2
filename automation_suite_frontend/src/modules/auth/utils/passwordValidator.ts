/**
 * Strict Password Rules Validation Utility.
 * Rule: >= 8 chars long.
 * Contains: >=1 uppercase, >=1 lowercase, >=1 digit, >=1 allowed symbol.
 * Allowed symbols: ~!@#$%^&*-_+=.?:
 * Forbidden: Whitespace, spaces, tabs, or any unapproved characters.
 */

export interface PasswordValidationResult {
  isValid: boolean;
  errorReason?: string;
}

export function validatePasswordRules(password: string): PasswordValidationResult {
  if (!password || password.length < 8) {
    return { isValid: false, errorReason: 'Password must be at least 8 characters long.' };
  }

  // Check forbidden whitespace
  if (/\s/.test(password)) {
    return { isValid: false, errorReason: 'Password must not contain any whitespace, spaces, or tabs.' };
  }

  // Allowed character set regex check: alphanumeric + allowed symbols ~!@#$%^&*-_+=.?:
  const allowedPattern = /^[A-Za-z0-9~!@#$%^&*_\-+=\.\?:]+$/;
  if (!allowedPattern.test(password)) {
    return { isValid: false, errorReason: 'Password contains unapproved special characters. Only ~!@#$%^&*-_+=.?: are allowed.' };
  }

  // Uppercase letter check
  if (!/[A-Z]/.test(password)) {
    return { isValid: false, errorReason: 'Password must contain at least one uppercase letter (A-Z).' };
  }

  // Lowercase letter check
  if (!/[a-z]/.test(password)) {
    return { isValid: false, errorReason: 'Password must contain at least one lowercase letter (a-z).' };
  }

  // Digit check
  if (!/[0-9]/.test(password)) {
    return { isValid: false, errorReason: 'Password must contain at least one digit (0-9).' };
  }

  // Allowed symbol check
  const symbolPattern = /[~!@#$%^&*_\-+=\.\?:]/;
  if (!symbolPattern.test(password)) {
    return { isValid: false, errorReason: 'Password must contain at least one symbol (~!@#$%^&*-_+=.?:).' };
  }

  return { isValid: true };
}
