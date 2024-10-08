### 1. I don’t know if you have such a requirement. There is an interface used in multiple places. In this case, you may write a tool class to encapsulate it.

In fact, for this kind of demand, you can make an annotation aspect. When processing the aspect, you can pass the data back to the aspect method after requesting it, for example:

```kotlin
@AndroidAopPointCut(CommonDataCut::class)
@Target(
    AnnotationTarget.FUNCTION
)
@Retention(AnnotationRetention.RUNTIME)
@Keep
annotation class CommonData
```
```kotlin
class CommonDataCut : BasePointCut<CommonData> {
    override fun invoke(
        joinPoint: ProceedJoinPoint,
        anno: CommonData
    ): Any? {
        if (joinPoint.args[0] != null) {
// If there is data, continue to execute the method directly
            joinPoint.proceed()
        } else {
// If there is no data, write the network request data here, and call joinPoint.proceed(data) after the data is returned to pass the data back to the method
            HttpData.getInstance().getCountryList(req, new HttpResponeListener < Data >() {

                @Override
                public void onSuccess(String url, Data response) {
                    joinPoint.proceed(response)
                }

            });
        }

        return null
    }
}
```
```kotlin
@CommonData
fun onTest(data: Data) {
//Because the facet has passed the data back, the data is no longer null
}
//When calling the method, just pass null, and get the data after entering the facet. After entering the method, the data will be available
binding.btnSingleClick.setOnClickListener {
    onTest(null)
}

```
### 2. In addition, there is no way to pass objects to the facet annotation, or the data is dynamic, so what should I do?

```kotlin
@AndroidAopPointCut(CommonDataCut::class)
@Target(
    AnnotationTarget.FUNCTION
)
@Retention(AnnotationRetention.RUNTIME)
@Keep
annotation class CommonData

```
```kotlin
class CommonDataCut : BasePointCut<CommonData> {
    override fun invoke(
        joinPoint: ProceedJoinPoint,
        anno: CommonData
    ): Any? {
        if (!joinPoint.args.isNullOrEmpty()) {
            val arg1 =
                joinPoint.args[0] // This is the incoming data, so you can pass data to the slice at will

        }
        return joinPoint.proceed()
    }
}

```
```kotlin
@CommonData
fun onTest(number: Int) {//num is the dynamic data of the incoming slice, regardless of type

}

binding.btnSingleClick.setOnClickListener {
//Input dynamic data when calling the method
    onTest(1)
}

```

### 3. If you want to hook all onClick of android.view.View.OnClickListener, in other words, you want to globally monitor all click events of OnClickListener. The code is as follows:

```kotlin
@AndroidAopMatchClassMethod(
    targetClassName = "android.view.View.OnClickListener",
    methodName = ["onClick"],
    type = MatchType.EXTENDS //type must be EXTENDS because you want to hook all classes that inherit OnClickListener
)
class MatchOnClick : MatchClassMethod {
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e("MatchOnClick", "=====invoke=====$methodName")
        return joinPoint.proceed()
    }
}
```
Here is a tip for using lambda click listener;

The target of ProceedJoinPoint is not android.view.View.OnClickListener

- For Java, the target is the object of the outermost class of the file
- For Kotlin, the target is null

The methodName of the invoke callback is not onClick but the method name automatically generated during compilation, similar to onCreate$lambda$14, which contains the lambda keyword

For the view of onClick(view:View)

- If it is Kotlin code, ProceedJoinPoint.args[1]
- If it is Java code, ProceedJoinPoint.args[0]

I will not go into details about this, you will know it after using it yourself;

**To summarize: In fact, for all lambda's ProceedJoinPoint.args**

- If it is Kotlin, the first parameter is the object of the outermost class of the file where the cut point is located, and the subsequent parameters are all the parameters of the hook method
- If it is Java, it starts from the first parameter and is hook All parameters of the method

### 4. I believe that when you use the permission `@Permission`, you may think that now you only get permission to enter the method, but there is no callback without permission. The following example teaches you how to do it

- First, define an interface for permission rejection. Here, I define one callback as `@Permission` and the other as the result returned by the permission framework (here I use rxpermissions, you can use it at will)
    ```kotlin
    interface PermissionRejectListener {
        fun onReject(
            permission: com.flyjingfish.android_aop_core.annotations.Permission,
            permissionResult: Permission
        )
    }
    ```
- Use the `@Permission` permission annotation and implement the `PermissionRejectListener` interface for the object where its method is located
    ```kotlin
    // Implement PermissionRejectListener on the object using @Permission Interface 
    class MainActivity : BaseActivity2(), PermissionRejectListener {
        lateinit var binding: ActivityMainBinding
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState) binding = ActivityMainBinding . inflate (layoutInflater) setContentView (binding.root) binding . btnPermission . setOnClickenerListener { toGetPicture() } binding . btnPermission2 . setOnClickListener { toOpenCamera() }
        }
    
        @Permission(
            tag = "toGetPicture",
            value = [Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE]
        )
        fun toGetPicture() { //Find pictures } @Permission(tag = "toOpenCamera",value = [Manifest.permission.CAMERA]) fun toOpenCamera(){
    //Open the camera
        }
    
        override fun onReject(
            permission: Permission,
            permissionResult: com.tbruyelle.rxpermissions3.Permission
        ) {
    //Use the tag to distinguish which method's permission is denied
            if (permission.tag == "toGetPicture") {
    
            } else if (permission.tag == "toOpenCamera") {
    
            }
        }
    }
    ```
