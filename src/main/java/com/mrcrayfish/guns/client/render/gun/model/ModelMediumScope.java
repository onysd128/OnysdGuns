package com.mrcrayfish.guns.client.render.gun.model;

import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.client.event.RenderEvents;
import com.mrcrayfish.guns.client.render.gun.IOverrideModel;
import com.mrcrayfish.guns.client.util.RenderUtil;
import com.mrcrayfish.guns.proxy.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Author: MrCrayfish
 */
public class ModelMediumScope implements IOverrideModel
{
    private static final ResourceLocation RETICLE = new ResourceLocation(Reference.MOD_ID, "textures/effect/holo_reticle.png");
    private static final ResourceLocation VIGNETTE = new ResourceLocation(Reference.MOD_ID, "textures/effect/scope_vignette.png");

    @Override
    public void init() {}

    @Override
    public void tick(EntityLivingBase entity) {}

    @Override
    public void render(float partialTicks, ItemCameraTransforms.TransformType transformType, ItemStack stack, ItemStack parent, EntityLivingBase entity)
    {
        if(RenderEvents.shadersEnabled && isFirstPerson(transformType) && entity.equals(Minecraft.getMinecraft().player))
        {
            GlStateManager.translate(0, 0, 0.15 * ClientProxy.renderEvents.normalZoomProgress);
            GlStateManager.scale(1, 1, 0.2 + 0.8 * (1.0 - ClientProxy.renderEvents.normalZoomProgress));
        }
        RenderUtil.renderModel(stack, parent);

        if(isFirstPerson(transformType) && entity.equals(Minecraft.getMinecraft().player))
        {
            if(!RenderEvents.shadersEnabled && RenderEvents.screenTextureId != -1)
            {
                GlStateManager.color(1.0F, 1.0F, 1.0F, (float) ClientProxy.renderEvents.normalZoomProgress * 0.75F + 0.25F);
                GlStateManager.enableBlend();
                OpenGlHelper.glBlendFunc(770, 771, 1, 0);
                GlStateManager.disableLighting();
                GlStateManager.bindTexture(RenderEvents.screenTextureId);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

                /* Match the ported 1.19 medium scope tube aperture: the inner opening of the
                 * tube is 1.2485 blocks wide, with the four corners bevelled by 0.2485. */
                double scopeSize = 1.2485;
                double size = scopeSize / 16.0;
                double cut = 0.2485;
                double offset = cut / 16.0;
                double cornerRatio = cut / scopeSize;
                /* The viewfinder quad was reduced from the original 2.0 aperture to the ported
                 * 1.19 tube (1.2485), so shrink the sampled frame by the same ratio. Otherwise
                 * the smaller window shows a larger area of the screen and minifies the target. */
                double crop = (1.0 - 0.2 * (scopeSize / 2.0)) / 2.0;
                Minecraft mc = Minecraft.getMinecraft();
                int kickAmount = ClientProxy.renderEvents.recoilAngle > 0 ? 50 : 0;
                double texOffset = ClientProxy.renderEvents.recoilNormal * kickAmount * (1.0 / mc.displayHeight);
                double texU = ((mc.displayWidth - mc.displayHeight + mc.displayHeight * crop * 2.0) / 2.0) / mc.displayWidth;
                double texSpanX = 1.0 - texU * 2;
                double texSpanY = 1.0 - crop * 2;
                double centerY = 9.625 / 16.0 - 0.5;

                GlStateManager.pushMatrix();
                {
                    GlStateManager.translate(-size / 2, centerY - size / 2, 1.4 * 0.0625);
                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder buffer = tessellator.getBuffer();
                    buffer.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_TEX);
                    buffer.pos(offset, 0, 0).tex(texU + cornerRatio * texSpanX, crop + texOffset).endVertex();
                    buffer.pos(size - offset, 0, 0).tex(1.0 - texU - cornerRatio * texSpanX, crop + texOffset).endVertex();
                    buffer.pos(size, offset, 0).tex(1.0 - texU, crop + texOffset + cornerRatio * texSpanY).endVertex();
                    buffer.pos(size, size - offset, 0).tex(1.0 - texU, 1.0 - crop + texOffset - cornerRatio * texSpanY).endVertex();
                    buffer.pos(size - offset, size, 0).tex(1.0 - texU - cornerRatio * texSpanX, 1.0 - crop + texOffset).endVertex();
                    buffer.pos(offset, size, 0).tex(texU + cornerRatio * texSpanX, 1.0 - crop + texOffset).endVertex();
                    buffer.pos(0, size - offset, 0).tex(texU, 1.0 - crop + texOffset - cornerRatio * texSpanY).endVertex();
                    buffer.pos(0, offset, 0).tex(texU, crop + texOffset + cornerRatio * texSpanY).endVertex();
                    tessellator.draw();

                    GlStateManager.color(1.0F, 1.0F, 1.0F, (float) ClientProxy.renderEvents.normalZoomProgress * 0.8F + 0.2F);
                    GlStateManager.pushMatrix();
                    {
                        /* Base reticle size approved earlier (size * 0.045 when the screen was the
                         * old 2.0 aperture). Ring enlarged ~1.6x, centre dot shrunk and tinted red. */
                        double base = (2.0 / 16.0) * 0.045;
                        double ringSize = base * 1.6;
                        double dotSize = base * 0.3;
                        mc.getTextureManager().bindTexture(RETICLE);
                        GlStateManager.pushMatrix();
                        {
                            GlStateManager.translate((size - ringSize) / 2, (size - ringSize) / 2, 0.0001);
                            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                            buffer.pos(0, 0, 0).tex(0.6875, 0.6875).endVertex();
                            buffer.pos(ringSize, 0, 0).tex(0, 0.6875).endVertex();
                            buffer.pos(ringSize, ringSize, 0).tex(0, 0).endVertex();
                            buffer.pos(0, ringSize, 0).tex(0.6875, 0).endVertex();
                            tessellator.draw();
                        }
                        GlStateManager.popMatrix();
                        /* Centre dot: tint the white reticle pixel red. */
                        GlStateManager.color(1.0F, 0.0F, 0.0F, (float) ClientProxy.renderEvents.normalZoomProgress * 0.8F + 0.2F);
                        GlStateManager.pushMatrix();
                        {
                            GlStateManager.translate((size - dotSize) / 2, (size - dotSize) / 2, 0.0002);
                            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                            buffer.pos(0, 0, 0).tex(0.0625, 0.75).endVertex();
                            buffer.pos(dotSize, 0, 0).tex(0, 0.75).endVertex();
                            buffer.pos(dotSize, dotSize, 0).tex(0, 0.6875).endVertex();
                            buffer.pos(0, dotSize, 0).tex(0.0625, 0.6875).endVertex();
                            tessellator.draw();
                        }
                        GlStateManager.popMatrix();
                    }
                    GlStateManager.popMatrix();

                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.translate(0, 0, 0.0001);
                    mc.getTextureManager().bindTexture(VIGNETTE);
                    buffer.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_TEX);
                    buffer.pos(offset, 0, 0).tex(cornerRatio, 1.0).endVertex();
                    buffer.pos(size - offset, 0, 0).tex(1.0 - cornerRatio, 1.0).endVertex();
                    buffer.pos(size, offset, 0).tex(1.0, 1.0 - cornerRatio).endVertex();
                    buffer.pos(size, size - offset, 0).tex(1.0, cornerRatio).endVertex();
                    buffer.pos(size - offset, size, 0).tex(1.0 - cornerRatio, 0.0).endVertex();
                    buffer.pos(offset, size, 0).tex(cornerRatio, 0.0).endVertex();
                    buffer.pos(0, size - offset, 0).tex(0.0, cornerRatio).endVertex();
                    buffer.pos(0, offset, 0).tex(0.0, 1.0 - cornerRatio).endVertex();
                    tessellator.draw();
                }
                GlStateManager.popMatrix();

                GlStateManager.enableLighting();
            }
        }
    }

    private boolean isFirstPerson(ItemCameraTransforms.TransformType transformType)
    {
        return transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND || transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND;
    }
}
