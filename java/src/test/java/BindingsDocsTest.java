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
