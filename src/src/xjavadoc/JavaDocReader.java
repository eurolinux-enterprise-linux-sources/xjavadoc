/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

import java.io.*;

/**
 * A reader which strips away any spaces and stars at the beginning of javadoc.
 * It also keeps track of line numbers, which is needed for error reporting.
 *
 * @author    Aslak Hellesøy
 * @created   3. januar 2002
 */
final class JavaDocReader extends FilterReader
{
	private boolean    badChar = true;
	private boolean    endOfLine = true;
//	private boolean    lastStar = true;
	private int        c = -1;
	private int        lastC = -1;
	private boolean    atEnd = false;

	private int        _lineOffset = 0;

    private int nextChar = -1;

	/**
	 * @param in  the underlying reader, containing javadoc
	 */
	public JavaDocReader( Reader in )
	{
		super( in );
		// we can skip the slash-star-star
		try
		{
			in.read();
			in.read();
			in.read();
		}
		catch( IOException e )
		{
			throw new RuntimeException( "We weren't given javadoc!!" );
		}
	}

	/**
	 * Returns the line offset we're currently reading
	 *
	 * @return   line in the javadoc.
	 */
	public int getLineOffset()
	{
		return _lineOffset;
	}

	/**
	 * Reads a byte of data. The method will block if no input is available.
	 *
	 * @return                 the byte read, or -1 if the end of the stream is
	 *      reached.
	 * @exception IOException  If an I/O error has occurred.
	 */
	public int read() throws IOException
	{
		if( atEnd )
		{
			return -1;
		}
		if( endOfLine )
		{
			endOfLine = false;
			badChar = true;
		}

		do
		{
            if( c == -1 ) {
                // will only happen 1st time
                c = in.read();
            } else {
                c = nextChar;
            }
            if( c == -1 ) {
                return c;
            }
            nextChar = in.read();

			if( c == '*' && nextChar == '/' )
			{
				atEnd = true;
				return -1;
			}
			// UNIX: \n  (LF)
			// MAC:  \r  (CR)
			// PC:   \r\n (CR/LF)
			//
			// We don't want to interpret \r\n as two newlines
			if( c == '\r' || c == '\n' || c == '\f' )
			{
				badChar = false;
				endOfLine = true;
				if( !( lastC == '\r' && c == '\n' ) )
				{
					_lineOffset++;
				}
			}
			else
			{
				if( !( badChar && ( c == '\t' || c == ' ' || c == '*' ) ) )
				{
					// finally found something not a star or spaceish
					badChar = false;
				}
			}
			lastC = c;
		}while ( badChar && c != -1 );
		return c;
	}

	/**
	 * Reads into an array of bytes. Blocks until some input is available.
	 *
	 * @param b                the buffer into which the data is read
	 * @param off              the start offset of the data
	 * @param len              the maximum number of bytes read
	 * @return                 the actual number of bytes read, -1 is returned when
	 *      the end of the stream is reached.
	 * @exception IOException  If an I/O error has occurred.
	 */
	public int read( char[] b, int off, int len ) throws IOException
	{
		if( atEnd )
		{
			return -1;
		}
		for( int i = off; i < len; i++ )
		{
			int c = read();

			if( c == -1 )
			{
				atEnd = true;
				return i - off;
			}
			b[i] = ( char ) c;
		}
		return len;
	}

	/**
	 * Skips bytes of input.
	 *
	 * @param n                bytes to be skipped
	 * @return                 actual number of bytes skipped
	 * @exception IOException  If an I/O error has occurred.
	 */
	public long skip( long n ) throws IOException
	{
		// Can't just read n bytes from 'in' and throw them
		// away, because n bytes from 'in' doesn't necessarily
		// correspond to n bytes from 'this'.
		for( int i = 1; i <= n; i++ )
		{
			int c = read();

			if( c == -1 )
			{
				return i - 1;
			}
		}
		return n;
	}

	/**
	 * Returns the number of bytes that can be read without blocking.
	 *
	 * @return                 the number of available bytes
	 * @exception IOException  Describe the exception
	 */
	public int available() throws IOException
	{
		// We don't really know.  We can ask 'in', but some of those bytes
		// are probably whitespace, and it's possible that all of them are.
		// So we have to be conservative and return zero.
		return 0;
	}
}
