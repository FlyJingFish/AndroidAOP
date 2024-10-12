## 简述


此切面是做匹配某类及其对应方法的切面的，这个切面方式关注的是方法的执行（Method Execution） 注意区分和[@AndroidAopReplaceClass](/AndroidAOP/zh/AndroidAopReplaceClass)的区别。




```kotlin
@AndroidAopMatchClassMethod(
    targetClassName = 目标类名（包含包名）,
    methodName = 方法名数组,
    type = 匹配类型，非必须，默认 EXTENDS
    excludeClasses = 排除继承关系中的一些类的数组（type 不是 SELF 才有效），非必须
)
```

- targetClassName 是内部类时不使用`$`字符，而是用`.`


- type 有四种类型（不设置默认 ```EXTENDS```）：
    - ```SELF``` 表示匹配的是 targetClassName 所设置类的**自身**
    - ```EXTENDS``` 表示匹配的是**所有继承于** targetClassName 所设置的类
    - ```DIRECT_EXTENDS``` 表示匹配的是 **<em><strong>直接继承于</strong></em>** targetClassName 所设置的类
    - ```LEAF_EXTENDS``` 表示匹配的是 **<em><strong>末端继承（就是没有子类了）</strong></em>** targetClassName 所设置的类

    简单来说，```LEAF_EXTENDS```和```DIRECT_EXTENDS```是两个极端，前者关注的是继承关系中最后一个节点，后者关注的是继承关系中第一个节点。另外注意 ```EXTENDS``` 这种匹配类型范围比较大，所有继承的中间类也可能会加入切面代码


- excludeClasses
    - 如果 targetClassName 是类名，就是排除掉继承关系中的一些类，可以设置多个，且 type 不是 SELF 才有效
    - 如果 targetClassName 是包名，就是排除掉匹配到的一些类，可以设置多个，且 type 是 SELF 才有效

- overrideMethod 默认false
    - 设置为true，如果子类（非接口，可以是抽象类）中没有匹配的方法则重写父类的方法
        - targetClassName 不可以包含 *
        - methodName 不可以定义 [ "*" ]
        - 重写的方法不能是private 、final修饰的才可以

    - 设置为false，如果子类没有匹配的方法则不作处理

!!! note 
    另外不是所有类都可以Hook进去<br> <li>```type``` 类型为 ```SELF``` 时，```targetClassName``` 所设置的类必须是安装包里的代码。
    例如：如果这个类（如：Toast）在 **android.jar** 里边是不行的。如有这种需求应该使用[@AndroidAopReplaceClass](/AndroidAOP/zh/AndroidAopReplaceClass)</li><br> <li>```type``` 类型不是 ```SELF``` 时，这个切面要想有作用需要有匹配的那个方法，如果子类没有重写匹配的方法，子类就不会被匹配到，使用 overrideMethod 可忽略此限制 </li> <br> <li><strong>当你修改这个切面的配置后多数情况下你应该clean项目再继续开发</strong></li>


## 创建切面处理类

切面处理类需要实现 MatchClassMethod 接口，在 invoke 中处理切面逻辑

```kotlin
interface MatchClassMethod {
    fun invoke(joinPoint: ProceedJoinPoint, methodName:String): Any?
}
```

!!! note
    如果切点函数是 suspend [点此前往查看](/AndroidAOP/zh/Suspend_cut)

