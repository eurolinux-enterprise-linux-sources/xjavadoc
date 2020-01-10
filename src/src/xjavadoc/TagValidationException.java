/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

/**
 * This exception will be thrown if a tag is misnamed or has bad content. This
 * exception is a RuntimeException, in order to not break the existing API.
 *
 * @author    Aslak Hellesøy
 * @created   9. februar 2003
 * @version   $Revision: 1.4 $
 */
public class TagValidationException extends RuntimeException
{
	private XTag       _tag;

	public TagValidationException( String message, XTag tag )
	{
        super( message );
		_tag = tag;
	}

	public final XTag getTag()
	{
		return _tag;
	}

    public String getMessage()
    {
        return getMessage() + ":" + getTag().getInfo();
    }
}
