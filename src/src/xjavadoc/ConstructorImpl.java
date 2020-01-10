/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

import java.util.List;

/**
 * Describe what this class does
 *
 * @author    Aslak Hellesøy
 * @created   18. januar 2002
 */
final class ConstructorImpl extends AbstractExecutableMember implements XConstructor
{
	public static int  instanceCount = 0;

	public ConstructorImpl( AbstractClass containingClass, XTagFactory tagFactory  )
	{
		super( containingClass, tagFactory );
		instanceCount++;
	}

	public final boolean isConstructor()
	{
		return true;
	}

	public XProgramElement getSuperElement()
	{
		return getSuperElement( false );
	}

	public List getAllSuperElements()
	{
		return null;
	}

	public String getName()
	{
		return getContainingClass().getName();
	}

	public final void setName( String name )
	{
		throw new UnsupportedOperationException( "Can't set name for constructors" );
	}

	protected String buildStringId()
	{
		StringBuffer sb = new StringBuffer();

		sb = new StringBuffer();
		sb.append( getModifiers() );
		sb.append( ' ' );
		sb.append( getNameWithSignature( false ) );
		return sb.toString();
	}
}
