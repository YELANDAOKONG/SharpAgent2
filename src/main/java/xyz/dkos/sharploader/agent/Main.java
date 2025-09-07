package xyz.dkos.sharploader.agent;

import xyz.dkos.sharploader.agent.loader.ClassTransformer;
import xyz.dkos.sharploader.agent.loader.CustomClassLoader;

import java.lang.instrument.Instrumentation;

import static xyz.dkos.sharploader.agent.NativeMethods.notifyExit;

public class Main {

    public static void main(String[] args) {
        System.out.println("Agent cannot be run directly. Please use -javaagent option.");
        System.err.println("Agent cannot be run directly. Please use -javaagent option.");
        System.exit(1);
        // throw new RuntimeException("Agent cannot be run directly. Please use -javaagent option.");
        return;
    }

    public static void agentmain(String args, Instrumentation inst) {
        System.out.println("Agent cannot be attached after startup.");
        System.err.println("Agent cannot be attached after startup.");
        System.exit(1);
        // throw new RuntimeException("Agent cannot be attached after startup.");
        return;
    }

    public static volatile boolean switched = false;
    public static volatile boolean initialized = false;
    public static volatile boolean finished = false;

    public static volatile String premainArgs;
    public static volatile Instrumentation premainInst;

    public static void setInitialized(boolean initialized) {
        Main.initialized = initialized;
    }

    public static void premain(String args, Instrumentation inst) {
        if (!switched){
            System.out.println("[+] Hello, World!");
            System.out.println("[&] Switching Thread...");
            Thread mainThread = new Thread(() -> {
                switched = true;
                premain(args, inst);
            });
            mainThread.setName("Switched Premain Thread");
            mainThread.start();
            return;
        }else{
            System.out.println("[&] Thread Switched...");
        }

        premainArgs = args;
        premainInst = inst;

        System.out.println("[+] Hello, World!");
        System.out.println("Loader Agent initializing...");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            notifyExit(0);
        }));

        System.out.println("Waiting for Loader initialization...");
        while (!initialized) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Agent initialization interrupted.");
                return;
            }
        }

        System.out.println("Loader initialization completed.");
        Logger.info("Loader initialization completed.");

        // CustomClassLoader classLoader = new CustomClassLoader();
        // Thread.currentThread().setContextClassLoader(classLoader);
        // inst.addTransformer(new ClassTransformer());

        Logger.info("Agent method completed.");
        finished = true;
    }
}
