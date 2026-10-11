package com.mrcrayfish.guns.item;

import com.mrcrayfish.guns.item.attachment.IScope;
import com.mrcrayfish.guns.item.attachment.impl.Scope;
import net.minecraft.item.ItemStack;

/**
 * A basic scope attachment item implementation.
 *
 * Author: MrCrayfish
 */
public class ScopeItem extends AttachmentItem<Scope> implements IScope
{
    private final Scope scope;

    public ScopeItem(Scope scope)
    {
        super(IAttachment.Type.SCOPE);
        this.scope = scope;
    }

    @Override
    public Scope getProperties()
    {
        return this.scope;
    }
}