/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

/**
 * Implementation of Type for method return types.
 *
 * @author    Aslak Hellesøy
 * @created   20. mars 2003
 * @version   $Revision: 1.3 $
 */
class ReturnType extends AbstractType
{
	private MethodImpl _method;
	private String     _dimensionAsString;
	private XClass     _type;
	private String     _typeString = "void";
	private int        _dimension = 0;

	public ReturnType( MethodImpl method )
	{
		_method = method;
	}

	public String getDimensionAsString()
	{
		if( _dimensionAsString == null )
		{
			_dimensionAsString = Util.appendDimensionAsString( getDimension(), new StringBuffer() ).toString();
		}
		return _dimensionAsString;
	}

	public XClass getType()
	{
		if( _type == null )
		{
			_type = _method.getContainingAbstractClass().qualify( _typeString );
		}
		return _type;
	}

	public int getDimension()
	{
		return _dimension;
	}

	public void setDimension( int dimension )
	{
		_dimension = dimension;
	}

	public void setType( String typeString )
	{
		_typeString = typeString;
	}
}
