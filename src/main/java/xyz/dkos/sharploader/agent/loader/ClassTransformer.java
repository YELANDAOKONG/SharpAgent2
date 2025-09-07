package xyz.dkos.sharploader.agent.loader;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class ClassTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {
        if (className == null || classfileBuffer == null) {
            return null;
        }

        if (shouldModifyClass(className)) {
            return modifyClassFile(className, classfileBuffer);
        }

        return null;
    }

    private static native boolean shouldModifyClass(String className);

    private static native byte[] modifyClassFile(String className, byte[] classfileBuffer);
}
