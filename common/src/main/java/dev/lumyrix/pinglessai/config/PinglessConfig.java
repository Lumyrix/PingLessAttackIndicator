package dev.lumyrix.pinglessai.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;

public class PinglessConfig {

    public enum Mode {
        CLIENT_SIDE,   // cooldown totalmente client-side, reseta só no clique
        SMOOTHED,      // suaviza as correções do servidor
        VANILLA        // sem mudança
    }

    private static PinglessConfig INSTANCE = new PinglessConfig();
    private static final Gson   GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path   PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("pinglessai.json");

    private Mode mode = Mode.CLIENT_SIDE;

    public static PinglessConfig getInstance() { return INSTANCE; }
    public Mode getMode()           { return mode == null ? Mode.CLIENT_SIDE : mode; }
    public void setMode(Mode m)     { this.mode = m; }

    public static void load() {
        File f = PATH.toFile();
        if (f.exists()) {
            try (Reader r = new FileReader(f)) {
                PinglessConfig loaded = GSON.fromJson(r, PinglessConfig.class);
                if (loaded != null) INSTANCE = loaded;
            } catch (Exception e) {
                INSTANCE = new PinglessConfig();
            }
        }
        save();
    }

    public static void save() {
        try (Writer w = new FileWriter(PATH.toFile())) {
            GSON.toJson(INSTANCE, w);
        } catch (Exception ignored) {}
    }
}
