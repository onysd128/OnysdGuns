package com.mrcrayfish.guns.item.attachment.impl;

import com.mrcrayfish.guns.interfaces.IGunModifier;
import com.mrcrayfish.guns.item.attachment.IScope;
import net.minecraft.item.ItemStack;

/**
 * An attachment class related to scopes. Scopes need to at least specify the aim FOV modifier
 * they provide and the reticle offset for them to render correctly. Use
 * {@link #builder()} to create a scope.
 * <p>
 * Author: MrCrayfish
 */
public class Scope extends Attachment
{
    protected float aimFovModifier;
    protected float additionalZoom;
    protected double reticleOffset;
    protected boolean stable;
    protected double viewFinderDist;

    private Scope() {}

    private Scope(float aimFovModifier, float additionalZoom, double reticleOffset, boolean stable, double viewFinderDist, IGunModifier... modifiers)
    {
        super(modifiers);
        this.aimFovModifier = aimFovModifier;
        this.additionalZoom = additionalZoom;
        this.reticleOffset = reticleOffset;
        this.stable = stable;
        this.viewFinderDist = viewFinderDist;
    }

    public float getFovModifier()
    {
        return this.aimFovModifier;
    }

    @Deprecated
    public float getAdditionalZoom()
    {
        return this.additionalZoom;
    }

    @Deprecated
    public double getCenterOffset()
    {
        return this.reticleOffset;
    }

    @Deprecated
    public double getReticleOffset()
    {
        return this.reticleOffset;
    }

    @Deprecated
    public boolean isStable()
    {
        return this.stable;
    }

    @Deprecated
    public double getViewFinderOffset()
    {
        return this.viewFinderDist;
    }

    public double getViewFinderDistance()
    {
        return this.viewFinderDist;
    }

    public double getHeightToCenter()
    {
        return this.reticleOffset;
    }

    public Scope copy()
    {
        Scope scope = new Scope();
        scope.aimFovModifier = this.aimFovModifier;
        scope.additionalZoom = this.additionalZoom;
        scope.reticleOffset = this.reticleOffset;
        scope.stable = this.stable;
        scope.viewFinderDist = this.viewFinderDist;
        scope.modifiers = this.modifiers;
        return scope;
    }

    @Deprecated
    public static Scope create(float additionalZoom, double centerOffset, IGunModifier... modifiers)
    {
        return new Scope(1.0F, additionalZoom, centerOffset, false, 0.0, modifiers);
    }

    public static Builder builder()
    {
        return new Builder();
    }

    /**
     * Gets the Scope properties from an attached scope ItemStack.
     * Returns null if the stack is not a scope or is empty.
     */
    public static Scope getScopeFromStack(ItemStack stack)
    {
        if(stack == null || stack.isEmpty() || !(stack.getItem() instanceof IScope))
        {
            return null;
        }
        IScope itemScope = (IScope) stack.getItem();
        return itemScope.getProperties();
    }

    public static class Builder
    {
        private float aimFovModifier = 1.0F;
        private float additionalZoom = 0.0F;
        private double reticleOffset = 0.0;
        private boolean stable = false;
        private double viewFinderDist = 0.0;
        private IGunModifier[] modifiers = new IGunModifier[]{};

        private Builder() {}

        public Builder aimFovModifier(float fovModifier)
        {
            this.aimFovModifier = fovModifier;
            return this;
        }

        @Deprecated
        public Builder additionalZoom(float additionalZoom)
        {
            this.additionalZoom = additionalZoom;
            return this;
        }

        @Deprecated
        public Builder centerOffset(double centerOffset)
        {
            this.reticleOffset = centerOffset;
            return this;
        }

        @Deprecated
        public Builder reticleOffset(double reticleOffset)
        {
            this.reticleOffset = reticleOffset;
            return this;
        }

        public Builder stable(boolean stable)
        {
            this.stable = stable;
            return this;
        }

        @Deprecated
        public Builder viewFinderOffset(double viewFinderOffset)
        {
            this.viewFinderDist = viewFinderOffset;
            return this;
        }

        @Deprecated
        public Builder viewFinderDistance(double viewFinderDist)
        {
            this.viewFinderDist = viewFinderDist;
            return this;
        }

        public Builder modifiers(IGunModifier... modifiers)
        {
            this.modifiers = modifiers;
            return this;
        }

        public Scope build()
        {
            return new Scope(this.aimFovModifier, this.additionalZoom, this.reticleOffset, this.stable, this.viewFinderDist, this.modifiers);
        }
    }
}