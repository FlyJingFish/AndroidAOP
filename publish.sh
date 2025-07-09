#!/bin/bash

set -e  # 有错误就退出

echo "📦 开始执行 aaBumpVersion1..."
./gradlew aaBumpVersion1

echo "🚀 开始执行 publishToMavenCentral..."
./gradlew publishToMavenCentral

echo "📦 开始执行 aaBumpVersion2..."
./gradlew aaBumpVersion2