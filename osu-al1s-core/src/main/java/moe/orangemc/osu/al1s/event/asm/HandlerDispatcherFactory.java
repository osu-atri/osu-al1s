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

package moe.orangemc.osu.al1s.event.asm;

import moe.orangemc.osu.al1s.api.event.Event;
import moe.orangemc.osu.al1s.bot.OsuBotImpl;
import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.util.DigestUtil;
import moe.orangemc.osu.al1s.util.SneakyExceptionHelper;
import org.apache.commons.lang3.Validate;
import org.objectweb.asm.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class HandlerDispatcherFactory {
    @Inject
    private OsuBotImpl osuBot;

    private final HandlerDispatcherClassLoader classLoader = new HandlerDispatcherClassLoader();
    private final Map<Method, Class<HandlerDispatcher<?>>> cache = new HashMap<>();

    @SuppressWarnings("unchecked")
    public HandlerDispatcher<?> createDispatcher(Object handler, Method m, Class<?> expectedParam, boolean ignoreCancelled, int orderIndex) {
        Validate.isTrue(handler.getClass() == m.getDeclaringClass(), "Invalid handler method: " + m);
        Validate.isTrue(m.getParameters()[0].getType() == expectedParam, "Invalid handler method: " + m);

        if (cache.containsKey(m)) {
            return SneakyExceptionHelper.call(() -> cache.get(m).getConstructor(m.getDeclaringClass()).newInstance(handler));
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        String generatedName = "moe/orangemc/osu/al1s/event/asm/HandlerDispatcherImpl_" + DigestUtil.sha256sum(Type.getType(m.getDeclaringClass()) + "." + m.getName() + "@" + Type.getMethodDescriptor(m) + ":" + ignoreCancelled);

        cw.visit(Opcodes.V22, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, generatedName, null, "java/lang/Object", new String[]{"moe/orangemc/osu/al1s/event/asm/HandlerDispatcher"});
        cw.visitSource("HandlerDispatcherFactory.java", null);

        /* handler */ {
            cw.visitField(Opcodes.ACC_PRIVATE, "handler", Type.getDescriptor(m.getDeclaringClass()), null, null);
        }

        /* <init>(handler) */ {
            MethodVisitor mv = new LineNumberedMethodVisitor(cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", Type.getMethodDescriptor(Type.getType(void.class), Type.getType(m.getDeclaringClass())), null, null));

            mv.visitCode();

            // this.handler = handler;
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitFieldInsn(Opcodes.PUTFIELD, generatedName, "handler", Type.getDescriptor(m.getDeclaringClass()));
            mv.visitInsn(Opcodes.RETURN);

            mv.visitMaxs(2, 2);

            mv.visitEnd();
        }

        /* dispatchEvent(Event event) */ {
            MethodVisitor mv = new LineNumberedMethodVisitor(cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "dispatchEvent", Type.getMethodDescriptor(Type.getType(void.class), Type.getType(Event.class)), null, null));

            mv.visitCode();

            int stack = 2;
            int locals = 2;

            if (ignoreCancelled) {
                // if (event instanceof CancellableEvent)
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitInsn(Opcodes.DUP);
                mv.visitTypeInsn(Opcodes.INSTANCEOF, "moe/orangemc/osu/al1s/api/event/CancellableEvent");

                Label notCancellable = new Label();
                mv.visitInsn(Opcodes.ICONST_0);
                mv.visitJumpInsn(Opcodes.IF_ICMPEQ, notCancellable);

                Label end = new Label();

                /* if (((CancellableEvent) event).isCancelled()) */ {
                    // return;
                    mv.visitTypeInsn(Opcodes.CHECKCAST, "moe/orangemc/osu/al1s/api/event/CancellableEvent"); // We did a `dup` before
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "moe/orangemc/osu/al1s/api/event/CancellableEvent", "isCancelled", "()Z", false);
                    mv.visitInsn(Opcodes.ICONST_0);
                    mv.visitJumpInsn(Opcodes.IF_ICMPEQ, end);
                    mv.visitInsn(Opcodes.RETURN);
                }

                // else
                mv.visitLabel(notCancellable);
                mv.visitInsn(Opcodes.POP);

                mv.visitLabel(end);

                stack ++;
            }

            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, generatedName, "handler", Type.getDescriptor(m.getDeclaringClass()));
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(expectedParam));
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(m.getDeclaringClass()), m.getName(), Type.getMethodDescriptor(m), false);
            mv.visitInsn(Opcodes.RETURN);

            mv.visitMaxs(stack, locals);

            mv.visitEnd();
        }

        /* getOwner */ {
            MethodVisitor mv = new LineNumberedMethodVisitor(cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "getOwner", Type.getMethodDescriptor(Type.getType(Object.class)), null, null));

            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, generatedName, "handler", Type.getDescriptor(m.getDeclaringClass()));
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Object");
            mv.visitInsn(Opcodes.ARETURN);

            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }

        /* getOrderIndex */ {
            MethodVisitor mv = new LineNumberedMethodVisitor(cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "getOrderIndex", Type.getMethodDescriptor(Type.getType(int.class)), null, null));

            mv.visitCode();
            mv.visitLdcInsn(orderIndex);
            mv.visitInsn(Opcodes.IRETURN);

            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }

        byte[] classBytes = cw.toByteArray();
        dumpClass(classBytes);

        Class<HandlerDispatcher<?>> clazz = (Class<HandlerDispatcher<?>>) classLoader.makeClass(generatedName.replace('/', '.'), classBytes);
        cache.put(m, clazz);

        return SneakyExceptionHelper.call(() -> clazz.getConstructor(handler.getClass()).newInstance(handler));
    }

    private void dumpClass(byte[] data) {
        if (!osuBot.debug) {
            return;
        }

        try {
            File tmp = File.createTempFile("dump", ".class");
            System.out.println("Dumping class to " + tmp.getAbsolutePath());

            try (FileOutputStream fos = new FileOutputStream(tmp)) {
                fos.write(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
