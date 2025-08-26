
## `owasp-labs/VULNERABILITIES.md`
```md
# Vulnerability Catalog

Each item has **three levels**. “Hard” is still vulnerable but less obvious.

| OWASP | Area | Endpoints / UI | Easy | Medium | Hard |
|---|---|---|---|---|---|
| A01 | Broken Access Control | `/api/labs/bac/*` (UI “BAC”) | No checks at all | Spoofable `X-Admin:true` header | Client-controlled role parameter |
| A02 | Cryptographic Failures | `/api/labs/crypto/{level}/register` | MD5 | SHA-1 | Fake “bcrypt-like” with static salt and weak rounds |
| A03 | Injection – SQLi | `/api/labs/sqli/{level}` (UI “SQLi”) | Full string concat | Naive quote escaping | ORDER BY injection while WHERE uses prepared stmt |
| A03 | Injection – XSS (Reflected) | `/api/labs/xss/reflected/{level}` (UI “XSS Reflected”) | Raw echo HTML | Strip `<script>` only | Encode `<`/`>` only, attributes & URL handlers survive |
| A03 | Injection – XSS (Stored) | `/api/labs/xss/stored/{level}/post,list` (UI “XSS Stored”) | Store & render as-is | Remove `<script>` tags only | Partially mangle handlers; svg/on* survive |
| A04 | Insecure Design | `/api/labs/design/*` | Static reset code `000000` | Random code but reusable/no expiry | Random code but leaked in response |
| A05 | Security Misconfiguration | `/api/labs/misconfig/{level}/boom` | Full stacktrace & messages (also globally enabled) | Verbose messages | Stacktrace still enabled via server config |
| A06 | Vulnerable/Outdated Components | `/api/labs/components/{level}/substitute` | commons-text 1.9 variable expansion | Same, more contexts | Also expands system/env properties |
| A07 | Identification & Auth Failures | `/api/labs/auth/{level}/rememberme` | Base64(user:pass) | user:MD5(pass) | Token with static MAC/signature |
| A08 | Software & Data Integrity | `/api/labs/integrity/{level}/deserialize` | Java deserialization raw | Weak “allowlist” | Fixed-signature “signed” blobs |
| A09 | Logging/Monitoring | `/api/labs/logging/{level}/login` + `/audit` | No logs | Minimal logs | Leaks hashed password & sensitive info |
| A10 | SSRF | `/api/labs/ssrf/{level}` (UI “SSRF”) | Open fetch | Naive blacklist `localhost` | Scheme-only allow; IPv6/decimal/redirect bypasses |

**Extra labs:** Command Injection (`/api/labs/cmd/{level}/ping`), unrestricted file upload (`/api/files/upload`), classic app flows (courses/grades/forum).
