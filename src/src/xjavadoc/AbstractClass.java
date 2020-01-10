/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.io.IOException;
import java.io.File;

/**
 * Base implementation of XClass.
 *
 * @author    Ara Abrahamian
 * @author    Aslak Hellesøy
 * @created   18. oktober 2002
 */
public abstract class AbstractClass extends AbstractProgramElement implements XClass
{
	/**
	 * The implemented interfaces according to the source
	 */
	private List       _declaredInterfaces;
	/**
	 * The implemented interfaces according to the hierarchy
	 */
	private List       _allInterfaces;

	private List       _importedClasses;
	private List       _importedClassNames;
	private List       _importedPackages;
	private List       _constructors;
	private Map        _namedConstructors;
	private List       _methods;
	private HashMap    _namedMethods;
	private List       _fields;
	private List       _innerClasses;
	private XPackage   _containingPackage;
	private boolean    _isInterface;
	private boolean    _isAnonymous = false;
	private XClass     _superclass;
	private int        _hash = Integer.MIN_VALUE;
	private List       _directSubclasses;
	private List       _allSubclasses;
	private List       _implementingClasses;
	private List       _extendingInterfaces;
	private String     _name;
	private String     _transformedName;
	private String     _qualifiedName;
	private String     _transformedQualifiedName;

    protected AbstractClass( AbstractClass containingClass, XTagFactory tagFactory )
    {
        super( containingClass, tagFactory );
    }

    protected AbstractClass( XJavaDoc xJavaDoc, XTagFactory tagFactory )
    {
        super( xJavaDoc, tagFactory );
    }

	/**
	 * Gets the Interface attribute of the SourceClass object
	 *
	 * @return   The Interface value
	 */
	public final boolean isInterface()
	{
		return _isInterface;
	}

	public final boolean isA( String full_qualified_type_name )
	{
		return isA( full_qualified_type_name, true );
	}

	public final boolean isA( final String full_qualified_type_name, final boolean superclasses )
	{
		final boolean sameClass = getQualifiedName().equals( full_qualified_type_name );
		final boolean subClass = isSubclassOf( full_qualified_type_name, superclasses );
		final boolean implementz = isImplementingInterface( full_qualified_type_name, superclasses );

		return sameClass || subClass || implementz;
	}

	public final XMethod getMethod( String methodNameWithSignature )
	{
		return getMethod( methodNameWithSignature, false );
	}

	public final XMethod getMethod( String methodNameWithSignature, boolean superclasses )
	{
		XMethod result = null;

		initializeNamedMethodsHashMap();

		if( _namedMethods != null )
		{
			result = ( XMethod ) _namedMethods.get( methodNameWithSignature );
		}
		if( result == null && superclasses )
		{
			XClass superclass = getSuperclass();

			if( superclass != null )
			{
				result = superclass.getMethod( methodNameWithSignature, true );
			}
		}
		return result;
	}

	/**
	 * Gets the Constructor attribute of the AbstractClass object
	 *
	 * @param constructorNameWithSignature  Describe what the parameter does
	 * @return                              The Constructor value
	 */
	public final XConstructor getConstructor( String constructorNameWithSignature )
	{
		initializeNamedConstructorsHashMap();

		if( _namedConstructors != null )
			return ( XConstructor ) _namedConstructors.get( constructorNameWithSignature );
		else
			return null;
	}

	/**
	 * Returns an XField with the given name. Example: getField("id");
	 *
	 * @param fieldName  Describe what the parameter does
	 * @return           The Field value
	 */
	public final XField getField( String fieldName )
	{
		if( _fields == null )
		{
			return null;
		}
		for( int i = 0; i < _fields.size(); i++ )
		{
			XField field = ( XField ) _fields.get( i );

			if( field.getName().equals( fieldName ) )
			{
				return field;
			}
		}
		return null;
	}

	/**
	 * Describe what the method does
	 *
	 * @return   Describe the return value for method for return value
	 */
	public final List getImportedClasses()
	{
		return _importedClasses == null ? EMPTY_LIST : Collections.unmodifiableList( _importedClasses );
	}

	/**
	 * Describe what the method does
	 *
	 * @return   Describe the return value for method for return value
	 */
	public final List getImportedPackages()
	{
		return _importedPackages == null ? EMPTY_LIST : Collections.unmodifiableList( _importedPackages );
	}

	public final List getMethods()
	{
		return getMethods( false );
	}

