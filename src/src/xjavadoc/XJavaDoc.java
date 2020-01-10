/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

import xjavadoc.filesystem.AbstractFile;
import xjavadoc.tags.TagIntrospector;

import java.io.PrintStream;
import java.util.*;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.CollectionUtils;

/**
 * This class represents the entry-point for xjavadoc classes. Come here to get
 * classes and packages.
 *
 * @author    Aslak Hellesøy
 * @created   3. januar 2002
 */
public final class XJavaDoc
{
	/**
	 * Indicates whether this XJavaDoc was built with or without unicode support
	 */
	public final static String IS_UNICODE = "@IS_UNICODE@";
	/**
	 * messgage level for reporting unqualified classes when there are no imported
	 * packages
	 */
	public final static int NO_IMPORTED_PACKAGES = 0;
	/**
	 * messgage level for reporting unqualified classes when there are one or more
	 * imported packages
	 */
	public final static int ONE_OR_MORE_IMPORTED_PACKAGES = 1;

	private final static List PRIMITIVES = Collections.unmodifiableList( Arrays.asList( new String[]
		{"void", "java.lang.Void.TYPE",
		"byte", "java.lang.Byte.TYPE",
		"short", "java.lang.Short.TYPE",
		"int", "java.lang.Integer.TYPE",
		"long", "java.lang.Long.TYPE",
		"char", "java.lang.Character.TYPE",
		"float", "java.lang.Float.TYPE",
		"double", "java.lang.Double.TYPE",
		"boolean", "java.lang.Boolean.TYPE"
		} ) );

	private static HashMap _primitiveClasses = new HashMap();
	private final Map  _binaryClasses = new HashMap();
	private final Map  _unknownClasses = new HashMap();
	private final Map  _packages = new HashMap();
	private final Set  _sourceSets = new HashSet();

	/**
	 * This map contains all the classes that were passed in the source sets,
	 * excluding all inner classes.
	 */
	private final Map  _sourceSetSourceClasses = new HashMap();

	/**
	 * This map contains the same classes as _sourceSetSourceClasses, but it is
	 * also populated with additional classes that may be accessed that were not in
	 * the source sets. This can be superclasses, classes referenced in methods,
	 * import statements etc.
	 */
	private final Map  _allSourceClasses = new HashMap();

	private final Set  _sourceSetClassNames = new TreeSet();

	private final Map  _properties = new HashMap();

	private final Map  _abstractFileClasses = new HashMap();

    private final XTagFactory _tagFactory = new XTagFactory();

	/**
	 * This map contains all the classes that were passed in the source sets,
	 * including all inner classes.
	 */
	private Collection _sourceSetSourceClassesWithInnerClasses = new ArrayList();

	/**
	 * Remember when we're born. We hate sources that are born after us and we
	 * pretend they don't exist, because if we don't we'll have very unpredictable
	 * behaviour. Well, since we have editor plugin and this is singleton object,
	 * we have to relax our policy on this. Or we will have to restart editor every
	 * time we like to tag the same class again...
	 */
	private long       _birthday;

	/**
	 * info, error and warning messages related to parsing and class qualification
	 */
	private List       _logMessages = new LinkedList();

	/**
	 * sticky parameter for useNodeParser. _useNodeParser = true -> slower parsing,
	 * but modifiable javaodcs.
	 */
	private boolean    _useNodeParser = false;

	/** charset for source file */
	private String _encoding = null;
	
	/** charset for generated file */
	private String _docEncoding = null;
	
	public XJavaDoc()
	{
		_birthday = System.currentTimeMillis();
        for( int i = 0; i < PRIMITIVES.size(); i += 2 )
        {
            addPrimitive( ( String ) PRIMITIVES.get( i ), ( String ) PRIMITIVES.get( i + 1 ) );
        }
	}

