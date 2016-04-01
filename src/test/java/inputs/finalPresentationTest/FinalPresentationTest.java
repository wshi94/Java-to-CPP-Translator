package inputs.finalPresentationTest;

/**
 * Created by evanjohnson on 12/21/15.
 */
class A {
    String a;

    public void setA(String x) {
        a = x;
    }

    public void printOther(A other) {
        System.out.println(other.myToString());
    }

    public String myToString() {
        return a;
    }
}

class B1 extends A {
    String b;
}

class B2 extends A {
    String b;
}

class C extends B1 {
    String c;

    public String myToString() {
        return "still C";
    }
}

class ALoop {
    int i;

    public ALoop(int i) {
        this.i = i;
    }

    public int get() {
        return i;
    }
}

class BLoop extends ALoop {
    public BLoop(int i) {
        super(i);
    }

    public int get() {
        return (10 - i);
    }
}

public class FinalPresentationTest {
    public static void main(String[] args) {

        //we support Hello World!
        System.out.println("Hello world!");

        //inheritance/overriding example
        A a = new A();
        a.setA("A");
        B1 b1 = new B1();
        b1.setA("B1");
        B2 b2 = new B2();
        b2.setA("B2");
        C c = new C();
        c.setA("C");
        a.printOther(a);
        a.printOther(b1);
        a.printOther(b2);
        a.printOther(c);

        //array and loop example
        Object[] as = new ALoop[10];

        for (int i = 0; i < as.length; i++) {
            as[i] = new BLoop(i);
        }

        int k = 0;
        while (k < 10) {
            System.out.println(((ALoop) as[k]).get());
            k = k + 1;
        }


    }
}
