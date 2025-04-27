## Brief description

```java
@AndroidAopModifyExtendsClass(
    value = "Modify target class",
    isParent = false // value refers to the class name or the inherited class of the class
    excludeWeaving = exclude weaving range
    includeWeaving = include weaving range
)
```

- `isParent = true` means modifying all classes whose inherited classes are value
- `isParent = false` means modifying the class whose class name is value
- excludeWeaving and includeWeaving are similar to exclude and include in the [getting started](/AndroidAOP/getting_started/#4-add-the-androidaopconfig-configuration-item-in-apps-buildgradle-this-step-is-an-optional-configuration-item)

This function is relatively simple. It modifies the inherited class of a class. Fill in the full name of the class to be modified in the ```value``` position. The annotated class is the modified inherited class.

In addition, if the class name is an internal class, do not use the `$` character, but `.`


!!! note
    - **:warning::warning::warning:But it should be noted that the modified inherited class cannot inherit the modified class. The inherited class of the modified class is generally set to the inherited class of the class before modification**
    - **:warning::warning::warning:If the original inherited class has generic information, please note that the modified inherited class also needs to have the same generic information**
    - **When you modify the configuration of this aspect, in most cases you should clean the project and continue development**


## Usage example

### Example 1

- As shown in the following example, the inherited class of ```AppCompatImageView``` is replaced with ```ReplaceImageView```

- Because `isParent = false` is set, only the inherited class of ```AppCompatImageView``` is replaced

```java

@AndroidAopModifyExtendsClass(
        value = "androidx.appcompat.widget.AppCompatImageView",
        isParent = false
)
public class ReplaceImageView1 extends ImageView {
    public ReplaceImageView1(@NonNull Context context) {
        super(context);
    }

    public ReplaceImageView1(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ReplaceImageView1(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
//Do some monitoring or modify again
    }
}
```

!!! note
    The inherited class of the above `ReplaceImageView1` cannot be `AppCompatImageView`, so after the change it becomes `AppCompatImageView` --> `ReplaceImageView1` --> `AppCompatImageView`

### Example 2

- As shown in the following example, all classes whose parent class is ```AppCompatImageView``` need to be replaced with ```ReplaceImageView```

- Because `isParent = true` is set, there may be more than one class that inherits from ```AppCompatImageView```, and all of their inherited classes will be replaced

```java
@AndroidAopModifyExtendsClass(
value = "androidx.appcompat.widget.AppCompatImageView",
isParent = true
)
public class ReplaceImageView2 extends ImageView {
    public ReplaceImageView2(@NonNull Context context) {
        super(context);
    }

    public ReplaceImageView2(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ReplaceImageView2(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
//Do some monitoring or modify again
    }
}
```

!!! note
    The inherited class of the above `ReplaceImageView2` can be `AppCompatImageView`. After this change, the original `A` --> `AppCompatImageView` becomes `A` --> `ReplaceImageView2` --> `AppCompatImageView`

## Usage Inspiration

1. In the inheritance relationship of the class, you can modify the inherited class and rewrite some methods in the middle. In this way, you can process the original logic in the middle, which is also a monitoring of the calling of some methods of the object