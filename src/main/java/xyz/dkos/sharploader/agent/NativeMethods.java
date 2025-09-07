package xyz.dkos.sharploader.agent;

public class NativeMethods {
    public static native void notifyExit(int exitCode);

    public static native boolean shouldModifyClass(String className);
    public static native byte[] modifyClassFile(String className, byte[] classfileBuffer);
}
