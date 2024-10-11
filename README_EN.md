<h4 align="right">
  <strong>English</strong> | <a href="https://github.com/FlyJingFish/AndroidAOP/blob/master/README.md">简体中文</a>
</h4>

<div align="center">
    <img src="/docs/assets/web_logo.svg" width="200" height="200"/>
</div>

# AndroidAOP

[![Maven central](https://img.shields.io/maven-central/v/io.github.FlyJingFish.AndroidAop/android-aop-plugin)](https://central.sonatype.com/search?q=io.github.FlyJingFish.AndroidAop)
[![GitHub stars](https://img.shields.io/github/stars/FlyJingFish/AndroidAop.svg)](https://github.com/FlyJingFish/AndroidAop/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/FlyJingFish/AndroidAop.svg)](https://github.com/FlyJingFish/AndroidAop/network/members)
[![GitHub issues](https://img.shields.io/github/issues/FlyJingFish/AndroidAop.svg)](https://github.com/FlyJingFish/AndroidAop/issues)
[![GitHub license](https://img.shields.io/github/license/FlyJingFish/AndroidAop.svg)](https://github.com/FlyJingFish/AndroidAop/blob/master/LICENSE)


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

<img src="/screenshot/qrcode.png" alt="show" width="200px" />

### Version restrictions

Minimum Gradle version: 7.6👇

<img src="/screenshot/gradle_version.png" alt="show" />


Minimum SDK version: minSdkVersion >= 21

## Star trend chart

[![Stargazers over time](https://starchart.cc/FlyJingFish/AndroidAOP.svg?variant=adaptive)](https://starchart.cc/FlyJingFish/AndroidAOP)

---

## Steps for usage

**Can I give the project a Star before starting? Thank you very much, your support is my only motivation. Welcome Stars and Issues!**

### 1. Introduce the plug-in, choose one of the two methods below (required)

#### Method 1: ```apply``` method

<p align = "left">    
<picture>
  <!-- 亮色模式下显示的 SVG -->
  <source srcset="https://github.com/FlyJingFish/AndroidAOP/blob/master/svg/one.svg" media="(prefers-color-scheme: light)">
  <!-- 暗黑模式下显示的 SVG -->
  <source srcset="https://github.com/FlyJingFish/AndroidAOP/blob/master/svg/one_dark.svg" media="(prefers-color-scheme: dark)">
  <!-- 默认图片 -->
  <img src="https://github.com/FlyJingFish/AndroidAOP/blob/master/svg/one.svg" align = "center"  width="22" height="22" />
</picture>
Depend on the plug-in in <code>build.gradle</code> in the <strong>project root directory</strong>
</p>  

- Using the **plugins DSL**:
  ```gradle
  
  plugins {
      //Required item 👇 apply is set to true to automatically apply debugMode to all modules, if false, follow step 5 below.
      id "io.github.FlyJingFish.AndroidAop.android-aop" version "2.2.3" apply true
  }
  ```
  <details>
  <summary><strong>Using legacy plugin application:</strong></summary>
       
  ```gradle
  buildscript {
       dependencies {
           //Required items 👇
           classpath 'io.github.FlyJingFish.AndroidAop:android-aop-plugin:2.2.3'
       }
  }
  //👇Add this sentence to automatically apply debugMode to all modules. If not, follow step 5 below.
  apply plugin: "android.aop"
  ```
  </details>

<p align = "left">    
<picture>
  <!-- 亮色模式下显示的 SVG -->
  <source srcset="https://github.com/FlyJingFish/AndroidAOP/blob/master/svg/two.svg" media="(prefers-color-scheme: light)">
  <!-- 暗黑模式下显示的 SVG -->
  <source srcset="https://github.com/FlyJingFish/AndroidAOP/blob/master/svg/two_dark.svg" media="(prefers-color-scheme: dark)">
  <!-- 默认图片 -->
  <img src="https://github.com/FlyJingFish/AndroidAOP/blob/master/svg/two.svg" align = "center"  width="22" height="22"/>
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
       id "io.github.FlyJingFish.AndroidAop.android-aop" version "2.2.3"
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
     implementation 'io.github.FlyJingFish.AndroidAop:android-aop-core:2.2.3'
     //Optional 👇This package provides some common annotation aspects
     implementation 'io.github.FlyJingFish.AndroidAop:android-aop-extra:2.2.3'
    
     //Required item 👇If you already have this item in your project, you don’t need to add it.
     implementation 'androidx.appcompat:appcompat:1.3.0' // At least in 1.3.0 and above
     
     //Optional 👇, if you want to customize aspects, you need to use them, ⚠️supports aspects written in Java and Kotlin code
     ksp 'io.github.FlyJingFish.AndroidAop:android-aop-ksp:2.2.3'
     //Optional 👇, if you want to customize aspects, you need to use them, ⚠️only applies to aspects written in Java code
     annotationProcessor 'io.github.FlyJingFish.AndroidAop:android-aop-processor:2.2.3'
     //⚠️Choose one of the above android-aop-ksp and android-aop-processor
}
```
> [!TIP]\
> Tip: ksp or annotationProcessor can only scan the current module. Custom aspect codes are added to the module where they are located. **But custom aspect codes are globally effective**; required dependencies can be added only to public modules through the API.

### 4. Add the androidAopConfig configuration item in app’s build.gradle (this step is an optional configuration item)

- Related development configurations

```gradle
plugins {
     ...
     id 'android.aop'//It is best to put it on the last line
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
}
android {
     ...
}
```
> [!TIP]\
> **1. Include and exclude support precise setting to a class**<br>
> **2. Reasonable use of include and exclude can improve compilation speed. It is recommended to use include to set the relevant package name of your project (including app and custom module)**<br>
> **3. If LEAF_EXTENDS is not set for @AndroidAopMatchClassMethod and @AndroidAopCollectMethod, setting verifyLeafExtends to false can also speed up**

> [!CAUTION]\
> **⚠️⚠️⚠️After setting include and exclude, all aspects are only valid within the rules you set. Please remember your settings! In addition, since Android Studio may have cache after setting here, it is recommended to clean before continuing development**

### 5. The code weaving method can be set during development (this step is an optional configuration item)

**The following configuration steps also apply to componentized scenarios [Click here to view](https://flyjingfish.github.io/AndroidAOP/FAQ/#14-different-modules-of-componentized-projects-use-products-such-as-aar-for-compilation-how-to-speed-up-the-packaging-speed)**

<p align = "left">    
<picture>
  <source srcset="https://github.com/FlyJingFish/AndroidAOP/blob/master/svg/one.svg" media="(prefers-color-scheme: light)">
  <source srcset="https://github.com/FlyJingFish/AndroidAOP/blob/master/svg/one_dark.svg" media="(prefers-color-scheme: dark)">
  <img src="https://github.com/FlyJingFish/AndroidAOP/blob/master/svg/one.svg" align = "center"  width="22" height="22"/>
</picture>
For <strong>all sub-modules</strong> also rely on plug-ins, please follow the above<a href="#1-introduce-the-plug-in-choose-one-of-the-two-methods-below-required">step 1 method 1 to configure the project</a>, then choose one of the following methods
</p>  

- **Method 1 (recommended):**

  Follow the above [Step 1 Method 1 Configuration Project](#1-introduce-the-plug-in-choose-one-of-the-two-methods-below-required) and you are done. **This method automatically applies debugMode to all Android modules**

- **Method 2:**

  Please configure the project according to the above [Step 1 Method 1 to configure the project](#1-introduce-the-plug-in-choose-one-of-the-two-methods-below-required), and then manually set the required sub-module module, for example:

  ```gradle
  plugins {
       ...
       id 'android.aop'//It is best to put it on the last line
  }
  ```

> [!TIP]\
> **1. This method can only apply debugMode to the modules you have added, and the related aspects in the modules that have not been added will not take effect** <br>
> **2. If your module is a Java or Kotlin library, this method can only enable all Android libraries. You need to use method 2 to configure your module separately for it to take effect.**

<p align = "left">    
<picture>
  <source srcset="https://github.com/FlyJingFish/AndroidAOP/blob/master/svg/two.svg" media="(prefers-color-scheme: light)">
  <source srcset="https://github.com/FlyJingFish/AndroidAOP/blob/master/svg/two_dark.svg" media="(prefers-color-scheme: dark)">
  <img src="https://github.com/FlyJingFish/AndroidAOP/blob/master/svg/two.svg" align = "center"  width="22" height="22"/>
</picture>
Add the following settings in <code>gradle.properties</code> in the <strong>root directory</strong>
</p>  

```properties
androidAop.debugMode=true //Set to true to use the current packaging method of your project, false to use the full packaging method, otherwise the default is false
```

> [!CAUTION]\
> **⚠️⚠️⚠️ Please note that when set to true, the compilation speed will be faster but some functions will be invalid. Only the aop code will be woven into the set module. The third-party jar package will not weave in the code, so please be careful to turn it off when building the official package. Configure this and clean the project**

<p align = "left">    
<picture>
  <source srcset="https://github.com/FlyJingFish/AndroidAOP/blob/master/svg/three.svg" media="(prefers-color-scheme: light)">
  <source srcset="https://github.com/FlyJingFish/AndroidAOP/blob/master/svg/three_dark.svg" media="(prefers-color-scheme: dark)">
  <img src="https://github.com/FlyJingFish/AndroidAOP/blob/master/svg/three.svg" align = "center"  width="22" height="22"/>
</picture>
Add the following settings in <code>gradle.properties</code> in the <strong>root directory</strong>
</p> 


```properties
androidAop.debugMode.variantOnlyDebug = true //If this is not written by default, it is true
```

> [!TIP]\
> 1.If this option is not set, it will be true by default. Please note that when it is set to true, the release package will ignore the setting of `androidAop.debugMode = true` and automatically use the full packaging method. When it is set to false, there will be no such effect <br>
> 2.This feature is enabled by default, so the release package does not need to manually disable `androidAop.debugMode` <br>
> **3. This feature is only valid for Android libraries, not for Java or Kotlin libraries**

<p align = "left">    
<picture>
  <source srcset="https://github.com/FlyJingFish/AndroidAOP/blob/master/svg/four.svg" media="(prefers-color-scheme: light)">
  <source srcset="https://github.com/FlyJingFish/AndroidAOP/blob/master/svg/four_dark.svg" media="(prefers-color-scheme: dark)">
  <img src="https://github.com/FlyJingFish/AndroidAOP/blob/master/svg/four.svg" align = "center"  width="22" height="22"/>
</picture>
Add the following settings to <code>gradle.properties</code> in the <strong>root directory</strong> (optional, you can configure this if you want to be the best)
</p>  


```properties
androidAop.reflectInvokeMethod = true //Set to true to reflect the execution of the facet method, if not set, the default is false
androidAop.reflectInvokeMethod.variantOnlyDebug = true //Set to true to be effective only in debug, if not set, the default is false
```
> [!TIP]\
> 1.Reflection execution of the facet method will speed up packaging <br>
> 2.Please note that when `androidAop.reflectInvokeMethod.variantOnlyDebug` is set to true, the release package will ignore the setting of `androidAop.reflectInvokeMethod = true` and automatically not reflect, and there will be no such effect when it is set to false (if not set, the default is false) <br>
> 3.In versions 1.8.7 and above, the speed of secondary compilation has been optimized to be basically the same as the speed of enabling reflection.<br>
> **4. `androidAop.reflectInvokeMethod.variantOnlyDebug` is only valid for Android libraries, not for Java or Kotlin libraries**

<p align = "left">    
<picture>
  <source srcset="https://github.com/FlyJingFish/AndroidAOP/blob/master/svg/five.svg" media="(prefers-color-scheme: light)">
  <source srcset="https://github.com/FlyJingFish/AndroidAOP/blob/master/svg/five_dark.svg" media="(prefers-color-scheme: dark)">
  <img src="https://github.com/FlyJingFish/AndroidAOP/blob/master/svg/five.svg" align = "center"  width="22" height="22"/>
</picture>
Add the following settings to <code>gradle.properties</code> in the <strong>root directory</strong> (optional)
</p>  

```properties
androidAop.debugMode.buildConfig = true //If set to true, it means exporting a DebugModeBuildConfig.java file. If not set, the default value is true.
```

> [!TIP]\
> Because some modules have only Kotlin code, debugMode cannot take effect. You can insert a Java code to make it effective by setting it to true. If you don't need it, you can set it to false, but you need to manually create a Java code.


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

( * Supports suspend functions, returns results when conditions are met, and supports suspend functions whose return type is not Unit type)

[All examples of the above annotations are here](https://github.com/FlyJingFish/AndroidAOP/blob/master/app/src/main/java/com/flyjingfish/androidaop/MainActivity.kt#L128),[Also This](https://github.com/FlyJingFish/AndroidAOP/blob/master/app/src/main/java/com/flyjingfish/androidaop/SecondActivity.java#L64)

### Let me emphasize this @OnLifecycle

- 1. **The object to which the method added by @OnLifecycle must belong is a method directly or indirectly inherited from FragmentActivity or Fragment to be useful, or the object annotated method can also implement LifecycleOwner**
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

- @Permission Use of this annotation must match the following settings (⚠️This step is required, otherwise it will have no effect)[Perfect usage inspiration](https://flyjingfish.github.io/AndroidAOP/Implications/#4-i-believe-that-when-you-use-the-permission-permission-you-may-think-that-now-you-only-get-permission-to-enter-the-method-but-there-is-no-callback-without-permission-the-following-example-teaches-you-how-to-do-it)
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

### This library uses the following five annotations to implement custom aspects

- @AndroidAopPointCut is an aspect that annotates methods
- @AndroidAopMatchClassMethod is the aspect of matching class methods
- @AndroidAopReplaceClass is called by the replacement method
- @AndroidAopModifyExtendsClass is a modified inherited class
- @AndroidAopCollectMethod Is a collection inheritance class

#### 1. **@AndroidAopPointCut** is used to make aspects in the form of annotations on the method. The above annotations are all made through this. [Please see the wiki document for detailed usage](https://flyjingfish.github.io/AndroidAOP/AndroidAopPointCut)


The following uses @CustomIntercept as an example to introduce how to use it.

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

[About ProceedJoinPoint usage instructions](https://flyjingfish.github.io/AndroidAOP/ProceedJoinPoint), the same applies to ProceedJoinPoint below

- use

Directly add the annotation you wrote to any method, for example, to onCustomIntercept(). When onCustomIntercept() is called, it will first enter the invoke method of CustomInterceptCut mentioned above.

```kotlin
@CustomIntercept("I am custom data")
fun onCustomIntercept(){
    
}

```

#### 2. **@AndroidAopMatchClassMethod** is used to match aspects of a certain class and its corresponding method.

**The matching method supports accurate matching, [click here to see detailed usage documentation on the wiki](https://flyjingfish.github.io/AndroidAOP/AndroidAopMatchClassMethod)**

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

You can see that the type set by AndroidAopMatchClassMethod above is MatchType.EXTENDS, which means matching all subclasses inherited from OnClickListener. For more inheritance methods, [please refer to the Wiki document](https://flyjingfish.github.io/AndroidAOP/AndroidAopMatchClassMethod/#brief-description)

**⚠️Note: If the subclass does not have this method, the aspect will be invalid. In addition, do not match the same method multiple times in the same class, otherwise only one will take effect, Use overrideMethod to ignore this restriction [Click here for details](https://flyjingfish.github.io/AndroidAOP/AndroidAopMatchClassMethod)**


#### 3. **@AndroidAopReplaceClass** is used for replacement method calls

@AndroidAopReplaceClass and @AndroidAopReplaceMethod are used together

**Detailed usage of replacement method call, [click here to see detailed usage documentation in wiki](https://flyjingfish.github.io/AndroidAOP/AndroidAopReplaceClass)**

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

#### 4. **@AndroidAopModifyExtendsClass** is an inherited class that modifies the target class[Detailed usage](https://flyjingfish.github.io/AndroidAOP/AndroidAopModifyExtendsClass)

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

#### 5. **@AndroidAopCollectMethod** is a aspects that collects inherited classes of a class [detailed usage](https://flyjingfish.github.io/AndroidAOP/AndroidAopCollectMethod)

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

- Java

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

### common problem

1. Build reports an error "ZipFile invalid LOC header (bad signature)"

- Please restart Android Studio and clean the project


2. How to deal with multiple annotations or matching aspects for the same method?

- When multiple aspects are superimposed on a method, annotations take precedence over matching aspects (the matching aspects above), and the annotation aspects are executed sequentially from top to bottom.
- Call **[proceed](https://flyjingfish.github.io/AndroidAOP/ProceedJoinPoint)** to execute the next aspect, and the last aspect among multiple aspects will be executed **[proceed](https://flyjingfish.github.io/AndroidAOP/ProceedJoinPoint)** will call the code in the cut-in method
- Call **[proceed(args)](https://flyjingfish.github.io/AndroidAOP/ProceedJoinPoint)** in the previous aspect to pass in the parameters that can be updated, and the previous aspect will also be obtained in the next aspect One layer of updated parameters
- When there is an asynchronous call [proceed](https://flyjingfish.github.io/AndroidAOP/ProceedJoinPoint), the first asynchronous call [proceed](https://flyjingfish.github.io/AndroidAOP/ProceedJoinPoint) ) The return value of the aspect (that is, the return value of invoke) is the return value of the cut-in method;


#### [Obfuscation rules](https://flyjingfish.github.io/AndroidAOP/About_obfuscation)

> The library comes with `proguard-rules.pro` rules and is automatically imported. Normally no manual import is required.
> You can also go here to view [proguard-rules](https://github.com/FlyJingFish/AndroidAOP/blob/master/android-aop-core/proguard-rules.pro)



### Appreciation

You’ve all seen it here. If you like AndroidAOP, or feel that AndroidAOP has helped you, you can click “Star” in the upper right corner to support it. Your support is my motivation, thank you~ 😃

If you feel that AndroidAOP has saved you a lot of development time and added luster to your project, you can also scan the QR code below and invite the author for a cup of coffee ☕

#### [Donation List](https://github.com/FlyJingFish/AndroidAOP/blob/master/give_list.md)

<div>
<img src="/screenshot/IMG_4075.PNG" width="280" height="350">
<img src="/screenshot/IMG_4076.JPG" width="280" height="350">
</div>

If you comment on the name in the donation message, it will be recorded in the list~ If you are also a GitHub open source author, you can leave the GitHub project address or personal homepage address when donating, and the link will be added to the list.

### Contact information

* If you have any questions, you can join the group to communicate [QQ: 641697838](https://qm.qq.com/cgi-bin/qm/qr?k=w2qDbv_5bpLl0lO0qjXxijl3JHCQgtXx&jump_from=webapi&authKey=Q6/YB+7q9BvOGbYv1qXZGAZLigsfwaBxDC8kz03/5Pwy7018XunUcHoC11kVLqCb)

<img src="/screenshot/qq.png" width="220"/>

### Finally, I recommend some other libraries I wrote

- [OpenImage makes it easy to click on a small image in the application to view the animated enlargement effect of the large image](https://github.com/FlyJingFish/OpenImage)

- [ShapeImageView supports displaying any graphics, you can’t think of it without it](https://github.com/FlyJingFish/ShapeImageView)

- [GraphicsDrawable supports displaying arbitrary graphics, but is more lightweight](https://github.com/FlyJingFish/GraphicsDrawable)

- [ModuleCommunication solves the communication needs between modules and has more convenient router functions](https://github.com/FlyJingFish/ModuleCommunication)

- [FormatTextViewLib supports bolding, italics, size, underline, and strikethrough for some text. The underline supports custom distance, color, and line width; supports adding network or local images](https://github.com/FlyJingFish/FormatTextViewLib )

- [View more open source libraries on the homepage](https://github.com/FlyJingFish)
