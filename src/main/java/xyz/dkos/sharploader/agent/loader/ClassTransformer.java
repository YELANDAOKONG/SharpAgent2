package xyz.dkos.sharploader.agent.loader;

import xyz.dkos.sharploader.agent.Logger;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import static xyz.dkos.sharploader.agent.NativeMethods.modifyClassFile;
import static xyz.dkos.sharploader.agent.NativeMethods.shouldModifyClass;

public class ClassTransformer implements ClassFileTransformer {

    public static boolean printLoadLog = false;
    public static boolean printModifyLog = true;

    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {
        if (className == null || classfileBuffer == null) {
            return null;
        }

        if (printLoadLog) {
            Logger.debug("(ClassTransformer) Class: " + className);
        }
        if (shouldModifyClass(className)) {
            if (printModifyLog){
                Logger.debug("(ClassTransformer) Modify: " + className);
            }
            return modifyClassFile(className, classfileBuffer);
        }

        String slashName = className.replace('.', '/');
        if (LoggerRedirector.shouldModifyClass(slashName)){
            return LoggerRedirector.modifyClassFile(slashName, classfileBuffer);
        }

        return null;
    }
}
