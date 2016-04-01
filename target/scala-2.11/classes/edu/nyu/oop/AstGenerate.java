package edu.nyu.oop;

import java.util.List;
import java.util.ArrayList;
import java.io.*;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;

import edu.nyu.oop.util.*;

import org.slf4j.Logger;
import xtc.*;
import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;
import xtc.util.Runtime;
import xtc.tree.Location;
import xtc.lang.JavaPrinter;
import xtc.parser.ParseException;
import xtc.util.Tool;

import edu.nyu.oop.util.NodeUtil;
import edu.nyu.oop.Boot;
import edu.nyu.oop.util.JavaFiveImportParser;
import edu.nyu.oop.util.XtcProps;


/**
 * Phase 2 of translation.
 * Generates AST schema
 *
 *
 */

public class AstGenerate {

    //GNode objectHeader;
    private GNode header;
    private GNode object;

    //change the name of the node passed in to parent
    public AstGenerate(GNode parent, List<GNode> dependencyNode) {

        /**
         * We should first create the Object AST, add String and Class ASTs to it as nodes, and then
         * add the other classes to the Object AST as a node.
         */

        object = GNode.create(javalanggenerate.objectGenerate());
        object.addNode(javalanggenerate.stringGenerate());
        object.addNode(javalanggenerate.classGenerate());

        //hacky way to add the dependency nodes to the parent, reimplement if we have time
        for (GNode n : dependencyNode) {
            GNode dependency = GNode.create((GNode) n.getNode(1));
            parent.addNode(dependency);
        }

        new Visitor() {
            public void visitClassDeclaration(GNode classNode) {
                //prevent main class from getting added
                /*
                todo
                    code is operating under the assumption that main will always be the first method, should make it
                    more generic later on
                 */
                GNode findMain = (GNode) NodeUtil.dfs(classNode, "MethodDeclaration");
                if (findMain == null || !findMain.getString(3).equals("main")) {
                    //these get the name of the class
                    //at index 1 of the top CompilationUnit node is the node specifying the ClassDeclaration node
                    //GNode test = (GNode) parent.getNode(1);
                    //at index 1 of the ClassDeclaration node is the name of the class
                    String className = classNode.getString(1);

                    //should probably use another name besides header
                    header = GNode.create(className);

                    GNode headerDeclaration = GNode.create("HeaderDeclaration");

                    header.addNode(headerDeclaration);

                    //path to the input file (i.e. "inputs_test000")
                    //headerDeclaration.add(//path//);

                    //the name of the class
                    headerDeclaration.add(className);

                    //data layout
                    headerDeclaration.add(makeDataLayoutNode(classNode));

                    //vTable
                    headerDeclaration.add(makeVTableNode(classNode));

                    object.add(header);

                    visit(classNode);

                }
            }

            public void visit(Node n) {
                for (Object o : n) if (o instanceof Node) dispatch((Node) o);
            }
        } .dispatch(parent);
    }



