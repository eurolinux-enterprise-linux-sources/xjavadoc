/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Iterator;
import java.lang.reflect.Modifier;

/**
 * Baseclass for XExecutableMember.
 *
 * @author    Aslak Hellesøy
 * @created   9. mars 2003
 */
abstract class AbstractExecutableMember extends MemberImpl implements XExecutableMember
{
	/**
	 * Maximum dimension of a parameter. We want to avoid exessive Integer object
	 * creation.
	 */
	private final static int MAX_ARRAY_SIZE = 6;
	private final static Integer[] _dimensions = new Integer[MAX_ARRAY_SIZE];

	/**
	 * Initial size of data to hold parameters. Estimate of average number of
	 * params in a method.
	 */
	private final static int PARAMETER_DATA_SIZE = 2;

	/**
	 * Initial size of ParameterImpl pool. Estimate of max number of params in a
	 * method
	 */
	private final static int INITIAL_PARAMETER_POOL_SIZE = 20;
	private static ParameterImpl[] _parameterPool = new ParameterImpl[INITIAL_PARAMETER_POOL_SIZE];
	private List       _thrownExceptions;
	private List       _parameterData;
	private String     _nameWithSignature;
	private String     _signature;
	private String     _stringId;

	static
	{
		for( int i = 0; i < MAX_ARRAY_SIZE; i++ )
		{
			_dimensions[i] = new Integer( i );
		}
	}

	static
	{
		for( int i = 0; i < INITIAL_PARAMETER_POOL_SIZE; i++ )
		{
			_parameterPool[i] = new ParameterImpl();
		}
	}

	protected AbstractExecutableMember( AbstractClass containingClass, XTagFactory tagFactory )
	{
		super( containingClass, tagFactory );
		if( containingClass == null )
		{
			throw new IllegalArgumentException( "containingClass can't be null" );
		}
	}

	private final static String toString( XParameter parameter, boolean withParam )
	{
		if( parameter == null )
		{
			throw new IllegalStateException( "parameter can't be null!" );
		}

		StringBuffer sb = new StringBuffer( parameter.getType().getQualifiedName() );

		Util.appendDimensionAsString( parameter.getDimension(), sb );
		if( withParam )
		{
			sb.append( " " ).append( parameter.getName() );
		}
		return sb.toString();
	}

	/**
	 * Gets the Native attribute of the AbstractExecutableMember object
	 *
	 * @return   The Native value
	 */
	public final boolean isNative()
	{
		return ( getModifierSpecifier() & Modifier.NATIVE ) != 0;
	}

	/**
	 * Gets the Synchronized attribute of the AbstractExecutableMember object
	 *
	 * @return   The Synchronized value
	 */
	public final boolean isSynchronized()
	{
		return ( getModifierSpecifier() & Modifier.SYNCHRONIZED ) != 0;
	}

	/**
	 * Returns the method parameters.
	 *
	 * @return   the method parameters
	 */
	public final List getParameters()
	{
		List parameters = null;

		if( _parameterData == null )
		{
			parameters = EMPTY_LIST;
		}
		else
		{
			int requiredSize = _parameterData.size() / 3;

			parameters = new ArrayList( requiredSize );
			if( _parameterPool.length < requiredSize )
			{
				// increase flyweight pool size
				ParameterImpl[] newPool = new ParameterImpl[requiredSize];

				System.arraycopy( _parameterPool, 0, newPool, 0, _parameterPool.length );
				for( int j = _parameterPool.length; j < newPool.length; j++ )
				{
					newPool[j] = new ParameterImpl();
				}
				_parameterPool = newPool;
			}

			for( int i = 0; i < requiredSize; i++ )
			{
				try
				{
					_parameterPool[i].setState( this, i );
					parameters.add( _parameterPool[i] );
				}
				catch( IndexOutOfBoundsException e )
				{
					throw new IllegalStateException( "In member " + getName() + ". Tried to set " + i + "th parameter. Size was " + requiredSize );
				}
			}
		}
		return Collections.unmodifiableList( parameters );
	}

	/**
	 * Gets the signature
	 *
	 * @param withParam  if true, include the parameters in the signature.
	 *      Otherwise, only the types will be used.
	 * @return           the signature
	 */
	public final String getSignature( boolean withParam )
	{
		if( _signature == null )
		{
			_signature = appendSignature( new StringBuffer(), withParam ).toString();
		}
		return _signature;
	}

	/**
	 * Gets the name and signature
	 *
	 * @param withParam  if true, include the parameters in the signature.
	 *      Otherwise, only the types will be used.
	 * @return           the name and signature
	 */
	public final String getNameWithSignature( boolean withParam )
	{
		if( _nameWithSignature == null )
		{
			_nameWithSignature = appendSignature( new StringBuffer( getName() ), withParam ).toString();
		}
		return _nameWithSignature;
	}

