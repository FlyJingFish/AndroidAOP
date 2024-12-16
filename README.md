<h4 align="right">
  <strong>简体中文</strong> | <a href="https://github.com/FlyJingFish/AndroidAOP/blob/master/README_EN.md">English</a>
</h4>

<div align="center">
    <a href = "https://flyjingfish.github.io/AndroidAOP/zh/"><img src="https://github.com/FlyJingFish/AndroidAOP/blob/master/docs/assets/webp/anim_css_image_pos.svg" width="200" height="200"/></a>
</div>

<p align="center">
  <strong>
    🔥🔥🔥帮助 Android App 改造成AOP架构的框架
    <a href="https://flyjingfish.github.io/AndroidAOP/zh/">AndroidAOP</a>
  </strong>
</p>

<p align="center">
  <a href="https://central.sonatype.com/search?q=io.github.FlyJingFish.AndroidAop"><img
    src="https://img.shields.io/maven-central/v/io.github.FlyJingFish.AndroidAop/android-aop-plugin"
    alt="Build"
  /></a>
  <a href="https://github.com/FlyJingFish/AndroidAop/stargazers"><img
    src="https://img.shields.io/github/stars/FlyJingFish/AndroidAop.svg"
    alt="Downloads"
  /></a>
  <a href="https://github.com/FlyJingFish/AndroidAop/network/members"><img
    src="https://img.shields.io/github/forks/FlyJingFish/AndroidAop.svg"
    alt="Python Package Index"
  /></a>
  <a href="https://github.com/FlyJingFish/AndroidAop/issues"><img
    src="https://img.shields.io/github/issues/FlyJingFish/AndroidAop.svg"
    alt="Docker Pulls"
  /></a>
  <a href="https://github.com/FlyJingFish/AndroidAop/blob/master/LICENSE"><img
    src="https://img.shields.io/github/license/FlyJingFish/AndroidAop.svg"
    alt="Sponsors"
  /></a>
</p>


# README.md

建议直接点击下边的 **Docs** 直接看体验更好的文档

