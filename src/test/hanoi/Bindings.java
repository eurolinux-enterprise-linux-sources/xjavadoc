/*
 * Hanoi ProcessInstance Engine
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package hanoi;

import com.tirsen.hanoi.beans.InvalidDefinitionException;
import com.tirsen.hanoi.beans.PropertyAccessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * In or out-parameters to activities are bound to the datasheet or resources by using this processor.
 *
 * Each step in a workflow has an instance of this associated with it.
 * It contains all the bindings to the input parameters and from the out parameters and the step.
 *
 * <p>
 * Input parameters can be bound to:
 * <li> A static/constant value.
 * <li> One of the variables in the datasheet.
 * <li> A named resouce of the resource registry.
 *
 * <p>
 * Output parameters can be bound to:
 * <li> One of the variables in the datasheet.
 *
 * <p>
 * Note that the only persistent state of an executing workflow is it's datasheet. If a step
 * contains state that is not bound to a variable in the datasheet that state will not be persisted.
 *
 * <!-- $Id: Bindings.java,v 1.1 2002/08/14 12:38:24 rinkrank Exp $ -->
 * <!-- $Author: rinkrank $ -->
 *
 * @author Jon Tirs&acute;n (tirsen@users.sourceforge.net)
 * @version $Revision: 1.1 $
 */
public class Bindings
{
    private static final Log logger = LogFactory.getLog(Bindings.class);

    private ProcessInstance instance;
    private ProcessDefinition definition;
    private Activity activity;
    private Collection inputBindings = new ArrayList();
    private Collection outputBindings = new ArrayList();

    /*
    // sorry, doesn't work yet...
    private static class MyPersistenceDelegate extends DefaultPersistenceDelegate
    {
        protected void initialize(Class type, Object oldInstance, Object newInstance, Encoder out)
        {
            super.initialize(type, oldInstance, newInstance, out);
            Bindings bindings = (Bindings) oldInstance;
            Binding[] before = bindings.getBefore();
            for(int i = 0; i < before.length; i++)
            {
                Binding binding = before[i];
                out.writeStatement(new Statement(oldInstance, "addBefore", new Object[] { binding }));
            }
            Binding[] after = bindings.getBefore();
            for(int i = 0; i < before.length; i++)
            {
                Binding binding = before[i];
                out.writeStatement(new Statement(oldInstance, "addAfter", new Object[] { binding }));
            }
        }
    }

    static
    {
        try
        {
            BeanInfo beanInfo = Introspector.getBeanInfo(Bindings.class);
            beanInfo.getBeanDescriptor().setValue("persistenceDelegate", new MyPersistenceDelegate());
        }
        catch(IntrospectionException e)
        {
            throw new Error("Could not initialize persistence-delegate. Bailing out!");
        }
    }
    */

    public static class Processor implements com.tirsen.hanoi.engine.Processor
    {
        private ProcessInstance instance;

        public void init(ProcessInstance instance)
        {
            this.instance = instance;
        }

        public int run(Next next, Activity activity)
        {
            Bindings bindings = (Bindings) instance.getDefinition().getProcessorConfig(this, activity);
            if(bindings != null)
            {
                bindings.setInstance(instance);
                bindings.setActivity(activity);
                bindings.beforeRun();
            }
            int result = next.runNext();
            if(bindings != null)
            {
                bindings.afterRun();
            }
            return result;
        }

        public Object createProcessorConfig(ProcessDefinition definition, Activity activity)
        {
            Bindings bindings = new Bindings();
            bindings.setDefinition(definition);
            bindings.setActivity(activity);
            return bindings;
        }
    }

    public static abstract class Binding
    {
        protected PropertyAccessor toAccessor;
        protected ProcessDefinition definition;

        public abstract void execute();

        public void setDefinition(ProcessDefinition definition)
        {
            this.definition = definition;
        }

        public String getToProperty()
        {
            return toAccessor == null ? null : toAccessor.getProperty();
        }

        public void setToProperty(String toProperty)
        {
            if(toProperty == null) toAccessor = null;
            else toAccessor = new PropertyAccessor(toProperty, true, false);
        }

        void setTo(Object to)
        {
            logger.debug("this = " + this);
            logger.debug("toAccessor = " + toAccessor);
            toAccessor.setBean(to);
        }

        public String toString()
        {
            return getClass().getName() + "[" + getToProperty() + "]";
        }
    }

    public static class ValueBinding extends Binding
    {
        private Object value;

        public ValueBinding()
        {
        }

        public ValueBinding(String toProperty, Object value)
        {
            setValue(value);
            setToProperty(toProperty);
        }

