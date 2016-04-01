package edu.nyu.oop;

import edu.nyu.oop.util.NodeUtil;
import xtc.tree.*;

import java.util.List;
import java.util.regex.*;


/**
 * Class Visitor.java
 * Visits each class declaration and handles Ast mutation
 * for class delaration nodes
 */

// Class Visitor
public class ClassVisitor extends Visitor {

    // Fields
    private GNode mutatedClassAst;  // Ast that will be mutated
    private GNode classDeclarationRoot;
    private String className;

    // Visitor methods

    public void visitClassDeclaration(GNode n) {

        // set the class name
        className = n.getString(1);
        visit(n);
    }

    public void visitClassBody(GNode n) {
        visit(n);
    }

    public void visitMethodDeclaration(GNode n) {

        String methodName = n.getString(3);

        //  If method is a constructor
        if(methodName.equals(className)) {

            // change the method's name to __init
            n.set(3, "__init");
        }

        visit(n);
    }

    public void visitBlock(GNode n) {
        visit(n);
    }

    public void visitExpressionStatement(GNode n) {
        visit(n);
    }

    public void visitCallExpression(GNode n) {

        if (null != n.get(0)) {
            // Checking for System.out.println statement
            if (((GNode) n.get(0)).hasName("SelectionExpression")) {
                GNode selectionExpression = (GNode) n.get(0);
                GNode primaryIdentifier = (GNode) selectionExpression.get(0);

                // Check for System and out
                if (primaryIdentifier.get(0).equals("System") && selectionExpression.get(1).equals("out")) {

                    // check for println
                    if (n.get(2).equals("println")) {
                        n.set(0, null);                 // No selectionExpression
                        n.set(2, "cout");               // Change println to cout

                        GNode arguments = (GNode) n.get(3);                 // Get arguments node
                        arguments = replaceWithVarNode(3, arguments, n);    // Replace with a var size node


                        for (int i = 0; i < arguments.size(); i++) {

                            // Try getting an argument GNode
                            try {
                                GNode argument = arguments.getGeneric(i);

                                /*
                                TODO
                                    make so that "this" is put in as the first argument in the method being called

                                    i.e. toString(__this) instead of just toString

                                    refer to Test009

                                    if done, remove the hardcoded __this in CppMainPrinter
                                 */

                                // TODO: add more checks here for arguments such as integer literal
                                // Translate something like System.out.println (1 + 1)
                                // Should print 2 instead of "1 + 1";
                                // Check for literal string
                                //if (argument.hasName("StringLiteral")) {

                            } catch (Exception e) {
                                // It is not a generic so we just print concatenate it normally
                            }
                        }

                        arguments.add("endl");                // Print new line
                    }
                }
            } else if (n.getNode(0).hasName("PrimaryIdentifier")) {
                //set the "this" from primary identifier
                String __this = n.getNode(0).getString(0);

                //add implicit "this" as the first argument of any method
                //don't add it for static methods
                GNode newArguments = GNode.create("Arguments"); //if you add in the create method, it becomes fixed size node


                List<Node> classes = NodeUtil.dfsAll(classDeclarationRoot, "ClassDeclaration");     //all class declarations
                boolean isStatic = false;                                                           //boolean to keep track of static

                for (Node m : classes) {
                    //if the object is a class name, we're doing a static call
                    if (m.getString(1).equals(__this)) {
                        isStatic = true;
                    }
                    //System.out.println(__this + " " + className);
                }

                //only add implicit this if it's not static
                if (!isStatic) {
                    newArguments.add(__this);
                }


                GNode oldArguments = (GNode) n.getNode(3);

                //then add all the normal arguments onto the new node
                for (int i = 0; i < oldArguments.size(); i++) {
                    newArguments.add(oldArguments.get(i));
                }

                //finally set the new node in the position of the old one
                n.set(3, newArguments);
                //System.out.println(newArguments);

            }

            //Input 28 issue here. toString is missing argument.
            else if (n.getNode(0).hasName("CallExpression")) {

                //Pull out some data from the ast
                String as = "";
                try{
                    as = n.getNode(0).getNode(0).getString(0);
                }
                catch(Exception e){
                    System.out.println("Expected String and String wasnt found - in visitCallExpression in ClassVisitor");
                }
                String getClass = n.getNode(0).getString(2);

                //Concatonate shit
                String toStringArgument = as + "->__vptr->" + getClass + "(" + as + ")";

                //Create new GNode
                GNode newArguments = GNode.create("Arguments");
                //Add new stuff to node
                newArguments.add(toStringArgument);

                //Replace node in AST to the new node
                n.set(3, newArguments);

            } else if (n.getNode(0).hasName("CastExpression")) {
                String castType = n.getGeneric(0).getGeneric(0).getGeneric(0).getString(0);
                String castee = "";

                //an array is being cast
                if (n.getGeneric(0).getGeneric(1).hasName("SubscriptExpression")) {
                    GNode subExp = n.getGeneric(0).getGeneric(1);

                    castee = subExp.getGeneric(0).getString(0) + "->__data[" + subExp.getGeneric(1).getString(0) + "]";
                } else {
                    castee = "we found a new castee in class visitor";
                }

                String neededArgument = "((" + castType + ") " + castee + ")";


                //Create new GNode
                GNode newArguments = GNode.create("Arguments");
                //Add new stuff to node
                newArguments.add(neededArgument);

                //Replace node in AST to the new node
                n.set(3, newArguments);
            } else {
                System.out.println("Found a new node type in ClassVisitor.java");
            }
        } else {
            if (n.getString(2).equals("super")) {
                GNode castExpression = GNode.create("CastExpression");

                Node extension = NodeUtil.dfs(classDeclarationRoot, "Extension");

                String parent = extension.getGeneric(0).getGeneric(0).getString(0);

                GNode QI = GNode.create("QualifiedIdentifier", parent);
                GNode type = GNode.create("Type");
                type.add(QI);
                type.add(null);

                GNode PI = GNode.create("PrimaryIdentifier");
                PI.add("__this");

                castExpression.add(type);
                castExpression.add(PI);

                n.set(0, castExpression);

                n.set(2, "__init");





                String castType = n.getGeneric(0).getGeneric(0).getGeneric(0).getString(0);
                String castee = "";

                //an array is being cast
                if (n.getGeneric(0).getGeneric(1).hasName("SubscriptExpression")) {
                    GNode subExp = n.getGeneric(0).getGeneric(1);

                    castee = subExp.getGeneric(0).getString(0) + "->__data[" + subExp.getGeneric(1).getString(0) + "]";
                } else if (n.getGeneric(0).getGeneric(1).hasName("PrimaryIdentifier")) {
                    castee = n.getGeneric(0).getGeneric(1).getString(0);
                } else {
                    castee = "we found a new castee in class visitor";
                }

                String neededArgument = "((" + castType + ") " + castee + ")";


                //Create new GNode
                GNode newArguments = GNode.create("Arguments");
                //Add new stuff to node
                newArguments.add(neededArgument);

                for (int i = 0; i < n.getGeneric(3).size(); i++) {
                    newArguments.add(n.getGeneric(3).get(i));
                }

                //Replace node in AST to the new node
                n.set(3, newArguments);

                //System.out.println("here");
            }
        }

        visit(n);
    }

