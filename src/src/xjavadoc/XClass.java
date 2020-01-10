/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

import org.apache.commons.collections.Predicate;

import java.util.List;
import java.io.File;
import java.io.IOException;

/**
 * This class represents any type: source class, binary class or primitive type.
 *
 * @author    Aslak Hellesøy
 * @created   7. mars 2003
 */
public interface XClass extends XType
{
	/**
	 * Returns true if this is an inner class.
	 *
	 * @return   true if this is an inner class.
	 */
	public boolean isInner();

	/**
	 * Returns the containing class, if this is an inner class.
	 *
	 * @return   the containing class.
	 */
	public XClass getContainingClass();

	/**
	 * Returns true if this class is a primitive. That is, one of the following:
	 *
	 * <ul>
	 *   <li> boolean</li>
	 *   <li> byte</li>
	 *   <li> char</li>
	 *   <li> double</li>
	 *   <li> float</li>
	 *   <li> int</li>
	 *   <li> long</li>
	 *   <li> short</li>
	 *   <li> java.lang.Boolean</li>
	 *   <li> java.lang.Byte</li>
	 *   <li> java.lang.Character</li>
	 *   <li> java.lang.Double</li>
	 *   <li> java.lang.Float</li>
	 *   <li> java.lang.Integer</li>
	 *   <li> java.lang.Long</li>
	 *   <li> java.lang.Short</li>
	 *   <li> java.lang.String</li>
	 * </ul>
	 *
	 *
	 * @return true if a primitive
	 */
	public boolean isPrimitive();

	/**
	 * Returns true if this class is anonymous.
	 *
	 * @return   true if this class is anonymous.
	 */
	public boolean isAnonymous();

	/**
	 * Gets the qualified class name.
	 *
	 * @return   the qualified class name.
	 */
	String getQualifiedName();

	/**
	 * Gets the transformed class name, for example: <code>Character$Subset</code>
	 *
	 * @return   the transformed class name.
	 */
	String getTransformedName();

	/**
	 * Gets the transformed qualified class name, for example: <code>java.lang.Character$Subset</code>
	 *
	 * @return   the transformed qualified class name.
	 */
	String getTransformedQualifiedName();

	/**
	 * Gets the type, e.g. <code>java.lang.String.class</code> or <code>java.lang.Integer.TYPE</code>
	 * .
	 *
	 * @return   the qualified class name.
	 */
	String getType();

	/**
	 * Gets the constructor with the given signature.
	 *
	 * @param constructorNameWithSignature  the signature of the constructor, e.g.
	 *      <code>Foo(int,java.lang.String)>/code>.
	 *
	 *
	 *
	 *
	 *
	 * @return                              the constructor.
	 */
	XConstructor getConstructor( String constructorNameWithSignature );

	XField getField( String name );

	boolean isAbstract();

	/**
	 * Returns true if we are subclass or implement the class/interface with the
	 * name classOrInterfaceName
	 *
	 * @param full_qualified_type_name  The full qualified type name
	 * @return                          true if of the specified type; false
	 *      otherwise
	 */
	boolean isA( String full_qualified_type_name );

	/**
	 * Returns true if we are subclass or implement the class/interface with the
	 * name classOrInterfaceName
	 *
	 * @param full_qualified_type_name  The full qualified type name
	 * @param superclases               whether the isA search should search the
	 *      whole hierarchy
	 * @return                          true if of the specified type; false
	 *      otherwise
	 */
	boolean isA( String full_qualified_type_name, boolean superclases );

	/**
	 * Return superclass of this class. If this class represents an interface, null
	 * will be returned.
	 *
	 * @return   superclass of this class
	 */
	XClass getSuperclass();

	/**
	 * Returns the (known) direct subclasses. If this instance represents an
	 * interface, UnsupportedOperationException will be thrown. This can be avoided
	 * by testing with isInterface() prior to calling this method.
	 *
	 * @return   the (known) subclasses
	 */
	List getDirectSubclasses();

	/**
	 * Returns the (known) subclasses, regardless of how deep in the class
	 * hierarchy. If this instance represents an interface,
	 * UnsupportedOperationException will be thrown. This can be avoided by testing
	 * with isInterface() prior to calling this method.
	 *
	 * @return   the (known) subclasses
	 */
	List getAllSubclasses();

	/**
	 * Return the (known) classes that implement this interface. If this instance
	 * represents a class, an UnsupportedOperationException will be thrown. This
	 * can be avoided by testing with isInterface() prior to calling this method.
	 *
	 * @return   the (known) subinterfaces
	 */
	List getImplementingClasses();

