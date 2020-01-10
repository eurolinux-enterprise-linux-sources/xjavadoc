package <XDtPackage:packageName/>;

import junit.framework.*;
import xjavadoc.*;
import xjavadoc.filesystem.*;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * Automatically generated JUnit test for xjavadoc
 *
 * @author xjavadoc/xdoclet
 * @created 8. januar 2002
 * @todo-javadoc Write javadocs
 */
public class <XDtClass:className/>__GENERATED__Test extends TestCase {

	private XClass _testedClass;
	private final XJavaDoc _xJavaDoc = new XJavaDoc();

	private static final String tokenizeAndTrim( final String s ) {
		StringBuffer sb = new StringBuffer();
		StringTokenizer st = new StringTokenizer(s);
		while( st.hasMoreTokens() ) {
			sb.append( st.nextToken() ).append(" ");
		}
		return sb.toString().trim();
	}

	public <XDtClass:className/>__GENERATED__Test( String name ) {
		super( name );
	}

	public void setUp() throws Exception {
		// hardcoded to xjavadoc's own sources
		File dir = new File(System.getProperty("basedir"),"src");
		_xJavaDoc.reset(true);
		_xJavaDoc.addSourceSet(new FileSourceSet(dir, null));
		_testedClass = _xJavaDoc.getXClass( "<XDtClass:fullClassName/>" );
	}

	public void tearDown() {
	}

	public void testPackage() {
		assertEquals( "<XDtPackage:packageName/>", _testedClass.getContainingPackage().getName() );
	}

	public void testSuperclass() {
		XClass superclass = _testedClass.getSuperclass();
		String superclassName;
		if( superclass == null ) {
			superclassName = "java.lang.Object";
		} else {
			superclassName = superclass.getQualifiedName();
		}

		assertEquals( "<XDtClass:fullSuperclassName/>", superclassName );
	}

	public void testInterfaces() {
		// not implemented in xdoclet yet
	}

	public void testFields() {

		// Sort the fields
		Collection fields = _testedClass.getFields();
		ArrayList sortedFields = new ArrayList();
		sortedFields.addAll(fields);
		Collections.sort(sortedFields);
		Iterator fieldIterator = sortedFields.iterator();

		XField field = null;
	    <XDtField:forAllFields superclasses="false" sort="true">
		// test if field type is the same
		field = (XField) fieldIterator.next();
		assertEquals( "<XDtField:fieldType/>", field.getType().getQualifiedName() + field.getDimensionAsString());
		// test if field name is the same
		assertEquals( "<XDtField:fieldName/>", field.getName() );
	    </XDtField:forAllFields>
	}

	public void testMethods() {

		// Sort the methods
		Collection methods = _testedClass.getMethods();
		ArrayList sortedMethods = new ArrayList();
		sortedMethods.addAll(methods);
		Collections.sort(sortedMethods);
		Iterator methodIterator = sortedMethods.iterator();
		XMethod method = null;

		Iterator parameters = null;
		XParameter parameter = null;

		Iterator paramTags = null;
		XTag paramTag = null;

	    <XDtMethod:forAllMethods superclasses="false" sort="true">
	    	method = (XMethod) methodIterator.next();
		// test if return type is the same
		assertEquals( "<XDtMethod:methodType/>", method.getReturnType().getType().getQualifiedName() + method.getReturnType().getDimensionAsString());

		// test if method name is the same
		assertEquals( "<XDtMethod:methodName/>", method.getName() );

		// test if parameters are the same
		parameters = method.getParameters().iterator();
	      <XDtParameter:forAllMethodParams>
	        parameter = (XParameter) parameters.next();
		assertEquals( "<XDtParameter:methodParamName/>", parameter.getName() );
		assertEquals( "<XDtParameter:methodParamType/>", parameter.getType().getQualifiedName() + parameter.getDimensionAsString());
	      </XDtParameter:forAllMethodParams>

		// test if doc is the same
		paramTags = method.getDoc().getTags("param",true).iterator();
	      <XDtMethod:forAllMethodTags tagName="param">
	        paramTag = (XTag) paramTags.next();
		assertEquals( tokenizeAndTrim("<XDtMethod:methodTagValue tagName="param"/>"), paramTag.getValue() );
	      </XDtMethod:forAllMethodTags>

	    </XDtMethod:forAllMethods>
	}
}