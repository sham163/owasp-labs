import React, { useState } from 'react';
import { Typography, TextField, Button } from '@mui/material';
import { api } from '../api';

export default function Labs() {
  const [out, setOut] = useState('');
  const runSQLi = async (level, q) => setOut(JSON.stringify(await api(`/labs/sqli/${level}?q=${encodeURIComponent(q)}`), null, 2));
  return (
    <>
      <Typography variant="h5" sx={{ mb:2 }}>Labs (OWASP)</Typography>
      <div style={{display:'flex', gap:8}}>
        <TextField label="SQLi term" size="small" id="sqliq" />
        <Button variant="outlined" onClick={() => runSQLi('easy', document.getElementById('sqliq').value)}>SQLi Easy</Button>
        <Button variant="outlined" onClick={() => runSQLi('hard', document.getElementById('sqliq').value)}>SQLi Hard</Button>
      </div>
      <pre style={{ marginTop: 16 }}>{out}</pre>
    </>
  );
}
