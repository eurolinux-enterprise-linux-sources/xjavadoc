/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * This is a flyweight implementation of XParameter
 *
 * @author    Ara Abrahamian (ara_e_w@yahoo.com)
 * @author    Aslak Hellesøy
 * @created   9. mars 2003
 * @version   $Revision: 1.18 $
 */
public final class ParameterImpl extends AbstractType implements XParameter
{
	public static int  instanceCount = 0;

	/**
	 * XMember we're currently reresenting.
	 */
	private AbstractExecutableMember _containingExecutableMember;

	/**
	 * Index of the parameter we're currently representing.
	 */
	private int        _parameterIndex;

	private String     _description;

	public ParameterImpl()
	{
		instanceCount++;
	}

	public final String getName()
	{
		return _containingExecutableMember.getParameterName( _parameterIndex );
	}

	/**
	 * Returns the class describing the type of this parameter.
	 *
	 * @return
	 */
	public final XClass getType()
	{
		String type = _containingExecutableMember.getParameterType( _parameterIndex );
		AbstractClass containingClass = _containingExecutableMember.getContainingAbstractClass();

		XClass result = containingClass.qualify( type );

		return result;
	}

	public final int getDimension()
	{
		return _containingExecutableMember.getParameterDimension( _parameterIndex );
	}

	public XTag getParamTag()
	{
		for( Iterator paramTags = _containingExecutableMember.getDoc().getTags( "param", true ).iterator(); paramTags.hasNext();  )
		{
			XTag paramTag = ( XTag ) paramTags.next();
			StringTokenizer st = new StringTokenizer( paramTag.getValue() );

			if( st.hasMoreTokens() )
			{
				if( st.nextToken().equals( getName() ) )
				{
					// We found the @param tag.

					// Set the description so it's readily available if someone asks for it.
					_description = paramTag.getValue().substring( getName().length() ).trim();
					return paramTag;
				}
			}
		}
		// Didn't find any param tags.
		_description = null;
		return null;
	}

	public String getDescription()
	{
		XTag paramTag = getParamTag();

		if( paramTag != null )
		{
			return _description;
		}
		else
		{
			return null;
		}
	}

	public String getDimensionAsString()
	{
		return Util.appendDimensionAsString( getDimension(), new StringBuffer() ).toString();
	}

	public final String toString()
	{
		StringBuffer sb = new StringBuffer( getType().getQualifiedName() );

		Util.appendDimensionAsString( getDimension(), sb ).append( " " ).append( getName() );
		return sb.toString();
	}

	/**
	 * Sets the extrinsic flyweight state.
	 *
	 * @param containingExecutableMember  The containing member
	 * @param parameterIndex
	 */
	final void setState( AbstractExecutableMember containingExecutableMember, int parameterIndex )
	{
		_containingExecutableMember = containingExecutableMember;
		_parameterIndex = parameterIndex;
	}
}
