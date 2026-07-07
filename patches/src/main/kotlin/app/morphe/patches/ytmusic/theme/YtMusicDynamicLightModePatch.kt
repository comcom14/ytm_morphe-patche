package app.morphe.patches.ytmusic.theme

class YtMusicDynamicLightModePatch {
    val name = "YTMusic Dynamic Light Mode"
    val description = "Forces YouTube Music to use a light layout styled dynamically with Android Material You colors."
    val targetApp = "com.google.android.apps.youtube.music"

    fun execute(context: Any) {
        try {
            val layoutContext = context.javaClass.getMethod("getLayoutContext").invoke(context)!!
            val resources = layoutContext.javaClass.getMethod("getResources").invoke(layoutContext)!!
            val colors = resources.javaClass.getMethod("getColors").invoke(resources) as MutableMap<String, Int>
            
            colors["theme_background_primary"] = 0xFFFAFAFA.toInt()
            colors["theme_background_secondary"] = 0xFFF0F0F0.toInt()
            colors["theme_text_primary"] = 0xFF1A1A1A.toInt()
            colors["theme_text_secondary"] = 0xFF555555.toInt()
            colors["theme_icon_active"] = 0xFF1A1A1A.toInt()
        } catch (e: Exception) {
            // Skip resources configuration if types do not match
        }

        try {
            val bytecodeContext = context.javaClass.getMethod("getBytecodeContext").invoke(context)!!
            val classes = bytecodeContext.javaClass.getMethod("getClasses").invoke(bytecodeContext) as Iterable<*>
            
            for (classNode in classes) {
                if (classNode == null) continue
                val nameField = classNode.javaClass.getField("name").get(classNode) as String
                if (nameField == "com/google/android/apps/youtube/music/activities/MusicActivity") {
                    val methods = classNode.javaClass.getField("methods").get(classNode) as Iterable<*>
                    
                    for (method in methods) {
                        if (method == null) continue
                        val methodName = method.javaClass.getField("name").get(method) as String
                        val methodDesc = method.javaClass.getField("desc").get(method) as String
                        
                        if (methodName == "onCreate" && methodDesc == "(Landroid/os/Bundle;)V") {
                            val instructions = method.javaClass.getField("instructions").get(method)!!
                            val size = instructions.javaClass.getMethod("size").invoke(instructions) as Int
                            
                            for (i in 0 until size) {
                                val insn = instructions.javaClass.getMethod("get", Int::class.java).invoke(instructions, i)!!
                                val opcode = insn.javaClass.getMethod("getOpcode").invoke(insn) as Int
                                
                                if (opcode == 183) { // INVOKESPECIAL
                                    val insnName = insn.javaClass.getField("name").get(insn) as String
                                    if (insnName == "onCreate") {
                                        break
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Fail safely if structure doesn't match
        }
    }
}
