<div align="center">
    <img src="../assets/web_logo.svg" width="200" height="200"/>
</div>



欢迎使用 AndroidAOP wiki文档，点击右侧导航栏看你想看的内容👈👈👈

建议您先通过浏览[首页](https://github.com/FlyJingFish/AndroidAOP?tab=readme-ov-file#%E6%AD%A4%E5%A4%96%E6%9C%AC%E5%BA%93%E4%B9%9F%E5%90%8C%E6%A0%B7%E6%94%AF%E6%8C%81%E8%AE%A9%E4%BD%A0%E8%87%AA%E5%B7%B1%E5%81%9A%E5%88%87%E9%9D%A2%E5%AE%9E%E7%8E%B0%E8%B5%B7%E6%9D%A5%E9%9D%9E%E5%B8%B8%E7%AE%80%E5%8D%95)来快速了解本库的使用方法，形成大致思路，然后再来阅读 wiki 文档。

选择合适的方式可以写出优美的代码，遇到问题请优先通过浏览wiki文档解决，解决不了的再去首页[加群](https://github.com/FlyJingFish/AndroidAOP?tab=readme-ov-file#%E8%81%94%E7%B3%BB%E6%96%B9%E5%BC%8F)交流

## 本库提供的切面方式分别如下

- **@AndroidAopPointCut** 是注解切面，设置的注解加到任意一个方法上即可，当所加方法被调用时即可进入切面处理类
- **@AndroidAopMatchClassMethod** 是匹配切面，匹配到某个类的某些方法，当所在类的方法被调用时即可进入切面处理类
- **@AndroidAopReplaceClass** 是替换切面，会把设置的类的方法的所有调用处替换成替换切面类的方法
- **@AndroidAopModifyExtendsClass** 是修改继承类，会将目标类的继承类换成所注解的类
- **@AndroidAopCollectMethod** 是收集继承类

除了 @AndroidAopPointCut 都可以利用 [“AOP 代码生成助手”](AOP_Helper/)辅助你使用本库

### 其区别如下：
- **@AndroidAopMatchClassMethod 和 @AndroidAopPointCut 关注的是方法的执行（Method execution）**

- **@AndroidAopReplaceClass 关注的是方法的调用（Method call）**

- **注意@AndroidAopReplaceClass 和其他两种的有着本质的区别，前两种关注的是方法的执行，并且会自动保留可以执行原有逻辑的方法（即[ProceedJoinPoint](ProceedJoinPoint/)）；**

- **@AndroidAopReplaceClass 关注的是方法的调用，是将所有调用的地方替换为您设置的类的静态方法，并且不会自动保留执行原有逻辑的方法**

- **_@AndroidAopReplaceClass 的优点在于“相当于”可以监测到某些系统方法（android.jar里的代码）的调用，前两者不具备这个特点，所以如果不是基于此种需求，建议使用 [@AndroidAopMatchClassMethod](AndroidAopMatchClassMethod/)_**

