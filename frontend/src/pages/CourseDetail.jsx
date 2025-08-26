import React, { useEffect, useState } from 'react';
import { api } from '../api';
import { useParams } from 'react-router-dom';
import { Typography, Button, Paper } from '@mui/material';

export default function CourseDetail() {
  const { id } = useParams();
  const [c, setC] = useState(null);
  const load = async () => setC(await api(`/courses/${id}`));
  useEffect(()=>{ load(); }, [id]);

  const enroll = async () => {
    await api(`/courses/${id}/enroll`, { method:'POST', body: JSON.stringify({}) });
    alert('Enrolled!');
  };

  if (!c) return null;
  return (
    <Paper sx={{ p:2 }}>
      <Typography variant="h5">{c.name}</Typography>
      <Typography sx={{ mb:2 }}>{c.description}</Typography>
      <Button onClick={enroll} variant="contained">Enroll</Button>
    </Paper>
  );
}
