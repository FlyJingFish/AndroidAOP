## Brief description

@AndroidAopPointCut is a method-based annotation. This annotation method focuses on the execution of the method (Method Execution). The built-in annotations in this library are all made in this way

**In addition, please try not to place the annotation on the system method, such as: Activity's onCreate() onResume(), etc.**
**Even if it is added, there should be no time-consuming operations during the section processing. JoinPoint.proceed() should be executed normally, otherwise unexpected problems will occur, such as: ANR**

## 1. Create annotations

### Example

Create an annotation named CustomIntercept and add **@AndroidAopPointCut** to your annotation

```kotlin
@AndroidAopPointCut(CustomInterceptCut::class)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(
    AnnotationRetention.RUNTIME
)
annotation class CustomIntercept(vararg val value: String = [])
```

<details>
<summary><strong>Java writing:</strong></summary>

```java

@AndroidAopPointCut(CustomInterceptCut.class)
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomIntercept {
    String[] value() default {};
}
```
</details>

- **@AndroidAopPointCut**'s **CustomInterceptCut.class** is the class that handles the section for you (described below)

- **@Target** only works on methods, and setting other ones has no effect
- For Java, you can set ElementType.METHOD
- For Kotlin, you can set AnnotationTarget.FUNCTION,AnnotationTarget.PROPERTY_GETTER,AnnotationTarget.PROPERTY_SETTER

- **@Retention** can only use RetentionPolicy.RUNTIME

## 2. Create a section processing class

The section processing class needs to implement the BasePointCut interface and handle the section logic in invoke

```kotlin
interface BasePointCut<T : Annotation> {
    fun invoke(joinPoint: ProceedJoinPoint, anno: T): Any?
}
```

!!! note
    If the point cutting function is suspend [Click here to view](/AndroidAOP/Suspend_cut/)

- [Introduction to ProceedJoinPoint](/AndroidAOP/ProceedJoinPoint/)
- [Introduction to invoke return value](https://flyjingfish.github.io/AndroidAOP/Pointcut_return/)
- [Life Cycle](https://flyjingfish.github.io/AndroidAOP/FAQ/#6-what-is-the-life-cycle-of-the-aspect-processing-class-of-the-matching-aspect-and-the-annotation-aspect) 

### For example

CustomInterceptCut inherits from BasePointCut. You can see that there is a generic on BasePointCut. This generic is the @CustomIntercept annotation above. The two are related to each other.
```kotlin
class CustomInterceptCut : BasePointCut<CustomIntercept> {
    override fun invoke(
        joinPoint: ProceedJoinPoint,
        annotation: CustomIntercept//annotation is the annotation you added to the method
    ): Any? {
        // Write your logic here
        return joinPoint.proceed()
    }
}
```
## 3. Use

Add the annotation you wrote directly to any method, such as onCustomIntercept(). When onCustomIntercept() is called, it will first enter the invoke method of CustomInterceptCut mentioned above.

```kotlin
@CustomIntercept("I am custom data")
fun onCustomIntercept() {

}

```