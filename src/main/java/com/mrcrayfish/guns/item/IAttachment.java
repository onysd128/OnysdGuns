package com.mrcrayfish.guns.item;

import com.google.common.annotations.Beta;

import net.minecraft.item.ItemStack;
import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
@Beta
public interface IAttachment<T>
{
    Type getType();

    T getProperties();

    default boolean canAttachTo(ItemStack stack)
    {
        return true;
    }

    enum Type
    {
        SCOPE("scope", "Scope", "scope"),
        BARREL("barrel", "Barrel", "barrel"),
        STOCK("stock", "Stock", "stock"),
        UNDER_BARREL("under_barrel", "Under_Barrel", "underBarrel");

        private final String translationKey;
        private final String tagKey;
        private final String serializeKey;

        Type(String translationKey, String tagKey, String serializeKey)
        {
            this.translationKey = translationKey;
            this.tagKey = tagKey;
            this.serializeKey = serializeKey;
        }

        public String getTranslationKey()
        {
            return this.translationKey;
        }

        public String getTagKey()
        {
            return this.tagKey;
        }

        public String getSerializeKey()
        {
            return this.serializeKey;
        }

        // Backward compatibility
        public String getName()
        {
            return this.serializeKey;
        }

        @Nullable
        public static Type byTagKey(String s)
        {
            for(Type type : values())
            {
                if(type.tagKey.equalsIgnoreCase(s))
                {
                    return type;
                }
            }
            return null;
        }

        // Backward compatibility
        @Nullable
        public static Type getType(String s)
        {
            return byTagKey(s);
        }
    }
}