	public final List getMethods( Predicate predicate, boolean superclasses )
	{
		return Collections.unmodifiableList( new ArrayList( CollectionUtils.select( getMethods( superclasses ), predicate ) ) );
	}

	public final List getFields( Predicate predicate, boolean superclasses )
	{
		return Collections.unmodifiableList( new ArrayList( CollectionUtils.select( getFields( superclasses ), predicate ) ) );
	}

	public final List getMethods( boolean superclasses )
	{
		return getMembers( superclasses, false );
	}

	public final List getFields( boolean superclasses )
	{
		return getMembers( superclasses, true );
	}

	/**
	 * Describe what the method does
	 *
	 * @return   Describe the return value for method for return value
	 */
	public final List getFields()
	{
		return _fields == null ? EMPTY_LIST : Collections.unmodifiableList( _fields );
	}

	/**
	 * Gets the constructors.
	 *
	 * @return   the constructors.
	 */
	public final List getConstructors()
	{
		return _constructors == null ? EMPTY_LIST : Collections.unmodifiableList( _constructors );
	}

	public final boolean isSubclassOf( String full_qualified_type_name )
	{
		return isSubclassOf( full_qualified_type_name, true );
	}

	public final boolean isImplementingInterface( String full_qualified_type_name )
	{
		return isImplementingInterface( full_qualified_type_name, true );
	}

	public String getType()
	{
		return getQualifiedName() + ".class";
	}

	public boolean isInner()
	{
		boolean hasContainingClass = getContainingClass() != null;
		return hasContainingClass;
	}

	public boolean isSubclassOf( String full_qualified_type_name, boolean superclasses )
	{
		XClass superclass = this.getSuperclass();

		if( superclass == null )
			return false;
		do
		{
			if( superclass.getQualifiedName().equals( full_qualified_type_name ) )
				return true;

			superclass = superclass.getSuperclass();
		}while ( superclasses == true && superclass != null );

		return false;
	}

	public boolean isImplementingInterface( String full_qualified_type_name, boolean superclasses )
	{
		XClass cur_class = this;

		do
		{
			for( Iterator iterator = cur_class.getInterfaces().iterator(); iterator.hasNext();  )
			{
				XClass intf = ( XClass ) iterator.next();

				//if intf is full_qualified_type_name directly or it extends from another interface which extends full_qualified_type_name
				if( intf.getQualifiedName().equals( full_qualified_type_name ) || intf.isImplementingInterface( full_qualified_type_name, superclasses ) )
					return true;
			}

			cur_class = cur_class.getSuperclass();
		}while ( superclasses == true && cur_class != null );

		return false;
	}

	public String getName()
	{
		return _name;
	}

	/**
	 * Describe what the method does
	 *
	 * @return   Describe the return value for method for return value
	 */
	public String getQualifiedName()
	{
		return _qualifiedName;
	}

	/**
	 * Gets the transformed class name, for example: <code>Character$Subset</code>
	 *
	 * @return   the transformed class name.
	 */
	public String getTransformedName()
	{
		return _transformedName;
	}

	/**
	 * Gets the transformed qualified class name, for example: <code>java.lang.Character$Subset</code>
	 *
	 * @return   the transformed qualified class name.
	 */
	public String getTransformedQualifiedName()
	{
		return _transformedQualifiedName;
	}

	/**
	 * Returns all the implemented interfaces (if this is a class) or all the
	 * extended interfaces (if this is an interface)
	 *
	 * @return   Describe the return value for method for return value
	 */
	public List getInterfaces()
	{
		// IMPORTANT: This method should not be called before all classes have been parsed.
		if( _allInterfaces == null )
		{
			// Temporarily use a Set
			Set allInterfaces = new HashSet();

			if( _declaredInterfaces != null )
			{
				allInterfaces.addAll( _declaredInterfaces );

				// Add all the declared interfaces' superinterfaces
				for( Iterator i = _declaredInterfaces.iterator(); i.hasNext();  )
				{
					XClass intf = ( XClass ) i.next();

					allInterfaces.addAll( intf.getInterfaces() );
				}
			}

			// Add all the superclasses' interfaces
			XClass superclass = getSuperclass();

			while( superclass != null )
			{
				allInterfaces.addAll( superclass.getInterfaces() );
				superclass = superclass.getSuperclass();
			}

			_allInterfaces = Arrays.asList( allInterfaces.toArray() );
		}
		return _allInterfaces;
	}

