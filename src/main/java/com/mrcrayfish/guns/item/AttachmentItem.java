package com.mrcrayfish.guns.item;

import com.mrcrayfish.guns.item.attachment.impl.Attachment;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

/**
 * A base attachment item implementation.
 *
 * Author: MrCrayfish
 */
public class AttachmentItem<T extends Attachment> extends ItemColored implements IAttachment<T>
{
    private final IAttachment.Type type;

    public AttachmentItem(IAttachment.Type type)
    {
        this.type = type;
    }

    @Override
    public IAttachment.Type getType()
    {
        return type;
    }

    @Override
    public T getProperties()
    {
        return null;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        return super.getItemStackDisplayName(stack);
    }
}