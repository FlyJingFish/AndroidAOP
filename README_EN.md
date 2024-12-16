<h4 align="right">
  <strong>English</strong> | <a href="https://github.com/FlyJingFish/AndroidAOP/blob/master/README.md">简体中文</a>
</h4>

<div align="center">
    <a href = "https://flyjingfish.github.io/AndroidAOP/"><img src="https://github.com/FlyJingFish/AndroidAOP/blob/master/docs/assets/webp/anim_css_image_pos.svg" width="200" height="200"/></a>
</div>

<p align="center">
  <strong>
    🔥🔥🔥Help you transform into an Android platform framework with AOP architecture
    <a href="https://flyjingfish.github.io/AndroidAOP/">AndroidAOP</a>
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

It is recommended to click **Docs** below to directly view the documentation with better experience

- en [English](https://github.com/FlyJingFish/AndroidAOP/blob/master/README_EN.md)&emsp;[Docs](https://flyjingfish.github.io/AndroidAOP/)
- zh_CN [简体中文](https://github.com/FlyJingFish/AndroidAOP/blob/master/README.md)&emsp;[Docs](https://flyjingfish.github.io/AndroidAOP/zh/)

# Brief Description

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;This is a framework that helps Android App transform into AOP architecture. With just one annotation, you can request permissions, switch threads, prohibit multiple clicks, monitor all click events at once, monitor the life cycle, etc. You can also customize your own Aop code without using AspectJ.


## Special feature

1 . This library has built-in some aspect annotations commonly used in development for you to use.

2 . This library supports you to make aspects by yourself, and the syntax is simple and easy to use.

3 . This library supports Java and Kotlin code simultaneously

4 . This library supports switching into third-party libraries

5 . This library supports the case where the pointcut method is a Lambda expression.

6 . This library supports coroutine functions whose pointcut methods are suspend.

7 . This library supports generating Json files of all pointcut information to facilitate an overview of all pointcut locations [Configure here](#4-add-the-androidaopconfig-configuration-item-in-apps-buildgradle-this-step-is-an-optional-configuration-item)

**8 . This library supports debug rapid development mode, allowing you to package at almost the same speed**

**9 . This library supports component-based development mode**

**10. This library is pure static weaving into AOP code**

**11. This library is not implemented based on AspectJ. The amount of woven code is very small and the intrusion is extremely low**

**12. Rich and complete usage documentation helps you fully understand the usage rules of this library [click here to go to the wiki document](https://flyjingfish.github.io/AndroidAOP)**

**13. There are also plug-in assistants that help you generate section codes for your use [click here to download](https://flyjingfish.github.io/AndroidAOP/AOP_Helper)**

#### [Click here to download apk, or scan the QR code below to download](https://github.com/FlyJingFish/AndroidAOP/blob/master/apk/product/release/app-product-release.apk?raw=true)

<img src="/docs/screenshot/qrcode.png" alt="show" width="200px" />

### Version restrictions

Minimum Gradle version: 7.6👇

<img src="/docs/screenshot/gradle_version.png" alt="show" />


Minimum SDK version: minSdkVersion >= 21

## Star trend chart

[![Stargazers over time](https://starchart.cc/FlyJingFish/AndroidAOP.svg?variant=adaptive)](https://starchart.cc/FlyJingFish/AndroidAOP)

---

## Steps for usage

**Can I give the project a Star before starting? Thank you very much, your support is my only motivation. Welcome Stars and Issues!**

For the full version of the document, please click [AndroidAOP](https://flyjingfish.github.io/AndroidAOP/zh/)

### 1. Introduce the plug-in, choose one of the two methods below (required)

#### Method 1: ```apply``` method

<p align = "left">    
<picture>
  <!-- 亮色模式下显示的 SVG -->
  <source srcset="https://github.com/FlyJingFish/AndroidAOP/blob/master/docs/svg/one.svg" media="(prefers-color-scheme: light)">
  <!-- 暗黑模式下显示的 SVG -->
  <source srcset="https://github.com/FlyJingFish/AndroidAOP/blob/master/docs/svg/one_dark.svg" media="(prefers-color-scheme: dark)">
  <!-- 默认图片 -->
  <img src="https://github.com/FlyJingFish/AndroidAOP/blob/master/docs/svg/one.svg" align = "center"  width="22" height="22" />
</picture>
Depend on the plug-in in <code>build.gradle</code> in the <strong>project root directory</strong>
</p>  

- Using the **plugins DSL**:
  ```gradle
  
  plugins {
      //Required item 👇 apply is set to true to automatically apply debugMode to all modules, if false, follow step 5 below.
      id "io.github.FlyJingFish.AndroidAop.android-aop" version "2.3.3" apply true
  }
  ```
  <details>
  <summary><strong>Using legacy plugin application:</strong></summary>
       
  ```gradle
  buildscript {
       dependencies {
           //Required items 👇
           classpath 'io.github.FlyJingFish.AndroidAop:android-aop-plugin:2.3.3'
       }
  }
  //👇Add this sentence to automatically apply debugMode to all modules. If not, follow step 5 below.
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
 Add in <code>build.gradle</code> of <strong>app</strong>
</p>  

- Using the **plugins DSL**:
  ```gradle
  //Required items 👇
  plugins {
       ...
       id 'android.aop'//It is best to put it on the last line
  }
  ```
  
  
  <details>
  <summary><strong>Using legacy plugin application:</strong></summary>
  
  ```gradle
  //Required items 👇
  apply plugin: 'android.aop' //It's best to put it on the last line
  ```
  
  </details>

#### ~~Method 2: ```plugins``` method~~

- Add directly to ```build.gradle``` of **app**

  ```gradle
  //Required items 👇
  plugins {
       ...
       id "io.github.FlyJingFish.AndroidAop.android-aop" version "2.3.3"
  }
  ```





### 2. If you need to customize aspects, and the code is ```Kotlin``` (optional)

- Depend on the plug-in in ```build.gradle``` in the **project root directory**

```gradle
plugins {
     //Optional 👇, if you need to customize aspects and use the android-aop-ksp library, you need to configure it. The version number below is determined according to the Kotlin version of your project
     id 'com.google.devtools.ksp' version '1.8.0-1.0.9' apply false
}
```
[List of matching version numbers for Kotlin and KSP Github](https://github.com/google/ksp/releases)

### 3. Introduce dependent libraries (required)

```gradle
plugins {
     //Optional 👇, if you need to customize aspects and use the android-aop-ksp library, you need to configure it
     id 'com.google.devtools.ksp'
}

dependencies {
     //Required items 👇
     implementation 'io.github.FlyJingFish.AndroidAop:android-aop-core:2.3.3'
     //Optional 👇This package provides some common annotation aspects
     implementation 'io.github.FlyJingFish.AndroidAop:android-aop-extra:2.3.3'
    
     //Required item 👇If you already have this item in your project, you don’t need to add it.
     implementation 'androidx.appcompat:appcompat:1.3.0' // At least in 1.3.0 and above
     
     //Optional 👇, if you want to customize aspects, you need to use them, ⚠️supports aspects written in Java and Kotlin code
     ksp 'io.github.FlyJingFish.AndroidAop:android-aop-ksp:2.3.3'
     //Optional 👇, if you want to customize aspects, you need to use them, ⚠️only applies to aspects written in Java code
     annotationProcessor 'io.github.FlyJingFish.AndroidAop:android-aop-processor:2.3.3'
     //⚠️Choose one of the above android-aop-ksp and android-aop-processor
}
```
> [!TIP]\
> 1、ksp or annotationProcessor can only scan the current module. Custom aspect codes are added to the module where they are located. **But custom aspect codes are globally effective**; required dependencies can be added only to public modules through the API. <br>
> ["android-aop-extra" usage tutorial](https://flyjingfish.github.io/AndroidAOP/android_aop_extra/)

### 4. Add the androidAopConfig configuration item in app’s build.gradle (this step is an optional configuration item)

[Click here to see how to configure](https://flyjingfish.github.io/AndroidAOP/getting_started/#4-add-the-androidaopconfig-configuration-item-in-apps-buildgradle-this-step-is-an-optional-configuration-item)

### 5. The code weaving method can be set during development (this step is an optional configuration item)

[Click here to see how to configure](https://flyjingfish.github.io/AndroidAOP/getting_started/#5-the-code-weaving-method-can-be-set-during-development-this-step-is-an-optional-configuration-item)

### This library has some built-in functional annotations for you to use directly.

| Annotation name          |                                                                            Parameter description                                                                            |                                                                          Function description                                                                           |
|--------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
| @SingleClick             |                                                              value = interval of quick clicks, default 1000ms                                                               |                                      Click the annotation and add this annotation to make your method accessible only when clicked                                      |
| @DoubleClick             |                                                           value = maximum time between two clicks, default 300ms                                                            |                                   Double-click annotation, add this annotation to make your method enterable only when double-clicked                                   |
| @IOThread                |                                                                          ThreadType = thread type                                                                           |                      Switch to the sub-thread operation. Adding this annotation can switch the code in your method to the sub-thread for execution                      |
| @MainThread              |                                                                                No parameters                                                                                |                The operation of switching to the main thread. Adding this annotation can switch the code in your method to the main thread for execution                |
| @OnLifecycle<sup>*</sup> |                                                                           value = Lifecycle.Event                                                                           |              Monitor life cycle operations. Adding this annotation allows the code in your method to be executed only during the corresponding life cycle               |
| @TryCatch                |                                                                        value = a flag you customized                                                                        |                                                Adding this annotation can wrap a layer of try catch code for your method                                                |
| @Permission<sup>*</sup>  |                                                                     value = String array of permissions                                                                     |                 The operation of applying for permissions. Adding this annotation will enable your code to be executed only after obtaining permissions                 |
| @Scheduled               | initialDelay = delayed start time<br>interval = interval<br>repeatCount = number of repetitions<br>isOnMainThread = whether to be the main thread<br>id = unique identifier |       Scheduled tasks, add this annotation to make your method Executed every once in a while, call AndroidAop.shutdownNow(id) or AndroidAop.shutdown(id) to stop       |
| @Delay                   |                                          delay = delay time<br>isOnMainThread = whether the main thread<br>id = unique identifier                                           | Delay task, add this annotation to delay the execution of your method for a period of time, call AndroidAop.shutdownNow(id) or AndroidAop .shutdown(id) can be canceled |
| @CheckNetwork            |                   tag = custom tag<br>toastText = toast prompt when there is no network<br>invokeListener = whether to take over the check network logic                    |                       Check whether the network is available, adding this annotation will allow your method to enter only when there is a network                       |
| @CustomIntercept         |                                                            value = a flag of a string array that you customized                                                             |                                          Custom interception, used with AndroidAop.setOnCustomInterceptListener, is a panacea                                           |

> [!TIP]\
> The above functions are located in the `android-aop-extra` library. [For detailed instructions, please see the documentation](https://flyjingfish.github.io/AndroidAOP/android_aop_extra/)


## Custom Aspects

### This library uses the following five annotations to implement custom aspects

- @AndroidAopPointCut is an aspect that annotates methods
- @AndroidAopMatchClassMethod is the aspect of matching class methods
- @AndroidAopReplaceClass is called by the replacement method
- @AndroidAopModifyExtendsClass is a modified inherited class
- @AndroidAopCollectMethod Is a collection inheritance class

#### 1. **@AndroidAopPointCut** is used to make aspects in the form of annotations on the method. The above annotations are all made through this. [Wiki documentation](https://flyjingfish.github.io/AndroidAOP/AndroidAopPointCut)


- Create annotations(You need to implement the BasePointCut interface, and fill in the annotations above for its generic type)

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
<summary><strong>Java writing method:</strong></summary>

```java
@AndroidAopPointCut(CustomInterceptCut.class)
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomIntercept {
    String[] value() default {};
}
```
</details>

- Create a class that annotates the aspect (needs to implement the BasePointCut interface, and fill in the above annotation with its generic type)

```kotlin
class CustomInterceptCut : BasePointCut<CustomIntercept> {
     override fun invoke(
         joinPoint: ProceedJoinPoint,
         annotation: CustomIntercept //annotation is the annotation you add to the method
     ): Any? {
         //Write your logic here
         // joinPoint.proceed() means to continue executing the logic of the point-cut method. If this method is not called, the code in the point-cut method will not be executed.
         // About ProceedJoinPoint, you can see the wiki document, click the link below for details
         return joinPoint.proceed()
     }
}
```

- use

Directly add the annotation you wrote to any method, for example, to onCustomIntercept(). When onCustomIntercept() is called, it will first enter the invoke method of CustomInterceptCut mentioned above.

```kotlin
@CustomIntercept("I am custom data")
fun onCustomIntercept(){
    
}

```

[This library has some built-in functional annotations for you to use directly](https://flyjingfish.github.io/AndroidAOP/android_aop_extra/)

#### 2. **@AndroidAopMatchClassMethod** is used to match aspects of a certain class and its corresponding method, [Wiki documentation](https://flyjingfish.github.io/AndroidAOP/AndroidAopMatchClassMethod)


If you want to Hook all onClicks of android.view.View.OnClickListener, to put it bluntly, you want to globally monitor all click events of OnClickListener. The code is as follows:

```kotlin
@AndroidAopMatchClassMethod(
     targetClassName = "android.view.View.OnClickListener",
     methodName = ["onClick"],
     type = MatchType.EXTENDS //type must be EXTENDS because you want to hook all classes that inherit OnClickListener
)
class MatchOnClick : MatchClassMethod {
// @SingleClick(5000) //Combined with @SingleClick, add multi-point prevention to all clicks, 6 is not 6
     override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
         Log.e("MatchOnClick", "======invoke=====$methodName")
         return joinPoint.proceed()
     }
}
```


#### 3. **@AndroidAopReplaceClass** is used for replacement method calls, [Wiki documentation](https://flyjingfish.github.io/AndroidAOP/AndroidAopReplaceClass)

This method is a supplement to @AndroidAopMatchClassMethod

- Kotlin

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

<details>
<summary>Java</summary>

```java
@AndroidAopReplaceClass(
         "android.widget.Toast"
)
public class ReplaceToast {
    @AndroidAopReplaceMethod(
            "android.widget.Toast makeText(android.content.Context, java.lang.CharSequence, int)"
    )
    //  Because the replaced method is static, the parameter type and order correspond to the replaced method one-to-one.
    public static Toast makeText(Context context, CharSequence text, int duration) {
        return Toast.makeText(context, "ReplaceToast-"+text, duration);
    }
    @AndroidAopReplaceMethod(
            "void setGravity(int , int , int )"
    )
    //  Because the replaced method is not a static method, the first parameter is the replaced class, and the subsequent parameters correspond to the replaced method one-to-one.
    public static void setGravity(Toast toast,int gravity, int xOffset, int yOffset) {
        toast.setGravity(Gravity.CENTER, xOffset, yOffset);
    }
    @AndroidAopReplaceMethod(
            "void show()"
    )
    //  Although the replaced method has no parameters, because it is not a static method, the first parameter is still the replaced class.
    public static void show(Toast toast) {
        toast.show();
    }
}
```
</details>

#### 4. **@AndroidAopModifyExtendsClass** is an inherited class that modifies the target class[Wiki documentation](https://flyjingfish.github.io/AndroidAOP/AndroidAopModifyExtendsClass)

Usually, you replace one layer in the inheritance relationship of a class, then rewrite some functions, and add some logic code you want to add to the rewritten functions to monitor and rewrite the original logic.

```java
@AndroidAopModifyExtendsClass("androidx.appcompat.widget.AppCompatImageView")
public class ReplaceImageView extends ImageView {
    public ReplaceImageView(@NonNull Context context) {
        super(context);
    }
    public ReplaceImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ReplaceImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
        //做一些监测或者再次修改
    }
}
```

#### 5. **@AndroidAopCollectMethod** is a aspects that collects inherited classes of a class [Wiki documentation](https://flyjingfish.github.io/AndroidAOP/AndroidAopCollectMethod)

It is extremely simple to use, the sample code has already explained

- Kotlin

```kotlin
object InitCollect {
     private val collects = mutableListOf<SubApplication>()

     @AndroidAopCollectMethod
     @JvmStatic
     fun collect(sub: SubApplication){
       collects.add(sub)
     }
  
     // Call this method directly. The collects collection contains data.
     fun init(application: Application){
         for (collect in collects) {
             collect.onCreate(application)
         }
     }
}
```


<details>
<summary>Java</summary>

```java
public class InitCollect2 {
  private static List<SubApplication2> collects = new ArrayList<>();
  @AndroidAopCollectMethod
  public static void collect(SubApplication2 sub){
    collects.add(sub);
  }

  // Call this method directly. The collects collection contains data.
  public static void init(Application application){
    Log.e("InitCollect2","----init----");
    for (SubApplication2 collect : collects) {
      collect.onCreate(application);
    }
  }
}
```
</details>



#### [Obfuscation rules](https://flyjingfish.github.io/AndroidAOP/About_obfuscation)

> The library comes with `proguard-rules.pro` rules and is automatically imported. Normally no manual import is required.
> You can also go here to view [proguard-rules](https://github.com/FlyJingFish/AndroidAOP/blob/master/android-aop-core/proguard-rules.pro)



### Appreciation

You’ve all seen it here. If you like AndroidAOP, or feel that AndroidAOP has helped you, you can click “Star” in the upper right corner to support it. Your support is my motivation, thank you~ 😃

If you feel that AndroidAOP has saved you a lot of development time and added luster to your project, you can also scan the QR code below and invite the author for a cup of coffee ☕

#### [Donation List](https://flyjingfish.github.io/AndroidAOP/zh/give_list/)

<div>
<img src="/docs/screenshot/IMG_4075.PNG" width="280" height="350">
<img src="/docs/screenshot/IMG_4076.JPG" width="280" height="350">
</div>

If you comment on the name in the donation message, it will be recorded in the list~ If you are also a GitHub open source author, you can leave the GitHub project address or personal homepage address when donating, and the link will be added to the list.

### Contact information

* If you have any questions, you can join the group to communicate [QQ: 641697838](https://qm.qq.com/cgi-bin/qm/qr?k=w2qDbv_5bpLl0lO0qjXxijl3JHCQgtXx&jump_from=webapi&authKey=Q6/YB+7q9BvOGbYv1qXZGAZLigsfwaBxDC8kz03/5Pwy7018XunUcHoC11kVLqCb)

<img src="/docs/screenshot/qq.jpg" width="220"/>

### Finally, I recommend some other libraries I wrote

- [OpenImage makes it easy to click on a small image in the application to view the animated enlargement effect of the large image](https://github.com/FlyJingFish/OpenImage)

- [ShapeImageView supports displaying any graphics, you can’t think of it without it](https://github.com/FlyJingFish/ShapeImageView)

- [GraphicsDrawable supports displaying arbitrary graphics, but is more lightweight](https://github.com/FlyJingFish/GraphicsDrawable)

- [ModuleCommunication solves the communication needs between modules and has more convenient router functions](https://github.com/FlyJingFish/ModuleCommunication)

- [FormatTextViewLib supports bolding, italics, size, underline, and strikethrough for some text. The underline supports custom distance, color, and line width; supports adding network or local images](https://github.com/FlyJingFish/FormatTextViewLib )

- [View more open source libraries on the homepage](https://github.com/FlyJingFish)
