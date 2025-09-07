package xyz.dkos.sharploader.agent.loader;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import static xyz.dkos.sharploader.agent.NativeMethods.modifyClassFile;
import static xyz.dkos.sharploader.agent.NativeMethods.shouldModifyClass;

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
}