	/**
	 * Returns the interfaces that are declared in the source code. This excludes
	 * any interfaces that might be implicitly implemented. This method is only
	 * useful for CodeUnit, which compares source codes, and should normally not be
	 * called.
	 *
	 * @return   A Collection of XClass
	 */
	public Collection getDeclaredInterfaces()
	{
		return _declaredInterfaces != null ? _declaredInterfaces : EMPTY_LIST;
	}

	/**
	 * Describe what the method does
	 *
	 * @return   Describe the return value for method for return value
	 */
	public XClass getSuperclass()
	{
		return _superclass;
	}

	public List getDirectSubclasses()
	{
		if( isInterface() )
		{
			throw new UnsupportedOperationException( "Should never ask for directSubclasses of interfaces. Ask for implementingClasses or extendingInterfaces instead" );
		}
		if( _directSubclasses == null )
		{
			_directSubclasses = new LinkedList();

			for( Iterator classes = getXJavaDoc().getSourceClasses().iterator(); classes.hasNext();  )
			{
				XClass clazz = (XClass) classes.next();

				if( clazz.getSuperclass() == this )
				{
					_directSubclasses.add( clazz );
				}
			}
		}
		return Collections.unmodifiableList( _directSubclasses );
	}

	public List getAllSubclasses()
	{
		if( isInterface() )
		{
			throw new UnsupportedOperationException( "Should never ask for allSubclasses of interfaces. Ask for implementingClasses or extendingInterfaces instead" );
		}
		if( _allSubclasses == null )
		{
			_allSubclasses = new LinkedList();

			for( Iterator classes = getXJavaDoc().getSourceClasses().iterator(); classes.hasNext();  )
			{
				XClass clazz = (XClass) classes.next();

				while( clazz != null )
				{
					if( clazz.getSuperclass() == this )
					{
						_allSubclasses.add( clazz );
						break;
					}
					clazz = clazz.getSuperclass();
				}
			}
		}
		return Collections.unmodifiableList( _allSubclasses );
	}

	public List getImplementingClasses()
	{
		if( !isInterface() )
		{
			throw new UnsupportedOperationException( "Should never ask for implementingClasses of classes. Ask for directSubclasses or allSubclasses instead" );
		}
		if( _implementingClasses == null )
		{
			_implementingClasses = new LinkedList();

			for( Iterator classes = getXJavaDoc().getSourceClasses().iterator(); classes.hasNext();  )
			{
				XClass clazz = (XClass) classes.next();

				if( !clazz.isInterface() )
				{
					Collection interfaces = clazz.getInterfaces();

					if( interfaces.contains( this ) )
					{
						_implementingClasses.add( clazz );
					}
				}
			}
		}
		return Collections.unmodifiableList( _implementingClasses );
	}

	public List getExtendingInterfaces()
	{
		if( !isInterface() )
		{
			throw new UnsupportedOperationException( "Should never ask for extendingInterfaces of classes. Ask for directSubclasses or allSubclasses instead" );
		}
		if( _extendingInterfaces == null )
		{
			_extendingInterfaces = new LinkedList();

            for( Iterator classes = getXJavaDoc().getSourceClasses().iterator(); classes.hasNext();  )
            {
                XClass clazz = (XClass) classes.next();

				if( clazz.isInterface() )
				{
					Collection interfaces = clazz.getInterfaces();

					if( interfaces.contains( this ) )
					{
						_extendingInterfaces.add( clazz );
					}
				}
			}
		}
		return Collections.unmodifiableList( _extendingInterfaces );
	}

	/**
	 * Describe what the method does
	 *
	 * @return   Describe the return value
	 */
	public XPackage getContainingPackage()
	{
		if( _containingPackage == null )
		{
			_containingPackage = getXJavaDoc().addPackageMaybe( "" );
		}
		return _containingPackage;
	}

	/**
	 * Describe what the method does
	 *
	 * @return   Describe the return value
	 */
	public List getInnerClasses()
	{
		return _innerClasses == null ? EMPTY_LIST : Collections.unmodifiableList( _innerClasses );
	}
	/**
	 * Describe what the method does
	 *
	 * @return   Describe the return value
	 */
	public XProgramElement getSuperElement()
	{
		return getSuperclass();
	}

	public List getSuperInterfaceElements()
	{
		return getInterfaces();
	}

