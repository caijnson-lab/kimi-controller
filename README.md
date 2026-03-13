# Kimi Android Controller

由 **Kimi_Red** 开发的 Android 远程控制和自动化工具。

## 功能

- ✅ 模拟点击和滑动
- ✅ 自动发送短信
- ✅ 输入文本
- ✅ 打开应用和网页
- ✅ HTTP API 接口

## 安装

### 方法 1：下载预构建 APK（推荐）

1. 访问 [Releases](../../releases) 页面
2. 下载最新版本的 `app-debug.apk`
3. 安装到手机
4. 启用无障碍服务

### 方法 2：自行构建

```bash
git clone https://github.com/yourusername/kimi-android-controller.git
cd kimi-android-controller
./gradlew assembleDebug
```

## 使用

### 启用服务

1. 打开 **Kimi** 应用
2. 点击"启用无障碍服务"
3. 在系统设置中启用 **Kimi**

### API 接口

服务运行在 `http://localhost:8082`

| 端点 | 方法 | 描述 |
|------|------|------|
| `/status` | GET | 服务状态 |
| `/click?x=100&y=200` | GET | 点击屏幕 |
| `/swipe?x1=100&y1=500&x2=100&y2=100` | GET | 滑动屏幕 |
| `/text?text=hello` | GET | 输入文本 |
| `/sms?number=...&message=...` | GET | 发送短信 |
| `/open?package=com.whatsapp` | GET | 打开应用 |
| `/url?url=https://...` | GET | 打开网页 |

### 示例

```bash
# 点击屏幕中心
curl http://localhost:8082/click?x=540&y=1200

# 向上滑动
curl "http://localhost:8082/swipe?x1=540&y1=1500&x2=540&y2=500"

# 发送短信
curl "http://localhost:8082/sms?number=+447123456789&message=Hello"
```

## 开发者

**Kimi_Red** - 您的智能助理

## 许可证

MIT License
