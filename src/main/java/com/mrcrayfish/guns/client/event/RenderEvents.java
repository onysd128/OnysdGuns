package com.mrcrayfish.guns.client.event;

import com.mrcrayfish.guns.GunConfig;
import com.mrcrayfish.guns.ItemStackUtil;
import com.mrcrayfish.guns.MrCrayfishGunMod;
import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.client.SwayType;
import com.mrcrayfish.guns.client.handler.RecoilHandler;
import com.mrcrayfish.guns.client.render.gun.IOverrideModel;
import com.mrcrayfish.guns.client.render.gun.ModelOverrides;
import com.mrcrayfish.guns.client.util.Easings;
import com.mrcrayfish.guns.client.util.RenderUtil;
import com.mrcrayfish.guns.event.CommonEvents;
import com.mrcrayfish.guns.init.ModGuns;
import com.mrcrayfish.guns.init.ModPotions;
import com.mrcrayfish.guns.item.*;
import com.mrcrayfish.guns.item.attachment.impl.Scope;
import com.mrcrayfish.guns.object.Bullet;
import com.mrcrayfish.guns.object.GripType;
import com.mrcrayfish.guns.object.Gun;
import com.mrcrayfish.obfuscate.client.event.ModelPlayerEvent;
import com.mrcrayfish.obfuscate.client.event.RenderItemEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Field;
import java.util.*;

public class RenderEvents
{
    private static final ResourceLocation SCOPE_OVERLAY = new ResourceLocation(Reference.MOD_ID, "textures/scope_long_overlay.png");
    private static final ResourceLocation WHITE_GRADIENT = new ResourceLocation(Reference.MOD_ID, "textures/effect/white_gradient.png");
    private static final Map<UUID, CooldownTracker> COOLDOWN_TRACKER_MAP = new HashMap<>();
    private static final double ZOOM_TICKS = 4;

    /**
     * The original "fixed" display transform used by the holster before each gun got an
     * item frame specific "fixed" transform ("updated models display in frame"). Every
     * gun shared the same values {@code rotation [0, 90, 0]}, {@code translation [X, 10, 0]}
     * and {@code scale [2, 2, 2]} except for the X translation, which differs per gun
     * because each model's geometry is centred differently. The holster now renders with
     * {@link ItemCameraTransforms.TransformType#NONE} and applies this legacy transform
     * manually, so weapons on the back/hip keep their original size and orientation while
     * item frames keep their new look. Values are in 1/16 block units.
     */
    private static final Map<String, Float> HOLSTER_X_OFFSET = new HashMap<>();

    static
    {
        HOLSTER_X_OFFSET.put("assault_rifle", -7.0F);
        HOLSTER_X_OFFSET.put("rifle", -0.5F);
        HOLSTER_X_OFFSET.put("shotgun", -0.5F);
        HOLSTER_X_OFFSET.put("bazooka", -0.5F);
        HOLSTER_X_OFFSET.put("grenade_launcher", -7.0F);
        HOLSTER_X_OFFSET.put("chain_gun", -0.5F);
        HOLSTER_X_OFFSET.put("handgun", -9.5F);
        HOLSTER_X_OFFSET.put("machine_pistol", -7.0F);
    }
    public static boolean drawFlash = false;
    public static float flashRandom = 0.0F;
    public static int screenTextureId = -1;
    public static boolean shadersEnabled;

    /* Single instance access used by static callbacks (e.g. the shot-fired hook) */
    public static RenderEvents INSTANCE;

    public RenderEvents()
    {
        INSTANCE = this;
    }

    /**
     * Called by the client when a shot is fired. Resets the sprint animation
     * transition and starts the sprint cooldown, exactly like the 1.19 onGunFire.
     */
    public static void onGunFired()
    {
        if(INSTANCE != null)
        {
            INSTANCE.sprintTransition = 0;
            INSTANCE.sprintCooldown = 20; /* TODO make a config option */
        }
    }

    /**
     * @return the current normalised ads zoom progress (0.0 - 1.0)
     */
    public static double getNormalZoomProgress()
    {
        return INSTANCE != null ? INSTANCE.normalZoomProgress : 0.0;
    }

    private int zoomProgress;
    private int lastZoomProgress;
    public double normalZoomProgress;
    public double recoilNormal;
    public double recoilAngle;

    /* -- Ported 1.19 movement animation state -- */

    /* Sprinting animation transition (0 - 5) and shot cooldown */
    private int sprintTransition;
    private int prevSprintTransition;
    private int sprintCooldown;
    private float sprintIntensity = 1.0F;

    /* Immersive camera roll and fall sway state */
    private float immersiveRoll;
    private float prevImmersiveRoll;
    private float fallSway;
    private float prevFallSway;

    /* Smoothed head rotation used as the sway lag reference */
    private float bobPitch;
    private float prevBobPitch;
    private float bobYaw;
    private float prevBobYaw;

    public boolean playAnimation;
    private int startTick;
    private int reloadTimer;
    private int prevReloadTimer;
    private boolean lastSmoothCamera = Minecraft.getMinecraft().gameSettings.smoothCamera;

    private ItemStack flash = null;

