package inputs.testClasses;

/**
 * Created by evanjohnson on 10/18/15. Based on Huynh's design of exampleClass
 * Used to check that a dependent file of a dependent file will be found when printing phase1 AST's
 */
public class exampleClass2 {
    Integer number;

    public exampleClass2(int i) {
        number = i;
        exampleClass c = new exampleClass(2);
        System.out.println(c.toString());
    }

    public String toString() {
        return number.toString();
    }
}
