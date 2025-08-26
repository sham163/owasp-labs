import React, { useEffect, useState } from 'react';
import { api } from '../api';
import { Card, CardContent, Typography, Grid, TextField, Button } from '@mui/material';
import { Link } from 'react-router-dom';

export default function Courses() {
  const [rows, setRows] = useState([]);
  const [q, setQ] = useState('');
  const load = async () => setRows(await api('/courses'));
  const search = async () => setRows(await api(`/courses/search?q=${encodeURIComponent(q)}`));
  useEffect(() => { load(); }, []);
  return (
    <>
      <Typography variant="h5" sx={{ mb:2 }}>Courses</Typography>
      <div style={{display:'flex', gap:8, marginBottom:16}}>
        <TextField size="small" placeholder="Search courses" value={q} onChange={e=>setQ(e.target.value)} />
        <Button variant="outlined" onClick={search}>Search</Button>
      </div>
      <Grid container spacing={2}>
        {rows.map(c => (
          <Grid key={c.id} item xs={12} md={4}>
            <Card>
              <CardContent>
                <Typography variant="h6">{c.name}</Typography>
                <Typography variant="body2" sx={{ minHeight: 40, mb:1 }}>{c.description}</Typography>
                <Button component={Link} to={`/courses/${c.id}`} variant="contained">Open</Button>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </>
  );
}
