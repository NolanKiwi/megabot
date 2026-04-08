package com.megabot.engine

import android.content.Context
import org.mozilla.javascript.Context as RhinoContext
import org.mozilla.javascript.Function
import org.mozilla.javascript.ScriptableObject
import com.megabot.engine.api.*

/**
 * Wraps Mozilla Rhino JS engine with sandboxed execution
 * and MegaBot API bindings.
 */
class ScriptEngine(private val appContext: Context) {

    private var rhinoContext: RhinoContext? = null
    private var scope: ScriptableObject? = null
    private var responseFunction: Function? = null

    // API instances exposed to scripts
    private val botApi = BotApi(appContext)
    private val httpApi = HttpApi()
    private val fileApi = FileApi(appContext)
    private val databaseApi = DatabaseApi(appContext)
    private val deviceApi = DeviceApi(appContext)
    private val securityApi = SecurityApi()
    private val phoneApi = PhoneApi(appContext)
    private val smsApi = SmsApi(appContext)

    fun compile(code: String, scriptName: String) {
        // Clean up previous context
        cleanup()

        rhinoContext = RhinoContext.enter().apply {
            // MUST be -1 for Android (no JIT/dynamic class loading)
            optimizationLevel = -1
            languageVersion = RhinoContext.VERSION_ES6
            instructionObserverThreshold = 1_000_000
        }

        val ctx = rhinoContext!!
        scope = ScriptSandbox.createSandboxedScope(ctx)

        // Inject API objects into JS scope
        injectApis(ctx, scope!!)

        // Compile and execute the script (defines functions)
        val compiled = ctx.compileString(code, scriptName, 1, null)
        compiled.exec(ctx, scope)

        // Look for the response function
        val responseFn = scope!!.get("response", scope)
        if (responseFn is Function) {
            responseFunction = responseFn
        }
    }

    fun executeResponse(
        room: String,
        msg: String,
        sender: String,
        isGroupChat: Boolean,
        packageName: String,
        imageBase64: String? = null
    ) {
        val ctx = rhinoContext ?: throw IllegalStateException("Script not compiled")
        val sc = scope ?: throw IllegalStateException("Script not compiled")
        val fn = responseFunction ?: return // No response function defined

        val replier = botApi.createReplier(packageName, room)

        val args = arrayOf<Any?>(
            room,
            msg,
            sender,
            isGroupChat,
            replier,
            imageBase64 ?: RhinoContext.getUndefinedValue(),
            packageName
        )

        fn.call(ctx, sc, sc, args)
    }

    /**
     * Test execution: captures reply via callback instead of sending to NotificationListener.
     */
    fun executeResponseTest(
        room: String,
        msg: String,
        sender: String,
        isGroupChat: Boolean,
        packageName: String,
        onReply: (String) -> Unit
    ) {
        val ctx = rhinoContext ?: throw IllegalStateException("Script not compiled")
        val sc = scope ?: throw IllegalStateException("Script not compiled")
        val fn = responseFunction ?: return

        val testReplier = TestReplier(onReply)

        val args = arrayOf<Any?>(
            room, msg, sender, isGroupChat, testReplier,
            RhinoContext.getUndefinedValue(), packageName
        )

        fn.call(ctx, sc, sc, args)
    }

    /** Replier used in test mode — captures reply text via callback */
    class TestReplier(private val onReply: (String) -> Unit) {
        fun reply(message: String): Boolean {
            onReply(message)
            return true
        }
        @Suppress("UNUSED_PARAMETER")
        fun reply(room: String, message: String): Boolean {
            onReply(message)
            return true
        }
    }

    private fun injectApis(ctx: RhinoContext, scope: ScriptableObject) {
        fun put(name: String, obj: Any) {
            ScriptableObject.putProperty(scope, name, RhinoContext.javaToJS(obj, scope))
        }

        put("Bot", botApi)
        put("Http", httpApi)
        put("FileStream", fileApi)
        put("DataBase", databaseApi)
        put("Device", deviceApi)
        put("Security", securityApi)
        put("Phone", phoneApi)
        put("Sms", smsApi)

        // Convenience globals
        put("Log", LogApi())

        // setTimeout / setInterval stubs
        ctx.evaluateString(scope, """
            var __timers = [];
            function setTimeout(fn, delay) {
                var id = java.lang.Thread(new java.lang.Runnable({ run: function() {
                    java.lang.Thread.sleep(delay || 0);
                    fn();
                }}));
                id.start();
                __timers.push(id);
                return __timers.length - 1;
            }
        """.trimIndent(), "timers", 1, null)
    }

    private fun cleanup() {
        try {
            RhinoContext.exit()
        } catch (_: Exception) {}
        rhinoContext = null
        scope = null
        responseFunction = null
    }
}
