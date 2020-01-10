/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import xjavadoc.event.XDocListener;
import xjavadoc.event.XDocEvent;
import xjavadoc.event.XTagListener;
import xjavadoc.event.XTagEvent;

/**
 * Represents documentation
 *
 * @author    Aslak Hellesøy
 * @created   20. mars 2003
 */
public final class XDoc implements XTagListener
{
	public static int  instanceCount = 0;

	/**
	 * Platform specific NEWLINE. Javadoc will use this as new line.
	 */
	private final static String NEWLINE = System.getProperty( "line.separator" );

	/**
	 * Default comment
	 */
	private final static String EMPTY_COMMENT = "/**\n */";

	/**
	 * Maps tag name to List. The Collection contains XTag instances whose name =
	 * name (the map key). The tags in the Lists are ordered by occurrance
	 */
	private Map        _tagMap;

	/**
	 * Token (which is linked in the AST) that holds the string representation of
	 * the doc. Needed for printing out the class.
	 */
	private Token      _javadocToken;

	private XProgramElement _owner;

	/**
	 * Contains all the tags in the doc, in order of occurrence.
	 */
	private List _tags;
	/**
	 * description of program element
	 */
	private String     _commentText = "";
	/**
	 * first sentence of comment text
	 */
	private String     _firstSentence;

	private boolean    _dirty = true;

	private Set        _docListeners = new HashSet();

    private final XTagFactory _tagFactory;

	/**
	 * Describe what the XDoc constructor does
	 *
	 * @param javadocToken  Describe what the parameter does
	 * @param owner         Describe what the parameter does
	 * @param tagFactory    Describe what the parameter does
	 */
	public XDoc( Token javadocToken, XProgramElement owner, XTagFactory tagFactory )
	{
		instanceCount++;
		if( javadocToken == null )
		{
			_javadocToken = Token.newToken( NodeParserConstants.FORMAL_COMMENT );
		}
		else
		{
			_javadocToken = javadocToken;
		}
		_owner = owner;
        _tagFactory = tagFactory;
		if( _javadocToken.image == null )
		{
			// the passed token was not from source code, but was created because no javadoc existed.
			_javadocToken.image = EMPTY_COMMENT;
		}
	}

	/**
	 * Convert a tag name from the old colon-separated form to the new preferred dot-separated form.
	 *
	 * @param tagName  The name of the tag
	 * @return         Preferred form of the tag
	 */
	public static String dotted( final String tagName )
	{
		return tagName.replace( ':', '.' );
	}

	private final static String tokenizeAndTrim( final String s )
	{
		StringBuffer sb = new StringBuffer();
		StringTokenizer st = new StringTokenizer( s );

		while( st.hasMoreTokens() )
		{
			sb.append( st.nextToken() ).append( " " );
		}
		return sb.toString().trim();
	}

	/**
	 * Gets the Owner attribute of the XDoc object
	 *
	 * @return   The Owner value
	 */
	public XProgramElement getOwner()
	{
		return _owner;
	}

	/**
	 * Returns all the tags in this doc with the specified tagName (not
	 * superclasses). If No tags are found, an empty Collection is returned.
	 *
	 * @param tagName  the name of the tags to return (without the 'at')
	 * @return         A Collection of XTag
	 */
	public List getTags( String tagName )
	{
		return getTags( tagName, false );
	}

	/**
	 * Returns all the tags with the specified tagName. If No tags are found, an
	 * empty Collection is returned.
	 *
	 * @param tagName       the name of the tags to return (without the 'at')
	 * @param superclasses  if this is true, return tags from superclasses too.
	 * @return              A Collection of XTag
	 */
	public List getTags( String tagName, boolean superclasses )
	{

		tagName = dotted( tagName );
		if( _dirty )
		{
			parse();
		}

		ensureTagMapInitialised();

		List tags = ( List ) _tagMap.get( tagName );

		if( !superclasses )
		{
			if( tags == null )
			{
				tags = AbstractProgramElement.EMPTY_LIST;
			}
			return Collections.unmodifiableList( tags );
		}
		else
		{
			// Make a new Collection where we append all tags from this and super
			LinkedList superTags = new LinkedList();

			// Add tags from this doc if any.
			if( tags != null )
			{
				superTags.addAll( tags );
			}

			// Now add tags from super
			XDoc superDoc = getSuperDoc();

			if( superDoc != null )
			{
				superTags.addAll( superDoc.getTags( tagName, true ) );
			}

			// Now add tags from implemented interfaces
			// TODO: How are we going to ensure uniqueness ...
			// we could get to the same interface tag twice
			Iterator docs = getAllSuperDocs().iterator();
			while ( docs.hasNext() )
			{
				XDoc interfaceDoc = (XDoc) docs.next();
				// Perhaps the last argument to getTagAttributeValue() should be true
				List interfaceTags = interfaceDoc.getTags( tagName, false );
				superTags.addAll( interfaceTags );
			}

			return Collections.unmodifiableList( superTags );
		}
	}

