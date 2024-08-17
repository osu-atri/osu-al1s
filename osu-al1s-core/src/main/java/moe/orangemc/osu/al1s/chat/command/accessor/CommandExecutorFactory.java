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

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

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
            mv.visitInsn(Opcodes.RETURN);
        }
        mv.visitMaxs(2, 2);
        mv.visitEnd();

        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "execute", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(User.class), Type.getType(OsuChannel.class), Type.getType(CommandManagerImpl.class), Type.getType(StringReader.class)), null, null);
        mv.visitCode();

        int depth;
        {
            depth = buildCommandInvocation(mv, tree, 0);
        }
        mv.visitEnd();

        cw.visitEnd();

        byte[] bytes = cw.toByteArray();
        Class<?> clazz = classLoader.makeClass(generatedName, bytes);
        GeneratedCommandExecutor executor = SneakyExceptionHelper.call(() -> {
            Constructor<? extends GeneratedCommandExecutor> constructor = (Constructor<? extends GeneratedCommandExecutor>) clazz.getConstructor(commandBase.getClass());
            return constructor.newInstance(commandBase);
        });

        cache.put(commandBase, executor);
        return executor;
    }

    private int buildCommandInvocation(MethodVisitor mv, CommandArgumentNode node, int depth) {
        if (node.getParameter() == null) {// root
            int maxDepth = 0;
            for (CommandArgumentNode child : node.getChildren()) {
                maxDepth = Math.max(buildCommandInvocation(mv, child, depth + 1), maxDepth);
            }
            return maxDepth;
        }

        Type commandManagerImplType = Type.getType(CommandManagerImpl.class);
        Type typeAdapterType = Type.getType(ArgumentTypeAdapter.class);
        mv.visitVarInsn(Opcodes.ALOAD, 3);
        mv.visitLdcInsn(node.getParameter());
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, commandManagerImplType.getInternalName(), "getAdapter", Type.getMethodDescriptor(Type.getType(ArgumentTypeAdapter.class), Type.getType(Class.class)), false);

        Label tryStart = new Label();
        Label tryEnd = new Label();
        Label catchStart = new Label();
        Label catchEnd = new Label();

        mv.visitTryCatchBlock(tryStart, tryEnd, catchStart, "java/lang/IllegalArgumentException");
        mv.visitLabel(tryStart);

        mv.visitVarInsn(Opcodes.ALOAD, 4);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, typeAdapterType.getInternalName(), "parse", Type.getMethodDescriptor(SneakyExceptionHelper.call(() -> ArgumentTypeAdapter.class.getMethod("parse", StringReader.class))), true);

        mv.visitInsn(Opcodes.DUP);
        mv.visitTypeInsn(Opcodes.INSTANCEOF, Type.getInternalName(node.getParameter()));
        mv.visitInsn(Opcodes.ICONST_0);

        Label typeCheckpoint = new Label();
        mv.visitJumpInsn(Opcodes.IF_ICMPEQ, typeCheckpoint);

        mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(node.getParameter()));
        mv.visitVarInsn(Opcodes.ASTORE, 4 + depth);
        mv.visitLabel(tryEnd);

        int maxDepth = depth;
        for (CommandArgumentNode child : node.getChildren()) {
            maxDepth = Math.max(buildCommandInvocation(mv, child, depth + 1), depth);
        }

        if (node.getMethod() != null) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, "moe/orangemc/osu/al1s/chat/command/accessor/GeneratedCommandExecutorImpl", "commandBase", Type.getDescriptor(node.getMethod().getDeclaringClass()));
            for (int i = 0; i <= maxDepth; i++) {
                mv.visitVarInsn(Opcodes.ALOAD, 4 + i);
            }
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(node.getMethod().getDeclaringClass()), node.getMethod().getName(), Type.getMethodDescriptor(node.getMethod()), false);
            mv.visitInsn(Opcodes.RETURN);
        }

        mv.visitLabel(catchStart);
        mv.visitLabel(typeCheckpoint);
        return maxDepth;
    }
}