- Set the code for requesting permissions on your `Application`
    ```kotlin
    class MyApp2 : Application() {
        override fun onCreate() {
            super.onCreate()
            AndroidAop.setOnPermissionsInterceptListener(object : OnPermissionsInterceptListener {
                @SuppressLint("CheckResult")
                override fun requestPermission(
                    joinPoint: ProceedJoinPoint,
                    permission: Permission,
                    call: OnRequestPermissionListener
                ) {
                    val target = joinPoint.getTarget()
                    val permissions: Array<out String> = permission.value
                    when (target) {
                        is FragmentActivity -> {
                            val rxPermissions = RxPermissions((target as FragmentActivity?)!!)
                            rxPermissions.requestEach(*permissions)
                                .subscribe { permissionResult: com.tbruyelle.rxpermissions3.Permission ->
                                    call.onCall(permissionResult.granted)
                                    //👇The key is here👇
                                    if (!permissionResult.granted && target is PermissionRejectListener) {
                                        (target as PermissionRejectListener).onReject(
                                            permission,
                                            permissionResult
                                        )
                                    }
                                }
                        }
    
                        is Fragment -> {
                            val rxPermissions = RxPermissions((target as Fragment?)!!)
                            rxPermissions.requestEach(*permissions)
                                .subscribe { permissionResult: com.tbruyelle.rxpermissions3.Permission ->
                                    call.onCall(permissionResult.granted)
                                    //👇The key is here👇
                                    if (!permissionResult.granted && target is PermissionRejectListener) {
                                        (target as PermissionRejectListener).onReject(
                                            permission,
                                            permissionResult
                                        )
                                    }
                                }
                        }
    
                        else -> {
                            // TODO: target is not FragmentActivity or Fragment, indicating that the method where the annotation is located is not in it. Please handle this situation yourself.
                            // Suggestion: The first parameter of the cut point method can be set to FragmentActivity or Fragment, and then joinPoint.args[0] can be obtained
                        }
                    }
                }
            })
    
        }
    }
    ```


!!! note
    The core of this technique is **"solving the problem of not being able to call the method of the object where the pointcut method is located"**, solving this problem by adding an interface to the object where the pointcut method is located, and most importantly, **making it universal**

### 5. Is the third-party routing library not compatible with AGP8? Here is an example of ARouter to teach you how to use AndroidAOP to solve this problem

What does the ARouter plugin library mainly do? Why can AndroidAOP solve this problem? In fact, its plugin library does two things:

1. Find three categories.
2. Automatically register these three categories.

**No more nonsense, let’s go straight to the code** 
```kotlin 
object AlibabaCollect {
    private val classNameSet =
        mutableSetOf<String>() @AndroidAopCollectMethod(regex = "com.alibaba.android.arouter.routes.*?", collectType = CollectType.DIRECT_EXTENDS) @JvmStatic
    fun collectIRouteRoot(sub: Class<out IRouteRoot>) {
        Log.e("A libabaCollect", "collectIRouteRoot=$sub") classNameSet . add (sub.name)
    }
    @AndroidAopCollectMethod(
        regex = "com.alibaba.android.arouter.routes.*?",
        collectType = CollectType.DIRECT_EXTENDS
    )
    @JvmStatic
    fun collectIProviderGroup(sub: Class<out IProviderGroup>) {
        Log.e("AlibabaCollect", "collectIProviderGroup=$sub")
        classNameSet.add(sub.name)
    }

    @AndroidAopCollectMethod(
        regex = "com.alibaba.android.arouter.routes.*?",
        collectType = CollectType.DIRECT_EXTENDS
    )
    @JvmStatic
    fun collectIInterceptorGroup(sub: Class<out IInterceptorGroup>) {
        Log.e("AlibabaCollect", "collectIInterceptorGroup=$sub")
        classNameSet.add(sub.name)
    }

    fun getClassNameSet(): MutableSet<String> {
        return classNameSet
    }
}
```

> The code above is the first thing that the ARouter plugin does, which is to search for these three categories

```kotlin
@AndroidAopMatchClassMethod(
    targetClassName = "com.alibaba.android.arouter.core.LogisticsCenter",
    methodName = ["loadRouterMap"],
    type = MatchType.SELF
)
class ARouterMatch : MatchClassMethod {
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        val any = joinPoint.proceed()
        val registerMethod = LogisticsCenter::class.java.getDeclaredMethod(
            "register",
            java.lang.String::class.java
        ) registerMethod . isAccessible = true
        val classNameSet = AlibabaCollect.getClassNameSet() classNameSet . forEach {
            registerMethod.invoke(
                null,
                it
            ) Log . e ("ARouterMatch", "registerMethod=$it")
        } return any
    }
} 
``` 
> The above is the second thing done by the ARouter plug-in. `ARouter.init(this)` code calls `LogisticsCenter.loadRouterMap()` to register all three types. That's all.

After these two steps of configuration, you can delete `classpath "com.alibaba:arouter-register:?"`. **It is worth noting that the above configuration is only effective if you turn off `androidAop.debugMode = false`. In addition, because ARouter can be used without plugins, you can still turn on debugMode after testing and turn it off when you build the release package (the latest version adds this sentence `androidAop.debugMode.variantOnlyDebug = true` without manual closing)**

!!! note
    Finally, when you call `ARouter.init(this)`, you will see the log below, which means the code has taken effect! **Finally, don't forget to do anti-obfuscation processing on `com.alibaba.android.arouter.core.LogisticsCenter`, because reflection is used above**

```console
Load router map by arouter-auto-register plugin.
```

### In summary, in fact, the aspect can bring a lot of portability to our development, the key is to see how everyone uses it