    public void visitArguments(GNode n) {
        visit(n);
    }

    public void visit(Node n) {
        for(Object o : n) {
            if( o instanceof Node) dispatch( (Node) o);
        }
    }

    /**
     * Calls dynamic dispatch and returns the fully mutated
     * tree for a class declaration node
     * @param classDeclarationRoot ClassDeclaration GNode
     * @return The mutated ast with ClassDeclaration mutations
     */
    public GNode getMutatedClassAst(GNode classDeclarationRoot) {
        this.classDeclarationRoot = classDeclarationRoot;
        super.dispatch(classDeclarationRoot);
        return mutatedClassAst;
    }

    /**
     * Takes a GNode and removes all of the children.
     * @param parent The parent node
     */
    private void removeAllChildren(GNode parent) {
        int numChildren = parent.size();

        for(int i = numChildren - 1; i > 0; i--) {
            parent.remove(i);
        }
    }

    /**
     * Takes a GNode and returns a variable size GNode and replaces
     * the current node at the index specified of the parent node with
     * the variable size child node.
     *
     * @param index The index of the parent node the child will be set to
     * @param child Child node
     * @param parent Parent node
     * @return A variable size GNode
     */
    private GNode replaceWithVarNode(int index, GNode child, GNode parent) {
        if(!child.hasVariable()) {
            child = GNode.ensureVariable(child);    // turn into a variable sized node
            parent.set(index, child);               // replace the node in the ast
        }

        return child;

    }
}