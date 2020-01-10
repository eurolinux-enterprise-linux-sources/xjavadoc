/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc.filesystem;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.io.Reader;
import java.io.OutputStream;
import java.io.FileNotFoundException;

/**
 * An interface that allows XJavadoc to read and write from any
 * source/destination and not just files.
 *
 * @author    <a href="dim@bigpond.net.au">Dmitri Colebatch</a>
 * @created   September 25, 2002
 */
public interface AbstractFile
{

	/**
	 * Obtain a reader for the file.
	 *
	 * @return
	 * @exception IOException
	 */
	Reader getReader() throws IOException;

	/**
	 * Obtain a writer for the file.
	 *
	 * @return
	 * @exception IOException
	 */
	Writer getWriter() throws IOException;

	/**
	 * Determine if the file is writeable or not.
	 *
	 * @return
	 */
	boolean isWriteable();

	/**
	 * Get the last modified timestamp of the file, or 0 if not available.
	 *
	 * @return
	 */
	long lastModified();

	/**
	 * Get an outputstream for the file.
	 *
	 * @return
	 * @exception IOException
	 */
	OutputStream getOutputStream() throws IOException;

	String getPath();

	/**
	 * @param encoding
	 * @return
	 */
	public Reader getReader(String enc) throws UnsupportedEncodingException, FileNotFoundException;
}
