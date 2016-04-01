package edu.nyu.oop;

import edu.nyu.oop.util.NodeUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.util.*;

import java.util.List;

/**
 * Created by Huynh on 10/27/15.
 */
public class CppMainPrinterTest {
    // TODO test the main method node extraction
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(CppMainPrinter.class);

    @BeforeClass
    public static void beforeClass() {
        logger.debug("Executing CppImplementation");
    }

    @Test
    public void testCppMainPrinter() {
        logger.debug("Begin testing cpp main file printer");

        GNode root = (GNode) XtcTestUtils.loadTestFile("/Users/Huynh/Idea/translator-sea-lions/src/test/java/inputs/test010/Test010.java");
        xtc.util.Runtime runtime = XtcTestUtils.newRuntime();
        // Create the java ast mutator
        AstMutator astMutator = new AstMutator(root);

        // Get mutated ast
        GNode mutatedRoot = astMutator.getMutatedAst();

        // Create the main method printer
        CppMainPrinter cppMainPrinter = new CppMainPrinter(mutatedRoot, runtime);

        // Get main method node
        GNode mainMethodNode = cppMainPrinter.getMain();

        // Get the cout statement
        GNode mainMethodBlock = (GNode) mainMethodNode.get(7);
        GNode expressionStatement = (GNode) mainMethodBlock.get(0);
        GNode callExpression = (GNode) expressionStatement.get(0);
        GNode arguments = (GNode) callExpression.get(3);

        // Test the translation of println to cout
        String correctString = "cout << \"Hello.\" << endl;";
        String translatedString = cppMainPrinter.cout(arguments);

        logger.debug(translatedString);
        assert (correctString.equals(translatedString));

        logger.debug("End of cpp main printer test");
    }
}
