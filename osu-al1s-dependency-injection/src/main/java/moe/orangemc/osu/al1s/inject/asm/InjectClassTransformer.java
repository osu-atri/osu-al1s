/*
 * Copyright 2024 Astro angelfish
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package moe.orangemc.osu.al1s.inject.asm;

import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.inject.api.InjectTiming;
import moe.orangemc.osu.al1s.inject.api.InjectionContext;
import moe.orangemc.osu.al1s.inject.api.Injector;
import org.objectweb.asm.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InjectClassTransformer extends ClassVisitor {
    private static final Type injectType = Type.getType(Inject.class);
    private static final Type injectorType = Type.getType(Injector.class);
    private static final Type loaderType = Type.getType(InjectorClassLoader.class);
    private static final Type reflectionClassType = Type.getType(Class.class);
    private static final Type contextType = Type.getType(InjectionContext.class);

    private final Map<InjectTiming, Set<FieldToInject>> fieldToInject = new HashMap<>();

    private String me;
    private String superName;
    private boolean hasClInit = false;

    protected InjectClassTransformer(ClassVisitor classVisitor) {
        super(Opcodes.ASM9, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        me = name;
        this.superName = superName;
        super.visit(version, access, name, signature, superName, interfaces);

        if ((access & Opcodes.ACC_INTERFACE) != Opcodes.ACC_INTERFACE) {
            super.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, "injectorContext@" + me.hashCode(), contextType.getDescriptor(), null, null);
        }
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        return new FieldIdentifier(super.visitField(access & ~Opcodes.ACC_FINAL, name, descriptor, signature, value), access, name, descriptor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (name.equals("<clinit>")) {
            hasClInit = true;
        }

        if (name.equals("<init>") || name.equals("<clinit>")) {
            return new ConstructorTransformer(super.visitMethod(access, name, descriptor, signature, exceptions), name.equals("<clinit>"));
        }

        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        if (!hasClInit) {
            MethodVisitor clInit = this.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
            clInit.visitCode();
            clInit.visitInsn(Opcodes.RETURN);
            clInit.visitMaxs(1, 0);
            clInit.visitEnd();
        }

        super.visitEnd();
    }

    private class FieldIdentifier extends FieldVisitor {
        private final int access;
        private final String name;
        private final String descriptor;
        private AnnotationReader annotationReader = null;

        protected FieldIdentifier(FieldVisitor fv, int access, String name, String descriptor) {
            super(Opcodes.ASM9, fv);
            this.access = access;
            this.name = name;
            this.descriptor = descriptor;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            if (descriptor.equals(injectType.getDescriptor())) {
                this.annotationReader = new AnnotationReader();
                return annotationReader;
            }

            return super.visitAnnotation(descriptor, visible);
        }

        @Override
        public void visitEnd() {
            if (annotationReader != null) {
                if (!fieldToInject.containsKey(annotationReader.time)) {
                    fieldToInject.put(annotationReader.time, new HashSet<>());
                }
                fieldToInject.get(annotationReader.time).add(new FieldToInject(access, name, descriptor, annotationReader.name));
            }
            super.visitEnd();
        }
    }

    private static class AnnotationReader extends AnnotationVisitor {
        private String name = "default";
        private InjectTiming time = InjectTiming.PRE;

        protected AnnotationReader() {
            super(Opcodes.ASM9);
        }

        @Override
        public void visit(String name, Object value) {
            if (name.equals("name")) {
                this.name = value.toString();
            }
            super.visit(name, value);
        }

        @Override
        public void visitEnum(String name, String descriptor, String value) {
            if (name.equals("when") && descriptor.equals(Type.getDescriptor(InjectTiming.class))) {
                time = InjectTiming.valueOf(value);
            }

            super.visitEnum(name, descriptor, value);
        }
    }

    private class ConstructorTransformer extends MethodVisitor {
        private final Label invalidInjectionLabel = new Label();
        private final boolean statis;

        protected ConstructorTransformer(MethodVisitor methodVisitor, boolean statis) {
            super(Opcodes.ASM9, methodVisitor);
            this.statis = statis;
        }

        private void putFieldsInjectInstruction(InjectTiming timing) {
            Set<FieldToInject> fieldSet = fieldToInject.get(timing);
            if (!fieldToInject.containsKey(timing) || fieldSet.isEmpty()) {
                return;
            }

            super.visitLdcInsn(Type.getObjectType(me).getClassName());
            super.visitMethodInsn(Opcodes.INVOKESTATIC, reflectionClassType.getInternalName(), "forName", "(Ljava/lang/String;)Ljava/lang/Class;", false);

            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, reflectionClassType.getInternalName(), "getClassLoader", "()Ljava/lang/ClassLoader;", false);

            super.visitInsn(Opcodes.DUP);
            super.visitTypeInsn(Opcodes.INSTANCEOF, loaderType.getInternalName());
            super.visitInsn(Opcodes.ICONST_0);
            super.visitJumpInsn(Opcodes.IF_ICMPEQ, invalidInjectionLabel);

            super.visitTypeInsn(Opcodes.CHECKCAST, loaderType.getInternalName());
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, loaderType.getInternalName(), "getInjector", Type.getMethodDescriptor(injectorType), false);
            super.visitMethodInsn(Opcodes.INVOKEINTERFACE, injectorType.getInternalName(), "getCurrentContext", Type.getMethodDescriptor(contextType), true);
            super.visitFieldInsn(Opcodes.PUTSTATIC, me, "injectorContext@" + me.hashCode(), contextType.getDescriptor());

            for (FieldToInject field : fieldSet) {
                int putFieldOpcode = Opcodes.PUTFIELD;
                if ((field.access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC) {
                    putFieldOpcode = Opcodes.PUTSTATIC;
                } else if (!statis) {
                    super.visitVarInsn(Opcodes.ALOAD, 0);
                } else {
                    continue;
                }

                Type fieldType = Type.getType(field.descriptor());

                super.visitFieldInsn(Opcodes.GETSTATIC, me, "injectorContext@" + me.hashCode(), contextType.getDescriptor());
                super.visitLdcInsn(fieldType.getClassName());
                super.visitMethodInsn(Opcodes.INVOKESTATIC, reflectionClassType.getInternalName(), "forName", "(Ljava/lang/String;)Ljava/lang/Class;", false);

                super.visitLdcInsn(field.injectName());

                super.visitMethodInsn(Opcodes.INVOKEINTERFACE, contextType.getInternalName(), "mapField", Type.getMethodDescriptor(Type.getType(Object.class), reflectionClassType, Type.getType(String.class)), true);

                super.visitTypeInsn(Opcodes.CHECKCAST, fieldType.getInternalName());
                super.visitFieldInsn(putFieldOpcode, me, field.name(), field.descriptor());
            }
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode == Opcodes.RETURN) {
                putFieldsInjectInstruction(InjectTiming.POST);

                super.visitInsn(opcode);

                super.visitLabel(invalidInjectionLabel);
                super.visitInsn(Opcodes.POP);
                super.visitTypeInsn(Opcodes.NEW, Type.getInternalName(IllegalStateException.class));
                super.visitInsn(Opcodes.DUP);
                super.visitLdcInsn("Current class isn't bootstrapped by injector");
                super.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(IllegalStateException.class), "<init>", "(Ljava/lang/String;)V", false);
                super.visitInsn(Opcodes.ATHROW);
                return;
            }

            super.visitInsn(opcode);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            if (opcode == Opcodes.INVOKESPECIAL && name.equals("<init>") && owner.equals(superName)) {
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                putFieldsInjectInstruction(InjectTiming.PRE);

                return;
            }

            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }

        @Override
        public void visitCode() {
            super.visitCode();

            if (statis) {
                putFieldsInjectInstruction(InjectTiming.PRE);
            }
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            super.visitMaxs(Math.max(maxStack, 5), maxLocals);
        }
    }

    private record FieldToInject(int access, String name, String descriptor, String injectName) {}
}
