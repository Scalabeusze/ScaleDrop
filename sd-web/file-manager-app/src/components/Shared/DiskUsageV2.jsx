import React, { useEffect, useMemo, useState } from 'react';
import { Box, Typography, Chip, Card, CardContent, useTheme, IconButton, Tooltip } from '@mui/material';
import { listAllFileMetas } from '../../utils/idb';
import RefreshIcon from '@mui/icons-material/Refresh';
import DownloadIcon from '@mui/icons-material/Download';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { motion, AnimatePresence } from 'motion/react';

const PALETTE = [
  { main: '#3b82f6', light: '#93c5fd', shadow: 'rgba(59, 130, 246, 0.4)' },
  { main: '#10b981', light: '#6ee7b7', shadow: 'rgba(16, 185, 129, 0.4)' },
  { main: '#f59e0b', light: '#fcd34d', shadow: 'rgba(245, 158, 11, 0.4)' },
  { main: '#8b5cf6', light: '#c4b5fd', shadow: 'rgba(139, 92, 246, 0.4)' },
  { main: '#ef4444', light: '#fca5a5', shadow: 'rgba(239, 68, 68, 0.4)' },
  { main: '#06b6d4', light: '#67e8f9', shadow: 'rgba(6, 182, 212, 0.4)' },
  { main: '#f43f5e', light: '#fda4af', shadow: 'rgba(244, 63, 94, 0.4)' },
];

function niceExt(name) {
  const parts = (name || '').split('.');
  return parts.length > 1 ? parts[parts.length-1].toUpperCase() : 'UNKNOWN';
}

function truncateName(name, len = 20) {
  if (!name) return '';
  if (name.length <= len) return name;
  return name.substring(0, len - 3) + '...';
}

function ringCirc(r) { return 2 * Math.PI * r; }

function formatBytes(bytes) {
  if (!bytes) return '0 KB';
  return bytes >= 1024 * 1024 ? (bytes / 1024 / 1024).toFixed(1) + ' MB' : Math.round(bytes / 1024) + ' KB';
}

