package edu.nyu.oop;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import xtc.tree.GNode;
import xtc.tree.Node;

import java.util.List;

import edu.nyu.oop.util.*;
import edu.nyu.oop.AstUtil;

/**
 * Created by Huynh on 10/25/15.
 */
public class CppFilePrinterTest {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(CppFilePrinter.class);
    xtc.util.Runtime runtime = XtcTestUtils.newRuntime();

    @BeforeClass
    public static void beforeClass() {
        logger.debug("Executing CppFilePrinterTest");
    }

    @Test
    public void testCppFilePrinter() {
        logger.debug("Begin testing cpp file printer");

        GNode root = (GNode) XtcTestUtils.loadTestFile("src/test/java/inputs/test000/Test000.java");

        CppFilePrinter cppPrinter = new CppFilePrinter(root, runtime);
        cppPrinter.print();

        logger.debug("End of cpp file printer test");
    }

    @Test
    public void testMainFinder() {
        logger.debug("Begin Main function determination test");

        GNode root = (GNode) XtcTestUtils.loadTestFile("src/test/java/inputs/test006/Test006.java");
        CppFilePrinter cppFilePrinter = new CppFilePrinter(root, runtime);

        // Find main method
        List<Node> methods = NodeUtil.dfsAll(root, "ClassDeclaration");
        //assert (AstUtil.isMainClass((GNode) methods.get(0)));

        logger.debug("End of main class finder test");
    }
}
