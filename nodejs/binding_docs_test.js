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
