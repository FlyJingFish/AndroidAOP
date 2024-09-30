When you use `@AndroidAopPointCut` and `@AndroidAopMatchClassMethod`, if the entry function is modified by `suspend`, you have two choices of aspect processing classes

### 1. BasePointCut and MatchClassMethod that do not fully support `suspend`

When you choose the first option, AndroidAOP will treat it as a normal function, but remember that the return value cannot be modified. It can only return the result of `joinPoint.proceed()`, so this method cannot modify the return result, for example:

```kotlin

class MyAnnoCut3 : BasePointCut<MyAnno3> {
    override fun invoke(joinPoint: ProceedJoinPoint, anno: MyAnno3): Any? {
        Log.e("MyAnnoCut3", "====invoke=====")
        return joinPoint.proceed()
    }
}
```

!!! note
    Although the return result cannot be modified, you can call `joinPoint.proceed()` instead, and you can also modify the input parameters, such as `joinPoint.proceed(1,2)`

### 2. BasePointCutSuspend and MatchClassMethodSuspend that support `suspend`

When you choose the second option, you need to specify the thread in invokeSuspend, such as using the `withContext` function, for example

```kotlin

class MyAnnoCut3 : BasePointCutSuspend<MyAnno3> {
    override suspend fun invokeSuspend(joinPoint: ProceedJoinPointSuspend, anno: MyAnno3) {
        withContext(Dispatchers.Main) {
            ...
            joinPoint.proceed(object : OnSuspendReturnListener {
                override fun onReturn(proceedReturn: ProceedReturn): Any? {
                    val result = proceedReturn.proceed()
                    Log.e(
                        "MyAnnoCut3",
                        "====onReturn=====${proceedReturn.returnType},result=$result"
                    )
                    return (result as Int) + 100
                }

            })
        }

    }
}
```

!!! note
    1. If you do not use the `withContext` function, a `ClassCastException` exception may occur when the cut point function returns the result. The advantage of using this aspect processing class is that you can modify the return result ([detailed introduction](https://flyjingfish.github.io/AndroidAOP/Pointcut_return/#suspend-function)), and call other suspend functions <br>
    2. Try to use `joinPoint.proceed` or `joinPoint.proceedIgnoreOther` in the last line of the withContext function

!!! warning
    If the cut point function is not a `suspend` function, even if `BasePointCutSuspend` and `MatchClassMethodSuspend` are used, the `invoke` method will still be called back instead of the `invokeSuspend` method

#### In addition, for ordinary aspect processing classes, `proceed()` is not called and returns directly, for example:

```kotlin

class MyAnnoCut3 : BasePointCut<MyAnno3> {
    override fun invoke(joinPoint: ProceedJoinPoint, anno: MyAnno3): Any? {
        Log.e("MyAnnoCut3", "====invoke=====")
        return null
    }
}
```
The processing method of suspend is

```kotlin

class MyAnnoCut3 : BasePointCutSuspend<MyAnno3> {
    override suspend fun invokeSuspend(joinPoint: ProceedJoinPointSuspend, anno: MyAnno3) {
        withContext(Dispatchers.Main) {
            ...
            joinPoint.proceedIgnoreOther(object : OnSuspendReturnListener2 {
                override fun onReturn(proceedReturn: ProceedReturn2): Any? {
                    Log.e("MyAnnoCut3", "====invokeSuspend=====") return null
                }
            })
        }
    }
} 
```