	/**
	 * Return the (known) interfaces that extend this interface. If this instance
	 * represents a class, an UnsupportedOperationException will be thrown. This
	 * can be avoided by testing with isInterface() prior to calling this method.
	 *
	 * @return   the (known) extending interfaces
	 */
	List getExtendingInterfaces();

	/**
	 * Returns all the interfaces implemented by this class. If this class
	 * represents an interface, it will return all the interfaces that this
	 * interface extends.
	 *
	 * @return   a Collection of {@link XClass}.
	 */
	List getInterfaces();

	/**
	 * Returns an XMethod with the given name and parameters. Example:
	 * getMethod("hello",new String[]{"java.lang.String","int"});
	 *
	 * @param methodNameWithSignature  Describe what the parameter does
	 * @param superclasses             Looks in superclasses too if true
	 * @return                         The XMethod if found, otherwise null
	 */
	XMethod getMethod( String methodNameWithSignature, boolean superclasses );

	/**
	 * @param methodNameWithSignature
	 * @return                         The XMethod if found, otherwise null
	 */
	XMethod getMethod( String methodNameWithSignature );

	String save( File rootDir ) throws IOException;

	/**
	 * Returns the package this class lives in.
	 *
	 * @return   the package this class lives in.
	 */
	XPackage getContainingPackage();

	/**
	 * Returns the imported classes.
	 *
	 * @return   a Collection of {@link XClass}.
	 */
	List getImportedClasses();

	/**
	 * Returns the inner classes.
	 *
	 * @return   a Collection of {@link XClass}.
	 */
	List getInnerClasses();

	/**
	 * Returns all the methods.
	 *
	 * @param superclasses  if true, include methods from superclasses and
	 *      interfaces too.
	 * @return              A collection of XMethod objects
	 */
	List getMethods( boolean superclasses );

	/**
	 * Returns all the methods that are accepted by the filter.
	 *
	 * @param superclasses  if true, include methods from superclasses too.
	 * @param predicate
	 * @return              A collection of XMethod objects
	 */
	List getMethods( Predicate predicate, boolean superclasses );

	/**
	 * Returns all the methods, not including superclasses
	 *
	 * @return   A collection of XMethod objects
	 */
	List getMethods();

	List getFields();

	List getFields( boolean superclasses );

	List getConstructors();

	List getImportedPackages();

	/**
	 * Returns true if the superclass (or recursively superclass of superclass) is
	 * full_qualified_type_name.
	 *
	 * @param full_qualified_type_name  Describe what the parameter does
	 * @return                          Describe the return value
	 */
	boolean isSubclassOf( String full_qualified_type_name );

	/**
	 * Returns true if the superclass (or recursively superclass of superclass, if
	 * superclasses==true) is full_qualified_type_name.
	 *
	 * @param full_qualified_type_name  Describe what the parameter does
	 * @param superclasses              Looks in superclasses too if true
	 * @return                          Describe the return value
	 */
	boolean isSubclassOf( String full_qualified_type_name, boolean superclasses );

	/**
	 * Returns true if it implements full_qualified_type_name (or recursively
	 * superclasses implement).
	 *
	 * @param full_qualified_type_name  Describe what the parameter does
	 * @return                          Describe the return value
	 */
	boolean isImplementingInterface( String full_qualified_type_name );

	/**
	 * Returns true if it implements full_qualified_type_name (or recursively
	 * superclasses implement, if superclasses==true).
	 *
	 * @param full_qualified_type_name  Describe what the parameter does
	 * @param superclasses              Looks in superclasses too if true
	 * @return                          Describe the return value
	 */
	boolean isImplementingInterface( String full_qualified_type_name, boolean superclasses );

	/**
	 * Returns true if this instance can be saved.
	 *
	 * @return   The Writeable value
	 */
	boolean isWriteable();

	/**
	 * mark this class dirty for saving
	 */
	void setDirty();

	/**
	 * whether class needs saving
	 *
	 * @return true if save needed
	 */
	boolean saveNeeded();

	/**
	 * @return   the time that this class was last modified
	 */
	long lastModified();

	boolean isInterface();

	/**
	 * Returns a collection of tags from the methods in this class (or
	 * superclasses).
	 *
	 * @param superclasses
	 * @param tagName
	 * @return              a List of {@link XTag}. If no tags are found, an empty
	 *      List is returned.
	 */
	List getMethodTags( String tagName, boolean superclasses );

	XClass qualify( String unqualifiedClassName );
}
