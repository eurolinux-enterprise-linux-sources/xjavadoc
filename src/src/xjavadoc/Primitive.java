/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

/**
 * This class represents primitive types
 *
 * @author    Ara Abrahamian
 * @author    Aslak Hellesøy
 * @created   February 17, 2002
 */
final class Primitive extends AbstractClass
{
	private final String _type;

	public Primitive( XJavaDoc xJavaDoc, String name, String type )
	{
		super( xJavaDoc, null );
		setQualifiedName( name );
		_type = type;
	}

	public final String getType()
	{
		return _type;
	}

	public final boolean isPrimitive()
	{
		return !getQualifiedName().equals( "void" );
	}

	/**
	 * whether this class can be saved ( it can not )
	 *
	 * @return   always false
	 */
	public boolean isWriteable()
	{
		return false;
	}

	public XPackage getContainingPackage()
	{
		return null;
	}

	/**
	 * no op since it's not writeable
	 */
	public void setDirty()
	{
	}

	/**
	 * this class is not intended to be saved
	 *
	 * @return   always false
	 */
	public boolean saveNeeded()
	{
		return false;
	}
}
