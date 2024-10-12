document.addEventListener("DOMContentLoaded", function () {
    const languageSelectors = document.querySelectorAll(".md-select__item");

    languageSelectors.forEach(selector => {
        selector.addEventListener("click", function () {
            var target = event.target || event.srcElement;      // 兼容处理
            if (target.nodeName.toLocaleLowerCase() === "a") {    // 判断是否匹配目标元素
                const selectedLang = target.getAttribute("hreflang");
                let currentPath = window.location.pathname;
				// let hash = window.location.hash;
                // console.log('currentPath='+currentPath);
                // console.log('selectedLang='+selectedLang);
                event.preventDefault();
                const isZh = currentPath.includes("/zh/");
				// console.log('isZh='+isZh);
				// console.log('hash='+hash);
				
                if (isZh) {//在中文网页
					if(selectedLang == "en"){
						window.location.href = currentPath.replace('/zh/', '/');
					}else{
						window.location.reload();
					}
                }else{//英文网页
                	if(selectedLang == "zh"){
                		window.location.href = currentPath.replace('/AndroidAOP/', '/AndroidAOP/zh/');
                	}else{
						window.location.reload();
                	}
                }
            }


        });
    });
});