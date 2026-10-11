package com.mrcrayfish.guns.item.attachment.impl;

import com.mrcrayfish.guns.interfaces.IGunModifier;

/**
 * An attachment class related to stocks. Use {@link #create(IGunModifier...)} to create a stock.
 * <p>
 * Author: MrCrayfish
 */
public class Stock extends Attachment
{
    private Stock(IGunModifier... modifier)
    {
        super(modifier);
    }

    /**
     * Creates a stock
     *
     * @param modifier an array of gun modifiers
     * @return a stock
     */
    public static Stock create(IGunModifier... modifier)
    {
        return new Stock(modifier);
    }
}