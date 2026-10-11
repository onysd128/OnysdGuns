package com.mrcrayfish.guns.init;

import com.mrcrayfish.guns.MrCrayfishGunMod;
import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.common.Attachments;
import com.mrcrayfish.guns.common.GunModifiers;
import com.mrcrayfish.guns.item.*;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModGuns
{
    @GameRegistry.ObjectHolder("cgm:handgun")
    public static final Item PISTOL = null;

    @GameRegistry.ObjectHolder("cgm:shotgun")
    public static final Item SHOTGUN = null;

    @GameRegistry.ObjectHolder("cgm:rifle")
    public static final Item RIFLE = null;

    @GameRegistry.ObjectHolder("cgm:grenade_launcher")
    public static final ItemGun GRENADE_LAUNCHER = null;

    @GameRegistry.ObjectHolder("cgm:bazooka")
    public static final Item BAZOOKA = null;

    @GameRegistry.ObjectHolder("cgm:chain_gun")
    public static final Item CHAIN_GUN = null;

    @GameRegistry.ObjectHolder("cgm:assault_rifle")
    public static final Item ASSAULT_RIFLE = null;

    @GameRegistry.ObjectHolder("cgm:machine_pistol")
    public static final Item MACHINE_PISTOL = null;

    @GameRegistry.ObjectHolder("cgm:heavy_rifle")
    public static final Item HEAVY_RIFLE = null;

    @GameRegistry.ObjectHolder("cgm:part")
    public static final Item PARTS = null;

    @GameRegistry.ObjectHolder("cgm:basic_ammo")
    public static final Item BASIC_AMMO = null;

    @GameRegistry.ObjectHolder("cgm:advanced_ammo")
    public static final Item ADVANCED_AMMO = null;

    @GameRegistry.ObjectHolder("cgm:shell")
    public static final Item SHELL = null;

    @GameRegistry.ObjectHolder("cgm:missile")
    public static final ItemAmmo MISSILE = null;

    @GameRegistry.ObjectHolder("cgm:grenade")
    public static final ItemAmmo GRENADE = null;

    @GameRegistry.ObjectHolder("cgm:stun_grenade")
    public static final Item STUN_GRENADE = null;

    @GameRegistry.ObjectHolder("cgm:silencer")
    public static final Item SILENCER = null;

    @GameRegistry.ObjectHolder("cgm:short_scope")
    public static final Item SHORT_SCOPE = null;

    @GameRegistry.ObjectHolder("cgm:medium_scope")
    public static final Item MEDIUM_SCOPE = null;

    @GameRegistry.ObjectHolder("cgm:long_scope")
    public static final Item LONG_SCOPE = null;

    @GameRegistry.ObjectHolder("cgm:light_stock")
    public static final Item LIGHT_STOCK = null;

    @GameRegistry.ObjectHolder("cgm:tactical_stock")
    public static final Item TACTICAL_STOCK = null;

    @GameRegistry.ObjectHolder("cgm:weighted_stock")
    public static final Item WEIGHTED_STOCK = null;

    @GameRegistry.ObjectHolder("cgm:light_grip")
    public static final Item LIGHT_GRIP = null;

    @GameRegistry.ObjectHolder("cgm:specialised_grip")
    public static final Item SPECIALISED_GRIP = null;

    public static void register()
    {
        register(new ItemGun(new ResourceLocation(Reference.MOD_ID, "handgun")).setCreativeTab(MrCrayfishGunMod.GUN_TAB));
        register(new ItemGun(new ResourceLocation(Reference.MOD_ID, "shotgun")).setCreativeTab(MrCrayfishGunMod.GUN_TAB));
        register(new ItemGun(new ResourceLocation(Reference.MOD_ID, "rifle")).setCreativeTab(MrCrayfishGunMod.GUN_TAB));
        register(new ItemGun(new ResourceLocation(Reference.MOD_ID, "grenade_launcher")).setCreativeTab(MrCrayfishGunMod.GUN_TAB));
        register(new ItemGun(new ResourceLocation(Reference.MOD_ID, "bazooka")).setCreativeTab(MrCrayfishGunMod.GUN_TAB));
        register(new ItemGun(new ResourceLocation(Reference.MOD_ID, "chain_gun")).setCreativeTab(MrCrayfishGunMod.GUN_TAB));
        register(new ItemGun(new ResourceLocation(Reference.MOD_ID, "assault_rifle")).setCreativeTab(MrCrayfishGunMod.GUN_TAB));
        register(new ItemGun(new ResourceLocation(Reference.MOD_ID, "machine_pistol")).setCreativeTab(MrCrayfishGunMod.GUN_TAB));
        register(new ItemGun(new ResourceLocation(Reference.MOD_ID, "heavy_rifle")).setCreativeTab(MrCrayfishGunMod.GUN_TAB));

        register(new ItemAmmo(new ResourceLocation(Reference.MOD_ID, "basic_ammo")).setCreativeTab(MrCrayfishGunMod.GUN_TAB));
        register(new ItemAmmo(new ResourceLocation(Reference.MOD_ID, "advanced_ammo")).setCreativeTab(MrCrayfishGunMod.GUN_TAB));
        register(new ItemAmmo(new ResourceLocation(Reference.MOD_ID, "shell")).setCreativeTab(MrCrayfishGunMod.GUN_TAB));
        register(new ItemAmmo(new ResourceLocation(Reference.MOD_ID, "missile")).setCreativeTab(MrCrayfishGunMod.GUN_TAB));
        register(new ItemGrenade(new ResourceLocation(Reference.MOD_ID, "grenade")).setCreativeTab(MrCrayfishGunMod.GUN_TAB));
        register(new ItemStunGrenade(new ResourceLocation(Reference.MOD_ID, "stun_grenade")).setCreativeTab(MrCrayfishGunMod.GUN_TAB));

        register(new ItemPart());

        // Scope attachments (separate items instead of metadata)
        register(new ScopeItem(new ResourceLocation(Reference.MOD_ID, "short_scope"), com.mrcrayfish.guns.common.Attachments.SHORT_SCOPE.copy()));
        register(new ScopeItem(new ResourceLocation(Reference.MOD_ID, "medium_scope"), com.mrcrayfish.guns.common.Attachments.MEDIUM_SCOPE.copy()));
        register(new ScopeItem(new ResourceLocation(Reference.MOD_ID, "long_scope"), com.mrcrayfish.guns.common.Attachments.LONG_SCOPE.copy()));

        // Barrel attachments
        register(new BarrelItem(new ResourceLocation(Reference.MOD_ID, "silencer"), com.mrcrayfish.guns.item.attachment.impl.Barrel.create(8.0F, GunModifiers.SILENCED, GunModifiers.REDUCED_DAMAGE)));

        // Stock attachments
        register(new StockItem(new ResourceLocation(Reference.MOD_ID, "light_stock"), com.mrcrayfish.guns.item.attachment.impl.Stock.create(GunModifiers.BETTER_CONTROL)));
        register(new StockItem(new ResourceLocation(Reference.MOD_ID, "tactical_stock"), com.mrcrayfish.guns.item.attachment.impl.Stock.create(GunModifiers.STABILISED)));
        register(new StockItem(new ResourceLocation(Reference.MOD_ID, "weighted_stock"), com.mrcrayfish.guns.item.attachment.impl.Stock.create(GunModifiers.SUPER_STABILISED)));

        // Under barrel attachments
        register(new UnderBarrelItem(new ResourceLocation(Reference.MOD_ID, "light_grip"), com.mrcrayfish.guns.item.attachment.impl.UnderBarrel.create(GunModifiers.LIGHT_RECOIL)));
        register(new UnderBarrelItem(new ResourceLocation(Reference.MOD_ID, "specialised_grip"), com.mrcrayfish.guns.item.attachment.impl.UnderBarrel.create(GunModifiers.REDUCED_RECOIL)));
    }

    private static void register(Item item)
    {
        RegistrationHandler.Items.add(item);
    }
}
