
## 普通函数


对于 @AndroidAopPointCut 和 @AndroidAopMatchClassMethod 这两种切面都有其切面回调处理类分别是

- @AndroidAopPointCut 对应 BasePointCut
```kotlin
interface BasePointCut<T : Annotation> {
    fun invoke(joinPoint: ProceedJoinPoint, anno: T): Any?
}
```
- @AndroidAopMatchClassMethod 对应 MatchClassMethod
```kotlin
interface MatchClassMethod {
    fun invoke(joinPoint: ProceedJoinPoint, methodName:String): Any?
}
```

> [!TIP]\
> 可以看到 两个 invoke 方法都有一个返回值，这个返回值将会替换掉切入点方法的返回值，并且会自动转化为原方法的返回类型，但是以下两种类型，则没有返回值



### 返回值是什么

- 如果切面方法 **有返回值** ，**invoke** 的返回值就是切面方法返回值
- 另外如果切面方法 **有返回值** ，**invoke** 的返回值类型要和切面方法返回类型保持一致
- 如果切面方法 **没有返回值** ，这块返回什么无所谓的

```java
@MyAnno
public int numberAdd(int value1,int value2){
    int result = value1+value2;
    return result;
}
```
```java
public class MyAnnoCut implements BasePointCut<MyAnno> {
    @Nullable
    @Override
    public Object invoke(@NonNull ProceedJoinPoint joinPoint, @NonNull MyAnno anno) {
        int value1 = (int) joinPoint.args[0];
        int value2 = (int) joinPoint.args[1];
        int result = value1 * value2;
        return result;
    }
}
```
例如上边 numberAdd 方法 return 是两个参数相加，进入切面后，我就想改成两个数相乘，所以返回值是什么呢？相信你已经知道了

## suspend 函数

- BasePointCutSuspend 继承自 BasePointCut
```kotlin
interface BasePointCutSuspend<T : Annotation>:BasePointCut<T> {
    suspend fun invokeSuspend(joinPoint: ProceedJoinPointSuspend, anno: T)
}
```
- MatchClassMethodSuspend 继承自 MatchClassMethod
```kotlin
interface MatchClassMethodSuspend : MatchClassMethod {
    suspend fun invokeSuspend(joinPoint: ProceedJoinPointSuspend, methodName: String)
    ...
}
```

> [!TIP]\
> 对于切点函数是 **suspend** 函数的，采用上述两种类型更好一些，如果继续使用 `BasePointCut`、`MatchClassMethod` 其返回值必须是 `joinPoint.proceed()` 的返回值，如需修改返回值请看如下代码：

```kotlin
class MyAnnoCut5 : BasePointCutSuspend<MyAnno5> {
    override suspend fun invokeSuspend(joinPoint: ProceedJoinPointSuspend, anno: MyAnno5){
        withContext(Dispatchers.IO) {
            //通过设置 OnSuspendReturnListener 来修改返回值 onReturn 的返回值就是 suspend 切点函数的返回值
            joinPoint.proceed(object : OnSuspendReturnListener {
                override fun onReturn(proceedReturn: ProceedReturn): Any? {
                    return (proceedReturn.proceed() as Int)+100
                }
            })
        }

    }
}
```

> [!TIP]\
> 此处 onReturn 的解释同[此处的返回值是什么](/AndroidAOP/zh/Pointcut_return/#_2)


