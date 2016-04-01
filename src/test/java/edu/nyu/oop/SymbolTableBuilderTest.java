package edu.nyu.oop;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import xtc.lang.JavaAstSimplifier;
import xtc.tree.GNode;
import xtc.type.VariableT;
import xtc.util.Runtime;

import java.util.List;
import java.util.ArrayList;

import xtc.type.AliasT;

/**
 * Created by Huynh on 11/27/15.
 */
public class SymbolTableBuilderTest {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(Translator.class);

    @BeforeClass
    public static void beforeClass() {
        logger.debug("Executing SymbolTableBuilder Test");
    }

    @Test
    public void generateAndPrintSymbolTable() {
        logger.debug("Begin symbol table test");
        Runtime runtime = XtcTestUtils.newRuntime();

        GNode root = (GNode) XtcTestUtils.loadTestFile("src/test/java/inputs/test016/Test016.java");

        new JavaAstSimplifier().dispatch(root);

        // Symbol table
        SymbolTable symbolTable = new SymbolTable(root, runtime);

        // assertions for test 10
        /*

        String[] methodNames = {"setA", "printOther", "toString"};
        ArrayList<List<VariableT>> methodParams = new ArrayList<>();

        for (int i = 0; i < methodNames.length; i++) {

            List<VariableT> params = symbolTable.getFormalParameters(methodNames[i]);
            methodParams.add(params);
        }

        // Expected parameter types
        List<VariableT> setA = new ArrayList<VariableT>();
        setA.add(VariableT.newParam(new AliasT("String", null), "x"));

        List<VariableT> printOther = new ArrayList<VariableT>();
        printOther.add(VariableT.newParam(new AliasT("A", null), "other"));

        List<VariableT> toString = new ArrayList<VariableT>();


        VariableT actualParam = setA.get(0);
        VariableT gottenParam = methodParams.get(0).get(0);
        assert (actualParam.hasName(gottenParam.getName())); // same name
        assert (methodParams.get(0).size() == 1);    // 1 param

        actualParam = printOther.get(0);
        gottenParam = methodParams.get(1).get(0);
        assert (actualParam.hasName(gottenParam.getName()));
        assert (methodParams.get(1).size() == 1);

        // toString should have no params
        assert (methodParams.get(2).size() == 0);
        */
        symbolTable.printSimple();

        logger.debug("End of symbol table test");
    }
}
