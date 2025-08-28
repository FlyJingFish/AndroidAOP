document.addEventListener("DOMContentLoaded", function () {
    const svgElement = document.getElementById('svgAnimation');
    if(svgElement == null){
        return;
    }

    function isMobileDevice() {
        const userAgent = navigator.userAgent || navigator.vendor || window.opera;

        // 检查是否包含常见的移动设备标识符
        return /(phone|pad|pod|iPhone|iPod|ios|iPad|Android|Mobile|BlackBerry|IEMobile|MQQBrowser|JUC|Fennec|wOSBrowser|BrowserNG|WebOS|Symbian|Windows Phone)/i.test(userAgent);
    }
    var start = false;
    function startAnim() {
        stopAnim();
        start = true;
        const div = document.getElementById('logo_anim_div')
        div.classList.add('svg-anim-frame-animation');
    }
    function stopAnim() {
        start = false;
        const div = document.getElementById('logo_anim_div')
        div.classList.remove("svg-anim-frame-animation");
        div.classList.remove("svg-anim-frame-animation1");
        div.classList.add("svg-anim-frame-animation-stop");
    }
    window.addEventListener('load', function() {



        if (isMobileDevice()) {
            svgElement.addEventListener('click', () => {
                if (start) {
                    stopAnim()
                }else{
                    startAnim()
                }
            });
        } else {
            // 鼠标悬停时，改变帧位置来播放动画
            svgElement.addEventListener('mouseenter', () => {
                if (!start) {
                    startAnim()
                }
            });

            // 鼠标移开时，回到初始帧
            svgElement.addEventListener('mouseleave', () => {
                if (start) {
                    stopAnim()
                }
            });
        }
        start = true;
        const div = document.getElementById('logo_anim_div')
        div.classList.add('svg-anim-frame-animation1');
        div.addEventListener('animationend', function(event) {
            start = false;
            div.removeEventListener('animationend', this);
        }, false);
    });
});