import { Routes, Route, Navigate } from 'react-router';
import { PublicLayout } from '../components/Layout/PublicLayout';
import { UserLayout } from '../components/Layout/UserLayout';

import { LandingPage } from '../pages/Public/LandingPage';
import { LoginPage } from '../pages/Public/LoginPage';

import { MyFilesPage } from '../pages/User/MyFilesPage';
import { SharedFilesPage } from '../pages/User/SharedFilesPage';
import { ProfilePage } from '../pages/User/ProfilePage';

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

      {/* Catch all */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};
