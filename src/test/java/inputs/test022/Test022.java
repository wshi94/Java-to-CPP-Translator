package inputs.test022;

/**
 * Created by Will on 10/17/15.
 */

/**
 * tests multiple parameters for the constructor
 */

class A {
    private String myToString() {
        return "A";
    }

    public A(String name, String parameters) {}
}

class B {

}

public class Test022 {
    public static void main(String[] args) {
        A a = new A("hi", "2");
    }
}
