/*
 * Copyright (c) 2001-2003 The XDoclet team
 * All rights reserved.
 */
package xjavadoc;

import xjavadoc.filesystem.AbstractFile;
import xjavadoc.filesystem.XJavadocFile;
import xjavadoc.filesystem.ReaderFile;

import java.util.*;
import java.io.*;

/**
 * This class represents a class for which the source code is available
 *XJavaDocFil
 * @author    Aslak Hellesøy
 * @created   3. januar 2002
 */
public final class SourceClass extends AbstractClass
{
	public static int  instanceCount = 0;

	private final Map  _qualifiedClasses = new HashMap();

	private final boolean _isExtraClass;

	private final List _tagsForValidation = new ArrayList();

	/**
	 * The root node of the AST
	 */
	private SimpleNode _compilationUnit;

	private Reader     _in = null;

	/**
	 * Keep a ref to the file in case of warning reporting
	 */
	private AbstractFile _sourceFile;

	//private JavaParser _parser;

	/**
	 * doe we nees saving?
	 */
	private boolean    _dirty;

	/**
	 * Constructor to use for inner classes.
	 *
	 * @param containingClass  The containing class;
	 */
	public SourceClass( SourceClass containingClass, XTagFactory tagFactory )
	{
		super( containingClass, tagFactory );
		setContainingPackage( containingClass.getContainingPackage().getName() );
		_isExtraClass = false;
	}

	/**
	 * Constructor to use for "extra" classes, that is, secondary classes that
	 * figure in the same source.
	 *
	 * @param mainClass  The containing class. Or rather the "main" class in the
	 *      source.
	 * @param dummy
	 */
	public SourceClass( SourceClass mainClass, int dummy, XTagFactory tagFactory )
	{
		super( mainClass.getXJavaDoc(), tagFactory );
		setContainingPackage( mainClass.getContainingPackage().getName() );
		_isExtraClass = true;
		_sourceFile = mainClass.getFile();
	}

	/**
	 * Constructor to use for outer classes
	 *
	 * @param sourceFile  The file containing the source
	 */
	public SourceClass( XJavaDoc xJavaDoc, File sourceFile, XTagFactory tagFactory )
	{
		this( xJavaDoc, new XJavadocFile( sourceFile ), false, tagFactory, null );
	}

	/**
	 * Constructor to use for outer classes
	 *
	 * @param sourceFile  The file containing the source
	 */
	public SourceClass( XJavaDoc xJavaDoc, Reader sourceFile, XTagFactory tagFactory )
	{
		this( xJavaDoc, new ReaderFile( sourceFile ), false, tagFactory, null);
	}

	/**
	 * @param sourceFile
	 * @param useNodeParser
	 */
	public SourceClass( XJavaDoc xJavaDoc, File sourceFile, boolean useNodeParser, XTagFactory tagFactory )
	{
		this( xJavaDoc, new XJavadocFile( sourceFile ), useNodeParser, tagFactory ,null );
	}

	/**
	 * Constructor to use for outer classes
	 *
	 * @param sourceFile     The file containing the source
	 * @param useNodeParser
	 */
	public SourceClass( XJavaDoc xJavaDoc, AbstractFile sourceFile, boolean useNodeParser, XTagFactory tagFactory ,String encoding)
	{
		super( xJavaDoc, tagFactory );
		if( sourceFile == null )
		{
			throw new IllegalArgumentException( "sourceFile can't be null for outer classes!" );
		}
		_sourceFile = sourceFile;

		try
		{
			_in = sourceFile.getReader(encoding);
			parse( useNodeParser );
		}
		catch( IOException e )
		{
			// That's tough. Shouldn't happen
			if(encoding==null)
			{
			throw new IllegalStateException( "Couldn't find " + sourceFile );
		}
			else
			{
				throw new IllegalStateException( "Invalid Encoding '"+encoding+"' or couldn't find '" + sourceFile +"'");
			}
		}

		instanceCount++;
		_dirty = false;
		_isExtraClass = false;
	}

	/**
	 * Describe what the method does
	 *
	 * @param qualifiedName  Describe what the parameter does
	 * @return               Describe the return value
	 */
	public static String getFileName( String qualifiedName )
	{
		return qualifiedName.replace( '.', File.separatorChar ) + ".java";
	}

	public boolean isExtraClass()
	{
		return _isExtraClass;
	}

