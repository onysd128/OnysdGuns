package com.mrcrayfish.guns.client;

import com.mrcrayfish.guns.event.CommonEvents;
import com.mrcrayfish.guns.item.ItemGun;
import com.mrcrayfish.guns.util.GunModifierHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Author: MrCrayfish
 */
public class AimTracker
{
    private static final double MAX_AIM = 8;

    private double currentAim;
    private double previousAim;

    public void handleAiming(EntityPlayer player)
    {
        previousAim = currentAim;
        /* Attachment modifiers can slow down or speed up the time taken to aim down sights */
        double speed = 1.0;
        ItemStack heldItem = player.getHeldItemMainhand();
        if(heldItem.getItem() instanceof ItemGun)
        {
            speed = GunModifierHelper.getModifiedAimDownSightSpeed(heldItem, 1.0);
        }
        if(player.getDataManager().get(CommonEvents.AIMING))
        {
            if(currentAim < MAX_AIM)
            {
                currentAim = Math.min(MAX_AIM, currentAim + speed);
            }
        }
        else
        {
            if(currentAim > 0)
            {
                currentAim = Math.max(0.0, currentAim - speed);
            }
        }
    }

    public boolean isAiming()
    {
        return currentAim != 0 || previousAim != 0;
    }

    public float getNormalProgress(float partialTicks)
    {
        return (float) ((previousAim + (currentAim - previousAim) * (previousAim == 0 || previousAim == MAX_AIM ? 0 : partialTicks)) / MAX_AIM);
    }
}
