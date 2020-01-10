/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc.ant;

import java.io.File;
import java.util.Iterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Parameter;
import org.apache.tools.ant.types.selectors.BaseExtendSelector;

import xjavadoc.XClass;
import xjavadoc.XJavaDoc;
import xjavadoc.filesystem.FileSourceSet;

/**
 * Custom file filter for Ant based on XJavadoc. Filters java sources according
 * to some Java specific features. <br/>
 * Usage:<br/>
 * <pre>
 *&lt;copy todir="filtered-src"&gt;
 *   &lt;fileset dir="src"&gt;
 *      &lt;or&gt;
 *         &lt;custom classname="xjavadoc.XJavadocFilter" classpathref="lib.jars"&gt;
 *            &lt;parameter name="implements" value="javax.ejb.EntityBean" /&gt;
 *         &lt;/custom&gt;
 *         &lt;custom classname="xjavadoc.XJavadocFilter" classpathref="lib.jars"&gt;
 *            &lt;parameter name="implements" value="javax.ejb.SessionBean" /&gt;
 *         &lt;/custom&gt;
 *      &lt;/or&gt;
 *   &lt;/fileset&gt;
 *&lt;/copy&gt;
 *</pre> Valid parameters are:<br/>
 *
 * <dl>
 *   <dt> <strong>implements</strong> </dt>
 *   <dd> full qualified name of the class or interface to implement</dd>
 *   <dt> <strong>contains-tag</strong> </dt>
 *   <dd> javadoc tag to contain</dd>
 * </dl>
 *
 *
 * @author    Ludovic Claude
 * @created   02 November 2002
 * @version   $Revision: 1.6 $
 */
public class XJavadocFilter extends BaseExtendSelector
{
    XJavaDoc _xJavaDoc = new XJavaDoc();

	/**
	 * Constructor for XJavadocFilter.
	 */
	public XJavadocFilter()
	{
		super();
	}

	/**
	 * @param basedir
	 * @param filename
	 * @param file
	 * @return
	 * @exception BuildException
	 * @see                       org.apache.tools.ant.types.selectors.FileSelector#isSelected(File,
	 *      String, File)
	 */
	public boolean isSelected( File basedir, String filename, File file )
		 throws BuildException
	{

		if( !filename.endsWith( ".java" ) )
			return false;

		_xJavaDoc.reset( true );
		try
		{
			//validateOptions();

			_xJavaDoc.addSourceSet( new FileSourceSet( basedir, new String[]{filename} ) );

			for( Iterator i =  _xJavaDoc.getSourceClasses().iterator() ; i.hasNext();  )
			{
				XClass clazz = (XClass) i.next();
				Parameter[] params = getParameters();

				for( int j = 0; j < params.length; j++ )
				{
					Parameter param = params[j];

					if( param.getName().equals( "implements" ) )
					{
						String mandatoryClass = param.getValue();

						if( !clazz.isA( mandatoryClass ) )
							return false;
					}
					else if( param.getName().equals( "contains-tag" ) )
					{
						String mandatoryTag = param.getValue();

						if( !clazz.getDoc().hasTag( mandatoryTag ) )
							return false;
					}
				}
			}
		}
		catch( OutOfMemoryError e )
		{
			System.err.println( e.getMessage() );
			XJavaDoc.printMemoryStatus();
			System.err.println( "Try to increase heap size. Can be done by defining ANT_OPTS=-Xmx640m" );
			System.err.println( "See the JDK tooldocs." );
			throw new BuildException( e.getMessage(), e );
		}
		catch( Throwable t )
		{
			t.printStackTrace();
			throw new BuildException( "Unexpected error", t );
		}
		finally
		{
			//XJavaDoc.printMemoryStatus();

			_xJavaDoc.printLogMessages( System.out, XJavaDoc.NO_IMPORTED_PACKAGES );
			_xJavaDoc.printLogMessages( System.out, XJavaDoc.ONE_OR_MORE_IMPORTED_PACKAGES );
			_xJavaDoc.reset( true );
			System.gc();
		}
		return true;
	}

}
