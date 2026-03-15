package com.kimired.autohelper

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import kotlinx.coroutines.*
import java.io.DataOutputStream
import java.io.IOException

class AutoHelperService : AccessibilityService() {

    companion object {
        const val TAG = "AutoHelperService"
        var instance: AutoHelperService? = null
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val handler = Handler(Looper.getMainLooper())

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.i(TAG, "无障碍服务已连接")
        Toast.makeText(this, "Kimi Controller 已启动", Toast.LENGTH_SHORT).show()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 处理无障碍事件
        when (event?.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                // 窗口状态变化
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                // 视图被点击
            }
            else -> {}
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "服务被中断")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        serviceScope.cancel()
    }

    // 点击坐标
    fun tap(x: Int, y: Int): Boolean {
        return performTap(x, y)
    }

    // 滑动
    fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, duration: Int = 300): Boolean {
        return performSwipe(x1, y1, x2, y2, duration)
    }

    // 输入文本
    fun typeText(text: String): Boolean {
        return try {
            val nodeInfo = findFocusedNode()
            if (nodeInfo != null) {
                val arguments = Bundle()
                arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                Log.i(TAG, "文本输入成功：$text")
                true
            } else {
                Log.e(TAG, "未找到焦点节点")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "文本输入失败", e)
            false
        }
    }

    // 发送短信
    fun sendSms(number: String, message: String): Boolean {
        return try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(number, null, message, null, null)
            Log.i(TAG, "短信发送成功：$number - $message")
            true
        } catch (e: Exception) {
            Log.e(TAG, "短信发送失败", e)
            false
        }
    }

    // 打开应用
    fun openApp(packageName: String): Boolean {
        return try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                Log.i(TAG, "应用已打开：$packageName")
                true
            } else {
                Log.e(TAG, "应用未找到：$packageName")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "打开应用失败", e)
            false
        }
    }

    // 打开 URL
    fun openUrl(url: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = android.net.Uri.parse(url)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            Log.i(TAG, "URL 已打开：$url")
            true
        } catch (e: Exception) {
            Log.e(TAG, "打开 URL 失败", e)
            false
        }
    }

    // 返回
    fun goBack(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_BACK)
    }

    // 主页
    fun goHome(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_HOME)
    }

    // 多任务
    fun goRecent(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_RECENTS)
    }

    // 通知栏
    fun openNotifications(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
    }

    // 锁屏
    fun lockScreen(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
    }

    // 截屏
    fun takeScreenshot(): Boolean {
        return try {
            performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
            Log.i(TAG, "截屏成功")
            true
        } catch (e: Exception) {
            Log.e(TAG, "截屏失败", e)
            false
        }
    }

    // 执行 Shell 命令
    fun executeShell(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec("sh")
            val os = DataOutputStream(process.outputStream)
            os.writeBytes("$command\n")
            os.writeBytes("exit\n")
            os.flush()
            val result = process.inputStream.bufferedReader().use { it.readText() }
            Log.i(TAG, "Shell 命令执行：$command")
            result
        } catch (e: IOException) {
            Log.e(TAG, "Shell 命令执行失败", e)
            ""
        }
    }

    // 查找文本节点
    fun findNodeByText(text: String): AccessibilityNodeInfo? {
        val root = rootInActiveWindow ?: return null
        return findNodeByTextRecursive(root, text)
    }

    private fun findNodeByTextRecursive(node: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        if (node.text?.contains(text, ignoreCase = true) == true) {
            return node
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findNodeByTextRecursive(child, text)
            if (result != null) return result
        }
        return null
    }

    // 查找 ID 节点
    fun findNodeById(id: String): AccessibilityNodeInfo? {
        val root = rootInActiveWindow ?: return null
        return root.findAccessibilityNodeInfosByViewId(id).firstOrNull()
    }

    // 查找焦点节点
    private fun findFocusedNode(): AccessibilityNodeInfo? {
        val root = rootInActiveWindow ?: return null
        return root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT) ?: root
    }

    // 执行点击
    private fun performTap(x: Int, y: Int): Boolean {
        return try {
            val path = Path()
            path.moveTo(x.toFloat(), y.toFloat())
            val gesture = GestureDescription.StrokeDescription(path, 0, 100)
            dispatchGesture(GestureDescription.Builder().addStroke(gesture).build(), null, null)
            Log.i(TAG, "点击执行：($x, $y)")
            true
        } catch (e: Exception) {
            Log.e(TAG, "点击失败", e)
            false
        }
    }

    // 执行滑动
    private fun performSwipe(x1: Int, y1: Int, x2: Int, y2: Int, duration: Int): Boolean {
        return try {
            val path = Path()
            path.moveTo(x1.toFloat(), y1.toFloat())
            path.lineTo(x2.toFloat(), y2.toFloat())
            val gesture = GestureDescription.StrokeDescription(path, 0, duration.toLong())
            dispatchGesture(GestureDescription.Builder().addStroke(gesture).build(), null, null)
            Log.i(TAG, "滑动执行：($x1,$y1) -> ($x2,$y2)")
            true
        } catch (e: Exception) {
            Log.e(TAG, "滑动失败", e)
            false
        }
    }
}
