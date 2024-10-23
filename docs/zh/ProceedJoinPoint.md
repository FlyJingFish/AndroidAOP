## 简述

切入点方法相关信息，包括切点方法参数、切点对象、继续执行原有方法逻辑等

## proceed

#### 1、执行原来方法的逻辑

在这介绍下 在使用 `ProceedJoinPoint` 这个对象的 `proceed()` 或 `proceed(args)` 表示执行原来方法的逻辑，区别是：

- `proceed()` 不传参，表示不改变当初的传入参数
- `proceed(args)` 有参数，表示改写当时传入的参数，注意传入的参数个数，以及每个参数的类型要和切面方法保持一致
- 不调用 `proceed` 就不会执行拦截切面方法内的代码

#### 2、执行下一个切面

同一个方法存在多个注解或匹配切面时，`proceed` 表示进入下一个切面，那具体怎么处理的呢？

- 多个切面叠加到一个方法上时注解优先于匹配切面，注解切面之间从上到下依次执行
- 调用 ```proceed``` 才会执行下一个切面，多个切面中最后一个切面执行 ```proceed``` 才会调用切入方法内的代码
- 在前边切面中调用 ```proceed(args)``` 可更新方法传入参数，并在下一个切面中也会拿到上一层更新的参数
- 存在异步调用 ```proceed``` 时，第一个异步调用 ```proceed``` 切面的返回值（就是 invoke 的返回值）就是切入方法的返回值；否则没有异步调用 ```proceed```，则返回值就是最后一个切面的返回值

``` mermaid
graph LR
X[调用方法] ---> |进入切面| A[注解A];
A[注解A] ---> |proceed| B[注解B];
B ---> |proceed| C[注解C];
B ---> |不调用proceed直接return| X;
C ---> |异步proceed之后return会直接返回调用处，但仍会继续下一个切面的逻辑| X;
C ---> |proceed「包括异步」| D[匹配切面];
D ---> |proceed| E[执行原方法];
E ---> |return| X;
```

#### 3、`ProceedJoinPointSuspend` 的 `proceed` 方法

ProceedJoinPointSuspend 新增两个包含 `OnSuspendReturnListener` 的 `proceed` 方法，新增两个包含 `OnSuspendReturnListener2` 的 `proceedIgnoreOther` 方法

- 新增的两个 `proceed` 方法 和 原本的 `proceed` 方法，逻辑和普通函数有所不同，调用后返回值不是切点函数的返回值，但其他逻辑和上边所述两点是一样的
- 新增的两个 `proceed` 方法传入的 `OnSuspendReturnListener` 可以通过回调的 `ProceedReturn` 拿到切点函数返回值，并且通过 `onReturn` 可以修改切点函数的返回值
- 新增的两个 `proceedIgnoreOther` 方法是不再执行切点函数内代码并修改切点函数的返回值 [详情点此查看](/AndroidAOP/zh/Suspend_cut/#proceed)


## getArgs

就是切点方法被调用时的传入的所有参数

[这块有关于 lambda 表达式的 args 是咋回事介绍](/AndroidAOP/zh/AndroidAopMatchClassMethod/#_8)

## getOriginalArgs()

和 args 一样的东西，只是引用地址不同，数组里边的对象引用地址相同，在同一个方法存在多个注解或匹配切面时，调用 proceed(args) 会改变 args 引用地址，或者改变args里边的引用地址，通过 getOriginalArgs() 可以拿到最初进切点方法时参数

## getTarget

如果切点方法不是静态方法，target 就是切点方法所在对象，如果切点方法是静态方法，target 就是 null

PS：ProceedJoinPoint.target 如果为null的话是因为注入的方法是静态的，一般是 Java 的静态方法和 Kotlin 的被@JvmStatic修饰的函数、顶层函数、lamaba表达式会出现这种情况

[这块有关于 lambda 表达式的 target 是咋回事介绍](/AndroidAOP/zh/AndroidAopMatchClassMethod/#_8)

## getTargetMethod()

返回切点方法相关信息，例如方法名，参数名，参数类型，返回类型等等……具体的可以在方法返回的类里(AopMethod)去查看

[这块有关于 lambda 表达式的 getTargetMethod 是咋回事介绍](/AndroidAOP/zh/AndroidAopMatchClassMethod/#_8)



## getTargetClass()

返回切点方法所在类的 Class<?> 对象



