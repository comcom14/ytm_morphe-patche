package app.morphe.patches.ytmusic.theme

import app.morphe.patcher.annotation.Patch
import app.morphe.patcher.patch.BytecodePatch
import app.morphe.patcher.patch.context.BytecodeContext
import app.morphe.patcher.patch.context.ResourceContext
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.VarInsnNode
import org.objectweb.asm.Opcodes

@Patch(
    name = "YTMusic Dynamic Light Mode",
    description = "Forces YouTube Music to use a light layout styled dynamically with Android Material You colors.",
    targetApp = "com.google.android.apps.youtube.music"
)
class YtMusicDynamicLightModePatch : BytecodePatch() {

    override fun execute(bytecodeContext: BytecodeContext, resourceContext: ResourceContext) {
        
        // STEP 1: Overriding hardcoded Dark Colors in the compiled resources
        val colors = resourceContext.resources.colors
        
        colors["theme_background_primary"] = 0xFFFAFAFA.toInt()
        colors["theme_background_secondary"] = 0xFFF0F0F0.toInt()
        colors["theme_text_primary"] = 0xFF1A1A1A.toInt()
        colors["theme_text_secondary"] = 0xFF555555.toInt()
        colors["theme_icon_active"] = 0xFF1A1A1A.toInt()

        // STEP 2: Bytecode Injection for Dynamic Material You Colors
        for (classNode in bytecodeContext.classes) {
            if (classNode.name == "com/google/android/apps/youtube/music/activities/MusicActivity") {
                for (method in classNode.methods) {
                    
                    if (method.name == "onCreate" && method.desc == "(Landroid/os/Bundle;)V") {
                        val instructions = method.instructions
                        val iterator = instructions.iterator()
                        
                        while (iterator.hasNext()) {
                            val insn = iterator.next()
                            
                            if (insn.opcode == Opcodes.INVOKESPECIAL) {
                                val methodInsn = insn as MethodInsnNode
                                if (methodInsn.name == "onCreate") {
                                    
                                    val injection = InsnList().apply {
                                        add(VarInsnNode(Opcodes.ALOAD, 0)) 
                                        add(MethodInsnNode(
                                            Opcodes.INVOKESTATIC,
                                            "com/google/android/material/color/DynamicColors",
                                            "applyToActivityIfAvailable",
                                            "(Landroid/app/Activity;)V",
                                            false
                                        ))
                                    }
                                    
                                    instructions.insert(insn, injection)
                                    break
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
