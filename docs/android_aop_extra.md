
### Introduce dependent libraries 

```gradle
dependencies {
     //Optional 👇This package provides some common annotation aspects
     implementation 'io.github.FlyJingFish.AndroidAop:android-aop-extra:2.2.3'
}
```

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

- @Permission Use of this annotation must match the following settings (⚠️This step is required, otherwise it will have no effect)[Perfect usage inspiration](https://github.com/FlyJingFish/AndroidAOP/wiki/%E5%88%87%E9%9D%A2%E5%90%AF%E7%A4%BA#4%E7%9B%B8%E4%BF%A1%E5%A4%A7%E5%AE%B6%E5%9C%A8%E4%BD%BF%E7%94%A8%E6%9D%83%E9%99%90-permission-%E6%97%B6%E5%8F%AF%E8%83%BD%E4%BC%9A%E6%83%B3%E7%8E%B0%E5%9C%A8%E5%8F%AA%E6%9C%89%E8%8E%B7%E5%BE%97%E6%9D%83%E9%99%90%E8%BF%9B%E5%85%A5%E6%96%B9%E6%B3%95%E8%80%8C%E6%B2%A1%E6%9C%89%E6%97%A0%E6%9D%83%E9%99%90%E7%9A%84%E5%9B%9E%E8%B0%83%E4%B8%8B%E8%BE%B9%E4%BE%8B%E5%AD%90%E6%95%99%E4%BD%A0%E6%80%8E%E4%B9%88%E5%81%9A)
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