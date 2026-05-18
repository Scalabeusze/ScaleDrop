import React, { useContext } from 'react';
import { Switch, FormControlLabel } from '@mui/material';
import { ThemeContext } from '../../context/themeContext';

export const ThemeSwitcher = ({ color = "default", sx = {} }) => {
  const { isDarkMode, toggleTheme } = useContext(ThemeContext);
  
  return (
    <FormControlLabel
      control={<Switch checked={isDarkMode} onChange={toggleTheme} color={color} />}
      label="Dark Mode"
      sx={sx}
    />
  );
};
