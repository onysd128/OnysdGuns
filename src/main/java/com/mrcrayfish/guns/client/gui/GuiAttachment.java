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
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class GuiAttachment extends GuiContainer
{
    private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("cgm:textures/gui/attachments.png");

    private final EntityPlayer player;
    private final ItemStack weapon;
    private final ContainerAttachment container;

    private boolean showHelp = true;
    private int windowZoom = 10;
    private int windowX, windowY;
    private float windowRotationX, windowRotationY;
    private boolean mouseGrabbed;
    private int mouseGrabbedButton;
    private int mouseClickedX, mouseClickedY;

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
        this.fontRenderer.drawString(this.player.inventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2 + 19, 4210752);

        int startX = (this.width - this.xSize) / 2;
        int startY = (this.height - this.ySize) / 2;

        // Render 3D weapon preview
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(startX + 26, this.height - (startY + 17 + 70), 142, 70);
        GlStateManager.pushMatrix();
        {
            GlStateManager.translate(this.guiLeft + 96, this.guiTop + 50, 150);
            GlStateManager.translate(this.windowX + (this.mouseGrabbed && this.mouseGrabbedButton == 0 ? mouseX - this.mouseClickedX : 0), 0, 0);
            GlStateManager.translate(0, this.windowY + (this.mouseGrabbed && this.mouseGrabbedButton == 0 ? mouseY - this.mouseClickedY : 0), 0);
            GlStateManager.rotate(-30F, 1, 0, 0);
            GlStateManager.rotate(this.windowRotationY - (this.mouseGrabbed && this.mouseGrabbedButton == 1 ? mouseY - this.mouseClickedY : 0), 1, 0, 0);
            GlStateManager.rotate(this.windowRotationX + (this.mouseGrabbed && this.mouseGrabbedButton == 1 ? mouseX - this.mouseClickedX : 0), 0, 1, 0);
            GlStateManager.rotate(150F, 0, 1, 0);
            GlStateManager.scale(this.windowZoom / 10F, this.windowZoom / 10F, this.windowZoom / 10F);
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
                
                // Render attachments manually (renderAttachments is private)
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

        if(this.showHelp)
        {
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            this.fontRenderer.drawString("Use mouse to rotate, scroll to zoom", 56, 38, 0xFFFFFF);
            GlStateManager.popMatrix();
        }

        // Render tooltips for attachment slots
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            Slot slot = this.container.getSlot(i);
            if(isPointInRegion(slot.xPos, slot.yPos, 16, 16, mouseX, mouseY))
            {
                IAttachment.Type type = IAttachment.Type.values()[i];
                if(!slot.isEnabled())
                {
                    this.drawHoveringText(Arrays.asList("slot.cgm.attachment." + type.getName(), TextFormatting.RED + "Not applicable"), mouseX - this.guiLeft, mouseY - this.guiTop);
                }
                else if(slot instanceof SlotAttachment && slot.getStack().isEmpty() && !this.isCompatible(this.mc.player.inventory.getItemStack(), (SlotAttachment) slot))
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
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(GUI_TEXTURES);
        int left = (this.width - this.xSize) / 2;
        int top = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(left, top, 0, 0, this.xSize, this.ySize);

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
        if(!slot.isEnabled()) return false;

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

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        int startX = (this.width - this.xSize) / 2;
        int startY = (this.height - this.ySize) / 2;
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int scroll = Mouse.getEventDWheel();

        if(isPointInRegion(startX + 26, startY + 17, 142, 70, mouseX, mouseY))
        {
            if(scroll < 0 && this.windowZoom > 0)
            {
                this.showHelp = false;
                this.windowZoom--;
            }
            else if(scroll > 0)
            {
                this.showHelp = false;
                this.windowZoom++;
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        int startX = (this.width - this.xSize) / 2;
        int startY = (this.height - this.ySize) / 2;

        if(isPointInRegion(startX + 26, startY + 17, 142, 70, mouseX, mouseY))
        {
            if(!this.mouseGrabbed && (mouseButton == 0 || mouseButton == 1))
            {
                this.mouseGrabbed = true;
                this.mouseGrabbedButton = mouseButton == 1 ? 1 : 0;
                this.mouseClickedX = mouseX;
                this.mouseClickedY = mouseY;
                this.showHelp = false;
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        if(this.mouseGrabbed)
        {
            if(this.mouseGrabbedButton == 0 && state == 0)
            {
                this.mouseGrabbed = false;
                this.windowX += (mouseX - this.mouseClickedX - 1);
                this.windowY += (mouseY - this.mouseClickedY);
            }
            else if(this.mouseGrabbedButton == 1 && state == 1)
            {
                this.mouseGrabbed = false;
                this.windowRotationX += (mouseX - this.mouseClickedX);
                this.windowRotationY -= (mouseY - this.mouseClickedY);
            }
        }
    }
}
