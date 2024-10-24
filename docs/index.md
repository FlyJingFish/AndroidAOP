
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


## Brief Description

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;This is a framework that helps Android App transform into AOP architecture. With just one annotation, you can request permissions, switch threads, prohibit multiple clicks, monitor all click events at once, monitor the life cycle, etc. You can also customize your own Aop code without using AspectJ.


## Special feature

1 . This library has built-in some aspect annotations commonly used in development for you to use.

2 . This library supports you to make aspects by yourself, and the syntax is simple and easy to use.

3 . This library supports Java and Kotlin code simultaneously

4 . This library supports switching into third-party libraries

5 . This library supports the case where the pointcut method is a Lambda expression.

6 . This library supports coroutine functions whose pointcut methods are suspend.

7 . This library supports generating Json files of all pointcut information to facilitate an overview of all pointcut locations [Configure here](#4-add-the-androidaopconfig-configuration-item-in-apps-buildgradle-this-step-is-an-optional-configuration-item)

**8 . This library supports debug rapid development mode, allowing you to package at almost the same speed**

**9 . This library supports component-based development mode**

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

It is recommended that you first browse [Getting Started](/AndroidAOP/getting_started/#custom-aspects) to quickly understand how to use this library and form a general idea before reading other content.

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

<img src="screenshot/qq.png" width="220"/>