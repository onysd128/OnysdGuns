package com.mrcrayfish.guns.common.container.slot;

import com.mrcrayfish.guns.common.container.ContainerAttachment;
import com.mrcrayfish.guns.init.ModSounds;
import com.mrcrayfish.guns.item.IAttachment;
import com.mrcrayfish.guns.item.ItemGun;
import com.mrcrayfish.guns.object.Gun;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

/**
 * Author: MrCrayfish
 */
public class SlotAttachment extends Slot
{
    private ContainerAttachment container;
    private ItemStack weapon;
    private IAttachment.Type type;
    private EntityPlayer player;

    public SlotAttachment(ContainerAttachment container, IInventory weaponInventory, ItemStack weapon, IAttachment.Type type, EntityPlayer player, int index, int x, int y)
    {
        super(weaponInventory, index, x, y);
        this.container = container;
        this.weapon = weapon;
        this.type = type;
        this.player = player;
    }

    public IAttachment.Type getType()
    {
        return this.type;
    }

    @Override
    public boolean isEnabled()
    {
        // Always enable all attachment slots
        return true;
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        if(!(this.weapon.getItem() instanceof ItemGun))
        {
            return false;
        }
        ItemGun item = (ItemGun) this.weapon.getItem();
        Gun gun = item.getModifiedGun(this.weapon);
        if(!(stack.getItem() instanceof IAttachment))
        {
            return false;
        }
        IAttachment attachment = (IAttachment) stack.getItem();
        // In 1.12.2, we don't have canAttachTo method, so we just check type compatibility
        return attachment.getType() == this.type && gun.canAttachType(this.type);
    }

    @Override
    public void onSlotChanged()
    {
        if(this.container.isLoaded())
        {
            float pitch = this.getHasStack() ? 1.0F : 0.75F;
            SoundEvent sound = ModSounds.getSound(com.mrcrayfish.guns.Reference.MOD_ID + ":ui_weapon_attach");
            if(sound != null)
            {
                this.player.world.playSound(null, this.player.posX, this.player.posY + 1.0, this.player.posZ, sound, SoundCategory.PLAYERS, 0.5F, pitch);
            }
        }
    }

    @Override
    public int getSlotStackLimit()
    {
        return 1;
    }
}
