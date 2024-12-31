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

import moe.orangemc.osu.al1s.api.chat.command.CommandManager;
import moe.orangemc.osu.al1s.chat.command.UserspaceCommandManager;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class UserspaceCommandExecutorFactory extends CommandExecutorFactory<UserspaceGeneratedCommandExecutor> {
    @Override
    protected Class<UserspaceGeneratedCommandExecutor> getSuperClass() {
        return UserspaceGeneratedCommandExecutor.class;
    }

    @Override
    protected int getParameterStart() {
        return 2;
    }

    @Override
    protected Class<? extends CommandManager> getCommandManagerClass() {
        return UserspaceCommandManager.class;
    }

    @Override
    protected void generateRootCommandInvocation(MethodVisitor mv, CommandArgumentNode node, String name, Type stringReaderType, Label lengthCheckpoint) {
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, stringReaderType.getInternalName(), "canRead", Type.getMethodDescriptor(Type.BOOLEAN_TYPE), false);
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitJumpInsn(Opcodes.IF_ICMPNE, lengthCheckpoint);

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, name, "commandBase", Type.getDescriptor(node.getMethod().getDeclaringClass()));
        mv.visitVarInsn(Opcodes.ALOAD, 1); // user
        mv.visitVarInsn(Opcodes.ALOAD, 2); // channel
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(node.getMethod().getDeclaringClass()), node.getMethod().getName(), Type.getMethodDescriptor(node.getMethod()), false);
        mv.visitInsn(Opcodes.RETURN);
    }

    protected void generateCommandInvocation(MethodVisitor mv, CommandArgumentNode node, int depth, String name) {
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, name, "commandBase", Type.getDescriptor(node.getMethod().getDeclaringClass()));
        mv.visitVarInsn(Opcodes.ALOAD, 1); // user
        mv.visitVarInsn(Opcodes.ALOAD, 2); // channel
        for (int i = 0; i < depth; i++) {
            mv.visitVarInsn(Opcodes.ALOAD, getGeneratedParameterStart() + i);
            Class<?> parameterType = node.getMethod().getParameterTypes()[i + getParameterStart()];
            visitConversion(mv, parameterType);
        }
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(node.getMethod().getDeclaringClass()), node.getMethod().getName(), Type.getMethodDescriptor(node.getMethod()), false);
        mv.visitInsn(Opcodes.RETURN);
    }
}
