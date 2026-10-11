package com.mrcrayfish.guns.item;

import com.google.common.annotations.Beta;
import com.mrcrayfish.guns.MrCrayfishGunMod;
import com.mrcrayfish.guns.item.attachment.impl.Attachment;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A base attachment item implementation.
 *
 * Author: MrCrayfish
 */
@Beta
public class AttachmentItem<T extends Attachment> extends ItemColored implements IAttachment<T>
{
    private final IAttachment.Type type;

    public AttachmentItem(ResourceLocation id, IAttachment.Type type)
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

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        T properties = this.getProperties();
        if(properties != null)
        {
            properties.addInformation(stack, tooltip);
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        return super.getItemStackDisplayName(stack);
    }
}