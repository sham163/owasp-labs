import React, { useEffect, useState } from 'react';
import { api } from '../api';
import { Typography, Grid, Paper, TextField, Button } from '@mui/material';

export default function Admin() {
  const [users, setUsers] = useState([]);
  const [newUser, setNewUser] = useState({ username:'', password:'password', email:'' });
  const [newCourse, setNewCourse] = useState({ name:'', description:'' });
  const [grade, setGrade] = useState({ username:'', courseId:'', grade:'A' });

  const loadUsers = async () => setUsers(await api('/users/admin/list'));
  useEffect(()=>{ loadUsers(); }, []);

  const createUser = async () => { await api('/admin/users/create', { method:'POST', body: JSON.stringify(newUser) }); loadUsers(); };
  const createCourse = async () => { await api('/admin/courses/create', { method:'POST', body: JSON.stringify(newCourse) }); alert('Course created'); };
  const setUserGrade = async () => { await api('/admin/grades/set', { method:'POST', body: JSON.stringify(grade) }); alert('Grade saved'); };

  return (
    <Grid container spacing={2}>
      <Grid item xs={12}><Typography variant="h5">Admin</Typography></Grid>
      <Grid item xs={12} md={6}>
        <Paper sx={{ p:2 }}>
          <Typography variant="h6">Users</Typography>
          <ul>{users.map(u => <li key={u.id}>{u.username} ({u.role}) — {u.email}</li>)}</ul>
        </Paper>
      </Grid>
      <Grid item xs={12} md={6}>
        <Paper sx={{ p:2, mb:2 }}>
          <Typography variant="h6">Create User</Typography>
          <TextField fullWidth label="Username" sx={{ my:1 }} value={newUser.username} onChange={e=>setNewUser({...newUser,username:e.target.value})}/>
          <TextField fullWidth label="Email" sx={{ my:1 }} value={newUser.email} onChange={e=>setNewUser({...newUser,email:e.target.value})}/>
          <TextField fullWidth label="Password" sx={{ my:1 }} value={newUser.password} onChange={e=>setNewUser({...newUser,password:e.target.value})}/>
          <Button variant="contained" onClick={createUser}>Create</Button>
        </Paper>
        <Paper sx={{ p:2, mb:2 }}>
          <Typography variant="h6">Create Course</Typography>
          <TextField fullWidth label="Name" sx={{ my:1 }} value={newCourse.name} onChange={e=>setNewCourse({...newCourse,name:e.target.value})}/>
          <TextField fullWidth label="Description" sx={{ my:1 }} value={newCourse.description} onChange={e=>setNewCourse({...newCourse,description:e.target.value})}/>
          <Button variant="contained" onClick={createCourse}>Create</Button>
        </Paper>
        <Paper sx={{ p:2 }}>
          <Typography variant="h6">Set Grade</Typography>
          <TextField fullWidth label="Username" sx={{ my:1 }} value={grade.username} onChange={e=>setGrade({...grade,username:e.target.value})}/>
          <TextField fullWidth label="Course ID" sx={{ my:1 }} value={grade.courseId} onChange={e=>setGrade({...grade,courseId:e.target.value})}/>
          <TextField fullWidth label="Grade" sx={{ my:1 }} value={grade.grade} onChange={e=>setGrade({...grade,grade:e.target.value})}/>
          <Button variant="contained" onClick={setUserGrade}>Save</Button>
        </Paper>
      </Grid>
    </Grid>
  );
}
