const {Session, Method, ALPN} = require("./index");
const {
    http_get_matrix,
    http_method,
    http_status,
    http_auth,
    query_param,
    http_redirect,
    http_cookie,
    http_header,
    http_stream,
    http_body,
    http_concurrency,
    assert,
    assertEq
} = require("./http_example")

/**
 * @param session {Session}
 **/
function test_large_body(session) {
    const resp = session.get("https://httpbin.reqrio.com/bytes/65536");
    assertEq(resp.status_code(), 200, "large status");
    assertEq(resp.bytes().length, 65536, "large length");
    resp.close();
}

function test_timeout() {
    const session = new Session(ALPN.HTTP11);
    session.set_timeout({connect: 3000, read: 3000, write: 3000, handle: 3000, connect_times: 1, handle_times: 1});
    try {
        let start = Date.now();
        let failed = false;
        try {
            session.get("https://192.0.2.1/");
        } catch (e) {
            failed = true;
        }
        assert(failed, "192.0.2.1 should timeout");
        let elapsed = Date.now() - start;
        assert(elapsed >= 3000 && elapsed < 6000, `elapsed = ${elapsed}`);

        start = Date.now();
        failed = false;
        const sid = session.send(Method.GET, "https://httpbin.reqrio.com/delay/10", {});
        try {
            session.recv(sid, false);
        } catch (e) {
            failed = true;
        }
        assert(failed, "delay/10 should timeout");
        elapsed = Date.now() - start;
        assert(elapsed >= 3000, `elapsed = ${elapsed}`);
    } finally {
        session.close();
    }
}

// ==========================>[Runner]<=============================


function both(fn) {
    fn(new Session(ALPN.HTTP11), ALPN.HTTP11)
    fn(new Session(ALPN.HTTP20), ALPN.HTTP20)
}

const CASES = [
    ["test_get", () => both(http_get_matrix)],
    ["test_method", () => both(http_method)],
    ["test_status", () => both(http_status)],
    ["test_auth", () => both(http_auth)],
    ["test_query_param", () => both(query_param)],
    ["test_redirect", () => both(http_redirect)],
    ["test_cookie", () => both(http_cookie)],
    ["test_headers", () => both(http_header)],
    ["test_stream_req", () => both(http_stream)],
    ["test_body", () => both(http_body)],
    ["test_concurrency", () => both(http_concurrency)],
    ["test_large_body", () => both(test_large_body)],
    ["test_timeout", () => test_timeout()],
];

const CASE_ENV = "HTTP_TEST_CASE";

function runCase(name) {
    const entry = CASES.find(([n]) => n === name);
    if (!entry) {
        console.log(`FAIL ${name}: unknown case`);
        return 1;
    }
    try {
        entry[1]();
        console.log(`PASS ${name}`);
        return 0;
    } catch (e) {
        console.log(`FAIL ${name}: ${e.message}`);
        if (e.stack) console.log(e.stack.split("\n").slice(0, 4).join("\n"));
        return 1;
    }
}

/** 并发执行：每个用例一个子进程，最多 jobs 个同时运行。 */
function mainParallel() {
    const {fork} = require("child_process");
    const jobs = Math.max(1, Number(process.env.HTTP_TEST_JOBS || 4));
    const queue = CASES.map(([name]) => name);
    const results = [];
    let running = 0;
    let finished = 0;

    return new Promise((resolve) => {
        const next = () => {
            while (running < jobs && queue.length > 0) {
                const name = queue.shift();
                running++;
                const child = fork(__filename, [], {
                    env: {...process.env, [CASE_ENV]: name},
                    stdio: "inherit",
                });
                child.on("exit", (code) => {
                    running--;
                    results.push([name, code === 0]);
                    finished++;
                    if (finished === CASES.length) {
                        const failed = results.filter(([, ok]) => !ok).length;
                        console.log(`\n${CASES.length - failed}/${CASES.length} passed (jobs=${jobs})`);
                        resolve(failed === 0 ? 0 : 1);
                    } else {
                        next();
                    }
                });
            }
        };
        next();
    });
}

if (require.main === module) {
    const only = process.env[CASE_ENV];
    if (only) {
        process.exit(runCase(only));
    }
    if (process.argv.includes("--serial")) {
        let failed = 0;
        for (const [name] of CASES) failed += runCase(name);
        process.exit(failed === 0 ? 0 : 1);
    }
    mainParallel().then((code) => process.exit(code));
}
