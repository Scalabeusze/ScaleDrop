import React, { useState, useEffect, useMemo } from 'react';
import { Box, Typography, Card, Grid, Stack, useTheme } from '@mui/material';
import StorageIcon from '@mui/icons-material/Storage';
import DataSaverOnIcon from '@mui/icons-material/DataSaverOn';
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';
import AnimatedDiskUsage from '../../components/Shared/DiskUsageV2';
import { listAllFileMetas } from '../../utils/idb';
import { motion } from 'motion/react';

const containerVariants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.15,
      delayChildren: 0.1,
    },
  },
};

const itemVariants = {
  hidden: { opacity: 0, y: 20 },
  visible: { 
    opacity: 1, 
    y: 0, 
    transition: { type: 'spring', stiffness: 100, damping: 15 } 
  },
};

export const DiskUsagePage = () => {
  const theme = useTheme();
  const [metas, setMetas] = useState([]);

  useEffect(() => {
    (async () => {
      const all = await listAllFileMetas();
      setMetas(all.map(m => ({ id: m.id, ...m.value })));
    })();
  }, []);

  const totals = useMemo(() => {
    let count = 0; let bytes = 0;
    for (const m of metas) {
      count += 1;
      if (m.versions && m.versions.length) bytes += m.versions.reduce((s, v) => s + (v.size || 0), 0);
      else bytes += m.size||0;
    }
    return { count, bytes };
  }, [metas]);

  return (
    <Box 
      component={motion.div}
      variants={containerVariants}
      initial="hidden"
      animate="visible"
      sx={{ p: { xs: 2, md: 4 }, maxWidth: 1400, mx: 'auto' }}
    >
      <motion.div variants={itemVariants}>
        <Stack direction="row" alignItems="center" spacing={2.5} sx={{ mb: 5 }}>
          <Box sx={{ 
            p: 2, 
            borderRadius: 3, 
            background: `linear-gradient(135deg, ${theme.palette.primary.light}, ${theme.palette.primary.main})`,
            boxShadow: `0 8px 16px ${theme.palette.mode === 'dark' ? 'rgba(0,0,0,0.4)' : 'rgba(25, 118, 210, 0.25)'}`,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: 'white'
          }}>
            <StorageIcon sx={{ fontSize: 32 }} />
          </Box>
          <Box>
            <Typography variant="h4" sx={{ fontWeight: 800, letterSpacing: '-0.5px' }}>
              Storage Analytics
            </Typography>
            <Typography variant="body1" color="text.secondary" sx={{ mt: 0.5 }}>
              Deep dive into your file usage and storage distribution.
            </Typography>
          </Box>
        </Stack>
      </motion.div>

      <Box sx={{ mb: 4 }}>
        <Grid container spacing={3} sx={{ mb: 4 }}>
           <Grid item xs={12} sm={6} md={4} component={motion.div} variants={itemVariants} whileHover={{ scale: 1.02 }} transition={{ type: 'spring', stiffness: 300 }}>
               <Card elevation={0} sx={{ 
                 p: 3, borderRadius: 4, 
                 bgcolor: theme.palette.mode === 'dark' ? 'background.paper' : '#f8fafc',
                 border: '1px solid', borderColor: 'divider'
               }}>
                   <Stack direction="row" alignItems="center" spacing={2}>
                       <DataSaverOnIcon color="primary" sx={{ fontSize: 40, opacity: 0.8 }} />
                       <Box>
                           <Typography variant="body2" color="text.secondary" fontWeight={600} textTransform="uppercase">Total Files</Typography>
                           <Typography variant="h4" fontWeight={800}>{totals.count}</Typography>
                       </Box>
                   </Stack>
               </Card>
           </Grid>
           <Grid item xs={12} sm={6} md={4} component={motion.div} variants={itemVariants} whileHover={{ scale: 1.02 }} transition={{ type: 'spring', stiffness: 300 }}>
               <Card elevation={0} sx={{ 
                 p: 3, borderRadius: 4, 
                 bgcolor: theme.palette.mode === 'dark' ? 'background.paper' : '#f8fafc',
                 border: '1px solid', borderColor: 'divider'
               }}>
                   <Stack direction="row" alignItems="center" spacing={2}>
                       <StorageIcon color="secondary" sx={{ fontSize: 40, opacity: 0.8 }} />
                       <Box>
                           <Typography variant="body2" color="text.secondary" fontWeight={600} textTransform="uppercase">Used Space</Typography>
                           <Typography variant="h4" fontWeight={800}>{Math.round((totals.bytes/1024/1024)*100)/100} MB</Typography>
                       </Box>
                   </Stack>
               </Card>
           </Grid>
           <Grid item xs={12} md={4} component={motion.div} variants={itemVariants} whileHover={{ scale: 1.02 }} transition={{ type: 'spring', stiffness: 300 }}>
               <Card elevation={0} sx={{ 
                 p: 3, borderRadius: 4, height: '100%',
                 background: `linear-gradient(135deg, ${theme.palette.primary.main}, ${theme.palette.secondary.main})`,
                 color: 'white', display: 'flex', flexDirection: 'column', justifyContent: 'center'
               }}>
                   <Stack direction="row" alignItems="flex-start" spacing={1.5}>
                       <InfoOutlinedIcon sx={{ opacity: 0.9 }} />
                       <Typography variant="body2" sx={{ fontWeight: 600, lineHeight: 1.6, opacity: 0.9 }}>
                         Hover over the storage rings below to see detailed format usage.
                       </Typography>
                   </Stack>
               </Card>
           </Grid>
        </Grid>
        <motion.div variants={itemVariants}>
          <AnimatedDiskUsage maxCircles={7} />
        </motion.div>
      </Box>
    </Box>
  );
};

export default DiskUsagePage;
