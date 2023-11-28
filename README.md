# AndroidAOP

[![Maven central](https://img.shields.io/maven-central/v/io.github.FlyJingFish.AndroidAop/android-aop-core)](https://central.sonatype.com/search?q=io.github.FlyJingFish.AndroidAop)
[![GitHub stars](https://img.shields.io/github/stars/FlyJingFish/AndroidAop.svg)](https://github.com/FlyJingFish/AndroidAop/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/FlyJingFish/AndroidAop.svg)](https://github.com/FlyJingFish/AndroidAop/network/members)
[![GitHub issues](https://img.shields.io/github/issues/FlyJingFish/AndroidAop.svg)](https://github.com/FlyJingFish/AndroidAop/issues)
[![GitHub license](https://img.shields.io/github/license/FlyJingFish/AndroidAop.svg)](https://github.com/FlyJingFish/AndroidAop/blob/master/LICENSE)

### AndroidAOP 是专属于 Android 端 Aop 框架，只需一个注解就可以请求权限、切换线程、禁止多点、监测生命周期等等，**没有使用 AspectJ**，也可以定制出属于你的 Aop 代码，心动不如行动，赶紧用起来吧
## 特色功能

1、本库内置了开发中常用的一些切面注解供你使用

2、本库支持让你自己做切面，语法简单易上手

3、本库同步支持 Java 和 Kotlin 代码

**4、本库没有使用 AspectJ，织入代码量极少，侵入性极低**

##### 声明下：本库没有使用 AspectJ ，目前的功能用不着市面上那些基于 AspectJ 做出来的Aop框架的那些配置，当然将来在增加功能的时候不排除会增加一些可选配置。


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
        classpath 'io.github.FlyJingFish.AndroidAop:android-aop-plugin:1.0.6'
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
    implementation 'io.github.FlyJingFish.AndroidAop:android-aop-core:1.0.6'
    implementation 'io.github.FlyJingFish.AndroidAop:android-aop-annotation:1.0.6'
    //非必须项 👇，如果你想自定义切面需要用到，⚠️支持Java和Kotlin代码写的切面
    ksp 'io.github.FlyJingFish.AndroidAop:android-aop-ksp:1.0.6'
    //非必须项 👇，如果你想自定义切面需要用到，⚠️只适用于Java代码写的切面
    annotationProcessor 'io.github.FlyJingFish.AndroidAop:android-aop-processor:1.0.6'
    //⚠️上边的 android-aop-ksp 和 android-aop-processor 二选一
}
```
**提示：ksp 或 annotationProcessor只是在当前 module 起作用**

#### 四、可选配置项

```gradle
androidAopConfig {
    // enabled 为false 切面不再起作用
    enabled true 
    // include 不设置默认全部扫描，设置后只扫描设置的包名的代码
    include 'com.flyjingfish.androidaop'
    include 'com.flyjingfish.test_lib'
    // exclude 是扫描时排除的包
    // 移除kotlin相关，编译错误和提升速度
    exclude 'kotlin.jvm', 'kotlin.internal'
    exclude 'kotlinx.coroutines.internal', 'kotlinx.coroutines.android'
}
```
**提示：合理使用 include 和 exclude 可提高编译速度**

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

**@OnLifecycle 加到的位置必须是属于直接或间接继承自 FragmentActivity 或 Fragment的方法才有用（即这个方法是直接或间接继承FragmentActivity 或 Fragment的类的）或者注解方法的对象实现LifecycleOwner也可以**

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

### 此外本库也同样支持让你自己做切面，语法相对来说也比较简单

## 本库中提供了 @AndroidAopPointCut 和 @AndroidAopMatchClassMethod 两种切面供你使用

- **@AndroidAopPointCut** 是只能在方法上做切面的，上述中注解都是通过这个做的

下面以 @CustomIntercept 为例介绍下该如何使用（⚠️注意：自定义的注解如果是 Kotlin 代码请用 android-aop-ksp 那个库）

```java
@AndroidAopPointCut(CustomInterceptCut.class)
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomIntercept {
    String[] value() default {};
}
```
**@AndroidAopPointCut** 的 **CustomInterceptCut.class** 为您处理切面的类

@Target 的 ElementType.METHOD 表示作用在方法上

@Retention 只可以用 RetentionPolicy.RUNTIME

@Target 只可以传 ElementType.METHOD传其他无作用

CustomInterceptCut 的代码如下：

CustomInterceptCut 继承自 BasePointCut，可以看到 BasePointCut 上有一泛型，这个泛型就是上边的 CustomIntercept 注解，两者是互相关联的
```kotlin
class CustomInterceptCut : BasePointCut<CustomIntercept> {
    override fun invoke(
        joinPoint: ProceedJoinPoint,
        annotation: CustomIntercept
    ): Any? {
        // 在此写你的逻辑
        return joinPoint.proceed()
    }
}
```


在这介绍下 在使用 ProceedJoinPoint 这个对象的 proceed() 或 proceed(args) 表示执行原来方法的逻辑，区别是：

- proceed() 不传参，表示不改变当初的传入参数
- proceed(args) 有参数，表示改写当时传入的参数
- 不调用 proceed 就不会执行拦截切面方法内的代码

在此的return 返回的就是对应拦截的那个方法返回的

**另外请注意尽量不要把切面注解放到系统方法上，例如：Activity 的 onCreate() onResume() 等**
**即便是加了在切面处理时不要有耗时操作，joinPoint.proceed() 要正常执行，否则会出现意想不到的问题，例如：ANR**


PS：ProceedJoinPoint.target 如果为null的话是因为注入的方法是静态的，通常只有java才会这样

- **@AndroidAopMatchClassMethod** 是做匹配继承自某类及其对应方法的切面的（⚠️注意：自定义的匹配类方法切面如果是 Kotlin 代码请用 android-aop-ksp 那个库）

```java
@AndroidAopMatchClassMethod(targetClassName = "androidx.appcompat.app.AppCompatActivity",methodName = {"startActivity"})
public class MatchActivityMethod implements MatchClassMethod {
    @Nullable
    @Override
    public Object invoke(@NonNull ProceedJoinPoint joinPoint, @NonNull String methodName) {
        Log.e("MatchActivityMethod","=====invoke====="+methodName);
        return joinPoint.proceed();
    }
}
```

其对应的就是下边的代码
```kotlin
abstract class BaseActivity :AppCompatActivity() {

    override fun startActivity(intent: Intent?, options: Bundle?) {
        super.startActivity(intent, options)
    }
}
```

上边表示凡是继承自 androidx.appcompat.app.AppCompatActivity 的类执行 startActivity 方法时则进行切面

⚠️注意如果你没写对应的方法或者没有重写父类的该方法则切面无效

例如你想做退出登陆逻辑时可以使用上边这个，只要在页面内跳转就可以检测是否需要退出登陆


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

-keepnames class * implements com.flyjingfish.android_aop_annotation.BasePointCut
-keepnames class * implements com.flyjingfish.android_aop_annotation.MatchClassMethod
-keep class * implements com.flyjingfish.android_aop_annotation.BasePointCut{
    public <init>();
}
-keepclassmembers class * implements com.flyjingfish.android_aop_annotation.BasePointCut{
    <methods>;
}

-keep class * implements com.flyjingfish.android_aop_annotation.MatchClassMethod{
    public <init>();
}
-keepclassmembers class * implements com.flyjingfish.android_aop_annotation.MatchClassMethod{
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

如果你用到了 **@AndroidAopMatchClassMethod** 做切面，那你需要为切面内的方法做混淆处理
下面是上文提到的 **MatchActivityOnCreate** 类的匹配规则，对应的逻辑是 匹配的 为继承自 com.flyjingfish.test_lib.BaseActivity 的类的 onCreate ，onResume，onTest三个方法加入切面

```
-keepnames class * extends com.flyjingfish.test_lib.BaseActivity{
    void onCreate(...);
    void onResume(...);
    void onTest(...);
}
```

### 常见问题

1、Build时报错 "ZipFile invalid LOC header (bad signature)"

- 请重启Android Studio，然后 clean 项目

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

