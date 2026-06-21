import React from 'react';
import FolderIcon from '@mui/icons-material/Folder';
import InsertDriveFileIcon from '@mui/icons-material/InsertDriveFile';
import ImageIcon from '@mui/icons-material/Image';
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';
import DescriptionIcon from '@mui/icons-material/Description';

export const FileIcon = ({ filename, isFolder }) => {
  if (isFolder) return <FolderIcon color="primary" />;
  if (!filename) return <InsertDriveFileIcon />;
  
  const ext = filename.split('.').pop().toLowerCase();
  if (['png', 'jpg', 'jpeg', 'gif', 'svg'].includes(ext)) return <ImageIcon />;
  if (ext === 'pdf') return <PictureAsPdfIcon />;
  if (['txt', 'doc', 'docx'].includes(ext)) return <DescriptionIcon />;
  return <InsertDriveFileIcon />;
};
