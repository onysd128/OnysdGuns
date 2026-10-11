package com.mrcrayfish.guns.init;

import com.mrcrayfish.guns.common.WorkbenchRegistry;
import com.mrcrayfish.guns.init.ModGuns;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;

/**
 * Author: MrCrayfish
 */
public class ModCrafting
{
    @SuppressWarnings("ConstantConditions")
    public static void register()
    {
        WorkbenchRegistry.registerRecipe(new ItemStack(ModGuns.PISTOL),
                new ItemStack(Items.IRON_INGOT, 14));
        WorkbenchRegistry.registerRecipe(new ItemStack(ModGuns.SHOTGUN),
                new ItemStack(Items.IRON_INGOT, 24));
        WorkbenchRegistry.registerRecipe(new ItemStack(ModGuns.RIFLE),
                new ItemStack(Items.IRON_INGOT, 28));
        WorkbenchRegistry.registerRecipe(new ItemStack(ModGuns.GRENADE_LAUNCHER),
                new ItemStack(Items.IRON_INGOT, 32));
        WorkbenchRegistry.registerRecipe(new ItemStack(ModGuns.BAZOOKA),
                new ItemStack(Items.IRON_INGOT, 44),
                new ItemStack(Items.REDSTONE, 4),
                new ItemStack(Items.DYE, 1, EnumDyeColor.RED.getDyeDamage()));
        WorkbenchRegistry.registerRecipe(new ItemStack(ModGuns.CHAIN_GUN),
                new ItemStack(Items.IRON_INGOT, 38));
        WorkbenchRegistry.registerRecipe(new ItemStack(ModGuns.ASSAULT_RIFLE),
                new ItemStack(Items.IRON_INGOT, 28));
        WorkbenchRegistry.registerRecipe(new ItemStack(ModGuns.HEAVY_RIFLE),
                new ItemStack(Items.IRON_INGOT, 36));
        WorkbenchRegistry.registerRecipe(new ItemStack(ModGuns.BASIC_AMMO, 32),
                new ItemStack(Items.GUNPOWDER, 1),
                new ItemStack(Items.IRON_NUGGET, 8));
        WorkbenchRegistry.registerRecipe(new ItemStack(ModGuns.ADVANCED_AMMO, 16),
                new ItemStack(Items.GUNPOWDER, 1),
                new ItemStack(Items.IRON_NUGGET, 4));
        WorkbenchRegistry.registerRecipe(new ItemStack(ModGuns.SHELL, 24),
                new ItemStack(Items.GUNPOWDER, 1),
                new ItemStack(Items.GOLD_NUGGET, 2),
                new ItemStack(Items.IRON_NUGGET, 4));
        WorkbenchRegistry.registerRecipe(new ItemStack(ModGuns.GRENADE, 2),
                new ItemStack(Items.GUNPOWDER, 4),
                new ItemStack(Items.IRON_INGOT, 2));
        WorkbenchRegistry.registerRecipe(new ItemStack(ModGuns.MISSILE, 2),
                new ItemStack(Items.GUNPOWDER, 8),
                new ItemStack(Items.IRON_INGOT, 4));
        WorkbenchRegistry.registerRecipe(new ItemStack(ModGuns.STUN_GRENADE, 2),
                new ItemStack(Items.GLOWSTONE_DUST, 4),
                new ItemStack(Items.GUNPOWDER, 2),
                new ItemStack(Items.IRON_INGOT, 2));
        WorkbenchRegistry.registerRecipe(new ItemStack(ModGuns.SHORT_SCOPE),
                new ItemStack(Items.IRON_INGOT, 4),
                new ItemStack(Blocks.GLASS_PANE, 1),
                new ItemStack(Items.DYE, 1, EnumDyeColor.RED.getDyeDamage()));
        WorkbenchRegistry.registerRecipe(new ItemStack(ModGuns.MEDIUM_SCOPE),
                new ItemStack(Items.IRON_INGOT, 6),
                new ItemStack(Blocks.GLASS_PANE, 2),
                new ItemStack(Items.DYE, 1, EnumDyeColor.BLACK.getDyeDamage()));
        WorkbenchRegistry.registerRecipe(new ItemStack(ModGuns.LONG_SCOPE),
                new ItemStack(Items.IRON_INGOT, 8),
                new ItemStack(Blocks.GLASS_PANE, 3),
                new ItemStack(Items.DYE, 2, EnumDyeColor.BLACK.getDyeDamage()));
        WorkbenchRegistry.registerRecipe(new ItemStack(ModGuns.SILENCER),
                new ItemStack(Items.IRON_INGOT, 12));
        WorkbenchRegistry.registerRecipe(new ItemStack(ModGuns.LIGHT_STOCK),
                new ItemStack(Items.IRON_INGOT, 4),
                new ItemStack(Items.STICK, 1));
        WorkbenchRegistry.registerRecipe(new ItemStack(ModGuns.TACTICAL_STOCK),
                new ItemStack(Items.IRON_INGOT, 6),
                new ItemStack(Items.STICK, 1));
        WorkbenchRegistry.registerRecipe(new ItemStack(ModGuns.WEIGHTED_STOCK),
                new ItemStack(Items.IRON_INGOT, 8),
                new ItemStack(Items.STICK, 1));
        WorkbenchRegistry.registerRecipe(new ItemStack(ModGuns.LIGHT_GRIP),
                new ItemStack(Items.IRON_INGOT, 4));
        WorkbenchRegistry.registerRecipe(new ItemStack(ModGuns.SPECIALISED_GRIP),
                new ItemStack(Items.IRON_INGOT, 6));
    }
}
