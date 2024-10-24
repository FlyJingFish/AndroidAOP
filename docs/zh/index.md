

<div style="text-align: center;">
    <svg width="250" height="250" xmlns="http://www.w3.org/2000/svg" id="svgAnimation">
        <image id="frame0" x="0" y="0" width="250" height="250" href="/AndroidAOP/assets/webp/0000.webp" />
        <foreignObject width="100%" height="100%">
            <div xmlns="http://www.w3.org/1999/xhtml">
                <script>
                    window.addEventListener('load', function() {
                        const frame0 = document.getElementById('frame0');
                        frame0.style.display = 'none'; // 初始隐藏

                    });
                </script>
                <div id = "logo_anim_div"></div>
            </div>
        </foreignObject>
    </svg>
</div>

<p align="center">
  <strong>
    🔥🔥🔥帮助 Android App 改造成AOP架构的框架
    <a href="https://flyjingfish.github.io/AndroidAOP/zh/">AndroidAOP</a>
  </strong>
</p>

<p align="center">
  <a href="https://central.sonatype.com/search?q=io.github.FlyJingFish.AndroidAop"><img
    src="https://img.shields.io/maven-central/v/io.github.FlyJingFish.AndroidAop/android-aop-plugin"
    alt="Build"
  /></a>
  <a href="https://github.com/FlyJingFish/AndroidAop/stargazers"><img
    src="https://img.shields.io/github/stars/FlyJingFish/AndroidAop.svg"
    alt="Downloads"
  /></a>
  <a href="https://github.com/FlyJingFish/AndroidAop/network/members"><img
    src="https://img.shields.io/github/forks/FlyJingFish/AndroidAop.svg"
    alt="Python Package Index"
  /></a>
  <a href="https://github.com/FlyJingFish/AndroidAop/issues"><img
    src="https://img.shields.io/github/issues/FlyJingFish/AndroidAop.svg"
    alt="Docker Pulls"
  /></a>
  <a href="https://github.com/FlyJingFish/AndroidAop/blob/master/LICENSE"><img
    src="https://img.shields.io/github/license/FlyJingFish/AndroidAop.svg"
    alt="Sponsors"
  /></a>
</p>


## 简述

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;这是一个帮助 Android App 改造成AOP架构的框架，只需一个注解就可以请求权限、切换线程、禁止多点、一次监测所有点击事件、监测生命周期等等，没有使用 AspectJ，也可以定制出属于你的 Aop 代码

## 特色功能

1、本库内置了开发中常用的一些切面注解供你使用

2、本库支持让你自己做切面，语法简单易上手

3、本库同步支持 Java项目 和 Kotlin项目

4、本库支持切入三方库

5、本库支持切点方法为 Lambda 表达式的情况

6、本库支持切点方法为 suspend 修饰的协程函数

7、本库支持生成所有切点信息Json文件，方便一览所有切点位置[在此配置](#%E5%9B%9B%E5%9C%A8-app-%E7%9A%84buildgradle%E6%B7%BB%E5%8A%A0-androidaopconfig-%E9%85%8D%E7%BD%AE%E9%A1%B9%E6%AD%A4%E6%AD%A5%E4%B8%BA%E5%8F%AF%E9%80%89%E9%85%8D%E7%BD%AE%E9%A1%B9)

**8、本库支持 debug 快速开发模式，让你打包速度几乎不变[在此配置](#%E4%BA%94%E5%BC%80%E5%8F%91%E4%B8%AD%E5%8F%AF%E8%AE%BE%E7%BD%AE%E4%BB%A3%E7%A0%81%E7%BB%87%E5%85%A5%E6%96%B9%E5%BC%8F%E6%AD%A4%E6%AD%A5%E4%B8%BA%E5%8F%AF%E9%80%89%E9%85%8D%E7%BD%AE%E9%A1%B9%E5%8F%AA%E4%B8%BA%E5%9C%A8%E5%BC%80%E5%8F%91%E8%BF%87%E7%A8%8B%E4%B8%AD%E6%8F%90%E9%AB%98%E6%89%93%E5%8C%85%E9%80%9F%E5%BA%A6)**

**9、本库支持 组件化开发[在此配置](#%E4%BA%94%E5%BC%80%E5%8F%91%E4%B8%AD%E5%8F%AF%E8%AE%BE%E7%BD%AE%E4%BB%A3%E7%A0%81%E7%BB%87%E5%85%A5%E6%96%B9%E5%BC%8F%E6%AD%A4%E6%AD%A5%E4%B8%BA%E5%8F%AF%E9%80%89%E9%85%8D%E7%BD%AE%E9%A1%B9%E5%8F%AA%E4%B8%BA%E5%9C%A8%E5%BC%80%E5%8F%91%E8%BF%87%E7%A8%8B%E4%B8%AD%E6%8F%90%E9%AB%98%E6%89%93%E5%8C%85%E9%80%9F%E5%BA%A6)**

**10、本库是纯静态织入AOP代码**

**11、本库不是基于 AspectJ 实现的，织入代码量极少，侵入性极低**

**12、丰富完善的使用文档助你完全理解本库的使用规则[点此前往wiki文档](https://flyjingfish.github.io/AndroidAOP/zh/)**

**13、更有帮助你生成切面代码的插件助手供你使用[点此前往下载](https://flyjingfish.github.io/AndroidAOP/zh/AOP_Helper/)**


#### [点此下载apk,也可扫下边二维码下载](https://github.com/FlyJingFish/AndroidAOP/blob/master/apk/product/release/app-product-release.apk?raw=true)

<img src="/AndroidAOP/screenshot/qrcode.png" alt="show" width="200px" />


## Star趋势图

[![Stargazers over time](https://starchart.cc/FlyJingFish/AndroidAOP.svg?variant=adaptive)](https://starchart.cc/FlyJingFish/AndroidAOP)

---

欢迎使用 AndroidAOP wiki文档，点击左侧导航栏看你想看的内容👈👈👈

建议您先通过浏览[入门](/AndroidAOP/zh/getting_started/#_5)来快速了解本库的使用方法，形成大致思路，然后再阅读其他内容。

选择合适的方式可以写出优美的代码，遇到问题请优先通过浏览wiki文档解决，解决不了的再去首页[加群](#_6)交流

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


## 赞赏

都看到这里了，如果您喜欢 AndroidAOP，或感觉 AndroidAOP 帮助到了您，可以点右上角“Star”支持一下，您的支持就是我的动力，谢谢～ 😃

如果感觉 AndroidAOP 为您节约了大量开发时间、为您的项目增光添彩，您也可以扫描下面的二维码，请作者喝杯咖啡 ☕

### [捐赠列表](/AndroidAOP/zh/give_list)

<div>
<img src="../screenshot/IMG_4075.PNG" width="280" height="350">
<img src="../screenshot/IMG_4076.JPG" width="280" height="350">
</div>

如果在捐赠留言中备注名称，将会被记录到列表中~ 如果你也是github开源作者，捐赠时可以留下github项目地址或者个人主页地址，链接将会被添加到列表中


## 联系方式

* 有问题可以加群大家一起交流 [点此加QQ群：641697838](https://qm.qq.com/cgi-bin/qm/qr?k=w2qDbv_5bpLl0lO0qjXxijl3JHCQgtXx&jump_from=webapi&authKey=Q6/YB+7q9BvOGbYv1qXZGAZLigsfwaBxDC8kz03/5Pwy7018XunUcHoC11kVLqCb)

<img src="../screenshot/qq.png" width="220"/>