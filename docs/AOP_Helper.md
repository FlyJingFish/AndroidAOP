### 1. Description

> This "assistant" is a plug-in for Android Studio, which is equivalent to a small helper when you use this library. It only helps you generate some AOP code, and has no effect on your code.

The plug-in generates AOP auxiliary code for the target class, including the following functions:

- @AndroidAopReplaceClass
- @AndroidAopMatchClassMethod
- @AndroidAopModifyExtendsClass
- @AndroidAopCollectMethod

Although there is such a plug-in, you also need to understand how to use this library to identify and select the generated code, and don't copy it blindly~

### 2. Install the plug-in

- [Plugin Market](https://plugins.jetbrains.com/plugin/25179-androidaop-code-viewer), search for the plug-in **AndroidAOP Code Viewer** in Android Studio and install it

- 👆The plug-in market needs to be reviewed and may not be the latest version ![](https://img.shields.io/jetbrains/plugin/v/25179?label=%E6%8F%92%E4%BB%B6%E5%B8%82%E5%9C%BA%E6%9C%80%E6%96%B0%E7%89%88%E6%9C%AC&color=blue&style=flat)

- [Click here to download the plugin](https://github.com/FlyJingFish/AndroidAOPPlugin/blob/master/out/artifacts/AndroidAOPPlugin_jar/AndroidAOPPlugin.jar?raw=true), then search for how to install the local plugin
- 👆Download link here to keep up with the latest features ![](https://img.shields.io/github/v/tag/FlyJingFish/AndroidAOPPlugin?label=%E5%B0%9D%E9%B2%9C%E7%89%88%E6%9C%AC&color=red&style=flat)

- After installation, a plug-in named AOPCode will be displayed on the right side of the IDE

### 3. Use

Right-click the mouse on the code you want to cut into -> Click AndroidAOP Code -> Click AOPCode on the right to view the generated code, as shown in the figure:

![about](https://github.com/user-attachments/assets/e168ac99-2951-4f95-8474-e1ea895b6306)

### 4. Special instructions

- The generated `@AndroidAopReplaceClass`, `@AndroidAopReplaceMethod` and The class name and function signature in the `@AndroidAopMatchClassMethod` code are absolutely correct ([Please correct me if you have any questions](https://github.com/FlyJingFish/AndroidAOP/issues/new?assignees=&labels=bug&projects=&template=%E6%8F%90%E4%BA%A4%E6%96%B0%E7%9A%84bug.md&title=)).
- The generated `@AndroidAopReplaceMethod` Java method does not include the suspend function of the Kotlin source code
- The generated `@AndroidAopReplaceMethod` method may have some deviations, so you need to compare it yourself. For example: `nullable?`, `Is it the type of Kotlin source code`, `variable parameter type becomes array type`, etc., these cannot be guaranteed to be accurately copied