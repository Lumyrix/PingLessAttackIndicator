package dev.lumyrix.pinglessai.tracker;

import dev.lumyrix.pinglessai.config.PinglessConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class AttackCooldownTracker {

    private static float displayed     = 1.0f;
    private static float prevDisplayed = 1.0f;
    private static float prevServer    = 1.0f;

    private AttackCooldownTracker() {}

    /** Chamado a cada client tick. */
    public static void tick(LocalPlayer player) {
        prevDisplayed = displayed;
        PinglessConfig.Mode mode = PinglessConfig.getInstance().getMode();

        switch (mode) {
            case CLIENT_SIDE -> {
                // Usa o mesmo delay inteiro que o vanilla calcula internamente
                double speed = player.getAttributeValue(Attributes.ATTACK_SPEED);
                int delay = Math.max(1, (int)(1.0 / speed * 20.0));
                displayed = Math.min(1.0f, displayed + 1.0f / delay);
            }
            case SMOOTHED -> {
                float server = player.getAttackStrengthScale(0f);
                if (server < prevServer - 0.05f) {
                    displayed = Math.max(server, displayed - 0.07f);
                } else {
                    displayed = server;
                }
                prevServer = server;
            }
            case VANILLA -> {
                displayed  = player.getAttackStrengthScale(0f);
                prevServer = displayed;
            }
        }
    }

    /** Chamado quando o jogador ataca (client-side). */
    public static void onAttack() {
        if (PinglessConfig.getInstance().getMode() == PinglessConfig.Mode.CLIENT_SIDE) {
            displayed     = 0.0f;
            prevDisplayed = 0.0f;
        }
    }

    /** Valor interpolado com partial tick — igual ao vanilla getAttackStrengthScale(pt). */
    public static float getInterpolated(float partialTick) {
        return prevDisplayed + (displayed - prevDisplayed) * partialTick;
    }

    public static float getDisplayed() { return displayed; }

    public static boolean isActive() {
        return PinglessConfig.getInstance().getMode() != PinglessConfig.Mode.VANILLA;
    }
}
