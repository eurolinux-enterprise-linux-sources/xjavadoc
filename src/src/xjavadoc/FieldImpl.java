/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

import java.lang.reflect.Modifier;

/**
 * @author    Ara Abrahamian (ara_e_w@yahoo.com)
 * @author    Aslak Hellesøy
 * @created   Feb 15, 2002
 * @version   $Revision: 1.17 $
 */
final class FieldImpl extends MemberImpl implements XField
{
	private String     _type;
	private int        _dimension;

	public FieldImpl( AbstractClass containingClass, XTagFactory tagFactory )
	{
		super( containingClass, tagFactory );
	}

	public final boolean isTransient()
	{
		return ( getModifierSpecifier() & Modifier.TRANSIENT ) != 0;
	}

	public final boolean isVolatile()
	{
		return ( getModifierSpecifier() & Modifier.VOLATILE ) != 0;
	}

	public int getDimension()
	{
		return _dimension;
	}

	public String getTypeAsString()
	{
		return _type;
	}

	public String getDimensionAsString()
	{
		return Util.appendDimensionAsString( getDimension(), new StringBuffer() ).toString();
	}

	public XClass getType()
	{
		return getContainingAbstractClass().qualify( _type );
	}

	public XProgramElement getSuperElement()
	{
		return null;
	}

	public void setType( String type )
	{
		_type = type;
	}

	public void setDimension( int dimension )
	{
		_dimension = dimension;
	}

	public String toString()
	{
		return getModifiers() + " " + getTypeAsString() + Util.appendDimensionAsString( getDimension(), new StringBuffer() ).toString() + " " +
			getName();
	}
}