    //should return a node with a list of children
    //Field, Constructor, and Method Declarations are traversed from the parent
    public GNode makeDataLayoutNode(GNode parent) {
        final GNode dataLayout = GNode.create("DataLayout");
        String parentClassName;     //name of the parent class (Object or "extends")
        GNode parentClass;          //node representing the parent class
        GNode parentDataLayout;     //data layout of the parent class to inherit from

        //index 3 of ClassDeclaration is the node that indicates whether the class "extends" another class
        if (parent.getNode(3) != null) {
            parentClassName = parent.getNode(3).getNode(0).getNode(0).getString(0);
        } else {
            parentClassName = "Object";
        }

        //set what the parent class will be for inheritance
        parentClass = GNode.create((GNode) NodeUtil.dfs(object, parentClassName));

        //get the parent class's data layout
        parentDataLayout = GNode.create((GNode) parentClass.getNode(0).getNode(1));

        //the list of inherited fields from the parent class
        final List<Node> inheritedFields = NodeUtil.dfsAll(parentDataLayout, "FieldDeclaration");

        //========== Initial Field Declarations ==========//
        //initialize
        GNode initField1 = GNode.create("FieldDeclaration");
        GNode initField2 = GNode.create("FieldDeclaration");

        GNode modifiers1 = GNode.create("Modifiers");
        GNode modifiers2 = GNode.create("Modifiers");

        String type1 = "__" + parent.getString(1) + "_VT*";
        String type2 = "__" + parent.getString(1) + "_VT";

        String name1 = "__vptr";
        String name2 = "__vtable";

        GNode declarators1 = GNode.create("Declarators");
        GNode declarators2 = GNode.create("Declarators");

        //this is an alternative way to do it
        /*GNode initField1 = GNode.create("FieldDeclaration",
                                          GNode.create("Modifiers"),//, "static"),
                                          "__" + parent.getString(1) + "_VT*",
                                          "__vptr",
                                          GNode.create("Declarators"));*/

        //modify
        modifiers2.add("static");

        //add
        initField1.addNode(modifiers1);
        initField1.add(type1);
        initField1.add(name1);
        initField1.addNode(declarators1);

        initField2.addNode(modifiers2);
        initField2.add(type2);
        initField2.add(name2);
        initField2.addNode(declarators2);

        //finish
        dataLayout.addNode(initField1);
        dataLayout.addNode(initField2);
        //========== End Init Field Declarations ==========//

        //adds inherited fields besides __vptr and __vtable and private fields to the data layout
        for (Node g : inheritedFields) {
            if (!(g.getString(2).equals("__vptr") ||
                    g.getString(2).equals("__vtable") ||
                    (g.getNode(0).size() > 0 && g.getNode(0).getString(0).equals("private")))) {  //need to check for size
                //otherwise out of bounds
                /*
                todo
                    private will always be at position 0???
                 */
                dataLayout.addNode(g);
            }
        }

        //this list should be traversed and .add() each GNode in the list to dataLayout
        //should include field declarations, constructor declarations, and method declarations
        final List<GNode> dataLayoutChildren = new LinkedList<GNode>();


        //this should visit each constructor/method declaration, parse it for the info we need, and
        //add a node with that information to the list of children
        new Visitor() {

            //*need the modifiers (at index 0 of FieldDeclaration is the node Modifiers which has children of all the modifiers.
            //  Each child has the name Modifier, and index 0 of Modifier is the string containing the modifier name)

            //*need the type of the field (at index 1 of FieldDeclaration is the node Type. At index 0 of this node is the
            //  QualifiedIdentifier node. At index 0 of this node is the string containing type)

            //*need name of the field (at index 2 of FieldDeclaration is a Declarators node. At index 0 is the Declarator child.
            //  At index 0 of the Declarator child is the string containing the name of the field.)

            //*need the declarators (not sure what this is ???) (at index 2 of FieldDeclaration is the Declarators node, which
            //  has a Declarator child. This child node has the name of the field at index 0, ===something???=== at index 1, and
            //  the node representing what the field is set to at index 2. For example, if String test = "x", the Declarator would have
            //  "test" at index 0 (the name), ===something???=== at index 1, and a node StringLiteral (representing the type) at
            //  index 2. The StringLiteral will have a string at index 0: "\"x\"")

            public void visitFieldDeclaration(GNode node) {
                int index = -1;     //set the default index to -1; change when there's overriding method
                for (Node n : inheritedFields) {
                    n = (GNode) n;      //need to cast to obtain GNode fields
                    //if the names of the fields are the same, remove the inherited field and record the index
                    if (n.getString(2).equals(node.getNode(2).getNode(0).getString(0))) {
                        index = dataLayout.indexOf(n);
                        dataLayout.remove(index);
                    }
                }

                GNode field = GNode.create("FieldDeclaration");

                //==== Modifiers ====//
                GNode modifiers = GNode.create("Modifiers");

                addModifiers(node, modifiers);

                /*
                                //sets the modifierTraverse node to the Modifiers node so we can go through each child Modifier
                                GNode modifierTraverse = (GNode) node.getNode(0);

                                //adds each string representing a modifier to the Modifiers node
                                for (int i = 0; i < modifierTraverse.size(); i++) {
                                    if (!(modifierTraverse.getNode(i).getString(0).equals("public") ||
                                            modifierTraverse.getNode(i).getString(0).equals("private"))) {
                                        modifiers.add(modifierTraverse.getNode(i).getString(0));
                                    }
                                }
                */

                field.add(modifiers);

                //==== Field Type ====//
                GNode type = (GNode) node.getNode(1);

                field.add(type.getNode(0).getString(0));

                //==== Field Name ====//
                GNode javaDeclarators = (GNode) node.getNode(2);

                field.add(javaDeclarators.getNode(0).getString(0));

                //==== Declarators ====//
                GNode declarators = GNode.create("Declarators");

                //if the node at index 2 is NOT null, then there is something that the field is being set to, so add
                //it to the declarators
                //can be StringLiteral, PrimaryIdentifier (this one is just "Object o = a", a being anything
                if (!(javaDeclarators.getNode(0).get(2) == null)) {
                    declarators.add(javaDeclarators.getNode(0).getNode(2));
                }

                field.add(declarators);

                //==== Finish ====//
                //an index >= 0 indicates it found a new implementation of an inherited field, so replace
                //the removed field with this new declaration
                if (index >= 0) {
                    //if there are modifiers, check if there is a private (in which case don't inherit
                    //if (field.getNode(0).size() > 0){
                    //    if (!field.getNode(0).getString(0).equals("private")){
                    dataLayout.add(index, field);
                    //    }
                    //}
                    //else {
                    //    dataLayoutChildren.add(field);
                    //}
                } else {
                    dataLayoutChildren.add(field);
                }

                /*
                if (!field.getNode(0).getString(0).equals("private")) {
                    //an index >= 0 indicates it found a new implementation of an inherited field, so replace
                    //the removed field with this new declaration
                    if (index >= 0) {
                        dataLayout.add(index, field);
                    } else {
                        dataLayoutChildren.add(field);
                    }
                }
                */

                //visit(node);
            }

            //*need the name (at index 2 of ConstructorDeclaration)

            //*need the parameters (FormalParameters is at index 3 of ConstructorDeclaration. FormalParameters has children
            //  called FormalParameter (no s). Each FormalParameter has properties on each parameter. Index 1 of FormalParameter
            //  is the Type node. Index 0 of the node is the QualifiedIdentifier node. The string at index 0 of this node is
            //  the string containing the type of the parameter, which is what we need)

            //take the name and add it as the first property of a new ConstructorDeclaration
            //add a new "Parameters()" node and add each parameter name as a property (in order) to this new node.
            public void visitConstructorDeclaration(GNode node) {
                GNode constructor = GNode.create("ConstructorDeclaration");

                //==== Constructor Name ====//
                constructor.add(node.getString(2));


                //==== Parameters ====//
                GNode parameters = GNode.create("Parameters");

                //need a paramTraverse to equal the FormalParameters node so that we can get all the parameters using a
                //for loop
                GNode paramTraverse = (GNode) node.getNode(3);

                //adds the string containing the type of each parameter to the Parameters node
                for (int i = 0; i < paramTraverse.size(); i++) {
                    String paramType = paramTraverse.getNode(i).getNode(1).getNode(0).getString(0);
                    String paramName = paramTraverse.getNode(i).getString(3);
                    parameters.add(paramType + " " + paramName);
                }

                constructor.add(parameters);

                //==== Finish ====//
                dataLayoutChildren.add(constructor);

                //visit(node);
            }

            //*need the modifiers (at index 0 of MethodDeclaration is Modifiers, which has children of all the modifiers)

            //*need the return type (at index 2 of MethodDeclaration is a node called Type or VoidType. If
            //  it is VoidType, then that means there is no return type (i.e. public void methodA). If it is Type,
            //  then there is a node at index 0 called QualifiedIdentifier. The string at index 0 of this QualifiedIdentifier
            //  node is the name of the return type)

            //*need the name of the method (at index 3 of MethodDeclaration)

            //*need implementing class of the method???

            //*need the parameters of the method (at index 4 is the node FormalParameters. Every child of FormalParameters
            //  is a node called FormalParameter that holds the information for a parameter. Index 1 for each of these
            //  FormalParameter nodes is the Type node. Index 0 of the Type node is the QualifiedIdentifier node. Index 0
            //  of the QualifiedIdentifier node is the string that contains the type, which is what we need)
            public void visitMethodDeclaration(GNode node) {
                GNode method = GNode.create("DLFunctionDeclaration");

                //==== Modifiers ====//
                GNode modifiers = GNode.create("Modifiers");

                addModifiers(node, modifiers);

                //modifiers.add("static");

                method.add(modifiers);

                //==== Return Type ====//
                GNode type = (GNode) node.getNode(2);

                //the type can be either VoidType or Type indicating void or return type that isn't void
                if (type.hasName("VoidType")) {
                    method.add(null);
                } else if (type.hasName("Type")) {
                    method.add(type.getNode(0).getString(0));
                }

                //==== Function Name ====//
                method.add(node.getString(3));

                //==== Owner Class ===//
                //not really needed in the data layout I think

                //==== Parameters ====//
                GNode parameters = GNode.create("Parameters");

                //need a paramTraverse to equal the FormalParameters node so that we can get all the parameters using a
                //for loop
                GNode paramTraverse = (GNode) node.getNode(4);

                for (int j = 0; j < method.getGeneric(0).size(); j++) {
                    if (!method.getGeneric(0).getString(j).equals("static")) {
                        //add implicit "this" class as first parameter for everything except static methods
                        parameters.add(header.getName());
                    }
                }

                //adds the string containing the type of each parameter to the Parameters node
                for (int i = 0; i < paramTraverse.size(); i++) {
                    String paramType = paramTraverse.getNode(i).getNode(1).getNode(0).getString(0);
                    String paramName = paramTraverse.getNode(i).getString(3);
                    parameters.add(paramType + " " + paramName);
                }

                method.add(parameters);

                //==== Finish ====//
                dataLayoutChildren.add(method);

                //visit(node);
            }

            private void addModifiers(GNode originalAst, GNode modifiers) {
                List <Node> modifierNodes = NodeUtil.dfsAll(originalAst, "Modifier");
                for (Node n : modifierNodes) {
                    modifiers.add(((GNode) n).getString(0));
                }
            }

            public void visit(Node n) {
                for (Object o : n) if (o instanceof Node) dispatch((Node) o);
            }

        } .dispatch(parent);

        for (GNode g : dataLayoutChildren) {
            dataLayout.add(g);
        }

        //if there was no explicit constructor declaration, add a no argument constructor to the class
        if (NodeUtil.dfs((Node) dataLayout, "ConstructorDeclaration") == null) {
            //hacky way of getting the index of the last field declaration, prob can fix up after midterm
            /*int lastFieldDeclaration = -1;
            for (GNode h : dataLayoutChildren){
                if (h.getName().equals("FieldDeclaration")){
                    lastFieldDeclaration = dataLayoutChildren.indexOf(h);
                }
            }*/

            GNode constructor = GNode.create("ConstructorDeclaration",
                                             header.getNode(0).getString(0),
                                             GNode.create("Parameters"));

            //add the constructor to the next index after the last field declaration
            dataLayout.add(constructor);
        }

        //create Class __class() function
        GNode __class = GNode.create("DLFunctionDeclaration",
                                     GNode.create("Modifiers"),
                                     "Class",
                                     "__class",
                                     GNode.create("Parameters"));

        dataLayout.add(__class);

        return dataLayout;
    }


