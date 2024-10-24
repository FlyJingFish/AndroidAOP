## Brief description

Information related to the pointcut method, including pointcut method parameters, pointcut object, and continued execution of the original method logic, etc.

## proceed

#### 1. Execute the logic of the original method

In this introduction, `proceed()` or `proceed(args)` of the `ProceedJoinPoint` object is used to execute the logic of the original method. The difference is:

- `proceed()` does not pass parameters, indicating that the original incoming parameters are not changed
- `proceed(args)` has parameters, indicating that the parameters passed in at the time are rewritten. Note that the number of parameters passed in and the type of each parameter must be consistent with the aspect method
- If `proceed` is not called, the code in the interception aspect method will not be executed

#### 2. Execute the next aspect

When there are multiple annotations or matching aspects for the same method, `proceed` means entering the next aspect. How to deal with it specifically?

- When multiple facets are superimposed on a method, annotations take precedence over matching facets, and annotated facets are executed from top to bottom
- The next facet will be executed only after ```proceed``` is called, and the code in the entry method will be called only after the last facet among multiple facets executes ```proceed```
- Calling ```proceed(args)``` in the previous facet can update the parameters passed in by the method, and the next facet will also get the parameters updated in the previous layer
- When there is an asynchronous call ```proceed```, the return value of the first asynchronous call ```proceed``` facet (that is, the return value of invoke) is the return value of the entry method; otherwise, if there is no asynchronous call ```proceed```, the return value is the return value of the last facet

<p align="center" style="font-size:14px">
👇Execution order and return diagram👇
</p>

``` mermaid
graph LR
Call[Calling method] --> |Enter section| A[Annotation section 1];
A --> |Synchronous proceed| B[Annotation section 2];
B --> |Synchronous proceed| C[Matching section];
C --> |Synchronous proceed| From[Execute original method];
From --> |return| C;
C --> |return| B;
B --> |return| A;
A --> |return| Call;
```

<p align="center" style="font-size:14px">
👇Schematic diagram of asynchronous call👇
</p>

``` mermaid
graph LR
Call[Calling method] --> |Enter section| A[Section];
A --> |Start asynchronous thread| B[thread];
B --> |asynchronous proceed| From[other aspects];
From --> |return| B;
A --> |<span style='color:red'>because asynchronous threads will return directly</span>| Call;
```

<p align="center" style="font-size:14px">
👇Sketch of not calling proceed👇
</p>

``` mermaid
graph LR
Call[Calling method] --> |Enter section| A[Section 1];
A --> |proceed| B[Section 2];
B -..-> |<span style='color:red'>X</span>| C[Section 3];
C -..-> |<span style='color:red'>X</span>| From[Execute original method];
B --> |<span style='color:red'>Do not call proceed directly return</span>| A;
A --> |return| Call;
```

#### 3. `ProceedJoinPointSuspend`'s `proceed` method

ProceedJoinPointSuspend adds two new methods including `OnSuspendReturnListener` `proceed` method, two `proceedIgnoreOther` methods containing `OnSuspendReturnListener2` are added

- The logic of the two new `proceed` methods and the original `proceed` method is different from that of ordinary functions. The return value after calling is not the return value of the pointcut function, but the other logic is the same as the two points mentioned above
- The `OnSuspendReturnListener` passed in by the two new `proceed` methods can get the return value of the pointcut function through the callback `ProceedReturn`, and the return value of the pointcut function can be modified through `onReturn`
- The two new `proceedIgnoreOther` methods are to stop executing the code in the pointcut function and modify the return value of the pointcut function [Click here for details](https://flyjingfish.github.io/AndroidAOP/Suspend_cut/#2-basepointcutsuspend-and-matchclassmethodsuspend-that-support-suspend) 

!!! note
    `ProceedJoinPointSuspend` The newly added methods are used to modify the return value of the call point suspend function. The suspend function can no longer modify the return value of the call point by modifying the return value <br>
    1. Calling **the newly added proceed function** Using `ProceedReturn.proceed` in the callback is equivalent to the process of calling `proceed` between each section in the above figure <br>
    2. Calling **the newly added proceedIgnoreOther function** in the callback is equivalent to <span style='color:red'>Do not call proceed directly return</span> in the above figure <br>
    3. The above two methods must be called in the suspend aspect, otherwise problems will occur

## getArgs

All the parameters passed in when the pointcut method is called

[This is an introduction to the args of lambda expressions](https://flyjingfish.github.io/AndroidAOP/AndroidAopMatchClassMethod/#example-2)

## getOriginalArgs()

Same as args, but with a different reference address. The object reference addresses in the array are the same. When there are multiple annotations or matching aspects in the same method, calling proceed(args) will change the reference address of args, or change the reference address in args. Through getOriginalArgs(), you can get the parameters when the pointcut method is first entered

## getTarget

If the pointcut method is not a static method, target is the object where the pointcut method is located. If the pointcut method is a static method, target is null

PS: If ProceedJoinPoint.target is null, it is because the injected method is static, usually Java This situation will occur in static methods of Kotlin and functions modified by @JvmStatic, top-level functions, and lamba expressions

[Here is an introduction to the target of lambda expressions](https://flyjingfish.github.io/AndroidAOP/AndroidAopMatchClassMethod/#example-2)

## getTargetMethod()

Returns information related to the point-cut method, such as method name, parameter name, parameter type, return type, etc. ... You can check the specific information in the class returned by the method (AopMethod)

[Here is an introduction to the getTargetMethod of lambda expressions](https://flyjingfish.github.io/AndroidAOP/AndroidAopMatchClassMethod/#example-2)

## getTargetClass()

Returns the Class<?> object of the class where the point-cut method is located