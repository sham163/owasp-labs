import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import { AuthProvider } from './AuthContext';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import Courses from './pages/Courses';
import CourseDetail from './pages/CourseDetail';
import Grades from './pages/Grades';
import Forum from './pages/Forum';
import Files from './pages/Files';
import Admin from './pages/Admin';
import Labs from './pages/Labs';
import VulnLabs from './pages/VulnLabs';
import ProtectedRoute from './components/ProtectedRoute';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Layout>
          <Routes>
            <Route path="/" element={<Home/>}/>
            <Route path="/login" element={<Login/>}/>
            <Route path="/register" element={<Register/>}/>
            <Route path="/courses" element={<Courses/>}/>
            <Route path="/courses/:id" element={<ProtectedRoute><CourseDetail/></ProtectedRoute>} />
            <Route path="/grades" element={<ProtectedRoute><Grades/></ProtectedRoute>} />
            <Route path="/forum" element={<Forum/>}/>
            <Route path="/files" element={<ProtectedRoute><Files/></ProtectedRoute>} />
            <Route path="/admin" element={<ProtectedRoute role="ADMIN"><Admin/></ProtectedRoute>} />
            <Route path="/labs" element={<Labs/>}/>
            <Route path="/vulnlabs" element={<VulnLabs/>}/>
            <Route path="*" element={<div>Not Found</div>}/>
          </Routes>
        </Layout>
      </BrowserRouter>
    </AuthProvider>
  );
}
