## Brief description

This aspect collects inherited classes or classes that match regular expressions. Its annotated methods will be automatically called back when you use the class for the first time

```kotlin
@AndroidAopCollectMethod(
    /**
     * Collection type
     */
    collectType = CollectType.DIRECT_EXTENDS,
    /**
     * This item is a regular expression
     * After setting the regular expression, the parameter of the annotated method can be Object or Any. If it is not set, the type must be specified
     * After setting the regular expression, the regular expression you set will be used to find the class name that meets the requirements
     */
    regex = ""
)
```

### Filling requirements for annotated methods

- The modified method must be a static method, and the return value type is not set

- It directly modifies the method, and the modified method has one and only one parameter. This function is to collect all classes in the application that inherit this parameter. If the parameter type is:
    - Object, only non-abstract classes and non-interface subclasses are collected; **There must be a default no-parameter constructor, otherwise there will be an exception**
    - Class, both interfaces and abstract classes will be collected; **No restrictions on constructor parameters**

- In addition, each class collected by its annotated method will only be initialized and called back once through this static method.
    - The initialization time is when you use this class for the first time, that is, when the class is initialized.
    - It will not be initialized if not used, so it is "lazy loading" and thread-safe!
    - Pure static weaving method, without any reflection, better performance.

- The last point is that this method should only contain relevant saving code, and do not perform other operations, and try to avoid abnormal behavior (because you only have one chance to receive...)

### collectType defaults to `DIRECT_EXTENDS`, and the following three types can be set

- ```EXTENDS``` means that it matches **all classes inherited from** annotation method parameters set

- ```DIRECT_EXTENDS``` means that it matches **<em><strong>directly inherited from</strong></em>** annotation method parameters set

- ```LEAF_EXTENDS``` means that it matches **<em><strong>terminal inheritance (that is, no subclasses)</strong></em>** annotation method parameters set

> **💡💡💡If the parameter is set to Object or Any, this setting will be ignored, but `regex` must be filled in**

### regex fill in `Regular expression`

After setting the regular expression, the class name that meets the requirements will be found according to the regular expression you set

- After setting the regular expression, the parameter of the annotation method can be Object or Any, as shown in the example below

- If you do not set the regular expression, you must specify the type

## How to use

It is extremely simple to use, and the sample code has already explained it

- Kotlin

```kotlin
object InitCollect {
    private val collects = mutableListOf<SubApplication>()
    private val collectClazz: MutableList<Class<out SubApplication>> = mutableListOf()

    @AndroidAopCollectMethod
    @JvmStatic
//Collect classes inherited from SubApplication and call back its instance object
    fun collect(sub: SubApplication) {
        collects.add(sub)
    }

    @AndroidAopCollectMethod
    @JvmStatic
//Collect classes inherited from SubApplication and call back its class object
    fun collect2(sub: Class<out SubApplication>) {
        collectClazz.add(sub)
    }

    @AndroidAopCollectMethod(regex = ".*?\\$\\\$Router")
    @JvmStatic
//Collect classes that match the regex regular expression and call back their class objects. Can also be used in conjunction with inheritance
    fun collectRouterClassRegex(sub: Class<out Any>) {
        Log.e("InitCollect", "----collectRouterClassRegexClazz----$sub")
    }

    @AndroidAopCollectMethod(regex = ".*?\\$\\\$Router")
    @JvmStatic
//Collect classes that match the regex regular expression and call back their instance objects. Can also be used in combination with inheritance
    fun collectRouterClassRegex(sub: Any) {
        Log.e("InitCollect", "----collectRouterClassRegexObject----$sub")
    }

    //Directly call this method (method name is not limited) The above functions will be called back
    fun init(application: Application) {
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
    public static void collect(SubApplication2 sub) {
        collects.add(sub);
    }

    @AndroidAopCollectMethod
    public static void collect3(Class<? extends SubApplication2> sub) {
        collectClazz.add(sub);
    }

    @AndroidAopCollectMethod(regex = ".*?\\$\\$Router")
    public static void collectRouterClassRegex(Object sub) {
        Log.e("InitCollect2", "----collectRouterClassRegexObject----" + sub);
    }

    @AndroidAopCollectMethod(regex = ".*?\\$\\$Router")
    public static void collectRouterClassRegex(Class<?> sub) {
        Log.e("InitCollect2", "----collectRouterClassRegexClazz----" + sub);
    }

    //Directly call this method (method name is not limited) The above functions will be called back in full
    public static void init(Application application) {
        Log.e("InitCollect2", "----init----");
        for (SubApplication2 collect : collects) {
            collect.onCreate(application);
        }
    }
}
```

Use this collection class
```java

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        InitCollect.init(this);
    }
}
```

## Application scenario

- Multiple modules need to use Application, such as the above example

- For those who are interested in implementing Router by themselves, this aspect can help you find what you want

- Adapting routing library [such as this ARouter adapts AGP8 Example](https://github.com/FlyJingFish/AndroidAOP/wiki/%E5%88%87%E9%9D%A2%E5%90%AF%E7%A4%BA#5%E4%B8%89%E6%96%B9%E8%B7%AF%E7%94%B1%E5%BA%93%E6%B2%A1%E6%9C%89%E9%80%82%E9%85%8D-agp8-%E4%B8%8B%E9%9D%A2%E4%BB%A5-arouter-%E4%B8%BA%E4%BE%8B%E6%95%99%E4%BD%A0%E5%A6%82%E4%BD%95%E5%88%A9%E7%94%A8-androidaop-%E8%A7%A3%E5%86%B3%E8%BF%99%E4%B8%AA%E9%97%AE%E9%A2%98)

- Another new way to use singletons that are both thread-safe and lazy-loaded!
```java 
public class TestInstance {
    private static TestInstance instance;

    @AndroidAopCollectMethod(regex = "^com.flyjingfish.lightrouter.TestInstance$")
    public static void collectInstance(Object any) {
        instance = (TestInstance) any;
    }

    public static TestInstance getInstance() {
        return instance;
    }

    public void test() {
        Log.e("TestInstance", "=====test=");
    }
} 
``` 
- More application scenarios are waiting for you to explore