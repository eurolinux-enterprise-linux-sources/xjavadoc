/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

import java.util.Collection;
import xjavadoc.event.XTagListener;

/**
 * @author    Aslak Hellesøy
 * @created   11. januar 2002
 */
public interface XTag
{
	/**
	 * Returns the value of the tag parameter with the given name, or null if none
	 * exist;
	 *
	 * @param attributeName  Describe what the parameter does
	 * @return               The Parameter value
	 */
	String getAttributeValue( String attributeName );

	/**
	 * Returns all tag attribute names, in the order they occur in the source.
	 *
	 * @return   The Parameters value
	 */
	Collection getAttributeNames();

	/**
	 * Returns the full name of the tag, excluding the @
	 *
	 * @return   Describe the return value
	 */
	String getName();

	/**
	 * Returns the full value of the tag.
	 *
	 * @return   Describe the return value
	 */
	String getValue();

	/**
	 * Returns the XDoc object we belong to.
	 *
	 * @return   the XDoc object we belong to.
	 */
	public XDoc getDoc();

	public int getLineNumber();

	public String getInfo();

	/**
	 * Adds a parameter
	 *
	 * @param attributeName   name of the attribute
	 * @param attributeValue  value of the attribute
	 */
	void setAttribute( String attributeName, String attributeValue );

	String removeAttribute( String attributeName );

	public void addTagListener( XTagListener tagListener );

	public void removeTagListener( XTagListener tagListener );

	boolean equals( Object o );

	int hashCode();

	/**
	 * Validates the tag.
	 *
	 * @exception TagValidationException  if the content of the tag is somehow
	 *      invalid
	 */
	public void validate() throws TagValidationException;
}

