/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Describe what this class does
 *
 * @author    Ara Abrahamian
 * @author    Aslak Hellesøy
 * @created   7. mars 2003
 */
public abstract class AbstractProgramElement implements XProgramElement
{
	final static List  EMPTY_LIST = Collections.unmodifiableList( new LinkedList() );

    private XJavaDoc _xJavaDoc;
	private AbstractClass _containingClass;
	private int        _modifiers = 0;
	private String     _modifierString;
	private XDoc       _doc;
	private Token      _token;
	private Token      _javadocToken;
    private final XTagFactory _tagFactory;

    protected AbstractProgramElement( AbstractClass containingClass, XTagFactory tagFactory )
    {
        _xJavaDoc = containingClass.getXJavaDoc();
        _containingClass = containingClass;
        _tagFactory = tagFactory;
    }

    protected AbstractProgramElement( XJavaDoc xJavaDoc,  XTagFactory tagFactory )
    {
        _xJavaDoc = xJavaDoc;
        _containingClass = null;
        _tagFactory = tagFactory;
    }

    public XJavaDoc getXJavaDoc() {
        return _xJavaDoc;
    }

	public final boolean isFinal()
	{
		return ( _modifiers & Modifier.FINAL ) != 0;
	}

	public final boolean isAbstract()
	{
		return ( _modifiers & Modifier.ABSTRACT ) != 0;
	}

	public final boolean isPackagePrivate()
	{
		return !isPrivate() && !isProtected() && !isPublic();
	}

	public final boolean isPrivate()
	{
		return ( _modifiers & Modifier.PRIVATE ) != 0;
	}

	public final boolean isProtected()
	{
		return ( _modifiers & Modifier.PROTECTED ) != 0;
	}

	public final boolean isPublic()
	{
		return ( _modifiers & Modifier.PUBLIC ) != 0;
	}

	public final boolean isStatic()
	{
		return ( _modifiers & Modifier.STATIC ) != 0;
	}

	/**
	 * Get the doc. If this is a binary, primitive or unknown, null is returned.
	 *
	 * @return   the class level doc
	 */
	public final XDoc getDoc()
	{
		if( _doc == null )
		{
			if( _token == null )
			{
				// We're not from source (we're binary, primitive or unknown)
				_doc = new XDoc( null, this, _tagFactory );
			}
			else
			{
				if( _javadocToken != null )
				{
					_doc = new XDoc( _javadocToken, this, _tagFactory );
				}
				else
				{
					// there was no doc in the original source. Create it.
					// We have to create a new token and attach it to _token as specialToken
					// The pre and post tokens are only to ensure proper line breaks before and after
					Token preJavadocToken = Token.newToken( NodeParserConstants.DEFAULT );

					preJavadocToken.image = "\n\n";

					_javadocToken = Token.newToken( NodeParserConstants.FORMAL_COMMENT );
					_javadocToken.image = "";

					Token postJavadocToken = Token.newToken( NodeParserConstants.DEFAULT );

					postJavadocToken.image = "\n";

					// Link the new tokens properly
					preJavadocToken.next = _javadocToken;
					_javadocToken.next = postJavadocToken;

					_token.specialToken = preJavadocToken;
					_doc = new XDoc( _javadocToken, this, _tagFactory );
				}
			}
			// Help the GC. We don't need the tokens anymore.
			_token = null;
			_javadocToken = null;
		}
		return _doc;
	}

	/**
	 * Get modifiers string.
	 *
	 * @return
	 */
	public final String getModifiers()
	{
		if( _modifierString == null )
		{
			_modifierString = Modifier.toString( _modifiers );
		}
		return _modifierString;
	}

	/**
	 * Get the modifier specifier integer.
	 *
	 * @return
	 */
	public final int getModifierSpecifier()
	{
		return _modifiers;
	}

	public final XClass getContainingClass()
	{
		return getContainingAbstractClass();
	}

	public final AbstractClass getContainingAbstractClass()
	{
		return _containingClass;
	}

	public XPackage getContainingPackage()
	{
		if( _containingClass != null )
		{
			return getContainingClass().getContainingPackage();
		}
		else
		{
			return null;
		}
	}

	/**
	 * Sets the Token where we start. Useful for doc mutation.
	 *
	 * @param token  The new Token value
	 */
	public final void setToken( Token token )
	{
		if( _token == null && token != null )
		{
			_token = token;
			setJavaDoc();
		}
	}

	public final void addModifier( int modifier )
	{
		_modifiers |= modifier;
	}

	public int compareTo( Object o )
	{
		XProgramElement other = ( XProgramElement ) o;

		return getName().compareTo( other.getName() );
	}

	/**
	 * update javadoc
	 */
	public void updateDoc()
	{
		if( _doc != null )
		{
			_doc.updateToken();
		}
	}

    protected XTagFactory getTagFactory() {
        return _tagFactory;
    }

	void reset()
	{
		_doc = null;
		_containingClass = null;
		_token = null;
		_javadocToken = null;
	}

	private final void setJavaDoc()
	{
		Token javadoc = null;

		Token tt = _token.specialToken;

		if( tt != null )
		{
			while( tt.specialToken != null )
			{
				tt = tt.specialToken;
			}
			while( tt != null )
			{
				if( tt.kind == NodeParserConstants.FORMAL_COMMENT )
				{
					// it's JavaDoc
					javadoc = tt;
				}
				else if( tt.kind == NodeParserConstants.SINGLE_LINE_COMMENT || tt.kind == NodeParserConstants.MULTI_LINE_COMMENT )
				{
					// reset it. some other comment is standalone or followed what could have been a javadoc comment
					javadoc = null;
				}
				tt = tt.next;
			}
		}
		if( javadoc != null )
		{
			// There was javadoc here!
			if( _doc != null )
			{
				throw new IllegalStateException( "Doc has already been set!!" );
			}
			else
			{
				_javadocToken = javadoc;
			}
		}
	}

}
