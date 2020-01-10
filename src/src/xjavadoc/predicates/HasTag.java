/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc.predicates;

import xjavadoc.XProgramElement;

/**
 * Filter that accepts program elements that have a certain tag
 *
 * @created   29. juli 2002
 */
public class HasTag extends ProgramElementPredicate
{
	private String     _tagName;

	public HasTag()
	{
	}

	public HasTag( String tagName )
	{
		setTagName( tagName );
	}

	public void setTagName( String tagName )
	{
		_tagName = tagName;
	}

	protected boolean evaluate( XProgramElement programElement )
	{
		return programElement.getDoc().hasTag( _tagName );
	}
}

