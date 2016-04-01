package edu.nyu.oop;

import org.junit.Test;
import org.slf4j.Logger;
import xtc.tree.GNode;

/**
 * Created by Huynh on 10/22/15.
 */
public class NodeMutationTest {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(NodeTransformationExample.class);

    @Test
    public void testPrintMutation() {
        logger.debug("Begin testing ast mutation");

        GNode root = (GNode) XtcTestUtils.loadTestFile("src/test/java/inputs/test016/Test016.java");

        logger.debug("Before mutation...");
        XtcTestUtils.prettyPrintAst(root);

        // Mutate Ast
        AstMutator astMutator = new AstMutator(root);

        logger.debug("After mutation...");
        XtcTestUtils.prettyPrintAst(astMutator.getMutatedAst());

    }
}
