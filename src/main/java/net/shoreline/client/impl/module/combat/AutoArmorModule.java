package net.shoreline.client.impl.module.combat;

import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberDisplay;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.init.Managers;

import java.util.PriorityQueue;
import java.util.Queue;

/**
 * @author linus
 * @since 1.0
 */
public class AutoArmorModule extends ToggleModule {

    //
    Config<Priority> priorityConfig = (new EnumConfig<>("Priority", "Armor enchantment priority", Priority.BLAST_PROTECTION, Priority.values()));
    Config<Float> minDurabilityConfig = (new NumberConfig<>("MinDurability", "Durability percent to replace armor", 0.0f, 0.0f, 20.0f, NumberDisplay.PERCENT));
    Config<Boolean> elytraPriorityConfig = (new BooleanConfig("ElytraPriority", "Prioritizes existing elytras in the chestplate armor slot", true));
    Config<Boolean> blastLeggingsConfig = (new BooleanConfig("Leggings-BlastPriority", "Prioritizes Blast Protection leggings", true));
    Config<Boolean> noBindingConfig = (new BooleanConfig("NoBinding", "Avoids armor with the Curse of Binding enchantment", true));
    Config<Boolean> inventoryConfig = (new BooleanConfig("AllowInventory", "Allows armor to be swapped while in the inventory menu", false));
    //
    private final Queue<ArmorSlot> helmet    = new PriorityQueue<>();
    private final Queue<ArmorSlot> chestplate = new PriorityQueue<>();
    private final Queue<ArmorSlot> leggings  = new PriorityQueue<>();
    private final Queue<ArmorSlot> boots     = new PriorityQueue<>();

    public AutoArmorModule() {
        super("AutoArmor", "Automatically replaces armor pieces", ModuleCategory.COMBAT);
    }

    @EventListener
    public void onTick(PlayerTickEvent event) {
        if (mc.currentScreen != null
                && !(mc.currentScreen instanceof InventoryScreen && inventoryConfig.getValue())) {
            return;
        }

        helmet.clear();
        chestplate.clear();
        leggings.clear();
        boots.clear();

        for (int j = 0; j < 36; j++) {
            ItemStack stack = mc.player.getInventory().getStack(j);
            if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem armor)) continue;
            if (noBindingConfig.getValue() && hasEnchantment(stack, Enchantments.BINDING_CURSE)) continue;

            int index = armor.getSlotType().getEntitySlotId();
            float dura = (stack.getMaxDamage() - stack.getDamage()) / (float) stack.getMaxDamage();
            if (dura < minDurabilityConfig.getValue()) continue;

            ArmorSlot data = new ArmorSlot(index, j, stack);
            switch (index) {
                case 0 -> helmet.add(data);
                case 1 -> chestplate.add(data);
                case 2 -> leggings.add(data);
                case 3 -> boots.add(data);
            }
        }

        for (int i = 0; i < 4; i++) {
            ItemStack armorStack = mc.player.getInventory().getArmorStack(i);
            if (elytraPriorityConfig.getValue() && armorStack.getItem() == Items.ELYTRA) continue;

            float armorDura = (armorStack.getMaxDamage() - armorStack.getDamage())
                    / (float) armorStack.getMaxDamage();
            if (!armorStack.isEmpty() || armorDura >= minDurabilityConfig.getValue()) continue;

            switch (i) {
                case 0 -> { if (!helmet.isEmpty())    swapFromQueue(helmet);    }
                case 1 -> { if (!chestplate.isEmpty()) swapFromQueue(chestplate); }
                case 2 -> { if (!leggings.isEmpty())  swapFromQueue(leggings);  }
                case 3 -> { if (!boots.isEmpty())     swapFromQueue(boots);     }
            }
        }
    }

    private void swapFromQueue(Queue<ArmorSlot> queue) {
        ArmorSlot slot = queue.poll();
        swapArmor(slot.getType(), slot.getSlot());
    }

    public void swapArmor(int armorSlot, int slot) {
        ItemStack stack = mc.player.getInventory().getArmorStack(armorSlot);
        armorSlot = 8 - armorSlot;

        int invSlot = slot < 9 ? slot + 36 : slot;
        Managers.INVENTORY.pickupSlot(invSlot);
        boolean hasOld = !stack.isEmpty();
        Managers.INVENTORY.pickupSlot(armorSlot);
        if (hasOld) {
            Managers.INVENTORY.pickupSlot(invSlot);
        }
    }

    public enum Priority {
        BLAST_PROTECTION(Enchantments.BLAST_PROTECTION),
        PROTECTION(Enchantments.PROTECTION),
        PROJECTILE_PROTECTION(Enchantments.PROJECTILE_PROTECTION);

        private final Enchantment enchant;
        Priority(Enchantment enchant) { this.enchant = enchant; }
        public Enchantment getEnchantment() { return enchant; }
    }

    public boolean hasEnchantment(ItemStack stack, Enchantment enchant) {
        return EnchantmentHelper.getLevel(enchant, stack) > 0;
    }

    public class ArmorSlot implements Comparable<ArmorSlot> {
        private final int armorType, slot;
        private final ItemStack armorStack;

        public ArmorSlot(int armorType, int slot, ItemStack armorStack) {
            this.armorType = armorType;
            this.slot = slot;
            this.armorStack = armorStack;
        }

        @Override
        public int compareTo(ArmorSlot other) {
            if (armorType != other.armorType) return 0;

            ArmorItem me    = (ArmorItem) armorStack.getItem();
            ArmorItem otherItem = (ArmorItem) other.armorStack.getItem();

            int protDiff = me.getMaterial().getProtection(me.getType())
                    - otherItem.getMaterial().getProtection(otherItem.getType());
            if (protDiff != 0) return -protDiff;

            Enchantment priorityEnchant = priorityConfig.getValue().getEnchantment();
            if (blastLeggingsConfig.getValue()
                    && armorType == 2
                    && hasEnchantment(armorStack, Enchantments.BLAST_PROTECTION)) {
                return -1;
            }
            boolean meHas  = hasEnchantment(armorStack, priorityEnchant);
            boolean youHas = hasEnchantment(other.armorStack, priorityEnchant);
            if (meHas == youHas) return 0;
            return meHas ? -1 : 1;
        }

        public ItemStack getArmorStack() {
            return armorStack;
        }

        public int getType()     {
            return armorType;
        }
        public int getSlot()     {
            return slot;
        }
    }
}
