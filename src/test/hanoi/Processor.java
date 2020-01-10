/*
 * Hanoi ProcessInstance Engine
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package hanoi;

import java.io.*;

/**
 * A processor may intercept the execution of activities in the engine to perform additional functionality.
 *
 * <!-- $Id: Processor.java,v 1.2 2002/08/25 18:02:45 rinkrank Exp $ -->
 * <!-- $Author: rinkrank $ -->
 *
 * @author Jon Tirs&acute;n (tirsen@users.sourceforge.net)
 * @version $Revision: 1.2 $
 */
public interface Processor
{
    /**
     * Interface for executing the next processor in the chain.
     *
     * <!-- $Id: Processor.java,v 1.2 2002/08/25 18:02:45 rinkrank Exp $ -->
     * <!-- $Author: rinkrank $ -->
     *
     * @author Jon Tirs&eacute;n (tirsen@users.sourceforge.net)
     * @version $Revision: 1.2 $
     */

    public interface Next extends Serializable
    {
        int runNext();
    }

	private Next anonClassImplements = new Next() {
		public int runNext() {return 0;}
	};

	private Exception anonClassExtends = new Exception() {
		public void fubar() {}
	};

    void init(ProcessInstance instance);

    int run(Next next, Activity activity);

    Object createProcessorConfig(ProcessDefinition definition, Activity activity);
}

