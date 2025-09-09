package xyz.dkos.sharploader.agent;

import xyz.dkos.sharploader.agent.loader.ClassTransformer;
import xyz.dkos.sharploader.agent.loader.CustomClassLoader;

import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import static xyz.dkos.sharploader.agent.NativeMethods.notifyExit;

public class WrappedMain {

    private static Map<String, String> cachedEnvironment = null;

    static {
        try {
            cachedEnvironment = System.getenv();
        } catch (Exception e) {
            System.err.println("Failed to get environment variables: " + e.getMessage());
        }
    }

    // public static volatile boolean switched = false;

    public static void main(String[] args) {
        /*
        if (!switched){
            System.out.println("[+] (Main) Hello, World!");
            System.out.println("[&] (Main) Switching Thread...");
            Thread mainThread = new Thread(() -> {
                switched = true;
                main(args);
            });
            mainThread.setName("Switched Main Thread");
            mainThread.start();
            return;
        }else{
            System.out.println("[&] (Main) Thread Switched...");
        }
        */

        System.out.println("(Main) Wrapped main started.");
        while (!Main.finished) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("(Main) Wrapped Main initialization interrupted.");
                return;
            }
        }

        Logger.info("(Main) Current Thread Id: " + Thread.currentThread().threadId());
        Logger.info("(Main) Current Thread Name: " + Thread.currentThread().getName());
        Logger.info("(Main) Class Path: " + System.getProperty("java.class.path"));
        Logger.info("(Main) Main Length: " + args.length);

        CustomClassLoader classLoader = new CustomClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        Main.premainInst.addTransformer(new ClassTransformer());
        Logger.info("(Main) Custom classloader created.");

        Class<?>[] loadedClasses = Main.premainInst.getAllLoadedClasses();
        for (Class<?> clazz : loadedClasses) {
            if ("java.io.PrintStream".equals(clazz.getName())) {
                try {
                    Main.premainInst.retransformClasses(clazz);
                    Logger.info("(Main) Print Stream class retransformed.");
                } catch (UnmodifiableClassException e) {
                    Logger.warn("(Main) Print Stream class not retransformed: " + e.getMessage());
                    e.printStackTrace();
                }
                break;
            }
        }

        String mainClassName = System.getenv("MAIN"); // cachedEnvironment.get("MAIN");
        Logger.info("(Main) Main Environment: " + mainClassName);
        if (mainClassName == null || mainClassName.trim().isEmpty()) {
            Logger.error("(Main) Environment variable MAIN is not set or empty.");
            System.exit(-255);
        }

        if (mainClassName.contains("/")) {
            mainClassName = mainClassName.replace('/', '.');
            Logger.info("(Main) Converted MAIN class name to: " + mainClassName);
        }

        Logger.info("(Main) Main class: " + mainClassName);

        try {
            Class<?> mainClass;
            try {
                mainClass = classLoader.loadClass(mainClassName);
                Logger.info("(Main) Loaded main class with custom classloader");
            } catch (ClassNotFoundException e) {
                try {
                    Logger.info("(Main) Custom classloader failed, trying system classloader");
                    mainClass = Class.forName(mainClassName);
                    Logger.info("(Main) Loaded main class with system classloader");
                }catch (ClassNotFoundException e2) {
                    Logger.error("(Main) System classloader failed");
                    return;
                }
            }

            Method mainMethod = mainClass.getMethod("main", String[].class);
            Logger.info("(Main) Found main method, invoking...");

            mainMethod.invoke(null, (Object) args);
            Logger.info("(Main) Main method completed successfully");
            // System.exit(0);
            return;
        } catch (Exception e) {
            Logger.error("(Main) Failed to start main class: " + mainClassName);
            Logger.error("(Main) Exception: " + e.getClass().getName() + ": " + e.getMessage());

            if (e.getCause() != null) {
                Logger.error("(Main) Caused by: " + e.getCause().getClass().getName() + ": " + e.getCause().getMessage());
                e.getCause().printStackTrace();
            }

            e.printStackTrace();
            System.exit(-255);
        } finally {
            notifyExit(0);
        }
    }
}
