package edu.nyu.oop;

import edu.nyu.oop.util.NodeUtil;
import edu.nyu.oop.util.SymbolTableUtil;
import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.type.VariableT;
import xtc.util.Runtime;
import xtc.util.SymbolTable.Scope;

import java.util.List;

/**
 * Wrapper class for xtc.util.SymbolTable
 */
public class SymbolTable {

    private xtc.util.SymbolTable instance;  // symbol table
    private xtc.util.Runtime runtime;       // runtime
    private GNode root;                     // root of ast used to build symbol table

    /**
     * SymbolTable constructor
     * @param root Root GNode of an AST
     * @param runtime xtc runtime
     */
    SymbolTable(GNode root, Runtime runtime) {

        // initialize member variables
        instance = new xtc.util.SymbolTable();
        this.runtime = runtime;
        this.root = root;

        // create the symbol table
        new SymbolTableBuilder(this.runtime, instance).dispatch(root);
    }

    /**
     * Gets the symbol table instance
     * @return Symbol table instance
     */
    public xtc.util.SymbolTable getInstance() {
        return instance;
    }

    /**
     * Enters the scope of node n in the symbol table
     * @param n Node scope that will be entered
     */
    public void enterScope(Node n) {
        SymbolTableUtil.enterScope(instance, n);
    }

    /**
     * Exits the scope of node n in the symbol table
     * @param n Node scope that will be exited from
     */
    public void exitScope(Node n) {
        SymbolTableUtil.exitScope(instance, n);
    }

    /**
     * Set scope to the root
     */
    public void setScopeToRoot() {
        instance.setScope(instance.root());
    }

    /**
     * Set scope to specified scope
     * @param scope The scope symbol table will be set to
     */
    public void setScope(Scope scope) {
        instance.setScope(scope);
    }

    /**
     * Return current scope
     * @return Symbol table's current scope
     */
    public Scope current() {
        return instance.current();
    }

    /**
     * Prints the simple symbol table
     */
    public void printSimple() {
        new SymbolTablePrinter(runtime, instance).simple();
    }

    /**
     * Prints the full symbol table
     */
    public void printFull() {
        new SymbolTablePrinter(runtime, instance).full();
    }

    /**
     * A helper function for retrieving formal parameters of a method given the method
     * name.
     * @param method The method name
     * @return A list of the method's formal parameters otherwise null
     */
    public List<VariableT> getFormalParameters(String method) {

        // local variables
        GNode methodNode = null;
        Scope originalScope = instance.current();
        Scope methodScope;

        // Get all methods in the program
        List<Node> methods = NodeUtil.dfsAll(root, "MethodDeclaration");

        // for every method
        for (Node node : methods) {

            // only process the methods with the method name we specified
            if (node.get(3).equals(method)) {

                // get method node
                methodNode = (GNode) node;
            }
        }

        // enter the scope of the method if i t exists
        if(null != methodNode) {
            enterScope(methodNode);
            methodScope = instance.current();

            // return to original scope so that we do not mutate state
            instance.setScope(originalScope);

            // get formal parameters
            return SymbolTableUtil.extractFormalParams(methodScope);
        }

        else {
            return null;
        }
    }
}