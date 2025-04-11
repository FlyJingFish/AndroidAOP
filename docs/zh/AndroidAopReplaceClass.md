## 简述（这个功能属于高阶功能，使用时需要特别注意否则会无效）

此切面是做替换代码中的方法调用的，需要与@AndroidAopReplaceMethod配合使用，被替换方法调用时会进入@AndroidAopReplaceMethod注解的方法

- _注意这种方式和其他两种有着本质的区别，前两种关注的是方法的执行，并且会自动保留可以执行原有逻辑的方法（即[ProceedJoinPoint](/AndroidAOP/zh/ProceedJoinPoint)）；_
- _这个关注的是方法的调用，是将所有调用的地方替换为您设置的类的静态方法，并且不会自动保留执行原有逻辑的方法_
- **_这个方式的优点在于“相当于”可以监测到某些系统方法（android.jar里的代码）的调用，前两者不具备这个特点，所以如果不是基于此种需求，建议使用 [@AndroidAopMatchClassMethod](/AndroidAOP/zh/AndroidAopMatchClassMethod)_**


!!! note
    <li>综上所述这个功能可以说是[@AndroidAopMatchClassMethod](/AndroidAOP/zh/AndroidAopMatchClassMethod)的一种补充（android.jar里的代码无法织入AOP代码），之所以不使用[ProceedJoinPoint](/AndroidAOP/zh/ProceedJoinPoint)也是因为这种方法可能受到Android不同版本的限制，既不可以使用反射来调用原方法，也不可以织入AOP代码，所以就不可以去用[ProceedJoinPoint](/AndroidAOP/zh/ProceedJoinPoint)封装了，如果实在想用，建议使用 [MatchClassMethodProxy](#4androidaopmatchclassmethod) </li><br> <li><strong>当你修改这个切面的相关配置后你应该clean项目后再继续开发</strong></li>


## 一、说明

:warning::warning::warning: 注意：定义的替换类要放到你设置的扫描规则之内 [就是这里 include exclude 的规则](/AndroidAOP/zh/getting_started/#app-buildgradle-androidaopconfig)，写在范围以外是不起作用的

### @AndroidAopReplaceClass

```kotlin
@AndroidAopReplaceClass(
    value = "类的全称（包括包名）"
    type = 匹配类型，非必须，默认 SELF
    excludeClasses = 排除继承关系中的一些类的数组（type 不是 SELF 才有效），非必须
)
```

- value 填写的类名如果是内部类时不使用`$`字符，而是用`.`

- 注解的类就是替换类；参数填写的是被替换调用的类

- 注解的类替换方法内存在对应的被替换方法时不会参与方法替换

- type 有四种类型（不设置默认 ```SELF```，注意区分和 ```@AndroidAopMatchClassMethod``` 的区别，两者不设置时默认的类型不一样）：
    - ```SELF``` 表示匹配的是 value 所设置类的 **自身**
    - ```EXTENDS``` 表示匹配的是 **所有继承于** value 所设置的类
    - ```DIRECT_EXTENDS``` 表示匹配的是 _**直接继承于**_ value 所设置的类
    - ```LEAF_EXTENDS``` 表示匹配的是 _**末端继承（就是没有子类了）**_ value 所设置的类
  
    ``` mermaid
    graph LR
    C(C 类) ---> |C类继承于B类| B{ B 类 };
    B --->|B类继承于A类| A[A 类];
    B --->|DIRECT_EXTENDS / EXTENDS| A;
    C ---->|LEAF_EXTENDS / EXTENDS| A;
    D(D 类) --->|D类继承于A类| A;
    D --->|DIRECT_EXTENDS/ LEAF_EXTENDS / EXTENDS| A;
    ```

    简单来说，```LEAF_EXTENDS```和```DIRECT_EXTENDS```是两个极端，前者关注的是继承关系中最后一个节点，后者关注的是继承关系中第一个节点。



### @AndroidAopReplaceNew

```kotlin
@AndroidAopReplaceNew
```

举个例子这个方法就是将 `new Thread()` 变成 `new MyThread()`

- 注解的方法必须是公共且静态的，但方法名随便定义

- 方法参数只能是一个，并且参数是替换后的类
    - 替换后的类没有继承替换类，那么接下来对象的方法调用，将会出现问题，你需要把他的所有方法使用 @AndroidAopReplaceMethod 全部替换掉
    - 替换后的类继承替换类，那么后续的方法调用基本没什么问题

- 方法返回类型是否为空
    - 不为空，则会将new出来的对象回调到这个方法，并且此方法返回的对象也将会替换 new 出来的对象。通常返回类型不为空时你应该定义的和参数类型一样。
    - 为空只会替换 new 之后的类名。
    - 无论如何 new 后的类名都会变成方法的参数类型。
- 这将换掉全部的构造方法的调用。


### @AndroidAopReplaceMethod

```kotlin
@AndroidAopReplaceMethod(
        "方法名（包括返回值类型和参数类型）"
)
```

- 注解的方法必须是公共且静态的，但方法名随便定义

- 注解的方法就是替换的方法；参数填写的是被替换的方法，必须包含返回类型和参数类型，填写匹配规则如下[匹配规则](#_3)

- 填写的被替换的方法如果是类的静态方法，那么你定义的替换方法的参数类型、顺序以及个数保持一致

- 填写的被替换的方法如果是类的成员方法，那么你定义的替换方法的参数第一个需要是被替换的类的类型（下文的Toast.show例子就是这个意思），然后剩余的参数类型、顺序以及个数和被替换方法保持一致
    - 有个例外情况：第一个类型可以设置成 Any (Java是Object)，这个功能主要为 @AndroidAopReplaceNew 不是继承替换类做呼应的，因为替换后的类不再属于被替换类

- 注解的方法返回类型和被替换方法保持一致，无论被替换方法是否是静态的都是一样的

- 填写的被替换的方法必须是属于 @AndroidAopReplaceClass 填写的被替换类的方法

- 如果填写的被替换的方法以 `<init>` 开头，功能分两种情况
    - 1、按以下要求填写，功能类似 @AndroidAopReplaceNew ，不同的是这只会回调 new 出来的类，不会改变 new 的类名，而且可以指定构造方法。（对象已经被创建出来了）
        - 方法必须只有一个参数就是定义的被替换类（必须等于@AndroidAopReplaceClass 的类）
        - 并且返回类型不可以为空（必须继承或等于@AndroidAopReplaceClass 的类）
        - 方法返回的对象将会替换new的对象（当然直接返回回调进来对象也可以）
    - 2、按以下要求填写，功能与之前所述完全不同，此时对象尚未创建出来（此功能在2.5.8及以上的版本开始使用）
        - 方法所定义参数 **最后一个** 参数必须是Class类型，其余参数必须和构造方法的参数类型和顺序完全一致
        - 需要你手动重新去写创建对象的代码，因为这种情况尚未创建出对象来
        - 并且返回类型不可以为空（必须继承或等于@AndroidAopReplaceClass 的类）
        - 方法返回的对象将会赋值给原调用处

具体写法要求看下边的使用方法

## 二、匹配规则

可以看到下边例子中都写上了返回值类型和参数类型，下边介绍下


**与 [@AndroidAopMatchClassMethod](/AndroidAOP/zh/AndroidAopMatchClassMethod) 不同的是这个必须是精准匹配，写法如下：**


匹配的写法公式： **返回值类型 方法名(参数类型,参数类型...)**


- 返回值类型、方法名、参数类型必须写全
- 参数类型 用 **()** 包裹起来，多个参数类型用 **,** 隔开，没有参数就只写 **()**
- 返回值类型 和 方法名 之间用空格隔开
- **返回值类型 和 参数类型 都要用 Java 的类型表示**，除了 8 种基本类型之外，其他引用类型都是 **包名.类名**
- 如果函数是 ```suspend``` 修饰的，那 返回值类型 无论是什么类型都写 ```suspend``` ，参数类型 还是按上述几点来写
- 对于存在泛型信息的（例如集合List）必须抹除泛型信息

- 与填写的替换类名不同的是方法参数和返回值类型如果是内部类则需要用`$`不能用`.`代替

!!!note
    [AOP 代码生成助手](/AndroidAOP/zh/AOP_Helper)，可辅助你一键生成代码

下边给出类型表示不同的 Kotlin 对 Java 对照表，如果是 Kotlin 代码请对号入座

（有发现不全的可以跟我反馈）

| Kotlin 类型 |       Java 类型       |
|-----------|:-------------------:|
| Int       |         int         |
| Short     |        short        |               
| Byte      |        byte         |               
| Char      |        char         |               
| Long      |        long         |               
| Float     |        float        |               
| Double    |       double        |               
| Boolean   |       boolean       |  
| Int?      |  java.lang.Integer  |
| Short?    |   java.lang.Short   |               
| Byte?     |   java.lang.Byte    |               
| Char?     | java.lang.Character |               
| Long?     |   java.lang.Long    |               
| Float?    |   java.lang.Float   |               
| Double?   |  java.lang.Double   |               
| Boolean?  |  java.lang.Boolean  |  
| String    |  java.lang.String   |  
| Unit（或不写） |        void         |  
| Unit?     |   java.lang.Void    |  
| Any       |  java.lang.Object   |   

其他不在上表中的数据类型，都属于引用类型，写法就是 **包名.类名**




!!! note
    1、Kotlin 中的 `vararg str : String` 相当于 Java 中的 `String...`，这种匹配时无论哪种代码都按 `String[]` 来表示（此处以 String 为例，其他类型也一样）<br>
    2、对于有泛型的类型不要写泛型，例如 `java.lang.List<String> methodName(java.lang.List<String>)` 应该直接写为 `java.lang.List methodName(java.lang.List)`

## 三、使用案例

### 1、Java写法

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

该例意思就是凡是代码中写```Toast.makeText```和```Toast.show```的地方都被替换成```ReplaceToast.makeText```和```ReplaceToast.show```

### 2、Kotlin写法
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

#### 如果被替换函数是 ```suspend``` 修饰的，那么你只能用Kotlin代码来写，并且替换函数也要被 ```suspend``` 修饰
```kotlin
@AndroidAopReplaceClass("com.flyjingfish.androidaop.MainActivity")
object ReplaceGetData {
    //注解参数唯一变化的返回类型 改为 suspend， 其他不变
    @AndroidAopReplaceMethod("suspend getData(int)")
    @JvmStatic
    //  这里函数定义写法规则依旧不变，只是多加一个 suspend 修饰
    suspend fun getData(mainActivity: MainActivity, num: Int): Int {
        Log.e("ReplaceGetData","getData")
        return mainActivity.getData(num + 1)
    }
}
```

### 3、构造方法

```kotlin
@AndroidAopReplaceClass(value = "com.flyjingfish.test_lib.TestMatch",type = MatchType.EXTENDS)
object ReplaceTestMatch {
    
    @AndroidAopReplaceNew
    @JvmStatic
    fun newTestMatch1(testBean: TestMatch3){
       //替换 new 后边的类名，参数类型就是替换后的类型，此方法返回类型为空，不会回调此方法
    }

    
    @AndroidAopReplaceNew
    @JvmStatic
    fun newTestMatch2(testBean: TestMatch3):TestMatch{
       //替换 new 后边的类名，参数类型就是替换后的类型，此方法返回类型不为空，则将回调此方法，并且返回的对象将会替换 new 出来的对象
       return new TestMatch()
    }

    
    @AndroidAopReplaceMethod("<init>(int)")
    @JvmStatic
    fun getTestBean(testBean: TestMatch) : TestMatch{
        //参数只能有一个就是被替换类，返回类型不可以为空，方法返回的对象将会替换 new 出来的对象
        return TestMatch(2)
    }

    @AndroidAopReplaceMethod("<init>(int)")
    @JvmStatic
    fun getTestBean(num: Int,clazz :Class<*>) : TestMatch{
        //最后一个参数是Class类型其余参数类型及顺序和原构造方法完全一致，在这个方法内再去创建对象，此前并没有对象被创建出来
        return TestMatch(num)
    }

}
```

上边三种使用方式都可以替换 new 的对象不同的是

- 第1种直接替换 new 的类名（直接换掉了类型）

- 第2种不但替换了 new 的类名，并且会回调到方法内，在此返回的对象也将会换掉刚刚 new 出来的对象（两者区别就是返回类型是否为空）

- 第3种与前两者不同的是它 **不会替换 new 的类名**，但是会回调到方法内，在此返回的对象将会换掉刚刚 new 出来的对象。并且定义的参数有且只有一个必须是 @AndroidAopReplaceClass 定义的类型，返回类型不可为空

- 第4种与前三者不同的是它 **既不会替换 new的类名，也没有对象回调进来**，需要你手动创建此对象，它的优点在于在尚未创建对象之前拿到创建对象的所有参数，这样可以提前去修改参数再去创建对象，**所以它的优点就是有一个前置作用**

- @AndroidAopReplaceNew 定义的函数有且只有一个参数，参数类型可以是除了基础类型以外的任何类型

### 4、@AndroidAopMatchClassMethod 的代理用法

- 1、首先你必须还是要使用 `@AndroidAopReplaceClass` 去替换方法的调用，并使用 `@ProxyMethod` 去给替换方法加入注解
    - 方法实现内需要调用被替换方法
    - 方法的定义除了上文提到的要求之外，其他要和原方法保持一致，例如原方法的名称、注解、参数的名称、参数上的注解等等，如果你不使用这些信息就无关紧要的，倘若你要使用就务必这样做

```java
package com.flyjingfish.test_lib.replace;
@AndroidAopReplaceClass(
        "android.widget.Toast"
)
public class ReplaceToast {
    @AndroidAopReplaceMethod(
            "android.widget.Toast makeText(android.content.Context, java.lang.CharSequence, int)"
    )
    @ProxyMethod(proxyClass = Toast.class,type = ProxyType.STATIC_METHOD)
    public static Toast makeText(Context context, CharSequence text, int duration) {
        return Toast.makeText(context, text, duration);
    }
    @AndroidAopReplaceMethod(
            "void setGravity(int , int , int )"
    )
    @ProxyMethod(proxyClass = Toast.class,type = ProxyType.METHOD)
    public static void setGravity(Toast toast,int gravity, int xOffset, int yOffset) {
        toast.setGravity(gravity, xOffset, yOffset);
    }
    @AndroidAopReplaceMethod(
            "void show()"
    )
    @ProxyMethod(proxyClass = Toast.class,type = ProxyType.METHOD)
    public static void show(Toast toast) {
        toast.show();
    }
}
```

- 2、使用 `@AndroidAopMatchClassMethod` 来定义 `ReplaceToast` 代理类
    - type 需要是 MatchType.SELF
    - 使用 MatchClassMethodProxy 或 MatchClassMethodSuspendProxy 类作为切面处理类
    - 实现 invokeProxy 或 invokeSuspendProxy 方法来处理逻辑

```kotlin
@AndroidAopMatchClassMethod(
    targetClassName = "com.flyjingfish.test_lib.replace.ReplaceToast",
    type = MatchType.SELF,
    methodName = ["*"]
)
class ReplaceToastProxy : MatchClassMethodProxy() {
    override fun invokeProxy(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e("ReplaceToastProxy","methodName=$methodName," +
                "parameterNames=${joinPoint.targetMethod.parameterNames.toList()}," +
                "parameterTypes=${joinPoint.targetMethod.parameterTypes.toList()}," +
                "returnType=${joinPoint.targetMethod.returnType}," +
                "args=${joinPoint.args?.toList()},target=${joinPoint.target},targetClass=${joinPoint.targetClass},"
        )

        return joinPoint.proceed()
    }
}
```

这样你就可以对某些系统方法使用 `ProceedJoinPoint` 来控制方法的调用了，**关键在于要使用 `@ProxyMethod` 来标记方法**，这样 `ProceedJoinPoint` 返回的信息就是被替换类的方法信息了
!!! note
    [AOP 代码生成助手](/AndroidAOP/zh/AOP_Helper)，可辅助你一键生成代理用法代码

