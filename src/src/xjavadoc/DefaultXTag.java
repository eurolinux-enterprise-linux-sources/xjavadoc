/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

import java.util.*;

import xjavadoc.event.XTagListener;
import xjavadoc.event.XTagEvent;
import xjavadoc.filesystem.AbstractFile;

/**
 * @author    Aslak Hellesøy
 * @created   11. januar 2002
 */
public class DefaultXTag implements XTag
{
	public static int  instanceCount = 0;

	/**
	 * tag name
	 */
	private String     _name;

	/**
	 * string representation of tag
	 */
	private String     _value;

	/**
	 * attribute map
	 */
	private Map        _attributes;

	/**
	 * Ordered List of attribute names
	 */
	private List       _attributeNames = null;

	/**
	 * tag parse status
	 */
	private boolean    _isParsed = false;

	private int        _hash = Integer.MIN_VALUE;

	/**
	 * indicate dirty state
	 */
	private boolean    _isDirty = false;

	private XDoc       _doc;

	/**
	 * tag listeners interested in changes. This would be parent xdoc
	 */
	private Set        _tagListeners;

	private int        _lineNumber;

    private XJavaDoc _xJavaDoc;

	public DefaultXTag()
	{
		instanceCount++;
	}

	/**
	 * Skips whitespaces, starting from index i till the first non-whitespace
	 * character or end of s and returns the new index.
	 *
	 * @param s  Describe what the parameter does
	 * @param i  Describe what the parameter does
	 * @return   Describe the return value
	 */
	private static int skipWhitespace( String s, int i )
	{
		while( i < s.length() && Character.isWhitespace( s.charAt( i ) ) )
		{
			i++;
		}
		return i;
	}

	public final XDoc getDoc()
	{
		return _doc;
	}

	/**
	 * Returns the first tag parameter with the given name, or null if none exist;
	 *
	 * @param attributeName  Describe what the parameter does
	 * @return               The Parameter value
	 */
	public final String getAttributeValue( String attributeName )
	{
		if( !_isParsed )
		{
			parse();
		}

		return _attributes == null ? null : ( String ) _attributes.get( attributeName );
	}

	/**
	 * Returns all tag parameters with the given name, or an empty List if none
	 * exist;
	 *
	 * @return   The Parameters value
	 */
	public final Collection getAttributeNames()
	{
		if( !_isParsed )
		{
			parse();
		}
		return _attributeNames == null ? AbstractProgramElement.EMPTY_LIST : _attributeNames;
	}

	/**
	 * Returns the full name of the tag, excluding the @
	 *
	 * @return   tag name
	 */
	public final String getName()
	{
		if( !_isParsed )
		{
			parse();
		}
		return _name;
	}

	/**
	 * Returns the full value of the tag.
	 *
	 * @return   full value of the tag
	 */
	public final String getValue()
	{
		return _value;
	}

	public final int getLineNumber()
	{
		return _lineNumber;
	}

	public final String getInfo()
	{
		XProgramElement pe = getDoc().getOwner();
		AbstractFile file = XJavaDoc.getSourceFileFor( pe );

		return "@" + getName() + " at " + file.getPath() + ":" + getLineNumber();
	}

	/**
	 * Adds a parameter
	 *
	 * @param attributeName   The new Attribute value
	 * @param attributeValue  The new Attribute value
	 */
	public final void setAttribute( String attributeName, String attributeValue )
	{
		if( !_isParsed )
		{
			parse();
		}
		setAttribute_Impl( attributeName, attributeValue );
		fireTagChanged();
		_isDirty = true;
		_value = null;
	}

	/**
	 * add doc listener interested in chages
	 *
	 * @param tagListener  The feature to be added to the TagListener attribute
	 */
	public final void addTagListener( XTagListener tagListener )
	{
		ensureTagListenersInitialised();

		_tagListeners.add( tagListener );
	}

	/**
	 * remove doc listener
	 *
	 * @param tagListener
	 */
	public final void removeTagListener( XTagListener tagListener )
	{
		ensureTagListenersInitialised();

		_tagListeners.remove( tagListener );
	}

	/**
	 * Removes an attribute
	 *
	 * @param attributeName  atribute to remove
	 * @return               the removed attribute value or null if it didn't exist
	 */
	public final String removeAttribute( String attributeName )
	{
		if( !_isParsed )
		{
			parse();
		}
		_isDirty = true;
        resetValue();
		fireTagChanged();

		String removed = ( String ) _attributes.remove( attributeName );

		if( removed != null )
		{
			_attributeNames.remove( attributeName );
			_value = null;
		}
		return removed;
	}

	public final boolean equals( Object o )
	{
		// we compare by equality
		return this == o;
	}

	public final int hashCode()
	{
		if( _hash == Integer.MIN_VALUE )
		{
			_hash += _name.hashCode();
		}
		return _hash;
	}

	/**
	 * Validates the tag
	 *
	 * @exception TagValidationException
	 */
	public void validate() throws TagValidationException
	{
		// Default is OK.
	}

    /**
     * Utility method that should be called from {@link #validate()} in
     * case ov a validation failure. Throws a new TagValidationException
     * with
     * @param message the message to include
     * @throws TagValidationException always thrown.
     */
    protected final void fail(String message) throws TagValidationException {
        throw new TagValidationException( message, this );
    }

