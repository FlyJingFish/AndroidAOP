document.addEventListener("DOMContentLoaded", function () {
    let currentPath = window.location.pathname;

    // 可以在这里操作 DOM
    if(currentPath == "/AndroidAOP/"){//首页
        let isZh = localStorage.getItem('isZh');
//        console.log("DOM 完全加载和解析完成，isZh="+isZh);
        if (isZh == "true") {//在中文网页
            window.location.href = currentPath.replace('/AndroidAOP/', '/AndroidAOP/zh/');
        }
    }

    //在这保存1
    const isZh = currentPath.includes("/zh/");
    localStorage.setItem("isZh",isZh+"");

    const languageSelectors2 = document.querySelectorAll(".md-tabs__link");

    languageSelectors2.forEach(selector => {
//        console.log("DOM 完全加载和解析完成，selector1="+selector.tagName);
        selector.addEventListener("click", function () {
            var target = event.target || event.srcElement;      // 兼容处理
            if (target.nodeName.toLocaleLowerCase() === "a") {    // 判断是否匹配目标元素
                const href = target.getAttribute("href");
                if(href == '..' || href == '.'){
                    localStorage.setItem('isZh', "false");
                }else{
                    localStorage.setItem('isZh', "true");
                }

                event.preventDefault();
                window.location.href = href

            }


        });
    });

    const languageSelectors3 = document.querySelectorAll(".md-logo");

    languageSelectors3.forEach(selector => {
        selector.addEventListener("click", function () {
            var target = event.target || event.srcElement;      // 兼容处理

            if (target.nodeName.toLocaleLowerCase() === "img") {    // 判断是否匹配目标元素
                localStorage.setItem('isZh', "false");
            }
        });
    });
});
