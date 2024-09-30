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
    If the point cutting function is suspend [Click here to view](https://github.com/FlyJingFish/AndroidAOP/wiki/Suspend-%E5%88%87%E7%82%B9%E5%87%BD%E6%95%B0)

- [Introduction to ProceedJoinPoint](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint)
- [Introduction to invoke return value](https://github.com/FlyJingFish/AndroidAOP/wiki/%E5%88%87%E7%82%B9%E6%96%B9%E6%B3%95%E8%BF%94%E5%9B%9E%E5%80%BC)
- [Life Cycle](https://github.com/FlyJingFish/AndroidAOP/wiki/%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98#6%E5%8C%B9%E9%85%8D%E5%88%87%E9%9D%A2%E5%92%8C%E6%B3%A8%E8%A7%A3%E 5%88%87%E9%9D%A2%E7%9A%84%E5%88%87%E9%9D%A2%E5%A4%84%E7%90%86%E7%B1%BB%E7%9A%84%E7%94%9F%E5%91%BD%E5%91%A8%E6%9C%9F%E6%98%AF %E6%80%8E%E6%A0%B7%E7%9A%84) ### For example

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