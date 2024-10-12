
从 AspectJ 迁移过来也是十分简单的

## 1、注解形式的切面

### AspectJ 代码

以单击注解为例介绍下,你可能有这样一段代码

单击注解

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SingleClick {

    long DEFAULT_INTERVAL_MILLIS = 1000;

    /**
     * @return 快速点击的间隔（ms），默认是1000ms
     */
    long value() default DEFAULT_INTERVAL_MILLIS;
}
```

单击注解切面

```java

@Aspect
public final class SingleClick$$AspectJ {
  @Pointcut("within(@com.flyjingfish.light_aop_core.annotations.SingleClick *)")
  public final void withinAnnotatedClass() {
  }

  @Pointcut("execution(!synthetic * *(..)) && withinAnnotatedClass()")
  public final void methodInsideAnnotatedType() {
  }

  @Pointcut("execution(@com.flyjingfish.light_aop_core.annotations.SingleClick * *(..)) || methodInsideAnnotatedType()")
  public final void method() {
  }

  @Around("method() && @annotation(vSingleClick)")
  public final Object cutExecute(final ProceedingJoinPoint joinPoint,
      final SingleClick vSingleClick) {
    // 切面处理逻辑
    return result;
  }
}

```

### AndroidAOP 代码

首先创建一个处理切面的类

```kotlin
class SingleClickCut : ClickCut<SingleClick>() {//这块范型填写你原有的注解即可
    override fun invoke(joinPoint: ProceedJoinPoint, anno: SingleClick): Any? {
        //在此把逻辑代码复制过来，稍加改动即可
        return null
    }

}
```

然后在你原有注解之上添加 [@AndroidAopPointCut(SingleClickCut.class)](/AndroidAOP/zh/AndroidAopPointCut) 注解，注解 ```@Retention``` 只能设置 ```RUNTIME```，```@Target``` 只能设置 ```METHOD```

```java
//只需添加这样一个注解即可，参数就是上边创建的切面处理类 SingleClickCut.class
@AndroidAopPointCut(SingleClickCut.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SingleClick {

    long DEFAULT_INTERVAL_MILLIS = 1000;

    /**
     * @return 快速点击的间隔（ms），默认是1000ms
     */
    long value() default DEFAULT_INTERVAL_MILLIS;
}
```
<details>
<summary><strong>Kotlin写法</strong></summary>

```kotlin
@AndroidAopPointCut(SingleClickCut::class)
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER
)
annotation class SingleClick(
    /**
     * 快速点击的间隔（ms），默认是1000ms
     */
    val value: Long = DEFAULT_INTERVAL_MILLIS
) {
    companion object {
        const val DEFAULT_INTERVAL_MILLIS: Long = 1000
    }
}
```
</details>

## 2、匹配某个类方法的执行过程的切面

@AndroidAopMatchClassMethod 针对的就是类似 AspectJ 中的 execution 的匹配类型，关注的是方法的执行

AndroidAOP目前只做针对某个类的方法做匹配，不做只对某个方法而不管是哪个类的匹配。因为作者觉得这样做几乎没什么意义，这么做很多时候会导致很多不想加入切面的类也会加进来，不利于大家管理控制自己代码（有点失控～）

### AspectJ 代码

例如你原来针对 ```MainActivity``` 的 ```threadTest``` 设定了切面代码，如下所示：

```kotlin

package com.flyjingfish.test

class MainActivity: BaseActivity() {
    fun threadTest(){
        Log.e("threadTest","------")
    }
}

```

AspectJ 的匹配切面代码如下：

```java

@Aspect
public class CheckAspectJ {
    private static final String TAG = "CheckAspectJ";

    @Pointcut("execution(* com.flyjingfish.test.MainActivity.threadTest())")
    public void pointcutThreadTest() {
    }

    @Around("pointcutThreadTest()")
    public void calculateFunctionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        Log.i(TAG,"pointcut1 ---------calculateFunctionTime---------@Around");
        long beginTime = System.currentTimeMillis();
        joinPoint.proceed();
        long endTime = System.currentTimeMillis();
        Log.i(TAG,"pointcut1 -----------calculateFunctionTime-------运行时间:" + (endTime - beginTime));
    }
}

```

### AndroidAOP 代码

[点此看@AndroidAopMatchClassMethod详细使用方法](/AndroidAOP/zh/AndroidAopMatchClassMethod)

```kotlin
@AndroidAopMatchClassMethod(
    targetClassName = "com.flyjingfish.test.MainActivity",
    methodName = ["threadTest"],
    type = MatchType.SELF
)
class MatchActivityMethod : MatchClassMethod {
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e("MatchActivityMethod", "=====invoke=====$methodName")
        long beginTime = System.currentTimeMillis();
        joinPoint.proceed();
        long endTime = System.currentTimeMillis();
        return null
    }
}
```

## 3、匹配某个类方法的被调用的切面

@AndroidAopReplaceClass 针对的就是类似 AspectJ 中的 call 的匹配类型，关注的是方法的调用

### AspectJ 代码

用过 AspectJ 的应该知道某些系统方法只能通过 call 来匹配，例如你原来针对 `android.util.Log` 的 `e` 设定了切面代码，AspectJ 的匹配切面代码如下：

```java
@Aspect
public final class TestAspectJ {
  @Pointcut("call(* android.util.Log.e(..))")
  public void pointcutThreadTest() {
  }

  @Around("pointcutThreadTest()")
  public final Object cutExecute(final JoinPoint joinPoint) throws Throwable {
    Log.e("TestAspectJ","====cutExecute");
    return null;
  }
}

```

### AndroidAOP 代码

[点此看@AndroidAopReplaceClass详细使用方法](/AndroidAOP/zh/AndroidAopReplaceClass)

```kotlin
@AndroidAopReplaceClass("android.util.Log")
object ReplaceLog {
    @AndroidAopReplaceMethod("int e(java.lang.String tag, java.lang.String msg)")
    @JvmStatic
    fun logE(String tag, String msg): Int{
        return Log.e(tag,msg) 
    }
}
```
!!! note
    不同于 AspectJ 的是，AndroidAOP 不会保留执行原来方法的方式，但你可以自己调原来的方法，并且不会造成无限递归调用的情况（间接调用原来方法会造成无限递归[这里有解决方法](/AndroidAOP/zh/FAQ/#12-androidaopreplacemethod)），[详细使用方法点此前往](/AndroidAOP/zh/AndroidAopReplaceClass)

## 4、其他切面方式

- @Before：在方法执行前执行代码。
- @After：在方法执行后执行代码，无论是否抛出异常。
- @AfterReturning：在方法执行后执行代码，仅在方法成功返回时执行。
- @AfterThrowing：在方法抛出异常时执行代码。

上述几种均可通过现有的几种注解切面间接实现[点此参考 常见问题#5想要在方法前后插入代码](/AndroidAOP/zh/FAQ/#5)



