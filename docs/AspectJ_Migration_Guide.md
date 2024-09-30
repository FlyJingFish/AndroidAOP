Migrating from AspectJ is also very simple

## 1. Annotation-based aspects

### AspectJ code

Take click annotation as an example, you may have such a code

Click annotation

```java

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SingleClick {

    long DEFAULT_INTERVAL_MILLIS = 1000;

    /**
     * @return The interval between quick clicks (ms), the default is 1000ms
     */
    long value() default DEFAULT_INTERVAL_MILLIS;
}
```

Click annotation aspect

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
// Section processing logic
        return result;
    }
}

```

### AndroidAOP code

First create a class to handle sections

```kotlin
class SingleClickCut : ClickCut<SingleClick>() {
    //Fill in your original annotations for this pattern
    override fun invoke(joinPoint: ProceedJoinPoint, anno: SingleClick): Any? {
//Copy the logic code here and make some changes
        return null
    }

}
```

Then add the [@AndroidAopPointCut(SingleClickCut.class)](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopPointCut) annotation on top of your original annotation. The annotation ```@Retention``` can only set ```RUNTIME```, and ```@Target``` can only set ```METHOD```

```java
//Just add such an annotation. The parameter is the section processing class SingleClickCut.class created above
@AndroidAopPointCut(SingleClickCut.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SingleClick {

    long DEFAULT_INTERVAL_MILLIS = 1000;

    /**
     * @return The interval between quick clicks (ms), the default is 1000ms
     */
    long value() default DEFAULT_INTERVAL_MILLIS;
}
```
<details>
<summary><strong>Kotlin writing</strong></summary>

```kotlin
@AndroidAopPointCut(SingleClickCut::class)
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER
)
annotation class SingleClick(
    /**
     * The interval between quick clicks (ms), the default is 1000ms
     */
    val value: Long = DEFAULT_INTERVAL_MILLIS
) {
    companion object {
        const val DEFAULT_INTERVAL_MILLIS: Long = 1000
    }
}
```
</details>

## 2. Aspects that match the execution process of a class method

@AndroidAopMatchClassMethod is similar to the execution matching type in AspectJ, focusing on the execution of methods

AndroidAOP currently only matches methods of a class, not just a method regardless of the class. Because the author thinks that doing so is almost meaningless, and doing so often leads to the addition of many classes that do not want to be added to the aspect, which is not conducive to everyone's management and control of their own code (a bit out of control~)

### AspectJ code

For example, you originally set the aspect code for ```threadTest``` of ```MainActivity```, as shown below:

```kotlin

package com.flyjingfish.test

class MainActivity : BaseActivity() {
    fun threadTest() {
        Log.e("threadTest", "------")
    }
}

```

The matching aspect code of AspectJ is as follows:

```java

@Aspect
public class CheckAspectJ {
    private static final String TAG = "CheckAspectJ";

    @Pointcut("execution(* com.flyjingfish.test.MainActivity.threadTest())")
    public void pointcutThreadTest() {
    }

    @Around("pointcutThreadTest()")
    public void calculateFunctionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        Log.i(TAG, "pointcut1 ---------calculateFunctionTime---------@Around");
        long beginTime = System.currentTimeMillis();
        joinPoint.proceed();
        long endTime = System.currentTimeMillis();
        Log.i(TAG, "pointcut1 ----------calculateFunctionTime-- -----Running time: " + (endTime - beginTime));
    }
} 
``` 
### AndroidAOP code[@AndroidAopMatchClassMethod](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopMatchClassMethod) 
```kotlin 
@AndroidAopMatchClassMethod(
    targetClassName = "com.flyjingfish.test.MainActivity",
    methodName = ["threadTest"],
    type = MatchType.SELF
)
class MatchActivityMethod : MatchClassMethod {
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e("MatchActivityMethod", "=====invoke=====$methodName")
        long beginTime = System . currentTimeMillis ();
        joinPoint.proceed();
        long endTime = System . currentTimeMillis ();
        return null
    }
}
```

## 3. Match the called aspect of a class method

@AndroidAopReplaceClass is similar to the call matching type in AspectJ, focusing on the method call

### AspectJ code

Those who have used AspectJ should know that some system methods can only be matched through call. For example, you originally targeted `e` of `android.util.Log` sets the aspect code, and the matching aspect code of AspectJ is as follows:

```java

@Aspect
public final class TestAspectJ {
    @Pointcut("call(* android.util.Log.e(..))")
    public void pointcutThreadTest() {
    }

    @Around("pointcutThreadTest()")
    public final Object cutExecute(final JoinPoint joinPoint) throws Throwable {
        Log.e("TestAspectJ", "====cutExecute");
        return null;
    }
}

```

### AndroidAOP code

[Click here to see detailed usage of @AndroidAopReplaceClass](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopReplaceClass)

```kotlin
@AndroidAopReplaceClass("android.util.Log")
object ReplaceLog {
    @AndroidAopReplaceMethod("int e(java.lang.String tag, java.lang.String msg)")
    @JvmStatic
    fun logE(String tag, String msg): Int {
        return Log.e(tag, msg)
    }
} 
```

!!! note
    Unlike AspectJ, AndroidAOP does not retain the way to execute the original method, but you can call the original method yourself without causing infinite recursive calls (indirect calls to the original method will cause infinite recursion [here is a solution](https://github.com/FlyJingFish/AndroidAOP/wiki/%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98#12%E5%9C%A8-androidaopreplacemethod-%E6%B3%A8%E8%A7%A3%E7%9A%84%E6%96%B9%E6%B3%95% E4%B8%AD%E8%B0%83%E7%94%A8%E5%8E%9F%E6%96%B9%E6%B3%95%E4%BC%9A%E4%B8%8D%E4%BC%9A%E9%80%A0%E6%88%90%E9%80%92%E5%BD%92%E7%9A%84%E6%83%85%E5%86%B5)), [Click here for detailed usage](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopReplaceClass)

## 4. Other aspect methods

- @Before: Execute code before method execution.
- @After: Execute code after method execution, regardless of whether an exception is thrown.
- @AfterReturning: Execute code after method execution, only when the method returns successfully.
- @AfterThrowing: Execute code when a method throws an exception.

All of the above can be indirectly implemented through several existing annotation aspects [click here to refer to FAQ #5 Want to insert code before and after the method](https://github.com/FlyJingFish/AndroidAOP/wiki/%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98#5%E6%83%B3%E8%A6%81%E5%9C%A8%E6%96%B9%E6%B3%95%E5%89%8D%E5%90%8E%E6%8F%92%E5%85%A5%E4%BB%A3%E7%A0%81)