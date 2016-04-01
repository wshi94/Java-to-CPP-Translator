package inputs.test023;

import inputs.testClasses.exampleClass;
import inputs.testClasses.exampleClass2;

/**
 * Created by evanjohnson on 10/18/15.
 * Tests that 2 dependent files AST's will be printed
 */
public class test023 {
    public static void main(String[] args) {
        exampleClass c1 = new exampleClass(1);
        System.out.println(c1.toString());
        exampleClass2 c2 = new exampleClass2(1);
        System.out.println(c2.toString());
    }
}
