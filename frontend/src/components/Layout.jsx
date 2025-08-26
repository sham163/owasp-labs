import React from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { AppBar, Toolbar, Typography, Box, Button, Container } from '@mui/material';
import { useAuth } from '../AuthContext';

const NavBtn = ({ to, children }) => (
  <Button component={NavLink} to={to} sx={{ color: 'white' }}>
    {children}
  </Button>
);

export default function Layout({ children }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: '#f7f9fc' }}>
      <AppBar position="static">
        <Toolbar sx={{ gap: 1 }}>
          <Typography component={Link} to="/" variant="h6" sx={{ color: 'white', textDecoration: 'none', mr: 2 }}>
            Secure University
          </Typography>
          <NavBtn to="/courses">Courses</NavBtn>
          <NavBtn to="/forum">Forum</NavBtn>
          <NavBtn to="/grades">My Grades</NavBtn>
          <NavBtn to="/files">Files</NavBtn>
          {user?.role === 'ADMIN' && <NavBtn to="/admin">Admin</NavBtn>}
          <Box sx={{ flex: 1 }} />
          {user?.username ? (
            <>
              <Typography sx={{ mr: 1 }}>Hi, <b>{user.username}</b></Typography>
              <Button color="inherit" variant="outlined" onClick={logout}>Logout</Button>
            </>
          ) : (
            <Button variant="outlined" color="inherit" onClick={() => navigate('/login')}>Login</Button>
          )}
        </Toolbar>
      </AppBar>
      <Container sx={{ py: 3 }}>
        {children}
        <Box sx={{ mt: 6, textAlign: 'center', color: 'text.secondary' }}>
          <small>
            Training app. Labs live under <Link to="/labs">/labs</Link>.
          </small>
        </Box>
      </Container>
    </Box>
  );
}