	/**
	 * Returns "1", "2", etc., depending on how many inner classes we have.
	 *
	 * @return String containing number of next anonymous inner class
	 */
	public String getNextAnonymousClassName()
	{
		return String.valueOf( getInnerClasses().size() + 1 );
	}

	/**
	 * Gets the OuterClass attribute of the SourceClass object
	 *
	 * @return   The OuterClass value
	 */
	private boolean isOuterClass()
	{
		return _sourceFile != null;
	}

	/**
	 * Gets the Writeable attribute of the SourceClass object
	 *
	 * @return   The Writeable value
	 */
	public boolean isWriteable()
	{
		return _compilationUnit != null;
	}

	public SimpleNode getCompilationUnit()
	{
		return _compilationUnit;
	}

	/**
	 * Returns a reader for the source code.
	 *
	 * @return   a reader for the source code.
	 */
	public Reader getReader()
	{
		return _in;
	}

	public AbstractFile getFile()
	{
		return _sourceFile;
	}

	public boolean isPrimitive()
	{
		return false;
	}

	/**
	 * say this class is dirty and needs saving propagate to outer class ( if any )
	 */
	public void setDirty()
	{
		if( isInner() )
		{
			getContainingClass().setDirty();
		}
		else
		{
			_dirty = true;
		}
	}
	/**
	 * Called by JavaParser at the end of the parsing
	 *
	 * @param compilationUnit  The new CompilationUnit value
	 */
	public void setCompilationUnit( SimpleNode compilationUnit )
	{
		_compilationUnit = compilationUnit;
	}

	/**
	 * Called by XJavaDoc after the entire source is parsed, but only if validation
	 * is on.
	 *
	 * @throws TagValidationException
	 */
	public void validateTags() throws TagValidationException
	{
		// Validate the tags on the class level and on our members.
		for( Iterator i = _tagsForValidation.iterator(); i.hasNext();  )
		{
			XTag tag = ( XTag ) i.next();

			tag.validate();
		}

		// then validate tags in all our inner classes.
		for( Iterator i = getInnerClasses().iterator(); i.hasNext();  )
		{
			SourceClass inner = ( SourceClass ) i.next();

			inner.validateTags();
		}
	}

	public void addTagForValidation( DefaultXTag tag )
	{
		_tagsForValidation.add( tag );
	}

	public boolean saveNeeded()
	{
		return isWriteable() && _dirty;
	}

	/**
	 * Describe what the method does
	 *
	 * @return   Describe the return value
	 */
	public long lastModified()
	{
		if( isOuterClass() )
		{
			return _sourceFile.lastModified();
		}
		else
		{
			return getContainingClass().lastModified();
		}
	}

	/**
	 * Prints this class to a stream
	 *
	 * @param out  Describe what the parameter does
	 */
	public void print( Writer out )
	{
		updateDoc();
		if( !isWriteable() )
		{
			// parsed with simple parser
			throw new UnsupportedOperationException( "Can't save classes that are parsed with simpleparser" );
		}
		NodePrinter.print( _compilationUnit, out );
	}

	/**
	 * Saves the class at root dir rootDir. The actual java file is derived from
	 * tha package name. If no root dir is specified, save where it was loaded from
	 *
	 * @param rootDir       the root directory.
	 * @return              the relative fileName to which the file was saved.
	 * @throws IOException  if the file couldn't be saved
	 */
	public String save( File rootDir ) throws IOException
	{
		if( !isWriteable() )
		{
			throw new UnsupportedOperationException( "Can't save classes that aren't parsed in AST mode (do getXJavaDoc().setUseNodeParser(true) before parsing starts!)" );
		}
		if( getContainingClass() != null )
		{
			// inner class. can't save these.
			throw new UnsupportedOperationException( "Can't save inner classes" );
		}
		else if( rootDir != null )
		{
			String fileName = getFileName( getQualifiedName() );
			File javaFile = new File( rootDir, fileName );

			javaFile.getParentFile().mkdirs();
			FileWriter fwtr = new FileWriter( javaFile );
			print( fwtr );
			fwtr.flush();
			fwtr.close(); 
			return fileName;
		}
		else
		{
			// no root dir specified, save in place
			Writer outputStream = _sourceFile.getWriter();

			print( new PrintWriter( outputStream ) );
			outputStream.flush();
			outputStream.close();
			return _sourceFile.toString();
		}
	}

