
## Steps for usage

### 1. Introduce the plug-in, choose one of the two methods below (required)

#### Method 1: ```apply``` method

<p align = "left">    
<picture>
  <!-- 亮色模式下显示的 SVG -->
  <source srcset="../svg/one.svg" media="(prefers-color-scheme: light)">
  <!-- 暗黑模式下显示的 SVG -->
  <source srcset="../svg/one_dark.svg" media="(prefers-color-scheme: dark)">
  <!-- 默认图片 -->
  <img src="../svg/one.svg" align = "center"  width="22" height="22" />
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
  <source srcset="../svg/two.svg" media="(prefers-color-scheme: light)">
  <!-- 暗黑模式下显示的 SVG -->
  <source srcset="../svg/two_dark.svg" media="(prefers-color-scheme: dark)">
  <!-- 默认图片 -->
  <img src="../svg/two.svg" align = "center"  width="22" height="22"/>
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
!!! note
    Tip: ksp or annotationProcessor can only scan the current module. Custom aspect codes are added to the module where they are located. **But custom aspect codes are globally effective**; required dependencies can be added only to public modules through the API.

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
!!! note
    1. Include and exclude support precise setting to a class**<br>
    2. Reasonable use of include and exclude can improve compilation speed. It is recommended to use include to set the relevant package name of your project (including app and custom module)**<br>
    3. If `LEAF_EXTENDS` is not set for `@AndroidAopMatchClassMethod` and `@AndroidAopCollectMethod`, setting `verifyLeafExtends` to false can also speed up**

!!! warning
    ⚠️⚠️⚠️After setting include and exclude, all aspects are only valid within the rules you set. Please remember your settings! In addition, since Android Studio may have cache after setting here, it is recommended to clean before continuing development**

### 5. The code weaving method can be set during development (this step is an optional configuration item)