	public boolean isAnonymous()
	{
		return _isAnonymous;
	}

	public List getMethodTags( String tagName, boolean superclasses )
	{
		Set result = new HashSet();

		for( Iterator methods = getMethods( superclasses ).iterator(); methods.hasNext();  )
		{
			XMethod method = (XMethod) methods.next();

			result.addAll( method.getDoc().getTags( tagName, superclasses ) );
		}
		return new ArrayList( result );
	}

	public final int compareTo( Object o )
	{
		XClass other = ( XClass ) o;

		return getQualifiedName().compareTo( other.getQualifiedName() );
	}

	public final String toString()
	{
		return getQualifiedName();
	}

	public String save( File rootDir ) throws IOException
	{
		throw new UnsupportedOperationException( getClass().getName() );
	}

	public boolean equals( Object obj )
	{
		if( !( obj instanceof XClass ) )
		{
			return false;
		}

		XClass other_clazz = ( XClass ) obj;

		return getQualifiedName().equals( other_clazz.getQualifiedName() );
	}

	public int hashCode()
	{
		if( _hash == Integer.MIN_VALUE )
		{
			_hash += getQualifiedName().hashCode();
		}
		return _hash;
	}

	public XClass qualify( String unqualifiedClassName )
	{
		return getXJavaDoc().getXClass( unqualifiedClassName );
	}

	public long lastModified()
	{
		return Long.MIN_VALUE;
	}

	/**
	 * update javadoc
	 */
	public void updateDoc()
	{
		super.updateDoc();

		// update docs on fields, methods and constructors
		for( Iterator i = getFields().iterator(); i.hasNext();  )
		{
			( ( XField ) i.next() ).updateDoc();
		}
		for( Iterator i = getMethods().iterator(); i.hasNext();  )
		{
			( ( XMethod ) i.next() ).updateDoc();
		}
		for( Iterator i = getConstructors().iterator(); i.hasNext();  )
		{
			( ( XConstructor ) i.next() ).updateDoc();
		}

		for( Iterator i = getInnerClasses().iterator(); i.hasNext();  )
		{
			( ( XClass ) i.next() ).updateDoc();
		}
	}

	protected final boolean hasImportedClasses()
	{
		return _importedClasses != null;
	}

	protected final boolean hasInnerClasses()
	{
		return _innerClasses != null;
	}

	protected final boolean hasImportedPackages()
	{
		return _importedPackages != null;
	}

	protected void addInnerClass( XClass clazz )
	{
		if( _innerClasses == null )
		{
			_innerClasses = new LinkedList();
		}
		_innerClasses.add( clazz );
	}

	/**
	 * Sets the qualified name of the class. Should only be called on objects that
	 * represent outer classes.
	 *
	 * @param qualifiedName  The new QualifiedName value
	 */
	final void setQualifiedName( String qualifiedName )
	{
		if( qualifiedName == null )
		{
			throw new IllegalArgumentException( "qualifiedName can't be null!" );
		}
		if( qualifiedName.startsWith( "." ) )
		{
			throw new IllegalArgumentException( "qualifiedName can't start with a dot! " + qualifiedName );
		}
		if( _qualifiedName != null )
		{
			throw new IllegalStateException( "Setting qualified name " + qualifiedName + " from " + _qualifiedName + " 2nd time!" );
		}
		if( isInner() )
		{
			throw new IllegalStateException( "Don't call setQualifiedName for inner classes. Call setName instead. (" + qualifiedName + ")" );
		}
		_qualifiedName = qualifiedName;
		_transformedQualifiedName = qualifiedName;
		_name = Util.classNameFromQualifiedClassName( _qualifiedName );
		_transformedName = _name;
	}

	/**
	 * Sets the ContainingPackage attribute of the AbstractProgramElement object
	 * Use package name specified in class, and do not complain about directory
	 * struct.
	 *
	 * @param containingPackage  The new ContainingPackage value
	 */
	final void setContainingPackage( String containingPackage )
	{
		_containingPackage = getXJavaDoc().addPackageMaybe( containingPackage );
	}

	/**
	 * Sets the Interface attribute of the SourceClass object
	 *
	 * @param flag  The new Interface value
	 */
	final void setInterface( boolean flag )
	{
		_isInterface = flag;
		_superclass = null;
	}

