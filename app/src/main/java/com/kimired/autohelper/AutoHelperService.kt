package com.kimired.autohelper

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.telephony.SmsManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ClickRequest(val x: Int, val y: Int)

@Serializable
data class SwipeRequest(val x1: Int, val y1: Int, val x2: Int, val y2: Int, val duration: Int = 300)

@Serializable
data class SmsRequest(val number: String, val message: String)

@Serializable
data class TypeRequest(val text: String)

@Serializable
data class ApiResponse(val success: Boolean, val message: String = "")

class AutoHelperService : AccessibilityService() {

    private var httpServer: io.ktor.server.engine.ApplicationEngine? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onServiceConnected() {
        super.onServiceConnected()
        startHttpServer()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 处理无障碍事件
    }

    override fun onInterrupt() {
        // 服务中断
    }

    override fun onDestroy() {
        super.onDestroy()
        stopHttpServer()
    }

    private fun startHttpServer() {
        serviceScope.launch {
            httpServer = embeddedServer(CIO, port = 8082) {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }

                routing {
                    get("/") {
                        call.respond(ApiResponse(true, "AutoHelper Service Running"))
                    }

                    get("/status") {
                        call.respond(ApiResponse(true, "Service Active"))
                    }

                    post("/click") {
                        try {
                            val request = call.receive<ClickRequest>()
                            val result = performClick(request.x, request.y)
                            call.respond(ApiResponse(result, if (result) "Clicked at (${request.x}, ${request.y})" else "Click failed"))
                        } catch (e: Exception) {
                            call.respond(ApiResponse(false, e.message ?: "Unknown error"))
                        }
                    }

                    post("/swipe") {
                        try {
                            val request = call.receive<SwipeRequest>()
                            val result = performSwipe(request.x1, request.y1, request.x2, request.y2, request.duration)
                            call.respond(ApiResponse(result, if (result) "Swiped" else "Swipe failed"))
                        } catch (e: Exception) {
                            call.respond(ApiResponse(false, e.message ?: "Unknown error"))
                        }
                    }

                    post("/sms") {
                        try {
                            val request = call.receive<SmsRequest>()
                            val result = sendSms(request.number, request.message)
                            call.respond(ApiResponse(result, if (result) "SMS sent" else "SMS failed"))
                        } catch (e: Exception) {
                            call.respond(ApiResponse(false, e.message ?: "Unknown error"))
                        }
                    }

                    post("/type") {
                        try {
                            val request = call.receive<TypeRequest>()
                            val result = typeText(request.text)
                            call.respond(ApiResponse(result, if (result) "Text typed" else "Type failed"))
                        } catch (e: Exception) {
                            call.respond(ApiResponse(false, e.message ?: "Unknown error"))
                        }
                    }

                    get("/screen") {
                        val info = getScreenInfo()
                        call.respond(info)
                    }
                }
            }.start(wait = false)
        }
    }

    private fun stopHttpServer() {
        httpServer?.stop(1000, 2000)
    }

    private fun performClick(x: Int, y: Int): Boolean {
        val path = Path()
        path.moveTo(x.toFloat(), y.toFloat())
        path.lineTo(x.toFloat(), y.toFloat())

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
            .build()

        return dispatchGesture(gesture, null, null)
    }

    private fun performSwipe(x1: Int, y1: Int, x2: Int, y2: Int, duration: Int): Boolean {
        val path = Path()
        path.moveTo(x1.toFloat(), y1.toFloat())
        path.lineTo(x2.toFloat(), y2.toFloat())

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, duration.toLong()))
            .build()

        return dispatchGesture(gesture, null, null)
    }

    private fun sendSms(number: String, message: String): Boolean {
        return try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(number, null, message, null, null)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun typeText(text: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val focusedNode = findFocusedNode(rootNode) ?: return false

        val arguments = Bundle()
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        return focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
    }

    private fun findFocusedNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isFocused) return node

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val focused = findFocusedNode(child)
            if (focused != null) return focused
        }
        return null
    }

    private fun getScreenInfo(): Map<String, Any> {
        val rootNode = rootInActiveWindow
        return mapOf(
            "packageName" to (rootNode?.packageName?.toString() ?: "unknown"),
            "windowId" to (rootNode?.windowId ?: -1),
            "childCount" to (rootNode?.childCount ?: 0)
        )
    }
}