	/**
	 * Returns fully qualified name of a class. 1: check for "." 2: if "." it's
	 * already qualified 3: if no ".", must try with all imported packages or
	 * classes
	 *
	 * @param unqualifiedClassName  Describe what the parameter does
	 * @return                      Describe the return value
	 */
	public XClass qualify( final String unqualifiedClassName )
	{
		XClass result = null;

		result = ( XClass ) _qualifiedClasses.get( unqualifiedClassName );
		if( result == null )
		{

			if( getContainingClass() == null )
			{
				// If there are dots, consider it to be qualified or a reference to an inner class in one
				// of the imported classes.
				if( unqualifiedClassName.indexOf( '.' ) != -1 )
				{
					result = unqualifiedNameInImportedClassesInnerClasses( unqualifiedClassName );
					if( result == null )
					{
						// It wasn't a ref to an imported inner class. Consider it already qualified
						result = getXJavaDoc().getXClass( unqualifiedClassName );
					}
				}
				else
				{
					// There are no dots in the class name. It's a primitive or unqualified.
					Primitive primitive;

					if( ( primitive = XJavaDoc.getPrimitive( unqualifiedClassName ) ) != null )
					{
						result = primitive;
					}
					else
					{
						String qualifiedName;

						if( ( qualifiedName = unqualifiedNameInTheSameClassAsAnInnerClass( unqualifiedClassName ) ) != null )
						{
							result = getXJavaDoc().getXClass( qualifiedName );
						}
						else if( ( qualifiedName = unqualifiedNameInInnerClasses( unqualifiedClassName ) ) != null )
						{
							result = getXJavaDoc().getXClass( qualifiedName );
						}
						else if( ( qualifiedName = unqualifiedNameInJavaDotLang( unqualifiedClassName ) ) != null )
						{
							result = getXJavaDoc().getXClass( qualifiedName );
						}
						else if( ( qualifiedName = unqualifiedNameInImportedClasses( unqualifiedClassName ) ) != null )
						{
							result = getXJavaDoc().getXClass( qualifiedName );
						}
						else if( ( qualifiedName = unqualifiedNameInImportedPackages( unqualifiedClassName ) ) != null )
						{
							result = getXJavaDoc().getXClass( qualifiedName );
						}
						else if( ( qualifiedName = unqualifiedNameInTheSamePackage( unqualifiedClassName ) ) != null )
						{
							result = getXJavaDoc().getXClass( qualifiedName );
						}
                        else if( ( qualifiedName = unqualifiedNameInInnerClassesOfSuperClass( unqualifiedClassName ) ) != null )
                        {
                            result = getXJavaDoc().getXClass( qualifiedName );
                        }
                        else if( ( qualifiedName = unqualifiedNameInInnerInterface( unqualifiedClassName ) ) != null )
                        {
                            result = getXJavaDoc().getXClass( qualifiedName );
                        }
						else
						{
							String unknownClassName;

							if( getContainingPackage().getName().equals( "" ) )
							{
								unknownClassName = unqualifiedClassName;
							}
							else
							{
								unknownClassName = getContainingPackage().getName() + "." + unqualifiedClassName;
							}

							UnknownClass unknownClass = new UnknownClass( getXJavaDoc(), unknownClassName );

							/*
							 * We couldn't qualify the class. If there are no package import statements,
							 * we'll assume the class belongs to the same package as ourself.
							 */
							if( !hasImportedPackages() )
							{
								// No import foo.bar.* statements. Just add an informative message that we guessed
								getXJavaDoc().logMessage( this, unknownClass, unqualifiedClassName, XJavaDoc.NO_IMPORTED_PACKAGES );
							}
							else
							{

								// We can't decide. Add a warning that will be displayed in the end.
								getXJavaDoc().logMessage( this, unknownClass, unqualifiedClassName, XJavaDoc.ONE_OR_MORE_IMPORTED_PACKAGES );
							}
							result = unknownClass;
						}
					}
				}
			}
			else
			{
				result = getContainingAbstractClass().qualify( unqualifiedClassName );
			}
			_qualifiedClasses.put( unqualifiedClassName, result );
		}

		return result;
	}

	public void reset()
	{
		super.reset();

		_compilationUnit = null;
		_in = null;
		_sourceFile = null;
		_qualifiedClasses.clear();
	}

