# AndroidAOP

[![Maven central](https://img.shields.io/maven-central/v/io.github.FlyJingFish.AndroidAop/android-aop-core)](https://central.sonatype.com/search?q=io.github.FlyJingFish.AndroidAop)
[![GitHub stars](https://img.shields.io/github/stars/FlyJingFish/AndroidAop.svg)](https://github.com/FlyJingFish/AndroidAop/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/FlyJingFish/AndroidAop.svg)](https://github.com/FlyJingFish/AndroidAop/network/members)
[![GitHub issues](https://img.shields.io/github/issues/FlyJingFish/AndroidAop.svg)](https://github.com/FlyJingFish/AndroidAop/issues)
[![GitHub license](https://img.shields.io/github/license/FlyJingFish/AndroidAop.svg)](https://github.com/FlyJingFish/AndroidAop/blob/master/LICENSE)

### AndroidAOP 是专属于 Android 端 Aop 框架，只需一个注解就可以请求权限、切换线程、禁止多点、监测生命周期等等，**本库不是基于 AspectJ 实现的 Aop**，当然你也可以定制出属于你的 Aop 代码，心动不如行动，赶紧用起来吧
## 特色功能

1、本库内置了开发中常用的一些切面注解供你使用

2、本库支持让你自己做切面，语法简单易上手

3、本库同步支持 Java 和 Kotlin 代码

4、本库支持切入三方库（只要是打进安装包里的代码，都是可以 Hook 的）

**5、本库不是基于 AspectJ 实现的，织入代码量极少，侵入性极低**


#### [点此下载apk,也可扫下边二维码下载](https://github.com/FlyJingFish/AndroidAOP/blob/master/apk/release/app-release.apk?raw=true)

<img src="/screenshot/qrcode.png" alt="show" width="200px" />

### 版本限制

最低Gradle版本：8.0

最低SDK版本：minSdkVersion >= 21

## 使用步骤

#### 一、在项目根目录下的build.gradle添加（必须）

```gradle
buildscript {
    dependencies {
        //必须项 👇
        classpath 'io.github.FlyJingFish.AndroidAop:android-aop-plugin:1.1.3'
    }
}
plugins {
    //非必须项 👇，如果需要自定义切面，并且使用 android-aop-ksp 这个库的话需要配置 ，下边版本号根据你项目的 Kotlin 版本决定
    id 'com.google.devtools.ksp' version '1.8.0-1.0.9' apply false
}
```
[Kotlin 和 KSP Github 的匹配版本号列表](https://github.com/google/ksp/releases)

#### 二、在 app 的build.gradle添加（此步为必须项）

#### ⚠️注意：👆此步为必须项👇

```gradle
//必须项 👇
plugins {
    id 'android.aop'
}
```

#### 三、引入依赖库

```gradle
plugins {
    //非必须项 👇，如果需要自定义切面，并且使用 android-aop-ksp 这个库的话需要配置 
    id 'com.google.devtools.ksp'
}

dependencies {
    //必须项 👇
    implementation 'io.github.FlyJingFish.AndroidAop:android-aop-core:1.1.3'
    implementation 'io.github.FlyJingFish.AndroidAop:android-aop-annotation:1.1.3'
    //非必须项 👇，如果你想自定义切面需要用到，⚠️支持Java和Kotlin代码写的切面
    ksp 'io.github.FlyJingFish.AndroidAop:android-aop-ksp:1.1.3'
    //非必须项 👇，如果你想自定义切面需要用到，⚠️只适用于Java代码写的切面
    annotationProcessor 'io.github.FlyJingFish.AndroidAop:android-aop-processor:1.1.3'
    //⚠️上边的 android-aop-ksp 和 android-aop-processor 二选一
}
```
**提示：ksp 或 annotationProcessor只是在当前 module 起作用**

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
    exclude 'kotlin.jvm', 'kotlin.internal'
    exclude 'kotlinx.coroutines.internal', 'kotlinx.coroutines.android'
}
android {
    ...
}
```
**提示：合理使用 include 和 exclude 可提高编译速度，建议直接使用 include 设置你项目的相关包名（包括 app 和自定义 module 的）**

**另外设置此处之后由于 Android Studio 可能有缓存，建议重启 AS 并 clean 下项目再继续开发**

### 本库内置了一些功能注解可供你直接使用

| 注解名称             |            参数说明            |                           功能说明                            |
|------------------|:--------------------------:|:---------------------------------------------------------:|
| @SingleClick     |  value = 快速点击的间隔，默认1000ms  |                单击注解，加入此注解，可使你的方法只有单击时才可进入                 |
| @DoubleClick     | value = 两次点击的最大用时，默认300ms  |                 双击注解，加入此注解，可使你的方法双击时才可进入                  |
| @IOThread        |     ThreadType = 线程类型      |             切换到子线程的操作，加入此注解可使你的方法内的代码切换到子线程执行             |
| @MainThread      |            无参数             |             切换到主线程的操作，加入此注解可使你的方法内的代码切换到主线程执行             |
| @OnLifecycle     |  value = Lifecycle.Event   |           监听生命周期的操作，加入此注解可使你的方法内的代码在对应生命周期内才去执行           |
| @TryCatch        |    value = 你自定义加的一个flag    |               加入此注解可为您的方法包裹一层 try catch 代码                |
| @Permission      |      value = 权限的字符串数组      |               申请权限的操作，加入此注解可使您的代码在获取权限后才执行                |
| @CustomIntercept | value = 你自定义加的一个字符串数组的flag | 自定义拦截，配合 AndroidAop.setOnCustomInterceptListener 使用，属于万金油 |

[上述注解使用示例都在这](https://github.com/FlyJingFish/AndroidAOP/blob/master/app/src/main/java/com/flyjingfish/androidaop/MainActivity.kt)

### 这块强调一下 @OnLifecycle

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


### 下面再着重介绍下 @TryCatch @Permission @CustomIntercept

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

👆上边三个监听，最好放到你的 application 中

## 此外本库也同样支持让你自己做切面，实现起来非常简单！

### 本库通过 @AndroidAopPointCut 和 @AndroidAopMatchClassMethod 两种注解，实现自定义切面

#### 一、**@AndroidAopPointCut** 是在方法上通过注解的形式做切面的，上述中注解都是通过这个做的

## [详细使用请看wiki文档](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopPointCut)

⚠️注意：自定义的注解（也就是被 @AndroidAopPointCut 注解的注解类）如果是 Kotlin 代码请用 android-aop-ksp 那个库

下面以 @CustomIntercept 为例介绍下该如何使用

- 创建注解

```java
@AndroidAopPointCut(CustomInterceptCut.class)
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomIntercept {
    String[] value() default {};
}
```

- 创建注解处理切面的类

```kotlin
class CustomInterceptCut : BasePointCut<CustomIntercept> {
    override fun invoke(
        joinPoint: ProceedJoinPoint,
        annotation: CustomIntercept
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

**匹配方法支持精准匹配，👇点击下方链接看使用说明**

## [详细使用请看wiki文档](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopMatchClassMethod)

⚠️注意：自定义的匹配类方法切面（也就是被 @AndroidAopMatchClassMethod 注解的代码）如果是 Kotlin 代码请用 android-aop-ksp 那个库

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

- 例子二

想要监测所有继承自 AppCompatActivity 类的所有 startActivity 的跳转

```java
@AndroidAopMatchClassMethod(
   targetClassName = "androidx.appcompat.app.AppCompatActivity",
   methodName = {"startActivity"},
   type = MatchType.EXTENDS
)
public class MatchActivityMethod implements MatchClassMethod {
    @Nullable
    @Override
    public Object invoke(@NonNull ProceedJoinPoint joinPoint, @NonNull String methodName) {
        // 在此写你的逻辑 
        return joinPoint.proceed();
    }
}
```

可以看到上方 AndroidAopMatchClassMethod 设置的 type 是 MatchType.EXTENDS 表示匹配所有继承自 AppCompatActivity 的子类，另外继承只考虑直接的子类，不考虑子类的子类

**⚠️注意：如果子类没有该方法，则切面无效，另外对同一个类的同一个方法不要做多次匹配，否则只有一个会生效**

#### 匹配切面实用场景：

- 例如你想做退出登陆逻辑时可以使用上边这个，只要在页面内跳转就可以检测是否需要退出登陆

- 又或者你想在三方库某个方法上设置切面，可以直接设置对应类名，对应方法，然后 type = MatchType.SELF，这样可以侵入三方库的代码，当然这么做记得修改上文提到的 androidAopConfig 的配置

## [详细使用请看wiki文档](https://github.com/FlyJingFish/AndroidAOP/wiki)

### 常见问题

1、Build时报错 "ZipFile invalid LOC header (bad signature)"

- 请重启Android Studio，然后 clean 项目


2、 同一个方法存在多个注解或匹配切面时，怎么处理的

- 多个切面叠加到一个方法上时注解优先于匹配切面（下文的匹配切面），注解切面之间从上到下依次执行
- 调用 **[proceed](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint)** 才会执行下一个切面，多个切面中最后一个切面执行 **[proceed](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint)** 才会调用切面方法内的代码
- 在前边切面中调用 **[proceed(args)](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint)** 可更新方法传入参数，并在下一个切面中也会拿到上一层更新的参数
- 最后一个非异步调用 [proceed](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint) 切面的返回值（就是 invoke 的返回值）就是切入方法的返回值；


#### 混淆规则

下边是涉及到本库的一些必须混淆规则

```
# AndroidAop必备混淆规则 -----start-----

-keep @com.flyjingfish.android_aop_core.annotations.* class * {*;}
-keep @com.flyjingfish.android_aop_annotation.anno.* class * {*;}
-keep class * {
    @com.flyjingfish.android_aop_core.annotations.* <fields>;
    @com.flyjingfish.android_aop_annotation.anno.* <fields>;
}
-keepclassmembers class * {
    @com.flyjingfish.android_aop_core.annotations.* <methods>;
    @com.flyjingfish.android_aop_annotation.anno.* <methods>;
}

-keepnames class * implements com.flyjingfish.android_aop_annotation.base.BasePointCut
-keepnames class * implements com.flyjingfish.android_aop_annotation.base.MatchClassMethod
-keep class * implements com.flyjingfish.android_aop_annotation.base.BasePointCut{
    public <init>();
}
-keepclassmembers class * implements com.flyjingfish.android_aop_annotation.base.BasePointCut{
    <methods>;
}

-keep class * implements com.flyjingfish.android_aop_annotation.base.MatchClassMethod{
    public <init>();
}
-keepclassmembers class * implements com.flyjingfish.android_aop_annotation.base.MatchClassMethod{
    <methods>;
}

# AndroidAop必备混淆规则 -----end-----
```

如果你自己写了新的切面代码，记得加上你的混淆规则

如果你用到了 **@AndroidAopPointCut** 做切面，那你需要对你自己写的注解类做如下处理

下边的 **com.flyjingfish.test_lib.annotation** 就是你自定义的注解存放包名，你可以将你的注解类统一放到一个包下

```
# 你自定义的混淆规则 -----start-----
-keep @com.flyjingfish.test_lib.annotation.* class * {*;}
-keep class * {
    @com.flyjingfish.test_lib.annotation.* <fields>;
}
-keepclassmembers class * {
    @com.flyjingfish.test_lib.annotation.* <methods>;
}
# 你自定义的混淆规则 -----end-----
```

如果你用到了 **@AndroidAopMatchClassMethod** 做切面 无需多做处理



### 赞赏

都看到这里了，如果您喜欢 AndroidAOP，或感觉 AndroidAOP 帮助到了您，可以点右上角“Star”支持一下，您的支持就是我的动力，谢谢～ 😃

如果感觉 AndroidAOP 为您节约了大量开发时间、为您的项目增光添彩，您也可以扫描下面的二维码，请作者喝杯咖啡 ☕

<div>
<img src="/screenshot/IMG_4075.PNG" width="280" height="350">
<img src="/screenshot/IMG_4076.JPG" width="280" height="350">
</div>

### 联系方式

* 有问题可以加群大家一起交流 [QQ：641697838](https://qm.qq.com/cgi-bin/qm/qr?k=w2qDbv_5bpLl0lO0qjXxijl3JHCQgtXx&jump_from=webapi&authKey=Q6/YB+7q9BvOGbYv1qXZGAZLigsfwaBxDC8kz03/5Pwy7018XunUcHoC11kVLqCb)

<img src="/screenshot/qq.png" width="220"/>

