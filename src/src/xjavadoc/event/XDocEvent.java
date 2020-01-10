/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc.event;

import java.util.EventObject;
import xjavadoc.XDoc;

/**
 * Describe what this class does
 *
 * @author         Aslak Hellesøy
 * @created        30. januar 2002
 * @todo-javadoc   Write javadocs
 */
public class XDocEvent extends EventObject
{
	/**
	 * create new event containing doc object.
	 *
	 * @param doc  object to wrap
	 */
	public XDocEvent( XDoc doc )
	{
		super( doc );
	}
}
