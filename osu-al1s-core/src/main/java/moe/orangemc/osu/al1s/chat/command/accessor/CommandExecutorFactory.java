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

package moe.orangemc.osu.al1s.chat.command.accessor;

import moe.orangemc.osu.al1s.accessor.AccessorClassLoader;
import moe.orangemc.osu.al1s.api.chat.OsuChannel;
import moe.orangemc.osu.al1s.api.chat.command.ArgumentTypeAdapter;
import moe.orangemc.osu.al1s.api.chat.command.Command;
import moe.orangemc.osu.al1s.api.chat.command.CommandBase;
import moe.orangemc.osu.al1s.api.chat.command.StringReader;
import moe.orangemc.osu.al1s.api.user.User;
import moe.orangemc.osu.al1s.chat.command.CommandManagerImpl;
import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.util.SneakyExceptionHelper;
import org.objectweb.asm.*;
import org.objectweb.asm.util.CheckClassAdapter;

import java.lang.reflect.Constructor;
import java.util.*;

public class CommandExecutorFactory {
    @Inject
    private AccessorClassLoader classLoader;

    private final Map<CommandBase, GeneratedCommandExecutor> cache = new HashMap<>();

    @SuppressWarnings("unchecked")
    public GeneratedCommandExecutor createExecutor(CommandBase commandBase) {
        if (cache.containsKey(commandBase)) {
            return cache.get(commandBase);
        }

        CommandArgumentNode tree = CommandArgumentNode.build(Arrays.stream(commandBase.getClass().getMethods()).filter(method -> method.getAnnotation(Command.class) != null).toList());

        ClassWriter cwo = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        CheckClassAdapter cw = new CheckClassAdapter(cwo);

        String generatedName = "moe/orangemc/osu/al1s/chat/command/accessor/GeneratedCommandExecutorImpl@" + commandBase.getClass().getName().replace(".", "_");
        cw.visit(Opcodes.V22, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, generatedName, null, "java/lang/Object", new String[]{"moe/orangemc/osu/al1s/chat/command/accessor/GeneratedCommandExecutor"});

        Type commandBaseType = Type.getType(commandBase.getClass());

        cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "commandBase", commandBaseType.getDescriptor(), null, null);

        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, commandBaseType), null, null);
        mv.visitCode();
        { // <init>
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitFieldInsn(Opcodes.PUTFIELD, generatedName, "commandBase", commandBaseType.getDescriptor());

            // Magic class loading. Avoids ClassCastException.
            mv.visitLdcInsn(GeneratedCommandExecutor.class.getName());
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Class", "forName", Type.getMethodDescriptor(Type.getType(Class.class), Type.getType(String.class)), false);
            mv.visitInsn(Opcodes.POP);

            mv.visitInsn(Opcodes.RETURN);
        }
        mv.visitMaxs(2, 2);
        mv.visitEnd();

        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "execute", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(User.class), Type.getType(OsuChannel.class), Type.getType(CommandManagerImpl.class), Type.getType(StringReader.class)), null, null);
        mv.visitCode();

        int depth;
        {
            depth = buildCommandInvocation(mv, tree, 0, generatedName);
            mv.visitInsn(Opcodes.RETURN);
        }
        mv.visitMaxs(depth + 3, depth + 5);
        mv.visitEnd();

        cw.visitEnd();

        byte[] bytes = cwo.toByteArray();
        Class<?> clazz = classLoader.makeClass(generatedName.replaceAll("/", "."), bytes);
        GeneratedCommandExecutor executor = SneakyExceptionHelper.call(() -> {
            Constructor<? extends GeneratedCommandExecutor> constructor = (Constructor<? extends GeneratedCommandExecutor>) clazz.getConstructor(commandBase.getClass());
            return constructor.newInstance(commandBase);
        });

        cache.put(commandBase, executor);
        return executor;
    }

    private int buildCommandInvocation(MethodVisitor mv, CommandArgumentNode node, int depth, String name) {
        if (depth == 0) {// root
            int maxDepth = 0;

            if (node.getMethod() != null) {
                Label lengthCheckpoint = new Label();

                mv.visitVarInsn(Opcodes.ALOAD, 4);

                Type stringReaderType = Type.getType(StringReader.class);
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, stringReaderType.getInternalName(), "canRead", Type.getMethodDescriptor(Type.BOOLEAN_TYPE), false);
                mv.visitInsn(Opcodes.ICONST_0);
                mv.visitJumpInsn(Opcodes.IF_ICMPNE, lengthCheckpoint);

                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(Opcodes.GETFIELD, name, "commandBase", Type.getDescriptor(node.getMethod().getDeclaringClass()));
                mv.visitVarInsn(Opcodes.ALOAD, 1); // user
                mv.visitVarInsn(Opcodes.ALOAD, 2); // channel
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(node.getMethod().getDeclaringClass()), node.getMethod().getName(), Type.getMethodDescriptor(node.getMethod()), false);
                mv.visitInsn(Opcodes.RETURN);

                mv.visitLabel(lengthCheckpoint);
            }

            for (CommandArgumentNode child : node.getChildren()) {
                maxDepth = Math.max(buildCommandInvocation(mv, child, depth + 1, name), maxDepth);
            }
            return maxDepth;
        }

        Type commandManagerImplType = Type.getType(CommandManagerImpl.class);
        Type typeAdapterType = Type.getType(ArgumentTypeAdapter.class);
        Type stringReaderType = Type.getType(StringReader.class);

        mv.visitVarInsn(Opcodes.ALOAD, 3);
        mv.visitLdcInsn(node.getParameter().getName());
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Class", "forName", Type.getMethodDescriptor(Type.getType(Class.class), Type.getType(String.class)), false);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, commandManagerImplType.getInternalName(), "getAdapter", Type.getMethodDescriptor(Type.getType(ArgumentTypeAdapter.class), Type.getType(Class.class)), false);

        Label tryStart = new Label();
        Label tryEnd = new Label();
        Label catchStart = new Label();
        Label beforeCatch = new Label();

        mv.visitTryCatchBlock(tryStart, tryEnd, catchStart, "java/lang/IllegalArgumentException");
        mv.visitTryCatchBlock(tryStart, tryEnd, catchStart, "java/lang/StringIndexOutOfBoundsException");
        mv.visitLabel(tryStart);

        mv.visitVarInsn(Opcodes.ALOAD, 4);
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, stringReaderType.getInternalName(), "canRead", Type.getMethodDescriptor(Type.BOOLEAN_TYPE), false);
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitJumpInsn(Opcodes.IF_ICMPEQ, beforeCatch);
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, stringReaderType.getInternalName(), "mark", Type.getMethodDescriptor(Type.VOID_TYPE), false);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, typeAdapterType.getInternalName(), "parse", Type.getMethodDescriptor(SneakyExceptionHelper.call(() -> ArgumentTypeAdapter.class.getMethod("parse", StringReader.class))), true);

        mv.visitVarInsn(Opcodes.ALOAD, 4);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, stringReaderType.getInternalName(), "skip", Type.getMethodDescriptor(Type.VOID_TYPE), false);

        mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(node.getParameter()));
        mv.visitVarInsn(Opcodes.ASTORE, 4 + depth);
        mv.visitLabel(tryEnd);

        int maxDepth = depth;
        for (CommandArgumentNode child : node.getChildren()) {
            maxDepth = Math.max(buildCommandInvocation(mv, child, depth + 1, name), depth);
        }

        if (node.getMethod() != null) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, name, "commandBase", Type.getDescriptor(node.getMethod().getDeclaringClass()));
            mv.visitVarInsn(Opcodes.ALOAD, 1); // user
            mv.visitVarInsn(Opcodes.ALOAD, 2); // channel
            for (int i = 0; i < depth; i++) {
                mv.visitVarInsn(Opcodes.ALOAD, 5 + i);
                Class<?> parameterType = node.getMethod().getParameterTypes()[i + 2];
                visitConversion(mv, parameterType);
            }
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(node.getMethod().getDeclaringClass()), node.getMethod().getName(), Type.getMethodDescriptor(node.getMethod()), false);
            mv.visitInsn(Opcodes.RETURN);
        } else {
            mv.visitTypeInsn(Opcodes.NEW, "java/lang/IllegalArgumentException");
            mv.visitInsn(Opcodes.DUP);
            mv.visitLdcInsn("Invalid command arguments");
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class)), false);
            mv.visitInsn(Opcodes.ATHROW);
        }
        mv.visitLabel(beforeCatch);
        mv.visitInsn(Opcodes.POP);

        mv.visitLabel(catchStart);
        mv.visitVarInsn(Opcodes.ALOAD, 4);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, stringReaderType.getInternalName(), "reset", Type.getMethodDescriptor(Type.VOID_TYPE), false);
        mv.visitInsn(Opcodes.POP);
        return maxDepth;
    }

    private void visitConversion(MethodVisitor mv, Class<?> parameterType) {
        if (parameterType.isPrimitive()) {
            if (parameterType == int.class) {
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", Type.getMethodDescriptor(Type.INT_TYPE), false);
            } else if (parameterType == long.class) {
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Long", "longValue", Type.getMethodDescriptor(Type.LONG_TYPE), false);
            } else if (parameterType == float.class) {
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Float", "floatValue", Type.getMethodDescriptor(Type.FLOAT_TYPE), false);
            } else if (parameterType == double.class) {
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Double", "doubleValue", Type.getMethodDescriptor(Type.DOUBLE_TYPE), false);
            } else if (parameterType == short.class) {
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Short", "shortValue", Type.getMethodDescriptor(Type.SHORT_TYPE), false);
            } else if (parameterType == byte.class) {
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Byte", "byteValue", Type.getMethodDescriptor(Type.BYTE_TYPE), false);
            } else if (parameterType == char.class) {
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Character", "charValue", Type.getMethodDescriptor(Type.CHAR_TYPE), false);
            } else if (parameterType == boolean.class) {
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", Type.getMethodDescriptor(Type.BOOLEAN_TYPE), false);
            }
        } else {
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(parameterType));
        }
    }
}
