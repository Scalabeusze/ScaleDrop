import React, { useState, useEffect } from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, Typography, Box } from '@mui/material';

export const ShareForm = ({ open, onClose, fileOptions = [], defaultSelected = [], onConfirm }) => {
  const [recipient, setRecipient] = useState('');

  useEffect(() => {
    if (!open) {
      Promise.resolve().then(() => {
        setRecipient('');
      });
    }
  }, [open]);

  const handleConfirm = () => {
    if (!recipient) return alert('Please enter recipient');
    if (!defaultSelected || defaultSelected.length === 0) return alert('No file selected to share');
    const fileSelections = defaultSelected.map(fid => ({ fileId: fid }));
    onConfirm && onConfirm({ recipient, fileSelections });
  };

  const selectedFileNames = fileOptions
    .filter(f => defaultSelected.includes(f.id))
    .map(f => f.name)
    .join(', ');

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>Share File</DialogTitle>
      <DialogContent>
        {selectedFileNames && (
          <Typography variant="subtitle2" sx={{ mb: 2 }}>
            Sharing: {selectedFileNames}
          </Typography>
        )}
        <TextField label="Recipient (email)" type="email" fullWidth value={recipient} onChange={(e) => setRecipient(e.target.value)} sx={{ mb: 2, mt: 1 }} />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button variant="contained" onClick={handleConfirm}>Share</Button>
      </DialogActions>
    </Dialog>
  );
};

export default ShareForm;


