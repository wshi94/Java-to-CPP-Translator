package edu.nyu.oop;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import xtc.tree.GNode;


/**
 * Created by Huynh on 12/8/15.
 */
public class ClassInheritanceHierarchyTest {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(CppFilePrinter.class);
    xtc.util.Runtime runtime = XtcTestUtils.newRuntime();

    @BeforeClass
    public static void beforeClass() {
        logger.debug("Executing Class Inheritance Hierarchy Test");
    }

    @Test
    public void testCppFilePrinter() {
        logger.debug("Begin testing the Inheritance hierarchy");

        GNode root = (GNode) XtcTestUtils.loadTestFile("src/test/java/inputs/test010/Test010.java");
        ClassInheritanceHierarchy cih = new ClassInheritanceHierarchy(root);

        assert (cih.getSuperClass("A") == null);
        assert (cih.getSuperClass("B1").equals("A"));
        assert (cih.getSuperClass("B2").equals("A"));
        assert (cih.getSuperClass("C").equals("B1"));

        logger.debug("End of Inheritance hierarchy test");
    }


}
