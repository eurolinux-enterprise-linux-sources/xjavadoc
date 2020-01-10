/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

import java.util.List;

/**
 * Common functionality for methods and constructors.
 *
 * @author    Ara Abrahamian
 * @author    Aslak Hellesøy
 * @created   9. mars 2003
 */
public interface XExecutableMember extends XMember
{
	boolean isNative();
	boolean isSynchronized();

	/**
	 * Returns the parameters.
	 *
	 * @return   a Collection of {@link XParameter}.
	 */
	List getParameters();

	/**
	 * Returns the thrown exception classes.
	 *
	 * @return   a Collection of {@link XClass}.
	 */
	List getThrownExceptions();

	/**
	 * Return true if the member throws the specified exception in its throws
	 * block.
	 *
	 * @param exception_class_name
	 * @return                      true if the member throws the exception
	 */
	boolean throwsException( String exception_class_name );

	/**
	 * Return true if this is a constructor.
	 *
	 * @return   true if this is a constructor.
	 */
	boolean isConstructor();

	/**
	 * Returns the signature. E.g. <code>(java.lang.String,int)</code> or
         * <code>(java.lang.String foo,int bar)</code>.
	 *
	 * @param withParam  whether or not to include the parameter names in the
	 *      signature.
	 * @return           the signature.
	 */
	String getSignature( boolean withParam );

	/**
	 * Gets the name and signature
	 *
	 * @param withParam  whether or not to include the parameter names in the
	 *      signature.
	 * @return           the name and signature
	 */
	String getNameWithSignature( boolean withParam );

	/**
	 * Returns the parameters as a comma separated list of classes. E.g. a method
	 * with signature <code>(java.lang.String,int)</code> would return
         * <code>java.lang.String.class, java.lang.Integer.TYPE</code>.
	 *
	 * @return   comma separated list of types for all parameters.
	 */
	String getParameterTypes();
}