	private final String unqualifiedNameInImportedClasses( final String unqualifiedClassName )
	{
		if( !hasImportedClasses() )
		{
			return null;
		}

		final String suffix = "." + unqualifiedClassName;
		String candidate = null;

		for( Iterator i = getImportedClasses().iterator(); i.hasNext();  )
		{
			XClass clazz = ( XClass ) i.next();
			String qualifiedClassName = clazz.getQualifiedName();

			if( qualifiedClassName.endsWith( suffix ) )
			{
				// perform sanity check for ambiguous imports
				if( candidate != null && !candidate.equals( qualifiedClassName ) )
				{
					// ambiguous class import
					throw new IllegalStateException( "In class " + getQualifiedName() + ": Ambiguous class:" + unqualifiedClassName + ". Is it " + candidate + " or " + qualifiedClassName + "?" );
				}
				else
				{
					candidate = qualifiedClassName;
				}
			}
		}
		return candidate;
	}

	private final XClass unqualifiedNameInImportedClassesInnerClasses( final String unqualifiedClassName )
	{
		if( !hasImportedClasses() )
		{
			return null;
		}

		XClass candidate = null;

		for( Iterator i = getImportedClasses().iterator(); i.hasNext();  )
		{
			XClass clazz = ( XClass ) i.next();

			// See if it's among the inner classes.
			for( Iterator inners = clazz.getInnerClasses().iterator(); inners.hasNext();  )
			{
				XClass inner = ( XClass ) inners.next();
				boolean isAccessible = inner.isPublic();

				if( inner.getName().equals( unqualifiedClassName ) && isAccessible )
				{
					if( candidate != null )
					{
						// ambiguous class import
						throw new IllegalStateException( "In class " + getQualifiedName() + ": Ambiguous class:" + unqualifiedClassName + ". Is it " + candidate.getQualifiedName() + " or " + inner.getQualifiedName() + "?" );
					}
					else
					{
						candidate = inner;
					}
				}
			}
		}
		return candidate;
	}

	/**
	 * Describe what the method does
	 *
	 * @param unqualifiedClassName  Describe what the parameter does
	 * @return                      Describe the return value
	 */
	private final String unqualifiedNameInInnerClasses( final String unqualifiedClassName )
	{
		if( !hasInnerClasses() )
		{
			return null;
		}

		final String innerClassName = getQualifiedName() + '.' + unqualifiedClassName;

		String candidate = null;

		for( Iterator i = getInnerClasses().iterator(); i.hasNext();  )
		{
			XClass innerClass = ( XClass ) i.next();
			String qualifiedClassName = innerClass.getQualifiedName();

			if( innerClassName.equals( qualifiedClassName ) )
			{
				candidate = qualifiedClassName;
				break;
			}
		}
		return candidate;
	}

	/**
	 * Resolves Inner interfaces that exist in current class.
    *
    * This catches inner classes as well because isInterface()
    * does not indicate if it's an interface.
	 *
	 * @param unqualifiedClassName  Name of the class to resolve
	 * @return                      The qualified name of the inner class.
	 */
	private final String unqualifiedNameInInnerInterface( final String unqualifiedClassName )
	{
        String qualifiedClassName = getQualifiedName() + '$' + unqualifiedClassName;
        if (getXJavaDoc().classExists(qualifiedClassName)) {
//            // The isInterface() method is not implemented for source classes.
//            if (XJavaDoc.getInstance().getXClass(qualifiedClassName).isInterface()) {
//                return getQualifiedName() + '.' + unqualifiedClassName;
//            }
            return getQualifiedName() + '.' + unqualifiedClassName;
        }
		return null;
	}

	/**
	 * Resolves Inner classes that exist in the super class hierarchy.
	 *
	 * @param unqualifiedClassName  Name of the class to resolve
	 * @return                      The qualified name of the inner class.
	 */
	private final String unqualifiedNameInInnerClassesOfSuperClass( final String unqualifiedClassName )
	{
        XClass clazz = getXJavaDoc().getXClass(getQualifiedName());
        XClass superClazz = clazz.getSuperclass();
        while (superClazz != null && ! superClazz.getQualifiedName().equals("java.lang.Object")) {
		    String innerClassName = superClazz.getQualifiedName() + '.' + unqualifiedClassName;
            for( Iterator i = superClazz.getInnerClasses().iterator(); i.hasNext();  )
            {
                XClass innerClass = ( XClass ) i.next();
                String qualifiedClassName = innerClass.getQualifiedName();
                if( innerClassName.equals( qualifiedClassName ) )
                {
                    return qualifiedClassName;
                }
            }
            superClazz = superClazz.getSuperclass();
        }
		return null;
	}

