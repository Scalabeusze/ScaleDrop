
import React from 'react';
import { Box, Typography, Paper, List, ListItem, ListItemText, ListItemIcon } from '@mui/material';
import { FileIcon } from '../../components/Shared/FileIcon';
import { motion, AnimatePresence } from 'motion/react';

const containerVariants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.1,
    },
  },
};

const itemVariants = {
  hidden: { opacity: 0, x: -20 },
  visible: { opacity: 1, x: 0, transition: { type: 'spring', stiffness: 200, damping: 20 } },
};

export const SharedFilesPage = () => {
  const shares = JSON.parse(localStorage.getItem('shared_files') || '[]');
  return (
    <Box sx={{ maxWidth: 1200, mx: 'auto', p: { xs: 2, md: 4 } }}>
      <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.4 }}>
        <Typography variant="h3" gutterBottom sx={{ fontWeight: 800, background: 'linear-gradient(45deg, #1976d2, #9c27b0)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent', mb: 1 }}>
          Shared With Me
        </Typography>
        <Typography variant="body1" color="text.secondary" paragraph sx={{ mb: 4 }}>
          View and manage files that others have shared with you.
        </Typography>
      </motion.div>
      <Paper sx={{ mt: 2, p: 3, borderRadius: 4, boxShadow: '0 8px 32px rgba(0,0,0,0.05)', border: '1px solid', borderColor: 'divider' }} component={motion.div} initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.4, type: 'spring', stiffness: 100 }}>
        <List component={motion.ul} variants={containerVariants} initial="hidden" animate="visible">
          <AnimatePresence>
            {shares.length === 0 ? (
              <motion.div variants={itemVariants} key="empty">
                <ListItem>
                  <ListItemText primary="No files shared with you." />
                </ListItem>
              </motion.div>
            ) : shares.map(s => (
              <motion.div variants={itemVariants} key={s.id} layout>
                <ListItem divider>
                  <ListItemIcon>
                    <FileIcon filename={s.name} />
                  </ListItemIcon>
                  <ListItemText primary={s.name} secondary={`Shared by: ${s.sharedBy} — to: ${s.recipient}`} />
                </ListItem>
              </motion.div>
            ))}
          </AnimatePresence>
        </List>
      </Paper>
    </Box>
  );
};

