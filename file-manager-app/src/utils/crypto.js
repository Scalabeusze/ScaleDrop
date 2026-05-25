// Minimal Web Crypto helpers for client-side file encryption/decryption
const enc = new TextEncoder();

const toBase64 = (buffer) => 
  btoa(Array.from(new Uint8Array(buffer), b => String.fromCharCode(b)).join(''));

const fromBase64 = (b64) => 
  Uint8Array.from(atob(b64), c => c.charCodeAt(0)).buffer;

export const deriveKey = async (password, salt, iterations = 100_000) => {
  const baseKey = await crypto.subtle.importKey('raw', enc.encode(password), 'PBKDF2', false, ['deriveKey']);
  return crypto.subtle.deriveKey(
    { name: 'PBKDF2', salt, iterations, hash: 'SHA-256' },
    baseKey,
    { name: 'AES-GCM', length: 256 },
    false,
    ['encrypt', 'decrypt']
  );
};

export const encryptFile = async (file, password) => {
  const salt = crypto.getRandomValues(new Uint8Array(16));
  const iv = crypto.getRandomValues(new Uint8Array(12));
  const key = await deriveKey(password, salt.buffer);
  
  const ciphertext = await crypto.subtle.encrypt(
    { name: 'AES-GCM', iv }, 
    key, 
    await file.arrayBuffer()
  );
  
  return {
    ciphertext,
    ivBase64: toBase64(iv.buffer),
    saltBase64: toBase64(salt.buffer),
  };
};

export const decryptData = async (ciphertextInput, password, ivBase64, saltBase64) => {
  const ciphertextBuffer = typeof ciphertextInput === 'string' 
    ? fromBase64(ciphertextInput) 
    : ciphertextInput;
    
  const iv = new Uint8Array(fromBase64(ivBase64));
  const salt = new Uint8Array(fromBase64(saltBase64));
  const key = await deriveKey(password, salt.buffer);
  
  return crypto.subtle.decrypt({ name: 'AES-GCM', iv }, key, ciphertextBuffer);
};

export const arrayBufferToBlob = (buffer, type = 'application/octet-stream') => 
  new Blob([buffer], { type });

export const hashBuffer = async (buffer) => {
  const buf = buffer instanceof ArrayBuffer ? buffer : buffer.buffer || buffer;
  const digest = await crypto.subtle.digest('SHA-256', buf);
  
  return Array.from(new Uint8Array(digest))
    .map(b => b.toString(16).padStart(2, '0'))
    .join('');
};
