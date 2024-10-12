
## 使用步骤

**在开始之前可以给项目一个Star吗？非常感谢，你的支持是我唯一的动力。欢迎Star和Issues!**

![Stargazers over time](../../screenshot/warning_maven_central.svg)

### 一、引入插件，下边两种方式二选一（必须）


#### 方式一：```apply``` 方式（推荐）

<p align = "left">    
<picture>
  <!-- 亮色模式下显示的 SVG -->
  <source srcset="../../svg/one.svg" media="(prefers-color-scheme: light)">
  <!-- 暗黑模式下显示的 SVG -->
  <source srcset="../../svg/one_dark.svg" media="(prefers-color-scheme: dark)">
  <!-- 默认图片 -->
  <img src="../../svg/one.svg" align = "center"  width="22" height="22" />
</picture>
在<strong>项目根目录</strong>的 <code>build.gradle</code> 里依赖插件
</p>  

- 新版本

    ```groovy
    
    plugins {
        //必须项 👇 apply 设置为 true 自动为所有module“预”配置debugMode，false则按下边步骤五的方式二
        id "io.github.FlyJingFish.AndroidAop.android-aop" version "2.2.3" apply true
    }
    ```

- 或者老版本
    ```groovy
    buildscript {
        dependencies {
            //必须项 👇
            classpath 'io.github.FlyJingFish.AndroidAop:android-aop-plugin:2.2.3'
        }
    }
    // 👇加上这句自动为所有module“预”配置debugMode，不加则按下边步骤五的方式二
    apply plugin: "android.aop"
    ```

<p align = "left">    
<picture>
  <!-- 亮色模式下显示的 SVG -->
  <source srcset="../../svg/two.svg" media="(prefers-color-scheme: light)">
  <!-- 暗黑模式下显示的 SVG -->
  <source srcset="../../svg/two_dark.svg" media="(prefers-color-scheme: dark)">
  <!-- 默认图片 -->
  <img src="../../svg/two.svg" align = "center"  width="22" height="22"/>
</picture>
在<strong>app</strong>的 <code>build.gradle</code> 添加
</p> 

- 新版本

    ```groovy
    //必须项 👇
    plugins {
        ...
        id 'android.aop'//最好放在最后一行
    }
    ```

- 或者老版本

    ```groovy
    //必须项 👇
    apply plugin: 'android.aop' //最好放在最后一行
    ```

!!! warning
    **⚠️⚠️⚠️`id 'android.aop'` 这句尽量放在最后一行，尤其是必须在 `id 'com.android.application'` 或 `id 'com.android.library'` 的后边**


#### ~~方式二：```plugins``` 方式（不推荐）~~

- 直接在 **app** 的 ```build.gradle``` 添加

    ```groovy
    //必须项 👇
    plugins {
        ...
        id "io.github.FlyJingFish.AndroidAop.android-aop" version "2.2.3"//最好放在最后一行
    }
    ```

### 二、如果你需要自定义切面，并且代码是 ```Kotlin``` (非必须)

- 在 **项目根目录** 的 ```build.gradle``` 里依赖插件

