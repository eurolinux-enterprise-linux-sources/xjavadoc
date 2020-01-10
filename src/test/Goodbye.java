// no package

import java.io.*;
import java.rmi.*;
import java.rmi.Remote;

// import a class with an inner class. it should be resolved corresctly
import hanoi.Processor;

/**
 * Bla bla
 * yadda yadda
 * @foo:bar
 *beer="good"
 *         tea="bad"
 *
 */
class Goodbye extends Hello {

// 2 methods here, one of them overrides one from superclass

	private String gaga;

	/**
	 * This overrides a method from Hello
	 *
	 * @titi toto="tata"
	 */
	public InputStream getNonsense() {
		return null;
	}

	private void newMethod() {}

	public Processor.Next gotThis() {
		return null;
	}
}