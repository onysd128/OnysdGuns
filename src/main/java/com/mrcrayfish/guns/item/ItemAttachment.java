package com.mrcrayfish.guns.item;

import com.google.common.annotations.Beta;
import com.mrcrayfish.guns.MrCrayfishGunMod;
import com.mrcrayfish.guns.item.attachment.impl.Attachment;
import net.minecraft.util.ResourceLocation;

/**
 * Author: MrCrayfish
 */
@Beta
public class ItemAttachment<T extends Attachment> extends ItemColored implements IAttachment<T>
{
    private final IAttachment.Type type;

    public ItemAttachment(ResourceLocation id, IAttachment.Type type)
    {
        this.setTranslationKey(id.getNamespace() + "." + id.getPath());
        this.setRegistryName(id);
        this.setCreativeTab(MrCrayfishGunMod.GUN_TAB);
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
}