```groovy
plugins {
    //非必须项 👇，如果需要自定义切面，并且使用 android-aop-ksp 这个库的话需要配置 ，下边版本号根据你项目的 Kotlin 版本决定
    id 'com.google.devtools.ksp' version '1.8.0-1.0.9' apply false
}
```
[Kotlin 和 KSP Github 的匹配版本号列表](https://github.com/google/ksp/releases)

### 三、引入依赖库(必须)

```groovy
plugins {
    //非必须项 👇，如果需要自定义切面，并且使用 android-aop-ksp 这个库的话需要配置 
    id 'com.google.devtools.ksp'
}

dependencies {
    //必须项 👇
    implementation 'io.github.FlyJingFish.AndroidAop:android-aop-core:2.2.3'
    //非必须项 👇这个包提供了一些常见的注解切面
    implementation 'io.github.FlyJingFish.AndroidAop:android-aop-extra:2.2.3'
    
    //必须项 👇如果您项目内已经有了这项不用加也可以
    implementation 'androidx.appcompat:appcompat:1.3.0' // 至少在1.3.0及以上
    
    //非必须项 👇，如果你想自定义切面需要用到，⚠️支持Java和Kotlin代码写的切面
    ksp 'io.github.FlyJingFish.AndroidAop:android-aop-ksp:2.2.3'
    
    //非必须项 👇，如果你想自定义切面需要用到，⚠️只适用于Java代码写的切面
    annotationProcessor 'io.github.FlyJingFish.AndroidAop:android-aop-processor:2.2.3'
    //⚠️上边的 android-aop-ksp 和 android-aop-processor 二选一
}
```

!!! note
    提示：ksp 或 annotationProcessor只能扫描当前 module ，在哪个 module 中有自定义切面代码就加在哪个 module，**但是自定义的切面代码是全局生效的**；必须依赖项可以通过 api 方式只加到公共 module 上

### 四、在 app 的build.gradle添加 androidAopConfig 配置项（此步为可选配置项）

- 相关开发配置

```groovy
plugins {
    ...
    id 'android.aop'//最好放在最后一行
}
androidAopConfig {
    // enabled 为 false 切面不再起作用，默认不写为 true
    enabled true 
    // include 不设置默认全部扫描，设置后只扫描设置的包名的代码
    include '你项目的包名','自定义module的包名','自定义module的包名'
    // exclude 是扫描时排除的包
    // 可排除 kotlin 相关，提高速度
    exclude 'kotlin.jvm', 'kotlin.internal','kotlinx.coroutines.internal', 'kotlinx.coroutines.android'
    
    // verifyLeafExtends 是否开启验证叶子继承，默认打开，@AndroidAopMatchClassMethod 和 @AndroidAopCollectMethod 如果没有设置 LEAF_EXTENDS，可以关闭
    verifyLeafExtends true
    //默认关闭，开启在 Build 或 打包后 将会生成切点信息json文件在 app/build/tmp/cutInfo.json
    cutInfoJson false
}
android {
    ...
}
```
!!! note
    **1、include 和 exclude支持精确设置为一个类**<br>
    **2、合理使用 include 和 exclude 可提高编译速度，建议直接使用 include 设置你项目的相关包名（包括 app 和自定义 module 的）**<br>
    **3、@AndroidAopMatchClassMethod 和 @AndroidAopCollectMethod 如果没有设置 LEAF_EXTENDS,就主动设置 verifyLeafExtends 为 false 也可以提速**

!!! warning
    **⚠️⚠️⚠️设置完了 include 和 exclude 所有切面只在您设置规则之内才有效，麻烦记住自己的设置！另外设置此处之后由于 Android Studio 可能有缓存，建议先 clean 再继续开发**

### 五、开发中可设置代码织入方式（此步为可选配置项，只为在开发过程中提高打包速度）

![Stargazers over time](../../screenshot/warning_debug_mode.svg)

**以下的配置步骤同样也适用于 组件化的场景 [点此查看](/AndroidAOP/zh/FAQ/#14-module-aar)**


<p align = "left">    
<picture>
  <source srcset="../../svg/one.svg" media="(prefers-color-scheme: light)">
  <source srcset="../../svg/one_dark.svg" media="(prefers-color-scheme: dark)">
  <img src="../../svg/one.svg" align = "center"  width="22" height="22"/>
</picture>
为<strong>所有的子module</strong>也依赖插件，请按照上述<a href="#apply">步骤一的方式一配置项目</a>，然后以下方式二选一
</p>  


- **方式一：（推荐）**

    按照上述[步骤一的方式一配置项目](#apply)，就可以了。**这个方式自动为所有 Android 的 module 应用 debugMode**


- ~~**方式二：（不推荐）**~~

    请按照上述[步骤一的方式一配置项目](#apply)后，手动为**需要的子 module 模块**设置，例如：

    ```groovy
    plugins {
        ...
        id 'android.aop'//最好放在最后一行，尤其是必须在 `id 'com.android.application'` 或 `id 'com.android.library'` 的后边
    }
    ```


!!! note
    **1、这个方式可以只为你加过的 module 应用 debugMode，没加的 module 里边的相关切面不会生效**<br>
    **2、如果你的 module 是 Java或Kotlin 的 Library，方式一只能让所有的 Android 的 Library，需要采用方式二单独为你的 module 配置才会生效**


<p align = "left">    
<picture>
  <source srcset="../../svg/two.svg" media="(prefers-color-scheme: light)">
  <source srcset="../../svg/two_dark.svg" media="(prefers-color-scheme: dark)">
  <img src="../../svg/two.svg" align = "center"  width="22" height="22"/>
</picture>
在<strong>根目录</strong>的 <code>gradle.properties</code> 添加如下设置
</p>  

```properties
androidAop.debugMode=true //设置为 true 走您项目当前的打包方式 ，false 则为全量打包方式，不写默认false
```
!!! warning
    **⚠️⚠️⚠️请注意设置为 true 时编译速度会变快但部分功能将失效，只会为设置的 module 织入 aop 代码，三方jar包 不会织入代码，因此打正式包时请注意关闭此项配置并clean项目**

<p align = "left">    
<picture>
  <source srcset="../../svg/three.svg" media="(prefers-color-scheme: light)">
  <source srcset="../../svg/three_dark.svg" media="(prefers-color-scheme: dark)">
  <img src="../../svg/three.svg" align = "center"  width="22" height="22"/>
</picture>
在<strong>根目录</strong>的 <code>gradle.properties</code> 添加如下设置
</p>  

```properties
androidAop.debugMode.variantOnlyDebug = true //默认不写这项就是true
```
!!! note
    1、这项不写默认就是true，请注意设置为 true 时 release 包会忽略 `androidAop.debugMode = true` 的设置自动走全量打包方式，设为 false 时则没有这种效果 <br>
    2、此项功能默认开启，因此release包无需手动关闭 `androidAop.debugMode` <br>
    **3、此项只对 Android 的 Library 有效,对 Java 或 Kotlin 的 Library 无效**

<p align = "left">    
<picture>
  <source srcset="../../svg/four.svg" media="(prefers-color-scheme: light)">
  <source srcset="../../svg/four_dark.svg" media="(prefers-color-scheme: dark)">
  <img src="../../svg/four.svg" align = "center"  width="22" height="22"/>
</picture>
在<strong>根目录</strong>的 <code>gradle.properties</code> 添加如下设置（选填，追求极致可以配置这项）
</p>  

```properties
androidAop.reflectInvokeMethod = true //设置为 true 反射执行切面方法 ，不写默认 false
androidAop.reflectInvokeMethod.variantOnlyDebug = true // 设置为 true 则只会在 debug 下才有效，不写默认false
```
!!! note
    1、反射执行切面方法会加快打包速度<br>
    2、请注意`androidAop.reflectInvokeMethod.variantOnlyDebug` 设置为 true 时 release 包会忽略 `androidAop.reflectInvokeMethod = true` 的设置自动不走反射，设为 false 时则没有这种效果（不写默认false）<br>
    3、在 1.8.7 及其以上的版本上，已优化到二次编译速度和开启反射速度是基本一样的 <br>
    **4、`androidAop.reflectInvokeMethod.variantOnlyDebug` 只对 Android 的 Library 有效,对 Java 或 Kotlin 的 Library 无效**


<p align = "left">    
<picture>
  <source srcset="../../svg/five.svg" media="(prefers-color-scheme: light)">
  <source srcset="../../svg/five_dark.svg" media="(prefers-color-scheme: dark)">
  <img src="../../svg/five.svg" align = "center"  width="22" height="22"/>
</picture>
在<strong>根目录</strong>的 <code>gradle.properties</code> 添加如下设置（非必须项）
</p>  

```properties
androidAop.debugMode.buildConfig = true //设置为 true 表示导出一个 DebugModeBuildConfig.java 文件，不写默认为 true
```

!!! note
    1、因为有些 module 的代码只有 kotlin 代码，导致 debugMode 无法生效，设置为true可插入一个 java 代码即可生效，如果不需要，可以设置为 false，但需要你手动创建一个 java 代码 <br>
    2、通常不需要配置此项，除非你遇到上述情况


## 自定义切面

### 本库通过以下五种注解，实现自定义切面

本篇介绍是大纲式的大致讲解

- @AndroidAopPointCut 是为方法加注解的切面
- @AndroidAopMatchClassMethod 是匹配类的方法的切面
- @AndroidAopReplaceClass 是替换方法调用的
- @AndroidAopModifyExtendsClass 是修改继承类
- @AndroidAopCollectMethod 是收集继承类



#### 一、**@AndroidAopPointCut** 是在方法上通过注解的形式做切面的，上述中注解都是通过这个做的，[详细使用请看wiki文档](/AndroidAOP/zh/AndroidAopPointCut)

下面以 @CustomIntercept 为例介绍下该如何使用

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

[关于 ProceedJoinPoint 使用说明](/AndroidAOP/zh/ProceedJoinPoint)，下文的 ProceedJoinPoint 同理

- 使用

直接将你写的注解加到任意一个方法上，例如加到了 onCustomIntercept() 当 onCustomIntercept() 被调用时首先会进入到上文提到的 CustomInterceptCut 的 invoke 方法上

```kotlin
@CustomIntercept("我是自定义数据")
fun onCustomIntercept(){
    
}

```

#### 二、**@AndroidAopMatchClassMethod** 是做匹配某类及其对应方法的切面的

**匹配方法支持精准匹配，[点此看wiki详细使用文档](/AndroidAOP/zh/AndroidAopMatchClassMethod)**


- 例子一

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

假如 TestMatch 是要匹配的类，而你想要匹配到 test2 这个方法，下边是匹配写法：


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
    // 在此写你的逻辑 
    //不想执行原来方法逻辑，👇就不调用下边这句
    return joinPoint.proceed()
  }
}

```

可以看到上方 AndroidAopMatchClassMethod 设置的 type 是 MatchType.SELF 表示只匹配 TestMatch 这个类自身，不考虑其子类

⚠️⚠️⚠️ 不是所有类都可以Hook进去，```type``` 类型为 ```SELF``` 时，```targetClassName``` 所设置的类必须是安装包里的代码。例如：Toast 这个类在 **android.jar** 里边是不行的

- 例子二

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

可以看到上方 AndroidAopMatchClassMethod 设置的 type 是 MatchType.EXTENDS 表示匹配所有继承自 OnClickListener 的子类，另外更多继承方式，[请参考Wiki文档](/AndroidAOP/zh/@AndroidAopMatchClassMethod/#_1)

**⚠️注意：如果子类没有该方法，则切面无效，使用 overrideMethod 可忽略此限制[详情点此](/AndroidAOP/zh/AndroidAopMatchClassMethod)**

#### 三、**@AndroidAopReplaceClass** 是做替换方法调用的

此方式是对 @AndroidAopMatchClassMethod 的一个补充，[点此看wiki详细说明文档](/AndroidAOP/zh/AndroidAopReplaceClass)

- Java写法
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


#### 四、**@AndroidAopModifyExtendsClass** 是修改目标类的继承类

通常是在某个类的继承关系中替换掉其中一层，然后重写一些函数，在重写的函数中加入一些你想加的逻辑代码，起到监听、改写原有逻辑的作用，[详细使用方式](/AndroidAOP/zh/AndroidAopModifyExtendsClass)


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

#### 五、**@AndroidAopCollectMethod** 是收集继承类

使用起来极其简单，示例代码已经说明了[详细使用方式](/AndroidAOP/zh/AndroidAopCollectMethod)

- Kotlin

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

- Java

```java
public class InitCollect2 {
    private static final List<SubApplication2> collects = new ArrayList<>();
    private static final List<Class<? extends SubApplication2>> collectClazz = new ArrayList<>();
    @AndroidAopCollectMethod
    public static void collect(SubApplication2 sub){
        collects.add(sub);
    }

    @AndroidAopCollectMethod
    public static void collect3(Class<? extends SubApplication2> sub){
        collectClazz.add(sub);
    }
  //直接调这个方法（方法名不限）上边的函数会被悉数回调
    public static void init(Application application){
        Log.e("InitCollect2","----init----");
        for (SubApplication2 collect : collects) {
            collect.onCreate(application);
        }
    }
}
```
