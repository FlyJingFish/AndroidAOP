
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
    🔥🔥🔥Help you transform into an Android platform framework with AOP architecture
    <a href="https://flyjingfish.github.io/AndroidAOP/">AndroidAOP</a>
  </strong>
</p>

<p align="center">
  <a href="https://central.sonatype.com/search?q=io.github.FlyJingFish.AndroidAop"><img
    src="https://img.shields.io/maven-central/v/io.github.FlyJingFish.AndroidAop/android-aop-plugin"
    alt="Build"
  /></a>
  <a href="https://github.com/FlyJingFish/AndroidAop/stargazers"><img
    src="https://img.shields.io/github/stars/FlyJingFish/AndroidAop.svg?style=flat"
    alt="Downloads"
  /></a>
  <a href="https://github.com/FlyJingFish/AndroidAop/network/members"><img
    src="https://img.shields.io/github/forks/FlyJingFish/AndroidAop.svg?style=flat"
    alt="Python Package Index"
  /></a>
  <a href="https://github.com/FlyJingFish/AndroidAop/issues"><img
    src="https://img.shields.io/github/issues/FlyJingFish/AndroidAop.svg?style=flat"
    alt="Docker Pulls"
  /></a>
  <a href="https://github.com/FlyJingFish/AndroidAop/blob/master/LICENSE"><img
    src="https://img.shields.io/github/license/FlyJingFish/AndroidAop.svg?style=flat"
    alt="Sponsors"
  /></a>
  <a href="https://android-arsenal.com/api?level=21"><img
    src="https://img.shields.io/badge/%20MinSdk%20-21%2B-f0ad4e.svg"
    alt="Sponsors"
  /></a>
  <a href="https://flyjingfish.github.io/AndroidAOP/getting_started/#version-restrictions"><img
    src="https://img.shields.io/badge/MinGradle-v7.6+-f0ad4e?logo=gradle"
    alt="Sponsors"
  /></a>
</p>

<p align="center">
  <strong>
    Say goodbye to boilerplate code, free your hands with AOP, and create efficient Android applications with one line of annotations
  </strong>
</p>
<p align="center">
 <strong>
    AndroidAOP, born for developers!
 </strong>
</p>

## Brief Description

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;This is a framework that helps Android App transform into AOP architecture. With just one annotation, you can request permissions, switch threads, prohibit multiple clicks, monitor all click events at once, monitor the life cycle, etc. You can also customize your own Aop code without using AspectJ.


## Special feature

1 . This library has built-in some commonly used aspect annotations for your use

2 . This library allows you to make your own aspects, and the syntax is simple and easy to use

3 .  This library supports Java projects and Kotlin projects at the same time

4 . This library supports cutting into third-party libraries

5 . This library supports the case where the cutting method is a Lambda expression

6 . This library supports the coroutine function modified by suspend for the cutting method

