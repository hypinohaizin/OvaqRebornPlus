package net.shoreline.client.util.player;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.Map;

public class EnchantmentUtil {


    public static int getLevel(ItemStack stack, Enchantment enchantmentRegistryKey) {
        if (stack.isEmpty()) {
            return 0;
        }
        Map<Enchantment, Integer> enchants = EnchantmentHelper.get(stack);
        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            Enchantment enchant = entry.getKey();
            Identifier id = Registries.ENCHANTMENT.getId(enchant);
            RegistryKey<Enchantment> key = RegistryKey.of(RegistryKeys.ENCHANTMENT, id);
            if (key.equals(enchantmentRegistryKey)) {
                return entry.getValue();
            }
        }
        return 0;
    }
    
    public static boolean isFakeEnchant2b2t(ItemStack itemStack) {
        Map<Enchantment, Integer> enchants = EnchantmentHelper.get(itemStack);
        if (enchants.size() > 1) {
            return false;
        }
        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            Enchantment enchant = entry.getKey();
            int lvl = entry.getValue();
            if (lvl == 0 && enchant == Enchantments.PROTECTION) {
                return true;
            }
        }
        return false;
    }
}
