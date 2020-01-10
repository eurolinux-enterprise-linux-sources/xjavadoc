/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc.ant;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.FileSet;

import java.util.LinkedList;
import java.io.File;

import xjavadoc.XJavaDoc;
import xjavadoc.DefaultXTag;
import xjavadoc.filesystem.FileSourceSet;

/**
 * This class should be subclassed to be used for XDocletImpl, revXDoclet etc.
 *
 * @author    Aslak Hellesøy
 * @author    Ara Abrahamian
 * @created   26. februar 2003
 */
public abstract class XJavadocTask extends Task
{
    private final XJavaDoc _xJavaDoc = new XJavaDoc();
	private final LinkedList _fileSets = new LinkedList();

    protected XJavaDoc getXJavaDoc() {
        return _xJavaDoc;
    }

	/**
	 * Sets the tags to ignore if validation is true. The value should be a
	 * comma-separated list of tag names (without the tag name)
	 *
	 * @param tags  tags that should be ignored when doing validation.
	 */
	public void setIgnoredtags( String tags )
	{
		_xJavaDoc.getTagFactory().setIgnoredTags( tags );
	}

	/**
	 * Sets whether or not tags will be validated.
	 *
	 * @param flag validate?
	 */
	public void setValidating( boolean flag )
	{
		_xJavaDoc.getTagFactory().setValidating( flag );
	}

	/**
	 * set source file charset
         *
	 * @param enc the encoding
	 */
	public void setEncoding(String enc)
	{
		_xJavaDoc.setEncoding(enc);
	}

	/**
	 * set generated file charset
         *
	 * @param enc the encoding
 	 */
	public void setDocencoding(String enc)
	{
		_xJavaDoc.setDocEncoding(enc);
	}

	/**
	 * Implementation of Ant's {@link Task#execute()}.
	 *
	 * @exception BuildException  Ant's way of reporting build exception
	 */
	public final void execute() throws BuildException
	{
		_xJavaDoc.reset( true );
		_xJavaDoc.setPropertyMap( project.getProperties() );
		try
		{
			validateOptions();

			FileSourceSet[] sourceSets = new FileSourceSet[_fileSets.size()];

			for( int i = 0; i < _fileSets.size(); i++ )
			{
				FileSet fs = ( FileSet ) _fileSets.get( i );
				File dir = fs.getDir( project );

				DirectoryScanner ds = fs.getDirectoryScanner( project );
				String[] files = ds.getIncludedFiles();

				sourceSets[i] = new FileSourceSet( dir, files );
				_xJavaDoc.addSourceSet( sourceSets[i] );
			}

			start();
		}
		catch( OutOfMemoryError e )
		{
			System.err.println( e.getMessage() );
			XJavaDoc.printMemoryStatus();
			System.err.println( "Try to increase heap size. Can be done by defining ANT_OPTS=-Xmx640m" );
			System.err.println( "See the JDK tooldocs." );
			throw new BuildException( e.getMessage(), e, location );
		}
		catch( Throwable t )
		{
			t.printStackTrace();
			throw new BuildException( "Unexpected error", t, location );
		}
		finally
		{
			//XJavaDoc.printMemoryStatus();

			_xJavaDoc.printLogMessages( System.out, XJavaDoc.NO_IMPORTED_PACKAGES );
			_xJavaDoc.printLogMessages( System.out, XJavaDoc.ONE_OR_MORE_IMPORTED_PACKAGES );
			_xJavaDoc.reset(true);
			System.gc();
		}
	}

	/**
	 * Ignores one tag
	 *
	 * @return
	 */
	public Object createIgnoredtag()
	{
		return
			new Object()
			{
				public void addText( String text )
				{
					_xJavaDoc.getTagFactory().registerTagClass( text, DefaultXTag.class );
				}
			};
	}

	/**
	 * Ant's &lt;fileset&gt; definition. To define the files to parse.
	 *
	 * @param set  a fileset to add
	 */
	public void addFileset( FileSet set )
	{
		_fileSets.add( set );
	}

	/**
	 * Returns the classpath
	 *
	 * @return   the classpath
	 */
	protected String getClasspath()
	{
		return ( ( AntClassLoader ) getClass().getClassLoader() ).getClasspath();
	}

	/**
	 * Implement this method and play with _xJavaDoc
	 *
	 * @exception BuildException  Ant's way of reporting exception
	 */
	protected abstract void start() throws BuildException;

	/**
	 * Validate a Xdoclet task before running it.
	 *
	 * @exception BuildException  in case the validation fails.
	 */
	protected void validateOptions() throws BuildException
	{
		if( _fileSets.size() == 0 )
		{
			throw new BuildException( "At least one fileset must be specified", location );
		}
	}
}
