/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 * $Id: XProgramElement.java,v 1.18 2004/07/10 14:09:03 pilhuhn Exp $
 */
package xjavadoc;

import java.util.List;

/**
 * Describe what this class does
 *
 * @author    Ara Abrahamian
 * @author    Aslak Hellesøy
 * @created   February 16, 2002
 * @version   $Revision: 1.18 $
 */

public interface XProgramElement extends Comparable, Named
{
	XClass getContainingClass();
	XPackage getContainingPackage();
	boolean isFinal();
	boolean isPackagePrivate();
	boolean isPrivate();
	boolean isProtected();
	boolean isAbstract();
	boolean isPublic();
	boolean isStatic();
	String getModifiers();
	int getModifierSpecifier();
	XDoc getDoc();
	XProgramElement getSuperElement();
	List getSuperInterfaceElements();
    XJavaDoc getXJavaDoc();

	/**
	 * update docs
	 */
	void updateDoc();

}

