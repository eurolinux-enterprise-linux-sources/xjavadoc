/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

/**
 * Base class for Type.
 *
 * @author    Aslak Hellesøy
 * @created   25. mars 2003
 * @version   Revision: 1.0 $
 */
abstract class AbstractType implements Type
{
	public boolean equals( Object o )
	{
		if( ( o instanceof Type ) )
		{
			Type other = ( Type ) o;
			boolean typeEqual = getType().equals( other.getType() );
			boolean dimensionEqual = getDimension() == other.getDimension();

			return typeEqual && dimensionEqual;
		}
		return false;
	}
}
