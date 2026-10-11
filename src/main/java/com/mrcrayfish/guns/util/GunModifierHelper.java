package com.mrcrayfish.guns.util;

import com.mrcrayfish.guns.interfaces.IGunModifier;
import com.mrcrayfish.guns.item.IAttachment;
import com.mrcrayfish.guns.item.attachment.impl.Attachment;
import com.mrcrayfish.guns.object.Gun;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

/**
 * Applies the {@link IGunModifier}s contributed by the attachments installed on a weapon.
 * <p>
 * Ported from 1.19.X. 1.19 applies the modifications at every use site (recoil, damage,
 * fire rate, muzzle flash, ...). The 1.12.2 fork routes almost everything through
 * {@link com.mrcrayfish.guns.item.ItemGun#getModifiedGun(ItemStack)}, so the numeric
 * modifications (spread, fire rate, recoil, kick, projectile damage/speed/life) are baked
 * into the modified gun by {@link #applyModifiers(ItemStack, Gun)} and every existing use
 * site picks them up for free. The remaining non-gun effects (silenced sound, sound radius,
 * aim down sight speed) are queried directly through the static helpers.
 * <p>
 * Author: MrCrayfish
 */
public class GunModifierHelper
{
    private static final IGunModifier[] EMPTY = {};

    private static IGunModifier[] getModifiers(ItemStack weapon, IAttachment.Type type)
    {
        ItemStack stack = Gun.getAttachment(type, weapon);
        if(stack != null && !stack.isEmpty() && stack.getItem() instanceof IAttachment)
        {
            Object properties = ((IAttachment) stack.getItem()).getProperties();
            if(properties instanceof Attachment)
            {
                return ((Attachment) properties).getModifiers();
            }
        }
        return EMPTY;
    }

    /**
     * Bakes the numeric attachment modifications into the supplied (already NBT applied) gun.
     * The gun instance is mutated and returned for convenience.
     */
    public static Gun applyModifiers(ItemStack weapon, Gun gun)
    {
        gun.general.spread = getModifiedSpread(weapon, gun.general.spread);
        gun.general.rate = getModifiedRate(weapon, gun.general.rate);
        /* getRecoilModifier returns the reduction (1 - multiplier) so invert it back to a multiplier */
        gun.general.recoilAngle = gun.general.recoilAngle * (1.0F - getRecoilModifier(weapon));
        gun.general.recoilKick = gun.general.recoilKick * (1.0F - getKickReduction(weapon));
        gun.projectile.damage = getModifiedDamage(weapon, gun.projectile.damage);
        gun.projectile.speed = getModifiedProjectileSpeed(weapon, gun.projectile.speed);
        gun.projectile.life = getModifiedProjectileLife(weapon, gun.projectile.life);
        return gun;
    }

    public static int getModifiedProjectileLife(ItemStack weapon, int life)
    {
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                life = modifier.modifyProjectileLife(life);
            }
        }
        return life;
    }

    public static double getModifiedProjectileGravity(ItemStack weapon, double gravity)
    {
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                gravity = modifier.modifyProjectileGravity(gravity);
            }
        }
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                gravity += modifier.additionalProjectileGravity();
            }
        }
        return gravity;
    }

    public static float getModifiedSpread(ItemStack weapon, float spread)
    {
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                spread = modifier.modifyProjectileSpread(spread);
            }
        }
        return spread;
    }

    public static double getModifiedProjectileSpeed(ItemStack weapon, double speed)
    {
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                speed = modifier.modifyProjectileSpeed(speed);
            }
        }
        return speed;
    }

    public static float getFireSoundVolume(ItemStack weapon)
    {
        float volume = 1.0F;
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                volume = modifier.modifyFireSoundVolume(volume);
            }
        }
        return MathHelper.clamp(volume, 0.0F, 16.0F);
    }

    public static double getMuzzleFlashScale(ItemStack weapon, double scale)
    {
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                scale = modifier.modifyMuzzleFlashScale(scale);
            }
        }
        return scale;
    }

    /**
     * @return the amount of kick reduction (0.0 - 1.0) contributed by the attachments
     */
    public static float getKickReduction(ItemStack weapon)
    {
        float kickReduction = 1.0F;
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                kickReduction *= MathHelper.clamp(modifier.kickModifier(), 0.0F, 1.0F);
            }
        }
        return 1.0F - kickReduction;
    }

    /**
     * @return the amount of recoil reduction (0.0 - 1.0) contributed by the attachments
     */
    public static float getRecoilModifier(ItemStack weapon)
    {
        float recoilReduction = 1.0F;
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                recoilReduction *= MathHelper.clamp(modifier.recoilModifier(), 0.0F, 1.0F);
            }
        }
        return 1.0F - recoilReduction;
    }

    public static boolean isSilencedFire(ItemStack weapon)
    {
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                if(modifier.silencedFire())
                {
                    return true;
                }
            }
        }
        return false;
    }

    public static double getModifiedFireSoundRadius(ItemStack weapon, double radius)
    {
        double minRadius = radius;
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                double newRadius = modifier.modifyFireSoundRadius(radius);
                if(newRadius < minRadius)
                {
                    minRadius = newRadius;
                }
            }
        }
        return MathHelper.clamp(minRadius, 0.0, Double.MAX_VALUE);
    }

    public static float getAdditionalDamage(ItemStack weapon)
    {
        float additionalDamage = 0.0F;
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                additionalDamage += modifier.additionalDamage();
            }
        }
        return additionalDamage;
    }

    public static float getModifiedProjectileDamage(ItemStack weapon, float damage)
    {
        float finalDamage = damage;
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                finalDamage = modifier.modifyProjectileDamage(finalDamage);
            }
        }
        return finalDamage;
    }

    /**
     * Applies the damage modifications and then adds any flat bonus damage from the attachments.
     */
    public static float getModifiedDamage(ItemStack weapon, float damage)
    {
        float finalDamage = getModifiedProjectileDamage(weapon, damage);
        finalDamage += getAdditionalDamage(weapon);
        return finalDamage;
    }

    public static double getModifiedAimDownSightSpeed(ItemStack weapon, double speed)
    {
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                speed = modifier.modifyAimDownSightSpeed(speed);
            }
        }
        return MathHelper.clamp(speed, 0.01, Double.MAX_VALUE);
    }

    public static int getModifiedRate(ItemStack weapon, int rate)
    {
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                rate = modifier.modifyFireRate(rate);
            }
        }
        return MathHelper.clamp(rate, 0, Integer.MAX_VALUE);
    }
}
