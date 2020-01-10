XJavaDoc is a sub-project of XDoclet to create a better and more 
extensible/flexible javadoc doclet engine.

--- B U I L D I N G   T H E   J A R---

XJavaDoc provides a dual build system, using either:
o http://ant.apache.org/
o http://maven.apache.org/

If you're building with Maven (recommended):

maven

If you're building with Ant (will eventually be phased out):

ant

--- U P L O A D I N G   T H E   J A R ---

The XJavaDoc binaries should always be available from
http://xdoclet.sourceforge.net/repository/xjavadoc/jars/
This makes it possible for other projects that depend on XJavaDoc
and that are built with Maven to download a specific XJavaDoc jar file.

In order to automatically build and upload the XJavaDoc jar here, just type:

maven jar:deploy

This requires that your public key be uploaded to your personal account on SF.
If you're on windows, you should also have PuTTY's pageant running with the
corresponding private key loaded.

See SF's SSH/PuTTY docs:
http://sourceforge.net/docman/display_doc.php?docid=6841&group_id=1
http://sourceforge.net/docman/display_doc.php?docid=761&group_id=1
http://sourceforge.net/docman/display_doc.php?docid=766&group_id=1
http://sourceforge.net/docman/display_doc.php?docid=2973&group_id=1

--- U P L O A D I N G   D O C S ---

The web site can be built with

maven xdoc (light version)
maven site (complete version)

--- U P L O A D I N G   D O C S ---

maven clover
maven site:deploy

This also requires the public/private keys to be properly set up as described above.