7 . This library supports the generation of all cutting information json and html files, which is convenient for viewing all cutting locations [Configure here](https://flyjingfish.github.io/AndroidAOP/getting_started/#4-add-the-androidaopconfig-configuration-item-in-apps-buildgradle-this-step-is-an-optional-configuration-item)

**8. This library supports multiple rapid development modes, which makes your packaging speed almost unchanged [Configure here](https://flyjingfish.github.io/AndroidAOP/getting_started/#5-you-can-set-the-packaging-method-during-development-this-step-is-an-optional-configuration-item-it-is-recommended-to-configure-this-item-to-speed-up-development)**

**9. This library supports Component-based development [configure here](https://flyjingfish.github.io/AndroidAOP/getting_started/#method-2-debugmode)**

**10. This library is pure static weaving into AOP code**

**11. This library is not implemented based on AspectJ. The amount of woven code is very small and the intrusion is extremely low**

**12. Rich and complete usage documentation helps you fully understand the usage rules of this library [click here to go to the wiki document](https://flyjingfish.github.io/AndroidAOP)**

**13. There are also plug-in assistants that help you generate section codes for your use [click here to download](https://flyjingfish.github.io/AndroidAOP/AOP_Helper)**

#### [Click here to download apk, or scan the QR code below to download](https://github.com/FlyJingFish/AndroidAOP/blob/master/apk/product/release/app-product-release.apk?raw=true)

<img src="/AndroidAOP/screenshot/qrcode.png" alt="show" width="200px" />


## Star trend chart

[![Stargazers over time](https://starchart.cc/FlyJingFish/AndroidAOP.svg?variant=adaptive)](https://starchart.cc/FlyJingFish/AndroidAOP)

---

Welcome to the AndroidAOP wiki document, click on the left navigation bar to see what you want to see👈👈👈

It is recommended that you first browse [Getting Started](/AndroidAOP/getting_started/) to quickly understand how to use this library and form a general idea before reading other content.

Choosing the right method can help you write beautiful code. If you encounter any problem, please browse the wiki document first. If you can't solve it, go to the homepage [join the group](#contact-information) to communicate

## The aspect methods provided by this library are as follows

- **@AndroidAopPointCut** is an annotation aspect. The set annotation can be added to any method. When the added method is called, the aspect processing class can be entered

- **@AndroidAopMatchClassMethod** is a matching aspect. It matches certain methods of a class. When the method of the class is called, the aspect processing class can be entered

- **@AndroidAopReplaceClass** is a replacement aspect. All calls to the method of the set class will be replaced with the method of the replacement aspect class

- **@AndroidAopModifyExtendsClass** It modifies the inherited class and replaces the inherited class of the target class with the annotated class

- **@AndroidAopCollectMethod** collects inherited classes

Except @AndroidAopPointCut, you can use [“AOP Code Generation Assistant”](https://flyjingfish.github.io/AndroidAOP/AOP_Helper/) to assist you in using this library

### The differences are as follows:

- **@AndroidAopMatchClassMethod and @AndroidAopPointCut focus on method execution**

- **@AndroidAopReplaceClass focuses on method call**

- **Note @AndroidAopReplaceClass It is essentially different from the other two. The first two focus on the execution of methods and automatically retain methods that can execute the original logic (i.e. [ProceedJoinPoint](https://flyjingfish.github.io/AndroidAOP/ProceedJoinPoint/));**

- **@AndroidAopReplaceClass focuses on the call of methods, replacing all call locations with static methods of the class you set, and does not automatically retain methods that execute the original logic**

- **_@AndroidAopReplaceClass has the advantage of "equivalent" to monitoring the call of certain system methods (code in android.jar), which the first two do not have. Therefore, if it is not based on this requirement, it is recommended to use [@AndroidAopMatchClassMethod](https://flyjingfish.github.io/AndroidAOP/AndroidAopMatchClassMethod/)_**


## Appreciation

You have read this far. If you like AndroidAOP or feel that AndroidAOP has helped you, you can click the "Star" in the upper right corner to support it. Your support is my motivation. Thank you~ 😃

If you feel that AndroidAOP has saved you a lot of development time and added luster to your project, you can also scan the QR code below to buy the author a cup of coffee ☕

### [Donation List](/AndroidAOP/zh/give_list)

<div>
<img src="screenshot/IMG_4075.PNG" width="280" height="350">
<img src="screenshot/IMG_4076.JPG" width="280" height="350">
</div>

If you note your name in the donation message, it will be recorded in the list~ If you are also a github open source author, you can leave the github project address or personal homepage address when donating, and the link will be added to the list

## Contact information

* If you have any questions, you can join the group to discuss [Click here to join QQ group: 641697838](https://qm.qq.com/cgi-bin/qm/qr?k=w2qDbv_5bpLl0lO0qjXxijl3JHCQgtXx&jump_from=webapi&authKey=Q6/YB+7q9BvOGbYv1qXZGAZLigsfwaBxDC8kz03/5Pwy7018XunUcHoC11kVLqCb)

<img src="screenshot/qq.jpg" width="220"/>