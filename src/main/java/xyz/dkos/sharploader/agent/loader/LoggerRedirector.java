package xyz.dkos.sharploader.agent.loader;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;

public class LoggerRedirector {

    public static boolean shouldModifyClass(String name) {
        return "java/io/PrintStream".equals(name) || "java.io.PrintStream".equals(name);
    }

    public static byte[] modifyClassFile(String name, byte[] classData) {
        if (!shouldModifyClass(name)) return classData;

        try {
            ClassReader cr = new ClassReader(classData);
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            ClassVisitor cv = new PrintStreamVisitor(cw);
            cr.accept(cv, ClassReader.EXPAND_FRAMES);
            return cw.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return classData; // Return original if modification fails
        }
    }

    public static class PrintStreamVisitor extends ClassVisitor {
        public PrintStreamVisitor(ClassVisitor cv) {
            super(Opcodes.ASM9, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            if (mv == null) return null;

            // Handle various print methods
            if ("print".equals(name) || "println".equals(name)) {
                return new RedirectMethodVisitor(mv, access, name, desc);
            }
            return mv;
        }
    }

    public static class RedirectMethodVisitor extends AdviceAdapter {
        private final String methodName;
        private final String descriptor;

        public RedirectMethodVisitor(MethodVisitor mv, int access, String name, String desc) {
            super(Opcodes.ASM9, mv, access, name, desc);
            this.methodName = name;
            this.descriptor = desc;
        }

        @Override
        protected void onMethodEnter() {
            // Check if this PrintStream instance is System.out
            Label skipRedirect = new Label();

            // Load 'this' (the PrintStream instance)
            loadThis();
            // Load System.out
            visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            // Compare if this == System.out
            visitJumpInsn(IF_ACMPNE, skipRedirect);

            // If this is System.out, redirect to our logger
            handleRedirection();

            // Return early to skip original method
            visitInsn(RETURN);

            // Label for non-System.out PrintStreams
            visitLabel(skipRedirect);
        }

        private void handleRedirection() {
            // Handle different parameter types
            if ("(Ljava/lang/String;)V".equals(descriptor)) {
                // For String parameter
                loadArg(0); // Load the string argument
                visitMethodInsn(INVOKESTATIC, "xyz/dkos/sharploader/agent/Logger", "standard", "(Ljava/lang/String;)V", false);
            } else if ("(Ljava/lang/Object;)V".equals(descriptor)) {
                // For Object parameter
                loadArg(0);
                visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;", false);
                visitMethodInsn(INVOKESTATIC, "xyz/dkos/sharploader/agent/Logger", "standard", "(Ljava/lang/String;)V", false);
            } else if ("(I)V".equals(descriptor)) {
                // For int parameter
                loadArg(0);
                visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(I)Ljava/lang/String;", false);
                visitMethodInsn(INVOKESTATIC, "xyz/dkos/sharploader/agent/Logger", "standard", "(Ljava/lang/String;)V", false);
            } else if ("(Z)V".equals(descriptor)) {
                // For boolean parameter
                loadArg(0);
                visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(Z)Ljava/lang/String;", false);
                visitMethodInsn(INVOKESTATIC, "xyz/dkos/sharploader/agent/Logger", "standard", "(Ljava/lang/String;)V", false);
            } else if ("(C)V".equals(descriptor)) {
                // For char parameter
                loadArg(0);
                visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(C)Ljava/lang/String;", false);
                visitMethodInsn(INVOKESTATIC, "xyz/dkos/sharploader/agent/Logger", "standard", "(Ljava/lang/String;)V", false);
            } else if ("(D)V".equals(descriptor)) {
                // For double parameter
                loadArg(0);
                visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(D)Ljava/lang/String;", false);
                visitMethodInsn(INVOKESTATIC, "xyz/dkos/sharploader/agent/Logger", "standard", "(Ljava/lang/String;)V", false);
            } else if ("(F)V".equals(descriptor)) {
                // For float parameter
                loadArg(0);
                visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(F)Ljava/lang/String;", false);
                visitMethodInsn(INVOKESTATIC, "xyz/dkos/sharploader/agent/Logger", "standard", "(Ljava/lang/String;)V", false);
            } else if ("(J)V".equals(descriptor)) {
                // For long parameter
                loadArg(0);
                visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(J)Ljava/lang/String;", false);
                visitMethodInsn(INVOKESTATIC, "xyz/dkos/sharploader/agent/Logger", "standard", "(Ljava/lang/String;)V", false);
            } else if ("([C)V".equals(descriptor)) {
                // For char array parameter
                loadArg(0);
                visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "([C)Ljava/lang/String;", false);
                visitMethodInsn(INVOKESTATIC, "xyz/dkos/sharploader/agent/Logger", "standard", "(Ljava/lang/String;)V", false);
            } else if ("()V".equals(descriptor) && "println".equals(methodName)) {
                // For println() with no arguments (just newline)
                visitLdcInsn("");
                visitMethodInsn(INVOKESTATIC, "xyz/dkos/sharploader/agent/Logger", "standard", "(Ljava/lang/String;)V", false);
            }
            // Add more parameter types as needed
        }
    }
}
