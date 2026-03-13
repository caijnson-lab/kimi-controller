#!/bin/bash
# 构建 AutoHelper APK

echo "Building AutoHelper..."
echo "========================"

# 检查是否安装了 Android SDK
if [ -z "$ANDROID_SDK_ROOT" ] && [ -z "$ANDROID_HOME" ]; then
    echo "Error: ANDROID_SDK_ROOT or ANDROID_HOME not set"
    echo "Please install Android SDK first"
    exit 1
fi

# 进入项目目录
cd "$(dirname "$0")"

# 清理构建
./gradlew clean

# 构建 Debug APK
./gradlew assembleDebug

# 检查构建结果
if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo ""
    echo "✅ Build successful!"
    echo "APK location: app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "Install with:"
    echo "  adb install app/build/outputs/apk/debug/app-debug.apk"
else
    echo "❌ Build failed"
    exit 1
fi