    private List<Bullet> bullets = new ArrayList<>();

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if(event.phase == TickEvent.Phase.START && event.player.world.isRemote)
        {
            getCooldownTracker(event.player.getUniqueID()).tick();
        }
    }

    @SubscribeEvent
    public void onKeyPressedEvent(KeyInputEvent event)
    {
        if(Minecraft.getMinecraft().gameSettings.keyBindSmoothCamera.isPressed())
        {
            Minecraft.getMinecraft().gameSettings.smoothCamera = !Minecraft.getMinecraft().gameSettings.smoothCamera;
            lastSmoothCamera = Minecraft.getMinecraft().gameSettings.smoothCamera;
        }
    }

    @SubscribeEvent
    public void onFovUpdate(FOVUpdateEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(!mc.player.getHeldItemMainhand().isEmpty() && mc.gameSettings.thirdPersonView == 0)
        {
            ItemStack heldItem = mc.player.getHeldItemMainhand();
            if(heldItem.getItem() instanceof ItemGun)
            {
                ItemStack scope = Gun.getScope(heldItem);

                ItemGun gun = (ItemGun) heldItem.getItem();
                if(isZooming(Minecraft.getMinecraft().player) && !mc.player.getDataManager().get(CommonEvents.RELOADING))
                {
                    mc.gameSettings.smoothCamera = gun.getGun().modules.zoom.smooth;
                    float newFov = gun.getGun().modules.zoom.fovModifier - 0.1F;
                    if(scope != null)
                    {
                        Scope scopeProps = Scope.getScopeFromStack(scope);
                        if(scopeProps != null)
                        {
                            newFov -= scopeProps.getAdditionalZoom();
                            mc.gameSettings.smoothCamera = gun.getGun().modules.attachments.scope.smooth;
                            if(scopeProps.getFovModifier() <= 0.3F)
                            {
                                mc.gameSettings.smoothCamera = true;
                            }
                        }
                    }
                    event.setNewfov(newFov + (1.0F - newFov) * (1.0F - (zoomProgress / (float) ZOOM_TICKS)));
                }
                else
                {
                    mc.gameSettings.smoothCamera = lastSmoothCamera;
                }
            }
            else
            {
                mc.gameSettings.smoothCamera = lastSmoothCamera;
            }
        }
        else
        {
            mc.gameSettings.smoothCamera = lastSmoothCamera;
        }
    }

    @SubscribeEvent
    public void onPreClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase != TickEvent.Phase.START)
            return;

        lastZoomProgress = zoomProgress;

        EntityPlayer player = Minecraft.getMinecraft().player;
        if(isZooming(player) && !player.getDataManager().get(CommonEvents.RELOADING))
        {
            if(zoomProgress < ZOOM_TICKS)
            {
                zoomProgress++;
            }
        }
        else
        {
            if(zoomProgress > 0)
            {
                zoomProgress--;
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase != TickEvent.Phase.END)
            return;

        prevReloadTimer = reloadTimer;

        EntityPlayer player = Minecraft.getMinecraft().player;
        if(isZooming(player) && !player.getDataManager().get(CommonEvents.RELOADING))
        {
            Minecraft.getMinecraft().player.prevCameraYaw = 0.0075F;
            Minecraft.getMinecraft().player.cameraYaw = 0.0075F;
        }

        if(player != null)
        {
            ItemStack heldItem = player.getHeldItemMainhand();
            if(!heldItem.isEmpty() && heldItem.getItem() instanceof ItemGun)
            {
                IOverrideModel model = ModelOverrides.getModel(heldItem);
                if(model != null)
                {
                    model.tick(player);
                }
            }

            if(player.getDataManager().get(CommonEvents.RELOADING))
            {
                if(startTick == -1)
                {
                    startTick = player.ticksExisted + 5;
                }

                if(reloadTimer < 5)
                {
                    reloadTimer++;
                }
            }
            else
            {
                playAnimation = false;

                if(startTick != -1)
                {
                    startTick = -1;
                }

                if(reloadTimer > 0)
                {
                    reloadTimer -= 1;
                }
            }

            /* Updates the sprinting and immersive camera animations */
            this.updateSprinting();
            this.updateImmersiveCamera();

            /* Smooths the head rotation and walk speed for the sway and bobbing animations */
            this.prevBobPitch = this.bobPitch;
            this.bobPitch += MathHelper.wrapDegrees(player.rotationPitch - this.bobPitch) * 0.5F;
            this.prevBobYaw = this.bobYaw;
            this.bobYaw += MathHelper.wrapDegrees(player.rotationYawHead - this.bobYaw) * 0.5F;
            /* Keep the smoothed yaw bounded to (-180, 180]. Because the increment above uses
             * wrapDegrees on the *difference*, the value would otherwise converge to any angle
             * congruent with the head yaw. After the player crosses the +/-180 boundary it can
             * settle a full 360 away, making headYaw - bobYaw equal +/-360 and permanently
             * skewing the whole weapon + arms assembly. See applySwayTransforms. */
            this.bobYaw = MathHelper.wrapDegrees(this.bobYaw);
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent event)
    {
        normalZoomProgress = (lastZoomProgress + (zoomProgress - lastZoomProgress) * (lastZoomProgress == 0 || lastZoomProgress == ZOOM_TICKS ? 0.0 : event.getPartialTicks())) / ZOOM_TICKS;
        if(normalZoomProgress > 0 && event.getType() == RenderGameOverlayEvent.ElementType.CROSSHAIRS)
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderSpecificHandEvent event)
    {
        boolean right = Minecraft.getMinecraft().gameSettings.mainHand == EnumHandSide.RIGHT ? event.getHand() == EnumHand.MAIN_HAND : event.getHand() == EnumHand.OFF_HAND;
        ItemStack heldItem = event.getItemStack();

        if(event.getHand() == EnumHand.OFF_HAND)
        {
            ItemStack mainHandStack = Minecraft.getMinecraft().player.getHeldItemMainhand();
            if(mainHandStack.getItem() instanceof ItemGun)
            {
                if(((ItemGun) mainHandStack.getItem()).getGun().general.gripType != GripType.ONE_HANDED)
                {
                    event.setCanceled(true);
                    return;
                }
            }
        }

        if(!(heldItem.getItem() instanceof ItemGun))
            return;

        // Cancel it because we are doing our own custom render
        event.setCanceled(true);

        // Ignores rendering the gun if the grip type doesn't allow it to be render in the offhand
        if(event.getHand() == EnumHand.OFF_HAND)
        {
            return;
        }

        ItemStack scope = Gun.getScope(heldItem);
        Scope scopeProps = Scope.getScopeFromStack(scope);
        IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(heldItem);
        float scaleX = model.getItemCameraTransforms().firstperson_right.scale.getX();
        float scaleY = model.getItemCameraTransforms().firstperson_right.scale.getY();
        float scaleZ = model.getItemCameraTransforms().firstperson_right.scale.getZ();
        float translateY = model.getItemCameraTransforms().firstperson_right.translation.getY() * scaleY;
        float translateZ = model.getItemCameraTransforms().firstperson_right.translation.getZ() * scaleZ;

        GlStateManager.pushMatrix();
        if(normalZoomProgress > 0)
        {
            ItemGun itemGun = (ItemGun) heldItem.getItem();
            if(event.getHand() == EnumHand.MAIN_HAND)
            {
                double xOffset = 0.0;
                double yOffset = 0.0;
                double zOffset = 0.0;

                Gun gun = itemGun.getGun();
                if(gun.canAttachType(IAttachment.Type.SCOPE) && scope != null && scopeProps != null)
                {
                    Gun.ScaledPositioned scaledPos = gun.modules.attachments.scope;
                    xOffset -= scaledPos.xOffset * 0.0625 * scaleX;
                    yOffset -= scaledPos.yOffset * 0.0625 * scaleY - translateY + scopeProps.getHeightToCenter() * scaleY * 0.0625 * scaledPos.scale;
                    zOffset -= scaledPos.zOffset * 0.0625 * scaleZ - translateZ - 0.35;
                }
                else if(gun.modules.zoom != null)
                {
                    xOffset -= gun.modules.zoom.xOffset * 0.0625 * scaleX;
                    yOffset -= gun.modules.zoom.yOffset * 0.0625 * scaleY - translateY;
                    zOffset -= gun.modules.zoom.zOffset * 0.0625 * scaleZ - translateZ;
                }
                double renderOffset = right ? -0.3415 : -0.3775;
                /* Eases the zoom movement using the 1.19 default sight curve */
                double time = Easings.EASE_OUT_QUAD.apply(this.normalZoomProgress);
                GlStateManager.translate((renderOffset + xOffset) * time * (right ? 1F : -1F), yOffset * time, zOffset * time);
            }
            else
            {
                GlStateManager.translate(0, -1 * normalZoomProgress, 0);
            }
        }

        /* Applies custom bobbing animations to the weapon (reverses the camera bob first) */
        EntityPlayer player = Minecraft.getMinecraft().player;
        this.applyBobbingTransforms(player, event.getPartialTicks());

        GlStateManager.translate(0, -event.getEquipProgress(), 0);

        EnumHandSide hand = right ? EnumHandSide.RIGHT : EnumHandSide.LEFT;

        /* Renders the reload arm. Will only render if actually reloading. This is applied before
         * any recoil or reload rotations as the animations would be borked if applied after. */
        renderReloadArm(heldItem, hand);

        /* Translate the item position based on the hand side */
        GlStateManager.translate(0.5602F - (right ? 0.0F : 0.72F), -0.55625F, -0.72F);

        /* Moves the weapon higher and closer to the body while idle. Fades out while
         * aiming/zooming (normalZoomProgress -> 1) so the scope/iron sight position
         * stays exactly centered during ADS. */
        GlStateManager.translate(0, (1.0 - this.normalZoomProgress) * 0.06F, (1.0 - this.normalZoomProgress) * 0.08F);

        /* Pivot values match the model's own first person translation so the weapon
         * tilts around its own centre during aiming and swaying */
        float pivotX = model.getItemCameraTransforms().firstperson_right.translation.getX() * 0.0625F * scaleX;
        float pivotY = model.getItemCameraTransforms().firstperson_right.translation.getY() * 0.0625F * scaleY;
        float pivotZ = model.getItemCameraTransforms().firstperson_right.translation.getZ() * 0.0625F * scaleZ;

        Gun gun = ((ItemGun) heldItem.getItem()).getModifiedGun(heldItem);
        int offset = right ? 1 : -1;

        /* Applies all the 1.19 movement animations: aiming, swaying, sprinting, recoil and reload */
        this.applyAimingTransforms(pivotX, pivotY, pivotZ, offset);
        this.applySwayTransforms(gun, player, pivotX, pivotY, pivotZ, event.getPartialTicks());
        this.applySprintingTransforms(gun, right, event.getPartialTicks());
        this.applyRecoilTransforms(heldItem, gun);
        this.applyReload(event.getPartialTicks());

        /* Render offhand arm so it is holding the weapon. Only applies if it's a two handed weapon */
        GlStateManager.pushMatrix();
        GlStateManager.translate(-(0.56F - (right ? 0.0F : 0.72F)), 0.56F, 0.72F);
        renderHeldArm(heldItem, hand, event.getPartialTicks());
        GlStateManager.popMatrix();

        /* Renders the weapon */
        this.renderWeapon(Minecraft.getMinecraft().player, heldItem, ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND, event.getPartialTicks());

        GlStateManager.popMatrix();
    }

    private void applyReload(float partialTicks)
    {
        float reloadProgress = (prevReloadTimer + (reloadTimer - prevReloadTimer) * partialTicks) / 5F;
        GlStateManager.translate(0, 0.35 * reloadProgress, 0);
        GlStateManager.translate(0, 0, -0.1 * reloadProgress);
        GlStateManager.rotate(45F * reloadProgress, 1, 0, 0);
    }

    private void applyRecoilTransforms(ItemStack item, Gun gun)
    {
        double recoilNormal = RecoilHandler.get().getGunRecoilNormal();

        /* Keeps the fields used by the scope models (ModelLongScope/ModelMediumScope) updated */
        this.recoilNormal = recoilNormal;
        this.recoilAngle = RecoilHandler.get().getGunRecoilAngle();

        ItemStack scope = Gun.getScope(item);
        if(gun.canAttachType(IAttachment.Type.SCOPE) && scope != null && !scope.isEmpty())
        {
            recoilNormal -= recoilNormal * (0.5 * this.normalZoomProgress);
        }

        float kickReduction = 1.0F; /* 1.12.2 has no GunModifierHelper */
        float recoilReduction = 1.0F;
        double kick = gun.general.recoilKick * 0.0625 * recoilNormal * RecoilHandler.get().getAdsRecoilReduction(gun);
        float recoilLift = (float) (gun.general.recoilAngle * recoilNormal) * (float) RecoilHandler.get().getAdsRecoilReduction(gun);
        float recoilSwayAmount = (float) (2F + 1F * (1.0 - this.normalZoomProgress));
        float recoilSway = (float) ((RecoilHandler.get().getGunRecoilRandom() * recoilSwayAmount - recoilSwayAmount / 2F) * recoilNormal);

        GlStateManager.translate(0, 0, kick * kickReduction);
        GlStateManager.translate(0, 0, 0.15);
        GlStateManager.rotate(recoilSway * recoilReduction, 0, 1, 0);
        GlStateManager.rotate(recoilSway * recoilReduction, 0, 0, 1);
        GlStateManager.rotate(recoilLift * recoilReduction, 1, 0, 0);
        GlStateManager.translate(0, 0, -0.15);
    }

    /**
     * Reverses the vanilla camera bob and applies the 1.19 weapon bobbing instead.
     * Ported 1:1 from 1.19 applyBobbingTransforms: the reversal multiplies the small
     * bobbing amplitude (bounded 0..0.5) inside sin/cos, never the raw accumulated
     * walk distance, so the weapon never drifts sideways.
     */
    private void applyBobbingTransforms(EntityPlayer player, float partialTicks)
    {
        if(Minecraft.getMinecraft().gameSettings.viewBobbing && player != null)
        {
            float deltaDistanceWalked = player.distanceWalkedModified - player.prevDistanceWalkedModified;
            float distanceWalked = -(player.distanceWalkedModified + deltaDistanceWalked * partialTicks);

            /* Reverses the vanilla camera bob inherited by the hand. The vanilla bob
             * (EntityRenderer.setupViewBobbing) is driven by cameraYaw (<= 0.1) and
             * cameraPitch, not by the raw walk speed, so the reversal must use those
             * same amplitudes to fully cancel it (this is what 1.19's player.bob is). */
            float bobbing = lerp(partialTicks, player.prevCameraYaw, player.cameraYaw);
            float pitchBob = lerp(partialTicks, player.prevCameraPitch, player.cameraPitch);
            GlStateManager.rotate(-pitchBob, 1, 0, 0);
            GlStateManager.rotate(-(MathHelper.abs(MathHelper.cos(distanceWalked * (float) Math.PI - 0.2F) * bobbing) * 5.0F), 1, 0, 0);
            GlStateManager.rotate(-(MathHelper.sin(distanceWalked * (float) Math.PI) * bobbing * 3.0F), 0, 0, 1);
            GlStateManager.translate(-(MathHelper.sin(distanceWalked * (float) Math.PI) * bobbing * 0.5F), -(-MathHelper.abs(MathHelper.cos(distanceWalked * (float) Math.PI) * bobbing)), 0);

            /* The new controlled bobbing */
            bobbing *= player.isSprinting() ? 8.0F : 4.0F;
            bobbing *= (float) GunConfig.CLIENT.display.bobbingIntensity;

            double invertZoomProgress = 1.0 - this.normalZoomProgress * this.sprintIntensity;
            GlStateManager.rotate((float) (Math.sin(distanceWalked * Math.PI) * bobbing * 3.0F) * (float) invertZoomProgress, 0, 0, 1);
            GlStateManager.rotate((float) (Math.abs(Math.cos(distanceWalked * Math.PI - 0.2F) * bobbing) * 5.0F) * (float) invertZoomProgress, 1, 0, 0);
        }
    }

    /**
     * Applies the tilting animation while aiming down sights. Identity when not aiming.
     */
    private void applyAimingTransforms(float x, float y, float z, int offset)
    {
        GlStateManager.translate(x * offset, y, z);
        GlStateManager.translate(0, -0.25, 0.25);
        float aiming = (float) Math.sin(Math.toRadians(this.normalZoomProgress * 180.0));
        aiming = Easings.EASE_IN_QUAD.apply(aiming);
        GlStateManager.rotate(aiming * 10F * offset, 0, 0, 1);
        GlStateManager.rotate(aiming * 5F, 1, 0, 0);
        GlStateManager.rotate(aiming * 5F * offset, 0, 1, 0);
        GlStateManager.translate(0, 0.25, -0.25);
        GlStateManager.translate(-x * offset, -y, -z);
    }

    /**
     * Applies the weapon sway based on head movement and fall sway. Identity when idle.
     */
    private void applySwayTransforms(Gun gun, EntityPlayer player, float x, float y, float z, float partialTicks)
    {
        if(!GunConfig.CLIENT.display.weaponSway || player == null)
            return;

        GlStateManager.translate(x, y, z);

        double zOffset = gun.general.gripType == GripType.ONE_HANDED ? 0.5 : 0.35;
        GlStateManager.translate(0, -0.25, zOffset);
        GlStateManager.rotate(lerp(partialTicks, this.prevFallSway, this.fallSway), 1, 0, 0);
        GlStateManager.translate(0, 0.25, -zOffset);

        float bobPitch = rotLerp(partialTicks, this.prevBobPitch, this.bobPitch);
        float headPitch = rotLerp(partialTicks, player.prevRotationPitch, player.rotationPitch);
        float swayPitch = MathHelper.wrapDegrees(headPitch - bobPitch);
        swayPitch *= (float) (1.0 - 0.5 * this.normalZoomProgress);
        GlStateManager.rotate(swayPitch * (float) GunConfig.CLIENT.display.swaySensitivity * GunConfig.CLIENT.display.getSwayType().getPitchDirection(), 1, 0, 0);

        float bobYaw = rotLerp(partialTicks, this.prevBobYaw, this.bobYaw);
        float headYaw = rotLerp(partialTicks, player.prevRotationYawHead, player.rotationYawHead);
        /* Always take the shortest angular difference: headYaw and bobYaw can otherwise sit on
         * opposite sides of the +/-180 boundary and differ by a full 360, which rotates the
         * entire first person assembly by ~108 degrees (360 * swaySensitivity). */
        float swayYaw = MathHelper.wrapDegrees(headYaw - bobYaw);
        swayYaw *= (float) (1.0 - 0.5 * this.normalZoomProgress);
        GlStateManager.rotate(swayYaw * (float) GunConfig.CLIENT.display.swaySensitivity * GunConfig.CLIENT.display.getSwayType().getYawDirection(), 0, 1, 0);

        GlStateManager.translate(-x, -y, -z);
    }

    /**
     * Applies the sprinting animation. Only two handed weapons tilt while sprinting in 1.19.
     */
    private void applySprintingTransforms(Gun gun, boolean right, float partialTicks)
    {
        if(GunConfig.CLIENT.display.sprintAnimation && gun.general.gripType == GripType.TWO_HANDED)
        {
            float leftHanded = right ? 1 : -1;
            float transition = (this.prevSprintTransition + (this.sprintTransition - this.prevSprintTransition) * partialTicks) / 5F;
            transition = (float) Math.sin((transition * Math.PI) / 2);
            GlStateManager.translate(-0.25F * leftHanded * transition, -0.1F * transition, 0);
            GlStateManager.rotate(45F * leftHanded * transition, 0, 1, 0);
            GlStateManager.rotate(-25F * transition, 1, 0, 0);
        }
    }

    /**
     * Updates the sprinting animation transition. Ported from 1.19: sprinting only
     * while actually sprinting, not shooting, not reloading, not aiming and while
     * the sprint cooldown after a shot has expired.
     */
    private void updateSprinting()
    {
        this.prevSprintTransition = this.sprintTransition;
        Minecraft mc = Minecraft.getMinecraft();
        if(mc.player != null && mc.player.isSprinting() && !this.isHoldingShooting(mc.player) && !mc.player.getDataManager().get(CommonEvents.RELOADING) && !this.isZooming(mc.player) && this.sprintCooldown == 0)
        {
            if(this.sprintTransition < 5)
                this.sprintTransition++;
        }
        else if(this.sprintTransition > 0)
        {
            this.sprintTransition--;
        }
        if(this.sprintCooldown > 0)
            this.sprintCooldown--;
    }

    /**
     * 1.12.2 has no SHOOTING data key, so the mod's own cooldown tracker is used as
     * a proxy to determine if the player is actively shooting.
     */
    private boolean isHoldingShooting(EntityPlayer player)
    {
        ItemStack heldItem = player.getHeldItemMainhand();
        if(heldItem.isEmpty() || !(heldItem.getItem() instanceof ItemGun))
            return false;
        return getCooldownTracker(player.getUniqueID()).hasCooldown(heldItem.getItem());
    }

    /**
     * Updates the immersive camera roll and fall sway. Ported from 1.19.
     */
    private void updateImmersiveCamera()
    {
        this.prevImmersiveRoll = this.immersiveRoll;
        this.prevFallSway = this.fallSway;

        Minecraft mc = Minecraft.getMinecraft();
        if(mc.player == null)
            return;

        EntityPlayer player = mc.player;
        float strafe = player instanceof EntityPlayerSP ? ((EntityPlayerSP) player).movementInput.moveStrafe : 0F;
        float targetAngle = player.getHeldItemMainhand().getItem() instanceof ItemGun || !GunConfig.CLIENT.display.restrictCameraRollToWeapons ? strafe : 0F;
        float speed = strafe != 0 ? 0.1F : 0.15F;
        this.immersiveRoll = lerp(speed, this.immersiveRoll, targetAngle);

        float deltaY = MathHelper.clamp((float) (player.lastTickPosY - player.posY), -1.0F, 1.0F);
        deltaY *= (float) (1.0 - this.normalZoomProgress);
        deltaY *= (float) (1.0 - (Math.abs(player.rotationPitch) / 90.0F));
        this.fallSway = approach(this.fallSway, deltaY * 60F * (float) GunConfig.CLIENT.display.swaySensitivity, 10.0F);

        float intensity = player.isSprinting() ? 0.75F : 1.0F;
        this.sprintIntensity = approach(this.sprintIntensity, intensity, 0.1F);
    }

    /**
     * Applies the camera roll effect when moving side to side. Ported from 1.19,
     * which uses the camera setup event to apply the roll.
     */
    @SubscribeEvent
    public void onCameraSetup(EntityViewRenderEvent.CameraSetup event)
    {
        if(GunConfig.CLIENT.display.cameraRollEffect)
        {
            float roll = lerp((float) event.getRenderPartialTicks(), this.prevImmersiveRoll, this.immersiveRoll);
            roll = (float) Math.sin((roll * Math.PI) / 2.0);
            roll *= (float) GunConfig.CLIENT.display.cameraRollAngle;
            event.setRoll(-roll);
        }
    }

    private static float rotLerp(float delta, float start, float end)
    {
        return start + delta * MathHelper.wrapDegrees(end - start);
    }

    private static float lerp(float delta, float start, float end)
    {
        return start + delta * (end - start);
    }

    private static float approach(float current, float target, float maxIncrease)
    {
        if(current < target)
            return Math.min(current + maxIncrease, target);
        if(current > target)
            return Math.max(current - maxIncrease, target);
        return target;
    }

    private boolean isZooming(EntityPlayer player)
    {
        if(player != null && player.getHeldItemMainhand() != ItemStack.EMPTY)
        {
            ItemStack stack = player.getHeldItemMainhand();
            if(stack.getItem() instanceof ItemGun)
            {
                Gun gun = ((ItemGun) stack.getItem()).getGun();
                return gun.modules != null && gun.modules.zoom != null && MrCrayfishGunMod.proxy.isZooming();
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onTick(TickEvent.RenderTickEvent event)
    {
        if(event.phase.equals(TickEvent.Phase.START))
            return;

        Minecraft mc = Minecraft.getMinecraft();
        if(!mc.inGameHasFocus)
            return;

        EntityPlayer player = mc.player;
        if(player == null)
            return;

        if(Minecraft.getMinecraft().gameSettings.thirdPersonView != 0)
            return;

        ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
        if(heldItem.isEmpty() || !(heldItem.getItem() instanceof ItemGun))
            return;

        ScaledResolution scaledResolution = new ScaledResolution(mc);

        if(!heldItem.isEmpty() && heldItem.getItem() instanceof ItemGun)
        {
            Scope scopeProps = Scope.getScopeFromStack(Gun.getAttachment(IAttachment.Type.SCOPE, heldItem));
            if(scopeProps != null && scopeProps.getFovModifier() <= 0.3F && normalZoomProgress == 1.0)
            {
                if(true) return;

                mc.getTextureManager().bindTexture(SCOPE_OVERLAY);
                GlStateManager.color(1.0F, 1.0F, 1.0F);
                GlStateManager.enableBlend();
                GlStateManager.enableAlpha();
                GlStateManager.disableDepth();

                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();
                buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
                buffer.pos(0, scaledResolution.getScaledHeight(), 0).tex(0, 1).endVertex();
                buffer.pos(scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight(), 0).tex(1, 1).endVertex();
                buffer.pos(scaledResolution.getScaledWidth(), 0, 0).tex(1, 0).endVertex();
                buffer.pos(0, 0, 0).tex(0, 0).endVertex();
                tessellator.draw();

                GlStateManager.disableAlpha();
                GlStateManager.disableBlend();
                GlStateManager.enableDepth();
            }

            Gun gun = ((ItemGun) heldItem.getItem()).getGun();
            if(!gun.general.auto)
            {
                float coolDown = player.getCooldownTracker().getCooldown(heldItem.getItem(), event.renderTickTime);
                if(coolDown > 0.0F)
                {
                    double scale = 3;
                    int i = (int) ((scaledResolution.getScaledHeight() / 2 - 7 - 60) / scale);
                    int j = (int) Math.ceil((scaledResolution.getScaledWidth() / 2 - 8 * scale) / scale);

                    GlStateManager.enableAlpha();
                    mc.getTextureManager().bindTexture(Gui.ICONS);

                    GlStateManager.pushMatrix();
                    {
                        GlStateManager.scale(scale, scale, scale);
                        int progress = (int) Math.ceil((coolDown + 0.05) * 17.0F) - 1;
                        GuiScreen.drawModalRectWithCustomSizedTexture(j, i, 36, 94, 16, 4, 256, 256);
                        GuiScreen.drawModalRectWithCustomSizedTexture(j, i, 52, 94, progress, 4, 256, 256);
                    }
                    GlStateManager.popMatrix();
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderHeldItem(RenderItemEvent.Held.Pre event)
    {
        EnumHand hand = Minecraft.getMinecraft().gameSettings.mainHand == EnumHandSide.RIGHT ? event.getHandSide() == EnumHandSide.RIGHT ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND : event.getHandSide() == EnumHandSide.LEFT ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
        EntityLivingBase entity = event.getEntity();
        ItemStack heldItem = entity.getHeldItem(hand);

        if(heldItem.getItem() instanceof ItemGun)
        {
            event.setCanceled(true);
            Gun gun = ((ItemGun) heldItem.getItem()).getGun();
            gun.general.gripType.getHeldAnimation().applyHeldItemTransforms(hand, entity instanceof EntityPlayer ? GunHandler.getAimProgress((EntityPlayer) entity, event.getPartialTicks()) : 0f);
            if(hand == EnumHand.MAIN_HAND)
            {
                this.renderWeapon(entity, heldItem, event.getTransformType(), event.getPartialTicks());
            }
        }

        if(hand == EnumHand.OFF_HAND)
        {
            ItemStack mainHandStack = entity.getHeldItemMainhand();
            if(!mainHandStack.isEmpty() && mainHandStack.getItem() instanceof ItemGun)
            {
                Gun mainHandGun = ((ItemGun) mainHandStack.getItem()).getGun();
                if(!mainHandGun.general.gripType.canRenderOffhand())
                {
                    event.setCanceled(true);
                }
                else if(heldItem.getItem() instanceof ItemGun)
                {
                    Gun gun = ((ItemGun) heldItem.getItem()).getGun();
                    if(gun.general.gripType.canRenderOffhand())
                    {
                        this.renderWeapon(entity, heldItem, event.getTransformType(), event.getPartialTicks());
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onSetupAngles(ModelPlayerEvent.SetupAngles.Post event)
    {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack heldItem = player.getHeldItemMainhand();
        if(!heldItem.isEmpty() && heldItem.getItem() instanceof ItemGun)
        {
            ModelPlayer model = event.getModelPlayer();
            Gun gun = ((ItemGun) heldItem.getItem()).getGun();
            gun.general.gripType.getHeldAnimation().applyPlayerModelRotation(model, EnumHand.MAIN_HAND, GunHandler.getAimProgress((EntityPlayer) event.getEntity(), event.getPartialTicks()));
            copyModelAngles(model.bipedRightArm, model.bipedRightArmwear);
            copyModelAngles(model.bipedLeftArm, model.bipedLeftArmwear);
        }
    }

    @SubscribeEvent
    public void onRenderPlayer(RenderPlayerEvent.Pre event)
    {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack heldItem = player.getHeldItemMainhand();
        if(!heldItem.isEmpty() && heldItem.getItem() instanceof ItemGun)
        {
            Gun gun = ((ItemGun) heldItem.getItem()).getGun();
            gun.general.gripType.getHeldAnimation().applyPlayerPreRender(player, EnumHand.MAIN_HAND, GunHandler.getAimProgress((EntityPlayer) event.getEntity(), event.getPartialRenderTick()));
        }
    }

    @SubscribeEvent
    public void onModelRender(ModelPlayerEvent.Render.Pre event)
    {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack offHandStack = player.getHeldItemOffhand();
        if(offHandStack.getItem() instanceof ItemGun)
        {
            switch(player.getPrimaryHand().opposite())
            {
                case LEFT:
                    event.getModelPlayer().leftArmPose = ModelBiped.ArmPose.EMPTY;
                    break;
                case RIGHT:
                    event.getModelPlayer().rightArmPose = ModelBiped.ArmPose.EMPTY;
                    break;
            }
        }
    }

    @SubscribeEvent
    public void onRenderPlayer(ModelPlayerEvent.Render.Post event)
    {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack heldItem = player.getHeldItemOffhand();
        if(!heldItem.isEmpty() && heldItem.getItem() instanceof ItemGun)
        {
            Gun gun = ((ItemGun) heldItem.getItem()).getGun();
            if(!gun.general.gripType.canRenderOffhand())
            {
                GlStateManager.pushMatrix();
                GlStateManager.rotate(180F, 0, 1, 0);
                GlStateManager.rotate(180F, 0, 0, 1);
                if(player.isSneaking())
                {
                    GlStateManager.translate(0 * 0.0625, -7 * 0.0625, -5 * 0.0625);
                    GlStateManager.rotate(30F, 1, 0, 0);
                }
                else
                {
                    GlStateManager.translate(0 * 0.0625, -5 * 0.0625, -2.75 * 0.0625);
                }
                GlStateManager.rotate(-45F, 0, 0, 1);
                GlStateManager.scale(0.5, 0.5, 0.5);
                /* The model's "fixed" transform is now used for item frames and no longer
                 * matches the holster, so apply the original holster transform here and
                 * render without reading the JSON transform to restore the weapon size. */
                applyHolsterTransform(gun.id);
                this.renderWeapon(player, heldItem, ItemCameraTransforms.TransformType.NONE, event.getPartialTicks());
                GlStateManager.popMatrix();
            }
            else
            {
                GlStateManager.pushMatrix();
                GlStateManager.rotate(180F, 0, 1, 0);
                GlStateManager.rotate(180F, 0, 0, 1);
                if(player.isSneaking())
                {
                    GlStateManager.translate(-4.5 * 0.0625, -15 * 0.0625, -4 * 0.0625);
                }
                else
                {
                    GlStateManager.translate(-4.5 * 0.0625, -13 * 0.0625, 1 * 0.0625);
                }
                GlStateManager.rotate(90F, 0, 1, 0);
                GlStateManager.rotate(75F, 0, 0, 1);
                GlStateManager.rotate((float) Math.toDegrees(event.getModelPlayer().bipedRightLeg.rotateAngleX) / 10F, 0, 0, 1);
                GlStateManager.scale(0.5, 0.5, 0.5);
                applyHolsterTransform(gun.id);
                this.renderWeapon(player, heldItem, ItemCameraTransforms.TransformType.NONE, event.getPartialTicks());
                GlStateManager.popMatrix();
            }
        }
    }

    @SubscribeEvent
    public void onRenderEntityItem(RenderItemEvent.Entity.Pre event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        event.setCanceled(this.renderWeapon(mc.player, event.getItem(), event.getTransformType(), event.getPartialTicks()));
    }

    @SubscribeEvent
    public void onRenderEntityItem(RenderItemEvent.Gui.Pre event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        event.setCanceled(this.renderWeapon(mc.player, event.getItem(), event.getTransformType(), event.getPartialTicks()));
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
    }

    /**
     * Applies the legacy holster transform that used to come from the item model's
     * "fixed" display transform. See {@link RenderEvents#HOLSTER_X_OFFSET}.
     */
    private static void applyHolsterTransform(String gunId)
    {
        float x = -7.0F;
        if(gunId != null && HOLSTER_X_OFFSET.containsKey(gunId))
        {
            x = HOLSTER_X_OFFSET.get(gunId);
        }
        GlStateManager.translate(x * 0.0625F, 10 * 0.0625F, 0F);
        GlStateManager.rotate(90F, 0, 1, 0);
        GlStateManager.scale(2F, 2F, 2F);
    }

    private boolean renderWeapon(EntityLivingBase entity, ItemStack stack, ItemCameraTransforms.TransformType transformType, float partialTicks)
    {
        if(stack.getItem() instanceof ItemGun)
        {
            GlStateManager.pushMatrix();

            ItemStack model = ItemStack.EMPTY;
            if(stack.getTagCompound() != null)
            {
                if(stack.getTagCompound().hasKey("model", Constants.NBT.TAG_COMPOUND))
                {
                    model = new ItemStack(stack.getTagCompound().getCompoundTag("model"));
                    model.setTagCompound(stack.getTagCompound().copy());
                }
            }

            RenderUtil.applyTransformType(model.isEmpty() ? stack : model, transformType);

            if(transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND)
            {
                this.renderMuzzleFlash(stack);
            }

            this.renderGun(entity, transformType, model.isEmpty() ? stack : model, partialTicks);
            this.renderAttachments(entity, transformType, stack, partialTicks);

            GlStateManager.popMatrix();
            return true;
        }
        return false;
    }

    private void renderGun(EntityLivingBase entity, ItemCameraTransforms.TransformType transformType, ItemStack stack, float partialTicks)
    {
        if(ModelOverrides.hasModel(stack))
        {
            IOverrideModel model = ModelOverrides.getModel(stack);
            if(model != null)
            {
                model.render(partialTicks, transformType, stack, ItemStack.EMPTY, entity);
            }
        }
        else
        {
            RenderUtil.renderModel(stack);
        }
    }

    private void renderAttachments(EntityLivingBase entity, ItemCameraTransforms.TransformType transformType, ItemStack stack, float partialTicks)
    {
        if(stack.getItem() instanceof ItemGun)
        {
            Gun gun = ((ItemGun) stack.getItem()).getGun();
            NBTTagCompound gunTag = ItemStackUtil.createTagCompound(stack);
            // Check for "Attachments" (capital A) first for compatibility with 1.19.X
            NBTTagCompound attachments = null;
            if(gunTag.hasKey("Attachments", Constants.NBT.TAG_COMPOUND))
            {
                attachments = gunTag.getCompoundTag("Attachments");
            }
            // Fallback to "attachments" (lowercase) for backward compatibility
            else if(gunTag.hasKey("attachments", Constants.NBT.TAG_COMPOUND))
            {
                attachments = gunTag.getCompoundTag("attachments");
            }
            
            if(attachments != null)
            {
                for(String attachmentKey : attachments.getKeySet())
                {
                    IAttachment.Type type = IAttachment.Type.getType(attachmentKey);
                    if(gun.canAttachType(type))
                    {
                        ItemStack attachmentStack = Gun.getAttachment(type, stack);
                        if(!attachmentStack.isEmpty())
                        {
                            GlStateManager.pushMatrix();
                            {
                                Gun.ScaledPositioned positioned = gun.getAttachmentPosition(type);
                                if(positioned != null)
                                {
                                    double displayX = positioned.xOffset * 0.0625;
                                    double displayY = positioned.yOffset * 0.0625;
                                    double displayZ = positioned.zOffset * 0.0625;
                                    GlStateManager.translate(displayX, displayY, displayZ);
                                    GlStateManager.translate(0, -0.5, 0);
                                    GlStateManager.scale(positioned.scale, positioned.scale, positioned.scale);

                                    IOverrideModel model = ModelOverrides.getModel(attachmentStack);
                                    if(model != null)
                                    {
                                        model.render(partialTicks, transformType, attachmentStack, stack, entity);
                                    }
                                    else
                                    {
                                        RenderUtil.renderModel(attachmentStack, stack);
                                    }
                                }
                            }
                            GlStateManager.popMatrix();
                        }
                    }
                }
            }
        }
    }

    private void renderMuzzleFlash(ItemStack weapon)
    {
        Gun gun = ((ItemGun) weapon.getItem()).getGun();
        if(drawFlash)
        {
            if(flash == null)
            {
                flash = new ItemStack(ModGuns.PARTS, 1, 2);
            }
            IBakedModel flashModel = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(flash);
            GlStateManager.pushMatrix();
            {
                GlStateManager.disableLighting();
                GlStateManager.translate(gun.display.flash.xOffset * 0.0625, gun.display.flash.yOffset * 0.0625, gun.display.flash.zOffset * 0.0625);

                if(!Gun.getAttachment(IAttachment.Type.BARREL, weapon).isEmpty())
                {
                    Gun.ScaledPositioned positioned = gun.getAttachmentPosition(IAttachment.Type.BARREL);
                    if(positioned != null)
                    {
                        GlStateManager.translate(0, 0, (gun.display.flash.zOffset - positioned.zOffset) * 0.0625 - positioned.scale);
                    }
                }

                /* Ported from the 1.19 version: every shot gets a random rotation around the
                 * barrel axis with a chance to be flipped vertically, and the size comes from
                 * the weapon's "display.flash.scale" (1.19 halves it). RenderUtil already
                 * centres the model, so rotating and scaling around the origin spins the quad
                 * about its own centre without moving it. */
                GlStateManager.rotate(360F * flashRandom, 0, 0, 1);
                boolean flip = flashRandom >= 0.5F;
                if(flip)
                {
                    GlStateManager.rotate(180F, 1, 0, 0);
                    GlStateManager.disableCull();
                }
                float flashScale = (float) gun.display.flash.scale * 0.5F;
                GlStateManager.scale(flashScale, flashScale, 1.0F);

                Minecraft.getMinecraft().entityRenderer.disableLightmap();
                RenderUtil.renderModel(flashModel, weapon);
                Minecraft.getMinecraft().entityRenderer.enableLightmap();
                if(flip)
                {
                    GlStateManager.enableCull();
                }
                GlStateManager.enableLighting();
            }
            GlStateManager.popMatrix();
            drawFlash = false;
        }
    }

    private static void copyModelAngles(ModelRenderer source, ModelRenderer dest)
    {
        dest.rotateAngleX = source.rotateAngleX;
        dest.rotateAngleY = source.rotateAngleY;
        dest.rotateAngleZ = source.rotateAngleZ;
    }

    private void renderHeldArm(ItemStack stack, EnumHandSide hand, float partialTicks)
    {
        GlStateManager.pushMatrix();

        Gun gun = ((ItemGun) stack.getItem()).getModifiedGun(stack);
        if(gun.general.gripType == GripType.TWO_HANDED)
        {
            GlStateManager.translate(0, 0, -1);
            GlStateManager.rotate(180F, 0, 1, 0);

            /* Front arm holding the barrel */
            GlStateManager.pushMatrix();

            float reloadProgress = (prevReloadTimer + (reloadTimer - prevReloadTimer) * partialTicks) / 5F;
            /* Ported from 1.19 TwoHandedPose: the front arm moves down, back and side while reloading */
            GlStateManager.translate(reloadProgress * 0.5F, -reloadProgress, -reloadProgress * 0.5F);

            int side = hand.opposite() == EnumHandSide.RIGHT ? 1 : -1;
            GlStateManager.translate(6.5 * side * 0.0625, -0.55, -0.5625);

            if(Minecraft.getMinecraft().player.getSkinType().equals("slim") && hand.opposite() == EnumHandSide.LEFT)
            {
                GlStateManager.translate(0.03125F * -side, 0, 0);
            }

            GlStateManager.rotate(90F, 1, 0, 0);
            GlStateManager.rotate(15F * -side, 0, 1, 0);
            GlStateManager.rotate(15F * -side, 0, 0, 1);
            GlStateManager.rotate(-35F, 1, 0, 0);

            GlStateManager.scale(0.5, 0.5, 0.5);
            this.renderArm(hand.opposite(), 0.0625F);

            GlStateManager.popMatrix();

            /* Back arm holding the handle. Ported from 1.15.X TwoHandedPose.renderFirstPersonArms,
             * which is the same source the 1.19 pose was derived from. */
            double centerOffset = 2.5;
            if(Minecraft.getMinecraft().player.getSkinType().equals("slim"))
            {
                centerOffset += hand == EnumHandSide.RIGHT ? 0.2 : 0.8;
            }
            centerOffset = hand == EnumHandSide.RIGHT ? -centerOffset : centerOffset;
            GlStateManager.translate(centerOffset * 0.0625, -0.4, -0.975);

            GlStateManager.rotate(80F, 1, 0, 0);

            GlStateManager.scale(0.5, 0.5, 0.5);
            this.renderArm(hand, 0.0625F);
        }
        else if(gun.general.gripType == GripType.ONE_HANDED)
        {
            GlStateManager.translate(0, 0, -1);
            GlStateManager.rotate(180F, 0, 1, 0);

            double centerOffset = 2.5;
            if(Minecraft.getMinecraft().player.getSkinType().equals("slim"))
            {
                centerOffset += hand == EnumHandSide.RIGHT ? 0.2 : 0.8;
            }
            centerOffset = hand == EnumHandSide.RIGHT ? -centerOffset : centerOffset;
            GlStateManager.translate(centerOffset * 0.0625, -0.45, -1.0);

            GlStateManager.rotate(75F, 1, 0, 0);

            GlStateManager.scale(0.5, 0.5, 0.5);
            this.renderArm(hand, 0.0625F);
        }
        GlStateManager.popMatrix();
    }

    private void renderReloadArm(ItemStack stack, EnumHandSide hand)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(mc.player.ticksExisted < startTick || reloadTimer != 5)
            return;

        Gun gun = ((ItemGun) stack.getItem()).getModifiedGun(stack);

        ItemAmmo ammo = AmmoRegistry.getInstance().getAmmo(gun.projectile.item);
        if(ammo == null)
            return;

        GlStateManager.pushMatrix();

        float reload = ((mc.player.ticksExisted - startTick + mc.getRenderPartialTicks()) % 10F) / 10F;
        float percent = 1.0F - reload;
        if(percent >= 0.5F)
        {
            percent = 1.0F - percent;
        }
        percent *= 2F;
        percent = percent < 0.5 ? 2 * percent * percent : -1 + (4 - 2 * percent) * percent;

        int side = hand.opposite() == EnumHandSide.RIGHT ? 1 : -1;
        GlStateManager.translate(-2.75 * side * 0.0625, -0.5625, -0.5625);
        GlStateManager.rotate(180F, 0, 1, 0);
        GlStateManager.translate(0, -0.35 * (1.0 - percent), 0);

        GlStateManager.translate(side * 1 * 0.0625, 0, 0);
        GlStateManager.rotate(90F, 1, 0, 0);
        GlStateManager.rotate(35F * -side, 0, 1, 0);
        GlStateManager.rotate(-75F * percent, 1, 0, 0);

        GlStateManager.scale(0.5, 0.5, 0.5);
        this.renderArm(hand.opposite(), 0.0625F);
        if(reload < 0.5F)
        {
            GlStateManager.pushMatrix();
            GlStateManager.translate(-side * 5 * 0.0625, 15 * 0.0625, -1 * 0.0625);
            GlStateManager.rotate(180F, 1, 0, 0);
            GlStateManager.scale(0.75, 0.75, 0.75);
            RenderUtil.renderModel(new ItemStack(ammo), ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND);
            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
    }

    private void renderArm(EnumHandSide hand, float scale)
    {
        AbstractClientPlayer abstractClientPlayer = Minecraft.getMinecraft().player;
        Minecraft.getMinecraft().getTextureManager().bindTexture(abstractClientPlayer.getLocationSkin());
        RenderPlayer renderPlayer = (RenderPlayer) Minecraft.getMinecraft().getRenderManager().<AbstractClientPlayer>getEntityRenderObject(abstractClientPlayer);

        GlStateManager.disableCull();

        if(hand == EnumHandSide.RIGHT)
        {
            renderPlayer.getMainModel().bipedRightArm.rotateAngleX = 0F;
            renderPlayer.getMainModel().bipedRightArm.rotateAngleY = 0F;
            renderPlayer.getMainModel().bipedRightArm.rotateAngleZ = 0F;
            renderPlayer.getMainModel().bipedRightArm.render(scale);
        }
        else
        {
            renderPlayer.getMainModel().bipedLeftArm.rotateAngleX = 0F;
            renderPlayer.getMainModel().bipedLeftArm.rotateAngleY = 0F;
            renderPlayer.getMainModel().bipedLeftArm.rotateAngleZ = 0F;
            renderPlayer.getMainModel().bipedLeftArm.render(scale);
        }

        GlStateManager.enableCull();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void blindPlayer(RenderGameOverlayEvent.Pre event)
    {
        if(event.getType() != ElementType.ALL)
            return;

        PotionEffect effect = Minecraft.getMinecraft().player.getActivePotionEffect(ModPotions.BLINDED);
        if(effect != null)
        {
            // Render white screen-filling overlay at full alpha effect when duration is above threshold
            // When below threshold, fade to full transparency as duration approaches 0
            float percent = Math.min((effect.getDuration() / (float) GunConfig.SERVER.stunGrenades.blind.alphaFadeThresholdSynced), 1);
            Gui.drawRect(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, ((int) (percent * GunConfig.SERVER.stunGrenades.blind.alphaOverlaySynced + 0.5) << 24) | 16777215);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderLastWorld(RenderWorldLastEvent event)
    {
        if(FMLClientHandler.instance().hasOptifine())
        {
            try
            {
                Class<?> clazz = Class.forName("net.optifine.shaders.Shaders");
                if(clazz != null)
                {
                    Field field = clazz.getDeclaredField("activeProgramID");
                    int activeProgramID = (int) field.get(null);
                    shadersEnabled = activeProgramID != 0;
                }
            }
            catch(ClassNotFoundException | NoSuchFieldException | IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }
        if(!shadersEnabled)
        {
            if(screenTextureId == -1)
            {
                screenTextureId = GlStateManager.generateTexture();
            }
            Minecraft mc = Minecraft.getMinecraft();
            GlStateManager.bindTexture(screenTextureId);
            GL11.glCopyTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, 0, 0, mc.displayWidth, mc.displayHeight, 0);
        }
        else if(screenTextureId != -1)
        {
            GlStateManager.deleteTexture(screenTextureId);
            screenTextureId = -1;
        }
    }

    public static CooldownTracker getCooldownTracker(UUID uuid)
    {
        if(!COOLDOWN_TRACKER_MAP.containsKey(uuid))
        {
            COOLDOWN_TRACKER_MAP.put(uuid, new CooldownTracker());
        }
        return COOLDOWN_TRACKER_MAP.get(uuid);
    }

    @SubscribeEvent
    public void onRenderBullets(RenderWorldLastEvent event)
    {
        for(Bullet bullet : bullets)
        {
            this.renderBullet(bullet, event.getPartialTicks());
        }
    }

    private void renderBullet(Bullet bullet, float partialTicks)
    {
        Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
        if(entity == null || bullet.isFinished() || bullet.getProjectile() == null)
            return;

        GlStateManager.pushMatrix();

        double doubleX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        double doubleY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        double doubleZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;

        GlStateManager.translate(-doubleX, -doubleY, -doubleZ);

        double bulletX = bullet.getPosX() + bullet.getMotionX() * partialTicks;
        double bulletY = bullet.getPosY() + bullet.getMotionY() * partialTicks;
        double bulletZ = bullet.getPosZ() + bullet.getMotionZ() * partialTicks;
        GlStateManager.translate(bulletX, bulletY, bulletZ);

        GlStateManager.rotate(bullet.getRotationYaw(), 0, 1, 0);
        GlStateManager.rotate(-bullet.getRotationPitch() + 90, 1, 0, 0);

        {
            GlStateManager.pushAttrib();
            GlStateManager.enableBlend();
            GlStateManager.disableCull();

            Minecraft.getMinecraft().getTextureManager().bindTexture(WHITE_GRADIENT);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

            Vec3d motionVec = new Vec3d(bullet.getMotionX(), bullet.getMotionY(), bullet.getMotionZ());
            double length = (motionVec.length() / 3.0) * bullet.getTrailLengthMultiplier();

            int trailColor = bullet.getTrailColor();
            float r = (float)(trailColor >> 16 & 255) / 255.0F;
            float g = (float)(trailColor >> 8 & 255) / 255.0F;
            float b = (float)(trailColor & 255) / 255.0F;
            float a = 1.0F;

            buffer.pos(0, 0, -0.035).tex(0, 0).color(r, g, b, a).endVertex();
            buffer.pos(0, 0, 0.035).tex(1, 0).color(r, g, b, a).endVertex();
            buffer.pos(0, -length, 0.035).tex(1, 1).color(r, g, b, a).endVertex();
            buffer.pos(0, -length, -0.035).tex(0, 1).color(r, g, b, a).endVertex();

            buffer.pos(-0.035, 0, 0).tex(0, 0).color(r, g, b, a).endVertex();
            buffer.pos(0.035, 0, 0).tex(1, 0).color(r, g, b, a).endVertex();
            buffer.pos(0.035, -length, 0).tex(1, 1).color(r, g, b, a).endVertex();
            buffer.pos(-0.035, -length, 0).tex(0, 1).color(r, g, b, a).endVertex();

            tessellator.draw();

            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.popAttrib();
        }

        // No point rendering item if empty, so return
        if(bullet.getProjectile().getItem().isEmpty())
        {
            GlStateManager.popMatrix();
            return;
        }

        GlStateManager.rotate((bullet.getProjectile().ticksExisted + partialTicks) * (float) 50, 0, 1, 0);

        GlStateManager.scale(0.275, 0.275, 0.275);

        Minecraft mc = Minecraft.getMinecraft();
        mc.entityRenderer.enableLightmap();
        int brightness = 0;
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(MathHelper.floor(doubleX), 0, MathHelper.floor(doubleZ));
        if (mc.world.isBlockLoaded(blockPos))
        {
            blockPos.setY(MathHelper.floor(doubleY));
            brightness = mc.world.getCombinedLight(blockPos, 0);
        }
        int x = brightness % 65536;
        int y = brightness / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)x, (float)y);
        Minecraft.getMinecraft().getRenderItem().renderItem(bullet.getProjectile().getItem(), ItemCameraTransforms.TransformType.NONE);
        mc.entityRenderer.disableLightmap();

        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public void onTickBullets(TickEvent.ClientTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
        {
            bullets.forEach(bullet -> bullet.tick(Minecraft.getMinecraft().world));
            bullets.removeIf(Bullet::isFinished);
        }
    }

    public void addBullet(Bullet bullet)
    {
        this.bullets.add(bullet);
    }
}
