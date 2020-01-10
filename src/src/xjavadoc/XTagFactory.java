/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Creates XTag instances.
 *
 * @author    Aslak Hellesøy
 * @created   10. februar 2002
 */
public final class XTagFactory
{

	/**
	 * Maps tag name to XTag class.
	 */
	private final Map _tagClasses = new HashMap();
	private boolean _isValidating = false;

    public XTagFactory() {
		// ignore standard tags. See:
		// http://java.sun.com/j2se/1.4.1/docs/tooldocs/windows/javadoc.html#javadoctags
		setIgnoredTags( "author,deprecated,exception,param,return,see,serial,serialData,serialField,since,throws,version" );
	}

	public boolean isValidating()
	{
		return _isValidating;
	}

	public void setValidating( boolean isValidating )
	{
		_isValidating = isValidating;
	}

	/**
	 * Set the name of the tags that shouldn't be validated against.
	 *
	 * @param tags
	 */
	public void setIgnoredTags( String tags )
	{
		StringTokenizer st = new StringTokenizer( tags, "," );

		while( st.hasMoreTokens() )
		{
			registerTagClass( st.nextToken(), DefaultXTag.class );
		}
	}

	/**
	 * Creates a new XTag. If a special tag class has been previously registeres,
	 * an instance of the corresponding class will be returned. This allows for
	 * special tag implementations.
	 *
	 * @param tagName                  name of the tag, without the '@'
	 * @param text                     content of the tag. Will be parsed into
	 *      attributes.
	 * @param doc
	 * @param lineNumber
	 * @return                         an instance of XTag
	 * @exception UnknownTagException
	 * @throws TagValidationException  if validation is activated and an unknown
	 *      tag was encountered.
	 */
	public XTag createTag( String tagName, String text, XDoc doc, int lineNumber ) throws UnknownTagException
	{
		tagName = XDoc.dotted( tagName );

		// Let's see if there is a custom class for that tag
		Class tagClass = ( Class ) _tagClasses.get( tagName );
		DefaultXTag tag;

		if( tagClass != null )
		{
			try
			{
				tag = ( DefaultXTag ) tagClass.newInstance();
			}
			catch( InstantiationException e )
			{
				e.printStackTrace();
				throw new IllegalStateException( e.getMessage() );
			}
			catch( IllegalAccessException e )
			{
				e.printStackTrace();
				throw new IllegalStateException( e.getMessage() );
			}
		}
		else
		{
			tag = new DefaultXTag();
		}
		tag.init( tagName, text, doc, lineNumber );

		// Throw validation ex if validation is on and the tag is unknown
		if( _isValidating && ( tagClass == null ) )
		{
			throw new UnknownTagException( tag );
		}
		return tag;
	}

	public void registerTagClass( String tagName, Class tagClass )
	{
        Class old = (Class) _tagClasses.get( XDoc.dotted(tagName) );
        if( old != null ) {
            throw new IllegalStateException( "The tag @" + XDoc.dotted(tagName) +
                    " has already been mapped to " + old.getName() +
                    ". Can't reregister it to " + tagClass.getName());
        }
		_tagClasses.put( XDoc.dotted( tagName ), tagClass );
	}
}
