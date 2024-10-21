## Brief description

```@AndroidAopModifyExtendsClass(value)```

This function is relatively simple. It modifies the inherited class of a class. Fill in the full name of the class to be modified in the ```value``` position. The annotated class is the modified inherited class.

In addition, if the class name is an internal class, do not use the `$` character, but `.`

**:warning::warning::warning:But it should be noted that the modified inherited class cannot inherit the modified class. The inherited class of the modified class is generally set to the inherited class of the class before modification**

## Usage example

As shown in the following example, the inherited class of ```AppCompatImageView``` is replaced with ```ReplaceImageView```

Application scenario: non-invasively implement the function of monitoring large image loading

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
//Do some monitoring or modify again
    }
}
```

## Usage Inspiration

1. In the inheritance relationship of the class, you can modify the inherited class and rewrite some methods in the middle. In this way, you can process the original logic in the middle, which is also a monitoring of the calling of some methods of the object