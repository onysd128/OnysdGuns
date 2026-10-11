package com.mrcrayfish.guns.item;

import com.mrcrayfish.guns.item.attachment.IBarrel;
import com.mrcrayfish.guns.item.attachment.impl.Barrel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * A basic barrel attachment item implementation.
 *
 * Author: MrCrayfish
 */
public class BarrelItem extends AttachmentItem<Barrel> implements IBarrel
{
    private final Barrel barrel;

    public BarrelItem(ResourceLocation id, Barrel barrel)
    {
        super(id, IAttachment.Type.BARREL);
        this.barrel = barrel;
    }

    @Override
    public Barrel getProperties()
    {
        return this.barrel;
    }
}