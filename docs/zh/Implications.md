### 1、不知道大家有没有这样的需求，有一个接口在多处使用，这种情况大家可能写一个工具类封装一下。

其实对于这种需求，可以做一个注解切面，在切面处理时可以在请求完数据后，给切面方法传回去即可，例如：

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
        if(joinPoint.args[0] != null){
            // 有数据，直接继续执行方法
            joinPoint.proceed()
        }else{
            // 没有数据，在这写网络请求数据,数据返回后调用 joinPoint.proceed(data) 把数据传回方法
            HttpData.getInstance().getCountryList(req, new HttpResponeListener<Data>() {
           
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
fun onTest(data:Data){
    //因为切面已经把数据传回来了，所以数据不再为null
}
//在调用方法时随便传个null，当进入到切面后得到数据，在进入方法后数据就有了
binding.btnSingleClick.setOnClickListener {
    onTest(null)
}

```
### 2、另外对于切面注解是没办法传入对象什么的，或者数据是动态的，那怎么办呢？

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
            val arg1 = joinPoint.args[0] // 这个就是传入的数据，这样可以随便往切面内传数据了
            
            
        }
        return joinPoint.proceed()
    }
}

```
```kotlin
@CommonData
fun onTest(number:Int){//num是传入切面动态数据，不限类型
    
}

binding.btnSingleClick.setOnClickListener {
   //在调用方法时传入动态数据
    onTest(1)
}

```

### 3、假如想 Hook 所有的 android.view.View.OnClickListener 的 onClick，说白了就是想全局监测所有的设置 OnClickListener 的点击事件，代码如下：

```kotlin
@AndroidAopMatchClassMethod(
    targetClassName = "android.view.View.OnClickListener",
    methodName = ["onClick"],
    type = MatchType.EXTENDS //type 一定是 EXTENDS 因为你想 hook 所有继承了 OnClickListener 的类
)
class MatchOnClick : MatchClassMethod {
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e("MatchOnClick", "=====invoke=====$methodName")
        return joinPoint.proceed()
    }
}
```

这块提示下，对于使用了 lambda 点击监听的；

ProceedJoinPoint 的 target 不是 android.view.View.OnClickListener

- 对于Java target 是 所在文件最外层的那个类的对象
- 对于Kotlin target 是 null

invoke 回调的 methodName 也不是 onClick 而是编译时自动生成的方法名，类似于这样 onCreate\$lambda\$14 里边包含了 lambda 关键字

对于 onClick(view:View) 的 view

- 如果是 Kotlin 的代码 ProceedJoinPoint.args[1]
- 如果是 Java 的代码 ProceedJoinPoint.args[0]

这块不在继续赘述了，自己用一下就知道了；

**总结下：其实对于所有的 lambda 的 ProceedJoinPoint.args**

- 如果是 Kotlin 第一个参数是切点所在文件最外层的那个类的对象，后边的参数就是 hook 方法的所有参数
- 如果是 Java 从第一个参数开始就是 hook 方法的所有参数

### 4、相信大家在使用权限 `@Permission` 时，可能会想现在只有获得权限进入方法，而没有无权限的回调，下边例子教你怎么做

- 首先定义一个权限拒绝的接口，在这我定义的回调一个是 `@Permission` ，另一个是权限框架返回的结果（在这我用的是rxpermissions，你随意）
    ```kotlin
    interface PermissionRejectListener {
        fun onReject(permission:com.flyjingfish.android_aop_core.annotations.Permission,permissionResult: Permission)
    }
    ```
- 使用 `@Permission` 权限注解，并给其方法所在对象实现 `PermissionRejectListener` 接口
    ```kotlin
    // 在使用 @Permission 的对象上实现 PermissionRejectListener 接口
    class MainActivity: BaseActivity2(), PermissionRejectListener{
        lateinit var binding:ActivityMainBinding
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            binding.btnPermission.setOnClickListener {
                toGetPicture()
            }
            binding.btnPermission2.setOnClickListener {
                toOpenCamera()
            }
        }
    
        @Permission(tag = "toGetPicture",value = [Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE])
        fun toGetPicture(){
            //查找图片
        }
    
        @Permission(tag = "toOpenCamera",value = [Manifest.permission.CAMERA])
        fun toOpenCamera(){
            //打开相机
        }
    
        override fun onReject(permission:Permission,permissionResult: com.tbruyelle.rxpermissions3.Permission) {
            //根据 tag 来区分是哪个方法的权限被拒绝了
            if (permission.tag == "toGetPicture"){
                
            }else if (permission.tag == "toOpenCamera"){
                
            }
        }
    }
    ```
- 在你的 `Application` 上设置请求权限的代码
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
                                    //👇关键在这👇
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
                                    //👇关键在这👇
                                    if (!permissionResult.granted && target is PermissionRejectListener) {
                                        (target as PermissionRejectListener).onReject(
                                            permission,
                                            permissionResult
                                        )
                                    }
                                }
                        }
    
                        else -> {
                            // TODO: target 不是 FragmentActivity 或 Fragment ，说明注解所在方法不在其中，请自行处理这种情况 
                            // 建议：切点方法第一个参数可以设置为 FragmentActivity 或 Fragment ，然后 joinPoint.args[0] 就可以拿到
                        }
                    }
                }
            })
    
        }
    }
    ```

!!! note
    这个技巧的核心点在于 **“解决不能调用切点方法所在对象的方法的问题”** ，通过为切点方法所在对象增加接口的方式，来解决此问题，并且最重要的是**做到了通用**

### 5、三方路由库没有适配 AGP8 ？下面以 ARouter 为例教你如何利用 AndroidAOP 解决这个问题

ARouter 插件库主要干了点什么呢？为什么 AndroidAOP 能解决？其实它的插件库就是做了两件事：
- 1⃣️找三种类。
- 2⃣️自动注册这三种类。

**废话不多说，直接上代码**

```kotlin
object AlibabaCollect {
    private val classNameSet = mutableSetOf<String>()