	/**
	 * Returns all the tags in this doc (not superclasses). If No tags are found,
	 * an empty Collection is returned.
	 *
	 * @return   A Collection of XTag
	 */
	public List getTags()
	{
		return getTags( false );
	}

	/**
	 * Returns all the tags. If no tags are found, an
	 * empty List is returned.
	 *
	 * @param superclasses  if this is true, return tags from superclasses too.
	 * @return              A List of XTag
	 */
	public List getTags( boolean superclasses )
	{

		if( _dirty )
		{
			parse();
		}
		if( !superclasses )
		{
			if( _tags == null )
			{
				return AbstractProgramElement.EMPTY_LIST;
			}
			else
			{
				return _tags;
			}
		}
		else
		{
			// Make a new Collection where we append all tags from this and super
			LinkedList tags = new LinkedList();

			// Add tags from this doc if any.
			if( _tags != null )
			{
				tags.addAll( _tags );
			}

			// Now add tags from super
			XDoc superDoc = getSuperDoc();

			if( superDoc != null )
			{
				tags.addAll( superDoc.getTags( true ) );
			}

			// Now add tags from implemented interfaces
			// TODO: How are we going to ensure uniqueness ...
			// we could get to the same interface tag twice
			Iterator docs = getAllSuperDocs().iterator();
			while ( docs.hasNext() )
			{
				XDoc interfaceDoc = (XDoc) docs.next();
				// Perhaps the last argument to getTagAttributeValue() should be true
				List interfaceTags = interfaceDoc.getTags( false );
				tags.addAll( interfaceTags );
			}

			return Collections.unmodifiableList( tags );
		}
	}

	/**
	 * Get the first tag of name tagName from this doc.  Superclasses are not searched.
	 *
	 * @param tagName  the name of the tag
	 * @return         the tag
	 */
	public XTag getTag( String tagName )
	{
		return getTag( tagName, false );
	}

	/**
	 * Get the first tag of name tagName.
	 *
	 * @param tagName       the name of the tag to get (without the 'at')
	 * @param superclasses  if this is true, return tags from superclasses too.
	 * @return              the first XTag with name equal to tagName
	 */
	public XTag getTag( String tagName, boolean superclasses )
	{
		tagName = dotted( tagName );

		Collection tags = getTags( tagName, superclasses );

		if( tags.size() == 0 )
		{
			return null;
		}
		else
		{
			return ( XTag ) tags.iterator().next();
		}
	}

	/**
	 * Returns the tag attribute value. Does not look in superclasses. If nothing
	 * is found, null is returned.
	 *
	 * @param tagName        The name of the tag to look for (without the 'at')
	 * @param attributeName  The name of the attribute to look for within the tag.
	 * @return               The value of the tag attribute.
	 */
	public String getTagAttributeValue( String tagName, String attributeName )
	{
		return getTagAttributeValue( tagName, attributeName, false );
	}

	/**
	 * Returns the tag attribute value. If superclasses is true, the first
	 * occurrence is returned when walking up the class hierarchy. If nothing is
	 * found, null is returned.
	 *
	 * @param tagName        The name of the tag to look for (without the 'at')
	 * @param attributeName  The name of the attribute to look for within the tag.
	 * @param superclasses   Set it to true to look in superclasses too.
	 * @return               The value of the tag attribute.
	 */
	public String getTagAttributeValue( String tagName, String attributeName, boolean superclasses )
	{
		tagName = dotted( tagName );

		// Get all the tags, loop over them and return the first occurrence of the attribute.
		for( Iterator tags = getTags( tagName ).iterator(); tags.hasNext();  )
		{
			XTag tag = ( XTag ) tags.next();
			String attributeValue = tag.getAttributeValue( attributeName );

			if( attributeValue != null )
			{
				// found one! Return that.
				return attributeValue;
			}
		}

		// Couldn't find anything here. Ask superclasses

		if( superclasses )
		{
			XDoc superDoc = getSuperDoc();

			if( superDoc != null )
			{
				// prefer tags defined on a superclass to tags on interfaces
				String superclassTagValue = superDoc.getTagAttributeValue( tagName, attributeName, true );
				if (superclassTagValue!=null) return superclassTagValue;
			}


			// check interfaces!
			Iterator docs = getAllSuperDocs().iterator();
			while ( docs.hasNext() )
			{
				XDoc interfaceDoc = (XDoc) docs.next();
				// Note: this will do a "depth first" search, is that desirable?
				// Perhaps the last argument to getTagAttributeValue() should be false
				String tagValue = interfaceDoc.getTagAttributeValue( tagName, attributeName, true );
				if (tagValue!=null) return tagValue;
			}

			// We've come to an end. Nothing found. Return null;
			return null;

		}
		else
		{
			// Don't look in superclasses or implemented interfaces. Just return null;
			return null;
		}
	}

