document.addEventListener('DOMContentLoaded', function () {
    var targetElement = document.querySelector('.md-header__title');
    if (targetElement) {
        var div = document.createElement('div');
        div.classList.add('md-header__option');
        var label = document.createElement('label');
        label.classList.add('md-header__button','md-icon');


        // 创建 img 元素，指向外部的 SVG 文件
        var img = document.createElement('img');
        img.src = '../svg/star.svg';
        img.alt = 'Icon';
        label.appendChild(img);
        div.appendChild(label);
        div.addEventListener('click', function() {
            window.location.href = "https://github.com/FlyJingFish/AndroidAOP";
        });
        var label1 = document.createElement('label');
        label1.innerText = "Star";
        label1.classList.add('star-center');
        label.appendChild(label1);
//        label1.classList.add('md-header__button','md-icon');
        targetElement.insertAdjacentElement('afterend', div);
    }
});
