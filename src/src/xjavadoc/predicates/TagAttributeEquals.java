/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc.predicates;

import xjavadoc.*;

/**
 * Filter that accepts program elements that have a tag attribute equal to a
 * certain value
 *
 * @created   29. juli 2002
 */
public class TagAttributeEquals extends ProgramElementPredicate
{
	private String     _tagName;
	private String     _attributeName;
	private String     _attributeValue;

	public TagAttributeEquals( String tagName, String attributeName, String attributeValue )
	{
		_tagName = tagName;
		_attributeName = attributeName;
		_attributeValue = attributeValue;
	}

	public void setTagName( String tagName )
	{
		_tagName = tagName;
	}
	public void setAttributeName( String attributeName )
	{
		_attributeName = attributeName;
	}
	public void setAttributeValue( String attributeValue )
	{
		_attributeValue = attributeValue;
	}

	protected boolean evaluate( XProgramElement programElement )
	{
		return _attributeValue.equals( programElement.getDoc().getTagAttributeValue( _tagName, _attributeName ) );
	}
}

