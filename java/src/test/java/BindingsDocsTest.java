/**
 * 本测试由AI生成
 **/

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.xllgl2017.*;
import org.xllgl2017.tls.ALPN;
import org.xllgl2017.tls.CipherSuite;
import org.xllgl2017.tls.Version;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** reqrio Java binding conformance tests (docs.html based). */
public class BindingsDocsTest {
    static String token() {
        Path p = Path.of("../../TOKEN");
        try {
            return Files.isRegularFile(p) ? Files.readString(p).trim() : System.getenv("REQRIO_TOKEN");
        } catch (Exception e) {
            return System.getenv("REQRIO_TOKEN");
        }
    }

    static class Result {
        final String name;
        final boolean ok;
        final String msg;

        Result(String name, boolean ok, String msg) {
            this.name = name;
            this.ok = ok;
            this.msg = msg;
        }

        boolean ok() {
            return ok;
        }

        String name() {
            return name;
        }

        String msg() {
            return msg;
        }
    }

    static final List<Result> RESULTS = new ArrayList<>();

    interface Case {
        void run() throws Exception;
    }

    static void check(String name, Case c) {
        try {
            c.run();
            RESULTS.add(new Result(name, true, ""));
            System.out.println("PASS " + name);
        } catch (Throwable e) {
            RESULTS.add(new Result(name, false, e.getClass().getSimpleName() + ": " + e.getMessage()));
            System.out.println("FAIL " + name + ": " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace(System.out);
        }
    }

    public static void main(String[] args) {
        check("session_get", () -> {
            try (Session s = new Session(ALPN.HTTP20)) {
                Response r = s.get("https://www.baidu.com");
                if (r.statusCode() != 200) throw new AssertionError("status=" + r.statusCode());
                if (r.bytes().length == 0) throw new AssertionError("empty body");
            }
        });

        check("query_params", () -> {
            try (Session s = new Session(ALPN.HTTP20)) {
                Url url = new Url("https://httpbin.org/get");
                url.addParam("q", "reqrio");
                url.addParam("page", "1");
                Response r = s.get(url);
                JsonObject j = r.json().getAsJsonObject();
                if (!"reqrio".equals(j.getAsJsonObject("args").get("q").getAsString()))
                    throw new AssertionError(j.toString());
            }
        });

        check("post_json", () -> {
            try (Session s = new Session(ALPN.HTTP20)) {
                JsonObject obj = new JsonObject();
                obj.add("a", new JsonPrimitive(1));
                obj.add("b", new JsonPrimitive("two"));
                Response r = s.post("https://httpbin.org/post", new Body(obj));
                JsonObject j = r.json().getAsJsonObject();
                if (j.getAsJsonObject("json").get("a").getAsInt() != 1)
                    throw new AssertionError(j.toString());
            }
        });

        check("post_form", () -> {
            try (Session s = new Session(ALPN.HTTP20)) {
                HashMap<String, String> form = new HashMap<>();
                form.put("a", "1");
                form.put("b", "x y");
                Response r = s.post("https://httpbin.org/post", new Body(form));
                JsonObject j = r.json().getAsJsonObject();
                String a = j.getAsJsonObject("form").get("a").getAsString();
                if (!"1".equals(a)) throw new AssertionError(j.toString());
            }
        });

        check("post_bytes_text", () -> {
            try (Session s = new Session(ALPN.HTTP20)) {
                Response r = s.post("https://httpbin.org/post", new Body(new byte[]{0, 1, 65}, "application/octet-stream"));
                if (r.statusCode() != 200) throw new AssertionError("status=" + r.statusCode());
                r = s.post("https://httpbin.org/post", new Body("plain-text"));
                JsonObject j = r.json().getAsJsonObject();
                if (!"plain-text".equals(j.get("data").getAsString()))
                    throw new AssertionError(j.toString());
            }
        });

        check("response_headers_cookies", () -> {
            try (Session s = new Session(ALPN.HTTP20)) {
                s.setAutoRedirect(false);
                Response r = s.get("https://httpbin.org/cookies/set?k=v");
                boolean found = r.getCookies().stream().anyMatch(c -> "k".equals(c.getName()) && "v".equals(c.getValue()));
                if (!found) throw new AssertionError("cookie k=v missing: " + r.getCookies());
                r = s.get("https://httpbin.org/cookies");
                JsonObject j = r.json().getAsJsonObject();
                if (!"v".equals(j.getAsJsonObject("cookies").get("k").getAsString()))
                    throw new AssertionError(j.toString());
            }
        });

        check("fingerprint_ja3", () -> {
            String ja3 = "771,4865-4866-4867-49195-49199-49196-49200-52393-52392-49171-49172-156-157-47-53,18-45-5-10-11-16-23-27-0-13-35-65281-43-51-17613,29-23-24,0";
            Fingerprint fp = Fingerprint.fromJa3(ja3, token());
            try (Session s = new Session(ALPN.HTTP20)) {
                s.setFingerprint(fp);
                Response r = s.get("https://m.so.com");
                if (r.statusCode() != 200) throw new AssertionError("status=" + r.statusCode());
            }
        });

        check("fingerprint_builtin", () -> {
            Fingerprint fp = Fingerprint.fromBuiltin("win-x64-chrome-152", false, token());
            try (Session s = new Session(ALPN.HTTP20)) {
                s.setFingerprint(fp);
                Response r = s.get("https://m.so.com");
                if (r.statusCode() != 200) throw new AssertionError("status=" + r.statusCode());
            }
        });

        check("tlcp", () -> {
            Fingerprint fp = new Fingerprint(Version.TLCP, Version.TLCP, token());
            fp.addSuite(CipherSuite.ECC_SM4_CBC_SM3);
            fp.addExtension(0xFAFA);
            fp.addExtension(0x0000);
            fp.addExtension(0x8A8A, new byte[]{0});
            try (Session s = new Session(ALPN.HTTP11)) {
                s.setFingerprint(fp);
                Response r = s.get("https://test.gmssl.cn");
                if (r.statusCode() != 200) throw new AssertionError("status=" + r.statusCode());
            }
        });

        long failed = RESULTS.stream().filter(r -> !r.ok()).count();
        System.out.println("\n=== summary ===");
        for (Result r : RESULTS) System.out.printf("%s %s %s%n", r.ok() ? "PASS" : "FAIL", r.name(), r.msg());
        System.out.printf("total=%d pass=%d fail=%d%n", RESULTS.size(), RESULTS.size() - failed, failed);
        System.exit(failed == 0 ? 0 : 1);
    }
}
