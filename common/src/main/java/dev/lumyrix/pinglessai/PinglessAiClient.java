package dev.lumyrix.pinglessai;

import dev.lumyrix.pinglessai.config.PinglessConfig;
import dev.lumyrix.pinglessai.features.AttackCooldownFix;
import dev.lumyrix.pinglessai.tracker.AttackCooldownTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class PinglessAiClient implements ClientModInitializer {

    public static final String MOD_ID = "pinglessai";
    private AttackCooldownFix attackCooldownFix;

    @Override
    public void onInitializeClient() {
        PinglessConfig.load();
        attackCooldownFix = new AttackCooldownFix();
        System.out.println("PingLess Attack Indicator loaded.");

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                attackCooldownFix.tick(client.player);
                AttackCooldownTracker.tick(client.player);
            }
        });
    }
}
