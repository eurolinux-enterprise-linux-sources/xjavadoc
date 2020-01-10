/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

import java.lang.reflect.Constructor;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Arrays;

/**
 * Describe what this class does
 *
 * @author    Ara Abrahamian
 * @author    Aslak Hellesøy
 * @created   20. mars 2003
 */
final class BinaryClass extends AbstractClass
{
	public static int  instanceCount = 0;

	private final static List _primitiveTypes = Arrays.asList( new String[]{
		"java.lang.Boolean",
		"java.lang.Byte",
		"java.lang.Character",
		"java.lang.Double",
		"java.lang.Float",
		"java.lang.Integer",
		"java.lang.Long",
		"java.lang.Short",
		"java.lang.String"
		} );
	private final Class _clazz;

	private boolean    _isSuperclassSet = false;

	private boolean    _isInterfacesSet = false;
	public BinaryClass( XJavaDoc xJavaDoc, Class clazz  )
	{
		super( xJavaDoc, null );
		_clazz = clazz;
		setQualifiedName( clazz.getName() );
		setContainingPackage( Util.getPackageNameFor( clazz.getName() ) );

		addModifier( clazz.getModifiers() );

//		setSuperclassMaybe();
//		setInterfacesMaybe();

		// We gain speed by not instantiating the methods and constructors, and we probably don't need them (Aslak)
		// Well, looks like we need it anyway:
		// http://opensource.atlassian.com/projects/xdoclet/secure/ViewIssue.jspa?id=10100
		// To avoid sacrificing speed, this will be controlled with a system property.
		if( "true".equals( System.getProperty( "xjavadoc.compiledmethods" ) ) )
		{
			setMethods( _clazz );
			setConstructors( _clazz );
		}
		instanceCount++;
	}

	private static int getDimension( Class c )
	{
		return c.getName().lastIndexOf( '[' ) + 1;
	}

	private static String getTypeName( Class c )
	{
		return c.getComponentType() != null ? c.getComponentType().getName() : c.getName();
	}

	public XClass getSuperclass()
	{
		setSuperclassMaybe();
		return super.getSuperclass();
	}

	public List getInterfaces()
	{
		setInterfacesMaybe();
		return super.getInterfaces();
	}

	/**
	 * whether this class is writeable an can be save ( it can not )
	 *
	 * @return   false since this class can not be mutated or saved
	 */
	public boolean isWriteable()
	{
		return false;
	}
	public boolean isImplementingInterface( String full_qualified_type_name, boolean superclasses )
	{
		return isClassImplementingInterface( _clazz, full_qualified_type_name, superclasses );
	}

	public boolean isSubclassOf( String full_qualified_type_name, boolean superclasses )
	{
		Class superclass = _clazz.getSuperclass();

		if( superclass == null )
			return false;
		do
		{
			if( superclass.getName().equals( full_qualified_type_name ) )
				return true;

			superclass = superclass.getSuperclass();
		}while ( superclasses == true && superclass != null );

		return false;
	}

	public boolean isPrimitive()
	{
		return _primitiveTypes.contains( getQualifiedName() );
	}

	/**
	 * no op since we do not save binary classes
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

	private boolean isClassImplementingInterface( Class cur_class, String full_qualified_type_name, boolean superclasses )
	{
		do
		{
			Class[] interfaces = cur_class.getInterfaces();

			for( int i = 0; i < interfaces.length; i++ )
			{
				Class intf = interfaces[i];

				if( intf.getName().equals( full_qualified_type_name ) || isClassImplementingInterface( intf, full_qualified_type_name, superclasses ) )
					return true;
			}

			cur_class = cur_class.getSuperclass();
		}while ( superclasses == true && cur_class != null );

		return false;
	}

	private void setSuperclassMaybe()
	{
		if( !_isSuperclassSet )
		{
			Class superclass = _clazz.getSuperclass();

			if( superclass != null )
			{
				setSuperclass( superclass.getName() );
			}
			_isSuperclassSet = true;
		}
	}

	private void setInterfacesMaybe()
	{
		if( !_isInterfacesSet )
		{
			Class[] interfaces = _clazz.getInterfaces();

			for( int i = 0; i < interfaces.length; i++ )
			{
				addInterface( interfaces[i].getName() );
			}
			_isInterfacesSet = true;
		}
	}

	/**
	 * Discovers constructors. This method is currently never called
	 *
	 * @param clazz
	 */
	private void setConstructors( Class clazz )
	{
		Constructor[] constructors = clazz.getDeclaredConstructors();

		for( int i = 0; i < constructors.length; i++ )
		{
			ConstructorImpl constructor = new ConstructorImpl( this, null );

			constructor.addModifier( constructors[i].getModifiers() );
			populateExecutableMember( constructor, constructors[i] );
			addConstructor( constructor );
		}
	}

	/**
	 * Discovers constructors. This method is currently never called
	 *
	 * @param clazz
	 */
	private void setMethods( Class clazz )
	{
		Method[] methods = clazz.getDeclaredMethods();

		for( int i = 0; i < methods.length; i++ )
		{
			// Don't include static initialiser "methods"
			if( !"<clinit>".equals( methods[i].getName() ) )
			{
				MethodImpl method = new MethodImpl( this, null );

				method.setName( methods[i].getName() );

				method.addModifier( methods[i].getModifiers() );
				populateExecutableMember( method, methods[i] );

				Class returnType = methods[i].getReturnType();

				String typeName = getTypeName( returnType );
				int dimension = getDimension( returnType );

				method.setReturnType( typeName );
				method.setReturnDimension( dimension );
				addMethod( method );
			}
		}
	}

	private void populateExecutableMember( AbstractExecutableMember executableMember, AccessibleObject accessibleObject )
	{
		Class[] parameters;
		Class[] exceptions;

		// sadly, getParameterTypes is not in the AccesibleObject or Member interface
		if( accessibleObject instanceof Constructor )
		{
			parameters = ( ( Constructor ) accessibleObject ).getParameterTypes();
			exceptions = ( ( Constructor ) accessibleObject ).getExceptionTypes();
		}
		else
		{
			// It's method then
			parameters = ( ( Method ) accessibleObject ).getParameterTypes();
			exceptions = ( ( Method ) accessibleObject ).getExceptionTypes();
		}
		for( int i = parameters.length - 1; i >= 0; i-- )
		{
			String typeName = getTypeName( parameters[i] );
			int dimension = getDimension( parameters[i] );

			executableMember.addParameterData( typeName, "p" + i, dimension );
		}
		for( int i = exceptions.length - 1; i >= 0; i-- )
		{
			executableMember.addThrownException( exceptions[i].getName() );
		}
	}
}
