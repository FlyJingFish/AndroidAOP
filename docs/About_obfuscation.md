### Obfuscation rules

This resource library comes with [obfuscation rules](https://github.com/FlyJingFish/AndroidAOP/blob/master/android-aop-core/proguard-rules.pro), and it will be automatically imported. Under normal circumstances, there is no need to import it manually.

### About the mapping problem after obfuscation

Some friends will find that before obfuscation, the line number after the error can locate the error position, but after obfuscation, the original line number cannot be mapped out through the `ProGuard` tool, but it can be mapped out before using this library. That's right! Let's talk about the solution below.

- 1. First, you need to confirm whether your class is a class that has been processed by AOP (you can check the cut point [cutInfoJson](/AndroidAOP/getting_started/#4-add-the-androidaopconfig-configuration-item-in-apps-buildgradle-this-step-is-an-optional-configuration-item)), if yes, follow the next step to continue trying<br>
- 2. You just need to invalidate AndroidAOP and then generate an obfuscated package again, that is, generate a mapping file that does not contain AOP again. Configure it as follows under the application module<br>

```groovy
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