    public GNode makeVTableNode(GNode parent) {
        final GNode vTable;// = GNode.create("VTable");
        final String thisClassName = parent.getString(1);
        final List<GNode> vTableChildren = new LinkedList<GNode>();

        String parentClassName;     //name of the parent class (Object or "extends")
        GNode parentClass;          //node representing the parent class
        GNode parentVTable;         //vTable of the parent class to inherit from

        //index 3 of ClassDeclaration is the node that indicates whether the class "extends" another class
        if (parent.getNode(3) != null) {
            parentClassName = parent.getNode(3).getNode(0).getNode(0).getString(0);
        } else {
            parentClassName = "Object";
        }

        //set what the parent class will be for inheritance
        parentClass = GNode.create((GNode) NodeUtil.dfs(object, parentClassName));

        //get the parent class's vTable
        parentVTable = GNode.create((GNode) parentClass.getNode(0).getNode(2));

        //set the child class's vTable to the parent class's
        vTable = GNode.create(parentVTable);


        new Visitor() {

            //list of nodes with inherited vtable function declarations
            List<Node> inheritedVTableFunctions = NodeUtil.dfsAll(vTable, "VTFunctionDeclaration");

            //*need modifiers (at index 0 of MethodDeclaration is Modifiers, which has children of all the modifiers)

            //*need the return type (at index 2 of MethodDeclaration is a node called Type or VoidType. If
            //  it is VoidType, then that means there is no return type (i.e. public void methodA). If it is Type,
            //  then there is a node at index 0 called QualifiedIdentifier. The string at index 0 of this QualifiedIdentifier
            //  node is the name of the return type)

            //*need the name of the method (at index 3 of MethodDeclaration)

            //*need implementing class of the method

            //*need the parameters of the method (at index 4 is the node FormalParameters. Every child of FormalParameters
            //  is a node called FormalParameter that holds the information for a parameter. Index 1 for each of these
            //  FormalParameter nodes is the Type node. Index 0 of the Type node is the QualifiedIdentifier node. Index 0
            //  of the QualifiedIdentifier node is the string that contains the type, which is what we need)

            public void visitMethodDeclaration(GNode node) {
                int index = -1;     //set the default index to -1; change when there's overriding method
                for (Node n : inheritedVTableFunctions) {
                    n = (GNode) n;      //need to cast to obtain GNode methods
                    //if the names of the methods are the same, remove the inherited method and record the index
                    if (n.getString(2).equals(node.getString(3))) {
                        index = vTable.indexOf(n);
                        //dont try to remove if index is -1
                        if(index != -1) {
                            vTable.remove(index);
                        }
                    }
                }

                //boolean to hold whether the method is static or private, in which case we don't inherit it
                boolean isStaticOrPrivate = false;

                //for every object in the Modifiers node
                for (Object o : node.getNode(0)) {

                    String modifier = ((GNode) o).getString(0);
                    if (modifier.equals("private") || modifier.equals("static")) {
                        isStaticOrPrivate = true;
                    }

                    //else if (modifier.equals(""))
                    //modifiers.add(((GNode) o).getString(0));
                }

                //if the method is not static or private, then we add it to the vtable
                if (!isStaticOrPrivate) {

                    GNode method = GNode.create("VTFunctionDeclaration");

                    //==== Modifiers ====//
                    GNode modifiers = GNode.create("Modifiers");

                    for (Object o : node.getNode(0)) {
                        modifiers.add(((GNode) o).getString(0));
                    }

                    method.add(modifiers);

                    //==== Return Type ====//
                    GNode type = (GNode) node.getNode(2);

                    //the type can be either VoidType or Type
                    if (type.hasName("VoidType")) {
                        method.add(null);
                    } else if (type.hasName("Type")) {
                        method.add(type.getNode(0).getString(0));
                    }

                    //==== Function Name ====//
                    method.add(node.getString(3));

                    //==== Owner Class ====//
                    method.add(thisClassName);

                    //==== Parameters ====//
                    GNode parameters = GNode.create("Parameters");

                    //need a paramTraverse to equal the FormalParameters node so that we can get all the parameters using a
                    //for loop
                    GNode paramTraverse = (GNode) node.getNode(4);

                    /*
                    todo
                        add name to the type in the parameters (String name, String parameters) for test022
                     */

                    //add implicit "this" class as first parameter always
                    parameters.add(header.getName());

                    //adds the string containing the type of each parameter to the Parameters node
                    for (int i = 0; i < paramTraverse.size(); i++) {
                        parameters.add(paramTraverse.getNode(i).getNode(1).getNode(0).getString(0));
                    }

                    method.add(parameters);

                    //==== Finish ====//
                    /*
                    todo
                        add support for static (don't add static method to the vtable, but put it in data layout)
                     */
                    //if the method is private, don't add it
                    if (method.getNode(0).size() > 0) {
                        if (!method.getNode(0).getString(0).equals("private")) {
                            //an index >= 0 indicates it found a new implementation of an inherited method, so replace
                            //the removed VTFD with this new implementation
                            if (index >= 0) {
                                vTable.add(index, method);
                            } else {
                                vTableChildren.add(method);
                            }
                        }
                    } else {
                        vTableChildren.add(method);
                    }
                }

                visit(node);
            }

            public void visit(Node n) {
                for (Object o : n) if (o instanceof Node) dispatch((Node) o);
            }

        } .dispatch(parent);

        for (GNode g : vTableChildren) {
            vTable.add(g);
        }
        //vTable.add(parentClass);

        return vTable;
    }

    public GNode getTree() {
        return object;
    }

}
