package inputs.test025;

/**
 * Created by Will on 12/19/15.
 */
class A {
    public String toString() {
        return "A";
    }
}

public class Test025 {
    public static void main(String[] args) {
        int[] ints = new int[2];
        System.out.println(ints[1]);

        float[] floats = new float[2];
        System.out.println(floats[1]);

        A[] array = new A[5];

        for (int i = 0; i < array.length; ++i) {
            A a = new A();
            array[i] = a;
        }

        for (int i = 0; i < array.length; ++i) {
            A a = array[i];
            System.out.println(a.toString());
        }

        try {
            System.out.println(array[128]);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Caught ArrayIndexOutOfBoundsException");
        }

        try {
            Object[] o = array;
            o[2] = new Object();
        } catch (ArrayStoreException e) {
            System.out.println("Caught ArrayStoreException");
        }
    }
}

