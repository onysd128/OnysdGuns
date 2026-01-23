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
import net.minecraft.client.resources.I18n;
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

    private Slot hoveredSlot = null;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        // Find the hovered slot before calling super
        this.hoveredSlot = null;
        for(int i = 0; i < this.inventorySlots.inventorySlots.size(); ++i)
        {
            Slot slot = this.inventorySlots.inventorySlots.get(i);
            if(this.isPointInRegion(slot.xPos, slot.yPos, 16, 16, mouseX, mouseY) && slot.isEnabled())
            {
                this.hoveredSlot = slot;
                break; // Only highlight one slot
            }
        }
        
        // Call super to draw everything normally
        super.drawScreen(mouseX, mouseY, partialTicks);
        
        // Override the highlighting to only highlight the hovered slot
        if(this.hoveredSlot != null)
        {
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.pushMatrix();
            GlStateManager.translate((float)this.guiLeft, (float)this.guiTop, 0.0F);
            int slotColor = -2130706433;
            this.drawGradientRect(this.hoveredSlot.xPos, this.hoveredSlot.yPos, this.hoveredSlot.xPos + 16, this.hoveredSlot.yPos + 16, slotColor, slotColor);
            GlStateManager.popMatrix();
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRenderer.drawString("Attachments", 8, 6, 4210752);
        this.fontRenderer.drawString(this.player.inventory.getDisplayName().getUnformattedText(), 8, 90, 4210752);

        // Draw icons for each attachment slot (on top of slots)
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(GUI_TEXTURES);
        ItemStack heldStack = this.mc.player.inventory.getItemStack();
        
        // Get gun to check which attachment types it supports
        Gun gun = null;
        if(!this.weapon.isEmpty() && this.weapon.getItem() instanceof ItemGun)
        {
            gun = ((ItemGun) this.weapon.getItem()).getModifiedGun(this.weapon);
        }
        
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            Slot slot = this.container.getSlot(i);
            IAttachment.Type type = IAttachment.Type.values()[i];
            
            // Always draw icon for empty slots
            if(slot.getStack().isEmpty())
            {
                // First check if the weapon supports this attachment type at all
                boolean weaponSupportsType = gun != null && gun.canAttachType(type);
                
                if(!weaponSupportsType)
                {
                    // Draw not applicable icon - weapon doesn't support this type
                    this.drawTexturedModalRect(8, 17 + i * 18, 176, 0, 16, 16);
                }
                else
                {
                    // Weapon supports this type, check if held item can be placed
                    if(!this.canPlaceAttachmentInSlot(heldStack, slot))
                    {
                        // Draw not applicable icon - held item is incompatible
                        this.drawTexturedModalRect(8, 17 + i * 18, 176, 0, 16, 16);
                    }
                    else
                    {
                        // Draw icon for empty slot (176, 16 + i * 16 is the texture coordinate)
                        this.drawTexturedModalRect(8, 17 + i * 18, 176, 16 + i * 16, 16, 16);
                    }
                }
            }
        }
    }

    @Override
    protected void renderHoveredToolTip(int mouseX, int mouseY)
    {
        // Render tooltips for attachment slots
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            Slot slot = this.container.getSlot(i);
            if(this.isPointInRegion(slot.xPos, slot.yPos, 16, 16, mouseX, mouseY))
            {
                IAttachment.Type type = IAttachment.Type.values()[i];
                ItemStack heldStack = this.mc.player.inventory.getItemStack();
                if(slot instanceof SlotAttachment && slot.getStack().isEmpty() && !this.isCompatible(heldStack, (SlotAttachment) slot))
                {
                    String incompatibleText = I18n.format("slot.cgm.attachment.incompatible");
                    this.drawHoveringText(Arrays.asList(TextFormatting.YELLOW + incompatibleText), mouseX - this.guiLeft, mouseY - this.guiTop);
                }
                else if(slot.getStack().isEmpty())
                {
                    String tooltipText = I18n.format("slot.cgm.attachment." + type.getName());
                    this.drawHoveringText(Collections.singletonList(tooltipText), mouseX - this.guiLeft, mouseY - this.guiTop);
                }
                return; // Only show tooltip for the first hovered slot
            }
        }
        
        // Call super for other tooltips (items in slots, etc.)
        super.renderHoveredToolTip(mouseX, mouseY);
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
            // Calculate bounding box and optimal scale/offset
            double minX = 0.0, maxX = 0.0;
            double minY = 0.0, maxY = 0.0;
            double minZ = 0.0, maxZ = 0.0;
            float baseScale = 90.0F;
            
            if(this.player != null && !this.weapon.isEmpty() && this.weapon.getItem() instanceof ItemGun)
            {
                Gun gun = ((ItemGun) this.weapon.getItem()).getModifiedGun(this.weapon);
                
                // Base weapon size (approximate, in blocks)
                // Most weapons are roughly 0.4-0.6 blocks in size
                double baseSize = 0.5;
                minX = -baseSize;
                maxX = baseSize;
                minY = -baseSize;
                maxY = baseSize;
                minZ = -baseSize;
                maxZ = baseSize;
                
                // Get attachments and calculate their positions
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
                        if(type != null && gun.canAttachType(type))
                        {
                            Gun.ScaledPositioned positioned = gun.getAttachmentPosition(type);
                            if(positioned != null)
                            {
                                // Convert pixel offsets to block coordinates
                                double attachX = positioned.xOffset * 0.0625;
                                double attachY = positioned.yOffset * 0.0625;
                                double attachZ = positioned.zOffset * 0.0625;
                                double attachScale = positioned.scale;
                                
                                // Approximate attachment size (in blocks)
                                // Attachments are typically 0.15-0.25 blocks, scaled by their scale factor
                                double attachSize = 0.2 * attachScale;
                                
                                // Update bounding box to include attachment
                                minX = Math.min(minX, attachX - attachSize);
                                maxX = Math.max(maxX, attachX + attachSize);
                                minY = Math.min(minY, attachY - attachSize);
                                maxY = Math.max(maxY, attachY + attachSize);
                                minZ = Math.min(minZ, attachZ - attachSize);
                                maxZ = Math.max(maxZ, attachZ + attachSize);
                            }
                        }
                    }
                }
                
                // Calculate center of bounding box
                double centerX = (minX + maxX) / 2.0;
                double centerY = (minY + maxY) / 2.0;
                double centerZ = (minZ + maxZ) / 2.0;
                
                // Calculate size of bounding box
                double sizeX = maxX - minX;
                double sizeY = maxY - minY;
                double sizeZ = maxZ - minZ;
                double maxSize = Math.max(Math.max(sizeX, sizeY), sizeZ);
                
                // Calculate optimal scale to fit in viewport
                // Viewport is 142x70 pixels
                // After rotation (-30F on X, 150F on Y, 90F on Y), we need to account for perspective
                // The viewport appears as roughly 1.58 units wide and 0.78 units tall in 3D space
                // We want the model to fit with some padding (about 80% of viewport)
                double viewportWidth = 142.0 / 90.0 * 0.8; // Approximate viewport width in 3D space
                double viewportHeight = 70.0 / 90.0 * 0.8; // Approximate viewport height in 3D space
                double viewportSize = Math.min(viewportWidth, viewportHeight);
                
                if(maxSize > 0.01)
                {
                    baseScale = (float)(viewportSize / maxSize * 90.0F);
                    // Clamp scale to reasonable values
                    baseScale = Math.max(30.0F, Math.min(120.0F, baseScale));
                }
                
                // Calculate offset to center the model
                // After rotations, we need to apply negative center to move model to origin
                // Then translate to viewport center
                float viewportCenterX = left + 26 + 142 / 2.0F;
                float viewportCenterY = top + 17 + 70 / 2.0F - 3.0F; // Slightly higher
                
                // Translate to viewport center first
                GlStateManager.translate(viewportCenterX, viewportCenterY, 150);
                
                // Apply rotations (same as before)
                GlStateManager.rotate(-30F, 1, 0, 0);
                GlStateManager.rotate(150F, 0, 1, 0);
                GlStateManager.scale(1.0F, 1.0F, 1.0F);
                GlStateManager.rotate(90F, 0, 1, 0);
                GlStateManager.scale(1.0F, -1.0F, 1.0F);
                
                // Apply negative center offset to center the model
                GlStateManager.translate(-centerX, -centerY, -centerZ);
                
                // Apply scale
                GlStateManager.scale(baseScale, baseScale, baseScale);
            }
            else
            {
                // Fallback for non-gun items
                float viewportCenterX = left + 26 + 142 / 2.0F;
                float viewportCenterY = top + 17 + 70 / 2.0F - 6.0F; // Slightly higher
                GlStateManager.translate(viewportCenterX, viewportCenterY, 150);
                GlStateManager.rotate(-30F, 1, 0, 0);
                GlStateManager.rotate(150F, 0, 1, 0);
                GlStateManager.scale(1.0F, 1.0F, 1.0F);
                GlStateManager.rotate(90F, 0, 1, 0);
                GlStateManager.scale(1.0F, -1.0F, 1.0F);
                GlStateManager.scale(baseScale, baseScale, baseScale);
            }

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
