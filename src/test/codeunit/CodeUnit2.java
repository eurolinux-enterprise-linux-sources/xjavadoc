package codeunit;

import java.awt.Component;

/**
 * This is test data for CodeUnit. This source is similar to the one you'll
 * find in CodeUnit1.java. The comments and formattings are different.
 * The APIs are the same,
 * but the ASTs are different. (this one has int hahahahah = 0; the other one doesn't).
 * These classes also have different import statements and different qualified class
 * names for the extended/implemented classes. The ASTs are still the same at this level,
 * and so is the API. Neither the API or AST comparison is able to detect differences
 * at this level (contents of AST nodes are not compared).
 *
 * Look at the CodeTest class, which is an example of how to use CodeUnit
 *
 * @author <a href="mailto:aslak.hellesoy at bekk.no">Aslak Helles&oslash;y</a>
 */
class CodeUnit extends Component implements java.io.Serializable {
static { int i; }

/**
* do foo
*/
void foo() {
int hahahahah = 0;
Class c = int[].class;
Class d = Object[].class;
t.new T();
((T)t).method();
this( (int) (r * 255), (int) (g * 255));
return "[i=" + (value) + "]";
int q = (int)+3;
int z = (int)4;
int y = (z)+5;
String s = (String) "ff";
String t = (s)+"blort";
}
}
