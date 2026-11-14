#include <QFile>
#include <QRandomGenerator>
#include <QString>
#include <iostream>
#include <mutex>
#include <thread>
#include <vector>

#include "Response.h"
#include "Session.h"
#include "Timeout.h"
#include "example/http_example.h"



static void test_large_body(const Session *session) {
    const Response resp = session->get("https://httpbin.reqrio.com/bytes/65536");
    assert(resp.statusCode()==200);
    assert(resp.bytes().size()==65536);
}

/// 超时测试
static void test_timeout() {
    const auto *session = new Session(HTTP11);
    session->setTimeout(Timeout(3000, 3000, 3000, 3000, 1, 1));

    qint64 start = QDateTime::currentMSecsSinceEpoch();
    bool failed = false;
    try {
        auto resp = session->get("https://192.0.2.1/");
    } catch (const std::exception &) {
        failed = true;
    }
    assert(failed);
    qint64 elapsed = QDateTime::currentMSecsSinceEpoch() - start;
    assert(elapsed >= 3000 && elapsed < 6000);

    start = QDateTime::currentMSecsSinceEpoch();
    failed = false;
    const uint64_t sid = session->send(GET, "https://httpbin.reqrio.com/delay/10");
    try {
        auto resp = session->recv(sid);
    } catch (const std::exception &) {
        failed = true;
    }
    assert(failed);
    elapsed = QDateTime::currentMSecsSinceEpoch() - start;
    assert(elapsed >= 3000);
    delete session;
}

// ==========================>[Runner]<=============================

static std::mutex g_io_mutex;
static std::vector<std::thread> g_threads;


template<typename Fn>
static void run(const QString &name, Fn fn) {
    g_threads.emplace_back([name, fn] {
        try {
            fn();
            std::lock_guard lock(g_io_mutex);
            std::cout << "PASS " << name.toStdString() << std::endl;
        } catch (const std::exception &e) {
            std::lock_guard lock(g_io_mutex);
            std::cout << "FAIL " << name.toStdString() << ": " << e.what() << std::endl;
        }
    });
}

template<typename Fn>
static void run_both(Fn fn) {
    fn(new Session(HTTP11), HTTP11);
    fn(new Session(HTTP20), HTTP20);
}

int main(int argc, char *argv[]) {
    Q_UNUSED(argc)
    Q_UNUSED(argv)
    run("test_get", [] { run_both([](Session *s, const ALPN &alpn) { http_get_matrix(s, alpn); }); });
    run("test_method", [] { run_both([](const Session *s, const ALPN &) { http_method(s); }); });
    run("test_status", [] { run_both([](const Session *s, const ALPN &) { http_status(s); }); });
    run("test_auth", [] { run_both([](const Session *s, const ALPN &) { http_auth(s); }); });
    run("test_query_param", [] { run_both([](const Session *s, const ALPN &) { query_param(s); }); });
    run("test_redirect", [] { run_both([](const Session *s, const ALPN &) { http_redirect(s); }); });
    run("test_cookie", [] { run_both([](const Session *s, const ALPN &) { http_cookie(s); }); });
    run("test_headers", [] { run_both([](const Session *s, const ALPN &) { http_header(s); }); });
    run("test_stream_req", [] { run_both([](const Session *s, const ALPN &) { http_stream(s); }); });
    run("test_body", [] { run_both([](const Session *s, const ALPN &) { http_body(s); }); });
    run("test_concurrency", [] { run_both([](const Session *s, const ALPN &) { http_concurrency(s); }); });
    run("test_large_body", [] { run_both([](const Session *s, const ALPN &) { test_large_body(s); }); });
    run("test_timeout", [] { test_timeout(); });

    for (auto &thread: g_threads) {
        if (thread.joinable()) thread.join();
    }
    return 0;
}
