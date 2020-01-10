/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

import org.apache.commons.collections.Predicate;

/**
 * A class that can validate tags. It reuses logic from predicates.
 *
 * @author    Aslak Hellesøy
 * @created   24. februar 2003
 * @version   $Revision: 1.3 $
 */
public class TagValidator
{
	private Predicate  _predicate;

	public TagValidator( Predicate predicate )
	{
		setPredicate( predicate );
	}

	public void setPredicate( Predicate predicate )
	{
		_predicate = predicate;

	}

	public void validate( XTag tag ) throws TagValidationException
	{
		if( !_predicate.evaluate( tag ) )
		{
			throw new TagValidationException( "Validation error", tag );
		}
	}
}
