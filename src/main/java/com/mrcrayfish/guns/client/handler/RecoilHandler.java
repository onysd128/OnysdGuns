package com.mrcrayfish.guns.client.handler;

import com.mrcrayfish.guns.GunConfig;
import com.mrcrayfish.guns.client.event.RenderEvents;
import com.mrcrayfish.guns.item.ItemGun;
import com.mrcrayfish.guns.object.Gun;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.CooldownTracker;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Random;

/**
 * Author: MrCrayfish
 *
 * Ported from 1.19.X. Owns the camera recoil and the gun recoil state used by
 * the weapon renderer. The gun recoil normal is computed from the mod's own
 * cooldown tracker (1.12.2 has no SHOOTING data key), on the render hand event
 * before {@link RenderEvents} applies the recoil transforms.
 */
public class RecoilHandler
{
    private static RecoilHandler instance;

    public static RecoilHandler get()
    {
        if(instance == null)
        {
            instance = new RecoilHandler();
        }
        return instance;
    }

    private Random random = new Random();
    private double gunRecoilNormal;
    private double gunRecoilAngle;
    private float gunRecoilRandom;
    private float cameraRecoil;
    private float progressCameraRecoil;

    private RecoilHandler() {}

    /**
     * Called each time the player fires a shot on the client (from GunHandler.fire).
     * Mirrors GunFireEvent.Post in 1.19: resets the camera recoil and picks a new
     * random value for the gun recoil sway. Note the early return when camera recoil
     * is disabled also skips updating the random value, exactly like 1.19.
     *
     * @param gun the modified gun that was fired
     */
    public void onGunFire(Gun gun)
    {
        if(!GunConfig.CLIENT.display.enableCameraRecoil)
            return;

        float recoilModifier = 1.0F; /* 1.12.2 has no GunModifierHelper */
        recoilModifier *= (float) this.getAdsRecoilReduction(gun);
        this.cameraRecoil = gun.general.recoilAngle * recoilModifier;
        this.progressCameraRecoil = 0F;
        this.gunRecoilRandom = random.nextFloat();
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event)
    {
        if(event.phase != TickEvent.Phase.END || this.cameraRecoil <= 0)
            return;

        Minecraft mc = Minecraft.getMinecraft();
        if(mc.player == null)
            return;

        if(!GunConfig.CLIENT.display.enableCameraRecoil)
            return;

        float recoilAmount = this.cameraRecoil * event.renderTickTime * 0.15F;
        float startProgress = this.progressCameraRecoil / this.cameraRecoil;
        float endProgress = (this.progressCameraRecoil + recoilAmount) / this.cameraRecoil;

        float pitch = mc.player.rotationPitch;
        if(startProgress < 0.2F)
        {
            mc.player.rotationPitch = pitch - ((endProgress - startProgress) / 0.2F) * this.cameraRecoil;
        }
        else
        {
            mc.player.rotationPitch = pitch + ((endProgress - startProgress) / 0.8F) * this.cameraRecoil;
        }

        this.progressCameraRecoil += recoilAmount;

        if(this.progressCameraRecoil >= this.cameraRecoil)
        {
            this.cameraRecoil = 0;
            this.progressCameraRecoil = 0;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderOverlay(RenderSpecificHandEvent event)
    {
        if(event.getHand() != EnumHand.MAIN_HAND)
            return;

        ItemStack heldItem = event.getItemStack();
        if(!(heldItem.getItem() instanceof ItemGun))
            return;

        EntityPlayer player = Minecraft.getMinecraft().player;
        if(player == null)
            return;

        ItemGun gunItem = (ItemGun) heldItem.getItem();
        Gun modifiedGun = gunItem.getModifiedGun(heldItem);

        /* 1.12.2 keeps its own cooldown tracker so the recoil is timed with the shot */
        CooldownTracker tracker = RenderEvents.getCooldownTracker(player.getUniqueID());
        float cooldown = tracker.getCooldown(gunItem, Minecraft.getMinecraft().getRenderPartialTicks());
        cooldown = cooldown >= modifiedGun.general.recoilDurationOffset ? (cooldown - modifiedGun.general.recoilDurationOffset) / (1.0F - modifiedGun.general.recoilDurationOffset) : 0.0F;
        if(cooldown >= 0.8)
        {
            float amount = 1.0F * ((1.0F - cooldown) / 0.2F);
            this.gunRecoilNormal = 1 - (--amount) * amount * amount * amount;
        }
        else
        {
            float amount = (cooldown / 0.8F);
            this.gunRecoilNormal = amount < 0.5 ? 2 * amount * amount : -1 + (4 - 2 * amount) * amount;
        }

        this.gunRecoilAngle = modifiedGun.general.recoilAngle;
    }

    /**
     * @return the recoil reduction applied while aiming down sights, based on the
     * configured recoilAdsReduction and the current ads zoom progress
     */
    public double getAdsRecoilReduction(Gun gun)
    {
        return 1.0 - gun.general.recoilAdsReduction * RenderEvents.getNormalZoomProgress();
    }

    public double getGunRecoilNormal()
    {
        return this.gunRecoilNormal;
    }

    public double getGunRecoilAngle()
    {
        return this.gunRecoilAngle;
    }

    public float getGunRecoilRandom()
    {
        return this.gunRecoilRandom;
    }
}