	/**
	 * Sets the SuperClass attribute of the SourceClass object
	 *
	 * @param superclass  The new Superclass value
	 */
	final void setSuperclass( String superclass )
	{
		_superclass = qualify( superclass );
		// Now tell the superclass and all its superclasses
		// that we're a subclass (Except java.lang.Object)
		/*
		 * XClass superclass = _superclass;
		 * while(superclass.getQualifiedName().equals("java.lang.Object")) {
		 * if(superclass._subclasses == null) {
		 * superclass._subclasses=new TreeSet();
		 * }
		 * superclass._subclasses.add(this);
		 * }
		 */
	}

	/**
	 * This method is called for anonymous classes only. Anon classes come in 2
	 * flavours. They either realise a class or an interface.
	 *
	 * @param clazz
	 */
	final void setRealised( String clazz )
	{
		_isAnonymous = true;

		XClass realised = qualify( clazz );

		if( realised.isInterface() )
		{
			// We're realising an interface
			addInterface( clazz );
			setSuperclass( "java.lang.Object" );
		}
		else
		{
			// We're realising a class
			setSuperclass( clazz );
		}
	}

	/**
	 * Sets the unqualified name of the class. Should only be called on objects
	 * that represent inner classes.
	 *
	 * @param name  The new Name value
	 */
	void setName( String name )
	{
		if( !isInner() )
		{
			throw new IllegalStateException( "Don't call setName for outer classes. Call setQualifiedName instead. (" + name + ")" );
		}
		if( name == null )
		{
			throw new IllegalStateException( "name can't be null!" );
		}

		// The *real* name is the containing class' name + '.' + the name

		String realName = getContainingClass().getName() + '.' + name;

		// The *transformed* name is the containing class' transformed name + '$' + the name

		String transformedName = getContainingClass().getTransformedName() + '$' + name;

		if( _name != null && !_name.equals( realName ) )
		{
			throw new IllegalStateException( "Setting name 2nd time with a different value! 1st time: '" + _name + "', 2nd time: '" + name + "'" );
		}
		_name = realName;
		_transformedName = transformedName;

		if( getContainingPackage().getName().equals( "" ) )
		{
			_qualifiedName = _name;
			_transformedQualifiedName = _transformedName;
		}
		else
		{
			_qualifiedName = getContainingPackage().getName() + '.' + _name;
			_transformedQualifiedName = getContainingPackage().getName() + '.' + _transformedName;
		}
		if( _qualifiedName.startsWith( "." ) )
		{
			throw new IllegalStateException( "qualifiedName can't start with a dot! " + _qualifiedName );
		}
	}

	/**
	 * Adds an interface that this class implements (if this is a class) or an
	 * interface that this interface extends (if this is an interface)
	 *
	 * @param interfaceName  Describe the method parameter
	 */
	final void addInterface( String interfaceName )
	{
		if( _declaredInterfaces == null )
		{
			_declaredInterfaces = new LinkedList();
		}

		XClass qualifiedInterface = qualify( interfaceName );

		_declaredInterfaces.add( qualifiedInterface );
	}

	void resolveImportedClasses()
	{
		if( _importedClassNames == null )
		{
			// No imported classes
			return;
		}

		// The first time we're called, none of the imported classes are resolved.
		if( _importedClasses == null )
		{
			_importedClasses = new ArrayList( _importedClassNames.size() );
			for( Iterator i = _importedClassNames.iterator(); i.hasNext();  )
			{
				String importedClassName = ( String ) i.next();

				_importedClasses.add( qualify( importedClassName ) );
			}
		}
	}
	/**
	 * Sets the ImportedClasses attribute of the AbstractClass object
	 *
	 * @param importedClass  Describe the method parameter
	 */
	void addImportedClass( String importedClass )
	{
		if( _importedClassNames == null )
		{
			_importedClassNames = new LinkedList();
		}
		_importedClassNames.add( importedClass );
	}
	/**
	 * Sets the ImportedPackages attribute of the AbstractClass object
	 *
	 * @param importedPackage  Describe the method parameter
	 */
	void addImportedPackage( String importedPackage )
	{
		if( _importedPackages == null )
		{
			_importedPackages = new LinkedList();
		}

		XPackage pakkage = getXJavaDoc().addPackageMaybe( importedPackage );

		_importedPackages.add( pakkage );
	}

