/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

/**
 * Describe what this class does
 *
 * @author         Ara Abrahamian
 * @author         Aslak Hellesøy
 * @created        February 16, 2002
 * @todo-javadoc   Write javadocs for interface
 */
public interface XField extends XMember, Type
{
	/**
	 * Gets the Transient attribute of the XField object
	 *
	 * @return   The Transient value
	 */
	boolean isTransient();

	/**
	 * Gets the Volatile attribute of the XField object
	 *
	 * @return   The Volatile value
	 */
	boolean isVolatile();
}
