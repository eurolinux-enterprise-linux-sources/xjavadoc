/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc.event;

import java.util.EventObject;
import xjavadoc.XTag;

/**
 * Describe what this class does
 *
 * @author         Aslak Hellesøy
 * @created        30. januar 2002
 * @todo-javadoc   Write javadocs
 */
public class XTagEvent extends EventObject
{
	/**
	 * create new event containing tag object.
	 *
	 * @param tag  object to wrap
	 */
	public XTagEvent( XTag tag )
	{
		super( tag );
	}
}
