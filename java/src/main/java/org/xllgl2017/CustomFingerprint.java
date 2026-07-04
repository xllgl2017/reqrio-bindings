package org.xllgl2017;

import com.google.gson.*;

import java.util.*;

public class CustomFingerprint {
    List<Integer> suites = new ArrayList<>();
    Map<String, JsonElement> extensions = new LinkedHashMap<>();
    HashMap<String, JsonElement> settings = new HashMap<>();
    int window_size;
    int weight;
    boolean priority;


    public void addSuite(CipherSuite suite) {
        this.addSuite(suite.value);
    }

    public void addSuite(int suite) {
        this.suites.add(suite);
    }

    public void addSupportedGroup(SupportGroup group) {
        this.addSupportedGroup(group.value);
    }

    public void addSupportedGroup(int group) {
        String key = String.valueOf(ExtensionType.SupportedGroup.value);
        JsonArray values;
        try {
            values = this.extensions.getOrDefault(key, new JsonArray()).getAsJsonArray();
        } catch (NullPointerException e) {
            values = new JsonArray();
        }
        values.add(group);
        this.extensions.put(key, values);
    }

    public void addAlgorithm(Algorithm algorithm) {
        this.addAlgorithm(algorithm.value);
    }

    public void addAlgorithm(int algorithm) {
        String key = String.valueOf(ExtensionType.SignatureAlgorithms.value);
        JsonArray values;
        try {
            values = this.extensions.getOrDefault(key, new JsonArray()).getAsJsonArray();
        } catch (NullPointerException e) {
            values = new JsonArray();
        }
        values.add(algorithm);
        this.extensions.put(key, values);
    }

    public void addSupportedVersion(Version version) {
        this.addSupportedVersion(version.value);
    }

    public void addSupportedVersion(int version) {
        String key = String.valueOf(ExtensionType.SupportedVersions.value);
        JsonArray values;
        try {
            values = this.extensions.getOrDefault(key, new JsonArray()).getAsJsonArray();
        } catch (NullPointerException e) {
            values = new JsonArray();
        }
        values.add(version);
        this.extensions.put(key, values);
    }

    public void addKeyShare(SupportGroup group) {
        this.addKeyShare(group.value);
    }

    public void addKeyShare(int group) {
        String key = String.valueOf(ExtensionType.KeyShare.value);
        JsonArray values;
        try {
            values = this.extensions.getOrDefault(key, new JsonArray()).getAsJsonArray();
        } catch (NullPointerException e) {
            values = new JsonArray();
        }
        values.add(group);
        this.extensions.put(key, values);
    }

    public void addEcPointFormat(EcPointFormat format) {
        this.addEcPointFormat(format.value);
    }

    public void addEcPointFormat(int format) {
        String key = String.valueOf(ExtensionType.EcPointFormats.value);
        JsonArray values;
        try {
            values = this.extensions.getOrDefault(key, new JsonArray()).getAsJsonArray();
        } catch (NullPointerException e) {
            values = new JsonArray();
        }
        values.add(format);
        this.extensions.put(key, values);
    }

    public void addCompressionCertificate(CompressionMethod method) {
        this.addCompressionCertificate(method.value);
    }

    public void addCompressionCertificate(int method) {
        String key = String.valueOf(ExtensionType.CompressionCertificate.value);
        JsonArray values;
        try {
            values = this.extensions.getOrDefault(key, new JsonArray()).getAsJsonArray();
        } catch (NullPointerException e) {
            values = new JsonArray();
        }
        values.add(method);
        this.extensions.put(key, values);
    }

    public void addApplicationLayerProtocolNegotiation(ALPN alpn) {
        this.addApplicationLayerProtocolNegotiation(alpn.getValue());
    }

    public void addApplicationLayerProtocolNegotiation(String alpn) {
        String key = String.valueOf(ExtensionType.ApplicationLayerProtocolNegotiation.value);
        JsonArray values;
        try {
            values = this.extensions.getOrDefault(key, new JsonArray()).getAsJsonArray();
        } catch (NullPointerException e) {
            values = new JsonArray();
        }
        values.add(alpn);
        this.extensions.put(key, values);
    }

    public void addApplicationSetting(ALPN alpn) {
        this.addApplicationSetting(alpn.getValue());
    }

    public void addApplicationSetting(String alpn) {
        String key = String.valueOf(ExtensionType.ApplicationSetting.value);
        JsonArray values;
        try {
            values = this.extensions.getOrDefault(key, new JsonArray()).getAsJsonArray();
        } catch (NullPointerException e) {
            values = new JsonArray();
        }
        values.add(alpn);
        this.extensions.put(key, values);
    }

    public void addApplicationSettingOld(ALPN alpn) {
        this.addApplicationSettingOld(alpn.getValue());
    }

    public void addApplicationSettingOld(String alpn) {
        String key = String.valueOf(ExtensionType.ApplicationSettingOld.value);
        JsonArray values;
        try {
            values = this.extensions.getOrDefault(key, new JsonArray()).getAsJsonArray();
        } catch (NullPointerException e) {
            values = new JsonArray();
        }
        values.add(alpn);
        this.extensions.put(key, values);
    }

    public void setPskMode(int mode) {
        String key = String.valueOf(ExtensionType.PskKeyExchangeMode.value);
        if (this.extensions.containsKey(key)) {
            this.extensions.replace(key, new JsonPrimitive(mode));
        } else {
            this.extensions.put(key, new JsonPrimitive(mode));
        }


    }

    public void addExtension(ExtensionType extensionType) {
        this.addExtension(extensionType.value);
    }

    public void addExtension(ExtensionType typ, JsonElement value) {
        this.addExtension(typ.value, value);
    }

    public void addExtension(int value) {
        String key = String.valueOf(value);
        if (this.extensions.containsKey(key)) return;
        this.extensions.put(key, null);
    }

    public void addExtension(int typ, JsonElement value) {
        String key = String.valueOf(typ);
        if (this.extensions.containsKey(key)) return;
        this.extensions.put(key, value);
    }

    public void addSetting(H2SettingType type, int value) {
        this.settings.put(type.value, new JsonPrimitive(value));
    }

    public void addSetting(int flag, int value) {
        JsonObject obj = new JsonObject();
        obj.add("flag", new JsonPrimitive(flag));
        obj.add("value", new JsonPrimitive(value));
        this.settings.put(H2SettingType.Reversed.value, obj);
    }

    public void setWindowSize(int size) {
        this.window_size = size;
    }

    public void setPriority(boolean priority, int weight) {
        this.priority = priority;
        this.weight = weight;
    }
}


