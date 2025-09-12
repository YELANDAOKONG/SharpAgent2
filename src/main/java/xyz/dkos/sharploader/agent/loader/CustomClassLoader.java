package xyz.dkos.sharploader.agent.loader;

import xyz.dkos.sharploader.agent.Logger;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static xyz.dkos.sharploader.agent.NativeMethods.modifyClassFile;
import static xyz.dkos.sharploader.agent.NativeMethods.shouldModifyClass;

public class CustomClassLoader extends ClassLoader {

    public static boolean printLoadLog = false;
    public static boolean printModifyLog = true;

    public CustomClassLoader() {
        super(getSystemClassLoader());
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            String path = name.replace('.', '/') + ".class";
            InputStream is = getClass().getClassLoader().getResourceAsStream(path);
            if (is == null) {
                throw new ClassNotFoundException(name);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            byte[] classData = baos.toByteArray();

            if (printLoadLog) {
                Logger.debug("(ClassLoader) Class: " + name);
            }
            if (shouldModifyClass(name)) {
                if (printModifyLog) {
                    Logger.debug("(ClassLoader) Modify: " + name);
                }
                classData = modifyClassFile(name, classData);
            }

            String slashName = name.replace('.', '/');
            if (LoggerRedirector.shouldModifyClass(slashName)){
                classData = LoggerRedirector.modifyClassFile(slashName, classData);
            }

            return defineClass(name, classData, 0, classData.length);
        } catch (Exception e) {
            throw new ClassNotFoundException(name, e);
        }
    }
}
