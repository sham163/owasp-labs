import React, { useEffect, useState } from 'react';
import { Typography, Paper, Button } from '@mui/material';

export default function Files() {
  const [files, setFiles] = useState([]);
  const load = async () => {
    const r = await fetch('/api/files/list'); const j = await r.json(); setFiles(j.files || []);
  };
  useEffect(()=>{ load(); }, []);
  const onFile = async (e) => {
    const f = e.target.files?.[0]; if (!f) return;
    const fd = new FormData(); fd.append('file', f);
    await fetch('/api/files/upload', { method:'POST', body: fd });
    load();
  };
  return (
    <Paper sx={{ p:2 }}>
      <Typography variant="h6">Files</Typography>
      <Button sx={{ mt:1, mb:2 }} variant="contained" component="label">Upload<input hidden type="file" onChange={onFile} /></Button>
      <ul>{files.map(n => <li key={n}><a href={`/uploads/${n}`} target="_blank" rel="noreferrer">{n}</a></li>)}</ul>
    </Paper>
  );
}
