
### 引入依赖库

=== "Groovy"

    ```groovy
    dependencies {
        //非必须项 👇这个包提供了一些常见的注解切面
        implementation "io.github.FlyJingFish.AndroidAop:android-aop-extra:2.6.1"
    }
    ```

=== "Kotlin"

    ```kotlin
    dependencies {
        //非必须项 👇这个包提供了一些常见的注解切面
        implementation("io.github.FlyJingFish.AndroidAop:android-aop-extra:2.6.1")
    }
    ```

### 本库内置了一些功能注解可供你直接使用

| 注解名称                     |                                                参数说明                                                 |                                        功能说明                                         |
|--------------------------|:---------------------------------------------------------------------------------------------------:|:-----------------------------------------------------------------------------------:|
| @SingleClick             |                                      value = 快速点击的间隔，默认1000ms                                       |                             单击注解，加入此注解，可使你的方法只有单击时才可进入                              |
| @DoubleClick             |                                      value = 两次点击的最大用时，默认300ms                                      |                              双击注解，加入此注解，可使你的方法双击时才可进入                               |
| @IOThread                |                                          ThreadType = 线程类型                                          |                          切换到子线程的操作，加入此注解可使你的方法内的代码切换到子线程执行                          |
| @MainThread              |                                                 无参数                                                 |                          切换到主线程的操作，加入此注解可使你的方法内的代码切换到主线程执行                          |
| @OnLifecycle<sup>*</sup> |                                       value = Lifecycle.Event                                       |                        监听生命周期的操作，加入此注解可使你的方法内的代码在对应生命周期内才去执行                        |
| @TryCatch                |                                        value = 你自定义加的一个flag                                         |                            加入此注解可为您的方法包裹一层 try catch 代码                             |
| @Permission<sup>*</sup>  |                                   tag = 自定义标记<br>value = 权限的字符串数组                                   |                            申请权限的操作，加入此注解可使您的代码在获取权限后才执行                             |
| @Scheduled               | initialDelay = 延迟开始时间<br>interval = 间隔<br>repeatCount = 重复次数<br>isOnMainThread = 是否主线程<br>id = 唯一标识 | 定时任务，加入此注解，可使你的方法每隔一段时间执行一次，调用AndroidAop.shutdownNow(id)或AndroidAop.shutdown(id)可停止 |
| @Delay                   |                         delay = 延迟时间<br>isOnMainThread = 是否主线程<br>id = 唯一标识                         |  延迟任务，加入此注解，可使你的方法延迟一段时间执行，调用AndroidAop.shutdownNow(id)或AndroidAop.shutdown(id)可取消  |
| @CheckNetwork            |                tag = 自定义标记<br>toastText = 无网络时toast提示<br>invokeListener = 是否接管检查网络逻辑                |                            检查网络是否可用，加入此注解可使你的方法在有网络才可进去                             |
| @CustomIntercept         |                                     value = 你自定义加的一个字符串数组的flag                                      |              自定义拦截，配合 AndroidAop.setOnCustomInterceptListener 使用，属于万金油              |

( * 支持 suspend 函数，达到条件时返回结果，并支持返回类型不是 Unit 类型的suspend函数)

[上述注解使用示例都在这](https://github.com/FlyJingFish/AndroidAOP/blob/master/app/src/main/java/com/flyjingfish/androidaop/MainActivity.kt#L128),[还有这](https://github.com/FlyJingFish/AndroidAOP/blob/master/app/src/main/java/com/flyjingfish/androidaop/SecondActivity.java#L64),[还有这](https://github.com/FlyJingFish/AndroidAOP/blob/master/app/src/main/java/com/flyjingfish/androidaop/MyApp.java)

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

- @Permission 使用此注解必须配合以下设置（:warning:此步为必须设置的，否则是没效果的）

    💡💡💡[完善使用启示](https://flyjingfish.github.io/AndroidAOP/zh/Implications/#4-permission)

    ```java
    AndroidAop.INSTANCE.setOnPermissionsInterceptListener(new OnPermissionsInterceptListener() {
        @SuppressLint("CheckResult")
        @Override
        public void requestPermission(@NonNull ProceedJoinPoint joinPoint, @NonNull Permission permission, @NonNull OnRequestPermissionListener call) {
            Object target = joinPoint.getTarget();
            String[] permissions = permission.value();
            if (target instanceof FragmentActivity){
                RxPermissions rxPermissions = new RxPermissions((FragmentActivity) target);
                rxPermissions.request(permission.value()).subscribe(call::onCall);
            }else if (target instanceof Fragment){
                RxPermissions rxPermissions = new RxPermissions((Fragment) target);
                rxPermissions.request(permission.value()).subscribe(call::onCall);
            }else {
                // TODO: target 不是 FragmentActivity 或 Fragment ，说明注解所在方法不在其中，请自行处理这种情况
                // 建议：切点方法第一个参数可以设置为 FragmentActivity 或 Fragment ，然后 joinPoint.args[0] 就可以拿到
            }
        }
    });
    ```

    
- @CustomIntercept 使用此注解你必须配合以下设置（:warning:此步为必须设置的，否则还有什么意义呢？）
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

    - 权限是必须加的
      ```xml
      <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
      ```

    - 以下设置为可选设置项
      ```java
      AndroidAop.INSTANCE.setOnCheckNetworkListener(new OnCheckNetworkListener() {
          @Nullable
          @Override
          public Object invoke(@NonNull ProceedJoinPoint joinPoint, @NonNull CheckNetwork checkNetwork, boolean availableNetwork) {
              return null;
          }
      });
      ```

    - 在使用时 invokeListener 设置为true，即可进入上边回调
      ```kotlin
      @CheckNetwork(invokeListener = true)
      fun toSecondActivity(){
          startActivity(Intent(this,SecondActivity::class.java))
      }
      ```

    - 另外内置 Toast 可以让你接管（意思不是说你自己写的 Toast 会走这个回调，而是这个库使用 Toast 时会回调这里）
      ```java
      AndroidAop.INSTANCE.setOnToastListener(new OnToastListener() {
          @Override
          public void onToast(@NonNull Context context, @NonNull CharSequence text, int duration) {
            
          }
      });
      ```

👆以上所有的的监听，最好放到你的 application 中