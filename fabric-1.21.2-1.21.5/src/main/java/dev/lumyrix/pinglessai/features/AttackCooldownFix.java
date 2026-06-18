package dev.lumyrix.pinglessai.features;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.lumyrix.pinglessai.mixin.InventoryAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class AttackCooldownFix {
    private int lastSelected = -1;
    /** Tipo do item na mão no tick anterior — detecta troca via inventário sem mudar de slot. */
    private net.minecraft.world.item.Item lastMainHandItem = null;

    public static Multimap<Holder<Attribute>, AttributeModifier> getAttackSpeed(@NotNull Player player) {
        Multimap<Holder<Attribute>, AttributeModifier> attackSpeed = HashMultimap.create();
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
            ItemAttributeModifiers comp = stack.getComponents().get(DataComponents.ATTRIBUTE_MODIFIERS);
            if (comp == null) continue;
            for (ItemAttributeModifiers.Entry entry : comp.modifiers()) {
                if (entry.attribute().equals(Attributes.ATTACK_SPEED)) {
                    if (entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE) {
                        base += entry.modifier().amount();
                    } else if (entry.modifier().operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE) {
                        modifier += entry.modifier().amount();
                    } else if (entry.modifier().operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                        modifierFinal += entry.modifier().amount();
                    }
                }
            }
        }

        attackSpeed.put(Attributes.ATTACK_SPEED,
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath("pinglessai", "pingless_base"),
                base, AttributeModifier.Operation.ADD_VALUE));
        attackSpeed.put(Attributes.ATTACK_SPEED,
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath("pinglessai", "pingless_mult_base"),
                modifier, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        attackSpeed.put(Attributes.ATTACK_SPEED,
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath("pinglessai", "pingless_mult_total"),
                modifierFinal, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

        if (player.hasEffect(MobEffects.DIG_SPEED)) {
            attackSpeed.put(Attributes.ATTACK_SPEED,
                new AttributeModifier(ResourceLocation.fromNamespaceAndPath("pinglessai", "pingless_haste"),
                    (Objects.requireNonNull(player.getEffect(MobEffects.DIG_SPEED)).getAmplifier() + 1) * 0.1,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }

        if (player.hasEffect(MobEffects.CONDUIT_POWER)) {
            attackSpeed.put(Attributes.ATTACK_SPEED,
                new AttributeModifier(ResourceLocation.fromNamespaceAndPath("pinglessai", "pingless_conduit"),
                    (Objects.requireNonNull(player.getEffect(MobEffects.CONDUIT_POWER)).getAmplifier() + 1) * 0.1,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }

        return attackSpeed;
    }

    public void tick(@NotNull Player player) {
        int selected = ((InventoryAccessor) player.getInventory()).getSelectedSlot();
        net.minecraft.world.item.Item currentMainHandItem = player.getMainHandItem().getItem();

        boolean slotChanged = lastSelected != selected;
        boolean itemChanged = currentMainHandItem != lastMainHandItem;

        if (slotChanged || itemChanged) {
            lastSelected     = selected;
            lastMainHandItem = currentMainHandItem;

            Multimap<Holder<Attribute>, AttributeModifier> newSpeed = getAttackSpeed(player);
            AttributeMap attributeMap = player.getAttributes();
            var instance = attributeMap.getInstance(Attributes.ATTACK_SPEED);
            if (instance != null) {
                Set<AttributeModifier> mods = instance.getModifiers();
                Multimap<Holder<Attribute>, AttributeModifier> remove = ArrayListMultimap.create();
                for (AttributeModifier mod : mods) {
                    remove.put(Attributes.ATTACK_SPEED, mod);
                }
                attributeMap.removeAttributeModifiers(remove);
                attributeMap.addTransientAttributeModifiers(newSpeed);
            }
        }
    }
}
