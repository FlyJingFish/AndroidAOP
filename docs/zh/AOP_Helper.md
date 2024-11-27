### 1、说明

> 这个“助手”是 Android Studio 的插件，相当于你使用本库时的一个小帮手，只是帮你生成一些 AOP 的代码，除此之外对你的代码没有任何作用

插件为目标类生成AOP辅助代码，包含以下功能：

- @AndroidAopReplaceClass
- @AndroidAopMatchClassMethod
- @AndroidAopModifyExtendsClass
- @AndroidAopCollectMethod

虽然有这样的插件，但也需要你了解本库的使用方法，来甄别挑选生成的代码，不要无脑复制～

### 2、安装插件

- [插件市场](https://plugins.jetbrains.com/plugin/25179-androidaop-code-viewer)，在 Android Studio 中搜索插件 **AndroidAOP Code Viewer** 安装即可
    - 👆插件市场需要审核不一定是最新版本 ![](https://img.shields.io/jetbrains/plugin/v/25179?label=%E6%8F%92%E4%BB%B6%E5%B8%82%E5%9C%BA%E6%9C%80%E6%96%B0%E7%89%88%E6%9C%AC&color=blue&style=flat)


- [点此下载插件](https://github.com/FlyJingFish/AndroidAOPPlugin/blob/master/out/artifacts/AndroidAOPPlugin_jar/AndroidAOPPlugin.jar?raw=true)，然后自行搜索如何安装本地插件
    - 👆此处下载链接保持最新功能 ![](https://img.shields.io/github/v/tag/FlyJingFish/AndroidAOPPlugin?label=%E5%B0%9D%E9%B2%9C%E7%89%88%E6%9C%AC&color=red&style=flat)

- 安装后 IDE 右侧会显示出名为 AOPCode 的插件

### 3、使用

在你想要切入的代码上右击鼠标 -> 点击 AndroidAOP Code -> 右侧点击AOPCode查看生成的代码，如图：

- 如果不能出现AOP代码，你可以尝试找到对应的 class 文件再去执行该步骤进行尝试

![about](https://github.com/user-attachments/assets/e168ac99-2951-4f95-8474-e1ea895b6306)

### 4、特别说明

- 生成的 `@AndroidAopReplaceClass`、`@AndroidAopReplaceMethod` 和 `@AndroidAopMatchClassMethod` 代码中的类名和函数签名都是绝对正确的（[如有问题欢迎指正](https://github.com/FlyJingFish/AndroidAOP/issues/new?assignees=&labels=bug&projects=&template=%E6%8F%90%E4%BA%A4%E6%96%B0%E7%9A%84bug.md&title=)）。
- 生成的 `@AndroidAopReplaceMethod` Java 方法不包括 Kotlin 源代码的 suspend 函数
- 生成的 `@AndroidAopReplaceMethod` 方法有可能有所偏差，需要你亲自比对一下。如: `可空 ?`、`是否Kotlin源代码的类型`、`可变参数类型变成数组类型` 等等，这些是无法保证准确复制的
