package edu.nyu.oop;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import xtc.tree.GNode;
import xtc.util.Runtime;

/**
 * Created by Huynh on 10/28/15.
 */
public class TranslatorTest {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(CppFilePrinter.class);
    Runtime runtime = XtcTestUtils.newRuntime();

    @BeforeClass
    public static void beforeClass() {
        logger.debug("Executing Translator Test");
    }

    @Test
    public void testCppFilePrinter() {
        logger.debug("Begin testing cpp file printer");

        GNode root = (GNode) XtcTestUtils.loadTestFile("src/test/java/inputs/finalPresentationTest/FinalPresentationTest.java");
        Translator translator = new Translator(root, runtime);
        translator.translate();

        logger.debug("End of cpp file printer test");
    }
}
