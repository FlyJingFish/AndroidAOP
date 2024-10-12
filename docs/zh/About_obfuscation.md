
### 混淆规则

此资源库自带[混淆规则](https://github.com/FlyJingFish/AndroidAOP/blob/master/android-aop-core/proguard-rules.pro)，并且会自动导入，正常情况下无需手动导入。

### 关于混淆后映射问题

有些小伙伴会发现混淆前，报错后的行号是可以定位错误位置的，但是混淆后无法通过 `ProGuard` 工具映射出原有行号，但是使用本库前是可以映射出来的。的确如此！下边讲一下解决方法。

- 1、首先需要确认你的这个类是否是经过AOP处理过后的类（可通过查看切点[cutInfo.json](/AndroidAOP/zh/getting_started/#app-buildgradle-androidaopconfig)确认），如果是则按下一步说明继续尝试<br>
- 2、只需要让 AndroidAOP 失效，然后再次打一个混淆包，也就是再次生成一个不包含AOP的映射文件即可，在 application 的 module 下配置如下：

```groovy
androidAopConfig {
    //设置为false 使 AndroidAOP 失效
    enabled false
}
```

关于映射文件配置，在混淆配置文件中加入以下配置

```
# 映射文件
-printmapping proguard-map.txt
# 抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable
```

