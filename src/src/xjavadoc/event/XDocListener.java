/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc.event;

/**
 * Describe what this class does
 *
 * @author         Aslak Hellesøy
 * @created        30. januar 2002
 * @todo-javadoc   Write javadocs for interface
 */
public interface XDocListener extends java.util.EventListener
{
	/**
	 * Describe what the method does
	 *
	 * @param event    Describe what the parameter does
	 * @todo-javadoc   Write javadocs for method
	 * @todo-javadoc   Write javadocs for method parameter
	 */
	void docChanged( XDocEvent event );
}
