// Minimal IndexedDB helper for storing encrypted file ciphertext and metadata
function openDb() {
  // Open DB, ensure required stores exist. If missing, reopen with higher version to create them.
  const REQUIRED = ['encrypted_files', 'files_meta'];
  return new Promise((resolve, reject) => {
    const req = indexedDB.open('file-manager-db');
    req.onsuccess = (e) => {
      const db = e.target.result;
      try {
        const missing = REQUIRED.filter(s => !db.objectStoreNames.contains(s));
        if (missing.length === 0) {
          resolve(db);
          return;
        }
        // Need to upgrade DB to create missing stores
        const newVersion = db.version + 1;
        db.close();
        const upReq = indexedDB.open('file-manager-db', newVersion);
        upReq.onupgradeneeded = (ue) => {
          const udb = ue.target.result;
          for (const s of missing) {
            if (!udb.objectStoreNames.contains(s)) udb.createObjectStore(s);
          }
        };
        upReq.onsuccess = () => resolve(upReq.result);
        upReq.onerror = () => reject(upReq.error);
      } catch (err) {
        db.close();
        reject(err);
      }
    };
    req.onerror = () => reject(req.error);
  });
}

export async function saveEncryptedFile(id, meta, arrayBuffer) {
  const db = await openDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction('encrypted_files', 'readwrite');
    const store = tx.objectStore('encrypted_files');
    const value = { meta, ciphertext: arrayBuffer };
    const putReq = store.put(value, id);
    putReq.onsuccess = () => {
      tx.oncomplete = () => { db.close(); resolve(); };
    };
    putReq.onerror = () => { db.close(); reject(putReq.error); };
  });
}

export async function getEncryptedFile(id) {
  const db = await openDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction('encrypted_files', 'readonly');
    const store = tx.objectStore('encrypted_files');
    const getReq = store.get(id);
    getReq.onsuccess = () => {
      db.close();
      resolve(getReq.result);
    };
    getReq.onerror = () => { db.close(); reject(getReq.error); };
  });
}

export async function getAllEncryptedFiles() {
  const db = await openDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction('encrypted_files', 'readonly');
    const store = tx.objectStore('encrypted_files');
    const req = store.openCursor();
    const results = [];
    req.onsuccess = (e) => {
      const cursor = e.target.result;
      if (cursor) {
        results.push({ id: cursor.key, value: cursor.value });
        cursor.continue();
      } else {
        db.close();
        resolve(results);
      }
    };
    req.onerror = () => { db.close(); reject(req.error); };
  });
}

export async function saveFileMeta(fileId, meta) {
  const db = await openDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction('files_meta', 'readwrite');
    const store = tx.objectStore('files_meta');
    const putReq = store.put(meta, fileId);
    putReq.onsuccess = () => { tx.oncomplete = () => { db.close(); resolve(); }; };
    putReq.onerror = () => { db.close(); reject(putReq.error); };
  });
}

export async function getFileMeta(fileId) {
  const db = await openDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction('files_meta', 'readonly');
    const store = tx.objectStore('files_meta');
    const getReq = store.get(fileId);
    getReq.onsuccess = () => { db.close(); resolve(getReq.result); };
    getReq.onerror = () => { db.close(); reject(getReq.error); };
  });
}

export async function listAllFileMetas() {
  const db = await openDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction('files_meta', 'readonly');
    const store = tx.objectStore('files_meta');
    const req = store.openCursor();
    const results = [];
    req.onsuccess = (e) => {
      const cursor = e.target.result;
      if (cursor) {
        results.push({ id: cursor.key, value: cursor.value });
        cursor.continue();
      } else {
        db.close();
        resolve(results);
      }
    };
    req.onerror = () => { db.close(); reject(req.error); };
  });
}

export async function deleteEncryptedFile(id) {
  const db = await openDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction('encrypted_files', 'readwrite');
    const store = tx.objectStore('encrypted_files');
    const delReq = store.delete(id);
    delReq.onsuccess = () => { tx.oncomplete = () => { db.close(); resolve(); }; };
    delReq.onerror = () => { db.close(); reject(delReq.error); };
  });
}

export async function deleteFileMeta(fileId) {
  const db = await openDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction('files_meta', 'readwrite');
    const store = tx.objectStore('files_meta');
    const delReq = store.delete(fileId);
    delReq.onsuccess = () => { tx.oncomplete = () => { db.close(); resolve(); }; };
    delReq.onerror = () => { db.close(); reject(delReq.error); };
  });
}
