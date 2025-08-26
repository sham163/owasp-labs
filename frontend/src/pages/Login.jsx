import React, { useState } from 'react';
import { Paper, TextField, Button, Typography, FormControlLabel, Checkbox } from '@mui/material';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../AuthContext';

export default function Login() {
  const { login } = useAuth();
  const nav = useNavigate();
  const [f, setF] = useState({ username: '', password: '', remember: true });
  const [err, setErr] = useState('');

  const submit = async (e) => {
    e.preventDefault();
    setErr('');
    try { await login(f.username, f.password, f.remember); nav('/'); }
    catch { setErr('Invalid credentials'); }
  };

  return (
    <Paper sx={{ p: 3, maxWidth: 420, mx: 'auto' }}>
      <Typography variant="h5" gutterBottom>Login</Typography>
      <form onSubmit={submit}>
        <TextField fullWidth label="Username" margin="normal" value={f.username} onChange={(e)=>setF({...f,username:e.target.value})}/>
        <TextField fullWidth label="Password" margin="normal" type="password" value={f.password} onChange={(e)=>setF({...f,password:e.target.value})}/>
        <FormControlLabel control={<Checkbox checked={f.remember} onChange={e=>setF({...f,remember:e.target.checked})} />} label="Remember me" />
        {err && <Typography color="error" sx={{ mb:1 }}>{err}</Typography>}
        <Button fullWidth type="submit" variant="contained">Sign in</Button>
      </form>
      <Typography sx={{ mt:2 }}>No account? <Link to="/register">Register</Link></Typography>
    </Paper>
  );
}
