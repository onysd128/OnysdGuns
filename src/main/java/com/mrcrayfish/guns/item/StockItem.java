package com.mrcrayfish.guns.item;

import com.mrcrayfish.guns.item.attachment.IStock;
import com.mrcrayfish.guns.item.attachment.impl.Stock;
import net.minecraft.item.ItemStack;

/**
 * A basic stock attachment item implementation.
 *
 * Author: MrCrayfish
 */
public class StockItem extends AttachmentItem<Stock> implements IStock
{
    private final Stock stock;
    private final boolean colored;

    public StockItem(Stock stock)
    {
        this(stock, true);
    }

    public StockItem(Stock stock, boolean colored)
    {
        super(IAttachment.Type.STOCK);
        this.stock = stock;
        this.colored = colored;
    }

    @Override
    public Stock getProperties()
    {
        return this.stock;
    }
}