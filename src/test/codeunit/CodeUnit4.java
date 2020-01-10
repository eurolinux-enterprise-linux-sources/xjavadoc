package codeunit;

// take a look at CodeUnit3.java
import java.io.Serializable;

interface Bar extends Serializable {
	// an interface's methods are implicitly public, even if not declared as such
void blah(String s);
}
