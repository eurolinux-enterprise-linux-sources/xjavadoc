/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

import java.io.*;
import junit.framework.*;

/**
 * JUnit test for JavaDocReader.
 *
 * @author      <a href="mailto:aslak.hellesoy@bekk.no">Aslak Hellesøy</a>
 * @created     3. januar 2002
 * @ejb:bla     bla
 * @param what  about this one? Or this one?
 * @oh          dear="we" should="go to bed"
 */
public class JavaDocReaderTest extends TestCase
{
	private String     javadoc =
		"/********************** This is in the doc too\n" +
		" * JUnit test for\n" +
		"   * JavaDocReader.\n" +
		"     * @ejb:bla * bla\n" +
		" *   @param what about\n" +
		" * this one?\n" +
		" *Or this\n" +
		"     one?\n" +
		" * @oh dear=\"we\" should=\"go to bed\"\n" +
		" */";

	private String     expected =
		"This is in the doc too\n" +
		"JUnit test for\n" +
		"JavaDocReader.\n" +
		"@ejb:bla * bla\n" +
		"@param what about\n" +
		"this one?\n" +
		"Or this\n" +
		"one?\n" +
		"@oh dear=\"we\" should=\"go to bed\"\n";

	public void testRead() throws IOException
	{
		JavaDocReader jr = new JavaDocReader( new StringReader( javadoc ) );
		int i;

		StringWriter w = new StringWriter();

		while( ( i = jr.read() ) != -1 )
		{
			w.write( i );
		}
		w.flush();

		assertEquals( expected, w.toString() );
	}

	public void testReadLine() throws IOException
	{
        JavaDocReader jr = new JavaDocReader( new StringReader( javadoc ) );
        BufferedReader br = new BufferedReader( jr );
        String line;
        StringBuffer sb = new StringBuffer();

        while( ( line = br.readLine() ) != null )
        {
            sb.append( line ).append( '\n' );
        }

        assertEquals( expected, sb.toString() );
	}

    public void testNoStarAtEndOfOneLineJavadocWithTag() throws Exception
    {
        JavaDocReader jr = new JavaDocReader( new StringReader( "/** dum di dum */" ) );
        BufferedReader br = new BufferedReader( jr );
        assertEquals( "dum di dum ", br.readLine() );
    }

}
