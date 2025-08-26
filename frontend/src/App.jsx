import React, { useState, useEffect } from 'react'

const api = (path, opts={}) =>
  fetch(`/api${path}`, opts).then(r => (r.headers.get('content-type')||'').includes('text/html') ? r.text() : r.json())

const LevelPicker = ({level, setLevel}) => (
  <div style={{marginBottom:8}}>
    Level: {['easy','medium','hard'].map(l =>
      <label key={l} style={{marginRight:12}}>
        <input type="radio" name="lvl" checked={level===l} onChange={()=>setLevel(l)} /> {l}
      </label>
    )}
  </div>
)

function SQLiLab(){
  const [q,setQ] = useState("")
  const [level,setLevel] = useState("easy")
  const [rows,setRows] = useState([])
  const run = async () => {
    const data = await api(`/labs/sqli/${level}?q=${encodeURIComponent(q)}`)
    setRows(data)
  }
  return (
    <section>
      <h2>SQL Injection</h2>
      <LevelPicker level={level} setLevel={setLevel}/>
      <input placeholder="search term or payload" value={q} onChange={e=>setQ(e.target.value)} style={{width:320}}/>
      <button onClick={run}>Run</button>
      <pre>{JSON.stringify(rows,null,2)}</pre>
      {level==='hard' && <p>Tip: try manipulating <code>sort</code> param: <code>?q=a&sort=id desc;--</code></p>}
    </section>
  )
}

function XSSReflected(){
  const [q,setQ] = useState("")
  const [level,setLevel] = useState("easy")
  const [html,setHtml] = useState("")
  const run = async () => {
    const res = await api(`/labs/xss/reflected/${level}?q=${encodeURIComponent(q)}`)
    setHtml(res)
  }
  return (
    <section>
      <h2>XSS - Reflected</h2>
      <LevelPicker level={level} setLevel={setLevel}/>
      <input placeholder='payload (e.g. <img src=x onerror=alert(1)>)'
             value={q} onChange={e=>setQ(e.target.value)} style={{width:420}}/>
      <button onClick={run}>Send</button>
      <div style={{border:'1px solid #ccc', padding:10, marginTop:10}}
           dangerouslySetInnerHTML={{__html: html}}/>
    </section>
  )
}

function XSSStored(){
  const [level,setLevel] = useState("easy")
  const [content,setContent] = useState("")
  const [posts,setPosts] = useState([])
  const post = async () => {
    await api(`/labs/xss/stored/${level}/post`, {
      method:'POST',
      headers:{'Content-Type':'application/json'},
      body: JSON.stringify({content})
    })
    list()
  }
  const list = async () => {
    const data = await api(`/labs/xss/stored/${level}/list`)
    setPosts(data)
  }
  useEffect(()=>{list()},[level])
  return (
    <section>
      <h2>XSS - Stored</h2>
      <LevelPicker level={level} setLevel={setLevel}/>
      <textarea placeholder='HTML content (dangerously rendered)'
                value={content} onChange={e=>setContent(e.target.value)} style={{width:520, height:80}}/>
      <div><button onClick={post}>Post</button></div>
      <div style={{marginTop:10}}>
        {posts.map(p =>
          <div key={p.id} style={{border:'1px solid #ddd',padding:8, marginBottom:6}}>
            <div dangerouslySetInnerHTML={{__html: p.content}}/>
            <small>{p.createdAt}</small>
          </div>
        )}
      </div>
    </section>
  )
}

function BAC(){
  const [level,setLevel] = useState("easy")
  const [data,setData] = useState(null)
  const run = async () => {
    const url = level==='easy' ? '/labs/bac/easy/admin-users'
              : level==='medium' ? '/labs/bac/medium/admin-users'
              : '/labs/bac/hard/admin-users'
    const res = await api(url, {
      headers: level==='medium' ? {'X-Admin':'true'} : {}
    })
    setData(res)
  }
  return (
    <section>
      <h2>Broken Access Control</h2>
      <LevelPicker level={level} setLevel={setLevel}/>
      <button onClick={run}>Fetch admin users</button>
      <pre>{JSON.stringify(data,null,2)}</pre>
    </section>
  )
}

function SSRF(){
  const [level,setLevel] = useState("easy")
  const [url,setUrl] = useState("http://example.org")
  const [resp,setResp] = useState("")
  const run = async () => {
    const res = await api(`/labs/ssrf/${level}?url=${encodeURIComponent(url)}`)
    setResp(typeof res === 'string' ? res : JSON.stringify(res))
  }
  return (
    <section>
      <h2>SSRF</h2>
      <LevelPicker level={level} setLevel={setLevel}/>
      <input style={{width:380}} value={url} onChange={e=>setUrl(e.target.value)}/>
      <button onClick={run}>Fetch</button>
      <pre style={{whiteSpace:'pre-wrap'}}>{resp}</pre>
    </section>
  )
}

export default function App(){
  const [tab,setTab] = useState('sqli')
  const tabs = [
    ['sqli','SQLi'],
    ['xssr','XSS Reflected'],
    ['xsss','XSS Stored'],
    ['bac','Broken Access Control'],
    ['ssrf','SSRF']
  ]
  return (
    <div style={{fontFamily:'system-ui', padding:20}}>
      <h1>Secure University – OWASP Labs</h1>
      <nav style={{marginBottom:12}}>
        {tabs.map(([k,t])=>
          <button key={k} onClick={()=>setTab(k)} style={{marginRight:8, padding:'6px 10px', fontWeight: tab===k?'700':'400'}}>{t}</button>
        )}
      </nav>
      {tab==='sqli' && <SQLiLab/>}
      {tab==='xssr' && <XSSReflected/>}
      {tab==='xsss' && <XSSStored/>}
      {tab==='bac' && <BAC/>}
      {tab==='ssrf' && <SSRF/>}
      <hr/>
      <p>Backend & API are proxied at <code>/api</code>. This is a training app. Do NOT expose publicly.</p>
    </div>
  )
}
