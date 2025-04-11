package com.flyjingfish.androidaop.testReplace;

public class BeanH extends BaseBean{

    public BeanH(BeanH o,int num1, int num2) {
        super(num1, num2);
    }

    public BeanH(int num1, int num2) {
        super(num1, num2);
    }

    @Override
    public void test() {
        super.test();
    }
}
