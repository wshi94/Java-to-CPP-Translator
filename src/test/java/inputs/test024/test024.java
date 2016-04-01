package inputs.test024;

import inputs.testClasses.exampleClass2;

/**
 * Created by evanjohnson on 10/18/15.
 * exmapleClass2 creates a new exampleClass so this test is to test if when printing the dependent AST's, the recursive-ness works.
 */
public class test024 {
    public static void main(String[] args) {
        exampleClass2 c2 = new exampleClass2(1);
        System.out.println(c2.toString());
    }

}
