import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App.jsx';
import './index.css';
import { GoogleOAuthProvider } from '@react-oauth/google';

const clientId = "393425779945-5hhvuddp5l97dokdl6kshg7gtdceo1lq.apps.googleusercontent.com";
//import.meta.env.VITE_GOOGLE_CLIENT_ID || 'BŁĄD_BRAK_CLIENT_ID';

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <GoogleOAuthProvider clientId={clientId}>
      <App />
    </GoogleOAuthProvider>
  </React.StrictMode>,
);
