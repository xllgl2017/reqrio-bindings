import org.xllgl2017.*;
import org.xllgl2017.tls.ALPN;


public class HttpTest {


    // ==========================>[Test cases]<=============================

    /**
     * 大 body
     */
    static void testLargeBody(Session s) throws Exception {
        Response resp = s.get("https://httpbin.reqrio.com/bytes/65536");
        HttpExample.assertEq(resp.statusCode(), 200, "large status");
        HttpExample.assertEq(resp.bytes().length, 65536, "large length");
    }

    /**
     * 超时测试
     */
    static void testTimeout() throws Exception {
        try (Session s = new Session(ALPN.HTTP11)) {
            s.setTimeout(new Timeout(3000, 3000, 3000, 3000, 1, 1));

            long start = System.currentTimeMillis();
            boolean failed = false;
            try {
                s.get("https://192.0.2.1/");
            } catch (Exception e) {
                failed = true;
            }
            HttpExample.assertTrue(failed, "connect 192.0.2.1 should timeout");
            long elapsed = System.currentTimeMillis() - start;
            HttpExample.assertTrue(elapsed >= 3000 && elapsed < 6000, "elapsed = " + elapsed);

            start = System.currentTimeMillis();
            int sid = s.send(Method.GET, new Url("https://httpbin.reqrio.com/delay/10"), new Body());
            failed = false;
            try {
                s.recv(sid);
            } catch (Exception e) {
                failed = true;
            }
            HttpExample.assertTrue(failed, "delay/10 should timeout");
            elapsed = System.currentTimeMillis() - start;
            HttpExample.assertTrue(elapsed >= 3000, "elapsed = " + elapsed);
        }
    }

    // ==========================>[Runner]<=============================

    interface Case {
        void run() throws Exception;
    }

    // 用例并发执行：reqrio 的 ScReq 不是线程安全的，每个用例使用独立的 Session。
    static final java.util.concurrent.ExecutorService POOL =
            java.util.concurrent.Executors.newFixedThreadPool(8);
    static final java.util.concurrent.atomic.AtomicInteger FAILED =
            new java.util.concurrent.atomic.AtomicInteger();

    static void run(String name, Case c) {
        POOL.submit(() -> {
            try {
                c.run();
                synchronized (System.out) {
                    System.out.println("PASS " + name);
                }
            } catch (Throwable e) {
                FAILED.incrementAndGet();
                synchronized (System.out) {
                    System.out.println("FAIL " + name + ": " + e.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }
        });
    }

    public static void main(String[] args) {
        run("test_get", () -> {
            try (Session session = new Session(ALPN.HTTP11)) {
                HttpExample.httpGetMatrix(session, ALPN.HTTP11);
            }
            try (Session session = new Session(ALPN.HTTP20)) {
                HttpExample.httpGetMatrix(session, ALPN.HTTP20);
            }
        });
        run("test_method", () -> {
            try (Session session = new Session(ALPN.HTTP11)) {
                HttpExample.httpMethod(session);
            }
            try (Session session = new Session(ALPN.HTTP20)) {
                HttpExample.httpMethod(session);
            }
        });
        run("test_status", () -> {
            try (Session session = new Session(ALPN.HTTP11)) {
                HttpExample.httpStatus(session);
            }
            try (Session session = new Session(ALPN.HTTP20)) {
                HttpExample.httpStatus(session);
            }
        });
        run("test_auth", () -> {
            try (Session session = new Session(ALPN.HTTP11)) {
                HttpExample.httpAuth(session);
            }
            try (Session session = new Session(ALPN.HTTP20)) {
                HttpExample.httpAuth(session);
            }
        });
        run("test_query_param", () -> {
            try (Session session = new Session(ALPN.HTTP11)) {
                HttpExample.queryParam(session);
            }
            try (Session session = new Session(ALPN.HTTP20)) {
                HttpExample.queryParam(session);
            }
        });
        run("test_redirect", () -> {
            try (Session session = new Session(ALPN.HTTP11)) {
                HttpExample.httpRedirect(session);
            }
            try (Session session = new Session(ALPN.HTTP20)) {
                HttpExample.httpRedirect(session);
            }
        });
        run("test_cookie", () -> {
            try (Session session = new Session(ALPN.HTTP11)) {
                HttpExample.httpCookie(session);
            }
            try (Session session = new Session(ALPN.HTTP20)) {
                HttpExample.httpCookie(session);
            }
        });
        run("test_headers", () -> {
            try (Session session = new Session(ALPN.HTTP11)) {
                HttpExample.httpHeader(session);
            }
            try (Session session = new Session(ALPN.HTTP20)) {
                HttpExample.httpHeader(session);
            }
        });
        run("test_stream_req", () -> {
            try (Session session = new Session(ALPN.HTTP11)) {
                HttpExample.httpStream(session);
            }
            try (Session session = new Session(ALPN.HTTP20)) {
                HttpExample.httpStream(session);
            }
        });
        run("test_body", () -> {
            try (Session session = new Session(ALPN.HTTP11)) {
                HttpExample.httpBody(session);
            }
            try (Session session = new Session(ALPN.HTTP20)) {
                HttpExample.httpBody(session);
            }
        });
        run("test_concurrency", () -> {
            try (Session session = new Session(ALPN.HTTP11)) {
                HttpExample.httpConcurrency(session);
            }
            try (Session session = new Session(ALPN.HTTP20)) {
                HttpExample.httpConcurrency(session);
            }
        });
        run("test_large_body", () -> {
            try (Session session = new Session(ALPN.HTTP11)) {
                testLargeBody(session);
            }
            try (Session session = new Session(ALPN.HTTP20)) {
                testLargeBody(session);
            }
        });
        run("test_timeout", HttpTest::testTimeout);

        POOL.shutdown();
        try {
            if (!POOL.awaitTermination(30, java.util.concurrent.TimeUnit.MINUTES)) {
                POOL.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println(FAILED.get() == 0 ? "ALL PASS" : (FAILED.get() + " case(s) FAILED"));
    }
}
