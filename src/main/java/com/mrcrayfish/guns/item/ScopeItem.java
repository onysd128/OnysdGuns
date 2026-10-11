package com.mrcrayfish.guns.item;

import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.item.attachment.IScope;
import com.mrcrayfish.guns.item.attachment.impl.Scope;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Author: MrCrayfish
 */
public class ScopeItem extends AttachmentItem<Scope> implements IScope
{
    private final Scope scope;

    public ScopeItem(ResourceLocation id, Scope scope)
    {
        super(id, IAttachment.Type.SCOPE);
        this.setHasSubtypes(false);
        this.setMaxDamage(0);
        this.scope = scope;
    }

    @Override
    public IAttachment.Type getType()
    {
        return IAttachment.Type.SCOPE;
    }

    @Override
    public Scope getProperties()
    {
        return this.scope;
    }

    @Override
    public String getTranslationKey(ItemStack stack)
    {
        return super.getTranslationKey(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems)
    {
        if(!isInCreativeTab(tab))
            return;

        subItems.add(new ItemStack(this));
    }
}
