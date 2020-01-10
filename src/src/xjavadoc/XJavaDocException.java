/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

/**
 * @author    Aslak Hellesøy
 * @created   19. februar 2002
 */
public class XJavaDocException extends Exception
{

	/**
	 * @todo-javadoc   Describe the field
	 */
	private Throwable  _source;

	/**
	 * Describe what the XJavaDocException constructor does
	 *
	 * @param source   Describe what the parameter does
	 * @todo-javadoc   Write javadocs for constructor
	 * @todo-javadoc   Write javadocs for method parameter
	 */
	public XJavaDocException( Throwable source )
	{
		this( source.getMessage() );
		_source = source;
	}

	/**
	 * Describe what the XJavaDocException constructor does
	 *
	 * @param message  Describe what the parameter does
	 * @todo-javadoc   Write javadocs for constructor
	 * @todo-javadoc   Write javadocs for method parameter
	 */
	public XJavaDocException( String message )
	{
		super( message );
	}

	/**
	 * Gets the Source attribute of the XJavaDocException object
	 *
	 * @return   The Source value
	 */
	public Throwable getSource()
	{
		return _source;
	}
}