	/**
	 * Describe what the method does
	 *
	 * @param unqualifiedClassName  Describe what the parameter does
	 * @return                      Describe the return value
	 */
	private final String unqualifiedNameInImportedPackages( final String unqualifiedClassName )
	{
		if( !hasImportedPackages() )
		{
			return null;
		}

		final String suffix = "." + unqualifiedClassName;
		String candidate = null;

		for( Iterator i = getImportedPackages().iterator(); i.hasNext();  )
		{
			String importedPackageName = ( ( XPackage ) i.next() ).getName();
			String qualifiedClassName = importedPackageName + suffix;

			if( getXJavaDoc().classExists( qualifiedClassName ) )
			{
				if( candidate != null && !candidate.equals( qualifiedClassName ) )
				{
					// ambiguous class import
					throw new IllegalStateException( "In class " + getQualifiedName() + ": Ambiguous class:" + unqualifiedClassName + ". Is it " + candidate + " or " + qualifiedClassName + "?" );
				}
				else
				{
					candidate = qualifiedClassName;
				}
			}
		}
		return candidate;
	}

	/**
	 * Returns the fully qualified class name if it's found in java.lang, otherwise
	 * null.
	 *
	 * @param unqualifiedClassName
	 * @return fully qualified class name, or null
	 */
	private final String unqualifiedNameInJavaDotLang( final String unqualifiedClassName )
	{
		String qualifiedClassName = "java.lang." + unqualifiedClassName;

		if( getXJavaDoc().classExists( qualifiedClassName ) )
		{
			return qualifiedClassName;
		}
		else
		{
			return null;
		}
	}

	/**
	 * Describe what the method does
	 *
	 * @param unqualifiedClassName  Describe what the parameter does
	 * @return                      Describe the return value
	 */
	private final String unqualifiedNameInTheSamePackage( final String unqualifiedClassName )
	{
		String qualifiedClassName;

		if( getContainingPackage().getName().equals( "" ) )
		{
			qualifiedClassName = unqualifiedClassName;
		}
		else
		{
			qualifiedClassName = getContainingPackage().getName() + '.' + unqualifiedClassName;
		}

		if( getXJavaDoc().classExists( qualifiedClassName ) )
		{
			return qualifiedClassName;
		}
		else
		{
			return null;
		}

	}

	private final String unqualifiedNameInTheSameClassAsAnInnerClass( final String unqualifiedClassName )
	{
		//containing class=com.p.A, inner-reference=B ->com.p.A.B
		String qualifiedClassName = getQualifiedName() + '.' + unqualifiedClassName;

		if( getXJavaDoc().classExists( qualifiedClassName ) )
			return qualifiedClassName;

		//containing class=com.p.A, inner-reference=A.B ->com.p.A.B
		if( getContainingPackage().getName().equals( "" ) )
		{
			qualifiedClassName = unqualifiedClassName;
		}
		else
		{
			qualifiedClassName = getContainingPackage().getName() + '.' + unqualifiedClassName;
		}

		if( getXJavaDoc().classExists( qualifiedClassName ) )
			return qualifiedClassName;

		return null;
	}

	/**
	 * Describe what the method does
	 *
	 * @param useNodeParser  Describe what the parameter does
	 */
	private void parse( boolean useNodeParser )
	{
		try
		{
			if( useNodeParser )
			{
				// We need a pool of parsers, because parsing one file
				// might kick away the parsing of another etc.
//				_nodeParser.populate( this );
				new NodeParser( getXJavaDoc(), getTagFactory() ).populate( this );
			}
			else
			{
//				_simpleParser.populate( this );
				new SimpleParser( getXJavaDoc(), getTagFactory() ).populate( this );
			}
		}
		catch( ParseException e )
		{
			// Source code is bad. Not according to grammar. User's fault.
			String cls = _sourceFile != null ? _sourceFile.toString() : getQualifiedName();

			System.err.println( "Error parsing " + cls + ':' + e.getMessage() );
		}
		catch( TokenMgrError e )
		{
			String cls = _sourceFile != null ? _sourceFile.toString() : getQualifiedName();

			System.err.println( "Error parsing " + cls + ':' + e.getMessage() );
		}
	}
}