	public String getParameterTypes()
	{
		StringBuffer sb = new StringBuffer();

		for( Iterator i = getParameters().iterator(); i.hasNext();  )
		{
			// resolve first
			( ( XParameter ) i.next() ).getType();
		}

		boolean comma = false;

		for( Iterator i = getParameters().iterator(); i.hasNext();  )
		{
			// By calling toString(XParameter) we risk that the current parameter flyweights'
			// state is overwritten. This will happen when toString is calling parameter.type()
			// and that type isn't resolved yet. That's why the additional loop is added above,
			// to make sure everything required is resolved before calling toString.
			// This solves the problem, but might slow down speed a little (Aslak)
			if( comma )
			{
				sb.append( ',' );
			}

			XParameter parameter = ( XParameter ) i.next();

			sb.append( parameter.getType().getType() );
			comma = true;
		}
		return sb.toString();
	}

	public List getThrownExceptions()
	{
		return _thrownExceptions == null ? EMPTY_LIST : Collections.unmodifiableList( getQualifiedExceptions() );
	}

	public XProgramElement getSuperElement( boolean forMethod )
	{
		XClass superclass = getContainingClass().getSuperclass();

		while( superclass != null )
		{
			XExecutableMember superExecutableMember;

			if( forMethod )
			{
				superExecutableMember = superclass.getMethod( getNameWithSignature( false ) );
			}
			else
			{
				// for constructor
				superExecutableMember = superclass.getConstructor( getNameWithSignature( false ) );
			}
			if( superExecutableMember != null )
			{
				return superExecutableMember;
			}
			else
			{
				superclass = superclass.getSuperclass();
			}
		}
		return null;
	}

	public boolean throwsException( String exception_class_name )
	{
		//we loop over _thrownExceptions, so we don't qualify exception classes unneccessarily
		for( Iterator iterator = getThrownExceptions().iterator(); iterator.hasNext();  )
		{
			XClass exception = (XClass) iterator.next();

			if( exception.getQualifiedName().equals( exception_class_name ) )
				return true;
		}

		return false;
	}

	/**
	 * Adds a parameter
	 *
	 * @param type       qualified nyme of parameter type
	 * @param name       parameter name
	 * @param dimension  parameter dimension
	 */
	public void addParameterData( String type, String name, int dimension )
	{
		if( _parameterData == null )
		{
			_parameterData = new ArrayList( PARAMETER_DATA_SIZE * 3 );
		}
		_parameterData.add( type );
		_parameterData.add( name );
		_parameterData.add( _dimensions[dimension] );
	}

	public void addThrownException( String thrownException )
	{
		if( _thrownExceptions == null )
		{
			_thrownExceptions = new ArrayList();
		}
		_thrownExceptions.add( thrownException );
	}

	public boolean equals( Object o )
	{
		if( !( o.getClass() == getClass() ) )
		{
			return false;
		}

		AbstractExecutableMember other = ( AbstractExecutableMember ) o;

		return stringId().equals( other.stringId() );
	}

	public int hashCode()
	{
		return stringId().hashCode();
	}

	public String toString()
	{
		return stringId();
	}

	protected abstract String buildStringId();

	final String getParameterType( int index )
	{
		return ( String ) _parameterData.get( index * 3 );
	}

	final String getParameterName( int index )
	{
		return ( String ) _parameterData.get( index * 3 + 1 );
	}

	final int getParameterDimension( int index )
	{
		return ( ( Integer ) _parameterData.get( index * 3 + 2 ) ).intValue();
	}

	private List getQualifiedExceptions()
	{
		//if the list is not yet full qualified then qualify it
		if( _thrownExceptions.get( 0 ) instanceof String )
		{
			List qualified_thrown_exceptions = new ArrayList();

			for( Iterator iterator = _thrownExceptions.iterator(); iterator.hasNext();  )
			{
				String exception_class_name = ( String ) iterator.next();

				qualified_thrown_exceptions.add( getContainingAbstractClass().qualify( exception_class_name ) );
			}

			_thrownExceptions = qualified_thrown_exceptions;
		}

		return _thrownExceptions;
	}

	/**
	 * Gets the StringId attribute of the MethodImpl object
	 *
	 * @return   The StringId value
	 */
	private final String stringId()
	{
		if( _stringId == null )
		{
			_stringId = buildStringId();
		}
		return _stringId;
	}

	private final StringBuffer appendSignature( StringBuffer sb, boolean withParam )
	{
		sb.append( '(' );

		for( Iterator i = getParameters().iterator(); i.hasNext();  )
		{
			// resolve first
			( ( XParameter ) i.next() ).getType();
		}

		boolean comma = false;

		for( Iterator i = getParameters().iterator(); i.hasNext();  )
		{
			// By calling toString(XParameter) we risk that the current parameter flyweights'
			// state is overwritten. This will happen when toString is calling parameter.type()
			// and that type isn't resolved yet. That's why the additional loop is added above,
			// to make sure everything required is resolved before calling toString.
			// This solves the problem, but might slow down speed a little (Aslak)
			if( comma )
			{
				sb.append( ',' );
			}
			sb.append( toString( ( XParameter ) i.next(), withParam ) );
			comma = true;
		}
		return sb.append( ')' );
	}
}