	/**
	 * Dump to sytem out the status of XJavadoc.
	 */
	public static void printMemoryStatus()
	{
		System.out.println( "ParameterImpl instances:   " + ParameterImpl.instanceCount );
		System.out.println( "MethodImpl instances:      " + MethodImpl.instanceCount );
		System.out.println( "ConstructorImpl instances: " + ConstructorImpl.instanceCount );
		System.out.println( "SimpleNode instances:      " + SimpleNode.instanceCount );
		System.out.println( "SourceClass instances:     " + SourceClass.instanceCount );
		System.out.println( "XDoc instances:            " + XDoc.instanceCount );
		System.out.println( "DefaultXTag instances:     " + DefaultXTag.instanceCount );
		System.out.println( "BinaryClass instances:     " + BinaryClass.instanceCount );
		System.out.println( "UnknownClass instances:    " + UnknownClass.instanceCount );

		System.out.println( "Total memory:    " + ( Runtime.getRuntime().totalMemory() / ( 1024 * 1024 ) ) );
		System.out.println( "Free memory:    " + Runtime.getRuntime().freeMemory() / ( 1024 * 1024 ) );
	}

	/**
	 * Replaces <code>${xxx}</code> style constructions in the given value with the
	 * string value of the corresponding data types. NOTE: This method was taken
	 * directly from Ant's source code (org.apache.tools.ant.ProjectHelper) and
	 * modified slightly to use a Map instead of a HashMap.
	 *
	 * @param value  The string to be scanned for property references. May be
	 *      <code>null</code> , in which case this method returns immediately with
	 *      no effect.
	 * @param keys   Mapping (String to String) of property names to their values.
	 *      Must not be <code>null</code>.
	 * @return       the original string with the properties replaced, or <code>null</code>
	 *      if the original string is <code>null</code>.
	 */
	public static String replaceProperties( String value, Map keys )
	{
		if( value == null )
		{
			return null;
		}

		ArrayList fragments = new ArrayList();
		ArrayList propertyRefs = new ArrayList();

		parsePropertyString( value, fragments, propertyRefs );

		StringBuffer sbuf = new StringBuffer();
		Iterator i = fragments.iterator();
		Iterator j = propertyRefs.iterator();

		while( i.hasNext() )
		{
			String fragment = ( String ) i.next();

			if( fragment == null )
			{
				String propertyName = ( String ) j.next();

				fragment = ( keys.containsKey( propertyName ) ) ? ( String ) keys.get( propertyName )
					 : "${" + propertyName + '}';
			}
			sbuf.append( fragment );
		}

		return sbuf.toString();
	}

	/**
	 * Parses a string containing <code>${xxx}</code> style property references
	 * into two lists. The first list is a collection of text fragments, while the
	 * other is a set of string property names. <code>null</code> entries in the
	 * first list indicate a property reference from the second list. NOTE: This
	 * method was taken directly from Ant's source code
	 * ({@link org.apache.tools.ant.ProjectHelper}) with the BuildException throwing
	 * removed.
	 *
	 * @param value         Text to parse. Must not be <code>null</code>.
	 * @param fragments     List to add text fragments to. Must not be <code>null</code>.
	 * @param propertyRefs  List to add property names to. Must not be <code>null</code>.
	 */
	public static void parsePropertyString( String value, List fragments, List propertyRefs )
	{
		int prev = 0;
		int pos;

		while( ( pos = value.indexOf( '$', prev ) ) >= 0 )
		{

			if( pos > 0 )
			{
				String fragment = value.substring( prev, pos );

				fragments.add( fragment );
			}

			if( pos == ( value.length() - 1 ) )
			{
				fragments.add( "$" );
				prev = pos + 1;
			}
			else if( value.charAt( pos + 1 ) != '{' )
			{
				fragments.add( value.substring( pos, pos + 1 ) );
				prev = pos + 1;
			}
			else
			{
				int endName = value.indexOf( '}', pos );

				if( endName < 0 )
				{
					// In Ant this is a BuildException condition as its an
					// incomplete property reference. Here we'll leave it
					// in the output string
					String fragment = value.substring( pos );

					fragments.add( fragment );
					continue;
				}

				String propertyName = value.substring( pos + 2, endName );

				fragments.add( null );
				propertyRefs.add( propertyName );
				prev = endName + 1;
			}
		}

		if( prev < value.length() )
		{
			String fragment = value.substring( prev );

			fragments.add( fragment );
		}
	}

	/**
	 * Gets the Primitive attribute of the XJavaDoc class
	 *
	 * @param name  Describe what the parameter does
	 * @return      The Primitive value
	 */
	static Primitive getPrimitive( String name )
	{
		return ( Primitive ) _primitiveClasses.get( name );
	}

