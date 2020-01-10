/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

import java.util.*;

/**
 * Describe what this class does
 *
 * @author         Aslak Hellesøy
 * @created        3. januar 2002
 * @todo-javadoc   Write javadocs
 */
public final class XPackage implements Comparable
{
	/**
	 * @todo-javadoc   Describe the field
	 */
	private String     _name;
	/**
	 * @todo-javadoc   Describe the field
	 */
	private List       _classes = new LinkedList();

//    private LinkedList _sourceClasses = new LinkedList();

	/**
	 * Describe what the XPackage constructor does
	 *
	 * @param name     Describe what the parameter does
	 * @todo-javadoc   Write javadocs for constructor
	 * @todo-javadoc   Write javadocs for method parameter
	 */
	public XPackage( String name )
	{
		_name = name;
	}

	/**
	 * Gets the DefaultPackage attribute of the XPackage object
	 *
	 * @return   The DefaultPackage value
	 */
	public final boolean isDefaultPackage()
	{
		return getName().equals( "" );
	}

	/**
	 * Describe what the method does
	 *
	 * @return         Describe the return value
	 * @todo-javadoc   Write javadocs for method
	 * @todo-javadoc   Write javadocs for return value
	 */
	public final String getName()
	{
		return _name;
	}

	/**
	 * Describe what the method does
	 *
	 * @return         Describe the return value
	 * @todo-javadoc   Write javadocs for method
	 * @todo-javadoc   Write javadocs for return value
	 */
	public Collection getClasses()
	{
		return Collections.unmodifiableCollection( _classes );
	}

	/**
	 * Describe what the method does
	 *
	 * @return         Describe the return value
	 * @todo-javadoc   Write javadocs for method
	 * @todo-javadoc   Write javadocs for return value
	 */
	public String toString()
	{
		return getName();
	}

	/**
	 * Describe what the method does
	 *
	 * @return         Describe the return value
	 * @todo-javadoc   Write javadocs for method
	 * @todo-javadoc   Write javadocs for return value
	 */
	public int hashCode()
	{
		return getName().hashCode();
	}

	/**
	 * Describe what the method does
	 *
	 * @param o        Describe what the parameter does
	 * @return         Describe the return value
	 * @todo-javadoc   Write javadocs for method
	 * @todo-javadoc   Write javadocs for method parameter
	 * @todo-javadoc   Write javadocs for return value
	 */
	public int compareTo( Object o )
	{
		XPackage other = ( XPackage ) o;

		return getName().compareTo( other.getName() );
	}

	/**
	 * Describe the method
	 *
	 * @param clazz    Describe the method parameter
	 * @todo-javadoc   Write javadocs for return value
	 * @todo-javadoc   Describe the method
	 * @todo-javadoc   Describe the method parameter
	 */
	void addClass( XClass clazz )
	{
		// This is to avoid dupes. There might be a proxy already in there. Remove it
		// if a real class comes after..
		_classes.remove( clazz );
		_classes.add( clazz );
	}
}