	/**
	 * Gets the Constructors attribute of the AbstractClass object
	 *
	 * @param constructor  Describe the method parameter
	 */
	void addConstructor( XConstructor constructor )
	{
		validate( constructor );
		if( _constructors == null )
		{
			_constructors = new LinkedList();
		}
		_constructors.add( constructor );
	}

	/**
	 * Gets the Fields attribute of the AbstractClass object
	 *
	 * @param field  Describe the method parameter
	 */
	void addField( XField field )
	{
		validate( field );
		if( _fields == null )
		{
			_fields = new LinkedList();
		}
		_fields.add( field );
	}
	/**
	 * Gets the Methods attribute of the AbstractClass object
	 *
	 * @param method  Describe the method parameter
	 */
	void addMethod( XMethod method )
	{
		validate( method );
		if( _methods == null )
		{
			_methods = new LinkedList();
		}

		_methods.add( method );
	}

	void reset()
	{
		super.reset();

		_declaredInterfaces = null;
		_allInterfaces = null;
		_importedClasses = null;
		_importedClassNames = null;
		_importedPackages = null;
		_constructors = null;
		_namedConstructors = null;
		_methods = null;
		_namedMethods = null;
		_fields = null;
		_innerClasses = null;
	}

	/**
	 * Returns all the fields or methods.
	 *
	 * @param forFields     true if you want the fields, false if you want methods
	 * @param superclasses
	 * @return   A List of XMember
	 */
	private final List getMembers( boolean superclasses, boolean forFields )
	{
		if( !superclasses )
		{
			if( forFields )
			{
				return _fields == null ? EMPTY_LIST : Collections.unmodifiableList( _fields );
			}
			else
			{
				return _methods == null ? EMPTY_LIST : Collections.unmodifiableList( _methods );
			}
		}
		else
		{
			// Make a new Collection where we append all methods from this and super
			LinkedList members = new LinkedList();

			// Add methods from this class if any.
			if( forFields )
			{
				if( _fields != null )
				{
					members.addAll( _fields );
				}
			}
			else
			{
				if( _methods != null )
				{
					members.addAll( _methods );
				}
			}

			// Now add members from super
			AbstractClass superclass = ( AbstractClass ) getSuperclass();

			if( superclass != null )
			{
				Collection superMembers = superclass.getMembers( true, forFields );

				// Iterate over the superclass methods. Don't add methods that exist in subclasses
				// (overridden methods)
				for( Iterator m = superMembers.iterator(); m.hasNext();  )
				{
					XMember superMember = ( XMember ) m.next();

					if( !superMember.isPrivate() && !members.contains( superMember ) )
					{
						members.add( superMember );
					}
				}
			}

			// Add members from interfaces too.
			Collection interfaces = getInterfaces();

			for( Iterator i = interfaces.iterator(); i.hasNext();  )
			{
				AbstractClass interfaze = ( AbstractClass ) i.next();

				// We're iterating over every interface, so we *don't* ask for the super members
				Collection interfaceMembers = interfaze.getMembers( false, forFields );

				// Iterate over the interface methods. Don't add methods that exist in the base class
				// or any of the immediate superclasses. Interface methods in the back of the line!!
				// (overridden methods)
				for( Iterator m = interfaceMembers.iterator(); m.hasNext();  )
				{
					XMember interfaceMember = ( XMember ) m.next();

					if( !members.contains( interfaceMember ) )
					{
						members.add( interfaceMember );
					}
				}
			}

			return Collections.unmodifiableList( members );
		}
	}

	private final void validate( XMember member ) throws IllegalStateException
	{
		if( member.getName() == null )
		{
			throw new IllegalStateException( "Trying to add a member with no name:" + member.getClass().getName() + ":" + hashCode() );
		}
	}

	private void initializeNamedMethodsHashMap()
	{
		if( _namedMethods != null || _methods == null )
			return;

		_namedMethods = new HashMap();

		for( int i = 0; i < _methods.size(); i++ )
		{
			XMethod method = ( XMethod ) _methods.get( i );

			_namedMethods.put( method.getNameWithSignature( false ), method );
		}
	}

	private void initializeNamedConstructorsHashMap()
	{
		if( _namedConstructors != null || _constructors == null )
			return;

		_namedConstructors = new HashMap();

		for( int i = 0; i < _constructors.size(); i++ )
		{
			XConstructor constructor = ( XConstructor ) _constructors.get( i );

			_namedConstructors.put( constructor.getNameWithSignature( false ), constructor );
		}
	}
}

