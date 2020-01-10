/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

import java.util.List;

/**
 * Baseclass for field, method and constructor
 *
 * @author    Aslak Hellesøy
 * @created   Feb 15, 2002
 * @version   $Revision: 1.5 $
 */
abstract class MemberImpl extends AbstractProgramElement implements XMember
{
	private String     _name;

	protected MemberImpl( AbstractClass containingClass, XTagFactory tagFactory )
	{
		super( containingClass, tagFactory );
	}

	public String getName()
	{
		return _name;
	}

	public void setName( String name )
	{
		if( name == null )
		{
			throw new IllegalArgumentException( "name can't be null" );
		}
		_name = name;
	}

	public List getSuperInterfaceElements()
	{
		return null;
	}

}
