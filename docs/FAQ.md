### 1. What to do when there are multiple annotations or matching aspects on the same method

- When multiple aspects are superimposed on a method, annotations take precedence over matching
  aspects, and annotation aspects are executed from top to bottom
- The next aspect will be executed only after **[proceed](https://flyjingfish.github.io/AndroidAOP/ProceedJoinPoint/)** is called, and the
  code in the aspect method will be called only after **[proceed](https://flyjingfish.github.io/AndroidAOP/ProceedJoinPoint/)** is executed on the
  last aspect among multiple aspects
- Calling **[proceed(args)](https://flyjingfish.github.io/AndroidAOP/ProceedJoinPoint/)** in
  the previous aspect can update the parameters passed in the method, and the updated parameters of
  the previous layer will also be obtained in the next aspect
- When there is an asynchronous
  call [proceed](https://flyjingfish.github.io/AndroidAOP/ProceedJoinPoint/), the return value
  of the first asynchronous
  call [proceed](https://flyjingfish.github.io/AndroidAOP/ProceedJoinPoint/) (that is, the
  return value of invoke) is the return value of the cut-in method; otherwise, if there is no
  asynchronous call [proceed](https://flyjingfish.github.io/AndroidAOP/ProceedJoinPoint/), the
  return value is the return value of the last cut-in method

### 2. Error "ZipFile invalid LOC header (bad signature)" when building

- Please restart Android Studio and then clean the project

### 3. Memory leak occurs in the cut-in processing class?

- This situation is usually because you have made a strong reference in the aspect processing class

### 4. Want to see all the locations where the aspect code is added

- [Add the androidAopConfig configuration item in the app's build.gradle, and set cutInfoJson to true](https://flyjingfish.github.io/AndroidAOP/getting_started/#4-add-the-androidaopconfig-configuration-item-in-apps-buildgradle-this-step-is-an-optional-configuration-item)

```groovy
plugins {
...
}
androidAopConfig {
  //Closed by default, enabled after build or packaging, a cut information json file will be generated in app/build/tmp/cutInfo.json
  cutInfoJson true
}
android {
...
}
```

### 5. Want to insert code before and after the method

- Matching aspect

```kotlin
@AndroidAopMatchClassMethod(
    targetClassName = "com.flyjingfish.test_lib.TestMatch",
    methodName = ["test2"],
    type = MatchType.SELF
)
class MatchTestMatchMethod : MatchClassMethod {
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        //Insert code before the method
        val value = joinPoint.proceed()
        //Insert code after the method
        return value
    }
}
```

??? note "Click here for more details"
    
    -  Annotation aspect
    
    ```kotlin
    class CustomInterceptCut : BasePointCut<CustomIntercept> {
        override fun invoke(
            joinPoint: ProceedJoinPoint,
            annotation: CustomIntercept //annotation is the annotation you add to the method
        ): Any? {
            //Insert code before the method
            val value = joinPoint.proceed()
            //Insert code after the method
            return value
        }
    }
    ```
    
    -  Replace the aspect
    
    ```kotlin
    @AndroidAopReplaceClass("android.util.Log")
    object ReplaceLog {
        @AndroidAopReplaceMethod("int e(java.lang.String,java.lang.String)")
        @JvmStatic
        fun e(tag: String, msg: String): Int {
            //Insert code before the method
            val log = Log.e(tag, "ReplaceLog-$msg")
            //Insert code after the method
            return log
        }
    }
    ```
    
    -  `AspectJ`'s `@AfterReturning` and `@AfterThrowing`
      **We will match the aspect Let's take an example**
    
    ```kotlin
    @AndroidAopMatchClassMethod(
        targetClassName = "com.flyjingfish.test_lib.TestMatch",
        methodName = ["test2"],
        type = MatchType.SELF
    )
    class MatchTestMatchMethod : MatchClassMethod {
        override fun invoke(joinPoint: ProceedJoinPoint, anno: TryCatch): Any? {
            return try {
                val value = joinPoint.proceed()
                // Here is @AfterReturning
                value
            } catch (e: Throwable) {
                // Here is @AfterThrowing
                throw RuntimeException(e)
            }
        }
    }
    ```


### 6. What is the life cycle of the aspect processing class of the matching aspect and the annotation aspect?

The aspect processing class is bound to the corresponding method of the class, which can be divided
into two cases

- a. If the pointcut method is **not static**
    - The aspect processing class will be recycled as the object where the method is located is
    recycled, but this is not timely; the recycling time is when any other aspect is processed.
    - And each object's pointcut method corresponds to an object of the aspect processing class, that
      is, there are as many objects as the class where the pointcut method is located creates.
- b. If the pointcut method is **static**
    - Because the aspect method is static, the aspect processing class will always exist once it is
    created.
    - And a class method only corresponds to one aspect processing class

No matter which type a or b is, the aspect processing class object will only be created when the
method is executed.

In short, the object of the aspect processing class is bound to the class or the object of the
class, and its life cycle is slightly longer than the object of the class of the pointcut non-static
method.

This is different from Aspectj, because we often want to set some member variables in the aspect
processing class to facilitate the use of the next aspect processing; if you want to do this in
Aspectj, you need to save the "member variable" as a "static variable", and you also need to
distinguish what object executes the pointcut method. You need to write a lot of code. AndroidAOP
just optimizes and solves this problem.

### 7. Compilation error or file usage on Windows computer

- Try upgrading `Gradle` to `8.7 or above` to see if it can be solved

![image](https://github.com/FlyJingFish/AndroidAOP/assets/96164429/e8f7db35-35bd-4e5e-9563-96dd47696bc0)

- Then make sure that this library is the latest version, and then make sure
       that `id 'android.aop'` is in the last line of `build.gradle` of the app module

<img width="848" alt="image" src="https://github.com/user-attachments/assets/7b0ddabc-650b-40a2-8d9c-2a62f919ae66">

- Otherwise, please try the following steps (:warning::warning::warning:Please note that the following is only for
       Windows computers, and Mac computers should not have this problem)

??? note "Click here for more details"

    - First, make sure that running `./gradlew --stop` directly can succeed. If it fails, please check it online and then proceed to the following steps
    <img width="848" alt="image" src="https://github.com/FlyJingFish/AndroidAOP/assets/96164429/c9a6a314-b422-48b0-b41c-80aca32f729c"/>
    
    - Click on the run configuration
           <img width="848" alt="image" src="https://github.com/FlyJingFish/AndroidAOP/assets/96164429/430e64c6-b21a-4db6-9f48-b69988e8698e"/>
    - Add the `Run External tool` task type based on the original one
           <img width="848" alt="image" src="https://github.com/FlyJingFish/AndroidAOP/assets/96164429/36057e2b-115d-41c4-b46a-eea1fdb6dea3"/>
  
    - Configure as follows
           <img width="848" alt="image" src="https://github.com/FlyJingFish/AndroidAOP/assets/96164429/d79429a0-ecff-4151-84f6-8778177d72ca"/>
    
    > Parameters:<br>
    > Program: ```The absolute path of the project\gradlew.bat```<br>
    > Arguments: ```./gradlew --stop```<br>
    > Working directory: ```The absolute path of the project\```<br>
    
    - Adjust the order to the top
           <img width="848" alt="image" src="https://github.com/FlyJingFish/AndroidAOP/assets/96164429/c52c856d-7325-4e59-85b1-6f2008317733"/>
    
    - Click OK to complete
          <img width="848" alt="image" src="https://github.com/FlyJingFish/AndroidAOP/assets/96164429/ffe00da7-c466-4f3a-81ed-78f75c79279b"/>
  
    - Run the project directly. If the following situation occurs, it means that the configuration is successful
           <img width="848" alt="image" src="https://github.com/FlyJingFish/AndroidAOP/assets/96164429/148d6ee9-2f73-4e45-b831-51c55483f6e2"/>
  
    - In addition, some netizens mentioned that changing `ksp` to `kapt` can also solve the problem


### 8. "Caused by: java.lang.SecurityException: digest error" related errors (The bug has been fixed in [1.5.5](https://github.com/FlyJingFish/AndroidAOP/releases/tag/1.5.5) version, and it is recommended to upgrade first)


??? note "Click here for more details"

    The jar package you added contains the following files, please delete them and import the jar
    package locally instead <br>
    
    <img src="https://github.com/FlyJingFish/AndroidAOP/assets/96164429/88023c1a-6710-4d93-a372-0b3ec32bf673" alt="show" width="600px" />
    <br>
    
    Operation steps
    
    - **Open the directory where the jar package is located**`cd /Users/a111/Downloads/ida-android-new/app/libs`
    
    - **Unzip the jar package** `jar -xvf bcprov-jdk15on-1.69.jar`
    
    <img src="https://github.com/FlyJingFish/AndroidAOP/assets/96164429/933aaa72-ab29-48d7-a0cd-f58ad064fee9" alt="show" width="600px" />
    
    - **After unzipping**
    
    <img src="https://github.com/FlyJingFish/AndroidAOP/assets/96164429/ccc46723-eb4a-4591-a7dc-6967d28f85bc" alt="show" width="600px" />
    
    - **Open META-INF and delete the following files**
    
    <img src="https://github.com/FlyJingFish/AndroidAOP/assets/96164429/9403e083-ff4d-4a45-8d7d-d08b869d5736" alt="show" width="600px" />
    
    - **Packaging, you can use it later**`jar -cfm0 bcprov-jdk15on-1.69.jar META-INF/MANIFEST.MF org`
    
    <img src="https://github.com/FlyJingFish/AndroidAOP/assets/96164429/8cd8f0a8-e6e6-4d34-8b39-425cf810c783" alt="show" width="600px" />


### 9. Why do I still feel that the package compilation is slow even though I have enabled `androidAop.debugMode = true`?

The main reason for this is that you may have used some `Router` libraries or other plugins that
change the packaging method. You can refer to here to transform your
project [click here](https://flyjingfish.github.io/AndroidAOP/Implications/#5-is-the-third-party-routing-library-not-compatible-with-agp8-here-is-an-example-of-arouter-to-teach-you-how-to-use-androidaop-to-solve-this-problem),
here is how to remove the plugin part of these libraries and use AndroidAOP to complete its plugin
work, so you can delete these plugins to speed up packaging

### 10. How to match variable parameters?

`vararg str : String` in Kotlin is equivalent to `String...` in Java. In this matching, no matter
what kind of code is represented by `String[]` (String is used as an example here, and other types
are the same)

### 11. After adding this library, it prompts that the file cannot be found when installing the app

Check whether the configuration in your build.gradle contains Chinese. If there is Chinese, please
try to change it to English and install it again

### 12. Will calling the original method in the method annotated with @AndroidAopReplaceMethod cause recursion?

- If it is a direct call, it will not cause recursion, and the framework has already handled it

- If it is an indirect call, it will cause recursion, such as calling methods of other classes that
  contain the original method. The framework does not handle this. If you need to do this, you can
  combine exclude To
  use [Homepage access, step 4 is introduced](https://flyjingfish.github.io/AndroidAOP/getting_started/#4-add-the-androidaopconfig-configuration-item-in-apps-buildgradle-this-step-is-an-optional-configuration-item),
  use exclude to exclude the indirect call class

### 13. What should I do if I don’t want to introduce the built-in annotation aspect?

Please upgrade to version 2.1.5 or later, and check the access step 3

### 14. Different modules of componentized projects use products such as aar for compilation. How to speed up the packaging speed?

- The answer is still to use debugMode, which has been explained in **"Getting Started"** [Step 5 - debugMode Mode](/AndroidAOP/getting_started/#method-2-debugmode). In this case, you should configure it as follows

```properties
//👇This item is undoubtedly, it must be turned on! !
androidAop.debugMode=true
//👇This item needs to be turned off when you release the aar package. You can't turn it on again, because the release of aar is actually the release of the release package, don't you think?
androidAop.debugMode.variantOnlyDebug=false
```

- As for whether to use reflection, it depends on the person. It depends on your choice

```properties
//👇Turn it on to use reflection
androidAop.reflectInvokeMethod=true
//👇This item is similar to androidAop.debugMode.variantOnlyDebug. If you use reflection in the release package, turn this item off! !
androidAop.reflectInvokeMethod.variantOnlyDebug=false
```

In summary, releasing aar is actually the same as releasing apk, and the understanding of the above
configurations is actually the same

Some people may still have questions, how should they be used in the final packaging?

- In fact, it is okay to continue using debugMode, and the configuration is basically the same as
  above.
- If you do not use debugMode, the aar packages of your different modules have actually been
  processed by AOP according to the above steps. You do not need to do it again when packaging. You
  can remove it under the app module, for example:

```groovy
androidAopConfig {
    //👇 Exclude the aar packages that have been processed by AOP, and you can still read the aspect configuration of these packages
    exclude 'aar package name 1', 'aar package name 2'
    //❗️❗️❗️It is worth mentioning that when you publish aar, do not configure the package name of the aar you want to publish here, otherwise the aar will not be processed by AOP
}
```

### 15. Compile-time error `Caused by: java.lang.RuntimeException: cannot find com.xxx.yyy.test: com.xxx.yyy.Test found in com/xxx/yyy/test.class`

- This is usually caused by a package and class with the same name. You should change one of the names.