	/**
	 * return description of program element
	 *
	 * @return   description of program element
	 */
	public String getCommentText()
	{
		if( _dirty )
		{
			parse();
		}
		return _commentText;
	}

	/**
	 * Return the first sentence of the text of the comment for this doc item.
	 *
	 * @return   First sentence
	 */
	public String getFirstSentence()
	{
		if( _dirty )
		{
			parse();
		}

		if( _firstSentence == null )
		{
			// Ok, we only have one sentence
			if( _commentText.indexOf( '.' ) == -1 )
			{
				_firstSentence = _commentText;
				return _firstSentence;
			}

			// Let's look for the first sentence separator. It should be a dot followed
			// by a blank, tab or line terminator.
			int fromIndex = 0;

			while( fromIndex < _commentText.length() - 1 && _firstSentence == null )
			{
				int dotIndex = _commentText.indexOf( '.', fromIndex );

				if( dotIndex != -1 && dotIndex < _commentText.length() - 1 )
				{
					if( " \t\r\n".indexOf( _commentText.charAt( dotIndex + 1 ) ) != -1 )
						_firstSentence = _commentText.substring( 0, dotIndex + 1 );
					else
						fromIndex = dotIndex + 1;
				}
				else
					_firstSentence = _commentText;
			}

			// We didn't find a proper first sentence separator. So we only have
			// one sentence.
			if( _firstSentence == null )
			{
				_firstSentence = _commentText;
			}
		}

		return _firstSentence;
	}

	/**
	 * Set the text of the comment for this doc item.
	 *
	 * @param commentText  The new comment text
	 */
	public void setCommentText( String commentText )
	{
		if( _dirty )
		{
			parse();
		}
		_commentText = commentText;
		_firstSentence = null;
		fireDocChanged();
	}

	/**
	 * Returns true if the tag exists. Does not look in superclasses.
	 *
	 * @param tagName  The name of the tag to look for (without the 'at')
	 * @return         true if the tag exists
	 */
	public boolean hasTag( String tagName )
	{
		return hasTag( tagName, false );
	}

	/**
	 * Returns true if the tag exists.
	 *
	 * @param tagName       The name of the tag to look for (without the 'at')
	 * @param superclasses  If true, look in superclasses too.
	 * @return              true if the tag exists
	 */
	public boolean hasTag( String tagName, boolean superclasses )
	{
		return getTags( tagName, superclasses ).size() != 0;
	}

	/**
	 * Utility method to set the value of a tag attribute. If the tag doesn't
	 * exist, it is created. If the attribute doesn't exist it is created. If the
	 * tag attribute exists, it is updated.
	 *
	 * @param tagName                The new name of the tag to update (without the
	 * @param tagIndex               The index of the tag to update, in case there
	 *      are several tags with the same name.
	 * @param attributeName          The attribute name
	 * @param attributeValue         The new attribute value
	 * @return                       the updated tag
	 * @exception XJavaDocException
	 */
	public XTag updateTagValue( String tagName, String attributeName, String attributeValue, int tagIndex ) throws XJavaDocException
	{
		XTag tag = null;
		List tags = getTags( tagName );

		if( tags.size() == 0 || tags.size() <= tagIndex )
		{
			//debug("@" + tagName + " at index " + tagIndex + " doesn't exist. creating new tag");
			// There was no such tags. Create a new one.
			String tagValue = attributeName + "=\"" + attributeValue + "\"";

			tag = addTag_Impl( tagName, tagValue, -1 );
		}
		else
		{
			// Iterate to the tag at the right index
			Iterator tagIterator = tags.iterator();

			for( int i = 0; i < tagIndex; i++ )
			{
				tag = (XTag) tagIterator.next();
			}
			if( tag != null )
			{
				tag.setAttribute( attributeName, attributeValue );
			}
		}
		return tag;
	}