- [ProceedJoinPoint介绍](/AndroidAOP/zh/ProceedJoinPoint)
- [invoke返回值介绍](/AndroidAOP/zh/Pointcut_return)
- [生命周期](/AndroidAOP/zh/FAQ/#6)

## 匹配规则

可以看到下边例子中有的设置的方法名就只写了方法名，有的也写上了返回值类型和参数类型，下边介绍下

### 模糊匹配

- 1、targetClassName 最后以 `.*` 结尾且有其他字符，并且 `type = MatchType.SELF` 时，则匹配该包下所有类，包括子包 [如下边的例九](#_15)
- 2、methodName 有且只有一个方法名为 `*` 的则匹配类中的所有方法 [如下边的例八](#_14)
- 3、methodName 只写方法名但不写返回类型和参数类型也是模糊匹配，这将会使目标类中所有同名方法都加入切点

!!! note
    对于匹配包名的我是强烈不建议这么做的，侵入代码过多，而且打包速度明显下降。建议只做调试、日志之用，原方法应全部放行


### 精准匹配

methodName 方法名写上返回类型、参数类型，这样就可以精准到一个方法加入切点（精准匹配异常时会自动退化到模糊匹配）

匹配的写法公式： **返回值类型 方法名(参数类型,参数类型...)**


- 返回值类型 可以不用写
- 方法名 必须写
- 参数类型 可以不用写，写的话用 **()** 包裹起来，多个参数类型用 **,** 隔开，没有参数就只写 **()**
- 返回值类型 和 方法名 之间用空格隔开
- 返回值类型 和 参数类型 不写的话就是不验证
- **返回值类型 和 参数类型 都要用 Java 的类型表示**，除了 8 种基本类型之外，其他引用类型都是 **包名.类名**
- 如果函数是 ```suspend``` 修饰的，那 返回值类型 无论是什么类型都写 ```suspend``` ，参数类型 还是按上述几点来写
- 对于存在泛型信息的（例如集合List）必须抹除泛型信息

- 与targetClassName不同的是方法参数和返回值类型如果是内部类则需要用`$`而不能用`.`代替

!!! note
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

## 各种场景示例

#### 例一

想要监测所有继承自 AppCompatActivity 类的所有 startActivity 跳转

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

**⚠️注意：对于匹配子类方法的，如果子类没有重写匹配的方法，是无效的，使用 overrideMethod 可忽略此限制**

#### 例二

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

这块提示下，对于使用了 lambda 点击监听的；

ProceedJoinPoint 的 target 不是 android.view.View.OnClickListener

- 对于Java target 是 设置lambda表达式的那个类的对象
- 对于Kotlin target 是 null

invoke 回调的 methodName 也不是 onClick 而是编译时自动生成的方法名，类似于这样 onCreate$lambda$14 里边包含了 lambda 关键字

对于 onClick(view:View) 的 view

- 如果是 Kotlin 的代码 ProceedJoinPoint.args[1]
- 如果是 Java 的代码 ProceedJoinPoint.args[0]

这块不在继续赘述了，自己用一下就知道了；

**总结下：其实对于所有的 lambda 的 ProceedJoinPoint.args**

- 如果是 Kotlin 第一个参数是设置lambda表达式的那个类的对象，后边的参数就是 hook 方法的所有参数
- 如果是 Java 从第一个参数开始就是 hook 方法的所有参数

#### 例三

目标类有多个重名方法，只想匹配某一个方法（上文有提到精准匹配规则）

```java
package com.flyjingfish.test_lib;

public class TestMatch {
    public void test(int value1){

    }

    public String test(int value1,String value2){
        return value1+value2;
    }
}

```
例如有 TestMatch 这个类 有两个名为 test 的方法，你只想匹配 test(int value1,String value2) 这个方法，那么写法如下：

```kotlin
package com.flyjingfish.test_lib.mycut;

@AndroidAopMatchClassMethod(
        targetClassName = "com.flyjingfish.test_lib.TestMatch",
        methodName = ["java.lang.String test(int,java.lang.String)"],
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
#### 例四

继承关系层次较多时不想每层都加入切面

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

```java
public abstract class MyOnClickListener implements View.OnClickListener {
    @Override
    public void onClick(View v) {
        ...
        //这块有必要的逻辑代码
    }
}

```

```kotlin
binding.btnSingleClick.setOnClickListener(object :MyOnClickListener(){
    override fun onClick(v: View?) {
        super.onClick(v)//尤其是这句调用父类 onClick 想要保留执行父类方法的逻辑
        onSingleClick()
    }
})
```

这么写，会导致上边的 MyOnClickListener onClick 也加入切面，如此一来相当于一个点击有回调了两次切面处理类的 invoke ，这可能不是我们想要的，所以可以这么改
```kotlin
@AndroidAopMatchClassMethod(
    targetClassName = "android.view.View.OnClickListener",
    methodName = ["onClick"],
    type = MatchType.EXTENDS,
    excludeClasses = ["com.flyjingfish.androidaop.test.MyOnClickListener"]//加上这个可以排除掉一些类
)
class MatchOnClick : MatchClassMethod {
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e("MatchOnClick", "=====invoke=====$methodName")
        return joinPoint.proceed()
    }
}
```
或者设置 type 为 LEAF_EXTENDS 直接过滤掉中间类（这让我想起一句广告：没有中间商赚差价）

#### 例五
切入点是伴生对象怎么办？

假如有这样一个代码
```kotlin
package com.flyjingfish.androidaop

class ThirdActivity : BaseActivity() {
    companion object{
        fun start(){
            ...
        }
    }
}
```
伴生对象修饰符 companion 首字母大写，如下即可
```kotlin
@AndroidAopMatchClassMethod(
    targetClassName = "com.flyjingfish.androidaop.ThirdActivity.Companion",
    methodName = ["start"],
    type = MatchType.SELF
)
class MatchCompanionStart : MatchClassMethod {
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e("MatchCompanionStart", "======$methodName")
        return joinPoint.proceed()
    }
}
```

#### 例六

切入点是 Kotlin 代码的成员变量，想要监听赋值、取值的操作

```kotlin
package com.flyjingfish.androidaop.test
class TestBean {
    var name:String = "test"
}
```

在代码中我们会有这样的操作

```kotlin
testBean.name = "1111" //赋值操作
val name = testBean.name //取值操作
```

您可以这么写

```kotlin
@AndroidAopMatchClassMethod(
    targetClassName = "com.flyjingfish.androidaop.test.TestBean",
    methodName = ["setName","getName"],
    type = MatchType.SELF
)
class MatchTestBean : MatchClassMethod {
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e("MatchTestBean", "======$methodName");
        ToastUtils.makeText(ToastUtils.app,"MatchTestBean======$methodName")
        return joinPoint.proceed()
    }
}
```



#### 例七

如果切点方法是 ```suspend``` 修饰的函数怎么办？

- 你可以直接使用 [模糊匹配](#_4)

- 如果想要使用 [精准匹配](#_5),则写法如下，具体规则 [看精准匹配部分](#_5)


```kotlin
package com.flyjingfish.androidaop

