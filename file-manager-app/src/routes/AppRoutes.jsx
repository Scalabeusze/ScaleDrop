import React from 'react';
import { Routes, Route, Navigate } from 'react-router';
import { PublicLayout } from '../components/Layout/PublicLayout';
import { UserLayout } from '../components/Layout/UserLayout';
import { AdminLayout } from '../components/Layout/AdminLayout';

import { LandingPage } from '../pages/Public/LandingPage';
import { LoginPage } from '../pages/Public/LoginPage';

import { MyFilesPage } from '../pages/User/MyFilesPage';
import { SharedFilesPage } from '../pages/User/SharedFilesPage';
import { ProfilePage } from '../pages/User/ProfilePage';

import { AdminDashboard } from '../pages/Admin/AdminDashboard';
import { SystemLogs } from '../pages/Admin/SystemLogs';

import { ProtectedRoute } from './ProtectedRoute';

export const AppRoutes = () => {
  return (
    <Routes>
      {/* Public Routes */}
      <Route element={<PublicLayout />}>
        <Route path="/" element={<LandingPage />} />
        <Route path="/login" element={<LoginPage />} />
      </Route>

      {/* User Routes */}
      <Route element={<ProtectedRoute allowedRoles={['user']} />}>
        <Route element={<UserLayout />}>
          <Route path="/user/my-files" element={<MyFilesPage />} />
          <Route path="/user/shared-files" element={<SharedFilesPage />} />
          <Route path="/user/profile" element={<ProfilePage />} />
        </Route>
      </Route>

      {/* Admin Routes */}
      <Route element={<ProtectedRoute allowedRoles={['admin']} />}>
        <Route element={<AdminLayout />}>
          <Route path="/admin/dashboard" element={<AdminDashboard />} />
          <Route path="/admin/logs" element={<SystemLogs />} />
        </Route>
      </Route>

      {/* Catch all */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};
