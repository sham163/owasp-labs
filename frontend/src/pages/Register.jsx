import React, { useState } from 'react';
import { Paper, TextField, Button, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../AuthContext';

export default function Register() {
  const { register, login } = useAuth();
  const nav = useNavigate();
  const [f, setF] = useState({ username:'', email:'', password:'' });
  const [err, setErr] = useState('');

  const submit = async (e) => {
    e.preventDefault();
    setErr('');
    try { await register(f.username, f.password, f.email); await login(f.username, f.password, true); nav('/'); }
    catch { setErr('Failed to register'); }
  };

  return (
    <Paper sx={{ p: 3, maxWidth: 480, mx:'auto' }}>
      <Typography variant="h5" gutterBottom>Register</Typography>
      <form onSubmit={submit}>
        <TextField fullWidth label="Username" margin="normal" value={f.username} onChange={(e)=>setF({...f,username:e.target.value})}/>
        <TextField fullWidth label="Email" margin="normal" value={f.email} onChange={(e)=>setF({...f,email:e.target.value})}/>
        <TextField fullWidth label="Password" type="password" margin="normal" value={f.password} onChange={(e)=>setF({...f,password:e.target.value})}/>
        {err && <Typography color="error">{err}</Typography>}
        <Button fullWidth variant="contained" type="submit" sx={{ mt:2 }}>Create account</Button>
      </form>
    </Paper>
  );
}
