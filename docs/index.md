
Welcome to the AndroidAOP wiki document, click on the left navigation bar to see what you want to see👈👈👈

It is recommended that you first browse the [Getting Started](/AndroidAOP/getting_started/#custom-aspects) to quickly understand how to use this library, form a general idea, and then read the wiki document.

Choosing the right method can help you write beautiful code. If you encounter any problem, please browse the wiki document first. If you can't solve it, go to the homepage [join the group](https://github.com/FlyJingFish/AndroidAOP?tab=readme-ov-file#%E8%81%94%E7%B3%BB%E6%96%B9%E5%BC%8F) to communicate

## The aspect methods provided by this library are as follows

- **@AndroidAopPointCut** is an annotation aspect. The set annotation can be added to any method. When the added method is called, the aspect processing class can be entered

- **@AndroidAopMatchClassMethod** is a matching aspect. It matches certain methods of a class. When the method of the class is called, the aspect processing class can be entered

- **@AndroidAopReplaceClass** is a replacement aspect. All calls to the method of the set class will be replaced with the method of the replacement aspect class

- **@AndroidAopModifyExtendsClass** It modifies the inherited class and replaces the inherited class of the target class with the annotated class
- **@AndroidAopCollectMethod** collects inherited classes

Except @AndroidAopPointCut, you can use [“AOP Code Generation Assistant”](https://github.com/FlyJingFish/AndroidAOP/wiki/AOP-%E4%BB%A3%E7%A0%81%E7%94%9F%E6%88%90%E5%8A%A9%E6%89%8B) to assist you in using this library

### The differences are as follows:
- **@AndroidAopMatchClassMethod and @AndroidAopPointCut focus on method execution**

- **@AndroidAopReplaceClass focuses on method call**

- **Note @AndroidAopReplaceClass It is essentially different from the other two. The first two focus on the execution of methods and automatically retain methods that can execute the original logic (i.e. [ProceedJoinPoint](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint)); **

- **@AndroidAopReplaceClass focuses on the call of methods, replacing all call locations with static methods of the class you set, and does not automatically retain methods that execute the original logic**

- **_@AndroidAopReplaceClass has the advantage of "equivalent" to monitoring the call of certain system methods (code in android.jar), which the first two do not have. Therefore, if it is not based on this requirement, it is recommended to use [@AndroidAopMatchClassMethod](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopMatchClassMethod)_**