export const DiskUsageV2 = ({ maxRings = 7 }) => {
  const theme = useTheme();
  const [metas, setMetas] = useState([]);
  const [mounted, setMounted] = useState(false);
  const [hoveredIdx, setHoveredIdx] = useState(null);
  const [selectedExt, setSelectedExt] = useState(null);
  const [refreshKey, setRefreshKey] = useState(0);

  const loadData = async () => {
    setMounted(false);
    try {
      const all = await listAllFileMetas();
      setMetas(all.map(m => ({ id: m.id, ...m.value })));
    } catch (e) {
      console.error(e);
    }
    setTimeout(() => setMounted(true), 50);
  };

  useEffect(() => {
    Promise.resolve().then(loadData);
  }, [refreshKey]);

  const displayData = useMemo(() => {
    if (!selectedExt) {
      const map = new Map();
      for (const m of metas) {
        const ext = niceExt(m.name);
        let total = 0;
        if (m.versions && m.versions.length) total = m.versions.reduce((s,v)=>s+(v.size||0),0);
        else total = m.size||0;
        map.set(ext, (map.get(ext)||0)+total);
      }
      const arr = Array.from(map.entries()).map(([ext,size])=>({ id: ext, label: ext, size }));
      arr.sort((a,b)=>b.size-a.size);
      
      const topArr = arr.slice(0, maxRings);
      const totalSize = topArr.reduce((acc, curr) => acc + curr.size, 0) || 1;

      return {
        mode: 'overview',
        items: topArr,
        totalSize,
        title: 'Storage Distribution',
        subtitle: 'Interactive capacity visualization',
      };
    } else {
      const files = metas.filter(m => niceExt(m.name) === selectedExt).map(m => {
        let size = 0;
        if (m.versions && m.versions.length) size = m.versions.reduce((s,v)=>s+(v.size||0),0);
        else size = m.size||0;
        return { id: m.id, label: m.name, size };
      });
      files.sort((a,b) => b.size - a.size);
      
      const topFiles = files.slice(0, maxRings);
      const totalSize = topFiles.reduce((acc, curr) => acc + curr.size, 0) || 1;

      return {
        mode: 'detail',
        items: topFiles,
        totalSize,
        title: `${selectedExt} Files`,
        subtitle: `Top ${selectedExt} files by size`,
      };
    }
  }, [metas, selectedExt, maxRings]);

  const [transitioning, setTransitioning] = useState(false);
  
  const handleItemClick = (item) => {
    if (displayData.mode === 'overview') {
      setTransitioning(true);
      setHoveredIdx(null);
      setTimeout(() => {
        setSelectedExt(item.label);
        setTransitioning(false);
      }, 300);
    }
  };

  const handleBack = () => {
    setTransitioning(true);
    setHoveredIdx(null);
    setTimeout(() => {
      setSelectedExt(null);
      setTransitioning(false);
    }, 300);
  };

  const handleRefresh = () => setRefreshKey(k => k + 1);

  const handleExportReport = () => {
    let finalCsvContent = '';

    if (!selectedExt) {
      const map = new Map();
      for (const m of metas) {
        const ext = niceExt(m.name);
        let size = 0;
        if (m.versions && m.versions.length) size = m.versions.reduce((s,v)=>s+(v.size||0),0);
        else size = m.size||0;
        map.set(ext, (map.get(ext) || 0) + size);
      }
      const summaryData = Array.from(map.entries()).map(([ext, size]) => ({ Type: ext, Size_Bytes: size, Size_Formatted: formatBytes(size) }));
      summaryData.sort((a,b)=>b.Size_Bytes-a.Size_Bytes);

      const filesData = metas.map(m => {
        let size = 0;
        if (m.versions && m.versions.length) size = m.versions.reduce((s,v)=>s+(v.size||0),0);
        else size = m.size||0;
        return { Name: m.name, Type: niceExt(m.name), Size_Bytes: size, Size_Formatted: formatBytes(size) };
      });
      filesData.sort((a,b)=>b.Size_Bytes-a.Size_Bytes);

      if (summaryData.length === 0 && filesData.length === 0) return;

      const summaryHeaders = summaryData.length > 0 ? Object.keys(summaryData[0]).join(',') : '';
      const summaryCsv = summaryData.length > 0 ? [
        summaryHeaders,
        ...summaryData.map(row => Object.values(row).map(val => `"${val}"`).join(','))
      ].join('\n') : '';

      const filesHeaders = filesData.length > 0 ? Object.keys(filesData[0]).join(',') : '';
      const filesCsv = filesData.length > 0 ? [
        filesHeaders,
        ...filesData.map(row => Object.values(row).map(val => `"${val}"`).join(','))
      ].join('\n') : '';

      finalCsvContent = "--- Summary ---\n" + summaryCsv + "\n\n--- All Files ---\n" + filesCsv;

    } else {
      const reportData = metas.filter(m => niceExt(m.name) === selectedExt).map(m => {
        let size = 0;
        if (m.versions && m.versions.length) size = m.versions.reduce((s,v)=>s+(v.size||0),0);
        else size = m.size||0;
        return { Name: m.name, Size_Bytes: size, Size_Formatted: formatBytes(size) };
      });
      reportData.sort((a,b)=>b.Size_Bytes-a.Size_Bytes);

      if (reportData.length === 0) return;

      const headers = Object.keys(reportData[0]).join(',');
      finalCsvContent = [
        headers,
        ...reportData.map(row => Object.values(row).map(val => `"${val}"`).join(','))
      ].join('\n');
    }

    const blob = new Blob([finalCsvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `disk_usage_report_${selectedExt || 'overview'}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  };

  const isVisible = mounted && !transitioning;

  const cardVariants = {
    hidden: { opacity: 0, y: 30 },
    visible: { opacity: 1, y: 0, transition: { type: 'spring', stiffness: 100, damping: 12 } }
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, gap: 3, alignItems: 'stretch' }}>
      <Card 
        component={motion.div}
        variants={cardVariants}
        initial="hidden"
        animate="visible"
        elevation={theme.palette.mode === 'dark' ? 2 : 4} 
        sx={{ 
          flex: 1, 
          borderRadius: 4, 
          overflow: 'visible',
          background: theme.palette.background.paper
        }}
      >
        <CardContent sx={{ p: { xs: 2, sm: 4 }, position: 'relative' }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 3 }}>
            <Box>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <AnimatePresence mode="popLayout">
                  {displayData.mode === 'detail' && (
                    <motion.div initial={{ opacity: 0, scale: 0 }} animate={{ opacity: 1, scale: 1 }} exit={{ opacity: 0, scale: 0 }} key="back-btn">
                      <IconButton onClick={handleBack} size="small" sx={{ bgcolor: 'action.hover', '&:hover': { bgcolor: 'action.selected' } }}>
                        <ArrowBackIcon fontSize="small" />
                      </IconButton>
                    </motion.div>
                  )}
                </AnimatePresence>
                <motion.div layout>
                  <Typography variant="h5" sx={{ fontWeight: 800, background: `linear-gradient(45deg, ${theme.palette.primary.main}, ${theme.palette.secondary.main})`, WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
                    {displayData.title}
                  </Typography>
                </motion.div>
              </Box>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5, ml: displayData.mode === 'detail' ? 5 : 0, transition: 'margin 0.3s ease' }}>
                {displayData.subtitle}
              </Typography>
            </Box>
            <Tooltip title="Refresh Data">
              <IconButton onClick={handleRefresh} sx={{ color: 'primary.main', bgcolor: 'primary.50', '&:hover': { bgcolor: 'primary.100', transform: 'rotate(180deg)' }, transition: 'all 0.4s ease' }}>
                <RefreshIcon />
              </IconButton>
            </Tooltip>
          </Box>

          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: 380, position: 'relative' }}>
            <svg width="100%" height="380" viewBox="0 0 380 380" style={{ filter: 'drop-shadow(0px 8px 16px rgba(0,0,0,0.08))', opacity: isVisible ? 1 : 0, transition: 'opacity 0.3s ease' }}>
              <defs>
                {displayData.items.map((e,i)=> (
                  <linearGradient id={`g-${i}`} key={i} x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" stopColor={PALETTE[i%PALETTE.length].light} />
                    <stop offset="100%" stopColor={PALETTE[i%PALETTE.length].main} />
                  </linearGradient>
                ))}
                <filter id="glow" x="-20%" y="-20%" width="140%" height="140%">
                  <feGaussianBlur stdDeviation="4" result="blur" />
                  <feComposite in="SourceGraphic" in2="blur" operator="over" />
                </filter>
              </defs>

              {displayData.items.map((e, idx) => {
                const isHovered = hoveredIdx === idx;
                const rBase = 65 + idx * 22;
                const circ = ringCirc(rBase);
                const used = Math.min(1, e.size / displayData.totalSize);
                const dash = circ * used;
                const gap = circ - dash;
                const dasharray = `${dash} ${gap}`;
                const dashoffset = isVisible ? 0 : circ;
                
                const rotation = -90; 

                return (
                  <g key={e.id} transform={`translate(190,190) rotate(${rotation})`}>
                    <motion.circle
                      r={rBase}
                      cx={0}
                      cy={0}
                      fill="none"
                      stroke={theme.palette.mode === 'dark' ? 'rgba(255,255,255,0.05)' : '#f1f5f9'}
                      strokeWidth={12}
                    />
                    
                    <motion.circle
                      r={rBase}
                      cx={0}
                      cy={0}
                      fill="none"
                      stroke={`url(#g-${idx})`}
                      initial={{ strokeDashoffset: circ }}
                      animate={{ strokeDashoffset: dashoffset, strokeWidth: isHovered ? 18 : 12 }}
                      transition={{ type: 'spring', bounce: 0, duration: 1.5 + idx * 0.1 }}
                      strokeLinecap="round"
                      strokeDasharray={dasharray}
                      filter={isHovered ? 'url(#glow)' : 'none'}
                      style={{ 
                        cursor: displayData.mode === 'overview' ? 'pointer' : 'default' 
                      }}
                      onMouseEnter={() => setHoveredIdx(idx)}
                      onMouseLeave={() => setHoveredIdx(null)}
                      onClick={() => handleItemClick(e)}
                    />
                  </g>
                );
              })}

              <g transform="translate(190,190)">
                <circle 
                  r={52} 
                  fill={theme.palette.background.paper} 
                  stroke={`url(#g-${hoveredIdx !== null ? hoveredIdx : 0})`} 
                  strokeWidth={hoveredIdx !== null ? 3 : 1}
                  style={{ transition: 'stroke 0.4s ease, stroke-width 0.4s ease', filter: 'drop-shadow(0 4px 6px rgba(0,0,0,0.1))' }}
                />
                <AnimatePresence mode="wait">
                  {hoveredIdx !== null && displayData.items[hoveredIdx] ? (
                    <motion.g key="hovered-text" initial={{ opacity: 0, y: 5 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -5 }}>
                      <text x={0} y={-8} textAnchor="middle" fontSize={11} fontWeight={700} fill={theme.palette.text.primary}>
                        {truncateName(displayData.items[hoveredIdx].label, 12)}
                      </text>
                      <text x={0} y={14} textAnchor="middle" fontSize={16} fontWeight={800} fill={PALETTE[hoveredIdx%PALETTE.length].main}>
                        {formatBytes(displayData.items[hoveredIdx].size)}
                      </text>
                    </motion.g>
                  ) : (
                    <motion.g key="total-text" initial={{ opacity: 0, y: 5 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -5 }}>
                      <text x={0} y={-4} textAnchor="middle" fontSize={12} fontWeight={600} fill={theme.palette.text.secondary}>
                        {displayData.mode === 'overview' ? 'Total Used' : 'Top Files'}
                      </text>
                      <text x={0} y={18} textAnchor="middle" fontSize={16} fontWeight={800} fill={theme.palette.text.primary}>
                        {formatBytes(displayData.totalSize)}
                      </text>
                    </motion.g>
                  )}
                </AnimatePresence>
              </g>
            </svg>
          </Box>
        </CardContent>
      </Card>

      <Box sx={{ width: { xs: '100%', md: 360 }, display: 'flex', flexDirection: 'column', gap: 3 }}>
        <Card 
          component={motion.div}
          variants={cardVariants}
          initial="hidden"
          animate="visible"
          transition={{ delay: 0.1 }}
          elevation={theme.palette.mode === 'dark' ? 2 : 4} 
          sx={{ borderRadius: 4, flex: 1 }}
        >
          <CardContent sx={{ p: 3, opacity: isVisible ? 1 : 0, transition: 'opacity 0.3s ease' }}>
            <Typography variant="h6" sx={{ fontWeight: 700, mb: 3 }}>Detailed Breakdown</Typography>
            <Box sx={{ 
              display: 'grid', 
              gap: 2.5, 
              maxHeight: 340, 
              overflowY: 'auto', 
              pr: 1,
              '&::-webkit-scrollbar': { width: '6px' },
              '&::-webkit-scrollbar-track': { backgroundColor: 'transparent' },
              '&::-webkit-scrollbar-thumb': { backgroundColor: theme.palette.mode === 'dark' ? 'rgba(255,255,255,0.2)' : 'rgba(0,0,0,0.2)', borderRadius: '4px' }
            }}>
              <AnimatePresence>
                {displayData.items.length === 0 && (
                  <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }}>
                    <Typography variant="body2" color="text.secondary" textAlign="center" py={4}>No files available.</Typography>
                  </motion.div>
                )}
                {displayData.items.map((e, i) => {
                  const pct = displayData.totalSize > 0 ? ((e.size/displayData.totalSize)*100).toFixed(1) : 0;
                  const pColor = PALETTE[i%PALETTE.length];
                  
                  return (
                    <motion.div 
                      key={e.id}
                      layout
                      initial={{ opacity: 0, x: 20 }}
                      animate={{ opacity: 1, x: 0 }}
                      exit={{ opacity: 0, x: -20 }}
                      transition={{ delay: i * 0.05 }}
                      onMouseEnter={() => setHoveredIdx(i)}
                      onMouseLeave={() => setHoveredIdx(null)}
                      onClick={() => handleItemClick(e)}
                      style={{ 
                        padding: '12px', 
                        borderRadius: '12px', 
                        cursor: displayData.mode === 'overview' ? 'pointer' : 'default',
                        border: '1px solid transparent'
                      }}
                      whileHover={{ scale: 1.02, backgroundColor: theme.palette.mode === 'dark' ? 'rgba(255,255,255,0.05)' : '#f8fafc', borderColor: theme.palette.mode === 'dark' ? 'rgba(255,255,255,0.1)' : theme.palette.divider }}
                    >
                      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1.5 }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, maxWidth: '65%' }}>
                          <Box sx={{ 
                            minWidth: 14, height: 14, 
                            borderRadius: '50%', 
                            background: `linear-gradient(135deg, ${pColor.light}, ${pColor.main})`,
                            boxShadow: `0 0 8px ${pColor.shadow}`
                          }} />
                          <Tooltip title={e.label} placement="top-start" arrow>
                            <Typography variant="subtitle2" sx={{ fontWeight: 700, wordBreak: 'break-word', whiteSpace: 'normal', lineHeight: 1.2 }}>
                              {e.label}
                            </Typography>
                          </Tooltip>
                        </Box>
                        <Box sx={{ textAlign: 'right', minWidth: '35%' }}>
                          <Typography variant="body2" sx={{ fontWeight: 800, color: pColor.main }}>{pct}%</Typography>
                          <Typography variant="caption" color="text.secondary" fontWeight={500}>
                             {formatBytes(e.size)}
                          </Typography>
                        </Box>
                      </Box>
                      <Box sx={{ 
                        height: 8, 
                        background: theme.palette.mode === 'dark' ? 'rgba(255,255,255,0.1)' : '#e2e8f0', 
                        borderRadius: 4, 
                        overflow: 'hidden' 
                      }}>
                        <motion.div 
                          initial={{ width: '0%' }}
                          animate={{ width: `${pct}%` }}
                          transition={{ duration: 1, delay: i * 0.1, type: 'spring' }}
                          style={{
                            height: '100%', 
                            background: `linear-gradient(90deg, ${pColor.light}, ${pColor.main})`, 
                            borderRadius: 4
                          }} 
                        />
                      </Box>
                    </motion.div>
                  );
                })}
              </AnimatePresence>
            </Box>
          </CardContent>
        </Card>

        <Card 
          component={motion.div}
          variants={cardVariants}
          initial="hidden"
          animate="visible"
          transition={{ delay: 0.2 }}
          elevation={theme.palette.mode === 'dark' ? 2 : 4} 
          sx={{ borderRadius: 4 }}
        >
          <CardContent sx={{ p: 3 }}>
            <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 2, color: 'text.secondary', textTransform: 'uppercase', letterSpacing: 1 }}>Actions</Typography>
            <Box sx={{ display: 'flex', gap: 1.5 }}>
              <motion.div whileHover={{ scale: 1.05 }} whileTap={{ scale: 0.95 }}>
                <Chip 
                  icon={<DownloadIcon />} 
                  label="Export Report" 
                  clickable 
                  onClick={handleExportReport}
                  color="primary"
                  variant="outlined"
                  sx={{ borderRadius: 2, fontWeight: 600, '&:hover': { bgcolor: 'primary.50' }, px: 1, py: 2.5 }} 
                />
              </motion.div>
            </Box>
          </CardContent>
        </Card>
      </Box>
    </Box>
  );
};

export default DiskUsageV2;
