/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

import org.apache.commons.collections.Predicate;

/**
 * Describe what this class does
 *
 * @author    Aslak Hellesøy
 * @created   25. februar 2003
 */
public interface XMethod extends XExecutableMember
{
	/**
	 * Predicate that can be used to retrieve all property mutator methods.
	 */
	public final static Predicate PROPERTY_MUTATOR_PREDICATE = new PropertyMutatorPredicate();

	/**
	 * Predicate that can be used to retrieve all property accessor methods.
	 */
	public final static Predicate PROPERTY_ACCESSOR_PREDICATE = new PropertyAccessorPredicate();

	/**
	 * Returns the return type of the method.
	 *
	 * @return   the return type of the method.
	 */
	Type getReturnType();

	/**
	 * Returns the type of the property this method represents, or null if this
	 * method is not a property method.
	 *
	 * @return   the property type
	 * @see      #isPropertyMutator
	 * @see      #isPropertyAccessor
	 * @see      #getPropertyName
	 */
	Type getPropertyType();

	/**
	 * Returns the property name of this method (if it is an accessor or mutator),
	 * or null if it is not.
	 *
	 * @return   the property name.
	 */
	String getPropertyName();

	/**
	 * Returns the name of the method with the prefix stripped away. The prefix is
	 * the first series of lower case characters. Example:
	 * <ul>
	 *   <li> "isIt" -> "It"</li>
	 *   <li> "setIt" -> "It"</li>
	 *   <li> "addIt" -> "It"</li>
	 *   <li> "createIt" -> "It"</li>
	 *   <li> "isit" -> null</li>
	 * </ul>
	 *
	 *
	 * @return   the property name.
	 */
	String getNameWithoutPrefix();

	/**
	 * @return   true if this is a public void setXxx(Xxx) method
	 */
	boolean isPropertyMutator();

	/**
	 * @return   true if this is a public Xxx getXxx() method
	 */
	boolean isPropertyAccessor();

	/**
	 * If this method is a mutator, and a corresponding accessor exists, that
	 * accessor will be returned. Otherwise, null is returned.
	 *
	 * @return   the corresponding accessor.
	 */
	public XMethod getAccessor();

	/**
	 * If this method is an accessor, and a corresponding mutator exists, that
	 * mutator will be returned. Otherwise, null is returned.
	 *
	 * @return   the corresponding mutator.
	 */
	public XMethod getMutator();

	/**
	 * @created   20. mars 2003
	 */
	static class PropertyAccessorPredicate implements Predicate
	{
		public boolean evaluate( Object o )
		{
			XMethod method = ( XMethod ) o;

			return method.isPropertyAccessor();
		}
	}

	/**
	 * @created   20. mars 2003
	 */
	static class PropertyMutatorPredicate implements Predicate
	{
		public boolean evaluate( Object o )
		{
			XMethod method = ( XMethod ) o;

			return method.isPropertyMutator();
		}
	}
}
