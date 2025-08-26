import React, { useEffect, useState } from 'react';
import { api } from '../api';
import { Typography, Table, TableBody, TableCell, TableHead, TableRow, Paper } from '@mui/material';

export default function Grades() {
  const [rows, setRows] = useState([]);
  useEffect(()=>{ api('/me/grades').then(setRows); }, []);
  return (
    <Paper sx={{ p:2 }}>
      <Typography variant="h6" sx={{ mb:2 }}>My Grades</Typography>
      <Table>
        <TableHead><TableRow><TableCell>Course</TableCell><TableCell>Grade</TableCell></TableRow></TableHead>
        <TableBody>
          {rows.map((r,i)=>(
            <TableRow key={i}>
              <TableCell>{r.course}</TableCell>
              <TableCell>{r.grade}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </Paper>
  );
}
