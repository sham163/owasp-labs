import React from 'react';
import { Card, CardContent, Grid, Typography, Button } from '@mui/material';
import { Link } from 'react-router-dom';
import { useAuth } from '../AuthContext';

const Tile = ({ title, children, to }) => (
  <Grid item xs={12} md={4}>
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom>{title}</Typography>
        <Typography variant="body2" sx={{ mb: 2 }}>{children}</Typography>
        <Button component={Link} to={to} variant="contained">Open</Button>
      </CardContent>
    </Card>
  </Grid>
);

export default function Home() {
  const { user } = useAuth();
  return (
    <Grid container spacing={2}>
      <Grid item xs={12}><Typography variant="h4">Welcome{user?.username ? `, ${user.username}` : ''}!</Typography></Grid>
      <Tile title="Courses" to="/courses">Browse courses, view details and enroll.</Tile>
      <Tile title="Forum" to="/forum">Discuss with classmates and staff.</Tile>
      <Tile title="My Grades" to="/grades">See your current grades.</Tile>
      {user?.role === 'ADMIN' && (
        <Tile title="Admin" to="/admin">Manage users, courses and grades.</Tile>
      )}
    </Grid>
  );
}
