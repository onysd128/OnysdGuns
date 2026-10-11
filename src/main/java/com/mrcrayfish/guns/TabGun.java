package com.mrcrayfish.guns;

import com.mrcrayfish.guns.init.ModBlocks;
import com.mrcrayfish.guns.init.ModGuns;
import com.mrcrayfish.guns.item.ItemGun;
import com.mrcrayfish.guns.ItemStackUtil;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class TabGun extends CreativeTabs
{
	public TabGun() 
	{
		super("tabCGM");
	}

	@Override
	public ItemStack createIcon()
	{
		if(ModGuns.BAZOOKA != null && ModGuns.BAZOOKA instanceof ItemGun)
		{
			ItemStack stack = new ItemStack(ModGuns.BAZOOKA);
			ItemGun bazooka = (ItemGun) ModGuns.BAZOOKA;
			ItemStackUtil.createTagCompound(stack).setInteger("AmmoCount", bazooka.getGun().general.maxAmmo);
			// Set color for the icon (example: red color - 0xFF0000)
			// You can change this to any RGB color value
			((ItemGun) ModGuns.BAZOOKA).setColor(stack, 0xFFA500); // Red color
			return stack;
		}
		return new ItemStack(ModGuns.PISTOL);
	}

	@Override
	public void displayAllRelevantItems(NonNullList<ItemStack> items)
	{
		// Matches 1.19, which just outputs every mod item in ModItems registration order:
		// firearms, ammunition, grenades, scopes, barrel, stocks, under barrel, then the workbench.

		// Firearms
		if(ModGuns.PISTOL != null)
		{
			ModGuns.PISTOL.getSubItems(this, items);
		}
		if(ModGuns.SHOTGUN != null)
		{
			ModGuns.SHOTGUN.getSubItems(this, items);
		}
		if(ModGuns.RIFLE != null)
		{
			ModGuns.RIFLE.getSubItems(this, items);
		}
		if(ModGuns.GRENADE_LAUNCHER != null)
		{
			ModGuns.GRENADE_LAUNCHER.getSubItems(this, items);
		}
		if(ModGuns.BAZOOKA != null)
		{
			ModGuns.BAZOOKA.getSubItems(this, items);
		}
		if(ModGuns.CHAIN_GUN != null)
		{
			ModGuns.CHAIN_GUN.getSubItems(this, items);
		}
		if(ModGuns.ASSAULT_RIFLE != null)
		{
			ModGuns.ASSAULT_RIFLE.getSubItems(this, items);
		}
		if(ModGuns.MACHINE_PISTOL != null)
		{
			ModGuns.MACHINE_PISTOL.getSubItems(this, items);
		}
		if(ModGuns.HEAVY_RIFLE != null)
		{
			ModGuns.HEAVY_RIFLE.getSubItems(this, items);
		}

		// Ammunition
		if(ModGuns.BASIC_AMMO != null)
		{
			items.add(new ItemStack(ModGuns.BASIC_AMMO));
		}
		if(ModGuns.ADVANCED_AMMO != null)
		{
			items.add(new ItemStack(ModGuns.ADVANCED_AMMO));
		}
		if(ModGuns.SHELL != null)
		{
			items.add(new ItemStack(ModGuns.SHELL));
		}
		if(ModGuns.MISSILE != null)
		{
			items.add(new ItemStack(ModGuns.MISSILE));
		}

		// Grenades
		if(ModGuns.GRENADE != null)
		{
			items.add(new ItemStack(ModGuns.GRENADE));
		}
		if(ModGuns.STUN_GRENADE != null)
		{
			items.add(new ItemStack(ModGuns.STUN_GRENADE));
		}

		// Scopes
		if(ModGuns.SHORT_SCOPE != null)
		{
			ModGuns.SHORT_SCOPE.getSubItems(this, items);
		}
		if(ModGuns.MEDIUM_SCOPE != null)
		{
			ModGuns.MEDIUM_SCOPE.getSubItems(this, items);
		}
		if(ModGuns.LONG_SCOPE != null)
		{
			ModGuns.LONG_SCOPE.getSubItems(this, items);
		}

		// Barrel
		if(ModGuns.SILENCER != null)
		{
			ModGuns.SILENCER.getSubItems(this, items);
		}

		// Stocks
		if(ModGuns.LIGHT_STOCK != null)
		{
			ModGuns.LIGHT_STOCK.getSubItems(this, items);
		}
		if(ModGuns.TACTICAL_STOCK != null)
		{
			ModGuns.TACTICAL_STOCK.getSubItems(this, items);
		}
		if(ModGuns.WEIGHTED_STOCK != null)
		{
			ModGuns.WEIGHTED_STOCK.getSubItems(this, items);
		}

		// Under barrel
		if(ModGuns.LIGHT_GRIP != null)
		{
			ModGuns.LIGHT_GRIP.getSubItems(this, items);
		}
		if(ModGuns.SPECIALISED_GRIP != null)
		{
			ModGuns.SPECIALISED_GRIP.getSubItems(this, items);
		}

		// Workbench
		if(ModBlocks.WORKBENCH != null)
		{
			items.add(new ItemStack(ModBlocks.WORKBENCH));
		}
	}
}
