package xyz.dkos.sharploader.agent;

import xyz.dkos.sharploader.agent.loader.ClassTransformer;
import xyz.dkos.sharploader.agent.loader.CustomClassLoader;
import java.lang.reflect.Method;
import java.util.Arrays;

public class WrappedMain {

    public static volatile boolean switched = false;

    public static void main(String[] args) {
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

        CustomClassLoader classLoader = new CustomClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        Main.premainInst.addTransformer(new ClassTransformer());
        Logger.info("(Main) Custom classloader created.");

        String mainClassName = System.getenv("MAIN");
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
                Logger.info("(Main) System classloader failed, trying custom classloader");
                mainClass = classLoader.loadClass(mainClassName);
                Logger.info("(Main) Loaded main class with custom classloader");
            } catch (ClassNotFoundException e) {
                mainClass = Class.forName(mainClassName);
                Logger.info("(Main) Loaded main class with system classloader");
            }

            Method mainMethod = mainClass.getMethod("main", String[].class);
            Logger.info("(Main) Found main method, invoking...");

            mainMethod.invoke(null, (Object) args);
            Logger.info("(Main) Main method completed successfully");

        } catch (Exception e) {
            Logger.error("(Main) Failed to start main class: " + mainClassName);
            Logger.error("(Main) Exception: " + e.getClass().getName() + ": " + e.getMessage());

            if (e.getCause() != null) {
                Logger.error("(Main) Caused by: " + e.getCause().getClass().getName() + ": " + e.getCause().getMessage());
                e.getCause().printStackTrace();
            }

            e.printStackTrace();
            System.exit(-255);
        }
    }
}
