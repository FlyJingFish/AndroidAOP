## Brief description

This aspect is used to match a class and its corresponding method. This aspect focuses on the execution of the method (Method Execution). Please note the difference between it and [@AndroidAopReplaceClass](/AndroidAOP/AndroidAopReplaceClass).

```kotlin
@AndroidAopMatchClassMethod(
    targetClassName = target class name(including package name),
    methodName = method name array,
    type = match type, optional, default EXTENDS
    excludeClasses = array of classes to exclude in inheritance relationship (valid only when type is not SELF), optional
)
```

- When targetClassName is an inner class, do not use the `$` character, but use `.`

- There are four types of type (default ```EXTENDS``` is not set):
    - ```SELF``` means that the match is **itself** of the class set by targetClassName
    - ```EXTENDS``` means that the match is **all classes inherited from** the class set by targetClassName
    - ```DIRECT_EXTENDS``` means that the match is **<em><strong>directly inherited from</strong></em>** the class set by targetClassName
    - ```LEAF_EXTENDS``` means that the match is **<em><strong>Terminal inheritance (no subclasses)</strong></em>** The class set by targetClassName

    Simply put, ```LEAF_EXTENDS``` and ```DIRECT_EXTENDS``` are two extremes. The former focuses on the last node in the inheritance relationship, while the latter focuses on the first node in the inheritance relationship. Also note that ```EXTENDS``` This type of match has a wide range, and all inherited intermediate classes may also add aspect codes

- excludeClasses
    - If targetClassName is a class name, it means excluding some classes in the inheritance relationship. You can set multiple, and type is not SELF to be effective
      - If targetClassName is a package name, it means excluding some matched classes. You can set multiple, and type is SELF to be effective

- overrideMethod defaults to false
    - Set to true, if there is no matching method in the subclass (non-interface, can be an abstract class), the parent class method is overwritten
        - targetClassName cannot contain *
        - methodName cannot define [ "*" ]
        - The overridden method cannot be private or final

    - Set to false, if there is no matching method in the subclass, no processing is done

!!! note
    In addition, not all classes can be hooked in<br> <li>```type``` Type is ```SELF``` When ```targetClassName``` is set, the class must be the code in the installation package.
    For example: if this class (such as Toast) is in **android.jar**, it will not work. If you have such a requirement, you should use [@AndroidAopReplaceClass](/AndroidAOP/AndroidAopReplaceClass)</li><br> <li>```type``` When the type is not ```SELF```, this aspect needs to have a matching method to work. If the subclass does not override the matching method, the subclass will not be matched. Use overrideMethod to ignore this restriction</li> <br> <li><strong>When you modify the configuration of this aspect, in most cases you should clean the project and continue development</strong></li>

## Create an aspect processing class

The aspect processing class needs to implement the MatchClassMethod interface and handle the aspect logic in invoke

```kotlin
interface MatchClassMethod {
    fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any?
}
```

!!! note
    If the point function is suspend [Click here to view](/AndroidAOP/AOP_Helper/)

