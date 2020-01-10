/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

/**
 * @author    Aslak Hellesøy
 * @created   9. februar 2003
 * @version   $Revision: 1.4 $
 */
public class UnknownTagException extends TagValidationException
{
	public UnknownTagException( XTag tag )
	{
		super( "Unknown tag", tag );
	}
}
