package com.mrcrayfish.guns.client.gui;

import com.mrcrayfish.guns.ItemStackUtil;
import com.mrcrayfish.guns.client.render.gun.IOverrideModel;
import com.mrcrayfish.guns.client.render.gun.ModelOverrides;
import com.mrcrayfish.guns.client.util.RenderUtil;
import com.mrcrayfish.guns.common.container.ContainerAttachment;
import com.mrcrayfish.guns.common.container.slot.SlotAttachment;
import com.mrcrayfish.guns.item.IAttachment;
import com.mrcrayfish.guns.item.ItemGun;
import com.mrcrayfish.guns.object.Gun;
import net.minecraftforge.common.util.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import org.lwjgl.opengl.GL11;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import java.util.Arrays;
import java.util.Collections;

/**
 * Author: MrCrayfish
 */
public class GuiAttachment extends GuiContainer
{
    private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("cgm:textures/gui/attachments.png");

    private final EntityPlayer player;
    private final ItemStack weapon;
    private final ContainerAttachment container;


    public GuiAttachment(EntityPlayer player, ItemStack weapon)
    {
        super(new ContainerAttachment(player.inventory, weapon, player));
        this.player = player;
        this.weapon = weapon;
        this.container = (ContainerAttachment) this.inventorySlots;
        this.xSize = 176;
        this.ySize = 184;
    }

    @Override
    public void initGui()
    {
        super.initGui();
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();
        if(this.player != null && !(this.player.getHeldItemMainhand().getItem() instanceof ItemGun))
        {
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRenderer.drawString("Attachments", 8, 6, 4210752);
        this.fontRenderer.drawString(this.player.inventory.getDisplayName().getUnformattedText(), 8, 90, 4210752);

        // Render tooltips for attachment slots
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            Slot slot = this.container.getSlot(i);
            if(isPointInRegion(slot.xPos, slot.yPos, 16, 16, mouseX, mouseY))
            {
                IAttachment.Type type = IAttachment.Type.values()[i];
                if(slot instanceof SlotAttachment && slot.getStack().isEmpty() && !this.isCompatible(this.mc.player.inventory.getItemStack(), (SlotAttachment) slot))
                {
                    this.drawHoveringText(Arrays.asList(TextFormatting.YELLOW + "Incompatible"), mouseX - this.guiLeft, mouseY - this.guiTop);
                }
                else if(slot.getStack().isEmpty())
                {
                    this.drawHoveringText(Collections.singletonList("slot.cgm.attachment." + type.getName()), mouseX - this.guiLeft, mouseY - this.guiTop);
                }
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        this.drawDefaultBackground(); // Draw darkened background
        
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(GUI_TEXTURES);
        int left = (this.width - this.xSize) / 2;
        int top = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(left, top, 0, 0, this.xSize, this.ySize);

        // Draw black background for weapon viewport
        this.drawRect(left + 26, top + 17, left + 26 + 142, top + 17 + 70, 0xFF000000);

        // Render 3D weapon preview
        ScaledResolution scaledResolution = new ScaledResolution(this.mc);
        int scaleFactor = scaledResolution.getScaleFactor();
        int scissorX = (left + 26) * scaleFactor;
        int scissorY = this.mc.displayHeight - ((top + 17 + 70) * scaleFactor);
        int scissorWidth = 142 * scaleFactor;
        int scissorHeight = 70 * scaleFactor;
        
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(scissorX, scissorY, scissorWidth, scissorHeight);
        GlStateManager.pushMatrix();
        {
            // Static position, rotation, and scale
            GlStateManager.translate(left + 96, top + 52, 150);
            GlStateManager.rotate(-30F, 1, 0, 0);
            GlStateManager.rotate(150F, 0, 1, 0);
            GlStateManager.scale(1.0F, 1.0F, 1.0F);
            GlStateManager.rotate(90F, 0, 1, 0);
            GlStateManager.scale(1.0F, -1.0F, 1.0F);
            GlStateManager.scale(90.0F, 90.0F, 90.0F);

            RenderHelper.enableStandardItemLighting();
            if(this.player != null && !this.weapon.isEmpty() && this.weapon.getItem() instanceof ItemGun)
            {
                GlStateManager.pushMatrix();
                RenderUtil.applyTransformType(this.weapon, ItemCameraTransforms.TransformType.NONE);
                
                // Render gun model
                IOverrideModel model = ModelOverrides.getModel(this.weapon);
                if(model != null)
                {
                    model.render(this.mc.getRenderPartialTicks(), ItemCameraTransforms.TransformType.NONE, this.weapon, ItemStack.EMPTY, this.player);
                }
                else
                {
                    RenderUtil.renderModel(this.weapon, ItemCameraTransforms.TransformType.NONE);
                }
                
                // Render attachments manually
                Gun gun = ((ItemGun) this.weapon.getItem()).getGun();
                net.minecraft.nbt.NBTTagCompound gunTag = ItemStackUtil.createTagCompound(this.weapon);
                net.minecraft.nbt.NBTTagCompound attachments = null;
                if(gunTag.hasKey("Attachments", Constants.NBT.TAG_COMPOUND))
                {
                    attachments = gunTag.getCompoundTag("Attachments");
                }
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
                            ItemStack attachmentStack = Gun.getAttachment(type, this.weapon);
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

                                        IOverrideModel attachmentModel = ModelOverrides.getModel(attachmentStack);
                                        if(attachmentModel != null)
                                        {
                                            attachmentModel.render(this.mc.getRenderPartialTicks(), ItemCameraTransforms.TransformType.NONE, attachmentStack, this.weapon, this.player);
                                        }
                                        else
                                        {
                                            RenderUtil.renderModel(attachmentStack, this.weapon);
                                        }
                                    }
                                }
                                GlStateManager.popMatrix();
                            }
                        }
                    }
                }
                GlStateManager.popMatrix();
            }
            RenderHelper.disableStandardItemLighting();
        }
        GlStateManager.popMatrix();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // Draw icons for each attachment slot
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            Slot slot = this.container.getSlot(i);
            if(!this.canPlaceAttachmentInSlot(this.mc.player.inventory.getItemStack(), slot))
            {
                this.drawTexturedModalRect(left + 8, top + 17 + i * 18, 176, 0, 16, 16);
            }
            else if(slot.getStack().isEmpty())
            {
                this.drawTexturedModalRect(left + 8, top + 17 + i * 18, 176, 16 + i * 16, 16, 16);
            }
        }
    }

    private boolean canPlaceAttachmentInSlot(ItemStack stack, Slot slot)
    {
        // All slots are always enabled, so we skip that check
        if(!slot.getStack().isEmpty()) return true;

        if(!(slot instanceof SlotAttachment)) return true;

        if(stack.isEmpty() || !(stack.getItem() instanceof IAttachment)) return true;

        IAttachment attachment = (IAttachment) stack.getItem();
        SlotAttachment attachmentSlot = (SlotAttachment) slot;

        if(!attachmentSlot.getType().equals(attachment.getType())) return true;

        return attachmentSlot.isItemValid(stack);
    }

    private boolean isCompatible(ItemStack stack, SlotAttachment slot)
    {
        if(stack.isEmpty()) return true;

        if(!(stack.getItem() instanceof IAttachment)) return false;

        IAttachment attachment = (IAttachment) stack.getItem();

        if(!attachment.getType().equals(slot.getType())) return true;

        return slot.isItemValid(stack);
    }

}