**The following configuration steps also apply to componentized scenarios [Click here to view](https://github.com/FlyJingFish/AndroidAOP/wiki/%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98#14%E7%BB%84%E4%BB%B6%E5%8C%96%E7%9A%84%E9%A1%B9%E7%9B%AE%E4%B8%8D%E5%90%8C-module-%E9%87%87%E7%94%A8%E7%9A%84%E6%96%B9%E6%A1%88%E6%98%AF-aar-%E8%BF%99%E6%A0%B7%E7%9A%84%E4%BA%A7%E7%89%A9%E8%BF%9B%E8%A1%8C%E7%BC%96%E8%AF%91%E5%A6%82%E4%BD%95%E5%8A%A0%E5%BF%AB%E6%89%93%E5%8C%85%E9%80%9F%E5%BA%A6%E5%91%A2)**

<p align = "left">    
<picture>
  <source srcset="../svg/one.svg" media="(prefers-color-scheme: light)">
  <source srcset="../svg/one_dark.svg" media="(prefers-color-scheme: dark)">
  <img src="../svg/one.svg" align = "center"  width="22" height="22"/>
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

!!! note
    1. This method can only apply debugMode to the modules you have added, and the related aspects in the modules that have not been added will not take effect** <br>
    2. If your module is a Java or Kotlin library, this method can only enable all Android libraries. You need to use method 2 to configure your module separately for it to take effect.**

<p align = "left">    
<picture>
  <source srcset="../svg/two.svg" media="(prefers-color-scheme: light)">
  <source srcset="../svg/two_dark.svg" media="(prefers-color-scheme: dark)">
  <img src="../svg/two.svg" align = "center"  width="22" height="22"/>
</picture>
Add the following settings in <code>gradle.properties</code> in the <strong>root directory</strong>
</p>  

```properties
androidAop.debugMode=true //Set to true to use the current packaging method of your project, false to use the full packaging method, otherwise the default is false
```

!!! warning
    ⚠️⚠️⚠️ Please note that when set to true, the compilation speed will be faster but some functions will be invalid. Only the aop code will be woven into the set module. The third-party jar package will not weave in the code, so please be careful to turn it off when building the official package. Configure this and clean the project**

<p align = "left">    
<picture>
  <source srcset="../svg/three.svg" media="(prefers-color-scheme: light)">
  <source srcset="../svg/three_dark.svg" media="(prefers-color-scheme: dark)">
  <img src="../svg/three.svg" align = "center"  width="22" height="22"/>
</picture>
Add the following settings in <code>gradle.properties</code> in the <strong>root directory</strong>
</p> 


```properties
androidAop.debugMode.variantOnlyDebug = true //If this is not written by default, it is true
```

!!! note
    1.If this option is not set, it will be true by default. Please note that when it is set to true, the release package will ignore the setting of `androidAop.debugMode = true` and automatically use the full packaging method. When it is set to false, there will be no such effect <br>
    2.This feature is enabled by default, so the release package does not need to manually disable `androidAop.debugMode` <br>
    **3. This feature is only valid for Android libraries, not for Java or Kotlin libraries**

<p align = "left">    
<picture>
  <source srcset="../svg/four.svg" media="(prefers-color-scheme: light)">
  <source srcset="../svg/four_dark.svg" media="(prefers-color-scheme: dark)">
  <img src="../svg/four.svg" align = "center"  width="22" height="22"/>
</picture>
Add the following settings to <code>gradle.properties</code> in the <strong>root directory</strong> (optional, you can configure this if you want to be the best)
</p>  


```properties
androidAop.reflectInvokeMethod = true //Set to true to reflect the execution of the facet method, if not set, the default is false
androidAop.reflectInvokeMethod.variantOnlyDebug = true //Set to true to be effective only in debug, if not set, the default is false
```
!!! note
    1.Reflection execution of the facet method will speed up packaging <br>
    2.Please note that when `androidAop.reflectInvokeMethod.variantOnlyDebug` is set to true, the release package will ignore the setting of `androidAop.reflectInvokeMethod = true` and automatically not reflect, and there will be no such effect when it is set to false (if not set, the default is false) <br>
    3.In versions 1.8.7 and above, the speed of secondary compilation has been optimized to be basically the same as the speed of enabling reflection.<br>
    **4. `androidAop.reflectInvokeMethod.variantOnlyDebug` is only valid for Android libraries, not for Java or Kotlin libraries**

<p align = "left">    
<picture>
  <source srcset="../svg/five.svg" media="(prefers-color-scheme: light)">
  <source srcset="../svg/five_dark.svg" media="(prefers-color-scheme: dark)">
  <img src="../svg/five.svg" align = "center"  width="22" height="22"/>
</picture>
Add the following settings to <code>gradle.properties</code> in the <strong>root directory</strong> (optional)
</p>  

```properties
androidAop.debugMode.buildConfig = true //If set to true, it means exporting a DebugModeBuildConfig.java file. If not set, the default value is true.
```

!!! note
    Because some modules have only Kotlin code, debugMode cannot take effect. You can insert a Java code to make it effective by setting it to true. If you don't need it, you can set it to false, but you need to manually create a Java code.


## Custom Aspects

- @AndroidAopPointCut is an aspect that annotates methods
- @AndroidAopMatchClassMethod is the aspect of matching class methods
- @AndroidAopReplaceClass is called by the replacement method
- @AndroidAopModifyExtendsClass is a modified inherited class
- @AndroidAopCollectMethod Is a collection inheritance class

#### **@AndroidAopPointCut** 

&emsp;&emsp;It is used to make aspects in the form of annotations on the method. The above annotations are all made through this. [Please see the wiki document for detailed usage](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopPointCut)


&emsp;&emsp;The following uses @CustomIntercept as an example to introduce how to use it.

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

    [About ProceedJoinPoint usage instructions](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint), the same applies to ProceedJoinPoint below

- use

    Directly add the annotation you wrote to any method, for example, to onCustomIntercept(). When onCustomIntercept() is called, it will first enter the invoke method of CustomInterceptCut mentioned above.
    
    ```kotlin
    @CustomIntercept("I am custom data")
    fun onCustomIntercept(){
        
    }
    
    ```

#### **@AndroidAopMatchClassMethod** 

&emsp;&emsp;It is used to match aspects of a certain class and its corresponding method.

&emsp;&emsp;**The matching method supports accurate matching, [click here to see detailed usage documentation on the wiki](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopMatchClassMethod)**

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

    **⚠️Note: If the subclass does not have this method, the aspect will be invalid. In addition, do not match the same method multiple times in the same class, otherwise only one will take effect, Use overrideMethod to ignore this restriction [Click here for details](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopMatchClassMethod)**


#### **@AndroidAopReplaceClass** 

&emsp;&emsp;It is used for replacement method calls

&emsp;&emsp;@AndroidAopReplaceClass and @AndroidAopReplaceMethod are used together

&emsp;&emsp;**Detailed usage of replacement method call, [click here to see detailed usage documentation in wiki](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopReplaceClass)**

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

#### **@AndroidAopModifyExtendsClass** 

&emsp;&emsp;It is an inherited class that modifies the target class[Detailed usage](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopModifyExtendsClass)

&emsp;&emsp;Usually, you replace one layer in the inheritance relationship of a certain class, then rewrite some functions, and add some logic code you want to the rewritten functions to monitor and rewrite the original logic.

&emsp;&emsp;As shown in the following example, you need to replace the inherited class of ```AppCompatImageView``` with ```ReplaceImageView```

- Application scenario: non-invasively implement the function of monitoring large image loading

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

#### **@AndroidAopCollectMethod** 

&emsp;&emsp;It is a aspects that collects inherited classes of a class [detailed usage](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopCollectMethod)

&emsp;&emsp;It is extremely simple to use, the sample code has already explained

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