
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
    <Box>
      <Typography variant="h4" gutterBottom>
        Shared With Me
      </Typography>
      <Paper sx={{ mt: 3, p: 3 }} component={motion.div} initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.4 }}>
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

