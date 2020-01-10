/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc.predicates;

import org.apache.commons.collections.Predicate;
import xjavadoc.XProgramElement;

/**
 * @created   6. oktober 2002
 */
public abstract class ProgramElementPredicate implements Predicate
{
	public final boolean evaluate( Object o )
	{
		if( !( o instanceof XProgramElement ) )
		{
			throw new IllegalArgumentException( "o must be of type XProgramElement, but was: " + o.getClass().getName() );
		}

		XProgramElement programElement = ( XProgramElement ) o;

		return evaluate( programElement );
	}

	protected abstract boolean evaluate( XProgramElement programElement );
}