	/**
	 * Sets the name and value. Called immediately after initialisation by
	 * XTagFactory. Don't call this method from anywhere else.
	 *
	 * @param name
	 * @param value
	 * @param doc
	 * @param lineNumber
	 */
	final void init( String name, String value, XDoc doc, int lineNumber )
	{
		_name = name;
		_doc = doc;
		_lineNumber = lineNumber;
		_isDirty = false;
        _value = value;

		// we register ourself as one of the global tags in the corresponding
		// SourceClass. This is to make it easier for the SourceClass to
		// loop over all tags and ask them to validate themselves when the parsing
		// is done.

		if( doc != null )
		{
			// In fact, doc should never be null. -But some of the JUnit tests
			// fail to set up mocks properly, so they pass in null for doc.
			// This is only to avoid NPE from tht JUnit tests. It's a dirty
			// hack and the tests should be fixed.
			XProgramElement owner = doc.getOwner();
            _xJavaDoc = owner.getXJavaDoc();
            _value = _xJavaDoc.dereferenceProperties( value );

			if( owner != null )
			{
				SourceClass sourceClass;

				if( owner.getContainingClass() != null )
				{
					sourceClass = ( SourceClass ) owner.getContainingClass();
				}
				else
				{
					sourceClass = ( SourceClass ) owner;
				}
				sourceClass.addTagForValidation( this );
			}
		}
	}

	private final void setAttribute_Impl( String attributeName, String attributeValue )
	{
		if( attributeName == null )
		{
			throw new IllegalArgumentException( "attributeName can't be null!" );
		}
		if( _attributes == null )
		{
			_attributes = new HashMap();
			_attributeNames = new LinkedList();
		}
		if( !_attributes.containsKey( attributeName ) )
		{
			// New attribute. Just append it.
			_attributeNames.add( attributeName );
		}

        if( _xJavaDoc != null ) {
            attributeValue = _xJavaDoc.dereferenceProperties( attributeValue );
        }
		_attributes.put( attributeName, attributeValue );

        resetValue();
	}

	private final void ensureTagListenersInitialised()
	{
		if( _tagListeners == null )
		{
			_tagListeners = new HashSet();
		}
	}

	/**
	 * fire tagChanged event
	 */
	private void fireTagChanged()
	{
		if( _tagListeners == null )
			return;

		for( Iterator i = _tagListeners.iterator(); i.hasNext();  )
		{
			XTagListener tagListener = ( XTagListener ) i.next();

			tagListener.tagChanged( new XTagEvent( this ) );
		}
	}

	/**
	 * Given the raw javadoc tag content as the <i>value</i> parameter parses it
	 * and sets the parameter. If anything is malformed (not (foo="xxx")+), then
	 * nothing is set.
	 */
	private final void parse()
	{
		if( !_isParsed )
		{
			String attributeName = null;
			StringBuffer attributeValue = new StringBuffer();
			int i = 0;
			int end = 0;

			String value = getValue();

			while( i < value.length() )
			{
				i = skipWhitespace( value, i );

				//explicitly to handle the tailing white spaces

				if( i >= value.length() )
				{
					break;
				}

				//read attribute name

				end = i;
				while( end < value.length() && value.charAt( end ) != '=' && ( !Character.isWhitespace( value.charAt( end ) ) ) )
				{
					end++;
				}

				attributeName = value.substring( i, end );
				i = skipWhitespace( value, end );

				//skip = sign

				if( i < value.length() && value.charAt( i ) == '=' )
				{
					i++;
				}

				/*
				 * removed single valued
				 */
				i = skipWhitespace( value, i );

				//skip " sign

				if( i < value.length() && value.charAt( i ) == '"' )
				{
					i++;
				}
				else
				{
					//if (_log.isDebugEnabled()) _log.debug("Error in @tag: \" sign expected but something different found, @tags=" + value);
					_isParsed = true;
					return;
				}

				//read attribute value

				while( i < value.length() )
				{
					if( value.charAt( i ) == '"' )
					{
						//if not escaped \" char

						if( value.charAt( i - 1 ) != '\\' )
						{
							//if last " (last parameter) in whole value string

							if( i + 1 >= value.length() )
							{
								break;
							}
							else
							{
								//if tailing " with whitespace after it

								if( Character.isWhitespace( value.charAt( i + 1 ) ) )
								{
									break;
								}
								else
								{
									//probably user does not know escaping is needed!
									//if (_log.isDebugEnabled()) _log.debug("Error in @tag: to put \" in a parameter value you need to escape \" character with \\\", @tags=" + value);
									_isParsed = true;
									return;
								}
							}
						}
						else
						{
							//remove previous \
							attributeValue.delete( attributeValue.length() - 1, attributeValue.length() );
						}
					}

					attributeValue.append( value.charAt( i ) );

					i++;
				}

				//skip " sign

				if( i < value.length() && value.charAt( i ) == '"' )
				{
					i++;
				}
				else
				{
					//_log.warn("Error in @tag: tailing \" sign expected but not found, @tags=" + value);
					_isParsed = true;
					return;
				}
				setAttribute_Impl( attributeName, attributeValue.toString() );
				attributeName = null;
				attributeValue.delete( 0, attributeValue.length() );
			}
			_isParsed = true;
		}
	}

    private final void resetValue() {
        StringBuffer sb = new StringBuffer();

        if( _attributeNames != null )
        {
            for( Iterator attributeNames = _attributeNames.iterator(); attributeNames.hasNext();  )
            {
                String attributeName = ( String ) attributeNames.next();
                String attributeValue = ( String ) _attributes.get( attributeName );

                sb.append( attributeName );
                sb.append( "=\"" );
                sb.append( attributeValue.trim() );
                sb.append( "\" " );
            }
        }
        _value = sb.toString().trim();
    }
}