	/**
	 * Gets the file the pe is contained in. Note: calling this method with a
	 * XProgramElement not from source (but from a binary or unknown class) will
	 * result in a ClassCastException, so don't do that. This method is only used
	 * for diagnostics in error reporting.
	 *
	 * @param pe  the program element we want the source for.
	 * @return    the file the program element is contained in.
	 */
	static AbstractFile getSourceFileFor( XProgramElement pe )
	{
		SourceClass containingClass = null;

		if( !( pe instanceof SourceClass ) )
		{
			// pe is a field, method or constructor. get the surrounding class
			containingClass = ( SourceClass ) pe.getContainingClass();
		}
		else
		{
			containingClass = ( SourceClass ) pe;
		}
		// in case the class is an inner class, loop until we have the outermost.
		while( containingClass.getContainingClass() != null )
		{
			containingClass = ( SourceClass ) containingClass.getContainingClass();
		}
		return containingClass.getFile();
	}

	/**
	 * Describe the method
	 *
	 * @param name  Describe the method parameter
	 * @param type  The feature to be added to the Primitive attribute
	 */
	private final void addPrimitive( String name, String type )
	{
		_primitiveClasses.put( name, new Primitive( this, name, type ) );
	}

	public Collection getSourceClasses( Predicate predicate )
	{
		return CollectionUtils.select( getSourceClasses(), predicate );
	}

	/**
	 * Returns all classes in the registered source sets, including inner classes
	 *
	 * @return   A Collection of XClass
	 */
	public Collection getSourceClasses()
	{
		if( _sourceSetSourceClassesWithInnerClasses.isEmpty() )
		{
			// Add the regular classes
			_sourceSetSourceClassesWithInnerClasses.addAll( getOuterSourceClasses() );

			// Add inner classes
			for( Iterator outers = getOuterSourceClasses().iterator(); outers.hasNext();  )
			{
				addInnerClassRecursive( (XClass) outers.next(), _sourceSetSourceClassesWithInnerClasses );
			}
		}
		return Collections.unmodifiableCollection( _sourceSetSourceClassesWithInnerClasses );
	}

	/**
	 * Returns the packages of the specified classes during parsing.
	 *
	 * @return   Describe the return value
	 */
	public Collection getSourcePackages()
	{
		Set packages = new TreeSet();
		Collection classes = getSourceClasses();

		for( Iterator i = classes.iterator(); i.hasNext();  )
		{
			packages.add( ((XClass)i.next()).getContainingPackage() );
		}

		return Collections.unmodifiableCollection( packages );
	}

	public Map getPropertyMap()
	{
		return Collections.unmodifiableMap( _properties );
	}

	/**
	 * Get the XClass corresponding to the qualifiedName. This can be a class from
	 * source, a precompiled class or a primitive. UnknownClass is never returned
	 * from this method, unless it has been previously instantiated. <b>IMPORTANT:
	 * </b> If the Java source can be located, an instance of SourceClass will be
	 * returned. -Even if that file was not among the files in the fileset or
	 * sourceset. <b>IMPORTANT: </b> If qualifiedName represents an inner class, an
	 * UnknownClass will be returned unless the enclousing "outer" class has been
	 * resolved first.
	 *
	 * @param qualifiedName  Fully qualified class name
	 * @return               The XClass value
	 */
	public XClass getXClass( String qualifiedName )
	{
		if( qualifiedName.equals( "" ) )
		{
			throw new IllegalStateException( "Classname can't be empty String" );
		}

		XClass result = null;
		Primitive primitive;
		SourceClass sourceClass;
		BinaryClass binaryClass;
		UnknownClass unknownClass;

		// first, check all caches
		if( ( primitive = getPrimitive( qualifiedName ) ) != null )
		{
			result = primitive;
		}
		else if( ( sourceClass = ( SourceClass ) _allSourceClasses.get( qualifiedName ) ) != null )
		{
			result = sourceClass;
		}
		else if( ( binaryClass = ( BinaryClass ) _binaryClasses.get( qualifiedName ) ) != null )
		{
			result = binaryClass;
		}
		else if( ( unknownClass = ( UnknownClass ) _unknownClasses.get( qualifiedName ) ) != null )
		{
			result = unknownClass;
		}
		else
		{
			// Let's try to read the class from source
			if( sourceExists( qualifiedName ) )
			{
				// The source exists. Let's parse it.
				sourceClass = scanAndPut( qualifiedName );
				result = sourceClass;
			}
			else
			{
				// Couldn't find the class among the sources.
				// Try a BinaryClass
				Class clazz = getClass( qualifiedName );

				if( clazz != null )
				{
					binaryClass = new BinaryClass( this, clazz );
					_binaryClasses.put( qualifiedName, binaryClass );
					result = binaryClass;
				}
				else
				{
					// Binary didn't exist either. Return an UnknownClass
					result = new UnknownClass( this, qualifiedName );
					_unknownClasses.put( qualifiedName, result );
				}
			}
		}
		return result;
	}