- en [English](https://github.com/FlyJingFish/AndroidAOP/blob/master/README_EN.md)&emsp;[Docs](https://flyjingfish.github.io/AndroidAOP/)
- zh_CN [简体中文](https://github.com/FlyJingFish/AndroidAOP/blob/master/README.md)&emsp;[Docs](https://flyjingfish.github.io/AndroidAOP/zh/)

# 简述

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;这是一个帮助 Android App 改造成AOP架构的框架，只需一个注解就可以请求权限、切换线程、禁止多点、一次监测所有点击事件、监测生命周期等等，没有使用 AspectJ，也可以定制出属于你的 Aop 代码

## 特色功能

1、本库内置了开发中常用的一些切面注解供你使用

2、本库支持让你自己做切面，语法简单易上手

3、本库同步支持 Java项目 和 Kotlin项目

4、本库支持切入三方库

5、本库支持切点方法为 Lambda 表达式的情况

6、本库支持切点方法为 suspend 修饰的协程函数

7、本库支持生成所有切点信息Json文件，方便一览所有切点位置[在此配置](#%E5%9B%9B%E5%9C%A8-app-%E7%9A%84buildgradle%E6%B7%BB%E5%8A%A0-androidaopconfig-%E9%85%8D%E7%BD%AE%E9%A1%B9%E6%AD%A4%E6%AD%A5%E4%B8%BA%E5%8F%AF%E9%80%89%E9%85%8D%E7%BD%AE%E9%A1%B9)

**8、本库支持 debug 快速开发模式，让你打包速度几乎不变[在此配置](#%E4%BA%94%E5%BC%80%E5%8F%91%E4%B8%AD%E5%8F%AF%E8%AE%BE%E7%BD%AE%E4%BB%A3%E7%A0%81%E7%BB%87%E5%85%A5%E6%96%B9%E5%BC%8F%E6%AD%A4%E6%AD%A5%E4%B8%BA%E5%8F%AF%E9%80%89%E9%85%8D%E7%BD%AE%E9%A1%B9%E5%8F%AA%E4%B8%BA%E5%9C%A8%E5%BC%80%E5%8F%91%E8%BF%87%E7%A8%8B%E4%B8%AD%E6%8F%90%E9%AB%98%E6%89%93%E5%8C%85%E9%80%9F%E5%BA%A6)**

**9、本库支持 组件化开发[在此配置](#%E4%BA%94%E5%BC%80%E5%8F%91%E4%B8%AD%E5%8F%AF%E8%AE%BE%E7%BD%AE%E4%BB%A3%E7%A0%81%E7%BB%87%E5%85%A5%E6%96%B9%E5%BC%8F%E6%AD%A4%E6%AD%A5%E4%B8%BA%E5%8F%AF%E9%80%89%E9%85%8D%E7%BD%AE%E9%A1%B9%E5%8F%AA%E4%B8%BA%E5%9C%A8%E5%BC%80%E5%8F%91%E8%BF%87%E7%A8%8B%E4%B8%AD%E6%8F%90%E9%AB%98%E6%89%93%E5%8C%85%E9%80%9F%E5%BA%A6)**

**10、本库是纯静态织入AOP代码**

**11、本库不是基于 AspectJ 实现的，织入代码量极少，侵入性极低**

**12、丰富完善的使用文档助你完全理解本库的使用规则[点此前往wiki文档](https://flyjingfish.github.io/AndroidAOP/zh/)**

**13、更有帮助你生成切面代码的插件助手供你使用[点此前往下载](https://flyjingfish.github.io/AndroidAOP/zh/AOP_Helper/)**


#### [点此下载apk,也可扫下边二维码下载](https://github.com/FlyJingFish/AndroidAOP/blob/master/apk/product/release/app-product-release.apk?raw=true)

<img src="/docs/screenshot/qrcode.png" alt="show" width="200px" />

### 版本限制

最低Gradle版本：7.6👇（支持8.0以上）

<img src="/docs/screenshot/gradle_version.png" alt="show" />

最低SDK版本：minSdkVersion >= 21

## Star趋势图

[![Stargazers over time](https://starchart.cc/FlyJingFish/AndroidAOP.svg?variant=adaptive)](https://starchart.cc/FlyJingFish/AndroidAOP)

---

## 使用步骤

**在开始之前可以给项目一个Star吗？非常感谢，你的支持是我唯一的动力。欢迎Star和Issues!**

![Stargazers over time](https://github.com/FlyJingFish/AndroidAOP/blob/master/docs/screenshot/warning_maven_central.svg)

完整版文档请点击 [AndroidAOP](https://flyjingfish.github.io/AndroidAOP/zh/)

### 一、引入插件，下边两种方式二选一（必须）


#### 方式一：```apply``` 方式（推荐）

<p align = "left">    
<picture>
  <!-- 亮色模式下显示的 SVG -->
  <source srcset="https://github.com/FlyJingFish/AndroidAOP/blob/master/docs/svg/one.svg" media="(prefers-color-scheme: light)">
  <!-- 暗黑模式下显示的 SVG -->
  <source srcset="https://github.com/FlyJingFish/AndroidAOP/blob/master/docs/svg/one_dark.svg" media="(prefers-color-scheme: dark)">
  <!-- 默认图片 -->
  <img src="https://github.com/FlyJingFish/AndroidAOP/blob/master/docs/svg/one.svg" align = "center"  width="22" height="22" />
</picture>
在<strong>项目根目录</strong>的 <code>build.gradle</code> 里依赖插件
</p>  

- 新版本

  ```gradle
  
  plugins {
      //必须项 👇 apply 设置为 true 自动为所有module“预”配置debugMode，false则按下边步骤五的方式二
      id "io.github.FlyJingFish.AndroidAop.android-aop" version "2.3.3" apply true
  }
  ```
  <details>
  <summary><strong>或者老版本</strong></summary>

  ```gradle
    buildscript {
        dependencies {
            //必须项 👇
            classpath 'io.github.FlyJingFish.AndroidAop:android-aop-plugin:2.3.3'
        }
    }
    // 👇加上这句自动为所有module“预”配置debugMode，不加则按下边步骤五的方式二
    apply plugin: "android.aop"
    ```
  </details>

  

<p align = "left">    
<picture>
  <!-- 亮色模式下显示的 SVG -->
  <source srcset="https://github.com/FlyJingFish/AndroidAOP/blob/master/docs/svg/two.svg" media="(prefers-color-scheme: light)">
  <!-- 暗黑模式下显示的 SVG -->
  <source srcset="https://github.com/FlyJingFish/AndroidAOP/blob/master/docs/svg/two_dark.svg" media="(prefers-color-scheme: dark)">
  <!-- 默认图片 -->
  <img src="https://github.com/FlyJingFish/AndroidAOP/blob/master/docs/svg/two.svg" align = "center"  width="22" height="22"/>
</picture>
在<strong>app</strong>的 <code>build.gradle</code> 添加
</p> 

- 新版本

  ```gradle
  //必须项 👇
  plugins {
      ...
      id 'android.aop'//最好放在最后一行
  }
  ```
  
  <details>
  <summary><strong>或者老版本</strong></summary>

  ```gradle
  //必须项 👇
  apply plugin: 'android.aop' //最好放在最后一行
  ```
  </details>

 

> [!CAUTION]\
> **⚠️⚠️⚠️`id 'android.aop'` 这句尽量放在最后一行，尤其是必须在 `id 'com.android.application'` 或 `id 'com.android.library'` 的后边**


#### ~~方式二：```plugins``` 方式（不推荐）~~

- 直接在 **app** 的 ```build.gradle``` 添加

  ```gradle
  //必须项 👇
  plugins {
      ...
      id "io.github.FlyJingFish.AndroidAop.android-aop" version "2.3.3"//最好放在最后一行
  }
  ```

### 二、如果你需要自定义切面，并且代码是 ```Kotlin``` (非必须)

- 在 **项目根目录** 的 ```build.gradle``` 里依赖插件

```gradle
plugins {
    //非必须项 👇，如果需要自定义切面，并且使用 android-aop-ksp 这个库的话需要配置 ，下边版本号根据你项目的 Kotlin 版本决定
    id 'com.google.devtools.ksp' version '1.8.0-1.0.9' apply false
}
```
[Kotlin 和 KSP Github 的匹配版本号列表](https://github.com/google/ksp/releases)

### 三、引入依赖库(必须)

```gradle
plugins {
    //非必须项 👇，如果需要自定义切面，并且使用 android-aop-ksp 这个库的话需要配置 
    id 'com.google.devtools.ksp'
}

dependencies {
    //必须项 👇
    implementation 'io.github.FlyJingFish.AndroidAop:android-aop-core:2.3.3'
    //非必须项 👇这个包提供了一些常见的注解切面
    implementation 'io.github.FlyJingFish.AndroidAop:android-aop-extra:2.3.3'
    
    //必须项 👇如果您项目内已经有了这项不用加也可以
    implementation 'androidx.appcompat:appcompat:1.3.0' // 至少在1.3.0及以上
    
    //非必须项 👇，如果你想自定义切面需要用到，⚠️支持Java和Kotlin代码写的切面
    ksp 'io.github.FlyJingFish.AndroidAop:android-aop-ksp:2.3.3'
    
    //非必须项 👇，如果你想自定义切面需要用到，⚠️只适用于Java代码写的切面
    annotationProcessor 'io.github.FlyJingFish.AndroidAop:android-aop-processor:2.3.3'
    //⚠️上边的 android-aop-ksp 和 android-aop-processor 二选一
}
```

> [!TIP]\
> 1、ksp 或 annotationProcessor只能扫描当前 module ，在哪个 module 中有自定义切面代码就加在哪个 module，**但是自定义的切面代码是全局生效的**；必须依赖项可以通过 api 方式只加到公共 module 上 <br>
> 2、["android-aop-extra" 使用教程](https://flyjingfish.github.io/AndroidAOP/zh/android_aop_extra/)

### 四、在 app 的build.gradle添加 androidAopConfig 配置项（此步为可选配置项）

[点此查看如何配置](https://flyjingfish.github.io/AndroidAOP/zh/getting_started/#app-buildgradle-androidaopconfig)

### 五、开发中可设置代码织入方式（此步为可选配置项，只为在开发过程中提高打包速度）

[点此查看如何配置](https://flyjingfish.github.io/AndroidAOP/zh/getting_started/#_4)


### 本库内置了一些功能注解可供你直接使用

| 注解名称                     |                                                参数说明                                                 |                                        功能说明                                         |
|--------------------------|:---------------------------------------------------------------------------------------------------:|:-----------------------------------------------------------------------------------:|
| @SingleClick             |                                      value = 快速点击的间隔，默认1000ms                                       |                             单击注解，加入此注解，可使你的方法只有单击时才可进入                              |
| @DoubleClick             |                                      value = 两次点击的最大用时，默认300ms                                      |                              双击注解，加入此注解，可使你的方法双击时才可进入                               |
| @IOThread                |                                          ThreadType = 线程类型                                          |                          切换到子线程的操作，加入此注解可使你的方法内的代码切换到子线程执行                          |
| @MainThread              |                                                 无参数                                                 |                          切换到主线程的操作，加入此注解可使你的方法内的代码切换到主线程执行                          |
| @OnLifecycle<sup>*</sup> |                                       value = Lifecycle.Event                                       |                        监听生命周期的操作，加入此注解可使你的方法内的代码在对应生命周期内才去执行                        |
| @TryCatch                |                                        value = 你自定义加的一个flag                                         |                            加入此注解可为您的方法包裹一层 try catch 代码                             |
| @Permission<sup>*</sup>  |                                   tag = 自定义标记<br>value = 权限的字符串数组                                   |                            申请权限的操作，加入此注解可使您的代码在获取权限后才执行                             |
| @Scheduled               | initialDelay = 延迟开始时间<br>interval = 间隔<br>repeatCount = 重复次数<br>isOnMainThread = 是否主线程<br>id = 唯一标识 | 定时任务，加入此注解，可使你的方法每隔一段时间执行一次，调用AndroidAop.shutdownNow(id)或AndroidAop.shutdown(id)可停止 |
| @Delay                   |                         delay = 延迟时间<br>isOnMainThread = 是否主线程<br>id = 唯一标识                         |  延迟任务，加入此注解，可使你的方法延迟一段时间执行，调用AndroidAop.shutdownNow(id)或AndroidAop.shutdown(id)可取消  |
| @CheckNetwork            |                tag = 自定义标记<br>toastText = 无网络时toast提示<br>invokeListener = 是否接管检查网络逻辑                |                            检查网络是否可用，加入此注解可使你的方法在有网络才可进去                             |
| @CustomIntercept         |                                     value = 你自定义加的一个字符串数组的flag                                      |              自定义拦截，配合 AndroidAop.setOnCustomInterceptListener 使用，属于万金油              |


> [!TIP]\
> 以上功能位于 `android-aop-extra` 库中，[详细说明请看文档](https://flyjingfish.github.io/AndroidAOP/zh/android_aop_extra/)


## 自定义切面

**本库通过以下五种注解，实现自定义切面**

本篇介绍是大纲式的大致讲解，[详细点此查看](https://flyjingfish.github.io/AndroidAOP/zh/)

- @AndroidAopPointCut 是为方法加注解的切面
- @AndroidAopMatchClassMethod 是匹配类的方法的切面
- @AndroidAopReplaceClass 是替换方法调用的
- @AndroidAopModifyExtendsClass 是修改继承类
- @AndroidAopCollectMethod 是收集继承类


#### 一、**@AndroidAopPointCut** 是在方法上通过注解的形式做切面的，上述中注解都是通过这个做的，[wiki文档](https://flyjingfish.github.io/AndroidAOP/zh/AndroidAopPointCut)

- 创建注解(将 @AndroidAopPointCut 加到你的注解上)

```kotlin
@AndroidAopPointCut(CustomInterceptCut::class)
@Target(
        AnnotationTarget.FUNCTION,
        AnnotationTarget.PROPERTY_GETTER,
        AnnotationTarget.PROPERTY_SETTER
)
@Retention(
        AnnotationRetention.RUNTIME
)
annotation class CustomIntercept(vararg val value: String = [])
```

<details>
<summary><strong>Java写法:</strong></summary>

```java
@AndroidAopPointCut(CustomInterceptCut.class)
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomIntercept {
    String[] value() default {};
}
```
</details>

- 创建注解处理切面的类（需要实现 BasePointCut 接口，它的泛型填上边的注解）

```kotlin
class CustomInterceptCut : BasePointCut<CustomIntercept> {
    override fun invoke(
        joinPoint: ProceedJoinPoint,
        annotation: CustomIntercept //annotation就是你加到方法上的注解
    ): Any? {
        // 在此写你的逻辑
        // joinPoint.proceed() 表示继续执行切点方法的逻辑，不调用此方法不会执行切点方法里边的代码
        // 关于 ProceedJoinPoint 可以看wiki 文档，详细点击下方链接
        return joinPoint.proceed()
    }
}
```

- 使用

直接将你写的注解加到任意一个方法上，例如加到了 onCustomIntercept() 当 onCustomIntercept() 被调用时首先会进入到上文提到的 CustomInterceptCut 的 invoke 方法上

```kotlin
@CustomIntercept("我是自定义数据")
fun onCustomIntercept(){
    
}

```

[本库内置了一些功能注解可供你直接使用](https://flyjingfish.github.io/AndroidAOP/zh/android_aop_extra/)

#### 二、**@AndroidAopMatchClassMethod** 是做匹配某类及其对应方法的切面的,[wiki文档](https://flyjingfish.github.io/AndroidAOP/zh/AndroidAopMatchClassMethod)

假如想 Hook 所有的 android.view.View.OnClickListener 的 onClick，说白了就是想全局监测所有的设置 OnClickListener 的点击事件，代码如下：

```kotlin
@AndroidAopMatchClassMethod(
    targetClassName = "android.view.View.OnClickListener",
    methodName = ["onClick"],
    type = MatchType.EXTENDS //type 一定是 EXTENDS 因为你想 hook 所有继承了 OnClickListener 的类
)
class MatchOnClick : MatchClassMethod {
//    @SingleClick(5000) //联合 @SingleClick ，给所有点击增加防多点，6不6
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e("MatchOnClick", "=====invoke=====$methodName")
        return joinPoint.proceed()
    }
}
```

#### 三、**@AndroidAopReplaceClass** 是做替换方法调用的，[wiki文档](https://flyjingfish.github.io/AndroidAOP/zh/AndroidAopReplaceClass)

此方式是对 @AndroidAopMatchClassMethod 的一个补充

- Kotlin写法

```kotlin

@AndroidAopReplaceClass("android.util.Log")
object ReplaceLog {
    @AndroidAopReplaceMethod("int e(java.lang.String,java.lang.String)")
    @JvmStatic
    fun e( tag:String, msg:String) :Int{
        return Log.e(tag, "ReplaceLog-$msg")
    }
}


```

该例意思就是凡是代码中写```Log.e```的地方都被替换成```ReplaceLog.e```

<details>
<summary>Java写法</summary>

```java
@AndroidAopReplaceClass(
        "android.widget.Toast"
)
public class ReplaceToast {
    @AndroidAopReplaceMethod(
            "android.widget.Toast makeText(android.content.Context, java.lang.CharSequence, int)"
    )
    //  因为被替换方法是静态的，所以参数类型及顺序和被替换方法一一对应
    public static Toast makeText(Context context, CharSequence text, int duration) {
        return Toast.makeText(context, "ReplaceToast-"+text, duration);
    }
    @AndroidAopReplaceMethod(
            "void setGravity(int , int , int )"
    )
    //  因为被替换方法不是静态方法，所以参数第一个是被替换类，之后的参数和被替换方法一一对应
    public static void setGravity(Toast toast,int gravity, int xOffset, int yOffset) {
        toast.setGravity(Gravity.CENTER, xOffset, yOffset);
    }
    @AndroidAopReplaceMethod(
            "void show()"
    )
    //  虽然被替换方法没有参数，但因为它不是静态方法，所以第一个参数仍然是被替换类
    public static void show(Toast toast) {
        toast.show();
    }
}
```

该例意思就是凡是代码中写```Toast.makeText```和```Toast.show```  ...的地方都被替换成```ReplaceToast.makeText```和```ReplaceToast.show``` ...
</details>


#### 四、**@AndroidAopModifyExtendsClass** 是修改目标类的继承类，[wiki文档](https://flyjingfish.github.io/AndroidAOP/zh/AndroidAopModifyExtendsClass)

通常是在某个类的继承关系中替换掉其中一层，然后重写一些函数，在重写的函数中加入一些你想加的逻辑代码，起到监听、改写原有逻辑的作用


```java
@AndroidAopModifyExtendsClass("androidx.appcompat.widget.AppCompatImageView")
public class ReplaceImageView extends ImageView {
    public ReplaceImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
        //做一些监测或者再次修改
    }
}
```

该例就是要把 ```AppCompatImageView``` 的继承类替换成 ```ReplaceImageView```

#### 五、**@AndroidAopCollectMethod** 是收集继承类，[wiki文档](https://flyjingfish.github.io/AndroidAOP/zh/AndroidAopCollectMethod)

使用起来极其简单，示例代码已经说明了

- Kotlin 写法

```kotlin
object InitCollect {
    private val collects = mutableListOf<SubApplication>()
    private val collectClazz: MutableList<Class<out SubApplication>> = mutableListOf()

    @AndroidAopCollectMethod
    @JvmStatic
    fun collect(sub: SubApplication){
      collects.add(sub)
    }
    @AndroidAopCollectMethod
    @JvmStatic
    fun collect2(sub:Class<out SubApplication>){
      collectClazz.add(sub)
    }
  //直接调这个方法（方法名不限）上边的函数会被悉数回调
    fun init(application: Application){
        for (collect in collects) {
            collect.onCreate(application)
        }
    }
}
```

<details>
<summary>Java写法</summary>

```java
public class InitCollect2 {
  private static final List<SubApplication2> collects = new ArrayList<>();
  private static final List<Class<? extends SubApplication2>> collectClazz = new ArrayList<>();

  @AndroidAopCollectMethod
  public static void collect(SubApplication2 sub) {
    collects.add(sub);
  }

  @AndroidAopCollectMethod
  public static void collect3(Class<? extends SubApplication2> sub) {
    collectClazz.add(sub);
  }

  //直接调这个方法（方法名不限）上边的函数会被悉数回调
  public static void init(Application application) {
    Log.e("InitCollect2", "----init----");
    for (SubApplication2 collect : collects) {
      collect.onCreate(application);
    }
  }
}
```
</details>





#### [关于混淆](https://flyjingfish.github.io/AndroidAOP/zh/About_obfuscation/)

> 此资源库自带[混淆规则](https://github.com/FlyJingFish/AndroidAOP/blob/master/android-aop-core/proguard-rules.pro)，并且会自动导入，正常情况下无需手动导入。



### 赞赏

都看到这里了，如果您喜欢 AndroidAOP，或感觉 AndroidAOP 帮助到了您，可以点右上角“Star”支持一下，您的支持就是我的动力，谢谢～ 😃

如果感觉 AndroidAOP 为您节约了大量开发时间、为您的项目增光添彩，您也可以扫描下面的二维码，请作者喝杯咖啡 ☕

#### [捐赠列表](https://flyjingfish.github.io/AndroidAOP/zh/give_list/)

<div>
<img src="/docs/screenshot/IMG_4075.PNG" width="280" height="350">
<img src="/docs/screenshot/IMG_4076.JPG" width="280" height="350">
</div>

如果在捐赠留言中备注名称，将会被记录到列表中~ 如果你也是github开源作者，捐赠时可以留下github项目地址或者个人主页地址，链接将会被添加到列表中



### 联系方式

* 有问题可以加群大家一起交流 [点此加QQ群：641697838](https://qm.qq.com/cgi-bin/qm/qr?k=w2qDbv_5bpLl0lO0qjXxijl3JHCQgtXx&jump_from=webapi&authKey=Q6/YB+7q9BvOGbYv1qXZGAZLigsfwaBxDC8kz03/5Pwy7018XunUcHoC11kVLqCb)

<img src="/docs/screenshot/qq.jpg" width="220"/>

### 最后推荐我写的另外一些库

- [OpenImage 轻松实现在应用内点击小图查看大图的动画放大效果](https://github.com/FlyJingFish/OpenImage)

- [ShapeImageView 支持显示任意图形，只有你想不到没有它做不到](https://github.com/FlyJingFish/ShapeImageView)

- [GraphicsDrawable 支持显示任意图形，但更轻量](https://github.com/FlyJingFish/GraphicsDrawable)

- [ModuleCommunication 解决模块间的通信需求，更有方便的router功能](https://github.com/FlyJingFish/ModuleCommunication)

- [FormatTextViewLib 支持部分文本设置加粗、斜体、大小、下划线、删除线，下划线支持自定义距离、颜色、线的宽度；支持添加网络或本地图片](https://github.com/FlyJingFish/FormatTextViewLib)

- [主页查看更多开源库](https://github.com/FlyJingFish)

