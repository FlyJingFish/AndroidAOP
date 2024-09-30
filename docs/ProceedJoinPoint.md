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

#### 3. `ProceedJoinPointSuspend`'s `proceed` method

ProceedJoinPointSuspend adds two new methods including `OnSuspendReturnListener` `proceed` method, two `proceedIgnoreOther` methods containing `OnSuspendReturnListener2` are added

- The logic of the two new `proceed` methods and the original `proceed` method is different from that of ordinary functions. The return value after calling is not the return value of the pointcut function, but the other logic is the same as the two points mentioned above
- The `OnSuspendReturnListener` passed in by the two new `proceed` methods can get the return value of the pointcut function through the callback `ProceedReturn`, and the return value of the pointcut function can be modified through `onReturn`
- The two new `proceedIgnoreOther` methods are to stop executing the code in the pointcut function and modify the return value of the pointcut function [Click here for details](https://github.com/FlyJingFish/AndroidAOP/wiki/Suspend-%E5%88%87%E7%82%B9%E5%87%BD%E6%95%B0#%E5%8F%A6%E5%A4%96%E5%AF%B9%E4%BA%8E%E6%99%AE%E9%80 %9A%E5%88%87%E9%9D%A2%E5%A4%84%E7%90%86%E7%B1%BB%E4%B8%8D%E8%B0%83%E7%94%A8-proceed-%E7%9B%B4%E6%8E%A5%E8%BF%94%E5%9B%9E%E7%9A% 84%E4%BE%8B%E5%A6%82) ## getArgs

All the parameters passed in when the pointcut method is called

[This is an introduction to the args of lambda expressions](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopMatchClassMethod#%E4%BE%8B%E4%BA%8C)

## getOriginalArgs()

Same as args, but with a different reference address. The object reference addresses in the array are the same. When there are multiple annotations or matching aspects in the same method, calling proceed(args) will change the reference address of args, or change the reference address in args. Through getOriginalArgs(), you can get the parameters when the pointcut method is first entered

## getTarget

If the pointcut method is not a static method, target is the object where the pointcut method is located. If the pointcut method is a static method, target is null

PS: If ProceedJoinPoint.target is null, it is because the injected method is static, usually Java This situation will occur in static methods of Kotlin and functions modified by @JvmStatic, top-level functions, and lamba expressions

[Here is an introduction to the target of lambda expressions](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopMatchClassMethod#%E4%BE%8B%E4%BA%8C)

## getTargetMethod()

Returns information related to the point-cut method, such as method name, parameter name, parameter type, return type, etc. ... You can check the specific information in the class returned by the method (AopMethod)

[Here is an introduction to the getTargetMethod of lambda expressions](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopMatchClassMethod#%E4%BE%8B%E4%BA%8C)

## getTargetClass()

Returns the Class<?> object of the class where the point-cut method is located