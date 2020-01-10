/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc.filesystem;

import java.io.Reader;
import java.io.IOException;
import java.io.Writer;
import java.io.OutputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

/**
 * @created   September 25, 2002
 */
public class ReaderFile implements AbstractFile
{
	private Reader       file;

	public ReaderFile( Reader file )
	{
		this.file = file;
	}

	public Reader getReader() throws IOException
	{
		return file;
	}

    public Reader getReader(String enc) throws UnsupportedEncodingException, FileNotFoundException
    {
        // Takashi: what to do here?
//        if (enc!=null)
//        {
//            return new InputStreamReader(new FileInputStream(file),enc);
//        }
//        else
//        {
//            return new InputStreamReader(new FileInputStream(file));
//        }
        return file;
    }

	public Writer getWriter() throws IOException
	{
		throw new IOException("Not supported");
	}

	public boolean isWriteable()
	{
		return false;
	}

	public OutputStream getOutputStream() throws FileNotFoundException
	{
		throw new FileNotFoundException("Not supported");
	}

	public String getPath()
	{
		throw new RuntimeException("Not supported");
	}

	public long lastModified()
	{
		return Long.MIN_VALUE;
	}

	public String toString()
	{
		return super.toString();
	}
}
