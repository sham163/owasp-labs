import React, { useState } from 'react';
import { Typography, TextField, Button, Card, CardContent, Grid, Box, Alert } from '@mui/material';
import { api } from '../api';

export default function VulnLabs() {
  const [results, setResults] = useState({});
  const [inputs, setInputs] = useState({});

  const handleInputChange = (key, value) => {
    setInputs(prev => ({ ...prev, [key]: value }));
  };

  const testVulnerability = async (type, endpoint, params = {}) => {
    try {
      const response = await fetch(`/api${endpoint}?${new URLSearchParams(params)}`);
      const data = await response.text();
      setResults(prev => ({ 
        ...prev, 
        [type]: { 
          success: true, 
          data, 
          endpoint,
          params: JSON.stringify(params, null, 2)
        } 
      }));
    } catch (error) {
      setResults(prev => ({ 
        ...prev, 
        [type]: { 
          success: false, 
          error: error.message,
          endpoint,
          params: JSON.stringify(params, null, 2)
        } 
      }));
    }
  };

  const testSQLi = async (level) => {
    const payload = inputs.sqli || "' OR '1'='1";
    await testVulnerability(`sqli_${level}`, `/labs/sqli/${level}`, { q: payload });
  };

  const testAdvancedSQLi = async (type) => {
    const payload = inputs.advsqli || "' OR '1'='1";
    if (type === 'union') {
      await testVulnerability('sqli_union', '/advsearch/union', { 
        category: payload,
        orderBy: inputs.orderBy || "(SELECT username FROM users LIMIT 1)"
      });
    } else if (type === 'blind') {
      await testVulnerability('sqli_blind', '/advsearch/query', { 
        q: payload,
        table: inputs.table || 'users',
        column: inputs.column || 'username'
      });
    } else if (type === 'time') {
      await testVulnerability('sqli_time', '/advsearch/filter', { 
        term: 'test',
        filter: inputs.filter || "1=1 AND (SELECT CASE WHEN (1=1) THEN pg_sleep(5) ELSE pg_sleep(0) END)"
      });
    }
  };

  const testXSS = async (type, level = 'easy') => {
    const payload = inputs.xss || "<script>alert('XSS')</script>";
    if (type === 'reflected') {
      await testVulnerability(`xss_reflected_${level}`, `/labs/xss/reflected/${level}`, { q: payload });
    } else if (type === 'stored') {
      const response = await fetch(`/api/labs/xss/stored/${level}/post`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ content: payload })
      });
      const data = await response.json();
      setResults(prev => ({ 
        ...prev, 
        [`xss_stored_${level}`]: { 
          success: true, 
          data: JSON.stringify(data, null, 2),
          endpoint: `/labs/xss/stored/${level}/post`,
          payload
        } 
      }));
    }
  };

  const testIDOR = async (type) => {
    const userId = inputs.userId || "2";
    if (type === 'profile') {
      await testVulnerability('idor_profile', `/profile/user/${userId}`, {});
    } else if (type === 'export') {
      await testVulnerability('idor_export', `/users/export/${userId}`, { format: 'full' });
    } else if (type === 'update') {
      const response = await fetch(`/api/users/${userId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ 
          email: 'hacked@evil.com',
          role: 'ADMIN',
          password: 'hacked123'
        })
      });
      const data = await response.json();
      setResults(prev => ({ 
        ...prev, 
        'idor_update': { 
          success: true, 
          data: JSON.stringify(data, null, 2),
          endpoint: `/users/${userId}`,
          method: 'PUT'
        } 
      }));
    }
  };

  const testPathTraversal = async (type) => {
    if (type === 'read') {
      const filename = inputs.filename || "../../../../../../etc/passwd";
      await testVulnerability('path_read', `/files/read/${encodeURIComponent(filename)}`, {});
    } else if (type === 'list') {
      const path = inputs.listPath || "../../../../../../etc/";
      await testVulnerability('path_list', '/files/list', { path });
    } else if (type === 'download') {
      const category = inputs.category || "..";
      const file = inputs.file || "../../etc/passwd";
      await testVulnerability('path_download', `/documents/download/${category}/${file}`, {});
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" sx={{ mb: 3 }}>Security Testing Labs</Typography>
      
      <Grid container spacing={3}>
        {/* SQL Injection */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>SQL Injection</Typography>
              <TextField
                fullWidth
                label="SQL Payload"
                value={inputs.sqli || ''}
                onChange={(e) => handleInputChange('sqli', e.target.value)}
                placeholder="' OR '1'='1"
                sx={{ mb: 2 }}
              />
              <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                <Button variant="outlined" onClick={() => testSQLi('easy')}>Basic SQLi</Button>
                <Button variant="outlined" onClick={() => testSQLi('medium')}>Filtered SQLi</Button>
                <Button variant="outlined" onClick={() => testSQLi('hard')}>ORDER BY SQLi</Button>
              </Box>
              
              <Typography variant="subtitle1" sx={{ mt: 2, mb: 1 }}>Advanced SQL Injection</Typography>
              <TextField
                fullWidth
                label="Table"
                value={inputs.table || ''}
                onChange={(e) => handleInputChange('table', e.target.value)}
                placeholder="users"
                size="small"
                sx={{ mb: 1 }}
              />
              <TextField
                fullWidth
                label="Column"
                value={inputs.column || ''}
                onChange={(e) => handleInputChange('column', e.target.value)}
                placeholder="password_md5"
                size="small"
                sx={{ mb: 1 }}
              />
              <Box sx={{ display: 'flex', gap: 1 }}>
                <Button variant="outlined" onClick={() => testAdvancedSQLi('union')}>UNION</Button>
                <Button variant="outlined" onClick={() => testAdvancedSQLi('blind')}>Blind</Button>
                <Button variant="outlined" onClick={() => testAdvancedSQLi('time')}>Time-based</Button>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* XSS */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Cross-Site Scripting (XSS)</Typography>
              <TextField
                fullWidth
                label="XSS Payload"
                value={inputs.xss || ''}
                onChange={(e) => handleInputChange('xss', e.target.value)}
                placeholder="<script>alert('XSS')</script>"
                sx={{ mb: 2 }}
              />
              <Typography variant="subtitle2" gutterBottom>Reflected XSS</Typography>
              <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
                <Button variant="outlined" onClick={() => testXSS('reflected', 'easy')}>Easy</Button>
                <Button variant="outlined" onClick={() => testXSS('reflected', 'medium')}>Medium</Button>
                <Button variant="outlined" onClick={() => testXSS('reflected', 'hard')}>Hard</Button>
              </Box>
              <Typography variant="subtitle2" gutterBottom>Stored XSS</Typography>
              <Box sx={{ display: 'flex', gap: 1 }}>
                <Button variant="outlined" onClick={() => testXSS('stored', 'easy')}>Easy</Button>
                <Button variant="outlined" onClick={() => testXSS('stored', 'medium')}>Medium</Button>
                <Button variant="outlined" onClick={() => testXSS('stored', 'hard')}>Hard</Button>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* IDOR */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>IDOR (Insecure Direct Object Reference)</Typography>
              <TextField
                fullWidth
                label="Target User ID"
                value={inputs.userId || ''}
                onChange={(e) => handleInputChange('userId', e.target.value)}
                placeholder="2"
                sx={{ mb: 2 }}
              />
              <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                <Button variant="outlined" onClick={() => testIDOR('profile')}>View Profile</Button>
                <Button variant="outlined" onClick={() => testIDOR('export')}>Export Data</Button>
                <Button variant="outlined" onClick={() => testIDOR('update')}>Update Profile</Button>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Path Traversal */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Path Traversal</Typography>
              <TextField
                fullWidth
                label="File Path"
                value={inputs.filename || ''}
                onChange={(e) => handleInputChange('filename', e.target.value)}
                placeholder="../../../../../../etc/passwd"
                size="small"
                sx={{ mb: 1 }}
              />
              <TextField
                fullWidth
                label="Directory Path"
                value={inputs.listPath || ''}
                onChange={(e) => handleInputChange('listPath', e.target.value)}
                placeholder="../../../../../../etc/"
                size="small"
                sx={{ mb: 2 }}
              />
              <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                <Button variant="outlined" onClick={() => testPathTraversal('read')}>Read File</Button>
                <Button variant="outlined" onClick={() => testPathTraversal('list')}>List Directory</Button>
                <Button variant="outlined" onClick={() => testPathTraversal('download')}>Download</Button>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Results Display */}
      {Object.keys(results).length > 0 && (
        <Box sx={{ mt: 3 }}>
          <Typography variant="h5" gutterBottom>Test Results</Typography>
          {Object.entries(results).map(([key, result]) => (
            <Card key={key} sx={{ mb: 2 }}>
              <CardContent>
                <Typography variant="h6" color={result.success ? 'success.main' : 'error.main'}>
                  {key.replace(/_/g, ' ').toUpperCase()}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Endpoint: {result.endpoint}
                </Typography>
                {result.params && (
                  <Typography variant="body2" color="text.secondary">
                    Parameters: <code>{result.params}</code>
                  </Typography>
                )}
                {result.method && (
                  <Typography variant="body2" color="text.secondary">
                    Method: {result.method}
                  </Typography>
                )}
                <Box sx={{ mt: 1, p: 1, bgcolor: 'grey.100', borderRadius: 1, overflow: 'auto' }}>
                  <pre style={{ margin: 0, fontSize: '0.85em' }}>
                    {result.data || result.error}
                  </pre>
                </Box>
              </CardContent>
            </Card>
          ))}
        </Box>
      )}
    </Box>
  );
}