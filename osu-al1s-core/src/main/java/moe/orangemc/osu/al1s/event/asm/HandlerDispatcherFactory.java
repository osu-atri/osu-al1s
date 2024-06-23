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

import moe.orangemc.osu.al1s.util.DigestUtil;
import moe.orangemc.osu.al1s.util.SneakyExceptionHelper;
import org.apache.commons.lang3.Validate;
import org.objectweb.asm.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class HandlerDispatcherFactory {
    private final HandlerDispatcherClassLoader classLoader = new HandlerDispatcherClassLoader();
    private final Map<Method, Class<HandlerDispatcher<?>>> cache = new HashMap<>();

    @SuppressWarnings("unchecked")
    public HandlerDispatcher<?> createDispatcher(Object handler, Method m, Class<?> expectedParam, boolean ignoreCancelled) {
        Validate.isTrue(handler.getClass() == m.getDeclaringClass(), "Invalid handler method: " + m);
        Validate.isTrue(m.getParameters()[0].getType() == expectedParam, "Invalid handler method: " + m);

        if (cache.containsKey(m)) {
            return SneakyExceptionHelper.call(() -> cache.get(m).getConstructor(m.getDeclaringClass()).newInstance(handler));
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        String generatedName = "moe/orangemc/osu/al1s/event/asm/HandlerDispatcherImpl_" + DigestUtil.sha256sum(Type.getType(m.getDeclaringClass()) + "@" + Type.getMethodDescriptor(m));

        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, generatedName, null, "java/lang/Object", new String[]{"moe/orangemc/osu/al1s/event/asm/HandlerDispatcher"});

        /* handler */ {
            cw.visitField(Opcodes.ACC_PRIVATE, "handler", Type.getDescriptor(m.getDeclaringClass()), null, null);
        }

        /* <init>(handler) */ {
            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", Type.getMethodDescriptor(Type.getType(void.class), Type.getType(m.getDeclaringClass())), null, null);

            mv.visitCode();
            // this.handler = handler;
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitFieldInsn(Opcodes.PUTFIELD, generatedName, "handler", Type.getDescriptor(m.getDeclaringClass()));
            mv.visitInsn(Opcodes.RETURN);

            mv.visitEnd();
        }

        /* dispatchEvent(Event event) */ {
            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "dispatchEvent", Type.getMethodDescriptor(Type.getType(void.class), Type.getType(Object.class)), null, null);

            mv.visitCode();

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
                    mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "moe/orangemc/osu/al1s/api/event/CancellableEvent", "isCancelled", "()Z", true);
                    mv.visitInsn(Opcodes.ICONST_0);
                    mv.visitJumpInsn(Opcodes.IF_ICMPEQ, end);
                    mv.visitInsn(Opcodes.RETURN);
                }

                // else
                mv.visitLabel(notCancellable);
                mv.visitInsn(Opcodes.POP);

                mv.visitLabel(end);
            }

            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, generatedName, "handler", Type.getDescriptor(m.getDeclaringClass()));
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(expectedParam));
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(m.getDeclaringClass()), m.getName(), Type.getMethodDescriptor(m), false);
            mv.visitInsn(Opcodes.RETURN);
        }

        /* getOwner */ {
            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "getOwner", Type.getMethodDescriptor(Type.getType(Object.class)), null, null);

            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, generatedName, "handler", Type.getDescriptor(m.getDeclaringClass()));
            mv.visitInsn(Opcodes.ARETURN);
        }

        byte[] classBytes = cw.toByteArray();
        Class<HandlerDispatcher<?>> clazz = (Class<HandlerDispatcher<?>>) classLoader.makeClass(generatedName.replace('/', '.'), classBytes);
        cache.put(m, clazz);
        return SneakyExceptionHelper.call(() -> clazz.getConstructor(handler.getClass()).newInstance(handler));
    }
}
