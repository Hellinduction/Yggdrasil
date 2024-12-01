package dev.heypr.yggdrasil.misc.object;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public final class SkinData implements ConfigurationSerializable {
    private final String skinValue;
    private final String skinSignature;

    private boolean retrievedFromSave = false;

    public SkinData(final String skinValue, final String skinSignature) {
        this.skinValue = skinValue;
        this.skinSignature = skinSignature;
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

    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> data = new HashMap<>();

        data.put("skinValue", this.skinValue);
        data.put("skinSignature", this.skinSignature);

        return data;
    }

    @Override
    public String toString() {
        return "SkinData{" +
                "skinValue='" + skinValue + '\'' +
                ", skinSignature='" + skinSignature + '\'' +
                '}';
    }

    public static SkinData deserialize(final Map<String, Object> args) {
        return new SkinData((String) args.get("skinValue"), (String) args.get("skinSignature"));
    }
}