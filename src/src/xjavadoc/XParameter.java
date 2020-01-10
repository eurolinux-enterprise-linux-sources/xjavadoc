/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

/**
 * @author    Aslak Hellesøy
 * @created   25. februar 2003
 */
public interface XParameter extends Type, Named
{
	/**
	 * Gets the param tag for this parameter.
	 *
	 * @return   the param tag for this parameter, or null if none is specified.
	 */
	XTag getParamTag();

	/**
	 * Gets the description of this parameter. This is the text in the param tag
	 * preceding the first token.
	 *
	 * @return   the description of this parameter, or null if there is no
	 *      corresponding param tag.
	 */
	String getDescription();
}

