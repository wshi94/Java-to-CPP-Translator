package edu.nyu.oop;

import java.util.ArrayList;
import java.util.List;

import edu.nyu.oop.util.JavaFiveImportParser;

import org.slf4j.Logger;
import xtc.lang.JavaAstSimplifier;
import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.util.Runtime;
import xtc.util.SymbolTable;

public class Translator {

    private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    // Class members
    private GNode srcNode;                  // Stores the Source file Ast
    private GNode mutatedSrcNode;           // Stores the Mutated ast
    private GNode inheritanceNode;          // Stores the inheritance ast
    List <GNode> dependencyNode;            // Stores the Ast of all dependencies used by the source file
    private Runtime runtime;                // Runtime

    /**
     * Phase 1 of translation,
     * Input : Java source files
     * Output: AST of java source files and dependencies
     *
     * This part should take any java source file and generate an AST
     * for it and all of its dependencies.
     *
     * @param n The Ast root node
     */
    // Constructor
    public Translator(GNode n, Runtime runtime) {

        // Initialize
        this.srcNode = n;
        this.mutatedSrcNode = n; // TODO : we need to make a deep copy of n
        this.runtime = runtime;

        // Proccess all dependencies into asts
        dependencyNode = new ArrayList<GNode>();
        recursiveDependency(n);
    }

    /**
     * Parses the dependency java source files into the Ast
     * @param n The Ast root node
     */
    private void recursiveDependency(GNode n) {

        for(GNode g : JavaFiveImportParser.parse(n)) {
            if(dependencyNode.contains(g)) {
                return;
            }
            dependencyNode.add(g);

            if (!JavaFiveImportParser.parse(g).isEmpty()) {

                recursiveDependency(g);
            }
        }

    }

    /**
     * Phase 2 of translation,
     * Input: Java Ast from phase 1
     * Output: C++ inheritance hierarchy ast
     *
     * This part should take the java ast and create a new ast
     * that will represent the inheritance hierarchy in c++ that
     * will be used for phase 3
     */
    public GNode generateInheritanceAst() {
        AstGenerate astGenerate = new AstGenerate((GNode) srcNode, dependencyNode);
        inheritanceNode = astGenerate.getTree();
        return inheritanceNode;
    }

    /**
     * Phase 3 of translation,
     * Input: C++ inheritance hierarchy ast
     * Output: C++ header file
     *
     * This part should take the inheritance ast from phase 2
     * and print out the header file "output.h"
     */
    public void printHeaderFile() {
        //generateInheritanceAst();
        CppHeaderMaker cppHeaderMaker = new CppHeaderMaker(inheritanceNode, srcNode);
        cppHeaderMaker.print();
    }

    /**
     * Phase 4 of translation,
     * Input: Java Ast from phase 1
     * Output: C++ Ast mutated from java Ast
     *
     * This part should take the java ast and mutate and decorate it
     * to represent the C++ ast that will be used for phase 5
     */
    public void mutateAst() {

        // mutate ast
        AstMutator astMutator = new AstMutator(mutatedSrcNode);
        mutatedSrcNode = astMutator.getMutatedAst();
    }

    /**
     * Phase 5 of translation,
     * Input mutated c++ Ast from phase 4
     * Output: C++ implementation file
     *
     * This part should take the mutated c++ ast from phase 4
     * and use it to print the implementation file "output.cpp"
     */
    public void printCppFile() {
        CppFilePrinter cppFilePrinter = new CppFilePrinter(mutatedSrcNode, this.runtime);
        cppFilePrinter.print();
    }

    /**
     * Prints translation of the main.cpp file
     */
    public void printMainFile() {
        CppMainPrinter cppMainPrinter = new CppMainPrinter(mutatedSrcNode, this.runtime);
        cppMainPrinter.print();
    }

    /**
     * Translates Java to C++ and prints the 3 files
     * output.h, output.cpp, main.cpp
     */
    public void translate() {

        // Phase 2
        generateInheritanceAst();

        // Phase 3
        printHeaderFile();

        // Phase 4
        mutateAst();

        // Phase 5
        printCppFile();
        printMainFile();
    }
}