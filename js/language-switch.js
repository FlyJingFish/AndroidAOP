document.addEventListener("DOMContentLoaded", function () {
    let currentPath = window.location.pathname;
    const isZh = currentPath.includes("/zh/");

    const languageSelectorsLink = document.querySelectorAll(".md-select__link");
    languageSelectorsLink.forEach(function(element) {
        var attributeValue = element.getAttribute('hreflang'); // 获取每个元素的 data-id 属性值
//        console.log('itemLang='+attributeValue);
        if(isZh){
            if (attributeValue == "zh") {
               element.parentElement.style.display = 'none';
            }
        }else{
            if (attributeValue != "zh") {
               element.parentElement.style.display = 'none';
            }
        }
    });

    const languageSelectors = document.querySelectorAll(".md-select__item");
    languageSelectors.forEach(selector => {
        selector.addEventListener("click", function () {
            var target = event.target || event.srcElement;      // 兼容处理
            if (target.nodeName.toLocaleLowerCase() === "a") {    // 判断是否匹配目标元素
                const selectedLang = target.getAttribute("hreflang");
				// let hash = window.location.hash;
                // console.log('currentPath='+currentPath);
                // console.log('selectedLang='+selectedLang);
                event.preventDefault();
				// console.log('isZh='+isZh);
				// console.log('hash='+hash);
				
                if (isZh) {//在中文网页
					if(selectedLang == "en"){
						window.location.href = currentPath.replace('/zh/', '/');
						//在这保存2
						localStorage.setItem("isZh", "false");
					}else{
						window.location.reload();
					}
                }else{//英文网页
                	if(selectedLang == "zh"){
                		window.location.href = currentPath.replace('/AndroidAOP/', '/AndroidAOP/zh/');
                		//在这保存2
                		localStorage.setItem("isZh", "true");
                	}else{
						window.location.reload();
                	}
                }
            }


        });
    });



    // 可以在这里操作 DOM
    if(currentPath == "/AndroidAOP/"){//首页
        let isZh = localStorage.getItem('isZh');
//        console.log("DOM 完全加载和解析完成，isZh="+isZh);
        if (isZh == "true") {//在中文网页
            window.location.href = currentPath.replace('/AndroidAOP/', '/AndroidAOP/zh/');
        }
    }

    //在这保存1

    localStorage.setItem("isZh",isZh+"");

    const languageSelectors2 = document.querySelectorAll(".md-tabs__link");

    languageSelectors2.forEach(selector => {
//        console.log("DOM 完全加载和解析完成，selector1="+selector.tagName);
        selector.addEventListener("click", function () {
            var target = event.target || event.srcElement;      // 兼容处理
            if (target.nodeName.toLocaleLowerCase() === "a") {    // 判断是否匹配目标元素
                const href = target.getAttribute("href");
                if(href == '..' || href == '.' || href == '../..'){
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

    const languageSelectors4 = document.querySelectorAll(".md-nav__link");

    languageSelectors4.forEach(selector => {
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
});