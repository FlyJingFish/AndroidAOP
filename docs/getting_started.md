
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

**The following configuration steps also apply to componentized scenarios [Click here to view](https://github.com/FlyJingFish/AndroidAOP/wiki/%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98#14%E7%BB%84%E4%BB%B6%E5%8C%96%E7%9A%84%E9%A1%B9%E7%9B%AE%E4%B8%8D%E5%90%8C-module-%E9%87%87%E7%94%A8%E7%9A%84%E6%96%B9%E6%A1%88%E6%98%AF-aar-%E8%BF%99%E6%A0%B7%E7%9A%84%E4%BA%A7%E7%89%A9%E8%BF%9B%E8%A1%8C%E7%BC%96%E8%AF%91%E5%A6%82%E4%BD%95%E5%8A%A0%E5%BF%AB%E6%89%93%E5%8C%85%E9%80%9F%E5%BA%A6%E5%91%A2)**

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
