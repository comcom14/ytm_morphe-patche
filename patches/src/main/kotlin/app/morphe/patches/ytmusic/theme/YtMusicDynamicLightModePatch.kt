package app.morphe.patches.ytmusic.theme

import app.morphe.patcher.patch.patch

val YtMusicDynamicLightModePatch = patch {
    name = "YTMusic Dynamic Light Mode"
    description = "Forces YouTube Music to use a light layout styled dynamically with Android Material You colors."
    targetApp = "com.google.android.apps.youtube.music"

    execute { context ->
        // STEP 1: Overriding hardcoded Dark Colors in the compiled resources
        val colors = context.layoutContext.resources.colors
        
        colors["theme_background_primary"] = 0xFFFAFAFA.toInt()
        colors["theme_background_secondary"] = 0xFFF0F0F0.toInt()
        colors["theme_text_primary"] = 0xFF1A1A1A.toInt()
        colors["theme_text_secondary"] = 0xFF555555.toInt()
        colors["theme_icon_active"] = 0xFF1A1A1A.toInt()

        // STEP 2: Bytecode Injection for Dynamic Material You Colors
        for (classNode in context.bytecodeContext.classes) {
            if (classNode.name == "com/google/android/apps/youtube/music/activities/MusicActivity") {
                for (method in classNode.methods) {
                    
                    if (method.name == "onCreate" && method.desc == "(Landroid/os/Bundle;)V") {
                        val instructions = method.instructions
                        val iterator = instructions.iterator()
                        
                        while (iterator.hasNext()) {
                            val insn = iterator.next()
                            
                            if (insn.opcode == 183) { // INVOKESPECIAL
                                val methodInsn = insn as org.objectweb.asm.tree.MethodInsnNode
                                if (methodInsn.name == "onCreate") {
                                    
                                    val injection = org.objectweb.asm.tree.InsnList().apply {
                                        add(org.objectweb.asm.tree.VarInsnNode(25, 0)) // ALOAD
                                        add(org.objectweb.asm.tree.MethodInsnNode(
                                            184, // INVOKESTATIC
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
