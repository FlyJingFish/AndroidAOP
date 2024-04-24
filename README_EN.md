# AndroidAOP

[![Maven central](https://img.shields.io/maven-central/v/io.github.FlyJingFish.AndroidAop/android-aop-core)](https://central.sonatype.com/search?q=io.github.FlyJingFish.AndroidAop)
[![GitHub stars](https://img.shields.io/github/stars/FlyJingFish/AndroidAop.svg)](https://github.com/FlyJingFish/AndroidAop/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/FlyJingFish/AndroidAop.svg)](https://github.com/FlyJingFish/AndroidAop/network/members)
[![GitHub issues](https://img.shields.io/github/issues/FlyJingFish/AndroidAop.svg)](https://github.com/FlyJingFish/AndroidAop/issues)
[![GitHub license](https://img.shields.io/github/license/FlyJingFish/AndroidAop.svg)](https://github.com/FlyJingFish/AndroidAop/blob/master/LICENSE)

### AndroidAOP is an Aop framework exclusive to Android. With just one annotation, you can request permissions, switch threads, prohibit multiple points, monitor life cycles, etc. **This library is not Aop implemented based on AspectJ**, of course you can You can customize your own Aop code. It’s better to act than to think, so use it quickly.

## Special feature

1. This library has built-in some aspect annotations commonly used in development for you to use.

2. This library supports you to make aspects by yourself, and the syntax is simple and easy to use.

3. This library supports Java and Kotlin code simultaneously

4. This library supports switching into third-party libraries

5. This library supports the case where the pointcut method is a Lambda expression.

6. This library supports coroutine functions whose pointcut methods are suspend-modified.

7. This library supports generating Json files of all cut-point information to facilitate an overview of all cut-point locations [Configure here](https://github.com/FlyJingFish/AndroidAOP#%E5%9B%9B%E5%9C%A8-app-%E7%9A%84buildgradle%E6%B7%BB%E5%8A%A0-androidaopconfig-%E9%85%8D%E7%BD%AE%E9%A1%B9%E6%AD%A4%E6%AD%A5%E4%B8%BA%E5%8F%AF%E9%80%89%E9%85%8D%E7%BD%AE%E9%A1%B9)

**8. This library is not implemented based on AspectJ. The amount of woven code is very small and the intrusion is extremely low**


#### [Click here to download apk, or scan the QR code below to download](https://github.com/FlyJingFish/AndroidAOP/blob/master/apk/release/app-release.apk?raw=true)

<img src="/screenshot/qrcode.png" alt="show" />

### Version restrictions

Minimum Gradle version: 7.6👇

<img src="/screenshot/gradle_version.png" alt="show" height="220px" />


Minimum SDK version: minSdkVersion >= 21

## Star trend chart

[![Stargazers over time](https://starchart.cc/FlyJingFish/AndroidAOP.svg?variant=adaptive)](https://starchart.cc/FlyJingFish/AndroidAOP)

---

## Steps for usage

**Can I give the project a Star before starting? Thank you very much, your support is my only motivation. Welcome Stars and Issues!**

#### 1. Introduce the plug-in, choose one of the two methods below (required)

##### Method 1: ```plugins``` method

Add directly to ```build.gradle``` of **app**

```gradle
//Required items 👇
plugins {
     ...
     id "io.github.FlyJingFish.AndroidAop.android-aop" version "1.5.8"
}
```

##### Method 2: ```apply``` method

1. Depend on the plug-in in ```build.gradle``` in the **project root directory**

new version

```gradle

plugins {
    //必须项 👇
    id "io.github.FlyJingFish.AndroidAop.android-aop" version "1.5.1" apply false
}
```

<details>
<summary><strong>or old version</strong></summary>
     
```gradle
buildscript {
     dependencies {
         //Required items 👇
         classpath 'io.github.FlyJingFish.AndroidAop:android-aop-plugin:1.5.8'
     }
}
```
</details>

2. Add in ```build.gradle``` of **app**

new version
```gradle
//Required items 👇
plugins {
     ...
     id 'android.aop'//It is best to put it on the last line
}
```


<details>
<summary><strong>or old version</strong></summary>
     
```gradle
//Required items 👇
apply plugin: 'android.aop' //It's best to put it on the last line
```

</details>



#### 2. If you need to customize aspects, and the code is ```Kotlin``` (optional)

1. Depend on the plug-in in ```build.gradle``` in the **project root directory**

```gradle
plugins {
     //Optional 👇, if you need to customize aspects and use the android-aop-ksp library, you need to configure it. The version number below is determined according to the Kotlin version of your project
     id 'com.google.devtools.ksp' version '1.8.0-1.0.9' apply false
}
```
[List of matching version numbers for Kotlin and KSP Github](https://github.com/google/ksp/releases)

#### 3. Introduce dependent libraries (required)

```gradle
plugins {
     //Optional 👇, if you need to customize aspects and use the android-aop-ksp library, you need to configure it
     id 'com.google.devtools.ksp'
}

dependencies {
     //Required items 👇
     implementation 'io.github.FlyJingFish.AndroidAop:android-aop-core:1.5.8'
     implementation 'io.github.FlyJingFish.AndroidAop:android-aop-annotation:1.5.8'
     //Optional 👇, if you want to customize aspects, you need to use them, ⚠️supports aspects written in Java and Kotlin code
     ksp 'io.github.FlyJingFish.AndroidAop:android-aop-ksp:1.5.8'
     //Optional 👇, if you want to customize aspects, you need to use them, ⚠️only applies to aspects written in Java code
     annotationProcessor 'io.github.FlyJingFish.AndroidAop:android-aop-processor:1.5.8'
     //⚠️Choose one of the above android-aop-ksp and android-aop-processor
}
```
**Tips: ksp or annotationProcessor only works in the current module. In whichever module there is custom aspect code, it will be added to that module. Required dependencies can only be added to the public module through the API**

#### 4. Add the androidAopConfig configuration item in app’s build.gradle (this step is an optional configuration item)

```gradle
plugins {
     ...
}
androidAopConfig {
     // enabled is false, the aspect no longer works, the default is not written as true
     enabled true
     // include does not set all scans by default. After setting, only the code of the set package name will be scanned.
     include 'Package name of your project', 'Package name of custom module', 'Package name of custom module'
     // exclude is the package excluded during scanning
     // Can exclude kotlin related and improve speed
     exclude 'kotlin.jvm', 'kotlin.internal','kotlinx.coroutines.internal', 'kotlinx.coroutines.android'
    
     // verifyLeafExtends Whether to turn on verification leaf inheritance, it is turned on by default. If type = MatchType.LEAF_EXTENDS of @AndroidAopMatchClassMethod is not set, it can be turned off.
     verifyLeafExtends true
     //Off by default, if enabled in Build or after packaging, the point cut information json file will be generated in app/build/tmp/cutInfo.json
     cutInfoJson false
     //It is enabled by default. After setting false, there will be no incremental compilation effect. Filter (keyword: AndroidAOP woven info code) build output log viewable time
    increment true
}
android {
     ...
}
```
**Tip: Reasonable use of include and exclude can improve compilation speed. It is recommended to directly use include to set the relevant package names of your project (including app and custom module)**

**⚠️⚠️⚠️After setting include and exclude, all aspects are only valid within the rules you set. Please remember your settings! **

**In addition, since Android Studio may have cache after setting this, it is recommended to restart AS and clean the project before continuing development**

#### 5. The code weaving method can be set during development (this step is an optional configuration item)

- **1. Please set the above [Step 1](#%E4%B8%80%E5%BC%95%E5%85%A5%E6%8F%92%E4%BB%B6% for all sub-module modules E4%B8%8B%E8%BE%B9%E4%B8%A4%E7%A7%8D%E6%96%B9%E5%BC%8F%E4%BA%8C%E9%80%89%E4% B8%80%E5%BF%85%E9%A1%BB), for example: **
```gradle
plugins {
     ...
     id 'android.aop'//It is best to put it on the last line
}
```
- **2. Add the following settings to `gradle.properties` in the root directory**

```
androidAop.debugMode=true //Set to true to default to the current packaging method of your project, false to use the full packaging method
```

**⚠️⚠️⚠️Please note that when set to true, some functions will be disabled. Only the aop code will be woven into the set module, and the code will not be woven into the third-party jar package**

### This library has some built-in functional annotations for you to use directly.

| Annotation name  |                                                                            Parameter description                                                                            |                                                                          Function description                                                                           |
|------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
| @SingleClick     |                                                              value = interval of quick clicks, default 1000ms                                                               |                                      Click the annotation and add this annotation to make your method accessible only when clicked                                      |
| @DoubleClick     |                                                           value = maximum time between two clicks, default 300ms                                                            |                                   Double-click annotation, add this annotation to make your method enterable only when double-clicked                                   |
| @IOThread        |                                                                          ThreadType = thread type                                                                           |                      Switch to the sub-thread operation. Adding this annotation can switch the code in your method to the sub-thread for execution                      |
| @MainThread      |                                                                                No parameters                                                                                |                The operation of switching to the main thread. Adding this annotation can switch the code in your method to the main thread for execution                |
| @OnLifecycle     |                                                                           value = Lifecycle.Event                                                                           |              Monitor life cycle operations. Adding this annotation allows the code in your method to be executed only during the corresponding life cycle               |
| @TryCatch        |                                                                        value = a flag you customized                                                                        |                                                Adding this annotation can wrap a layer of try catch code for your method                                                |
| @Permission      |                                                                     value = String array of permissions                                                                     |                 The operation of applying for permissions. Adding this annotation will enable your code to be executed only after obtaining permissions                 |
| @Scheduled       | initialDelay = delayed start time<br>interval = interval<br>repeatCount = number of repetitions<br>isOnMainThread = whether to be the main thread<br>id = unique identifier |       Scheduled tasks, add this annotation to make your method Executed every once in a while, call AndroidAop.shutdownNow(id) or AndroidAop.shutdown(id) to stop       |
| @Delay           |                                          delay = delay time<br>isOnMainThread = whether the main thread<br>id = unique identifier                                           | Delay task, add this annotation to delay the execution of your method for a period of time, call AndroidAop.shutdownNow(id) or AndroidAop .shutdown(id) can be canceled |
| @CheckNetwork    |                   tag = custom tag<br>toastText = toast prompt when there is no network<br>invokeListener = whether to take over the check network logic                    |                       Check whether the network is available, adding this annotation will allow your method to enter only when there is a network                       |
| @CustomIntercept |                                                            value = a flag of a string array that you customized                                                             |                                          Custom interception, used with AndroidAop.setOnCustomInterceptListener, is a panacea                                           |

[All examples of the above annotations are here](https://github.com/FlyJingFish/AndroidAOP/blob/master/app/src/main/java/com/flyjingfish/androidaop/MainActivity.kt#L128),[Also This](https://github.com/FlyJingFish/AndroidAOP/blob/master/app/src/main/java/com/flyjingfish/androidaop/SecondActivity.java#L64)

### Let me emphasize this @OnLifecycle

- **1. The object to which the method added by @OnLifecycle must belong is a method directly or indirectly inherited from FragmentActivity or Fragment to be useful, or the object annotated method can also implement LifecycleOwner**
- 2. If the first point is not met, you can set the first parameter of the aspect method to the type of point 1, and you can also pass it in when calling the aspect method, for example:

```java
public class StaticClass {
     @SingleClick(5000)
     @OnLifecycle(Lifecycle.Event.ON_RESUME)
     public static void onStaticPermission(MainActivity activity, int maxSelect, ThirdActivity.OnPhotoSelectListener back){
         back.onBack();
     }

}
```


### Let’s focus on @TryCatch @Permission @CustomIntercept @CheckNetwork

- @TryCatch Using this annotation you can set the following settings (not required)
```java
AndroidAop.INSTANCE.setOnThrowableListener(new OnThrowableListener() {
     @Nullable
     @Override
     public Object handleThrowable(@NonNull String flag, @Nullable Throwable throwable,TryCatch tryCatch) {
         // TODO: 2023/11/11 If an exception occurs, you can handle it accordingly according to the flag you passed in at the time. If you need to rewrite the return value, just return at return
         return 3;
     }
});
```

- @Permission Use of this annotation must match the following settings (⚠️This step is required, otherwise it will have no effect)
```java
AndroidAop.INSTANCE.setOnPermissionsInterceptListener(new OnPermissionsInterceptListener() {
     @SuppressLint("CheckResult")
     @Override
     public void requestPermission(@NonNull ProceedJoinPoint joinPoint, @NonNull Permission permission, @NonNull OnRequestPermissionListener call) {
         Object target = joinPoint.getTarget();
         if (target instanceof FragmentActivity){
             RxPermissions rxPermissions = new RxPermissions((FragmentActivity) target);
             rxPermissions.request(permission.value()).subscribe(call::onCall);
         }else if (target instanceof Fragment){
             RxPermissions rxPermissions = new RxPermissions((Fragment) target);
             rxPermissions.request(permission.value()).subscribe(call::onCall);
         }else{
             // TODO: target is not FragmentActivity or Fragment, which means the method where the annotation is located is not among them. Please handle this situation yourself.
             // Suggestion: The first parameter of the pointcut method can be set to FragmentActivity or Fragment, and then joinPoint.args[0] can be obtained
         }
     }
});
```

- @CustomIntercept To use this annotation you must match the following settings (⚠️This step is required, otherwise what’s the point?)
```java
AndroidAop.INSTANCE.setOnCustomInterceptListener(new OnCustomInterceptListener() {
    @Nullable
    @Override
    public Object invoke(@NonNull ProceedJoinPoint joinPoint, @NonNull CustomIntercept customIntercept) {
        // TODO: 2023/11/11 在此写你的逻辑 在合适的地方调用 joinPoint.proceed()，
        //  joinPoint.proceed(args)可以修改方法传入的参数，如果需要改写返回值，则在 return 处返回即可

        return null;
    }
});
```
- @CheckNetwork Using this annotation you can match the following settings (not required)
```java
AndroidAop.INSTANCE.setOnCheckNetworkListener(new OnCheckNetworkListener() {
     @Nullable
     @Override
     public Object invoke(@NonNull ProceedJoinPoint joinPoint, @NonNull CheckNetwork checkNetwork, boolean availableNetwork) {
         return null;
     }
});
```
When using invokeListener, set it to true to enter the callback above.
```kotlin
@CheckNetwork(invokeListener = true)
fun toSecondActivity(){
     startActivity(Intent(this,SecondActivity::class.java))
}
```
In addition, the built-in Toast allows you to take over
```java
AndroidAop.INSTANCE.setOnToastListener(new OnToastListener() {
     @Override
     public void onToast(@NonNull Context context, @NonNull CharSequence text, int duration) {
        
     }
});
```

👆The above three monitors are best placed in your application

## In addition, this library also supports you to make aspects by yourself, which is very simple to implement!

### This library uses the following four annotations to implement custom aspects

- @AndroidAopPointCut is an aspect that annotates methods
- @AndroidAopMatchClassMethod is the aspect of matching class methods
- @AndroidAopReplaceClass is called by the replacement method
- @AndroidAopModifyExtendsClass is a modified inherited class

#### 1. **@AndroidAopPointCut** is used to make aspects in the form of annotations on the method. The above annotations are all made through this. [Please see the wiki document for detailed usage](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopPointCut)


The following uses @CustomIntercept as an example to introduce how to use it.

- Create annotations(You need to implement the BasePointCut interface, and fill in the annotations above for its generic type)

```java
@AndroidAopPointCut(CustomInterceptCut.class)
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomIntercept {
    String[] value() default {};
}
```
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

[About ProceedJoinPoint usage instructions](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint), the same applies to ProceedJoinPoint below

- use

Directly add the annotation you wrote to any method, for example, to onCustomIntercept(). When onCustomIntercept() is called, it will first enter the invoke method of CustomInterceptCut mentioned above.

```kotlin
@CustomIntercept("I am custom data")
fun onCustomIntercept(){
    
}

```

#### 2. **@AndroidAopMatchClassMethod** is used to match aspects of a certain class and its corresponding method.

**The matching method supports accurate matching, [click here to see detailed usage documentation on the wiki](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopMatchClassMethod)**

- Example 1

```java
package com.flyjingfish.test_lib;

public class TestMatch {
     public void test1(int value1,String value2){

     }

     public String test2(int value1,String value2){
         return value1+value2;
     }
}

```

If TestMatch is the class to be matched, and you want to match the test2 method, the following is how to write the match:


```kotlin
package com.flyjingfish.test_lib.mycut;

@AndroidAopMatchClassMethod(
         targetClassName = "com.flyjingfish.test_lib.TestMatch",
         methodName = ["test2"],
         type = MatchType.SELF
)
class MatchTestMatchMethod : MatchClassMethod {
   override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
     Log.e("MatchTestMatchMethod","======"+methodName+",getParameterTypes="+joinPoint.getTargetMethod().getParameterTypes().length);
     //Write your logic here
     //If you don’t want to execute the original method logic, 👇 don’t call the following sentence
     return joinPoint.proceed()
   }
}

```

You can see that the type set by AndroidAopMatchClassMethod above is MatchType.SELF, which means that it only matches the TestMatch class itself, regardless of its subclasses.
- Example 2

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

You can see that the type set by AndroidAopMatchClassMethod above is MatchType.EXTENDS, which means matching all subclasses inherited from OnClickListener. For more inheritance methods, [please refer to the Wiki document](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopMatchClassMethod#excludeclasses-%E6%98%AF%E6%8E%92%E9%99%A4%E6%8E%89%E7%BB%A7%E6%89%BF%E5%85%B3%E7%B3%BB%E4%B8%AD%E7%9A%84%E4%B8%AD%E9%97%B4%E7%B1%BB%E6%95%B0%E7%BB%84)

**⚠️Note: If the subclass does not have this method, the aspect will be invalid. In addition, do not match the same method multiple times in the same class, otherwise only one will take effect**

#### Practical scenarios for matching aspects:

- For example, if you want to log out of the login logic, you can use the above method. Just jump within the page to detect whether you need to log out.

- Or if you want to set an aspect on a method of a third-party library, you can directly set the corresponding class name, corresponding method, and then type = MatchType.SELF. This can invade the code of the third-party library. Of course, remember to modify the above mentioned Configuration of androidAopConfig

#### 3. **@AndroidAopReplaceClass** is used for replacement method calls

@AndroidAopReplaceClass and @AndroidAopReplaceMethod are used together

**Detailed usage of replacement method call, [click here to see detailed usage documentation in wiki](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopReplaceClass)**

- Java writing method
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
- Kotlin writing method
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

#### 4. **@AndroidAopModifyExtendsClass** is an inherited class that modifies the target class[Detailed usage](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopModifyExtendsClass)

Usually, you replace one layer in the inheritance relationship of a certain class, then rewrite some functions, and add some logic code you want to the rewritten functions to monitor and rewrite the original logic.

As shown in the following example, you need to replace the inherited class of ```AppCompatImageView``` with ```ReplaceImageView```

Application scenario: non-invasively implement the function of monitoring large image loading

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


### common problem

1. Build reports an error "ZipFile invalid LOC header (bad signature)"

- Please restart Android Studio and clean the project


2. How to deal with multiple annotations or matching aspects for the same method?

- When multiple aspects are superimposed on a method, annotations take precedence over matching aspects (the matching aspects above), and the annotation aspects are executed sequentially from top to bottom.
- Call **[proceed](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint)** to execute the next aspect, and the last aspect among multiple aspects will be executed **[proceed](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint)** will call the code in the cut-in method
- Call **[proceed(args)](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint)** in the previous aspect to pass in the parameters that can be updated, and the previous aspect will also be obtained in the next aspect One layer of updated parameters
- When there is an asynchronous call [proceed](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint), the first asynchronous call [proceed](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint) ) The return value of the aspect (that is, the return value of invoke) is the return value of the cut-in method;


#### Obfuscation rules

> The library comes with `proguard-rules.pro` rules and is automatically imported. Normally no manual import is required.
> You can also go here to view [proguard-rules](https://github.com/FlyJingFish/AndroidAOP/blob/master/android-aop-core/proguard-rules.pro)



### Appreciation

You’ve all seen it here. If you like AndroidAOP, or feel that AndroidAOP has helped you, you can click “Star” in the upper right corner to support it. Your support is my motivation, thank you~ 😃

If you feel that AndroidAOP has saved you a lot of development time and added luster to your project, you can also scan the QR code below and invite the author for a cup of coffee ☕

<div>
<img src="/screenshot/IMG_4075.PNG" width="280" height="350">
<img src="/screenshot/IMG_4076.JPG" width="280" height="350">
</div>

### Contact information

* If you have any questions, you can join the group to communicate [QQ: 641697838](https://qm.qq.com/cgi-bin/qm/qr?k=w2qDbv_5bpLl0lO0qjXxijl3JHCQgtXx&jump_from=webapi&authKey=Q6/YB+7q9BvOGbYv1qXZGAZLigsfwaBxDC8kz03/5Pwy7018XunUcHoC11kVLqCb)

<img src="/screenshot/qq.png" width="220"/>

### Finally, I recommend some other libraries I wrote

- [OpenImage makes it easy to click on a small image in the application to view the animated enlargement effect of the large image](https://github.com/FlyJingFish/OpenImage)

- [ShapeImageView supports displaying any graphics, you can’t think of it without it](https://github.com/FlyJingFish/ShapeImageView)

- [FormatTextViewLib supports bolding, italics, size, underline, and strikethrough for some text. The underline supports custom distance, color, and line width; supports adding network or local images](https://github.com/FlyJingFish/FormatTextViewLib )

- [View more open source libraries on the homepage](https://github.com/FlyJingFish)
