package dev.lumyrix.pinglessai.forge;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod("pinglessai")
public class PinglessAiForge {
    private final AttackCooldownFix fix = new AttackCooldownFix();

    public PinglessAiForge() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.player != null) {
                fix.tick(mc.player);
            }
        }
    }
}
