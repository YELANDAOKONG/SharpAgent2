package xyz.dkos.sharploader.agent;

public class Logger {
    // Native method declarations
    public static native void standard(String message);
    public static native void all(String message);
    public static native void trace(String message);
    public static native void debug(String message);
    public static native void info(String message);
    public static native void warn(String message);
    public static native void error(String message);
    public static native void fatal(String message);
    public static native void off(String message);

    public static void test(){
        Logger.standard("(Test) Hello, World!");
        Logger.all("(Test) Hello, World! ");
        Logger.trace("(Test) Hello, World! ");
        Logger.debug("(Test) Hello, World! ");
        Logger.info("(Test) Hello, World! ");
        Logger.warn("(Test) Hello, World! ");
        Logger.error("(Test) Hello, World! ");
        Logger.fatal("(Test) Hello, World! ");
        Logger.off("(Test) Hello, World! ");
    }

    public static void test(long i){
        Logger.standard("(Test) Hello, World!");
        Logger.all("(Test) Hello, World! (" + i + ")");
        Logger.trace("(Test) Hello, World! (" + i + ")");
        Logger.debug("(Test) Hello, World! (" + i + ")");
        Logger.info("(Test) Hello, World! (" + i + ")");
        Logger.warn("(Test) Hello, World! (" + i + ")");
        Logger.error("(Test) Hello, World! (" + i + ")");
        Logger.fatal("(Test) Hello, World! (" + i + ")");
        Logger.off("(Test) Hello, World! (" + i + ")");
    }
}
