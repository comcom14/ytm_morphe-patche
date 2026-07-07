package app.morphe.patches.ytmusic.theme

import app.morphe.patcher.annotation.Patch
import app.morphe.patcher.patch.Patch as MorphePatch
import app.morphe.patcher.patch.context.BytecodePatchContext

@Patch(
    name = "YTMusic Dynamic Light Mode",
    description = "Forces YouTube Music to use a light layout styled dynamically with Android Material You colors.",
    targetApp = "com.google.android.apps.youtube.music"
)
class YtMusicDynamicLightModePatch : MorphePatch<BytecodePatchContext> {

    override fun execute(context: BytecodePatchContext) {
        // STEP 1: Overriding hardcoded Dark Colors in the compiled resources
        // Accessing the resources mapped directly via Morphe's context wrapper
        val colors = context.resources.colors
        
        colors["theme_background_primary"] = 0xFFFAFAFA.toInt()
        colors["theme_background_secondary"] = 0xFFF0F0F0.toInt()
        colors["theme_text_primary"] = 0xFF1A1A1A.toInt()
        colors["theme_text_secondary"] = 0xFF555555.toInt()
        colors["theme_icon_active"] = 0xFF1A1A1A.toInt()

        // STEP 2: Bytecode Injection for Dynamic Material You Colors
        for (classNode in context.classes) {
            if (classNode.name == "com/google/android/apps/youtube/music/activities/MusicActivity") {
                for (method in classNode.methods) {
                    
                    if (method.name == "onCreate" && method.desc == "(Landroid/os/Bundle;)V") {
                        val instructions = method.instructions
                        val iterator = instructions.iterator()
                        
                        while (iterator.hasNext()) {
                            val insn = iterator.next()
                            
                            // Check opcode using raw integer values to bypass missing ASM library references
                            if (insn.opcode == 183) { // 183 is the bytecode value for INVOKESPECIAL
                                val methodInsn = insn as org.objectweb.asm.tree.MethodInsnNode
                                if (methodInsn.name == "onCreate") {
                                    
                                    val injection = org.objectweb.asm.tree.InsnList().apply {
                                        add(org.objectweb.asm.tree.VarInsnNode(25, 0)) // 25 is ALOAD
                                        add(org.objectweb.asm.tree.MethodInsnNode(
                                            184, // 184 is INVOKESTATIC
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
