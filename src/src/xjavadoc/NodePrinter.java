/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

import java.io.PrintWriter;
import java.io.Writer;

/**
 * This visitor prints a node and all its children to a PrintWriter. The
 * PrintWriter must be passed to the data variable in the jjtAccept method of
 * the node from which printing should start. T print a whole class, call
 * compilationUnit.jjtAccept( new PrintVisitor(), System.out );
 *
 * @author    Aslak Hellesøy
 * @created   8. januar 2002
 * @todo      replace PrintWriter with PrintWriter and remove the
 *      addUnicodeEscapes()
 */
public final class NodePrinter
{

	/**
	 * Describe what the method does
	 *
	 * @param node     Describe what the parameter does
	 * @param o
	 * @todo-javadoc   Write javadocs for method parameter
	 * @todo-javadoc   Write javadocs for method
	 * @todo-javadoc   Write javadocs for method parameter
	 * @todo-javadoc   Write javadocs for method parameter
	 * @todo-javadoc   Write javadocs for return value
	 */
	public static void print( SimpleNode node, Writer o )
	{
		PrintWriter out = new PrintWriter( o );
		Token t1 = node.getFirstToken();
		Token t = new Token();

		t.next = t1;

		SimpleNode n;

		for( int ord = 0; ord < node.jjtGetNumChildren(); ord++ )
		{
			n = ( SimpleNode ) node.jjtGetChild( ord );
			while( true )
			{
				t = t.next;
				if( t == n.getFirstToken() )
				{
					break;
				}
				print( t, out );
			}
			print( n, out );
			t = n.getLastToken();
		}

		while( t != node.getLastToken() && t != null )
		{
			t = t.next;
			if( t != null )
			{
				print( t, out );
			}
		}
	}

	/**
	 * Describe what the method does
	 *
	 * @param t        Describe what the parameter does
	 * @param out      Describe what the parameter does
	 * @todo-javadoc   Write javadocs for method
	 * @todo-javadoc   Write javadocs for method parameter
	 * @todo-javadoc   Write javadocs for method parameter
	 */
	private final static void print( Token t, PrintWriter out )
	{
		Token tt = t.specialToken;

		if( tt != null )
		{
			while( tt.specialToken != null )
			{
				tt = tt.specialToken;
			}
			while( tt != null )
			{
				out.print( addUnicodeEscapes( tt.image ) );
//				out.print(tt.image);
				tt = tt.next;
			}
		}
		out.print( addUnicodeEscapes( t.image ) );
//		out.print(t.image);
	}

	/**
	 * Describe the method
	 *
	 * @param str      Describe the method parameter
	 * @return         Describe the return value
	 * @todo-javadoc   Describe the method
	 * @todo-javadoc   Describe the method parameter
	 * @todo-javadoc   Write javadocs for return value
	 */
	private final static String addUnicodeEscapes( final String str )
	{
		return str;
	}

	/**
	 * Describe the method
	 *
	 * @param str      Describe the method parameter
	 * @return         Describe the return value
	 * @todo-javadoc   Describe the method
	 * @todo-javadoc   Describe the method parameter
	 * @todo-javadoc   Write javadocs for return value
	 */
	protected String addUnicodeEscapesOld( String str )
	{
		String retval = "";
		char ch;

		for( int i = 0; i < str.length(); i++ )
		{
			ch = str.charAt( i );
			if( ( ch < 0x20 || ch > 0x7e ) &&
				ch != '\t' && ch != '\n' && ch != '\r' && ch != '\f' )
			{
				String s = "0000" + Integer.toString( ch, 16 );

				retval += "\\u" + s.substring( s.length() - 4, s.length() );
			}
			else
			{
				retval += ch;
			}
		}
		return retval;
	}

}
