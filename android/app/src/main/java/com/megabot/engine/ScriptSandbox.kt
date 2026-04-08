package com.megabot.engine

import org.mozilla.javascript.ClassShutter
import org.mozilla.javascript.Context
import org.mozilla.javascript.ScriptableObject

/**
 * Security sandbox for Rhino scripts.
 * Blocks access to dangerous Java classes while allowing
 * only the MegaBot API bindings.
 */
object ScriptSandbox {

    /** Java classes/packages allowed in script scope */
    private val ALLOWED_CLASSES = setOf(
        // MegaBot APIs
        "com.megabot.engine.api.BotApi",
        "com.megabot.engine.api.BotApi\$Replier",
        "com.megabot.engine.api.HttpApi",
        "com.megabot.engine.api.HttpApi\$HttpResponse",
        "com.megabot.engine.api.FileApi",
        "com.megabot.engine.api.DatabaseApi",
        "com.megabot.engine.api.DeviceApi",
        "com.megabot.engine.api.SecurityApi",
        "com.megabot.engine.api.PhoneApi",
        "com.megabot.engine.api.SmsApi",
        "com.megabot.engine.api.LogApi",
        // Basic Java types needed for interop
        "java.lang.String",
        "java.lang.Boolean",
        "java.lang.Integer",
        "java.lang.Long",
        "java.lang.Double",
        "java.lang.Float",
        "java.lang.Object",
        "java.lang.Thread",
        "java.lang.Runnable",
        "java.util.HashMap",
        "java.util.ArrayList",
    )

    /** Prefixes that are always blocked */
    private val BLOCKED_PREFIXES = listOf(
        "java.io.",
        "java.nio.",
        "java.net.",
        "java.lang.Runtime",
        "java.lang.Process",
        "java.lang.System",
        "java.lang.reflect.",
        "java.lang.Class",
        "android.",
        "dalvik.",
        "com.android.",
    )

    fun createSandboxedScope(ctx: Context): ScriptableObject {
        // Set ClassShutter to restrict Java class access
        ctx.classShutter = ClassShutter { className ->
            // Block dangerous prefixes first
            if (BLOCKED_PREFIXES.any { className.startsWith(it) }) {
                return@ClassShutter false
            }
            // Allow whitelisted classes
            className in ALLOWED_CLASSES
        }

        // Create standard scope with safety
        val scope = ctx.initSafeStandardObjects()
        return scope
    }
}
