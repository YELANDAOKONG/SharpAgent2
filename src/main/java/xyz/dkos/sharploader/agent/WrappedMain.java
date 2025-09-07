package xyz.dkos.sharploader.agent;

import xyz.dkos.sharploader.agent.loader.ClassTransformer;
import xyz.dkos.sharploader.agent.loader.CustomClassLoader;

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
        Logger.info("Custom classloader created.");

        // TODO...


    }
}
