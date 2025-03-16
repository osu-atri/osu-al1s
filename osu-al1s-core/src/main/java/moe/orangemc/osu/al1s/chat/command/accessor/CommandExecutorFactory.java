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
import moe.orangemc.osu.al1s.api.chat.command.CommandManager;
import moe.orangemc.osu.al1s.api.chat.command.argument.ArgumentTypeAdapter;
import moe.orangemc.osu.al1s.api.chat.command.Command;
import moe.orangemc.osu.al1s.api.chat.command.CommandBase;
import moe.orangemc.osu.al1s.api.chat.command.StringReader;
import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.util.SneakyExceptionHelper;
import org.objectweb.asm.*;
import org.objectweb.asm.util.CheckClassAdapter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public abstract class CommandExecutorFactory<I> {
    @Inject
    private AccessorClassLoader classLoader;

    private final Map<CommandBase, I> cache = new HashMap<>();

    private final List<Class<?>> proxiedParameters = new ArrayList<>();

    public CommandExecutorFactory() {
        collectProxiedParameters();
    }

    private void collectProxiedParameters() {
        try {
            Method proxyExecutorMethod = Arrays.stream(getExecutorInterfaceClass().getMethods()).filter(m -> m.getName().equals("execute")).findFirst().orElseThrow();
            Parameter[] parameters = proxyExecutorMethod.getParameters();
            if (parameters.length < 2) {
                throw new IllegalStateException("Bad method signature, you need at least execute(CommandManager, StringReader) in your executor interface.");
            }

            if (!CommandManager.class.isAssignableFrom(parameters[0].getType())) {
                throw new IllegalStateException("The first parameter of the execute method must be of type CommandManager.");
            }
            if (!StringReader.class.isAssignableFrom(parameters[1].getType())) {
                throw new IllegalStateException("The second parameter of the execute method must be of type StringReader.");
            }

            for (int i = 2; i < parameters.length; i++) {
                proxiedParameters.add(parameters[i].getType());
            }
        } catch (NoSuchElementException | IllegalStateException e) {
            throw new IllegalStateException("The executor interface must have an execute method with the correct signature.", e);
        }
    }

    @SuppressWarnings("unchecked")
    public I fetchExecutor(CommandBase commandBase) {
        if (cache.containsKey(commandBase)) {
            return cache.get(commandBase);
        }

        List<Method> list = new ArrayList<>();
        for (Method method : commandBase.getClass().getMethods()) {
            if (method.getAnnotation(Command.class) != null) {
                validateStartingMethod(method);
                list.add(method);
            }
        }

        CommandArgumentNode tree = CommandArgumentNode.build(list, proxiedParameters.size());

        ClassWriter cwo = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        CheckClassAdapter cw = new CheckClassAdapter(cwo);

        String generatedName = "moe/orangemc/osu/al1s/chat/command/accessor/GeneratedCommandExecutorImpl@" + commandBase.getClass().getName().replace(".", "_");
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, generatedName, null, "java/lang/Object", new String[]{Type.getInternalName(getExecutorInterfaceClass())});

        Type commandBaseType = Type.getType(commandBase.getClass());
        cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "commandBase", commandBaseType.getDescriptor(), null, null);
        generateClassMethods(cw, commandBaseType, generatedName, tree);

        cw.visitEnd();

        byte[] bytes = cwo.toByteArray();
        Class<?> clazz = classLoader.makeClass(generatedName.replaceAll("/", "."), bytes);
        I executor = SneakyExceptionHelper.call(() -> {
            Constructor<? extends I> constructor = (Constructor<? extends I>) clazz.getConstructor(commandBase.getClass());
            return constructor.newInstance(commandBase);
        });

        cache.put(commandBase, executor);
        return executor;
    }

    private void validateStartingMethod(Method target) {
        Parameter[] parameters = target.getParameters();
        if (parameters.length < proxiedParameters.size()) {
            throw new IllegalArgumentException("The method " + target + " must have at least " + proxiedParameters.size() + " parameters.");
        }

        for (int i = 0; i < proxiedParameters.size(); i++) {
            if (!parameters[i + 2].getType().isAssignableFrom(proxiedParameters.get(i))) {
                throw new IllegalArgumentException("The method " + target + " must have parameter " + (i + 2) + " of type " + proxiedParameters.get(i).getName() + ".");
            }
        }
    }

    protected void generateClassMethods(CheckClassAdapter cw, Type commandBaseType, String generatedName, CommandArgumentNode tree) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, commandBaseType), null, null);
        mv.visitCode();
        generateConstructor(mv, generatedName, commandBaseType);
        mv.visitMaxs(2, 2);
        mv.visitEnd();

        List<Type> generatedMethodTypes = new ArrayList<>();
        generatedMethodTypes.add(Type.getType(getCommandManagerClass()));
        generatedMethodTypes.add(Type.getType(StringReader.class));

        for (Class<?> proxiedParameter : proxiedParameters) {
            generatedMethodTypes.add(Type.getType(proxiedParameter));
        }

        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "execute", Type.getMethodDescriptor(Type.VOID_TYPE, generatedMethodTypes.toArray(new Type[0])), null, null);
        mv.visitCode();

        buildCommandInvocation(mv, tree, generatedName, commandBaseType);
        mv.visitMaxs(proxiedParameters.size() + 2, proxiedParameters.size() + 2 + 2 /* for type adapter calls. */);
        mv.visitEnd();
    }

    protected void generateConstructor(MethodVisitor mv, String generatedName, Type commandBaseType) {
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitFieldInsn(Opcodes.PUTFIELD, generatedName, "commandBase", commandBaseType.getDescriptor());

        // Magic class loading. Avoids ClassCastException.
        mv.visitLdcInsn(getExecutorInterfaceClass().getName());
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Class", "forName", Type.getMethodDescriptor(Type.getType(Class.class), Type.getType(String.class)), false);
        mv.visitInsn(Opcodes.POP);

        mv.visitInsn(Opcodes.RETURN);
    }

    // root call.
    protected void buildCommandInvocation(MethodVisitor mv, CommandArgumentNode tree, String generatedName, Type owner) {
        // the layout would be reused
        initiateStackFengshui(mv, generatedName, owner);
        buildCommandInvocation(mv, tree, 0, generatedName, owner, null, null);
    }

    protected void buildCommandInvocation(MethodVisitor mv, CommandArgumentNode node, int depth, String name, Type owner, Label parent, Label sibling) {
        // Ok, we want to clarify the arguments here.
        // node is a tree, at very first glance, we want to generate some if else statements for arguments.
        // but things would go wrong if we picked wrong parameters, so here i'm using IllegalArgumentExceptions for type failures.
        // and this method will be called recursively for each child node.
        // We need to be greedy, to parse as much parameters as possible. then a fallback call will be made

        // therefore, depth=current index.
        if (depth == 0) {
            visitCurrentAsRoot(mv, node, depth, name, owner);
            return;
        }

        // Non-root, 3 tasks here
        // 1. Check if there is more arguments, if not, fall to parent
        Type stringReaderType = Type.getType(StringReader.class);
        mv.visitVarInsn(Opcodes.ALOAD, 2);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, stringReaderType.getInternalName(), "canRead", Type.getMethodDescriptor(Type.BOOLEAN_TYPE), false);
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitJumpInsn(Opcodes.IF_ICMPEQ, parent); // no more args, fall to parent


        // 2. Parse the current argument, if failed, fall to siblings
        Type commandManagerImplType = Type.getType(getCommandManagerClass());
        Type typeAdapterType = Type.getType(ArgumentTypeAdapter.class);

        generateParameterParserFetch(mv, node, commandManagerImplType);

        Label tryStart = new Label();
        Label tryEnd = new Label();

        // Usually this is a signal that we've capture a wrong parameter type, so it is ok to ask for siblings for help.
        mv.visitTryCatchBlock(tryStart, tryEnd, sibling, "java/lang/IllegalArgumentException");
        mv.visitTryCatchBlock(tryStart, tryEnd, sibling, "java/lang/StringIndexOutOfBoundsException");

        // we are not going to fallback to parents, eg: "fal" is bad for boolean adapter, but ok for string adapter.
        generateNodeParameterParse(mv, node, tryStart, stringReaderType, typeAdapterType, tryEnd);

        // 3. If succeeded, do as the root.
        visitCurrentAsRoot(mv, node, depth, name, owner);
    }

    private void visitCurrentAsRoot(MethodVisitor mv, CommandArgumentNode node, int depth, String name, Type owner) {
        Label sibling;
        Label me;
        // root node
        // we are here to load proxied parameters, command managers etc.

        // current local layout should be like this:
        // +---+------------------+
        // | 1 | Command Manager  |
        // | 2 | StringReader     |
        // | 3 | ...proxied       |
        // | . | ...proxied       |
        // +---+------------------+

        // Stack layout should be similar, with extra arguments
        // +---+---------------------+
        // | . | same as local       |
        // | n | start of user input |
        // | . | ...                 |
        // +---+---------------------+

        // length checkpoint will be set at the end of other deeper argument parsing and calls
        // we will reuse the stack, and create a frame for them
        // root->lengthCheckPoint defaults null.
        me = new Label();
        sibling = new Label();

        lookForChildren(mv, node, depth + 1, name, owner, me, sibling);

        // we've waited for soo long
        if (node.getMethod() != null) {
            mv.visitLabel(me);
            generateCommandInvocation(mv, node);
            mv.visitInsn(Opcodes.RETURN);
        } else {
            generateUnknownExceptionRaiser(mv);
        }
    }

    private void lookForChildren(MethodVisitor mv, CommandArgumentNode node, int depth, String name, Type owner, Label me, Label sibling) {
        // next frame start:
        // for greedy, we need to find next args first before root call.
        // sibling for parameter iterate trials, and goes next if fails.
        for (CommandArgumentNode child : node.getChildren()) {
            // we need a quick rollback if fails,
            // StringReader#mark & StringReader#reset is going to help there.
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getType(StringReader.class).getInternalName(), "mark", Type.getMethodDescriptor(Type.VOID_TYPE), false);

            buildCommandInvocation(mv, child, depth++, name, owner, me, sibling);

            // if prev succeeds, they will not return.
            mv.visitLabel(sibling);

            // quick reset
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getType(StringReader.class).getInternalName(), "reset", Type.getMethodDescriptor(Type.VOID_TYPE), false);

            sibling = new Label();
        }
    }

    protected abstract Class<I> getExecutorInterfaceClass();

    protected abstract Class<? extends CommandManager> getCommandManagerClass();

    // Ok fine we've successfully taken jvm down.
    // now AL-1s requires -noverify to run.
    private void initiateStackFengshui(MethodVisitor mv, String name, Type owner) {
        // local layout:
        // +---+------------------+
        // | 1 | Command Manager  |
        // | 2 | StringReader     |
        // | 3 | ...proxied       |
        // | . | ...proxied       |
        // +---+------------------+

        // wanted stack:
        // +---+------------------+
        // | 0 | Command Base     |
        // | 1 | ...proxied       |
        // | . | ...proxied       |
        // +---+------------------+

        // setup a command base, for further calls.
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, name, "commandBase", owner.getDescriptor());

        // The terminal executor must start from 2, which is proxied parameters.
        int idx = 3;
        for (Class<?> proxiedPara : proxiedParameters) {
            visitLoad(mv, proxiedPara, idx++);
        }
    }

    protected void generateCommandInvocation(MethodVisitor mv, CommandArgumentNode node) {
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(node.getMethod().getDeclaringClass()), node.getMethod().getName(), Type.getMethodDescriptor(node.getMethod()), false);
    }

    protected void generateParameterParserFetch(MethodVisitor mv, CommandArgumentNode node, Type commandManagerImplType) {
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitLdcInsn(node.getParameter().getName());
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Class", "forName", Type.getMethodDescriptor(Type.getType(Class.class), Type.getType(String.class)), false);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, commandManagerImplType.getInternalName(), "getAdapter", Type.getMethodDescriptor(Type.getType(ArgumentTypeAdapter.class), Type.getType(Class.class)), false);
    }

    protected void generateUnknownExceptionRaiser(MethodVisitor mv) {
        mv.visitTypeInsn(Opcodes.NEW, "java/lang/IllegalArgumentException");
        mv.visitInsn(Opcodes.DUP);
        mv.visitLdcInsn("Invalid command arguments");
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class)), false);
        mv.visitInsn(Opcodes.ATHROW);
    }

    private void generateNodeParameterParse(MethodVisitor mv, CommandArgumentNode node, Label tryStart, Type stringReaderType, Type typeAdapterType, Label tryEnd) {
        // +---+------------------+
        // | 1 | Command Manager  |
        // | 2 | StringReader     |
        // | 3 | ...proxied       |
        // | . | ...proxied       |
        // +---+------------------+

        // here to parse arguments,
        // and positioning string reader to the start of next, or the end.
        mv.visitLabel(tryStart);
        mv.visitVarInsn(Opcodes.ALOAD, 2);
        // throws IllegalArgumentException if bad things happens, and helps to jump to siblings.
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, typeAdapterType.getInternalName(), "parse", Type.getMethodDescriptor(SneakyExceptionHelper.call(() -> ArgumentTypeAdapter.class.getMethod("parse", StringReader.class))), true);

        // We are not going to use `dup`. Or we'll have to `pop` without any prompt,
        // it will likely to take down our stack layout.
        mv.visitVarInsn(Opcodes.ALOAD, 2);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, stringReaderType.getInternalName(), "skip", Type.getMethodDescriptor(Type.VOID_TYPE), false);

        visitConversion(mv, node.getParameter());
        mv.visitLabel(tryEnd);
    }

    private void visitConversion(MethodVisitor mv, Class<?> parameterType) {
        // Boring maps, tell me if there is an API.
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

    private void visitLoad(MethodVisitor mv, Class<?> localType, int idx) {
        // Also tell me if there's an API
        if (localType.isPrimitive()) {
            if (localType == int.class) {
                mv.visitVarInsn(Opcodes.ILOAD, idx);
            } else if (localType == long.class) {
                mv.visitVarInsn(Opcodes.LLOAD, idx);
            } else if (localType == float.class) {
                mv.visitVarInsn(Opcodes.FLOAD, idx);
            } else if (localType == double.class) {
                mv.visitVarInsn(Opcodes.DLOAD, idx);
            } else if (localType == short.class || localType == byte.class || localType == char.class) {
                mv.visitVarInsn(Opcodes.ILOAD, idx);
            } else if (localType == boolean.class) {
                mv.visitVarInsn(Opcodes.ILOAD, idx);
            }
        } else {
            mv.visitVarInsn(Opcodes.ALOAD, idx);
        }
    }
}
