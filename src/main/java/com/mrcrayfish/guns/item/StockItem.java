package com.mrcrayfish.guns.item;

import com.mrcrayfish.guns.item.attachment.IStock;
import com.mrcrayfish.guns.item.attachment.impl.Stock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * A basic stock attachment item implementation.
 *
 * Author: MrCrayfish
 */
public class StockItem extends AttachmentItem<Stock> implements IStock
{
    private final Stock stock;
    private final boolean colored;

    public StockItem(ResourceLocation id, Stock stock)
    {
        this(id, stock, true);
    }

    public StockItem(ResourceLocation id, Stock stock, boolean colored)
    {
        super(id, IAttachment.Type.STOCK);
        this.stock = stock;
        this.colored = colored;
    }

    @Override
    public Stock getProperties()
    {
        return this.stock;
    }
}