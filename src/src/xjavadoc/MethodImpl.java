/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.beans.Introspector;

/**
 * Describe what this class does
 *
 * @author    Aslak Hellesøy
 * @created   25. februar 2003
 */
final class MethodImpl extends AbstractExecutableMember implements XMethod
{
	public static int  instanceCount = 0;

	private String     methodNameWithSignatureAndModifiers = null;
	private String     methodNameWithSignatureWithoutModifiers = null;

	private ReturnType _returnType = new ReturnType( this );

	public MethodImpl( AbstractClass containingClass, XTagFactory tagFactory )
	{
		super( containingClass, tagFactory );

		// if we're in an interface, add public modifier even if it isn't declared,
		// since interface methods are always public.
		if( containingClass.isInterface() )
		{
			addModifier( Modifier.PUBLIC );
		}
		instanceCount++;
	}

	/**
	 * Gets the Constructor attribute of the SourceMethod object
	 *
	 * @return   The Constructor value
	 */
	public final boolean isConstructor()
	{
		return false;
	}

	public final Type getReturnType()
	{
		return _returnType;
	}

	public XProgramElement getSuperElement()
	{
		return getSuperElement( true );
	}

	public List getSuperInterfaceElements()
	{

		Iterator interfaces = getContainingClass().getInterfaces().iterator();

		List result = new ArrayList();

		while ( interfaces.hasNext() )
		{

			XClass superinterface = (XClass) interfaces.next();

			XExecutableMember superExecutableMember = superinterface.getMethod( getNameWithSignature( false ) );
			if( superExecutableMember != null )
			{
				result.add( superExecutableMember );
			}

			//TODO: do we need to keep searching upwards, to find superinterfaces?

		}

		return result;
	}

	public XMethod getAccessor()
	{
		XMethod result = null;

		if( isPropertyMutator() )
		{
			Type requiredType = ( Type ) getParameters().iterator().next();
			String getterNameWithSignature = "get" + getNameWithoutPrefix() + "()";
			String isserNameWithSignature = "is" + getNameWithoutPrefix() + "()";
			XMethod getter = getContainingClass().getMethod( getterNameWithSignature, true );
			XMethod isser = getContainingClass().getMethod( isserNameWithSignature, true );

			// If only one is non null, return it. If both or none exist, return null.
			if( getter == null && isser != null )
			{
				result = isser;
			}
			else if( getter != null && isser == null )
			{
				result = getter;
			}
			// Verify that the types are compatible
			if( !requiredType.equals( result.getReturnType() ) )
			{
				result = null;
			}
		}
		return result;
	}

	public XMethod getMutator()
	{
		XMethod result = null;

		if( isPropertyAccessor() )
		{
			Type requiredType = getReturnType();
			String argument = requiredType.getType().getQualifiedName() + requiredType.getDimensionAsString();
			String setterNameWithSignature = "set" + getNameWithoutPrefix() + "(" + argument + ")";

			result = getContainingClass().getMethod( setterNameWithSignature, true );
		}
		return result;
	}

	public boolean isPropertyAccessor()
	{
		boolean signatureOk = false;
		boolean nameOk = false;

		if( getName().startsWith( "is" ) )
		{
			signatureOk = getReturnType().getType().getQualifiedName().equals( "boolean" ) || getReturnType().getType().getQualifiedName().equals( "java.lang.Boolean" );
			signatureOk = signatureOk && getReturnType().getDimension() == 0;
			if( getName().length() > 2 )
			{
				nameOk = Character.isUpperCase( getName().charAt( 2 ) );
			}
		}
		if( getName().startsWith( "get" ) )
		{
			signatureOk = true;
			if( getName().length() > 3 )
			{
				nameOk = Character.isUpperCase( getName().charAt( 3 ) );
			}
		}

		boolean noParams = getParameters().size() == 0;

		return signatureOk && nameOk && noParams;
	}

	public boolean isPropertyMutator()
	{
		boolean nameOk = false;

		if( getName().startsWith( "set" ) )
		{
			if( getName().length() > 3 )
			{
				nameOk = Character.isUpperCase( getName().charAt( 3 ) );
			}
		}

		boolean oneParam = getParameters().size() == 1;

		return nameOk && oneParam;
	}

	public String getPropertyName()
	{
		String result = null;

		if( getName().startsWith( "get" ) || getName().startsWith( "set" ) )
		{
			result = Introspector.decapitalize( getName().substring( 3 ) );
		}
		else if( getName().startsWith( "is" ) )
		{
			result = Introspector.decapitalize( getName().substring( 2 ) );
		}
		return result;
	}

	public Type getPropertyType()
	{
		Type result = null;

		if( isPropertyMutator() )
		{
			XParameter parameter = ( XParameter ) getParameters().iterator().next();

			result = parameter;
		}
		else if( isPropertyAccessor() )
		{
			result = getReturnType();
		}
		return result;
	}

	public String getNameWithoutPrefix()
	{
		for( int i = 0; i < getName().length(); i++ )
		{
			if( Character.isUpperCase( getName().charAt( i ) ) )
			{
				return getName().substring( i );
			}
		}
		return null;
	}

	/**
	 * Sets the ReturnType attribute of the SourceMethod object
	 *
	 * @param returnType  The new ReturnType value
	 */
	public final void setReturnType( String returnType )
	{
		_returnType.setType( returnType );
	}

	/**
	 * Sets the ReturnDimension attribute of the SourceMethod object
	 *
	 * @param d  The new ReturnDimension value
	 */
	public final void setReturnDimension( int d )
	{
		_returnType.setDimension( d );
	}

	/**
	 * Two methods are equal if they have the same return type, name and signature,
	 * regardless of the enclosing class and modifiers. Methods are compared for
	 * equality when calling XClass.getMethods(true)
	 *
	 * @param o
	 * @return
	 */
	public boolean equals( Object o )
	{
		MethodImpl other = ( MethodImpl ) o;

		return getMethodNameWithSignatureWithoutModifiers().equals( other.getMethodNameWithSignatureWithoutModifiers() );
	}

	public int hashCode()
	{
		return toString( false ).hashCode();
	}

	public String toString()
	{
		return getMethodNameWithSignatureAndModifiers() + " [" + getContainingClass().getQualifiedName() + "]";
	}

	protected String buildStringId()
	{
		return getMethodNameWithSignatureWithoutModifiers();
	}

	private String getMethodNameWithSignatureAndModifiers()
	{
		if( methodNameWithSignatureAndModifiers == null )
		{
			methodNameWithSignatureAndModifiers = toString( true );
		}
		return methodNameWithSignatureAndModifiers;
	}

	private String getMethodNameWithSignatureWithoutModifiers()
	{
		if( methodNameWithSignatureWithoutModifiers == null )
		{
			methodNameWithSignatureWithoutModifiers = toString( false );
		}
		return methodNameWithSignatureWithoutModifiers;
	}

	/**
	 * Builds a String uniquely describing this method
	 *
	 * @param modifiers
	 * @return           a String uniquely describing this method
	 */
	private String toString( boolean modifiers )
	{
		StringBuffer sb;

		if( modifiers )
		{
			sb = new StringBuffer( getModifiers() );
			if( sb.length() > 0 )
			{
				sb.append( ' ' );
			}
		}
		else
		{
			sb = new StringBuffer();
		}
		sb.append( getReturnType().getType().getQualifiedName() );
		sb.append( getReturnType().getDimensionAsString() );
		sb.append( ' ' );
		sb.append( getNameWithSignature( false ) );
		return sb.toString();
	}

}
