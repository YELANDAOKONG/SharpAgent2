package xyz.dkos.sharploader.agent.loader;

import org.objectweb.asm.*;

public class LoggerRedirector {
    public static boolean shouldModifyClass(String name) {
        return "java/io/PrintStream".equals(name);
    }

    public static byte[] modifyClassFile(String name, byte[] classData) {
        if (!shouldModifyClass(name)) return classData;
        ClassReader cr = new ClassReader(classData);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = new PrintStreamVisitor(cw);
        cr.accept(cv, ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }

    public static class PrintStreamVisitor extends ClassVisitor {
        public PrintStreamVisitor(ClassVisitor cv) {
            super(Opcodes.ASM9, cv);
        }
        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            if (mv == null) return null;
            if (name.equals("print") && desc.equals("(Ljava/lang/String;)V")) {
                return new PrintMethodVisitor(mv);
            } else if (name.equals("println") && desc.equals("(Ljava/lang/String;)V")) {
                return new PrintlnMethodVisitor(mv);
            }
            return mv;
        }
    }

    public static class PrintMethodVisitor extends MethodVisitor {
        public PrintMethodVisitor(MethodVisitor mv) {
            super(Opcodes.ASM9, mv);
        }
        @Override
        public void visitCode() {
            super.visitCode();
            Label l0 = new Label();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitJumpInsn(Opcodes.IF_ACMPNE, l0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "xyz/dkos/sharploader/agent/Logger", "standard", "(Ljava/lang/String;)V", false);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitLabel(l0);
        }
    }

    public static class PrintlnMethodVisitor extends MethodVisitor {
        public PrintlnMethodVisitor(MethodVisitor mv) {
            super(Opcodes.ASM9, mv);
        }
        @Override
        public void visitCode() {
            super.visitCode();
            Label l0 = new Label();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitJumpInsn(Opcodes.IF_ACMPNE, l0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "xyz/dkos/sharploader/agent/Logger", "standard", "(Ljava/lang/String;)V", false);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitLabel(l0);
        }
    }
}
