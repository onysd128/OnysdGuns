package com.mrcrayfish.guns.network.message;

import com.mrcrayfish.guns.MrCrayfishGunMod;
import com.mrcrayfish.guns.item.ItemGun;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Author: MrCrayfish
 */
public class MessageOpenAttachments implements IMessage, IMessageHandler<MessageOpenAttachments, IMessage>
{
    @Override
    public void toBytes(ByteBuf buf) {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public IMessage onMessage(MessageOpenAttachments message, MessageContext ctx)
    {
        FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() ->
        {
            EntityPlayer player = ctx.getServerHandler().player;
            ItemStack heldItem = player.inventory.getCurrentItem();
            if(heldItem.getItem() instanceof ItemGun)
            {
                player.openGui(MrCrayfishGunMod.instance, com.mrcrayfish.guns.common.GuiHandler.GUI_ATTACHMENTS, player.world, (int) player.posX, (int) player.posY, (int) player.posZ);
            }
        });
        return null;
    }
}
