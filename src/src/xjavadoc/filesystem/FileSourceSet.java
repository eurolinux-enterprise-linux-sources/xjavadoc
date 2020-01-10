/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc.filesystem;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.ArrayList;

import xjavadoc.filesystem.AbstractFile;
import xjavadoc.filesystem.XJavadocFile;
import xjavadoc.SourceSet;
import xjavadoc.Util;

/**
 * This class represents a set of Java source files. It designs a directory and
 * an optional array of files. The size() and getQualifiedName( int ) methods
 * depend on what files were passed in the constructor. The getSourceFile(
 * String ) will work regardless of wether the class was instantiated with files
 * or not (provided the file exists).
 *
 * @author    Aslak Hellesøy
 * @created   14. mars 2002
 */
public final class FileSourceSet implements SourceSet
{
	/**
	 * root directory
	 */
	private File       _dir;

	/**
	 * source files
	 */
	private ArrayList  _files;

	/**
	 * overridden hash code
	 */
	private int        hash = Integer.MIN_VALUE;

	/**
	 * Constructs a new FileSourceSet. If the files parameter is null, the
	 * FileSourceSet will report that it does not know about any java source files,
	 * even if they exist. See the general class comment.
	 *
	 * @param dir    The root directory of the java sources
	 * @param files  The desired files under the root directory
	 */
	public FileSourceSet( File dir, String[] files )
	{
		if( dir == null )
		{
			throw new IllegalArgumentException( "dir can't be null" );
		}
		if( !dir.isDirectory() )
		{
			throw new IllegalArgumentException( dir.getAbsolutePath() + " must be a directory" );
		}
		_dir = dir;
		_files = new ArrayList();
		if( files != null )
		{
			_files.addAll( Arrays.asList( files ) );
		}

	}

	/**
	 * Creates a SoureSet from a directory or a file. If fileOrDir is a directory,
	 * all java files under that directory (and all subdirectories) will be added
	 * to this FileSourceSet.
	 *
	 * @param fileOrDir
	 */
	public FileSourceSet( File fileOrDir )
	{
		if( !fileOrDir.isDirectory() && !fileOrDir.exists() )
		{
			throw new IllegalArgumentException( fileOrDir.getAbsolutePath() + " must exist" );
		}
		_files = new ArrayList();
		if( fileOrDir.isDirectory() )
		{
			_dir = fileOrDir;
			_files.addAll( Arrays.asList( Util.getJavaFiles( fileOrDir ) ) );

		}
		else
		{
			_dir = fileOrDir.getParentFile();
			_files.add( fileOrDir.getName() );

		}
	}

	/**
	 * Gets the files contained in the source set.
	 *
	 * @return
	 */
	public AbstractFile[] getFiles()
	{
		throw new UnsupportedOperationException( "Not yet implemented." );
//		return _files.size() == 0 ? null : ( String[] ) _files.toArray( new String[_files.size()] );
	}

	/**
	 * Gets the File containing the source of the class. <br>
	 * <b>IMPORTANT:</b> This method will find a file regardless of whether it was
	 * part of the files passed in the constructor.
	 *
	 * @param qualifiedName  fully qualified class name of the source file to find.
	 * @return               the File containing the source of the class
	 */
	public AbstractFile getSourceFile( String qualifiedName )
	{
		File sourceFile = new File( _dir, getRelativeFileName( qualifiedName ) );

		if( !sourceFile.exists() )
		{
			return null;
		}
		return new XJavadocFile( sourceFile );
	}

	/**
	 * Gets the fully qualified class name of the i'th file in the instance.
	 *
	 * @param i  the index of the class
	 * @return   fully qualified class name
	 */
	public String getQualifiedName( int i )
	{
		//_log.debug( "returning file: " + _files[i] );
		return getQualifiedName( ( String ) _files.get( i ) );
	}

	/**
	 * Returns the number of files in the instance
	 *
	 * @return   the number of files in the instance
	 */
	public int getSize()
	{
		return _files.size();
	}

	/**
	 * whether source set contains given absolute file name
	 *
	 * @param filename  absolute filename to check
	 * @return
	 */
	public boolean containsAbsolute( String filename )
	{
		return filename.startsWith( getDir().getPath() ) && _files.contains( filename.substring( getDir().getPath().length() + 1 ) );
	}
	/**
	 * whether source set contains relative file name
	 *
	 * @param filename  relative filename to check
	 * @return
	 */
	public boolean containsRelative( String filename )
	{
		return _files.contains( filename );
	}

	/**
	 * Compares with another object. They are equal if o is a FileSourceSet and
	 * have the same dir and the same files.
	 *
	 * @param o  object to compare
	 * @return   true if they are equal
	 */
	public boolean equals( Object o )
	{
		if( o instanceof FileSourceSet )
		{
			FileSourceSet other = ( FileSourceSet ) o;

			return _dir.equals( other._dir ) && _files.equals( other._files );
		}
		else
		{
			return false;
		}
	}

	public int hashCode()
	{
		if( hash == Integer.MIN_VALUE )
		{
			hash = _dir.hashCode();
			if( _files != null )
			{
				for( int i = 0; i < _files.size(); i++ )
				{
					hash += _files.get( i ).hashCode();
				}
			}
		}
		return hash;
	}

	/**
	 * Gets the root directory of the source files.
	 *
	 * @return   the root directory of the source files.
	 */
	private File getDir()
	{
		return _dir;
	}

	/**
	 * Gets the fully qualified class name for a relative file
	 *
	 * @param relativeFileName  filename relative to the dir
	 * @return                  fully qualified class name
	 */
	private String getQualifiedName( String relativeFileName )
	{
		String result = relativeFileName.replace( '/', '.' ).replace( '\\', '.' );

		result = result.substring( 0, result.length() - 5 );
		return result;
	}

	/**
	 * Gets the relative file name (relative to dir) for a fully qualified class
	 * name
	 *
	 * @param qualifiedName  fully qualified class name
	 * @return               the relative file name
	 */
	private String getRelativeFileName( String qualifiedName )
	{
		return qualifiedName.replace( '.', File.separatorChar ) + ".java";
	}

	/**
	 * FileFilter that only accepts java sources
	 *
	 * @created   24. august 2002
	 */
	class JavaSourceFilter implements FileFilter
	{
		private final static String suffix = ".java";
		public boolean accept( File file )
		{
			return file.getName().endsWith( suffix );
		}
	}

	/**
	 * FileFilter that only accepts directories
	 *
	 * @created   24. august 2002
	 */
	class DirectoryFilter implements FileFilter
	{
		public boolean accept( File file )
		{
			return file.isDirectory();
		}
	}

}