	/**
	 * Add doc listener interested in changes.
	 *
	 * @param docListener  doc listener to register
	 */
	public void addDocListener( XDocListener docListener )
	{
		_docListeners.add( docListener );
	}

	/**
	 * remove doc listener
	 *
	 * @param docListener
	 */
	public void removeDocListener( XDocListener docListener )
	{
		_docListeners.remove( docListener );
	}

	/**
	 * Returns a String representation of this doc.
	 *
	 * @return   a String representation of this doc.
	 */
	public String toString()
	{
		if( _dirty )
		{
			parse();
		}

		StringBuffer sb = new StringBuffer( "/**" ).append( NEWLINE );

		if( !_commentText.trim().equals( "" ) )
		{
			appendWhiteSpaces( sb ).append( " * " ).append( _commentText ).append( NEWLINE );
			appendWhiteSpaces( sb ).append( " * " ).append( NEWLINE );
		}

//		addSpaces(sb, _javadocToken).append(" * ").append(NEWLINE);

		for( Iterator tags = getTags().iterator(); tags.hasNext();  )
		{
			XTag tag = ( XTag ) tags.next();

			appendWhiteSpaces( sb ).append( " * @" ).append( tag.getName() );

			Collection attributeNames = tag.getAttributeNames();

			if( attributeNames.size() == 0 )
			{
				// no parameters, or malformed
				sb.append( ' ' ).append( tag.getValue() ).append( NEWLINE );
			}
			else
			{
				sb.append( NEWLINE );
				for( Iterator attrs = attributeNames.iterator(); attrs.hasNext();  )
				{
					String attibuteName = ( String ) attrs.next();
					String attributeValue = tag.getAttributeValue( attibuteName );

					appendWhiteSpaces( sb ).append( " *    " ).append( attibuteName ).append( "=\"" ).append( attributeValue ).append( "\"" ).append( NEWLINE );
				}
			}
		}
		appendWhiteSpaces( sb ).append( " */" );

		return sb.toString();
	}

	/**
	 * update token
	 */
	public void updateToken()
	{
		_javadocToken.image = toString();
	}

	/**
	 * Removes tag. Note that XTag objects are compared by identity.
	 *
	 * @param tag  tag to be removed
	 * @return     true if it was removed
	 */
	public boolean removeTag( XTag tag )
	{
		boolean removed = _tags.remove( tag );

		if( removed )
		{
			// purge it from tag map too
			ensureTagMapInitialised();

			Collection tags = ( Collection ) _tagMap.get( dotted( tag.getName() ) );

			tags.remove( tag );
			fireDocChanged();
		}
		return removed;
	}

	/**
	 * Add a tag to the doc item.
	 *
	 * @param tagName                  The name of the tag to add
	 * @param text                     The value of the tag
	 * @return                         The created XTag
	 * @throws TagValidationException  if validation is activated (in XTagFactory)
	 *      and tagName is not among the registered tags.
	 */
	public XTag addTag( String tagName, String text )
	{
		if( _dirty )
		{
			parse();
		}

		XTag rtag = addTag_Impl( tagName, text, -1 );

		// fire doc change event
		fireDocChanged();
		return rtag;
	}

	/**
	 * receive change notification from xtag
	 *
	 * @param event
	 */
	public void tagChanged( XTagEvent event )
	{
		// invalidate attribute value cache
		// for tag in question
		if( event.getSource() instanceof XTag )
		{
			//XTag tag = ( XTag ) event.getSource();

			/*
			 * if( tagAttrValueCurrent != null )
			 * {
			 * tagAttrValueCurrent.remove( tag.getName() );
			 * }
			 */
			fireDocChanged();
		}
	}

	/**
	 * Returns the doc in the superclass. If the super element is null, or not from
	 * source, null is returned.
	 *
	 * @return   the superclass' doc
	 */
	private XDoc getSuperDoc()
	{
		XProgramElement superElement = _owner.getSuperElement();

		if( superElement != null )
		{
			return superElement.getDoc();
		}
		else
		{
			return null;
		}
	}

	/**
	 * Returns the doc in all the superclasses. If the super element is null, or not from
	 * source, an empty list is returned.
	 *
	 * @return   A List of XDoc
	 */
	private List getAllSuperDocs()
	{
		List superElements = _owner.getSuperInterfaceElements();

		if ( superElements!=null )
		{
			List result = new ArrayList();
			Iterator elements = superElements.iterator();
			while ( elements.hasNext() )
			{
				XDoc doc = ( (XProgramElement) elements.next() ).getDoc();
				result.add(doc);
			}
			return result;
		}
		else
		{
			return Collections.EMPTY_LIST;
		}
	}