- [ProceedJoinPoint Introduction](/AndroidAOP/ProceedJoinPoint/)
- [Introduction to invoke return value](https://flyjingfish.github.io/AndroidAOP/Pointcut_return/)
- [Life Cycle](/AndroidAOP/FAQ/#6-what-is-the-life-cycle-of-the-aspect-processing-class-of-the-matching-aspect-and-the-annotation-aspect) ## Matching rules

You can see that in the following examples, some of the method names are set with only the method name, while others also have the return value type and parameter type. The following is an introduction

### Fuzzy matching

- 1. When targetClassName ends with `.*` and has other characters, and `type = MatchType.SELF`, it matches all classes under the package, including subpackages [as shown in Example 9 below](#example-9)
- 2. When methodName has only one method name `*`, it matches all methods in the class [as shown in Example 8 below](#example-8)
- 3. When methodName only writes the method name but does not write the return type and parameter type, it is also a fuzzy match, which will add all methods with the same name in the target class to the cut point

!!! note
    For matching package names, I strongly recommend not doing this, as it intrudes too much code and significantly reduces the packaging speed. It is recommended to use it only for debugging and logging, and the original method should be released in full

### Precise matching

Write the return type and parameter type on the methodName method name, so that you can accurately find a method to add a cut point (it will automatically degenerate to fuzzy matching when the precise matching is abnormal)

Matching formula: **Return value type method name (parameter type, parameter type...)**

- Return value type can be omitted
- Method name must be written
- Parameter type can be omitted. If written, wrap it with **()**. Multiple parameter types are separated by **,**. If there is no parameter, just write **()**
- Separate the return value type and method name with a space
- If the return value type and parameter type are not written, it means no verification
- **Return value type and parameter type must be expressed in Java types**. Except for the 8 basic types, other reference types are **package name.class name**
- If the function is modified with ```suspend```, then the return value type is written regardless of the type ```suspend```, parameter types should still be written according to the above points
- For generic information (such as collection List), the generic information must be erased

- Different from targetClassName, if the method parameter and return value type are inner classes, they need to be replaced with `$` instead of `.`

!!! note
    [AOP Code Generation Assistant](/AndroidAOP/AOP_Helper/), which can help you generate code with one click

Below is a comparison table of Kotlin and Java with different types. If it is Kotlin code, please check the corresponding code

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
| Unit?                 |   java.lang.Void    |  
| Any                   |  java.lang.Object   |   

Other data types not listed above are reference types, and are written as **package name.class name**

!!! note
    1. `vararg str : String` in Kotlin is equivalent to `String...` in Java. In this matching, no matter what kind of code is used, it is represented by `String[]` (String is used as an example here, and other types are the same)<br>
    2. For types with generics, do not write generics, for example, `java.lang.List<String> methodName(java.lang.List<String>)` should be directly written as `java.lang.List methodName(java.lang.List)`

## Examples of various scenarios

#### Example 1

Want to monitor all startActivity jumps inherited from the AppCompatActivity class

```java

@AndroidAopMatchClassMethod(
        targetClassName = "androidx.appcompat.app.AppCompatActivity",
        methodName = {"startActivity"},
        type = MatchType.EXTENDS
)
public class MatchActivityMethod implements MatchClassMethod {
    @Nullable
    @Override
    public Object invoke(@NonNull ProceedJoinPoint joinPoint, @NonNull String methodName) {
// Write your logic here
        return joinPoint.proceed();
    }
}
```

**:warning:Note: For matching subclass methods, if the subclass does not override the matching method, it is invalid. Use overrideMethod to ignore this limitation**

#### Example 2

If you want to hook all android.view.View.OnClickListener onClick, to put it simply, is to globally monitor all click events set onClickListener, the code is as follows:

```kotlin
@AndroidAopMatchClassMethod(
    targetClassName = "android.view.View.OnClickListener",
    methodName = ["onClick"],
    type = MatchType.EXTENDS //type must be EXTENDS because you want to hook all classes that inherit OnClickListener
)
class MatchOnClick : MatchClassMethod {
    // @SingleClick(5000) //Combined with @SingleClick, add multi-click protection to all clicks, 6 or not
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e("MatchOnClick", "=====invoke=====$methodName")
        return joinPoint.proceed()
    }
}
```

Here is a reminder for those who use lambda click monitoring;

ProceedJoinPoint The target is not android.view.View.OnClickListener
- For Java, the target is the object of the class that sets the lambda expression
- For Kotlin, the target is null

The methodName of the invoke callback is not onClick, but the method name automatically generated at compile time, similar to onCreate$lambda$14, which contains the lambda keyword

For the view of onClick(view:View)
- If it is Kotlin code, ProceedJoinPoint.args[1]
- If it is Java code, ProceedJoinPoint.args[0]

I will not go into details about this, you will know it after using it yourself;

**To summarize: In fact, for all lambda's ProceedJoinPoint.args**

- If it is Kotlin, the first parameter is the object of the class that sets the lambda expression, and the subsequent parameters are all the parameters of the hook method
- If it is Java, starting from the first parameter, it is all the parameters of the hook method

#### Example 3

The target class has multiple methods with the same name, and you only want to match one method (the exact matching rule is mentioned above)

```java
package com.flyjingfish.test_lib;

public class TestMatch {
    public void test(int value1) {

    }

    public String test(int value1, String value2) {
        return value1 + value2;
    }
}

```
For example, there is a class TestMatch with two methods named test. You only want to match the method test(int value1,String value2). Then write it as follows:

```kotlin
package com.flyjingfish.test_lib.mycut;

@AndroidAopMatchClassMethod(
    targetClassName = "com.flyjingfish.test_lib.TestMatch",
    methodName = ["java.lang.String test(int,java.lang.String)"],
    type = MatchType.SELF
)
class MatchTestMatchMethod : MatchClassMethod {
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e("MatchTestMatchMethod",
            "======" + methodName + ",getParameterTypes=" + joinPoint.getTargetMethod()
                .getParameterTypes().length
        );
// Write your logic here
// If you don't want to execute the original method logic, 👇 don't call the following sentence
        return joinPoint.proceed()
    }
}

```
#### Example 4

When there are many levels of inheritance relationships, you don't want to add aspects to each level

```kotlin
@AndroidAopMatchClassMethod(
    targetClassName = "android.view.View.OnClickListener",
    methodName = ["onClick"],
    type = MatchType.EXTENDS // type must be EXTENDS because you want to hook all classes that inherit OnClickListener
)
class MatchOnClick : MatchClassMethod {
    // @SingleClick(5000) //Join @SingleClick to add anti-multiple clicks to all clicks, 6 or 6
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e("MatchOnClick", "=====invoke=====$methodName")
        return joinPoint.proceed()
    }
}
```

```java
public abstract class MyOnClickListener implements View.OnClickListener {
    @Override
    public void onClick(View v) {
        ...
        //This is the necessary logic code
    }
}

```

```kotlin
binding.btnSingleClick.setOnClickListener(object : MyOnClickListener() {
    override fun onClick(v: View?) {
        super.onClick(v)//Especially this sentence calls the parent class onClick and wants to retain the logic of executing the parent class method
        onSingleClick()
    }
})
```

Writing this way will cause the MyOnClickListener onClick above to also be added to the aspect, which is equivalent to a click that calls back twice the invoke of the aspect processing class, which may not be what we want, so we can change it like this
```kotlin
@AndroidAopMatchClassMethod(
    targetClassName = "android.view.View.OnClickListener",
    methodName = ["onClick"],
    type = MatchType.EXTENDS,
    excludeClasses = ["com.flyjingfish.androidaop.test.MyOnClickListener"]//Adding this can exclude some classes
)
class MatchOnClick : MatchClassMethod {
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e("MatchOnClick", "=====invoke=====$methodName")
        return joinPoint.proceed()
    }
}
```
Or set type to LEAF_EXTENDS directly filters out the intermediate classes (this reminds me of an advertisement: no middlemen make a profit from the price difference)

#### Example 5
What if the entry point is a companion object?

Suppose there is such a code
```kotlin
package com.flyjingfish.androidaop

class ThirdActivity : BaseActivity() {
    companion object {
        fun start() {
            ...
        }
    }
}
```
The first letter of the companion object modifier companion is capitalized, as shown below
```kotlin
@AndroidAopMatchClassMethod(
    targetClassName = "com.flyjingfish.androidaop.ThirdActivity.Companion",
    methodName = ["start"],
    type = MatchType.SELF
)
class MatchCompanionStart : MatchClassMethod {
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e("MatchCompanionStart", "======$methodName")
        return joinPoint.proceed()
    }
}
```

#### Example 6

The entry point is Kotlin Member variables of the code, want to monitor the assignment and retrieval operations

```kotlin
package com.flyjingfish.androidaop.test

class TestBean {
    var name: String = "test"
}
```

In the code, we will have such operations

```kotlin
testBean.name = "1111" //Assignment operation
val name = testBean.name //Get value operation
```

You can write like this

```kotlin
@AndroidAopMatchClassMethod(
    targetClassName = "com.flyjingfish.androidaop.test.TestBean",
    methodName = ["setName", "getName"],
    type = MatchType.SELF
)
class MatchTestBean : MatchClassMethod {
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e("MatchTestBean", "======$methodName");
        ToastUtils.makeText(ToastUtils.app, "MatchTestBean======$methodName")
        return joinPoint.proceed()
    }
}
```

#### Example 7

If the cut point method is ```suspend``` What about modified functions?

- You can directly use [Fuzzy Matching](#fuzzy-matching)

- If you want to use [Exact Matching](#precise-matching), the writing is as follows. For specific rules, see [Exact Matching](#precise-matching)


```kotlin
package com.flyjingfish.androidaop

class MainActivity : BaseActivity2() {
    suspend fun getData(num: Int): Int {
        return withContext(Dispatchers.IO) {
            getDelayResult()
        }
    }
}
```

The exact match is written as follows. Regardless of the return value type of the matching function, write ```suspend```. For details, see the [Exact Matching Part](#precise-matching)

```kotlin
@AndroidAopMatchClassMethod(
    targetClassName = "com.flyjingfish.androidaop.MainActivity",
    methodName = ["suspend getData(int)"],
    type = MatchType.SELF
)
class MatchSuspend : MatchClassMethod {
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e("MatchSuspend", "======$methodName") return joinPoint.proceed()
    }
} 
``` 


#### Example 8

Want to match all methods of a class

```kotlin
@AndroidAopMatchClassMethod(
targetClassName = "com.flyjingfish.androidaop.SecondActivity",
methodName = ["*"],
type = MatchType.SELF
)
class MatchAllMethod : MatchClassMethod {
override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
Log.e("MatchMainAllMethod", "AllMethod======$methodName");
return joinPoint.proceed()
}
}
```
Write only one method name and fill in * to match all methods

#### Example 9

Want to match all methods of all classes in a package

```kotlin
@AndroidAopMatchClassMethod(
    targetClassName = "com.flyjingfish.androidaop.*",
    methodName = ["*"],
    type = MatchType.SELF
)
class MatchAll : MatchClassMethod {
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e(
            "MatchAll",
            "---->${joinPoint.targetClass}--${joinPoint.targetMethod.name}--${joinPoint.targetMethod.parameterTypes.toList()}"
        );
        return joinPoint.proceed()
    }
}
```
1. `*` replaces `class name` Or replace `part of the package name + class name`, this example represents all classes under the `com.flyjingfish.androidaop` package and its subpackages <br>
2. Of course, the methodName part can still be filled with multiple fuzzy matching or even exact matching method names