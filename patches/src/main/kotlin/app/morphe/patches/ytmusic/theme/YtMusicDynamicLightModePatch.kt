package app.morphe.patches.ytmusic.theme

class YtMusicDynamicLightModePatch {
    val name: String = "YTMusic Dynamic Light Mode"
    val description: String = "Forces YouTube Music to use a light layout styled dynamically with Android Material You colors."
    val targetApp: String = "com.google.android.apps.youtube.music"

    fun execute(context: Any) {
        val ctxClass = context.javaClass
        
        // STEP 1: Overriding hardcoded Dark Colors
        try {
            val layoutContext = ctxClass.getMethod("getLayoutContext").invoke(context) ?: return
            val resources = layoutContext.javaClass.getMethod("getResources").invoke(layoutContext) ?: return
            val colors = resources.javaClass.getMethod("getColors").invoke(resources) as? MutableMap<*, *> ?: return
            
            val colorsMap = colors as MutableMap<String, Int>
            colorsMap["theme_background_primary"] = 0xFFFAFAFA.toInt()
            colorsMap["theme_background_secondary"] = 0xFFF0F0F0.toInt()
            colorsMap["theme_text_primary"] = 0xFF1A1A1A.toInt()
            colorsMap["theme_text_secondary"] = 0xFF555555.toInt()
            colorsMap["theme_icon_active"] = 0xFF1A1A1A.toInt()
        } catch (e: Exception) {
            // Ignore resource errors
        }

        // STEP 2: Bytecode Injection
        try {
            val bytecodeContext = ctxClass.getMethod("getBytecodeContext").invoke(context) ?: return
            val classes = bytecodeContext.javaClass.getMethod("getClasses").invoke(bytecodeContext) as? Iterable<*> ?: return
            
            for (classNode in classes) {
                if (classNode == null) continue
                val nameField = classNode.javaClass.getField("name").get(classNode) as? String ?: continue
                if (nameField == "com/google/android/apps/youtube/music/activities/MusicActivity") {
                    val methods = classNode.javaClass.getField("methods").get(classNode) as? Iterable<*> ?: continue
                    
                    for (method in methods) {
                        if (method == null) continue
                        val methodName = method.javaClass.getField("name").get(method) as? String ?: continue
                        val methodDesc = method.javaClass.getField("desc").get(method) as? String ?: continue
                        
                        if (methodName == "onCreate" && methodDesc == "(Landroid/os/Bundle;)V") {
                            val instructions = method.javaClass.getField("instructions").get(method) ?: continue
                            val size = instructions.javaClass.getMethod("size").invoke(instructions) as? Int ?: continue
                            
                            for (i in 0 until size) {
                                val insn = instructions.javaClass.getMethod("get", Int::class.java).invoke(instructions, i) ?: continue
                                val opcode = insn.javaClass.getMethod("getOpcode").invoke(insn) as? Int ?: continue
                                
                                if (opcode == 183) { // INVOKESPECIAL
                                    val insnName = insn.javaClass.getField("name").get(insn) as? String ?: continue
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
            // Ignore structure errors
        }
    }
}
