/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

/**
 * @created   September 25, 2002
 */
public class XJavadocFile implements AbstractFile
{
	private File       file;

	public XJavadocFile( File file )
	{
		this.file = file;
	}

	public Reader getReader() throws IOException
	{
		return new FileReader( file );
	}

	public Reader getReader(String enc) throws UnsupportedEncodingException, FileNotFoundException
	{
		if (enc!=null)
		{
			return new InputStreamReader(new FileInputStream(file),enc);
		} 
		else 
		{
			return new InputStreamReader(new FileInputStream(file));
		}

	}

	public Writer getWriter() throws IOException
	{
		return new FileWriter( file );
	}

	public boolean isWriteable()
	{
		return file.canWrite();
	}

	public OutputStream getOutputStream() throws FileNotFoundException
	{
		return new FileOutputStream( file );
	}

	public String getPath()
	{
		return file.getAbsolutePath();
	}

	public long lastModified()
	{
		return file.lastModified();
	}

	public String toString()
	{
		return "File " + file.getAbsolutePath();
	}
}
