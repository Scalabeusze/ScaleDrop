import React, { useState, useEffect } from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, List, ListItem, ListItemText, Checkbox, ListItemIcon, FormControlLabel, Switch, Typography, Collapse, Box } from '@mui/material';

export const ShareForm = ({ open, onClose, fileOptions = [], defaultSelected = [], onConfirm }) => {
  const [recipient, setRecipient] = useState('');
  const [selectedFiles, setSelectedFiles] = useState(defaultSelected || []); // file ids
  const [includeVersionsGlobal, setIncludeVersionsGlobal] = useState(true);
  const [expanded, setExpanded] = useState([]);
  const [selectedVersions, setSelectedVersions] = useState({});

  useEffect(() => {
    Promise.resolve().then(() => setSelectedFiles(defaultSelected || []));
  }, [defaultSelected]);

  useEffect(() => {
    if (!open) {
      Promise.resolve().then(() => {
        setRecipient('');
        setIncludeVersionsGlobal(true);
        setExpanded([]);
        setSelectedVersions({});
      });
    }
  }, [open]);

  const toggleFile = (id) => {
    setSelectedFiles(prev => prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]);
  };

  const toggleExpand = (id) => {
    setExpanded(prev => prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]);
  };

  const toggleVersion = (fileId, versionKey) => {
    setSelectedVersions(prev => {
      const cur = new Set(prev[fileId] || []);
      if (cur.has(versionKey)) cur.delete(versionKey); else cur.add(versionKey);
      return { ...prev, [fileId]: cur };
    });
  };

  const toggleSelectAllVersions = (fileId, allVersionKeys) => {
    setSelectedVersions(prev => {
      const cur = new Set(prev[fileId] || []);
      const allSelected = allVersionKeys.every(k => cur.has(k));
      if (allSelected) {
        // deselect all
        cur.clear();
      } else {
        allVersionKeys.forEach(k => cur.add(k));
      }
      return { ...prev, [fileId]: cur };
    });
  };

  const handleConfirm = () => {
    if (!recipient) return alert('Please enter recipient');
    if (!selectedFiles || selectedFiles.length === 0) return alert('Please select at least one file');
    const fileSelections = selectedFiles.map(fid => {
      const sel = selectedVersions[fid];
      const keys = sel && Array.from(sel);
      return { fileId: fid, versionKeys: keys && keys.length > 0 ? keys : null };
    });
    onConfirm && onConfirm({ recipient, fileSelections, includeVersionsGlobal });
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>Share Files</DialogTitle>
      <DialogContent>
        <TextField label="Recipient (email)" type="email" fullWidth value={recipient} onChange={(e) => setRecipient(e.target.value)} sx={{ mb: 2 }} />
        <Typography variant="subtitle2" sx={{ mb: 1 }}>Select files to share</Typography>
        <List dense>
          {fileOptions.map(f => (
            <Box key={f.id}>
              <ListItem button onClick={() => toggleFile(f.id)}>
                <ListItemIcon>
                  <Checkbox edge="start" checked={selectedFiles.includes(f.id)} tabIndex={-1} disableRipple />
                </ListItemIcon>
                <ListItemText primary={f.name} secondary={`${f.size || 0} bytes`} />
                <Button size="small" onClick={() => toggleExpand(f.id)}>{expanded.includes(f.id) ? 'Hide versions' : 'Show versions'}</Button>
              </ListItem>
              <Collapse in={expanded.includes(f.id)} timeout="auto" unmountOnExit>
                <Box sx={{ pl: 4 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1 }}>
                    <Button size="small" onClick={() => toggleSelectAllVersions(f.id, (f.versions||[]).map(v=>v.versionKey))}>Toggle select all</Button>
                    <Typography variant="body2">Choose specific versions to share (leave empty to share latest)</Typography>
                  </Box>
                  <List dense>
                    {(f.versions || []).map(v => (
                      <ListItem key={v.versionKey} button onClick={() => toggleVersion(f.id, v.versionKey)}>
                        <ListItemIcon>
                          <Checkbox edge="start" checked={(selectedVersions[f.id]||new Set()).has(v.versionKey)} tabIndex={-1} disableRipple />
                        </ListItemIcon>
                        <ListItemText primary={`v${v.versionId}`} secondary={`${v.uploadedAt} — ${v.size || 0} bytes`} />
                      </ListItem>
                    ))}
                  </List>
                </Box>
              </Collapse>
            </Box>
          ))}
        </List>
        <FormControlLabel control={<Switch checked={includeVersionsGlobal} onChange={(e) => setIncludeVersionsGlobal(e.target.checked)} />} label="Include all versions by default" />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button variant="contained" onClick={handleConfirm}>Share</Button>
      </DialogActions>
    </Dialog>
  );
};

export default ShareForm;
