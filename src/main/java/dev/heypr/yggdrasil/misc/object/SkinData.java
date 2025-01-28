package dev.heypr.yggdrasil.misc.object;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public final class SkinData implements ConfigurationSerializable {
    private final String skinValue;
    private final String skinSignature;
    private final String fileHash;

    private boolean retrievedFromSave = false;

    public SkinData(final String skinValue, final String skinSignature, final String fileHash) {
        this.skinValue = skinValue;
        this.skinSignature = skinSignature;
        this.fileHash = fileHash;
    }

    public String skinValue() {
        return this.skinValue;
    }

    public String skinSignature() {
        return this.skinSignature;
    }

    public boolean isRetrievedFromSave() {
        return this.retrievedFromSave;
    }

    public void setRetrievedFromSave(final boolean retrievedFromSave) {
        this.retrievedFromSave = retrievedFromSave;
    }

    public String getFileHash() {
        return this.fileHash;
    }

    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> data = new HashMap<>();

        data.put("skinValue", this.skinValue);
        data.put("skinSignature", this.skinSignature);
        data.put("fileHash", this.fileHash);

        return data;
    }

    @Override
    public String toString() {
        return "SkinData{" +
                "skinValue='" + skinValue + '\'' +
                ", skinSignature='" + skinSignature + '\'' +
                ", fileHash='" + fileHash + "'\''" +
                '}';
    }

    public static SkinData deserialize(final Map<String, Object> args) {
        final String fileHash = args.containsKey("fileHash") ? (String) args.get("fileHash") : null;

        return new SkinData((String) args.get("skinValue"), (String) args.get("skinSignature"), fileHash);
    }
}