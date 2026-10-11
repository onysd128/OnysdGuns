package com.mrcrayfish.guns.item;

import com.mrcrayfish.guns.item.attachment.IUnderBarrel;
import com.mrcrayfish.guns.item.attachment.impl.UnderBarrel;
import net.minecraft.item.ItemStack;

/**
 * A basic under barrel attachment item implementation.
 *
 * Author: MrCrayfish
 */
public class UnderBarrelItem extends AttachmentItem<UnderBarrel> implements IUnderBarrel
{
    private final UnderBarrel underBarrel;

    public UnderBarrelItem(UnderBarrel underBarrel)
    {
        super(IAttachment.Type.UNDER_BARREL);
        this.underBarrel = underBarrel;
    }

    @Override
    public UnderBarrel getProperties()
    {
        return this.underBarrel;
    }
}