在你使用 `@AndroidAopPointCut` 和 `@AndroidAopMatchClassMethod` 两种切面时，如果切入函数是 `suspend` 修饰的，你有两种切面处理类选择

### 1、不完全支持 `suspend` 的 BasePointCut 和 MatchClassMethod


当你选择第1种时，AndroidAOP就会当普通函数去处理，但记住这是不可以修改返回值，只能返回 `joinPoint.proceed()` 的结果，所以这种方式不可以修改返回结果，例如：

```kotlin

class MyAnnoCut3 : BasePointCut<MyAnno3> {
    override fun invoke(joinPoint: ProceedJoinPoint, anno: MyAnno3): Any? {
        Log.e("MyAnnoCut3", "====invoke=====")
        return joinPoint.proceed()
    }
}
```

!!! note
    虽然不可以修改返回结果，但可以不调用 `joinPoint.proceed()` ，也可以修改传入参数例如 `joinPoint.proceed(1,2)`


### 2、支持 `suspend` 的 BasePointCutSuspend 和 MatchClassMethodSuspend

当你选择第2种时，你需要在 invokeSuspend 中去指明线程 例如使用 `withContext` 函数，例如

```kotlin

class MyAnnoCut3 : BasePointCutSuspend<MyAnno3> {
    override suspend fun invokeSuspend(joinPoint: ProceedJoinPointSuspend, anno: MyAnno3) {
        withContext(Dispatchers.Main) {
            ...
            joinPoint.proceed(object :OnSuspendReturnListener{
                override fun onReturn(proceedReturn: ProceedReturn): Any? {
                    val result = proceedReturn.proceed()
                    Log.e("MyAnnoCut3", "====onReturn=====${proceedReturn.returnType},result=$result")
                    return (result as Int)+100
                }

            })
        }

    }
}
```

!!! note
    1、如果你不使用 `withContext` 函数，有可能会出现切点函数返回结果时的 `ClassCastException` 异常。使用这个切面处理类的好处是可以修改返回结果（[具体介绍](/AndroidAOP/zh/Pointcut_return/#suspend)），可以调用其他 suspend 函数 <br>
    2、在withContext函数最后一行尽量是 `joinPoint.proceed` 或 `joinPoint.proceedIgnoreOther`

!!! warning
    如果切点函数不是 `suspend` 函数，即使使用 `BasePointCutSuspend` 和 `MatchClassMethodSuspend` 也依旧会回调 `invoke` 方法，而不会回调 `invokeSuspend` 方法

#### 另外对于普通切面处理类不调用 `proceed()` 直接返回的，例如：

```kotlin

class MyAnnoCut3 : BasePointCut<MyAnno3> {
    override fun invoke(joinPoint: ProceedJoinPoint, anno: MyAnno3): Any? {
        Log.e("MyAnnoCut3", "====invoke=====")
        return null
    }
}
```
suspend 的处理方式是

```kotlin

class MyAnnoCut3 : BasePointCutSuspend<MyAnno3> {
    override suspend fun invokeSuspend(joinPoint: ProceedJoinPointSuspend, anno: MyAnno3) {
        withContext(Dispatchers.Main) {
            ...
            joinPoint.proceedIgnoreOther(object :OnSuspendReturnListener2{
                override fun onReturn(proceedReturn: ProceedReturn2): Any? {
                    Log.e("MyAnnoCut3", "====invokeSuspend=====")
                    return null
                }

            })
        }

    }
}
```
 
