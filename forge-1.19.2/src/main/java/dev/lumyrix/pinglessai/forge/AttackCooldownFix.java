package dev.lumyrix.pinglessai.forge;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class AttackCooldownFix {
    private int lastSelected = -1;

    public static Multimap<Attribute, AttributeModifier> getAttackSpeed(@NotNull Player player) {
        Multimap<Attribute, AttributeModifier> attackSpeed = HashMultimap.create();
        List<ItemStack> items = new ArrayList<>();
        items.add(player.getItemBySlot(EquipmentSlot.HEAD));
        items.add(player.getItemBySlot(EquipmentSlot.CHEST));
        items.add(player.getItemBySlot(EquipmentSlot.LEGS));
        items.add(player.getItemBySlot(EquipmentSlot.FEET));
        items.add(player.getMainHandItem());
        items.add(player.getOffhandItem());

        double base = 0.0;
        double modifier = 0.0;
        double modifierFinal = 0.0;

        for (ItemStack stack : items) {
            if (stack.isEmpty()) continue;
            Multimap<Attribute, AttributeModifier> mods = stack.getAttributeModifiers(EquipmentSlot.MAINHAND);
            for (Map.Entry<Attribute, AttributeModifier> entry : mods.entries()) {
                if (entry.getKey().equals(Attributes.ATTACK_SPEED)) {
                    AttributeModifier mod = entry.getValue();
                    if (mod.getOperation() == AttributeModifier.Operation.ADDITION) {
                        base += mod.getAmount();
                    } else if (mod.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE) {
                        modifier += mod.getAmount();
                    } else if (mod.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL) {
                        modifierFinal += mod.getAmount();
                    }
                }
            }
        }

        attackSpeed.put(Attributes.ATTACK_SPEED,
            new AttributeModifier("pinglessai:pingless_base", base, AttributeModifier.Operation.ADDITION));
        attackSpeed.put(Attributes.ATTACK_SPEED,
            new AttributeModifier("pinglessai:pingless_mult_base", modifier, AttributeModifier.Operation.MULTIPLY_BASE));
        attackSpeed.put(Attributes.ATTACK_SPEED,
            new AttributeModifier("pinglessai:pingless_mult_total", modifierFinal, AttributeModifier.Operation.MULTIPLY_TOTAL));

        if (player.hasEffect(MobEffects.DIG_SPEED)) {
            attackSpeed.put(Attributes.ATTACK_SPEED,
                new AttributeModifier("pinglessai:pingless_haste",
                    (Objects.requireNonNull(player.getEffect(MobEffects.DIG_SPEED)).getAmplifier() + 1) * 0.1,
                    AttributeModifier.Operation.MULTIPLY_TOTAL));
        }

        if (player.hasEffect(MobEffects.CONDUIT_POWER)) {
            attackSpeed.put(Attributes.ATTACK_SPEED,
                new AttributeModifier("pinglessai:pingless_conduit",
                    (Objects.requireNonNull(player.getEffect(MobEffects.CONDUIT_POWER)).getAmplifier() + 1) * 0.1,
                    AttributeModifier.Operation.MULTIPLY_TOTAL));
        }

        return attackSpeed;
    }

    public void tick(@NotNull Player player) {
        int selected = player.getInventory().selected;  // accessible via AT
        if (lastSelected != selected) {
            lastSelected = selected;
            Multimap<Attribute, AttributeModifier> newSpeed = getAttackSpeed(player);
            var attributeMap = player.getAttributes();
            var instance = attributeMap.getInstance(Attributes.ATTACK_SPEED);
            if (instance != null) {
                Collection<AttributeModifier> mods = instance.getModifiers();
                Multimap<Attribute, AttributeModifier> remove = ArrayListMultimap.create();
                for (AttributeModifier mod : mods) {
                    remove.put(Attributes.ATTACK_SPEED, mod);
                }
                attributeMap.removeAttributeModifiers(remove);
                attributeMap.addTransientAttributeModifiers(newSpeed);
            }
        }
    }
}
