## 简述

```@AndroidAopModifyExtendsClass(value)```

这个功能比较简单，修改类的继承类，```value``` 位置填写要修改的类的全名，被注解的类就是修改后的继承类。

另外填写类名如果是内部类时不使用`$`字符，而是用`.`

**⚠️⚠️⚠️但需要特别注意的是修改后的继承类不可以继承被修改的类，修改后的类的继承类一般都设置为修改前的类的继承类**

## 使用示例

如下例所示，就是要把 ```AppCompatImageView``` 的继承类替换成 ```ReplaceImageView```

应用场景：非侵入式地实现监控大图加载的功能

```java
@AndroidAopModifyExtendsClass("androidx.appcompat.widget.AppCompatImageView")
public class ReplaceImageView extends ImageView {
    public ReplaceImageView(@NonNull Context context) {
        super(context);
    }
    public ReplaceImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ReplaceImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
        //做一些监测或者再次修改
    }
}
```

## 使用启示

1、在类的继承关系中修改继承类可以在中间重写一些方法，如此一来可以在中间处理一下原有逻辑，也是对对象的某些方法被调用的监测

