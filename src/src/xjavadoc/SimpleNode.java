/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

public class SimpleNode implements Node
{

	public static int  instanceCount = 0;
	protected Node     parent;
	protected Node[]   children;
	protected int      id;
	protected JavaParser parser;
	protected Token    first, last;

	public SimpleNode( int i )
	{
		id = i;
		instanceCount++;
	}

	public SimpleNode( final JavaParser p, int i )
	{
		this( i );
		parser = p;
	}

	/**
	 * Returns our position under our parent.
	 *
	 * @return   our position under our parent.
	 */
	public int getPosition()
	{
		int i;

		for( i = 0; i < jjtGetParent().jjtGetNumChildren(); i++ )
		{
			if( jjtGetParent().jjtGetChild( i ) == this )
			{
				break;
			}
		}
		return i;
	}

	public Token getFirstToken()
	{
		return first;
	}

	public Token getLastToken()
	{
		return last;
	}

	public String getType()
	{
		return NodeParserTreeConstants.jjtNodeName[id];
	}

	public void jjtOpen()
	{
		first = parser.getToken( 1 );
	}

	public void jjtClose()
	{
		last = parser.getToken( 0 );
	}

	public void jjtSetParent( Node n )
	{
		parent = n;
	}

	public Node jjtGetParent()
	{
		return parent;
	}

	public void jjtAddChild( Node n, int i )
	{
		if( children == null )
		{
			children = new Node[i + 1];
		}
		else if( i >= children.length )
		{
			Node c[] = new Node[i + 1];

			System.arraycopy( children, 0, c, 0, children.length );
			children = c;
		}
		children[i] = n;
	}

	public Node jjtGetChild( int i )
	{
		return children[i];
	}

	public int jjtGetNumChildren()
	{
		return ( children == null ) ? 0 : children.length;
	}

	public String toString()
	{
		return getType();
	}

	public String toString( String prefix )
	{
		return prefix + toString();
	}

	public String dump()
	{
		StringBuffer sb = new StringBuffer();

		dump( sb, "  " );
		return sb.toString();
	}

	private void dump( StringBuffer sb, String prefix )
	{
		sb.append( toString( prefix ) ).append( System.getProperty( "line.separator" ) );

		if( children != null )
		{
			for( int i = 0; i < children.length; ++i )
			{
				SimpleNode n = ( SimpleNode ) children[i];

				if( n != null )
				{
					n.dump( sb, prefix + prefix );
				}
			}
		}
	}
}