    @AndroidAopCollectMethod(
        regex = "com.alibaba.android.arouter.routes.*?",
        collectType = CollectType.DIRECT_EXTENDS
    )
    @JvmStatic
    fun collectIRouteRoot(sub: Class<out IRouteRoot>){
        Log.e("AlibabaCollect","collectIRouteRoot=$sub")
        classNameSet.add(sub.name)
    }

    @AndroidAopCollectMethod(
        regex = "com.alibaba.android.arouter.routes.*?",
        collectType = CollectType.DIRECT_EXTENDS
    )
    @JvmStatic
    fun collectIProviderGroup(sub: Class<out IProviderGroup>){
        Log.e("AlibabaCollect","collectIProviderGroup=$sub")
        classNameSet.add(sub.name)
    }

    @AndroidAopCollectMethod(
        regex = "com.alibaba.android.arouter.routes.*?",
        collectType = CollectType.DIRECT_EXTENDS
    )
    @JvmStatic
    fun collectIInterceptorGroup(sub: Class<out IInterceptorGroup>){
        Log.e("AlibabaCollect","collectIInterceptorGroup=$sub")
        classNameSet.add(sub.name)
    }

    fun getClassNameSet() :MutableSet<String>{
        return classNameSet
    }
}
```

> 上边的代码就是 ARouter 的插件干的第1个事，即搜索这三种类

```kotlin
@AndroidAopMatchClassMethod(
    targetClassName = "com.alibaba.android.arouter.core.LogisticsCenter",
    methodName = ["loadRouterMap"],
    type = MatchType.SELF
)
class ARouterMatch :MatchClassMethod {
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        val any = joinPoint.proceed()
        val registerMethod = LogisticsCenter::class.java.getDeclaredMethod("register",java.lang.String::class.java)
        registerMethod.isAccessible = true
        val classNameSet = AlibabaCollect.getClassNameSet()
        classNameSet.forEach {
            registerMethod.invoke(null,it)
            Log.e("ARouterMatch","registerMethod=$it")
        }
        return any
    }
}
```

> 上边就是 ARouter 的插件干的第2个事，就是他在 `ARouter.init(this)` 代码内调用了 `LogisticsCenter.loadRouterMap()` ,把这三种类全部注册进去。仅此而已

经过这边这两步配置，你可以删掉 `classpath "com.alibaba:arouter-register:?"` 了,**值得注意的是，上边的配置只有您关闭了 `androidAop.debugMode = false` 才有效哦～另外因为ARouter不加插件也可以，因此当你测试没问题后依然可以开启debugMode等到打release包时再关闭debugMode即可（最新版本加上这句`androidAop.debugMode.variantOnlyDebug = true`无需手动关闭）**

!!! note
    最后当你调用 `ARouter.init(this)` 后，看到下边的日志之后代表代码已生效！**最后的最后您别忘了对 `com.alibaba.android.arouter.core.LogisticsCenter` 做防混淆处理，因为上边用到了反射**

```console
Load router map by arouter-auto-register plugin.
```

### 综上所述，其实切面能给我们开发带来很多便携之处，关键看大家怎么用了 