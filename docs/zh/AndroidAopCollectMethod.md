## 简述


此切面是收集继承类或符合正则表达式的类，其注解的方法将会在您第一次使用所在类时自动回调

```kotlin
@AndroidAopCollectMethod(
    /**
     * 收集的类型
     */
    collectType = CollectType.DIRECT_EXTENDS,
    /**
     * 这一项是正则表达式
     * 设置了正则表达式之后，注解方法的参数可以是 Object 或 Any ，不设置则必须指定类型
     * 设置正则表达式之后会按照您设置的正则表达式，去查找符合要求的类名
     */
    regex = ""
)
```





### 注解的方法的填写要求

- 所修饰方法必须是静态方法，不设返回值类型

- 它直接修饰方法，并且修饰的方法有且只有一个参数，此功能就是收集应用内所有继承这个参数的类，参数类型如果是：
    - 对象，则只收集非抽象类，非接口的子类；**必须有默认的无参构造方法才可以，否则会有异常**
    - Class，则无论接口还是抽象类都会收集；**对构造方法参数无限制**

- 另外其注解的方法收集到的每一个类只会通过这个静态方法初始化回调一次。
    - 初始化时机，就是你第一次使用这个类的时候，即类被初始化的时候。
    - 不使用不会初始化因此它是“懒加载”，并且线程安全！
    - 纯静态织入方式，没有任何反射，性能更好。

- 最后的一点就是这个方法内尽量只有相关的保存代码，不要做其他的操作，尽量避开出现异常的行为（因为你只有一次接收机会……）

### collectType  默认 `DIRECT_EXTENDS`，可以设置以下三种类型

- ```EXTENDS``` 表示匹配的是**所有继承于** 注解方法参数 所设置的类
- ```DIRECT_EXTENDS``` 表示匹配的是 **<em><strong>直接继承于</strong></em>** 注解方法参数 所设置的类
- ```LEAF_EXTENDS``` 表示匹配的是 **<em><strong>末端继承（就是没有子类了）</strong></em>** 注解方法参数 所设置的类

> **💡💡💡如果设置参数为Object或Any时，则此项设置将会被忽略，但是 `regex` 必须要填写**


### regex 填写 `正则表达式`

设置正则表达式之后会按照您设置的正则表达式，去查找符合要求的类名

- 设置了正则表达式之后，注解方法的参数可以是 Object 或 Any ,可以看下边的例子
- 不设置正则表达式，则必须指定类型

## 使用方式

使用起来极其简单，示例代码已经说明了

- Kotlin

```kotlin
object InitCollect {
    private val collects = mutableListOf<SubApplication>()
    private val collectClazz: MutableList<Class<out SubApplication>> = mutableListOf()

    @AndroidAopCollectMethod
    @JvmStatic
    //收集继承自 SubApplication 的类，并回调他的实例对象
    fun collect(sub: SubApplication){
      collects.add(sub)
    }

    @AndroidAopCollectMethod
    @JvmStatic
    //收集继承自 SubApplication 的类，并回调他的 class 对象
    fun collect2(sub:Class<out SubApplication>){
      collectClazz.add(sub)
    }

    @AndroidAopCollectMethod(regex = ".*?\\$\\\$Router")
    @JvmStatic
    //收集符合 regex 正则表达式的类，并回调他的 class 对象。亦可结合继承使用
    fun collectRouterClassRegex(sub:Class<out Any>){
        Log.e("InitCollect", "----collectRouterClassRegexClazz----$sub")
    }

    @AndroidAopCollectMethod(regex = ".*?\\$\\\$Router")
    @JvmStatic
    //收集符合 regex 正则表达式的类，并回调他的实例对象。亦可结合继承使用
    fun collectRouterClassRegex(sub:Any){
        Log.e("InitCollect", "----collectRouterClassRegexObject----$sub")
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

    @AndroidAopCollectMethod(regex = ".*?\\$\\$Router")
    public static void collectRouterClassRegex(Object sub){
        Log.e("InitCollect2","----collectRouterClassRegexObject----"+sub);
    }

    @AndroidAopCollectMethod(regex = ".*?\\$\\$Router")
    public static void collectRouterClassRegex(Class<?> sub){
        Log.e("InitCollect2","----collectRouterClassRegexClazz----"+sub);
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

使用这个收集类
```java

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        InitCollect.init(this);
    }
}
```

## 应用场景

- 多个module需要使用Application的，例如上述的例子

- 对于有兴趣想要自己实现 Router 的，这个切面可以帮你找到你想要的

- 适配路由库[例如这个 ARouter 适配 AGP8 的样例](https://github.com/FlyJingFish/AndroidAOP/wiki/%E5%88%87%E9%9D%A2%E5%90%AF%E7%A4%BA#5%E4%B8%89%E6%96%B9%E8%B7%AF%E7%94%B1%E5%BA%93%E6%B2%A1%E6%9C%89%E9%80%82%E9%85%8D-agp8-%E4%B8%8B%E9%9D%A2%E4%BB%A5-arouter-%E4%B8%BA%E4%BE%8B%E6%95%99%E4%BD%A0%E5%A6%82%E4%BD%95%E5%88%A9%E7%94%A8-androidaop-%E8%A7%A3%E5%86%B3%E8%BF%99%E4%B8%AA%E9%97%AE%E9%A2%98)

- 既线程安全又是懒加载的单例的又一种新的方式！
```java
public class TestInstance {
    private static TestInstance instance;
    
    @AndroidAopCollectMethod(
            regex = "^com.flyjingfish.lightrouter.TestInstance$"
    )
    public static void collectInstance(Object any){
        instance = (TestInstance) any;
    }

    public static TestInstance getInstance() {
        return instance;
    }

    public void test(){
        Log.e("TestInstance","=====test=");
    }
}
```

- 更多应用场景待你探索