	/**
	 * Returns the package. The package must be one of the packages of the sources.
	 * Other packages, such as java.lang are not available.
	 *
	 * @param packageName
	 * @return             an XPackage, or null if the packageName is not among the
	 *      sources.
	 */
	public XPackage getSourcePackage( String packageName )
	{
		// This is not optimal, but this method is primarily used for testing.
		for( Iterator i = getSourcePackages().iterator(); i.hasNext();  )
		{
			XPackage p = ( XPackage ) i.next();

			if( p.getName().equals( packageName ) )
			{
				return p;
			}
		}
		return null;
	}

	/**
	 * This method can be called prior to parsing so that all classes are parsed
	 * with AST (to make it possible to write the source back to disk)
	 *
	 * @param useNodeParser
	 */
	public void setUseNodeParser( boolean useNodeParser )
	{
		_useNodeParser = useNodeParser;
	}

	public void setPropertyMap( Map properties )
	{
		_properties.putAll( properties );
	}

	/**
	 * Resets the caches.
	 *
	 * @param resetTimeStamp  true if timestamps should be reset too.
	 */
	public void reset( boolean resetTimeStamp )
	{
		for( Iterator iterator =  _packages.values().iterator(); iterator.hasNext();  )
		{
			XPackage xPackage = (XPackage) iterator.next();

			for( Iterator i = xPackage.getClasses().iterator(); i.hasNext();  )
			{
				AbstractClass clazz = ( AbstractClass ) i.next();

				clazz.reset();
			}
		}
		_binaryClasses.clear();
		_unknownClasses.clear();
		_packages.clear();
		_sourceSets.clear();
		_sourceSetSourceClasses.clear();
		_sourceSetClassNames.clear();
		_allSourceClasses.clear();
		_sourceSetSourceClassesWithInnerClasses.clear();

		_logMessages.clear();
		_properties.clear();
		_abstractFileClasses.clear();

		//_primitiveClasses = null;

		//AbstractProgramElement.NULL_XDOC = null;

		// if we start new life, we can as well get new birth certificate,
		// so classes saved in previous life can be loaded again without
		// hating them :)
		if( resetTimeStamp )
		{
			_birthday = System.currentTimeMillis();
		}
	}

	/**
	 * Prints the log messages encountered during parsing
	 *
	 * @param out
	 * @param level
	 */
	public void printLogMessages( PrintStream out, int level )
	{
		boolean printedHeader = false;

		for( Iterator i = _logMessages.iterator(); i.hasNext();  )
		{
			LogMessage m = ( LogMessage ) i.next();

			if( m._level == level )
			{
				if( !printedHeader )
				{
					if( level == ONE_OR_MORE_IMPORTED_PACKAGES )
					{
						// Could be an inner class too!!!
						out.println( "WARNING: Some classes refer to other classes that were not found among the sources or on the classpath." );
						out.println( "         (Perhaps the referred class doesn't exist? Hasn't been generated yet?)" );
						out.println( "         The referring classes do not import any fully qualified classes matching these classes." );
						out.println( "         Since at least one package is imported, it is impossible for xjavadoc to figure out" );
						out.println( "         what package the referred classes belong to. The classes are:" );
					}
					else
					{
						out.println( "INFO:    Some classes refer to other classes that were not found among the sources or on the classpath." );
						out.println( "         (Perhaps the referred class doesn't exist? Hasn't been generated yet?)" );
						out.println( "         The referring classes do not import any fully qualified classes matching these classes." );
						out.println( "         However, since no packages are imported, xjavadoc has assumed that the referred classes" );
						out.println( "         belong to the same package as the referring class. The classes are:" );
					}
					printedHeader = true;
				}
				out.println( m._sourceClass.getFile().getPath() + " --> " + m._unqualifiedClassName + " qualified to " + m._unknownClass.getQualifiedName() );
			}
		}
	}

