/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

/**
 * Everything that can have a type implements this interface
 *
 * @author    Aslak Hellesøy
 * @created   25. februar 2003
 */
public interface Type
{
	/**
	 * Returns the dimension as an int
	 *
	 * @return   dimension as an int
	 */
	int getDimension();

	/**
	 * Returns the dimension as a String, "", "[]", "[][]" etc.
	 *
	 * @return   dimension as a String
	 */
	String getDimensionAsString();

	/**
	 * Get type
	 *
	 * @return   type
	 */
	XClass getType();
}
