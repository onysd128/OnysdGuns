package com.mrcrayfish.guns.client;

/**
 * Author: MrCrayfish
 *
 * The type of sway animation applied to the held weapon. Ported from 1.19.X.
 * The directional multiplier is applied to the sway angle for each axis, mirroring
 * the Axis.XN/XP and Axis.YN/YP behaviour of the 1.19 implementation.
 */
public enum SwayType
{
    DIRECTIONAL(-1, -1),
    DRAG(1, 1);

    private final float pitchDirection;
    private final float yawDirection;

    SwayType(float pitchDirection, float yawDirection)
    {
        this.pitchDirection = pitchDirection;
        this.yawDirection = yawDirection;
    }

    /**
     * @return the multiplier for the pitch (X axis) sway rotation
     */
    public float getPitchDirection()
    {
        return this.pitchDirection;
    }

    /**
     * @return the multiplier for the yaw (Y axis) sway rotation
     */
    public float getYawDirection()
    {
        return this.yawDirection;
    }

    /**
     * Gets a sway type by its name. If the name doesn't match any type,
     * DRAG is returned as the default, matching the 1.19 config default.
     *
     * @param name the name of the sway type
     * @return the matching sway type, or DRAG if none match
     */
    public static SwayType byName(String name)
    {
        for(SwayType type : values())
        {
            if(type.name().equalsIgnoreCase(name))
            {
                return type;
            }
        }
        return DRAG;
    }
}