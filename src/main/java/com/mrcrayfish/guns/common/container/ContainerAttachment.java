package com.mrcrayfish.guns.common.container;

import com.mrcrayfish.guns.ItemStackUtil;
import com.mrcrayfish.guns.item.IAttachment;
import com.mrcrayfish.guns.object.Gun;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

/**
 * Author: MrCrayfish
 */
public class ContainerAttachment extends Container
{
    private ItemStack weapon;
    private IInventory playerInventory;
    private EntityPlayer player;
    private IInventory weaponInventory = new InventoryBasic("WeaponAttachments", false, IAttachment.Type.values().length)
    {
        @Override
        public void markDirty()
        {
            super.markDirty();
            ContainerAttachment.this.onCraftMatrixChanged(this);
        }
    };
    private boolean loaded = false;

    public ContainerAttachment(IInventory playerInventory, ItemStack weapon, EntityPlayer player)
    {
        this.weapon = weapon;
        this.playerInventory = playerInventory;
        this.player = player;

        // Load attachments from weapon NBT
        ItemStack[] attachments = new ItemStack[IAttachment.Type.values().length];
        for(int i = 0; i < attachments.length; i++)
        {
            attachments[i] = Gun.getAttachment(IAttachment.Type.values()[i], weapon);
        }
        for(int i = 0; i < attachments.length; i++)
        {
            this.weaponInventory.setInventorySlotContents(i, attachments[i]);
        }
        this.loaded = true;

        // Add attachment slots
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            this.addSlotToContainer(new com.mrcrayfish.guns.common.container.slot.SlotAttachment(this, this.weaponInventory, weapon, IAttachment.Type.values()[i], player, i, 8, 17 + i * 18));
        }

        // Add player inventory slots
        for(int i = 0; i < 3; i++)
        {
            for(int j = 0; j < 9; j++)
            {
                this.addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 102 + i * 18));
            }
        }

        // Add player hotbar slots
        for(int i = 0; i < 9; i++)
        {
            if(i == player.inventory.currentItem)
            {
                this.addSlotToContainer(new Slot(playerInventory, i, 8 + i * 18, 160)
                {
                    @Override
                    public boolean canTakeStack(EntityPlayer playerIn)
                    {
                        return false;
                    }
                });
            }
            else
            {
                this.addSlotToContainer(new Slot(playerInventory, i, 8 + i * 18, 160));
            }
        }
    }

    public boolean isLoaded()
    {
        return this.loaded;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return true;
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventoryIn)
    {
        if(this.loaded)
        {
            NBTTagCompound attachments = new NBTTagCompound();

            for(int i = 0; i < this.weaponInventory.getSizeInventory(); i++)
            {
                ItemStack attachment = this.getSlot(i).getStack();
                if(!attachment.isEmpty() && attachment.getItem() instanceof IAttachment)
                {
                    NBTTagCompound attachmentTag = new NBTTagCompound();
                    attachment.writeToNBT(attachmentTag);
                    attachments.setTag(((IAttachment) attachment.getItem()).getType().getName(), attachmentTag);
                }
            }

            NBTTagCompound tag = ItemStackUtil.createTagCompound(this.weapon);
            // Use "Attachments" with capital A for compatibility with 1.19.X
            tag.setTag("Attachments", attachments);
            // Also keep "attachments" for backward compatibility
            tag.setTag("attachments", attachments);
        }
        super.detectAndSendChanges();
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if(slot != null && slot.getHasStack())
        {
            ItemStack slotStack = slot.getStack();
            stack = slotStack.copy();

            if(index < this.weaponInventory.getSizeInventory())
            {
                if(!this.mergeItemStack(slotStack, this.weaponInventory.getSizeInventory(), this.inventorySlots.size(), true))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if(!this.mergeItemStack(slotStack, 0, this.weaponInventory.getSizeInventory(), false))
            {
                return ItemStack.EMPTY;
            }

            if(slotStack.isEmpty())
            {
                slot.putStack(ItemStack.EMPTY);
            }
            else
            {
                slot.onSlotChanged();
            }
        }

        return stack;
    }

    public IInventory getPlayerInventory()
    {
        return this.playerInventory;
    }

    public IInventory getWeaponInventory()
    {
        return this.weaponInventory;
    }
}
