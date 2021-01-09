package com.jonnyzzz.mplay.agent;

import java.lang.instrument.Instrumentation;
import java.net.JarURLConnection;
import java.net.URLConnection;
import java.util.jar.JarFile;

/**
 * A small wrapper for agent that injects
 * it's own classes into the bootstrap classloader
 * to make sure our API classes are available for
 * every class in the application (not only for the AppClassLoader)
 *
 * This code is made in Java to minimize the dependencies
 * (and thus classes that may cache instances of wrong classloader)
 */
public class MPlayAgent {
    public static void premain(String arguments, Instrumentation instrumentation) {
        try {
            premainImpl(arguments, instrumentation);
        } catch (Throwable t) {
            System.err.println("\n\nFailed to configure MPlay Agent:");
            t.printStackTrace(System.err);
        }
    }

    private static void premainImpl(String arguments, Instrumentation instrumentation) throws Exception {
        //it may turn out one uses non-application classloader inside the application
        //to deal with classes. This bootstrap is used to make sure all agent classes
        //are available from the bootstrap classloader
        var jarFile = resolveAgentJarFile();
        instrumentation.appendToBootstrapClassLoaderSearch(jarFile);

        //loading class dynamically to make sure it will be resolved from the right classloader
        Class.forName("com.jonnyzzz.mplay.agent.MPlayAgentImpl")
                .getMethod("premain", String.class, Instrumentation.class)
                .invoke(null, arguments, instrumentation);
    }

    private static JarFile resolveAgentJarFile() {
        var javaClass = MPlayAgent.class;
        var resourceName = javaClass.getName().replace(".", "/") + ".class";
        var url = javaClass.getClassLoader().getResource(resourceName);

        if (url == null) {
            throw new RuntimeException("Failed to find $resourceName in the classloader");
        }

        URLConnection con = null;
        try {
            con = url.openConnection();
            if (con instanceof JarURLConnection) {
                return ((JarURLConnection) con).getJarFile();
            }
        } catch (Throwable ignore) {
            //nop
        }

        throw new RuntimeException(
                "The code assumes the MPlay agent classes are in JAR and " +
                        "loaded via URLClassloader, but was " + con
        );
    }
}