	/**
	 * Adds a new set of java sources to be parsed.
	 *
	 * @param sourceSet  a set of java sources.
	 */
	public void addSourceSet( SourceSet sourceSet )
	{
		_sourceSets.add( sourceSet );
		for( int j = 0; j < sourceSet.getSize(); j++ )
		{
			String qualifiedName = sourceSet.getQualifiedName( j );

			if( _sourceSetClassNames.contains( qualifiedName ) )
			{
				String msg = "The class \"" + qualifiedName + "\" occurs in more than one fileset. That's illegal.";

				System.err.println( msg );
			}
			_sourceSetClassNames.add( qualifiedName );
		}
	}

	public void addAbstractFile( String qualifiedName, AbstractFile file )
	{
		_abstractFileClasses.put( qualifiedName, file );

		if( _sourceSetClassNames.contains( qualifiedName ) )
		{
			String msg = "The class \"" + qualifiedName + "\" occurs in more than one fileset. That's illegal.";

			System.err.println( msg );
		}
		_sourceSetClassNames.add( qualifiedName );
	}

	/**
	 * Describe what the method does
	 *
	 * @param className                qualified name of class
	 * @param tagName                  tag name
	 * @param parameterName            parameter name
	 * @param parameterValue           new parameter value
	 * @param tagIndex                 index of tag (??)
	 * @param methodNameWithSignature  method name followed by signature. no
	 *      spaces. Ex:<br>
	 *      <code>doIt(java.lang.String,int)</code>
	 * @return                         the class corresponding to the className
	 * @exception XJavaDocException    If the tag for some reason couldn't be
	 *      updated
	 */
	public XClass updateMethodTag(
		String className,
		String methodNameWithSignature,
		String tagName,
		String parameterName,
		String parameterValue,
		int tagIndex
		 ) throws XJavaDocException
	{
		XClass clazz = getXClass( className );
		XMethod method = clazz.getMethod( methodNameWithSignature );

		XDoc doc = method.getDoc();

		doc.updateTagValue( tagName, parameterName, parameterValue, tagIndex );

		return clazz;
	}

	/**
	 * Describe what the method does
	 *
	 * @param className              Describe what the parameter does
	 * @param tagName                Describe what the parameter does
	 * @param parameterName          Describe what the parameter does
	 * @param parameterValue         Describe what the parameter does
	 * @param tagIndex               Describe what the parameter does
	 * @return                       Describe the return value
	 * @exception XJavaDocException  Describe the exception
	 */
	public XClass updateClassTag(
		String className,
		String tagName,
		String parameterName,
		String parameterValue,
		int tagIndex
		 ) throws XJavaDocException
	{
		XClass clazz = getXClass( className );
		XDoc doc = clazz.getDoc();

		doc.updateTagValue( tagName, parameterName, parameterValue, tagIndex );
		return clazz;
	}

	public String dereferenceProperties( String value )
	{

		return replaceProperties( value, _properties );
	}

