package com.jellomakker.cpubooster.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/cpubooster.json");
    private static CpuBoosterConfig config;

    public static CpuBoosterConfig load() {
        if (config != null) return config;
        if (!CONFIG_FILE.getParentFile().exists()) {
            CONFIG_FILE.getParentFile().mkdirs();
        }
        if (!CONFIG_FILE.exists()) {
            config = new CpuBoosterConfig();
            save();
            return config;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            config = GSON.fromJson(reader, CpuBoosterConfig.class);
            if (config == null) config = new CpuBoosterConfig();
        } catch (IOException e) {
            config = new CpuBoosterConfig();
        }
        return config;
    }

    public static void save() {
        if (config == null) config = new CpuBoosterConfig();
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static CpuBoosterConfig get() {
        if (config == null) load();
        return config;
    }

    public static void set(CpuBoosterConfig cfg) {
        config = cfg;
        save();
    }
}
