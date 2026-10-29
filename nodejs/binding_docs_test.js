//本测试由AI生成
// reqrio Node.js binding conformance tests (docs.html based).
// Run from bindings/nodejs: node binding_docs_test.js

const fs = require('fs');
const path = require('path');
const { Session, ALPN, Method, Fingerprint } = require('./index');

const ROOT = path.resolve(__dirname, '..', '..');
const TOKEN = fs.existsSync(path.join(ROOT, 'TOKEN'))
    ? fs.readFileSync(path.join(ROOT, 'TOKEN'), 'utf8').trim()
    : (process.env.REQRIO_TOKEN || '');

const JA3 = '771,4865-4866-4867-49195-49199-49196-49200-52393-52392-49171-49172-156-157-47-53,18-45-5-10-11-16-23-27-0-13-35-65281-43-51-17613,29-23-24,0';

const results = [];

function check(name, fn) {
    try {
        fn();
        results.push([name, 'PASS', '']);
        console.log('PASS', name);
    } catch (e) {
        results.push([name, 'FAIL', `${e.name}: ${e.message}`]);
        console.log('FAIL', name, e.name, e.message);
        console.log(e.stack);
    }
}

function session(alpn = ALPN.HTTP20) {
    const s = new Session(alpn, {}, true, true);
    s.set_timeout({ connect: 10000, read: 10000, write: 10000, handle: 20000, connect_times: 1, handle_times: 1 });
    return s;
}

check('session_get_text', () => {
    const s = session();
    try {
        const r = s.get('https://www.baidu.com', {});
        if (r.status_code() !== 200) throw new Error('status=' + r.status_code());
        const text = r.text();
        if (!text || !(text.toLowerCase().includes('baidu') || text.includes('百度'))) throw new Error('unexpected body');
        r.close();
    } finally { s.close(); }
});

check('query_params', () => {
    const s = session();
    try {
        const r = s.get('https://httpbin.org/get', { params: { q: 'reqrio', page: 1 } });
        const j = r.json();
        if (j.args.q !== 'reqrio') throw new Error(JSON.stringify(j));
        if (String(j.args.page) !== '1') throw new Error(JSON.stringify(j));
        r.close();
    } finally { s.close(); }
});

check('post_json', () => {
    const s = session();
    try {
        const r = s.post('https://httpbin.org/post', { json: { a: 1, b: 'two' } });
        const j = r.json();
        if (j.json.a !== 1) throw new Error(JSON.stringify(j));
        if (!String(j.headers['Content-Type'] || '').includes('application/json')) throw new Error(JSON.stringify(j.headers));
        r.close();
    } finally { s.close(); }
});

check('post_form', () => {
    const s = session();
    try {
        const r = s.post('https://httpbin.org/post', { data: { a: '1', b: 'x y' } });
        const j = r.json();
        if (j.form.a !== '1') throw new Error(JSON.stringify(j.form));
        r.close();
    } finally { s.close(); }
});

check('post_bytes', () => {
    const s = session();
    try {
        const r = s.post('https://httpbin.org/post', { bytes: Buffer.from([0, 1, 65]), ct: 'application/octet-stream' });
        if (r.status_code() !== 200) throw new Error('status=' + r.status_code());
        r.close();
    } finally { s.close(); }
});

check('send_recv', () => {
    const s = session();
    try {
        const sid = s.send(Method.GET, 'https://httpbin.org/get?x=1', {});
        const r = s.recv(sid, false);
        if (r.status_code() !== 200) throw new Error('status=' + r.status_code());
        if (r.json().args.x !== '1') throw new Error(r.text());
        r.close();
    } finally { s.close(); }
});

check('cookies_headers', () => {
    const s = session();
    s.set_auto_redirect(false)
    try {
        let r = s.get('https://httpbin.org/cookies/set?k=v', {});
        const cks = r.cookies();
        if (!cks.some(c => c.name === 'k' && c.value === 'v')) throw new Error(JSON.stringify(cks));
        r.close();
        r = s.get('https://httpbin.org/cookies', {});
        if (r.json().cookies.k !== 'v') throw new Error(r.text());
        r.close();
    } finally { s.close(); }
});

if (!process.env.SKIP_STREAM) {
check('stream_chunks', () => {
    const s = session(ALPN.HTTP11);
    try {
        const r = s.get('https://httpbin.org/bytes/16384', { stream: true });
        let total = 0;
        for (const chunk of r.chunks()) total += chunk.length;
        if (total !== 16384) throw new Error('total=' + total);
        r.close();
    } finally { s.close(); }
});
}

check('fingerprint_ja3_random', () => {
    for (const fp of [
        Fingerprint.fromJa3(JA3, TOKEN, session().library),
        Fingerprint.fromRandom(TOKEN, session().library),
    ]) {
        const s = session();
        try {
            s.set_fingerprint(fp);
            const r = s.get('https://m.so.com', {});
            if (r.status_code() !== 200) throw new Error('status=' + r.status_code());
            r.close();
        } finally { s.close(); }
    }
});

check('fingerprint_builtin', () => {
    const s = session();
    try {
        const fp = Fingerprint.fromFingerBuiltin('win-x64-chrome-152', TOKEN, false, s.library);
        s.set_fingerprint(fp);
        const r = s.get('https://m.so.com', {});
        if (r.status_code() !== 200) throw new Error('status=' + r.status_code());
        r.close();
    } finally { s.close(); }
});

check('http3', () => {
    const s = session(ALPN.HTTP30);
    try {
        const r = s.get('https://www.bing.com', {});
        if (r.status_code() !== 200) throw new Error('status=' + r.status_code());
        r.close();
    } finally { s.close(); }
});

const failed = results.filter(r => r[1] === 'FAIL');
console.log('\n=== summary ===');
for (const [n, st, msg] of results) console.log(st, n, msg);
console.log(`total=${results.length} pass=${results.length - failed.length} fail=${failed.length}`);
process.exit(failed.length ? 1 : 0);
