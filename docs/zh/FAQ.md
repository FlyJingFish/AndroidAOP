### 1、 同一个方法存在多个注解或匹配切面时，怎么处理的

- 多个切面叠加到一个方法上时注解优先于匹配切面，注解切面之间从上到下依次执行
- 调用 **[proceed](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint)** 才会执行下一个切面，多个切面中最后一个切面执行 **[proceed](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint)** 才会调用切面方法内的代码
- 在前边切面中调用 **[proceed(args)](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint)** 可更新方法传入参数，并在下一个切面中也会拿到上一层更新的参数
- 存在异步调用[proceed](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint)时，第一个异步调用 [proceed](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint) 切面的返回值（就是 invoke 的返回值）就是切入方法的返回值；否则没有异步调用[proceed](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint)，则返回值就是最后一个切面的返回值


### 2、Build时报错 "ZipFile invalid LOC header (bad signature)"

- 请重启Android Studio，然后 clean 项目

### 3、 切面处理类发生内存泄漏？

- 这种情况一般是因为你在切面处理类做了强引用

### 4、 想要看到所有的加入切面代码的位置
- [在 app 的build.gradle添加 androidAopConfig 配置项，cutInfoJson 设置为 true](https://github.com/FlyJingFish/AndroidAOP?tab=readme-ov-file#%E5%9B%9B%E5%9C%A8-app-%E7%9A%84buildgradle%E6%B7%BB%E5%8A%A0-androidaopconfig-%E9%85%8D%E7%BD%AE%E9%A1%B9%E6%AD%A4%E6%AD%A5%E4%B8%BA%E5%8F%AF%E9%80%89%E9%85%8D%E7%BD%AE%E9%A1%B9)

```groovy
plugins {
    ...
}
androidAopConfig {
    //默认关闭，开启在 Build 或 打包后 将会生成切点信息json文件在 app/build/tmp/cutInfo.json
    cutInfoJson true
}
android {
    ...
}
```

### 5、想要在方法前后插入代码
- 匹配切面
```kotlin
@AndroidAopMatchClassMethod(
        targetClassName = "com.flyjingfish.test_lib.TestMatch",
        methodName = ["test2"],
        type = MatchType.SELF
)
class MatchTestMatchMethod : MatchClassMethod {
  override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
    //在方法前插入代码
    val value = joinPoint.proceed()
    //在方法后插入代码
    return value
  }
}
```

??? note "点此看更多详情"
    
    - 注解切面
    
    ```kotlin
    class CustomInterceptCut : BasePointCut<CustomIntercept> {
        override fun invoke(
            joinPoint: ProceedJoinPoint,
            annotation: CustomIntercept //annotation就是你加到方法上的注解
        ): Any? {
            //在方法前插入代码
            val value = joinPoint.proceed()
            //在方法后插入代码
            return value
        }
    }
    ```

    - 替换切面
    
    ```kotlin
    @AndroidAopReplaceClass("android.util.Log")
    object ReplaceLog {
        @AndroidAopReplaceMethod("int e(java.lang.String,java.lang.String)")
        @JvmStatic
        fun e( tag:String, msg:String) :Int{
            //在方法前插入代码
            val log = Log.e(tag, "ReplaceLog-$msg")
            //在方法后插入代码
            return log
        }
    }
    ```
    
    - `AspectJ` 的 `@AfterReturning`和 `@AfterThrowing`
    
    **我们就以 匹配切面 为例介绍下吧**
    
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
                // 这里就是 @AfterReturning 
                value
            } catch (e: Throwable) {
                // 这里就是 @AfterThrowing
                throw RuntimeException(e)
            }
        }
    }
    ```


### 6、匹配切面和注解切面的切面处理类的生命周期是怎样的？

切面处理类与类的对应方法绑定在一起，分为两种情况

- a、如果切点方法 **不是静态的**
  - 切面处理类会随着方法所在对象的回收而回收，但这不是及时的；回收时机是其他任意一个切面处理时才进行回收。
  - 并且每一个对象的切点方法各自对应一个切面处理类的对象，也就是说一个切点方法的所在类创建了多少个对象，就有多少个切面处理类对象。
- b、如果切点方法 **是静态的**
  - 切面方法因为是静态的，所以切面处理类一旦被创建会一直存在。
  - 并且一个类方法只对应一个切面处理类

不管是a、b哪种类型，切面处理类对象只有在方法被执行时才会被创建出来。

简单来说，切面处理类的对象和类或类的对象是绑定在一块的，它的生命周期略长于切点非静态方法的类的对象。

这与 Aspectj 是不同的，因为我们往往想在切面处理类中设置一些成员变量，方便在下次切面处理时的使用；而 Aspectj 你要想这么做是需要将“成员变量”保存为一个“静态变量”，而且还需要区分执行切点方法的对象是什么，需要写很多的代码，AndroidAOP 刚好优化解决了这一问题。

### 7、Windows电脑编译报错或提示文件占用

- 1、你先尝试升级 `Gradle` 到 `8.7 以上` 能否解决

![image](https://github.com/FlyJingFish/AndroidAOP/assets/96164429/e8f7db35-35bd-4e5e-9563-96dd47696bc0)

- 2、然后保证是本库是最新版，然后保证 app 模块的 `build.gradle` 中 `id 'android.aop'` 在最后一行

<img width="848" alt="image" src="https://github.com/user-attachments/assets/7b0ddabc-650b-40a2-8d9c-2a62f919ae66">


- 3、否则请按如下步骤进行尝试(⚠️⚠️⚠️请注意以下只针对 Windows 电脑，mac电脑不应有此问题)

??? note "点此看更多详情"

    - 0、首先确保直接运行 `./gradlew --stop` 能否成功，如果失败请自行上网查询，然后再进行下边的步骤
      ![image](https://github.com/FlyJingFish/AndroidAOP/assets/96164429/c9a6a314-b422-48b0-b41c-80aca32f729c)
    
    
    - 1、点开运行配置
      ![6b8df9add79f183b5af3357b8ee6951e](https://github.com/FlyJingFish/AndroidAOP/assets/96164429/430e64c6-b21a-4db6-9f48-b69988e8698e)
    - 2、在原来基础上，新增 ```Run External tool``` 任务类型
      ![image](https://github.com/FlyJingFish/AndroidAOP/assets/96164429/36057e2b-115d-41c4-b46a-eea1fdb6dea3)

    - 3、按如下配置——————
      ![a769abbe58f17d6a02f8a0e27965f359](https://github.com/FlyJingFish/AndroidAOP/assets/96164429/d79429a0-ecff-4151-84f6-8778177d72ca)


    > 参数:<br>
    > Program:           ```项目所在的绝对路径\gradlew.bat```<br>
    > Arguments:         ```./gradlew --stop```<br>
    > Working directory: ```项目所在的绝对路径\```<br>

    - 4、把顺序调整到上边
      ![image](https://github.com/FlyJingFish/AndroidAOP/assets/96164429/c52c856d-7325-4e59-85b1-6f2008317733)
    
    
    - 5、点击ok完成
      ![image](https://github.com/FlyJingFish/AndroidAOP/assets/96164429/ffe00da7-c466-4f3a-81ed-78f75c79279b)

    - 6、直接跑项目，出现以下情况，即代表配置成功
      ![0e441acab958a77ccf7282d5473417a5](https://github.com/FlyJingFish/AndroidAOP/assets/96164429/148d6ee9-2f73-4e45-b831-51c55483f6e2)


    - 7、另外有些网友提到把 `ksp` 换成 `kapt` 也可以解决问题



### 8、“Caused by: java.lang.SecurityException: digest error” 相关报错 （[1.5.5](https://github.com/FlyJingFish/AndroidAOP/releases/tag/1.5.5)版本已修复该bug，建议优先升级处理）

??? note "点此看更多详情"



    你新增的 jar 包中包含以下文件，请将其删除，改为本地导入 jar 包
    
    <img src="https://github.com/FlyJingFish/AndroidAOP/assets/96164429/88023c1a-6710-4d93-a372-0b3ec32bf673" alt="show" width="600px" />
    
    操作步骤
    
    - **1、打开 jar 包所在目录**```cd /Users/a111/Downloads/ida-android-new/app/libs```
    
    
    
    - **2、解压 jar 包** ```jar -xvf bcprov-jdk15on-1.69.jar```
    
    <img src="https://github.com/FlyJingFish/AndroidAOP/assets/96164429/933aaa72-ab29-48d7-a0cd-f58ad064fee9" alt="show" width="600px" />
    
    
    
    - **3、解压后**
    
    <img src="https://github.com/FlyJingFish/AndroidAOP/assets/96164429/ccc46723-eb4a-4591-a7dc-6967d28f85bc" alt="show" width="600px" />
    
    - **4、打开 META-INF 删除以下文件**
    
    <img src="https://github.com/FlyJingFish/AndroidAOP/assets/96164429/9403e083-ff4d-4a45-8d7d-d08b869d5736" alt="show" width="600px" />
    
    - **5、打包，之后可以用了**```jar -cfm0 bcprov-jdk15on-1.69.jar META-INF/MANIFEST.MF org```
    
    
    
    <img src="https://github.com/FlyJingFish/AndroidAOP/assets/96164429/8cd8f0a8-e6e6-4d34-8b39-425cf810c783" alt="show" width="600px" />



### 9、为什么我开启了 `androidAop.debugMode = true` 依然感觉打包编译慢？

造成这种情况的主要原因在于你可能用到了一些 `Router` 库，或者其他改变打包方式的插件。您可参考这里去改造你的项目[点此前往](https://github.com/FlyJingFish/AndroidAOP/wiki/%E5%88%87%E9%9D%A2%E5%90%AF%E7%A4%BA#5%E4%B8%89%E6%96%B9%E8%B7%AF%E7%94%B1%E5%BA%93%E6%B2%A1%E6%9C%89%E9%80%82%E9%85%8D-agp8-%E4%B8%8B%E9%9D%A2%E4%BB%A5-arouter-%E4%B8%BA%E4%BE%8B%E6%95%99%E4%BD%A0%E5%A6%82%E4%BD%95%E5%88%A9%E7%94%A8-androidaop-%E8%A7%A3%E5%86%B3%E8%BF%99%E4%B8%AA%E9%97%AE%E9%A2%98)，这里演示了如何去除这些库的插件部分，用 AndroidAOP 去完成它的插件的工作，这样你就可以删掉这些插件加快打包速度

### 10、可变参数如何进行匹配？

Kotlin 中的 `vararg str : String` 相当于 Java 中的 `String...`，这种匹配时无论哪种代码都按 `String[]` 来表示（此处以 String 为例，其他类型也一样）

### 11、加入本库之后安装app时提示找不到文件

检查你的 build.gradle 中配置中是否包含中文，如存在中文请尝试更改为英文后再次安装

### 12、在 @AndroidAopReplaceMethod 注解的方法中调用原方法会不会造成递归的情况？

- 如果是直接调用就不会造成递归，框架已作处理

- 如果属于间接调用就会造成递归，例如调用包含的原方法的其他类的方法，框架对此不做处理，如需这么做可以结合 exclude 来使用[首页接入第四步有介绍](https://github.com/FlyJingFish/AndroidAOP?tab=readme-ov-file#%E5%9B%9B%E5%9C%A8-app-%E7%9A%84buildgradle%E6%B7%BB%E5%8A%A0-androidaopconfig-%E9%85%8D%E7%BD%AE%E9%A1%B9%E6%AD%A4%E6%AD%A5%E4%B8%BA%E5%8F%AF%E9%80%89%E9%85%8D%E7%BD%AE%E9%A1%B9)，使用exclude排除掉间接调用类即可

### 13、不想引入内置的注解切面，怎么办？

请升级至 2.1.5及之后的版本，并查看接入步骤三

### 14、组件化的项目不同 module 采用的方案是 aar 这样的产物进行编译，如何加快打包速度呢？

- 答案依旧还是使用 debugMode ，这个首页接入[步骤五](https://github.com/FlyJingFish/AndroidAOP?tab=readme-ov-file#%E4%BA%94%E5%BC%80%E5%8F%91%E4%B8%AD%E5%8F%AF%E8%AE%BE%E7%BD%AE%E4%BB%A3%E7%A0%81%E7%BB%87%E5%85%A5%E6%96%B9%E5%BC%8F%E6%AD%A4%E6%AD%A5%E4%B8%BA%E5%8F%AF%E9%80%89%E9%85%8D%E7%BD%AE%E9%A1%B9%E5%8F%AA%E4%B8%BA%E5%9C%A8%E5%BC%80%E5%8F%91%E8%BF%87%E7%A8%8B%E4%B8%AD%E6%8F%90%E9%AB%98%E6%89%93%E5%8C%85%E9%80%9F%E5%BA%A6)已经进行了说明，针对这种情况你应按照如下方法进行配置


```properties
//👇这一项无可置疑，必须打开！！
androidAop.debugMode = true
//👇这一项在你发布 aar 包的时候需要将其关闭，不可以再开了，因为aar的发布其实也是发布 release 包了，你说是不是？
androidAop.debugMode.variantOnlyDebug = false
```

- 至于要不要使用反射，那就因人而定了，看你怎么选择了

```properties
//👇打开就是使用反射
androidAop.reflectInvokeMethod = true
//👇这项类似于 androidAop.debugMode.variantOnlyDebug 你打 release 包使用反射就把这一项关了！！
androidAop.reflectInvokeMethod.variantOnlyDebug = false
```

综上所述，发布 aar 其实和发布 apk 一样，对于上述几项配置的理解其实是一样的

可能有人还有疑问，最终打包时应该如何使用呢？

- 其实想继续使用 debugMode 也没问题的，基本还是和上述一样的配置
- 如果不使用 debugMode，你的不同 module 的 aar包 按照上述步骤其实已经进行过 AOP 的处理了，你完全不用在打包时再搞一次了，可以在app module下将其剔除掉，例如：

```groovy
androidAopConfig {
    //👇 排除掉已经进行过 AOP 处理的 aar 包，依旧可以读取这些包的切面配置
    exclude 'aar包名1', 'aar包名2'
    //❗️❗️❗️值得一提的是在你发布 aar 的时候，不要把你要发布的 aar 的包名配置到这，否则 aar 是不会经过 AOP 处理的
}
```






