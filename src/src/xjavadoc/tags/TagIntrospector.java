package xjavadoc.tags;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.jar.Manifest;
import java.util.jar.JarFile;
import java.util.jar.Attributes;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.IntrospectionException;

import xjavadoc.XTagFactory;
import xjavadoc.XTag;

/**
 * This class introspects the classpath and registers tags.
 *
 * @author Aslak Hellesøy
 * @version $Revision: 1.3 $
 */
public final class TagIntrospector {

    public void registerTags(String classpath, XTagFactory tagFactory ) {
        for( StringTokenizer st = new StringTokenizer(classpath, System.getProperty("path.separator") ); st.hasMoreTokens(); ) {
            File classpathElement = new File( st.nextToken() );
            if( classpathElement.exists() ) {
                List javaBeans = findJavaBeans( classpathElement );
                registerTags( javaBeans, tagFactory );
            } else {
                System.out.println( classpathElement.getAbsolutePath() + " was on classpath, but doesn't exist." );
            }
        }
    }

    private void registerTags(List javaBeans, XTagFactory tagFactory ) {
        for( Iterator i = javaBeans.iterator(); i.hasNext(); ) {
            Class javaBean = (Class) i.next();
            if( XTag.class.isAssignableFrom( javaBean ) ) {
                try {
                    BeanInfo beanInfo = Introspector.getBeanInfo( javaBean );

                    String tagName = beanInfo.getBeanDescriptor().getName();
                    tagFactory.registerTagClass( tagName, javaBean );
                } catch( IntrospectionException e ) {
                    System.out.println("No BeanInfo for " + javaBean.getName() );
                }
            } else {
                // System.out.println( javaBean.getName() + " isn't a xjavadoc.XTag class. Ignoring" );
            }
        }
    }


    /**
     * Returns a collection of classes that are Java Beans. The Java Bean
     * classes are found by looking at the MANIFEST.MF file.
     *
     * @param dirOrJar the directory of jar file containing the classes.
     * @return a Collection of {@link Class}.
     */
    private List findJavaBeans( File dirOrJar ) {
        List result = new ArrayList();

        try {
            ClassLoader classLoader = new URLClassLoader( new URL[] {dirOrJar.toURL()}, getClass().getClassLoader() );

            Manifest manifest = null;
            if (dirOrJar.isDirectory()) {
                try {
                    manifest = new Manifest(new FileInputStream(new File(dirOrJar, "META-INF/MANIFEST.MF")));
                } catch (IOException e) {
                    // Ignore. There was no Manifest here.
                }
            } else {
                try {
                    JarFile jarFile = new JarFile(dirOrJar);

                    manifest = jarFile.getManifest();
                } catch (IOException e) {
                    // Ignore. Wasn't a jar file.
                }
            }

            if (manifest != null) {
                // Now loop over all entries in the Manifest.
                for (Iterator entryNames = manifest.getEntries().keySet().iterator(); entryNames.hasNext();) {
                    String entryName = (String) entryNames.next();
                    // Is it a class?
                    if (entryName.endsWith(".class")) {
                        Attributes attributes = manifest.getAttributes(entryName);
                        // See if it's a java bean.
                        String javaBean = attributes.getValue("Java-Bean");

                        if ("true".equalsIgnoreCase(javaBean)) {
                            // OK. Get the BeanInfo.
                            String className = entryName.substring(0, entryName.length() - 6);

                            className = className.replace('/', '.');

                            // Load the class
                            try {
                                Class beanClass = classLoader.loadClass(className);
                                result.add( beanClass );
                            } catch (ClassNotFoundException e) {
                                String errorMessage = className
                                    + " was declared as a Java-Bean in the manifest, but the class was not found.";

                                e.printStackTrace();
                                throw new IllegalStateException(errorMessage);
                            }
                        }
                    }
                }
            }
            return result;
        } catch( MalformedURLException e ) {
            throw new IllegalStateException(e.getMessage());
        }
    }
}
