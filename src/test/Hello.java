// no package

import java.io.*;
import java.rmi.*;
import java.rmi.Remote;

/**
 * Bla bla
 * yadda yadda
 * @foo:bar
 *beer="good"
 *         tea="bad"
 *
 * @my:name this program is called ${name} guess why
 * @my:version version="${name} version is ${version}"
 *
 */
class Hello extends javax.swing.text.TextAction implements javax.swing.event.MouseInputListener, Remote, Serializable {

	/**
	 * This shouldn't be the first sentence.This one should be.
	 * Is everything OK?
	 */
	public void firstMethod()
	{
	}; // The spec says semicolon is illegal here, but javac accepts it, and xjavadoc will too.

   /**
    * Braba papa, barba mama, baraba brother, barba sister
    */
   private final String privateField = "barba papa";

   protected final String protectedField;
   public final String publicField;

 	 /**
	 * Blabla. Do you like Norwegian letters? Ê¯Â∆ÿ≈.
	 *
	 * @foo
	 */
	public Hello() {
	}

		/**
	 * Yadda yadda
	 *
	 * @bar
	 */
	Hello( File f ) {
	}

	protected Hello( String f ) {
	}
			// what can you do about thiis comment?

	/**
	 * This is getNonsense.
	 *
	 * @star:wars is="a crappy movie"
	 * but="I went to see it anyway"
	 *
	 * @empty:tag
	 */
	public InputStream getNonsense() {
		return null;
	}

	/**
	 * Mr. Jones
	 *
	 * @more testdata, bla bla
	 * @maybe this="is" only="testdata"
	 */
	protected void whatever( String[] s[], int i) {
		assert true;
		assert true:"blabla";
	}

    /**
	* What Ever
	    * @more howdy, bla bla
* @numbers one="two" three="four"
	  */
	private void whatever( String[] s, int i ) {
	}


	Long noComment(     ) {
	}

	public void setInner(InnerClass inner) {
	}

	public class InnerClass extends java.lang.Object {

	   private final String doodoo = "doodoo";

		private String justForFun() {
			return "justForFun";
		}
	}

    public void methodBlockInnerClass() {
        class MethodInnerClass extends Object
        {
            /**
            * What Ever
                * @more howdy, bla bla
        * @numbers one="two" three="four"
              */
            public void haha() {
                System.out.println("haha");
            }
        }
		MethodInnerClass methodInner = new MethodInnerClass();
        methodInner.haha();
	}
}

class OldFashioned {
   private String blah;

   public class InnerInOldFashioned {
      private String duh;
   }
}