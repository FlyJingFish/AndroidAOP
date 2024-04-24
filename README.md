# AndroidAOP

[![Maven central](https://img.shields.io/maven-central/v/io.github.FlyJingFish.AndroidAop/android-aop-core)](https://central.sonatype.com/search?q=io.github.FlyJingFish.AndroidAop)
[![GitHub stars](https://img.shields.io/github/stars/FlyJingFish/AndroidAop.svg)](https://github.com/FlyJingFish/AndroidAop/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/FlyJingFish/AndroidAop.svg)](https://github.com/FlyJingFish/AndroidAop/network/members)
[![GitHub issues](https://img.shields.io/github/issues/FlyJingFish/AndroidAop.svg)](https://github.com/FlyJingFish/AndroidAop/issues)
[![GitHub license](https://img.shields.io/github/license/FlyJingFish/AndroidAop.svg)](https://github.com/FlyJingFish/AndroidAop/blob/master/LICENSE)

## [English document](https://github.com/FlyJingFish/AndroidAOP/blob/master/README_EN.md)

### AndroidAOP 是专属于 Android 端 Aop 框架，只需一个注解就可以请求权限、切换线程、禁止多点、监测生命周期等等，**本库不是基于 AspectJ 实现的 Aop**，当然你也可以定制出属于你的 Aop 代码，心动不如行动，赶紧用起来吧

## 特色功能

1、本库内置了开发中常用的一些切面注解供你使用

2、本库支持让你自己做切面，语法简单易上手

3、本库同步支持 Java 和 Kotlin 代码

4、本库支持切入三方库

5、本库支持切点方法为 Lambda 表达式的情况

6、本库支持切点方法为 suspend 修饰的协程函数

7、本库支持生成所有切点信息Json文件，方便一览所有切点位置[在此配置](#%E5%9B%9B%E5%9C%A8-app-%E7%9A%84buildgradle%E6%B7%BB%E5%8A%A0-androidaopconfig-%E9%85%8D%E7%BD%AE%E9%A1%B9%E6%AD%A4%E6%AD%A5%E4%B8%BA%E5%8F%AF%E9%80%89%E9%85%8D%E7%BD%AE%E9%A1%B9)

**8、本库不是基于 AspectJ 实现的，织入代码量极少，侵入性极低**


#### [点此下载apk,也可扫下边二维码下载](https://github.com/FlyJingFish/AndroidAOP/blob/master/apk/release/app-release.apk?raw=true)

<img src="/screenshot/qrcode.png" alt="show" width="200px" />

### 版本限制

最低Gradle版本：7.6👇（支持8.0以上）

<img src="/screenshot/gradle_version.png" alt="show" />

最低SDK版本：minSdkVersion >= 21

## Star趋势图

[![Stargazers over time](https://starchart.cc/FlyJingFish/AndroidAOP.svg?variant=adaptive)](https://starchart.cc/FlyJingFish/AndroidAOP)

---

## 使用步骤

**在开始之前可以给项目一个Star吗？非常感谢，你的支持是我唯一的动力。欢迎Star和Issues!**

![Stargazers over time](https://github.com/FlyJingFish/AndroidAOP/blob/master/screenshot/warning_maven_central.svg)

#### 一、引入插件，下边两种方式二选一（必须）

##### 方式一：```plugins``` 方式

直接在 **app** 的 ```build.gradle``` 添加

```gradle
//必须项 👇
plugins {
    ...
    id "io.github.FlyJingFish.AndroidAop.android-aop" version "1.5.8"
}
```

##### 方式二：```apply``` 方式

1、在 **项目根目录** 的 ```build.gradle``` 里依赖插件

老版本

```gradle
buildscript {
    dependencies {
        //必须项 👇
        classpath 'io.github.FlyJingFish.AndroidAop:android-aop-plugin:1.5.8'
    }
}
```

或者新版本

```gradle

plugins {
    //必须项 👇
    id "io.github.FlyJingFish.AndroidAop.android-aop" version "1.5.8" apply false
}
```

2、在 **app** 的 ```build.gradle``` 添加

老版本

```gradle
//必须项 👇
apply plugin: 'android.aop' //最好放在最后一行
```

或者新版本

```gradle
//必须项 👇
plugins {
    ...
    id 'android.aop'//最好放在最后一行
}
```



#### 二、如果你需要自定义切面，并且代码是 ```Kotlin``` (非必须)

1、在 **项目根目录** 的 ```build.gradle``` 里依赖插件

```gradle
plugins {
    //非必须项 👇，如果需要自定义切面，并且使用 android-aop-ksp 这个库的话需要配置 ，下边版本号根据你项目的 Kotlin 版本决定
    id 'com.google.devtools.ksp' version '1.8.0-1.0.9' apply false
}
```
[Kotlin 和 KSP Github 的匹配版本号列表](https://github.com/google/ksp/releases)

#### 三、引入依赖库(必须)

```gradle
plugins {
    //非必须项 👇，如果需要自定义切面，并且使用 android-aop-ksp 这个库的话需要配置 
    id 'com.google.devtools.ksp'
}

dependencies {
    //必须项 👇
    implementation 'io.github.FlyJingFish.AndroidAop:android-aop-core:1.5.8'
    implementation 'io.github.FlyJingFish.AndroidAop:android-aop-annotation:1.5.8'
    
    //必须项 👇如果您项目内已经有了这项不用加也可以
    implementation 'androidx.appcompat:appcompat:1.3.0' // 至少在1.3.0及以上
    
    //非必须项 👇，如果你想自定义切面需要用到，⚠️支持Java和Kotlin代码写的切面
    ksp 'io.github.FlyJingFish.AndroidAop:android-aop-ksp:1.5.8'
    
    //非必须项 👇，如果你想自定义切面需要用到，⚠️只适用于Java代码写的切面
    annotationProcessor 'io.github.FlyJingFish.AndroidAop:android-aop-processor:1.5.8'
    //⚠️上边的 android-aop-ksp 和 android-aop-processor 二选一
}
```
提示：ksp 或 annotationProcessor只能扫描当前 module ，在哪个 module 中有自定义切面代码就加在哪个 module，**但是自定义的切面代码是全局生效的**；必须依赖项可以通过 api 方式只加到公共 module 上

#### 四、在 app 的build.gradle添加 androidAopConfig 配置项（此步为可选配置项）

```gradle
plugins {
    ...
}
androidAopConfig {
    // enabled 为 false 切面不再起作用，默认不写为 true
    enabled true 
    // include 不设置默认全部扫描，设置后只扫描设置的包名的代码
    include '你项目的包名','自定义module的包名','自定义module的包名'
    // exclude 是扫描时排除的包
    // 可排除 kotlin 相关，提高速度
    exclude 'kotlin.jvm', 'kotlin.internal','kotlinx.coroutines.internal', 'kotlinx.coroutines.android'
    
    // verifyLeafExtends 是否开启验证叶子继承，默认打开，如果没有设置 @AndroidAopMatchClassMethod 的 type = MatchType.LEAF_EXTENDS，可以关闭
    verifyLeafExtends true
    //默认关闭，开启在 Build 或 打包后 将会生成切点信息json文件在 app/build/tmp/cutInfo.json
    cutInfoJson false
    //默认开启，设置 false 后会没有增量编译效果 筛选（关键字： AndroidAOP woven info code） build 输出日志可看时间 
    increment true//修改、增加、删除匹配切面的话，就会走全量编译
}
android {
    ...
}
```
**提示：合理使用 include 和 exclude 可提高编译速度，建议直接使用 include 设置你项目的相关包名（包括 app 和自定义 module 的）**

**⚠️⚠️⚠️设置完了 include 和 exclude 所有切面只在您设置规则之内才有效，麻烦记住自己的设置！**

**另外设置此处之后由于 Android Studio 可能有缓存，建议重启 AS 并 clean 下项目再继续开发**

#### 五、开发中可设置代码织入方式（此步为可选配置项）

- 1、请为**所有子 module 模块**设置上述[步骤一](#%E4%B8%80%E5%BC%95%E5%85%A5%E6%8F%92%E4%BB%B6%E4%B8%8B%E8%BE%B9%E4%B8%A4%E7%A7%8D%E6%96%B9%E5%BC%8F%E4%BA%8C%E9%80%89%E4%B8%80%E5%BF%85%E9%A1%BB)，例如：
```gradle
plugins {
    ...
    id 'android.aop'//最好放在最后一行
} 
```
- 2、在**根目录**的 `gradle.properties` 添加如下设置

```
androidAop.debugMode=true //设置为 true 默认走您项目当前的打包方式 ，false 则为全量打包方式
```

**⚠️⚠️⚠️请注意设置为 true 时部分功能将失效，只会为设置的 module 织入 aop 代码，三方jar包 不会织入代码，因此打正式包时请注意关闭此项配置**

### 本库内置了一些功能注解可供你直接使用

| 注解名称             |                                                参数说明                                                 |                                        功能说明                                         |
|------------------|:---------------------------------------------------------------------------------------------------:|:-----------------------------------------------------------------------------------:|
| @SingleClick     |                                      value = 快速点击的间隔，默认1000ms                                       |                             单击注解，加入此注解，可使你的方法只有单击时才可进入                              |
| @DoubleClick     |                                      value = 两次点击的最大用时，默认300ms                                      |                              双击注解，加入此注解，可使你的方法双击时才可进入                               |
| @IOThread        |                                          ThreadType = 线程类型                                          |                          切换到子线程的操作，加入此注解可使你的方法内的代码切换到子线程执行                          |
| @MainThread      |                                                 无参数                                                 |                          切换到主线程的操作，加入此注解可使你的方法内的代码切换到主线程执行                          |
| @OnLifecycle     |                                       value = Lifecycle.Event                                       |                        监听生命周期的操作，加入此注解可使你的方法内的代码在对应生命周期内才去执行                        |
| @TryCatch        |                                        value = 你自定义加的一个flag                                         |                            加入此注解可为您的方法包裹一层 try catch 代码                             |
| @Permission      |                                   tag = 自定义标记<br>value = 权限的字符串数组                                   |                            申请权限的操作，加入此注解可使您的代码在获取权限后才执行                             |
| @Scheduled       | initialDelay = 延迟开始时间<br>interval = 间隔<br>repeatCount = 重复次数<br>isOnMainThread = 是否主线程<br>id = 唯一标识 | 定时任务，加入此注解，可使你的方法每隔一段时间执行一次，调用AndroidAop.shutdownNow(id)或AndroidAop.shutdown(id)可停止 |
| @Delay           |                         delay = 延迟时间<br>isOnMainThread = 是否主线程<br>id = 唯一标识                         |  延迟任务，加入此注解，可使你的方法延迟一段时间执行，调用AndroidAop.shutdownNow(id)或AndroidAop.shutdown(id)可取消  |
| @CheckNetwork    |                tag = 自定义标记<br>toastText = 无网络时toast提示<br>invokeListener = 是否接管检查网络逻辑                |                            检查网络是否可用，加入此注解可使你的方法在有网络才可进去                             |
| @CustomIntercept |                                     value = 你自定义加的一个字符串数组的flag                                      |              自定义拦截，配合 AndroidAop.setOnCustomInterceptListener 使用，属于万金油              |

[上述注解使用示例都在这](https://github.com/FlyJingFish/AndroidAOP/blob/master/app/src/main/java/com/flyjingfish/androidaop/MainActivity.kt#L128),[还有这](https://github.com/FlyJingFish/AndroidAOP/blob/master/app/src/main/java/com/flyjingfish/androidaop/SecondActivity.java#L64)

- @OnLifecycle

  - **1、@OnLifecycle 加到的方法所属对象必须是属于直接或间接继承自 FragmentActivity 或 Fragment的方法才有用，或者注解方法的对象实现 LifecycleOwner 也可以**
  - 2、如果第1点不符合的情况下，可以给切面方法第一个参数设置为第1点的类型，在调用切面方法传入也是可以的，例如：

```java
public class StaticClass {
    @SingleClick(5000)
    @OnLifecycle(Lifecycle.Event.ON_RESUME)
    public static void onStaticPermission(MainActivity activity, int maxSelect , ThirdActivity.OnPhotoSelectListener back){
        back.onBack();
    }

}
```

- @TryCatch 使用此注解你可以设置以下设置（非必须）
```java
AndroidAop.INSTANCE.setOnThrowableListener(new OnThrowableListener() {
    @Nullable
    @Override
    public Object handleThrowable(@NonNull String flag, @Nullable Throwable throwable,TryCatch tryCatch) {
        // TODO: 2023/11/11 发生异常可根据你当时传入的flag作出相应处理，如果需要改写返回值，则在 return 处返回即可
        return 3;
    }
});
```

- @Permission 使用此注解必须配合以下设置（⚠️此步为必须设置的，否则是没效果的）
```java
AndroidAop.INSTANCE.setOnPermissionsInterceptListener(new OnPermissionsInterceptListener() {
    @SuppressLint("CheckResult")
    @Override
    public void requestPermission(@NonNull ProceedJoinPoint joinPoint, @NonNull Permission permission, @NonNull OnRequestPermissionListener call) {
        Object target =  joinPoint.getTarget();
        if (target instanceof FragmentActivity){
            RxPermissions rxPermissions = new RxPermissions((FragmentActivity) target);
            rxPermissions.request(permission.value()).subscribe(call::onCall);
        }else if (target instanceof Fragment){
            RxPermissions rxPermissions = new RxPermissions((Fragment) target);
            rxPermissions.request(permission.value()).subscribe(call::onCall);
        }else{
            // TODO: target 不是 FragmentActivity 或 Fragment ，说明注解所在方法不在其中，请自行处理这种情况
            // 建议：切点方法第一个参数可以设置为 FragmentActivity 或 Fragment ，然后 joinPoint.args[0] 就可以拿到
        }
    }
});
```

- @CustomIntercept 使用此注解你必须配合以下设置（⚠️此步为必须设置的，否则还有什么意义呢？）
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

- @CheckNetwork 使用此注解你可以配合以下设置
权限是必须加的
```xml
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```
以下设置为可选设置项
```java
AndroidAop.INSTANCE.setOnCheckNetworkListener(new OnCheckNetworkListener() {
    @Nullable
    @Override
    public Object invoke(@NonNull ProceedJoinPoint joinPoint, @NonNull CheckNetwork checkNetwork, boolean availableNetwork) {
        return null;
    }
});
```
在使用时 invokeListener 设置为true，即可进入上边回调
```kotlin
@CheckNetwork(invokeListener = true)
fun toSecondActivity(){
    startActivity(Intent(this,SecondActivity::class.java))
}
```
另外内置 Toast 可以让你接管
```java
AndroidAop.INSTANCE.setOnToastListener(new OnToastListener() {
    @Override
    public void onToast(@NonNull Context context, @NonNull CharSequence text, int duration) {
        
    }
});
```

👆上边的监听，最好放到你的 application 中

## 此外本库也同样支持让你自己做切面，实现起来非常简单！

### 本库通过以下四种注解，实现自定义切面

- @AndroidAopPointCut 是为方法加注解的切面
- @AndroidAopMatchClassMethod 是匹配类的方法的切面
- @AndroidAopReplaceClass 是替换方法调用的
- @AndroidAopModifyExtendsClass 是修改继承类

#### 一、**@AndroidAopPointCut** 是在方法上通过注解的形式做切面的，上述中注解都是通过这个做的，[详细使用请看wiki文档](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopPointCut)

下面以 @CustomIntercept 为例介绍下该如何使用

- 创建注解(将 @AndroidAopPointCut 加到你的注解上)

```java
@AndroidAopPointCut(CustomInterceptCut.class)
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomIntercept {
    String[] value() default {};
}
```

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

[关于 ProceedJoinPoint 使用说明](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint)，下文的 ProceedJoinPoint 同理

- 使用

直接将你写的注解加到任意一个方法上，例如加到了 onCustomIntercept() 当 onCustomIntercept() 被调用时首先会进入到上文提到的 CustomInterceptCut 的 invoke 方法上

```kotlin
@CustomIntercept("我是自定义数据")
fun onCustomIntercept(){
    
}

```

#### 二、**@AndroidAopMatchClassMethod** 是做匹配某类及其对应方法的切面的

**匹配方法支持精准匹配，[点此看wiki详细使用文档](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopMatchClassMethod)**


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

可以看到上方 AndroidAopMatchClassMethod 设置的 type 是 MatchType.EXTENDS 表示匹配所有继承自 OnClickListener 的子类，另外更多继承方式，[请参考Wiki文档](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopMatchClassMethod#excludeclasses-%E6%98%AF%E6%8E%92%E9%99%A4%E6%8E%89%E7%BB%A7%E6%89%BF%E5%85%B3%E7%B3%BB%E4%B8%AD%E7%9A%84%E4%B8%AD%E9%97%B4%E7%B1%BB%E6%95%B0%E7%BB%84)

**⚠️注意：如果子类没有该方法，则切面无效，另外对同一个类的同一个方法不要做多次匹配，否则只有一个会生效**

#### 匹配切面实用场景：

- 例如你想做退出登陆逻辑时可以使用上边这个，只要在页面内跳转就可以检测是否需要退出登陆

- 又或者你想在三方库某个方法上设置切面，可以直接设置对应类名，对应方法，然后 type = MatchType.SELF，这样可以侵入三方库的代码，当然这么做记得修改上文提到的 androidAopConfig 的配置


#### 三、**@AndroidAopReplaceClass** 是做替换方法调用的

@AndroidAopReplaceClass 和 @AndroidAopReplaceMethod 配合使用

- **注意这种方式和前两种的有着本质的区别，前两种关注的是方法的执行，并且会自动保留可以执行原有逻辑的方法（即[ProceedJoinPoint](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint)）；**
- **这个关注的是方法的调用，是将所有调用的地方替换为您设置的类的静态方法，并且不会自动保留执行原有逻辑的方法**
- **_这个方式的优点在于“相当于”可以监测到某些系统方法（android.jar里的代码）的调用，前两者不具备这个特点，所以如果不是基于此种需求，建议使用 [@AndroidAopMatchClassMethod](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopMatchClassMethod)_**

**替换方法调用详细使用方法，[点此看wiki详细使用文档](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopReplaceClass)**

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

[如果函数是 suspend 修饰的，点此看详细说明](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopReplaceClass#%E5%A6%82%E6%9E%9C%E8%A2%AB%E6%9B%BF%E6%8D%A2%E5%87%BD%E6%95%B0%E6%98%AF-suspend-%E4%BF%AE%E9%A5%B0%E7%9A%84%E9%82%A3%E4%B9%88%E4%BD%A0%E5%8F%AA%E8%83%BD%E7%94%A8kotlin%E4%BB%A3%E7%A0%81%E6%9D%A5%E5%86%99%E5%B9%B6%E4%B8%94%E6%9B%BF%E6%8D%A2%E5%87%BD%E6%95%B0%E4%B9%9F%E8%A6%81%E8%A2%AB-suspend-%E4%BF%AE%E9%A5%B0)


#### 四、**@AndroidAopModifyExtendsClass** 是修改目标类的继承类[详细使用方式](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopModifyExtendsClass)

通常是在某个类的继承关系中替换掉其中一层，然后重写一些函数，在重写的函数中加入一些你想加的逻辑代码，起到监听、改写原有逻辑的作用

如下例所示，就是要把 ```AppCompatImageView``` 的继承类替换成 ```ReplaceImageView```

应用场景：非侵入式地实现监控大图加载的功能

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


### 常见问题

1、Build时报错 "ZipFile invalid LOC header (bad signature)"

- 请重启Android Studio，然后 clean 项目


2、 同一个方法存在多个注解或匹配切面时，怎么处理的

- 多个切面叠加到一个方法上时注解优先于匹配切面（上文的匹配切面），注解切面之间从上到下依次执行
- 调用 **[proceed](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint)** 才会执行下一个切面，多个切面中最后一个切面执行 **[proceed](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint)** 才会调用切入方法内的代码
- 在前边切面中调用 **[proceed(args)](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint)** 可更新方法传入参数，并在下一个切面中也会拿到上一层更新的参数
- 存在异步调用[proceed](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint)时，第一个异步调用 [proceed](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint) 切面的返回值（就是 invoke 的返回值）就是切入方法的返回值；否则没有异步调用[proceed](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint)，则返回值就是最后一个切面的返回值

3、 想 Hook 安装包以外的代码？

- AndroidAOP 这个库顾名思义就不是 Hook 库，它是致力于打造 AOP 思想的库，所以它只能 Hook 安装包以内的代码

#### 混淆规则

> 此资源库自带[混淆规则](https://github.com/FlyJingFish/AndroidAOP/blob/master/android-aop-core/proguard-rules.pro)，并且会自动导入，正常情况下无需手动导入。



### 赞赏

都看到这里了，如果您喜欢 AndroidAOP，或感觉 AndroidAOP 帮助到了您，可以点右上角“Star”支持一下，您的支持就是我的动力，谢谢～ 😃

如果感觉 AndroidAOP 为您节约了大量开发时间、为您的项目增光添彩，您也可以扫描下面的二维码，请作者喝杯咖啡 ☕

#### [捐赠列表](https://github.com/FlyJingFish/AndroidAOP/blob/master/give_list.md)

<div>
<img src="/screenshot/IMG_4075.PNG" width="280" height="350">
<img src="/screenshot/IMG_4076.JPG" width="280" height="350">
</div>

如果在捐赠留言中备注名称，将会被记录到列表中~ 如果你也是github开源作者，捐赠时可以留下github项目地址或者个人主页地址，链接将会被添加到列表中



### 联系方式

* 有问题可以加群大家一起交流 [QQ：641697838](https://qm.qq.com/cgi-bin/qm/qr?k=w2qDbv_5bpLl0lO0qjXxijl3JHCQgtXx&jump_from=webapi&authKey=Q6/YB+7q9BvOGbYv1qXZGAZLigsfwaBxDC8kz03/5Pwy7018XunUcHoC11kVLqCb)

<img src="/screenshot/qq.png" width="220"/>

### 最后推荐我写的另外一些库

- [OpenImage 轻松实现在应用内点击小图查看大图的动画放大效果](https://github.com/FlyJingFish/OpenImage)

- [ShapeImageView 支持显示任意图形，只有你想不到没有它做不到](https://github.com/FlyJingFish/ShapeImageView)

- [FormatTextViewLib 支持部分文本设置加粗、斜体、大小、下划线、删除线，下划线支持自定义距离、颜色、线的宽度；支持添加网络或本地图片](https://github.com/FlyJingFish/FormatTextViewLib)

- [主页查看更多开源库](https://github.com/FlyJingFish)

