package xyz.dkos.sharploader.agent;

import java.lang.instrument.Instrumentation;

public class Main {

    public static void main(String[] args) {
        System.err.println("Agent cannot be run directly. Please use -javaagent option.");
        System.exit(1);
    }

    public static void agentmain(String args, Instrumentation inst) {
        System.err.println("Agent cannot be attached after startup.");
        System.exit(1);
    }

    public static volatile boolean initialized = false;

    public static void premain(String args, Instrumentation inst) {
        System.out.println("[+] Hello, World!");
        System.out.println("Loader Agent initializing...");

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

        CustomClassLoader classLoader = new CustomClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        // 注册类转换器
        inst.addTransformer(new ClassTransformer());
    }
}
