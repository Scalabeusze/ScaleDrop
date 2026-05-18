// Minimal Web Crypto helpers for client-side file encryption/decryption
const enc = new TextEncoder();

function toBase64(buffer) {
  const bytes = new Uint8Array(buffer);
  let binary = '';
  for (let i = 0; i < bytes.byteLength; i++) binary += String.fromCharCode(bytes[i]);
  return btoa(binary);
}

function fromBase64(b64) {
  const binary = atob(b64);
  const len = binary.length;
  const bytes = new Uint8Array(len);
  for (let i = 0; i < len; i++) bytes[i] = binary.charCodeAt(i);
  return bytes.buffer;
}

export async function deriveKey(password, salt, iterations = 100_000) {
  const baseKey = await crypto.subtle.importKey('raw', enc.encode(password), 'PBKDF2', false, ['deriveKey']);
  return crypto.subtle.deriveKey(
    { name: 'PBKDF2', salt, iterations, hash: 'SHA-256' },
    baseKey,
    { name: 'AES-GCM', length: 256 },
    false,
    ['encrypt', 'decrypt']
  );
}


export async function encryptFile(file, password) {
  const salt = crypto.getRandomValues(new Uint8Array(16));
  const iv = crypto.getRandomValues(new Uint8Array(12));
  const key = await deriveKey(password, salt.buffer);
  const arrayBuffer = await file.arrayBuffer();
  const ciphertext = await crypto.subtle.encrypt({ name: 'AES-GCM', iv }, key, arrayBuffer);
  return {
    ciphertext, // ArrayBuffer
    ivBase64: toBase64(iv.buffer),
    saltBase64: toBase64(salt.buffer),
  };
}

// ciphertext can be ArrayBuffer or base64 string
export async function decryptData(ciphertextInput, password, ivBase64, saltBase64) {
  let ciphertextBuffer;
  if (typeof ciphertextInput === 'string') {
    ciphertextBuffer = fromBase64(ciphertextInput);
  } else {
    ciphertextBuffer = ciphertextInput;
  }
  const iv = new Uint8Array(fromBase64(ivBase64));
  const salt = new Uint8Array(fromBase64(saltBase64));
  const key = await deriveKey(password, salt.buffer);
  const plaintext = await crypto.subtle.decrypt({ name: 'AES-GCM', iv }, key, ciphertextBuffer);
  return plaintext; // ArrayBuffer
}

export function arrayBufferToBlob(buffer, type = 'application/octet-stream') {
  return new Blob([buffer], { type });
}

export async function hashBuffer(buffer) {
  const buf = buffer instanceof ArrayBuffer ? buffer : buffer.buffer || buffer;
  const digest = await crypto.subtle.digest('SHA-256', buf);
  return Array.from(new Uint8Array(digest)).map(b => b.toString(16).padStart(2, '0')).join('');
}
