/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

import xjavadoc.filesystem.AbstractFile;

import java.io.Serializable;

/**
 * This interface represents a set of Java source files.
 *
 * @author    <a href="mailto:dim@bigpond.net.au">Dmitri Colebatch</a>
 * @created   October 4, 2002
 */
public interface SourceSet extends Serializable
{

	/**
	 * @return
	 */
	AbstractFile[] getFiles();

	/**
	 * @param qualifiedName
	 * @return
	 */
	AbstractFile getSourceFile( String qualifiedName );

	/**
	 * @param i
	 * @return
	 */
	String getQualifiedName( int i );

	/**
	 * @return
	 */
	int getSize();
}
