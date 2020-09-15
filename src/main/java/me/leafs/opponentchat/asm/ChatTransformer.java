package me.leafs.opponentchat.asm;

import me.leafs.opponentchat.events.ClientPreChatEvent;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ChatTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        // ignore all classes that aren't EntityPlayerSP
        if (!transformedName.equals("net.minecraft.client.entity.EntityPlayerSP")) {
            return basicClass;
        }

        // consume the original bytes
        ClassReader reader = new ClassReader(basicClass);
        ClassNode node = new ClassNode();

        reader.accept(node, 0);

        for (MethodNode method : node.methods) {
            FMLDeobfuscatingRemapper remap = FMLDeobfuscatingRemapper.INSTANCE;

            // check the name, make sure it's EntityPlayerSP#sendChatMessage
            String mappedName = remap.mapMethodName(node.name, method.name, method.desc);
            if (!mappedName.equals("sendChatMessage") && !mappedName.equals("func_71165_d")) {
                continue;
            }

            // quick debugging line
            System.out.println("Found " + method.name);

            /*
                use ASM to inject this code into the method

                if (!MinecraftForge.EVENT_BUS.post(new ClientPreChatEvent(message))) {
                    return;
                }

                ugly ASM code below :v (there's no way to make it less ugly)
            */

            String eventName = Type.getInternalName(ClientPreChatEvent.class);
            String eventBusName = Type.getInternalName(EventBus.class);

            InsnList inst = new InsnList();

            inst.add(new FieldInsnNode(Opcodes.GETSTATIC, Type.getInternalName(MinecraftForge.class), "EVENT_BUS", Type.getDescriptor(EventBus.class)));

            inst.add(new TypeInsnNode(Opcodes.NEW, eventName));
            inst.add(new InsnNode(Opcodes.DUP));
            inst.add(new VarInsnNode(Opcodes.ALOAD, 1));

            inst.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, eventName, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class))));
            inst.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, eventBusName, "post", Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getType(Event.class))));

            LabelNode label = new LabelNode();
            inst.add(new JumpInsnNode(Opcodes.IFEQ, label));
            inst.add(new InsnNode(Opcodes.RETURN));
            inst.add(label);

            // modify the sendChatMessage method
            method.instructions.insertBefore(method.instructions.getFirst(), inst);

            break;
        }

        // write the changes
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        node.accept(writer);

        return writer.toByteArray();
    }
}
