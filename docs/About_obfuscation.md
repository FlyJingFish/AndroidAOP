### Obfuscation rules

This resource library comes with [obfuscation rules](https://github.com/FlyJingFish/AndroidAOP/blob/master/android-aop-core/proguard-rules.pro), and it will be automatically imported. Under normal circumstances, there is no need to import it manually.

### About the mapping problem after obfuscation

Some friends will find that before obfuscation, the line number after the error can locate the error position, but after obfuscation, the original line number cannot be mapped out through the `ProGuard` tool, but it can be mapped out before using this library. That's right! Let's talk about the solution below.

- 1. First, you need to confirm whether your class is a class that has been processed by AOP (you can check the cut point [cutInfo.json](https://github.com/FlyJingFish/AndroidAOP?tab=readme-ov-file#%E5%9B%9B%E5%9C%A8-app-%E7%9A%84buildgradle%E6%B7%BB%E5%8A%A0-androidaopconfig-%E9%85%8D%E7%BD%AE%E9%A1%B9%E6%AD%A4%E6%AD%A5%E4%B8%BA%E5%8F%AF%E9%80%89%E9%85%8D%E7%BD%AE%E9%A1%B9)), if yes, follow the next step to continue trying<br>
- 2. You just need to invalidate AndroidAOP and then generate an obfuscated package again, that is, generate a mapping file that does not contain AOP again. Configure it as follows under the application module<br>

```gradle
androidAopConfig {
    //Set to false to disable AndroidAOP
    enabled false
}
```

Regarding the mapping file configuration, add the following configuration to the obfuscation configuration file

```
# Mapping file
-printmapping proguard-map.txt
# Keep the code line number when throwing an exception
-keepattributes SourceFile,LineNumberTable
```