class MainActivity: BaseActivity2() {
    suspend fun getData(num:Int) :Int{
        return withContext(Dispatchers.IO) {
            getDelayResult()
        }
    }
}
```

精准匹配写法如下，匹配的函数返回值类型无论是哪种，都写 ```suspend``` ,具体说明看上文的[精准匹配部分](#_5)

```kotlin
@AndroidAopMatchClassMethod(
    targetClassName = "com.flyjingfish.androidaop.MainActivity",
    methodName = ["suspend getData(int)"],
    type = MatchType.SELF
)
class MatchSuspend : MatchClassMethod {
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e("MatchSuspend", "======$methodName")
        return joinPoint.proceed()
    }
}
```
#### 例八

想要匹配一个类的所有方法

```kotlin
@AndroidAopMatchClassMethod(
    targetClassName = "com.flyjingfish.androidaop.SecondActivity",
    methodName = ["*"],
    type = MatchType.SELF
)
class MatchAllMethod : MatchClassMethod {
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e("MatchMainAllMethod", "AllMethod======$methodName");
        return joinPoint.proceed()
    }
}
```
方法名部分只写一个，并且填 * 即可匹配所有方法


#### 例九

想要匹配一个包下的所有类的所有方法

```kotlin
@AndroidAopMatchClassMethod(
    targetClassName = "com.flyjingfish.androidaop.*",
    methodName = ["*"],
    type = MatchType.SELF
)
class MatchAll : MatchClassMethod {
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e("MatchAll", "---->${joinPoint.targetClass}--${joinPoint.targetMethod.name}--${joinPoint.targetMethod.parameterTypes.toList()}");
        return joinPoint.proceed()
    }
}
```
1、`*` 代替了 `类名` 或者代替`一部分的包名+类名`，该例代表 `com.flyjingfish.androidaop` 包以及子包下的所有类

2、当然 methodName 部分依旧可以填写多个模糊匹配甚至精准匹配的方法名


 

