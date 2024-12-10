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
class SingleClickCut : BasePointCut<SingleClick>() {
    //Fill in your original annotations for this pattern
    override fun invoke(joinPoint: ProceedJoinPoint, anno: SingleClick): Any? {
//Copy the logic code here and make some changes
        return null
    }

}
```

Then add the [@AndroidAopPointCut(SingleClickCut.class)](https://flyjingfish.github.io/AndroidAOP/AndroidAopPointCut/) annotation on top of your original annotation. The annotation ```@Retention``` can only set ```RUNTIME```, and ```@Target``` can only set ```METHOD```

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
### AndroidAOP code[@AndroidAopMatchClassMethod](https://flyjingfish.github.io/AndroidAOP/AndroidAopMatchClassMethod/) 
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

[Click here to see detailed usage of @AndroidAopReplaceClass](https://flyjingfish.github.io/AndroidAOP/AndroidAopReplaceClass/)

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
    Unlike AspectJ, AndroidAOP does not retain the way to execute the original method, but you can call the original method yourself without causing infinite recursive calls (indirect calls to the original method will cause infinite recursion [here is a solution](https://flyjingfish.github.io/AndroidAOP/FAQ/#12-will-calling-the-original-method-in-the-method-annotated-with-androidaopreplacemethod-cause-recursion)), [Click here for detailed usage](https://flyjingfish.github.io/AndroidAOP/AndroidAopReplaceClass)

## 4. Other aspect methods

- @Before: Execute code before method execution.
- @After: Execute code after method execution, regardless of whether an exception is thrown.
- @AfterReturning: Execute code after method execution, only when the method returns successfully.
- @AfterThrowing: Execute code when a method throws an exception.

All of the above can be indirectly implemented through several existing annotation aspects [click here to refer to FAQ #5 Want to insert code before and after the method](https://flyjingfish.github.io/AndroidAOP/FAQ/#5-want-to-insert-code-before-and-after-the-method)