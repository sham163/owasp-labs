import React, { useEffect, useState } from 'react';
import { api } from '../api';
import { Typography, Paper, TextField, Button } from '@mui/material';

export default function Forum() {
  const [posts, setPosts] = useState([]);
  const [content, setContent] = useState('');
  const load = async () => setPosts(await api('/forum/list'));
  const post = async () => { await api('/forum/post', { method:'POST', body: JSON.stringify({ content }) }); setContent(''); load(); }
  useEffect(()=>{ load(); }, []);
  return (
    <>
      <Typography variant="h5" sx={{ mb:2 }}>Forum</Typography>
      <Paper sx={{ p:2, mb:2 }}>
        <TextField fullWidth multiline minRows={3} placeholder="Write something…" value={content} onChange={e=>setContent(e.target.value)} />
        <Button sx={{ mt:1 }} variant="contained" onClick={post}>Post</Button>
      </Paper>
      {posts.map(p=>(
        <Paper key={p.id} sx={{ p:2, mb:1 }}>
          <div dangerouslySetInnerHTML={{ __html: p.content }} />
          <small>{p.createdAt}</small>
        </Paper>
      ))}
    </>
  );
}
