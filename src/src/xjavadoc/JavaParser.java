/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

/**
 * Describe what this class does
 *
 * @author         Aslak Hellesøy
 * @created        7. mars 2002
 */
interface JavaParser
{
	/**
	 * Populates the class by parsing its source.
	 *
	 * @param sourceClass         the XClass object to populate
	 * @exception ParseException  if the parsed file is not compliant with the Java
	 *      grammar
	 */
	void populate( SourceClass sourceClass ) throws ParseException;

	/**
	 * Gets the Token attribute of the JavaParser object.
	 *
	 * @param i        Describe what the parameter does
	 * @return         The Token value
	 */
	Token getToken( int i );
}