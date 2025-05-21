## 简述

@AndroidAopPointCut 是在方法上通过注解的形式做切面的，这个切面方式关注的是方法的执行（Method Execution），本库内置的注解都是通过这个做的



**另外请注意尽量不要把切面注解放到系统方法上，例如：Activity 的 onCreate() onResume() 等**
**即便是加了在切面处理时不要有耗时操作，joinPoint.proceed() 要正常执行，否则会出现意想不到的问题，例如：ANR**

## 一、创建注解

### 举例

创建一个名为 CustomIntercept 的注解 将 **@AndroidAopPointCut** 加到你的注解上

=== "Kotlin"
    ```kotlin
    @AndroidAopPointCut(CustomInterceptCut::class)
    @Target(AnnotationTarget.FUNCTION,AnnotationTarget.PROPERTY_GETTER,AnnotationTarget.PROPERTY_SETTER)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class CustomIntercept(
        vararg val value: String = []
    )
    ```
=== "Java"

    ```java
    @AndroidAopPointCut(CustomInterceptCut.class)
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CustomIntercept {
        String[] value() default {};
    }
    ```


- **@AndroidAopPointCut** 的 **CustomInterceptCut.class** 为您处理切面的类（下文有介绍）

- **@Target** 只作用在方法上，设置其他无作用
    - 对于 Java 可以设置 `ElementType.METHOD` 这一个
    - 对于 Kotlin 可以设置 `AnnotationTarget.FUNCTION`,`AnnotationTarget.PROPERTY_GETTER`,`AnnotationTarget.PROPERTY_SETTER` 这三个

- **@Retention** 只可以用 RetentionPolicy.RUNTIME

## 二、创建切面处理类

切面处理类需要实现 BasePointCut 接口，在 invoke 中处理切面逻辑

```kotlin
interface BasePointCut<T : Annotation> {
    fun invoke(joinPoint: ProceedJoinPoint, anno: T): Any?
}
```



!!! note
    如果切点函数是 suspend [点此前往查看](/AndroidAOP/zh/Suspend_cut)


- [ProceedJoinPoint介绍](/AndroidAOP/zh/ProceedJoinPoint)
- [invoke返回值介绍](/AndroidAOP/zh/Pointcut_return)
- [生命周期](/AndroidAOP/zh/FAQ/#6)

### 举例

CustomInterceptCut 继承自 BasePointCut，可以看到 BasePointCut 上有一泛型，这个泛型就是上边的 @CustomIntercept 注解，两者是互相关联的
```kotlin
class CustomInterceptCut : BasePointCut<CustomIntercept> {
    override fun invoke(
        joinPoint: ProceedJoinPoint,
        annotation: CustomIntercept//annotation就是你加到方法上的注解
    ): Any? {
        // 在此写你的逻辑
        return joinPoint.proceed()
    }
}
```
## 三、使用

直接将你写的注解加到任意一个方法上，例如加到了 onCustomIntercept() 当 onCustomIntercept() 被调用时首先会进入到上文提到的 CustomInterceptCut 的 invoke 方法上

```kotlin
@CustomIntercept("我是自定义数据")
fun onCustomIntercept(){
    
}

```