	private final void ensureTagMapInitialised()
	{
		if( _tagMap == null )
		{
			_tagMap = new TreeMap();
		}
	}

	/**
	 * Creates and adds a tag
	 *
	 * @param tagName                     The name of the tag (without the 'at')
	 * @param text                        The raw content of the tag
	 * @param lineNumber                  The feature to be added to the Tag_Impl
	 *      attribute
	 * @return                            An instance of XTag, created by the
	 *      current XTagFactory
	 * @exception TagValidationException
	 */
	private XTag addTag_Impl( String tagName, String text, int lineNumber ) throws TagValidationException
	{
		tagName = dotted( tagName );

		ensureTagMapInitialised();

		Collection tags = ( Collection ) _tagMap.get( tagName );

		if( tags == null )
		{
			tags = new LinkedList();
			_tagMap.put( tagName, tags );
		}

		if( _tags == null )
		{
			_tags = new LinkedList();
		}

		XTag tag = _tagFactory.createTag( tagName, text, this, lineNumber );

		// We want to be notified when this tag changes
		tag.addTagListener( this );

		// Add to the Collection in the map
		tags.add( tag );

		// Add to the global tag list
		_tags.add( tag );

		return tag;
	}

	/**
	 * fire docChange event
	 */
	private void fireDocChanged()
	{
		for( Iterator i = _docListeners.iterator(); i.hasNext();  )
		{
			XDocListener docListener = ( XDocListener ) i.next();

			docListener.docChanged( new XDocEvent( this ) );
		}

		// also set containing class to dirty
		if( _owner != null )
		{
			XClass clazz = _owner instanceof XClass ? ( XClass ) _owner : _owner.getContainingClass();

			clazz.setDirty();
		}
	}

	/**
	 * Add some white space to the string being built up in toString().
	 *
	 * @param sb  StringBuffer that the text is being built in
	 * @return    the StringBuffer
	 */
	private StringBuffer appendWhiteSpaces( StringBuffer sb )
	{
		return sb.append( "   " );
		/*
		 * Token tk = _programElementToken;
		 * while (tk.previous != null && isTabOrSpace(tk.previous)) {
		 * tk = tk.previous;
		 * }
		 * while (tk.next != null && tk != _programElementToken) {
		 * sb.append(tk.image);
		 * tk = tk.next;
		 * }
		 * return sb;
		 */
	}

	/**
	 * Parse token into comments, tags and tag attributes. We remove excess spaces.
	 *
	 * @exception TagValidationException
	 */
	private void parse() throws TagValidationException
	{
		if( _dirty )
		{
			//debug("parse");
			// We must read line by line, since a @tags can only begin as the first token of a line.
			JavaDocReader javaDocReader = new JavaDocReader( new StringReader( _javadocToken.image ) );
			BufferedReader in = new BufferedReader( javaDocReader );
			StringBuffer docElement = new StringBuffer();
			String tagName = null;
			String line = null;

			int tagStartLine = -1;

			try
			{
				while( ( line = in.readLine() ) != null )
				{
					if( line.startsWith( "@" ) )
					{
						// remember the line number where the tag starts.
						tagStartLine = _javadocToken.beginLine + javaDocReader.getLineOffset();

						// It's a new tag
						if( tagName == null )
						{
							// what we've been reading so far has been a general comment.
							_commentText = tokenizeAndTrim( docElement.toString() );
						}
						else
						{
							// Add the previous tag
							addTag_Impl( tagName, tokenizeAndTrim( docElement.toString() ), tagStartLine );
						}
						docElement = new StringBuffer();

						StringTokenizer st = new StringTokenizer( line );

						tagName = st.nextToken().substring( 1 );
						docElement.append( line.substring( tagName.length() + 1 ).trim() ).append( ' ' );
					}
					else
					{
						// It's the continuation of a tag or a comment, or start of comment;
						if( docElement == null )
						{
							// It was the start of the comment
							docElement = new StringBuffer();
						}
						docElement.append( line.trim() ).append( ' ' );
					}
				}
				if( tagName == null )
				{
					// what we've been reading so far has been a general comment.
					_commentText = docElement.toString().trim();
				}
				else
				{
					// Add the previous tag
					addTag_Impl( tagName, tokenizeAndTrim( docElement.toString() ), tagStartLine );
				}
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
			catch( StringIndexOutOfBoundsException e )
			{
				e.printStackTrace();
			}
			_dirty = false;
		}
	}

}
