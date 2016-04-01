package edu.nyu.oop;

import org.slf4j.Logger;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;
import xtc.util.Runtime;
import xtc.tree.Location;

import java.util.List;
import java.util.ArrayList;

/**
 * Homework 2 -
 * Here you will author methods that will visit each scope in the target
 * class and collect the location of the start of each scope.
 *
 * Hint, you can get the Location of the start of any node like so:
 *    node.getLocation()
 * Hint, you can print the location of a node easily like so:
 *    runtime.console().p("Entering scope at ").loc(node).pln();
 * Hint, some child nodes
 */
public class ScopeVisitor extends Visitor {

    private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());


    private Runtime runtime;

    // The "state collector", add to this as you traverse.
    private ScopeSummary summary = new ScopeSummary();

    public ScopeVisitor(Runtime runtime) {
        this.runtime = runtime;
    }

    public void visit(Node n) {
        for (Object o : n) {
            if (o instanceof Node) dispatch((Node) o);
        }
    }


    public void visitCompilationUnit(GNode n) {
        visit(n);
    }

    //Visit class
    public void visitClassDeclaration(GNode n) {
        summary.count++;
        summary.addScope(n.getLocation());
        runtime.console().p("Entering scope at ").loc(n).pln().flush();
        visit(n);
    }

    //Visit methods in class
    public void visitMethodDeclaration(GNode n) {
        findblock(n);

    }
    //visit conditionals
    public void visitConditionalStatement(GNode n) {
        findblock(n);
    }
    //visit constructors
    public void visitConstructorDeclaration(GNode n) {
        findblock(n);
    }
    //visit blockdecs
    public void visitBlockDeclaration(GNode n) {
        findblock(n);
    }

    public ScopeSummary getSummary(GNode n) {
        super.dispatch(n);
        return summary;
    }

    // An instance of this class will be mutated as the Ast is traversed.
    static class ScopeSummary {

        private List<Location> scopes = new ArrayList<Location>(); //list of locations

        String nodes = "";
        int count = 0;
        public void addScope(Location l) {
            this.scopes.add(l);
        } //adds scope to list

        public List<Location> getScopes() {
            return this.scopes;
        } //returns elements of the list

        public String toString() {  //prints string rep of everything
            String str = "";
            for(Location l : scopes) {
                str += l.toString() + "\n";
            }
            return str;
        }
    }

    // Searches Ast for a node with specified name. Returns first that it finds.
    public static Node dfs(Node node, String nodeName) {
        if (node.size() == 0) {
            return null;
        } else if (node.hasName(nodeName)) {
            return node;
        } else {
            for (Object o : node) {
                if (o instanceof Node) {
                    Node casted = (Node) o;
                    Node target = dfs(casted, nodeName);
                    if (target != null) return target;
                }
            }
        }
        return null;
    }

    //if there is a block then there is a scope
    public void findblock (GNode n) {

        for(int i = 0 ; i < n.size() ; ++i) {
            if (n.get(i) != null) {
                if (!(n.get(i) instanceof String)) {
                    //logger.debug("Root node name is " + n.getName());
                    GNode child = n.getGeneric(i); //this is the problem
                    //logger.debug("Child " + i + "'s name is " + child.getName());
                    if (child.getName().equals("Block")) {
                        summary.count++; //increment every time you traverse a method
                        summary.addScope(n.getLocation()); //Stores location of the start of any node
                        summary.nodes += n.getName() + " ";
                        runtime.console().p("Entering scope at ").loc(n).pln().flush();
                        visit(n);
                        runtime.console().p("Exit scope at ").loc(n).pln().flush();
                    }
                }
            }
        }
    }
}

/*
For each static scope, store the Location information in the ScopeSummary instance.
Note that you are not required to print/collect the line numbers that the scope exited on.
how many scopes does it have?
what line does a particular scope begin on?
The idea here is programmatically demonstrate that your ScopeVisitor visits every scope properl
 */