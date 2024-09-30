## Ordinary function

For @AndroidAopPointCut and @AndroidAopMatchClassMethod, both aspects have their aspect callback processing classes respectively

- @AndroidAopPointCut corresponds to BasePointCut
```kotlin
interface BasePointCut<T : Annotation> {
    fun invoke(joinPoint: ProceedJoinPoint, anno: T): Any?
}
```
- @AndroidAopMatchClassMethod corresponds to MatchClassMethod
```kotlin
interface MatchClassMethod {
    fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any?
}
```

!!! note
    You can see that both invoke methods have a return value, which will replace the return value of the entry point method and will be automatically converted to the return type of the original method, but the following two types have no return value

### What is the return value

- If the section method **has a return value**, the return value of **invoke** is the section method return value
- In addition, if the section method **has a return value**, the return value type of **invoke** must be consistent with the section method return type
- If the section method **does not have a return value**, it doesn’t matter what is returned

```java
@MyAnno
public int numberAdd(int value1,int value2){
        int result=value1+value2;
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
For example, the numberAdd method above returns the addition of two parameters. After entering the section, I want to change it to multiplication of two numbers, so what is the return value? I believe you already know it

## suspend function

- BasePointCutSuspend inherits from BasePointCut
```kotlin
interface BasePointCutSuspend<T : Annotation> : BasePointCut<T> {
    suspend fun invokeSuspend(joinPoint: ProceedJoinPointSuspend, anno: T)
}
```
- MatchClassMethodSuspend inherits from MatchClassMethod
```kotlin
interface MatchClassMethodSuspend : MatchClassMethod {
    suspend fun invokeSuspend(joinPoint: ProceedJoinPointSuspend, methodName: String)
    ...
}
```

!!! note
    For the **suspend** function of the cut point function, it is better to use the above two types. If you continue to use `BasePointCut` and `MatchClassMethod`, its return value must be `joinPoint.proceed()` The return value of the onReturn function. If you need to modify the return value, please see the following code:

```kotlin
class MyAnnoCut5 : BasePointCutSuspend<MyAnno5> {
    override suspend fun invokeSuspend(joinPoint: ProceedJoinPointSuspend, anno: MyAnno5) {
        withContext(Dispatchers.IO) {
//Modify the return value by setting OnSuspendReturnListener The return value of onReturn is the return value of the suspend point function
            joinPoint.proceed(object : OnSuspendReturnListener {
                override fun onReturn(proceedReturn: ProceedReturn): Any? {
                    return (proceedReturn.proceed() as Int) + 100
                }
            })
        }

    }
}
```

!!! note
    Here onReturn The explanation is the same as [What is the return value here](https://github.com/FlyJingFish/AndroidAOP/wiki/%E5%88%87%E7%82%B9%E6%96%B9%E6%B3%95%E8%BF%94%E5%9B%9E%E5%80%BC#%E8%BF%94%E5%9B%9E%E5%80%BC%E6%98%AF%E4%BB%80%E4%B9%88)