        public Object getValue()
        {
            return value;
        }

        public void setValue(Object value)
        {
            this.value = value;
        }

        public void execute()
        {
            toAccessor.set(value);
        }
    }

    public static class ResourceBinding extends Binding
    {
        private Object resource;

        public ResourceBinding()
        {
        }

        public ResourceBinding(String toProperty, Object resource)
        {
            setResource(resource);
            setToProperty(toProperty);
        }

        public void setResource(Object resource)
        {
            this.resource = resource;
        }

        public Object getResource()
        {
            return resource;
        }

        public void execute()
        {
            toAccessor.set(resource);
        }
    }

    public static class DynamicBinding extends Binding
    {
        private PropertyAccessor fromAccessor;

        public DynamicBinding()
        {
        }

        public DynamicBinding(String toProperty, String fromProperty)
        {
            setToProperty(toProperty);
            setFromProperty(fromProperty);
        }

        public void compile() throws InvalidDefinitionException
        {
        }

        public String getFromProperty()
        {
            return fromAccessor == null ? null : fromAccessor.getProperty();
        }

        public void setFromProperty(String fromProperty)
        {
            if(fromProperty == null) fromAccessor = null;
            else fromAccessor = new PropertyAccessor(fromProperty, false, true);
        }

        public void setFrom(Object from)
        {
            fromAccessor.setBean(from);
        }

        public void execute()
        {
            Object value = fromAccessor.get();

            toAccessor.set(value);
        }
    }

    public ProcessDefinition getDefinition()
    {
        return definition;
    }

    public void setDefinition(ProcessDefinition definition)
    {
        this.definition = definition;
        attachBindings();
    }

    public void setActivity(Activity activity)
    {
        this.activity = activity;
        attachBindings();
    }

    public Step getStep()
    {
        return activity;
    }

    public void removeBinding(Binding binding)
    {
        binding.setDefinition(null);
        inputBindings.remove(binding);
        outputBindings.remove(binding);
    }

    public void addInput(Binding binding)
    {
        binding.setDefinition(definition);
        inputBindings.add(binding);
    }

    public void addOutput(Binding binding)
    {
        binding.setDefinition(definition);
        outputBindings.add(binding);
    }

    private void attachBindings()
    {
        for(Iterator iterator = inputBindings.iterator(); iterator.hasNext();)
        {
            Binding binding = (Binding) iterator.next();
            attachInput(binding);
        }
        for(Iterator iterator = outputBindings.iterator(); iterator.hasNext();)
        {
            Binding binding = (Binding) iterator.next();
            attachOutput(binding);
        }
    }

    public void setInstance(ProcessInstance instance)
    {
        this.instance = instance;
        setDefinition(instance.getDefinition());
        attachBindings();
    }

    private void attachOutput(Binding binding)
    {
        if(definition != null)
        {
            binding.setDefinition(definition);
            binding.setTo(definition.getTemplateDatasheet());
        }
        if(instance != null)
        {
            binding.setTo(instance.getDatasheet());
        }
        if(binding instanceof DynamicBinding) ((DynamicBinding) binding).setFrom(activity);
    }

    private void attachInput(Binding binding)
    {
        if(definition != null)
        {
            binding.setDefinition(definition);
            if(binding instanceof DynamicBinding) ((DynamicBinding) binding).setFrom(definition.getTemplateDatasheet());
        }
        if(instance != null)
        {
            if(binding instanceof DynamicBinding) ((DynamicBinding) binding).setFrom(instance.getDatasheet());
        }
        binding.setTo(activity);
    }

    public void beforeRun()
    {
        for(Iterator iterator = inputBindings.iterator(); iterator.hasNext();)
        {
            Binding binding = (Binding) iterator.next();
            binding.execute();
        }
    }

    public void afterRun()
    {
        for(Iterator iterator = outputBindings.iterator(); iterator.hasNext();)
        {
            Binding binding = (Binding) iterator.next();
            binding.execute();
        }
    }

    public Binding[] getInput()
    {
        return (Binding[]) inputBindings.toArray(new Binding[0]);
    }

    public Binding[] getOutput()
    {
        return (Binding[]) outputBindings.toArray(new Binding[0]);
    }

    public void setInput(Binding[] bindings)
    {
        for(int i = 0; i < bindings.length; i++)
        {
            Binding binding = bindings[i];
            addInput(binding);
        }
    }

    public void setOutput(Binding[] bindings)
    {
        for(int i = 0; i < bindings.length; i++)
        {
            Binding binding = bindings[i];
            addOutput(binding);
        }
    }
}
