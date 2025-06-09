## Brief description 

This function belongs to the advanced function, special attention should be paid when using it, otherwise it will be invalid

This aspect is used to replace the method call in the code, and needs to be used in conjunction with @AndroidAopReplaceMethod. When the replaced method is called, it will enter the method annotated with @AndroidAopReplaceMethod

- _Note that this method is essentially different from the other two. The first two focus on the execution of the method, and automatically retain the method that can execute the original logic (i.e. [ProceedJoinPoint](https://flyjingfish.github.io/AndroidAOP/ProceedJoinPoint/));_
- _This focuses on the method call, which replaces all the calling places with the static methods of the class you set, and does not automatically retain the method that executes the original logic_
- **_The advantage of this method is that it can "equivalently" monitor the call of certain system methods (code in android.jar), and the first two do not have this feature, so if it is not based on this requirement, it is recommended to use [@AndroidAopMatchClassMethod](https://flyjingfish.github.io/AndroidAOP/AndroidAopMatchClassMethod/)_**

!!! note
    <li>To sum up, this function can be said to be a supplement to [@AndroidAopMatchClassMethod](https://flyjingfish.github.io/AndroidAOP/AndroidAopMatchClassMethod/) (the code in android.jar cannot be woven into AOP code). The reason why [ProceedJoinPoint](https://flyjingfish.github.io/AndroidAOP/ProceedJoinPoint/) is not used is that this method may be restricted by different versions of Android. It can neither use reflection to call the original method nor weave in AOP code, so it cannot be encapsulated with [ProceedJoinPoint](/AndroidAOP/ProceedJoinPoint/). If you really want to use it, it is recommended to use [MatchClassMethodProxy](#4-proxy-usage-of-androidaopmatchclassmethod) </li><br> <li><strong>After you modify the configuration of this aspect, you should clean the project before continuing development</strong></li>

## 1. Description

:warning::warning::warning: Note: The defined replacement class should be placed within the scan rules you set [here include exclude Rules](https://flyjingfish.github.io/AndroidAOP/getting_started/#4-add-the-androidaopconfig-configuration-item-in-apps-buildgradle-this-step-is-an-optional-configuration-item), it will not work if written outside the scope

### @AndroidAopReplaceClass

```kotlin
@AndroidAopReplaceClass(
    value = "Full name of the class (including package name)"
    type = Matching type, not required, default SELF
    excludeClasses = An array of classes that exclude inheritance(valid only when type is not SELF), not required
    excludeWeaving = exclude weaving range
    includeWeaving = include weaving range
)
```
- If the class name filled in by value is an internal class, do not use the `$` character, but `.`

- The annotated class is the replacement class; the parameter is the class to be replaced

- If the corresponding replaced method exists in the replacement method of the annotated class, it will not participate in the method replacement

- There are four types of type (if the default ```SELF``` is not set, please note the difference from ```@AndroidAopMatchClassMethod```, the default types are different when they are not set):
    - ```SELF``` means that the class set by value is matched **itself**
    - ```EXTENDS``` means that **all classes inherited from** the class set by value are matched
    - ```DIRECT_EXTENDS``` means that the class set by **<em><strong>directly inherited</strong></em>** is matched
    - ```LEAF_EXTENDS``` means matching the class set by **<em><strong>terminal inheritance (no subclasses)</strong></em>** value

    ``` mermaid
    graph LR
    C(Class C) ---> |Class C inherits from Class B| B{Class B};
    B --->|Class B inherits from Class A| A[Class A];
    B --->|DIRECT_EXTENDS / EXTENDS| A;
    C ---->|LEAF_EXTENDS / EXTENDS| A;
    D(Class D) --->|Class D inherits from Class A| A;
    D --->|DIRECT_EXTENDS/ LEAF_EXTENDS / EXTENDS| A;
    ```
  
    In simple terms, ```LEAF_EXTENDS``` and ```DIRECT_EXTENDS``` are two extremes. The former focuses on the last node in the inheritance relationship, while the latter focuses on the first node in the inheritance relationship.

- excludeWeaving and includeWeaving are similar to exclude and include in the [getting started](/AndroidAOP/getting_started/#4-add-the-androidaopconfig-configuration-item-in-apps-buildgradle-this-step-is-an-optional-configuration-item)

### @AndroidAopReplaceNew

```kotlin
@AndroidAopReplaceNew
```

For example, this method changes `new Thread()` to `new MyThread()`

- The annotated method must be public and static, but the method name can be defined arbitrarily

- There can only be one method parameter, and the parameter is the replaced class
    - If the replaced class does not inherit the replaced class, then there will be problems with the subsequent object method call. You need to replace all its methods with @AndroidAopReplaceMethod
    - If the replaced class inherits the replaced class, then there will be no problem with the subsequent method call

- Is the method return type empty?
    - If it is not empty, the new object will be called back to this method, and the object returned by this method will also replace the new object. Usually when the return type is not empty, you should define it the same as the parameter type.
    - If it is empty, only the class name after new will be replaced.
    - In any case, the class name after new will become the parameter type of the method.
- This will replace all constructor calls.

### @AndroidAopReplaceMethod

```kotlin
@AndroidAopReplaceMethod(
"Method name (including return value type and parameter type)"
)
```

- The annotated method must be public and static, but the method name can be defined arbitrarily

- The annotated method is the replacement method; the parameter is the method to be replaced, which must include the return type and parameter type. The matching rules are as follows [matching rules](https://flyjingfish.github.io/AndroidAOP/AndroidAopReplaceClass/#2-matching-rules)

- If the replaced method is a static method of the class, the parameter type, order and number of the replacement method you define should be consistent

- If the replaced method is a member method of a class, the first parameter of the replacement method you define must be the type of the replaced class (this is the meaning of the Toast.show example below), and the remaining parameter types, order, and number are consistent with the replaced method.
    - There is an exception: the first type can be set to Any (Java is Object). This function is mainly for @AndroidAopReplaceNew not to inherit the replacement class to respond, because the replaced class no longer belongs to the replaced class

- The return type of the annotated method is consistent with the replaced method, regardless of whether the replaced method is static or not

- The replaced method must belong to the replaced class filled in by @AndroidAopReplaceClass

- If the replaced method starts with `<init>`, the function is divided into two cases
    - 1、Fill in according to the following requirements. The function is similar to @AndroidAopReplaceNew. The difference is that this will only call back the new class, will not change the new class name, and can specify the construction method. (The object has been created)
         - The method must have only one parameter, which is the defined class to be replaced (must be equal to the class of @AndroidAopReplaceClass)
         - And the return type cannot be empty (must inherit or be equal to the class of @AndroidAopReplaceClass)
         - The object returned by the method will replace the new object (of course, it is also possible to directly return the callback object)
    - 2、Fill in according to the following requirements. The function is completely different from the previous one. At this time, the object has not been created (this function is available in versions 2.5.8 and above)
         - The parameters must be exactly the same as the constructor's parameter type and order, and finally a Class type parameter is appended (this is to let you know which class the original constructor belongs to)
         - You need to manually rewrite the code to create the object, because in this case the object has not yet been created (the constructor can be called based on the Class returned to you and the current constructor type)
         - And the return type cannot be empty (must inherit or be equal to the class of @AndroidAopReplaceClass)
         - The object returned by the method will be assigned to the original call

For specific writing requirements, please refer to the usage method below

## 2. Matching rules

You can see that the return value type and parameter type are written in the example below. The following is an introduction

**The difference from [@AndroidAopMatchClassMethod](https://flyjingfish.github.io/AndroidAOP/AndroidAopMatchClassMethod/) is that this must be an exact match, and the writing is as follows:**

Matching writing formula: **Return value type method name (parameter type, parameter type...)**

- The return value type, method name, and parameter type must be written in full
- Wrap the parameter type with **()**, separate multiple parameter types with **,**, and only write **()** if there is no parameter
- Use a space to separate the return value type and the method name
- **Both the return value type and the parameter type must be represented by Java types**. Except for the 8 basic types, other reference types are **package name.class name**
- If the function is modified with ```suspend```, then the return value type should be written as ```suspend``` regardless of the type, and the parameter type should still be written according to the above points
- For generic information (such as the collection List), the generic information must be erased

- Different from the replacement class name filled in, if the method parameter and return value type are inner classes, they need to be replaced with `$` instead of `.`

!!! note
    [AOP Code Generation Assistant](https://flyjingfish.github.io/AndroidAOP/AOP_Helper/), can help you generate code with one click

Below is a table showing different types of Kotlin vs. Java. If it is Kotlin code, please check the corresponding code

(If you find any incomplete information, please give me feedback)


| Kotlin type           |      Java type      |
|-----------------------|:-------------------:|
| Int                   |         int         |
| Short                 |        short        |               
| Byte                  |        byte         |               
| Char                  |        char         |               
| Long                  |        long         |               
| Float                 |        float        |               
| Double                |       double        |               
| Boolean               |       boolean       |  
| Int?                  |  java.lang.Integer  |
| Short?                |   java.lang.Short   |               
| Byte?                 |   java.lang.Byte    |               
| Char?                 | java.lang.Character |               
| Long?                 |   java.lang.Long    |               
| Float?                |   java.lang.Float   |               
| Double?               |  java.lang.Double   |               
| Boolean?              |  java.lang.Boolean  |  
| String                |  java.lang.String   |  
| Unit（Or do not write） |        void         |  
| Unit?                 |     kotlin.Unit     |  
| Nothing               |   java.lang.Void    |  
| Any                   |  java.lang.Object   |   


Other data types not in the table above are reference types, and are written as **package name.class name**

!!! note
    1. `vararg str : String` in Kotlin is equivalent to `String...` in Java. In this matching, no matter what kind of code is used, it is represented by `String[]` (String is used as an example here, and other types are the same)<br>
    2. For types with generics, do not write generics, for example, `java.lang.List<String> methodName(java.lang.List<String>)` should be directly written as `java.lang.List methodName(java.lang.List)`

## 3. Use cases

### 1. Java writing

```java

@AndroidAopReplaceClass(
        "android.widget.Toast"
)
public class ReplaceToast {
    @AndroidAopReplaceMethod(
            "android.widget.Toast makeText(android.content.Context, java.lang.CharSequence, int)"
    )
// Because the replaced method is static, the parameter type and order correspond to the replaced method
    public static Toast makeText(Context context, CharSequence text, int duration) {
        return Toast.makeText(context, "ReplaceToast-" + text, duration);
    }

    @AndroidAopReplaceMethod(
            "void setGravity(int , int , int )"
    )
// Because the replaced method is not a static method, the first parameter is the replaced class, and the subsequent parameters correspond to the replaced method
    public static void setGravity(Toast toast, int gravity, int xOffset, int yOffset) {
        toast.setGravity(Gravity.CENTER, xOffset, yOffset);
    }

    @AndroidAopReplaceMethod(
            "void show()"
    )
// Although the replaced method has no parameters, because it is not a static method, the first parameter is still the replaced class
    public static void show(Toast toast) {
        toast.show();
    }
}
```

This example means that all places where ```Toast.makeText``` and ```Toast.show``` in the code are replaced with ```ReplaceToast.makeText``` and ```ReplaceToast.show```

### 2. Kotlin writing method
```kotlin
@AndroidAopReplaceClass("android.util.Log")
object ReplaceLog {
    @AndroidAopReplaceMethod("int e(java.lang.String,java.lang.String)")
    @JvmStatic
    fun e(tag: String, msg: String): Int {
        return Log.e(tag, "ReplaceLog-$msg")
    }
}
```

This example means that all places where ```Log.e``` are written in the code are replaced with ```ReplaceLog.e```

#### If the replaced function is modified with ```suspend```, then you can only write it in Kotlin code, and the replacement function must also be modified with ```suspend```
```kotlin
@AndroidAopReplaceClass("com.flyjingfish.androidaop.MainActivity")
object ReplaceGetData {
    //The only change in the annotation parameter is the return type, which is changed to suspend, and the rest remain unchanged
    @AndroidAopReplaceMethod("suspend getData(int)")
    @JvmStatic
// The function definition writing rules here remain unchanged, just add an additional suspend modifier
    suspend fun getData(mainActivity: MainActivity, num: Int): Int {
        Log.e("ReplaceGetData", "getData")
        return mainActivity.getData(num + 1)
    }
}
```

### 3. Construction method

```kotlin
@AndroidAopReplaceClass(value = "com.flyjingfish.test_lib.TestMatch", type = MatchType.EXTENDS)
object ReplaceTestMatch {

    @AndroidAopReplaceNew
    @JvmStatic
    fun newTestMatch1(testBean: TestMatch3) {
        //Replace the class name after new, the parameter type is the replaced type, the return type of this method is empty, and this method will not be called back
    }

    @AndroidAopReplaceNew
    @JvmStatic
    fun newTestMatch2(testBean: TestMatch3): TestMatch {
        //Replace the class name after new, the parameter type is the replaced type, and the return type of this method is not empty, then this method will be called back, and the returned object will replace the new object
        return new TestMatch ()
    }

    @AndroidAopReplaceMethod("<init>(int)")
    @JvmStatic
    fun getTestBean(testBean: TestMatch): TestMatch {
        //Only one parameter can be the replaced class, the return type cannot be empty, and the object returned by the method will replace the newly created object
        return TestMatch(2)
    }

    @AndroidAopReplaceMethod("<init>(int)")
    @JvmStatic
    fun getTestBean(num: Int,clazz :Class<*>) : TestMatch{
        //The last parameter is of type Class. The types and order of the remaining parameters are exactly the same as the original constructor. The object is created in this method. No object has been created before.
        return TestMatch(num)
    }

}
```

The above three usage methods can replace the new object. The difference is

- The first method directly replaces the new class name (directly replaces the type)

- The second method not only replaces the new class name, but also calls back to the method. The object returned here will also replace the newly created object (the difference between the two is whether the return type is empty)

- The third method is different from the first two in that it **does not replace the new class name**, but calls back to the method. The object returned here will replace the newly created object. And the defined parameter must be one and only one type defined by @AndroidAopReplaceClass, and the return type cannot be null

- The fourth type is different from the first three in that it **will neither replace the new class name nor have an object callback**, you need to manually create this object. Its advantage is that it gets all the parameters for creating the object before the object is created, so that you can modify the parameters in advance and then create the object, **so its advantage is that it has a pre-effect**

- The function defined by @AndroidAopReplaceNew has one and only one parameter, and the parameter type can be any type except the basic type

### 4. Proxy usage of @AndroidAopMatchClassMethod

- 1. First, you must still use `@AndroidAopReplaceClass` to replace the method call, and use `@ProxyMethod` to add annotations to the replacement method

- The replaced method needs to be called in the method implementation
- In addition to the requirements mentioned above, the definition of the method must be consistent with the original method, such as the name, annotations, parameter names, annotations on parameters, etc. of the original method. If you don't use this information, it doesn't matter. If you want to use it, you must do so

```java
package com.flyjingfish.test_lib.replace;

@AndroidAopReplaceClass(
        "android.widget.Toast"
)
public class ReplaceToast {
    @AndroidAopReplaceMethod(
            "android.widget.Toast makeText(android.content.Context, java.lang.CharSequence, int)"
    )
    @ProxyMethod(proxyClass = Toast.class, type = ProxyType.STATIC_METHOD)
    public static Toast makeText(Context context, CharSequence text, int duration) {
        return Toast.makeText(context, text, duration);
    }

    @AndroidAopReplaceMethod(
            "void setGravity(int , int , int )"
    )
    @ProxyMethod(proxyClass = Toast.class, type = ProxyType.METHOD)
    public static void setGravity(Toast toast, int gravity, int xOffset, int yOffset) {
        toast.setGravity(gravity, xOffset, yOffset);
    }

    @AndroidAopReplaceMethod(
            "void show()"
    )
    @ProxyMethod(proxyClass = Toast.class, type = ProxyType.METHOD)
    public static void show(Toast toast) {
        toast.show();
    }
}
```

- 2. Use `@AndroidAopMatchClassMethod` to define the `ReplaceToast` proxy class
- type needs to be MatchType.SELF
- Use MatchClassMethodProxy or MatchClassMethodSuspendProxy class as the aspect processing class
- Implement the invokeProxy or invokeSuspendProxy method to handle the logic

```kotlin 
@AndroidAopMatchClassMethod(
    targetClassName = "com.flyjingfish.test_lib.replace.ReplaceToast",
    type = MatchType.SELF,
    methodName = ["*"]
)
class ReplaceToastProxy : MatchClassMethodProxy() {
    override fun invokeProxy(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e(
            "ReplaceToastProxy",
            "methodName=$methodName," + "parameterNames=${joinPoint.targetMethod.parameterNames.toList()}," + "parameterTypes=${joinPoint.targetMethod.parameterTypes.toList()}," + "returnType=${joinPoint.targetMethod.returnType}," + "args=${joinPoint.args?.toList()},target=${joinPoint.target},targetClass=${joinPoint.targetClass},"
        )

        return joinPoint.proceed()
    }
}
```

So you can For some system methods, `ProceedJoinPoint` is used to control the method call. The key is to use `@ProxyMethod` to mark the method, so that the information returned by `ProceedJoinPoint` is the method information of the replaced class> 

!!! note
    [AOP code generation assistant](https://flyjingfish.github.io/AndroidAOP/AOP_Helper/), which can help you generate proxy usage code with one click