	/**
	 * @param qualifiedClassName
	 * @return                    true if the class exists, either as source or
	 *      binary
	 */
	final boolean classExists( final String qualifiedClassName )
	{
		// See if we have the source
		if( sourceExists( qualifiedClassName ) )
		{
			return true;
		}
		// See if we kand find the class (binary)
		else if( getClass( qualifiedClassName ) != null )
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	void logMessage( SourceClass clazz, UnknownClass unknownClass, String unqualifiedClassName, int level )
	{
		_logMessages.add( new LogMessage( clazz, unknownClass, unqualifiedClassName, level ) );
	}

	/**
	 * Describe the method
	 *
	 * @param packageName  Describe the method parameter
	 * @return             Describe the return value
	 */
	XPackage addPackageMaybe( String packageName )
	{
		XPackage result = ( XPackage ) _packages.get( packageName );

		if( result == null )
		{
			// The package doesn't exist yet. Add it then
			result = new XPackage( packageName );
			_packages.put( packageName, result );
		}
		return result;
	}

	/**
	 * Adds a source class to the cache. This method is also called from JavaParser
	 * when parsing inner classes.
	 *
	 * @param sourceClass  Describe the method parameter
	 */
	void addSourceClass( SourceClass sourceClass )
	{
		_allSourceClasses.put( sourceClass.getQualifiedName(), sourceClass );

		// Also add it to _sourceSetSourceClasses if it was among the source sets
		// or if it is an "extra" class (this is due to XJD-8)
		if( _sourceSetClassNames.contains( sourceClass.getQualifiedName() ) || sourceClass.isExtraClass() )
		{
			_sourceSetSourceClasses.put( sourceClass.getQualifiedName(), sourceClass );
		}
	}

	/**
	 * Returns the Class with the given name, or null if unknown.
	 *
	 * @param qualifiedName  Describe what the parameter does
	 * @return               The Class value
	 */
	private final Class getClass( String qualifiedName )
	{
		try
		{
			return Class.forName( qualifiedName, false, getClass().getClassLoader() );
		}
		catch( Throwable e )
		{
			// e can be LinkageError, ClassNotFoundException or ExceptionInInitializerError
			// We don't care what we get. If the forName fails, we don't have a class to return
			return null;
		}
	}

	/**
	 * Returns all classes in the registered source sets
	 *
	 * @return   A Collection of XClass
	 */
	private Collection getOuterSourceClasses()
	{
		if( _sourceSetSourceClasses.isEmpty() )
		{
			for( Iterator i = _sourceSetClassNames.iterator(); i.hasNext();  )
			{
				String qualifiedName = ( String ) i.next();

				/*
				 * This will result in the class being added to
				 * _sourceSetSourceClasses AND _allSourceClasses
				 */
				getXClass( qualifiedName );
			}

			for( Iterator iterator = _abstractFileClasses.keySet().iterator(); iterator.hasNext();  )
			{
				String fqcn = ( String ) iterator.next();

				getXClass( fqcn );
			}
		}

		//a new collection should be created, becuase we might be looping over classes and generatePerClass for each
		//one, now if a new class is discovered and registered (maybe we are analyzing a new class, maybe a template
		//is now interested in superclass of a class, maybe ...), we'll get a ConcurrentModificationException. To prevent
		//it we need to looping over a new collection (initiated from XJavaDoc mostly).
		Collection new_collection = new ArrayList( _sourceSetSourceClasses.values() );

		return Collections.unmodifiableCollection( new_collection );
	}

	/**
	 * Gets the SourceFile attribute of the XJavaDoc object
	 *
	 * @param qualifiedName  Describe what the parameter does
	 * @return               The SourceFile value
	 */
	private AbstractFile getSourceFile( String qualifiedName )
	{
		// loop over all SourceSets. If a source is found more than once -> bang!
		AbstractFile found = null;

		for( Iterator i = _sourceSets.iterator(); i.hasNext();  )
		{
			SourceSet sourceSet = ( SourceSet ) i.next();
			AbstractFile javaFile = sourceSet.getSourceFile( qualifiedName );

			if( javaFile != null )
			{
				// isn't this an impossible situation?  Have a look at addSourceSet - we check
				// there to ensure that no classes are added twice.....
//				if( found != null && !found.getAbsolutePath().equals( javaFile.getAbsolutePath() ) )
//				{
//					throw new IllegalStateException( "Ambiguous sources for " + qualifiedName + " : " + found.getAbsolutePath() + " or " + javaFile.getAbsolutePath() );
//				}
				found = javaFile;
			}
		}
		return found;
	}

	/**
	 * Recursively adds inner classes to a collection
	 *
	 * @param outer  The feature to be added to the InnerClassRecursive attribute
	 * @param c      The feature to be added to the InnerClassRecursive attribute
	 */
	private void addInnerClassRecursive( XClass outer, Collection c )
	{
		for( Iterator inners = outer.getInnerClasses().iterator(); inners.hasNext();  )
		{
			XClass inner = (XClass) inners.next();

			c.add( inner );
			addInnerClassRecursive( inner, c );
		}
	}

	/**
	 * Checks is the source exists
	 *
	 * @param qualifiedName  the class to check for
	 * @return               true if source exists.
	 */
	private boolean sourceExists( String qualifiedName )
	{
		/*
		 * When used with XDoclet, some classes might be resolved by xjavadoc
		 * after they are generated by XDoclet.
		 * (An example is e.g. a primary key
		 * class that doesn't exist before XDoclet is run, and which might be
		 * referenced by some of the methods in the @tagged classes).
		 * We will pretend that any classes that didn't exist before xjavadoc started
		 * scanning sources (generated classes) don't exist. This is to avoid modifying
		 * collections that are being iterated over in parallel, as this will throw
		 * ConcurrentModificationException. (Aslak)
		 */
		AbstractFile sourceFile = getSourceFile( qualifiedName );

		if( sourceFile != null )
		{
			if( sourceFile.lastModified() > _birthday )
			{
				// The source appeared after xjavadoc was reset. Pretend it doesn't exist.
				System.out.println( "XJavaDoc Ignoring class " + qualifiedName + " in " + sourceFile.getPath() + ". It was generated (" + new Date( sourceFile.lastModified() ) + ") after XJavaDoc's timestamp was reset (" + new Date( _birthday ) + ")" );
				return false;
			}
		}

		boolean sourceFileExists = sourceFile != null;

		if( !sourceFileExists )
		{
			sourceFileExists = _abstractFileClasses.containsKey( qualifiedName );
		}

		return sourceFileExists;
	}

	/**
	 * Scan's a class and puts it in the cache.
	 *
	 * @param qualifiedName  Describe what the parameter does
	 * @return               Describe the return value
	 */
	private SourceClass scanAndPut( String qualifiedName )
	{
		AbstractFile sourceFile = getSourceFile( qualifiedName );

		sourceFile = sourceFile != null ? sourceFile : ( AbstractFile ) _abstractFileClasses.get( qualifiedName );

		if( sourceFile == null )
		{
			throw new IllegalStateException( "No source found for " + qualifiedName );
		}

		SourceClass sourceClass = new SourceClass( this, sourceFile, _useNodeParser, _tagFactory ,_encoding);

		// now that the entire file is parsed, validate the tags.
		if( _tagFactory.isValidating() )
		{
			sourceClass.validateTags();
		}

//		addSourceClass( sourceClass );

		return sourceClass;
	}

    public XTagFactory getTagFactory() {
        return _tagFactory;
    }

    /**
     * Registers tags.
     *
     * @param classpath where tags are found.
     */
    public void registerTags( String classpath ) {
        new TagIntrospector().registerTags( classpath, getTagFactory() );
    }

	public final static class NoInnerClassesPredicate implements Predicate
	{
		public boolean evaluate( Object o )
		{
			XClass clazz = ( XClass ) o;

			return !clazz.isInner();
		}
	}

	class LogMessage
	{
		public final SourceClass _sourceClass;
		public final UnknownClass _unknownClass;
		public final String _unqualifiedClassName;
		public final int  _level;
		LogMessage( SourceClass sourceClass, UnknownClass unknownClass, String unqualifiedClassName, int level )
		{
			_sourceClass = sourceClass;
			_unknownClass = unknownClass;
			_unqualifiedClassName = unqualifiedClassName;
			_level = level;
		}
	}

	/**
	 * Getter for source file charset.
	 * @return encoding
	 */
	public String getEncoding() {
		return _encoding;
	}

	/**
	 * Setter for source file charset.
	 * @param string encoding
	 */
	public void setEncoding(String encoding) {
		_encoding = encoding;
	}

	/**
	 * Getter for generated file charset.
	 * @return encoding
	 */
	public String getDocEncoding() {
		return _docEncoding;
	}

	/**
	 * Setter for generated file charset.
	 * @param string encoding 
	 */
	public void setDocEncoding(String docencoding) {
		_docEncoding = docencoding;
	}

}
