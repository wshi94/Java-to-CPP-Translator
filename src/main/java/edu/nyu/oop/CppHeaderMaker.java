package edu.nyu.oop;

import java.util.Scanner;

import java.util.List;
import java.util.ArrayList;
import java.io.*;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;

import edu.nyu.oop.util.*;
import edu.nyu.oop.AstUtil;

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
import xtc.tree.Printer;


import edu.nyu.oop.util.NodeUtil;
import edu.nyu.oop.Boot;
import edu.nyu.oop.util.JavaFiveImportParser;
import edu.nyu.oop.util.XtcProps;


/**
 * This file takes in a simplified AST representing the header file and makes a .h from that AST.
 */

public class CppHeaderMaker {

    private Printer printer;
    private GNode headerAst;

    private final int commentShort = 1;         //integer constant representing a short comment (//)
    private final int commentLong = 2;          //integer constant representing a long comment (/* */)

    private boolean debug = false;              //lets me know where in the code an error occurs and under what context
    private String tab = "    ";

    //don't have time, hacked this out
    /*
    todo
        fix this after midterm, shouldn't pass in root
     */
    private GNode root;

    //remove root when we have time for better implementation
    public CppHeaderMaker(GNode headerAst, GNode root) {
        //remove root when we have time
        this.root = root;

        //initialization for writing to a file
        Writer writer = null;
        try {
            FileOutputStream fos = new FileOutputStream("output/output.h");
            OutputStreamWriter osw = new OutputStreamWriter(fos, "utf-8");
            writer = new BufferedWriter(osw);
            this.printer = new Printer(writer);
        } catch (Exception e) {
            throw new RuntimeException("Cannot find the output location, please specify");
        }

        //printer.register(this);

        this.headerAst = headerAst;


        //print();
    }

    public void print() {
        if (debug) {
            System.out.println("in print");
        }

        //print initial setup code
        printInit();

        //print forward declarations and type defs
        printForwardDeclaration();

        new Visitor() {

            private String currentClassName;    //the current working class name
            private List<GNode> currentVTableFunctions = new ArrayList<GNode>();    //the current working VTable
            //functions list

            /*
            Visit the object to register the Visitor to the printer we're using
             */
            public void visitObject(GNode n) {
                printer.register(this);
                visit(n);
            }

            /*
            When visiting the header declaration, we should extract
                - the name of the class
            and use them in
                - the forward declaration (struct DL, struct VT, typedef)
                - the struct DL implementation (name, variables, parameters)
                - the struct VT implementation (name, parameters, constructor)
             */
            public void visitHeaderDeclaration(GNode n) {
                if (debug) {
                    System.out.println(tab + tab + "in " + AstUtil.getClassName(n) + " header declaration");
                }

                //set the current working class name
                currentClassName = AstUtil.getClassName(n);

                //don't print out java lang stuff
                if (!(currentClassName.equals("Object") ||
                        currentClassName.equals("String") ||
                        currentClassName.equals("Class"))) {
                    visit(n);
                }
            }

            //============================== Data Layout Print ==============================//

            /*
            When visiting the data layout, we just need to create the struct for the data layout of the class
             */
            public void visitDataLayout(GNode n) {
                printComment(commentShort, "The data layout for " + currentClassName);
                printer.indent().pln("struct __" + currentClassName + "{");
                printer.incr().pln();

                // default constructor
                printer.indent().pln("__" + currentClassName + "();");

                visit(n);
                /*
                todo
                    implement comments for all the data layout
                 */

                //after visiting all the child nodes, close the struct braces
                //printer.pln("Virtual void input16terror(){};");
                printer.decr().pln();
                printer.indent().pln("};");
                printer.pln();

            }

            /*
            When visiting the field declaration, we need to extract the modifiers, the field type, and the
            field name and print out the field with that information.
             */
            public void visitFieldDeclaration(GNode n) {
                //printComment(commentShort, "The field declarations");

                //if there are no modifiers, just print the field declaration
                if (n.getNode(0).size() == 0) {
                    printer.indent().pln(AstUtil.getType(n) + " " + AstUtil.getName(n) + ";");
                }
                //otherwise, print out the modifiers first, then print the field declaration
                else {
                    printModifiers(n);

                    printer.pln(AstUtil.getType(n) + " " + AstUtil.getName(n) + ";");
                }

                //no need to visit child node
                //visit(n);
            }

            /*
            When visiting the constructor declaration, we need to extract the constructor name, and the
            arguments that are passed into the constructor and print out the constructor with that information
             */
            public void visitConstructorDeclaration(GNode n) {
                //printComment(commentShort, "The constructor");

                printer.pln();


                //print all the arguments the constructor will take
                //printer.indent().p("__" + AstUtil.getName(n) + "(");

                // Instead of a constructor, we are now using an init method
                printer.indent().p("static void __init(");

                // object we are initing
                printer.p(currentClassName + " __this");

                printArguments(n);

                printer.p(")" + ";");
                printer.pln();
                //printer.indent().p("static void __init(" + AstUtil.getName(n) + ")");
                printer.pln();

                //no need to visit child node
                //visit(n);
            }

            public void visitDLFunctionDeclaration(GNode n) {
                //printComment(commentShort, "The instance methods");

                //printer.pln();

                //if there are no modifiers, just print the function declaration
                if (n.getNode(0).size() == 0) {
                    printer.indent().p("static " + AstUtil.getType(n) + " " + AstUtil.getName(n) + "(");
                }
                //otherwise, print out the modifiers first, then print the function declaration
                else {
                    boolean isStatic = false;

                    for (String s : AstUtil.getModifiers(n)) {
                        if (s.equals("static")) {
                            isStatic = true;
                        }
                    }

                    printModifiers(n);

                    printer.p(AstUtil.getType(n) + " " + AstUtil.getName(n) + "(");
                }

                printArguments(n);

                printer.pln(");");

                //printer.pln();

                //no need to visit child node
                //visit(n);
            }

            //============================== Data Layout Print End ==============================//


            //============================== VTable Print ==============================//

            public void visitVTable(GNode n) {
                printComment(commentShort, "The vtable layout for " + currentClassName);
                printer.indent().pln("struct __" + currentClassName + "_VT{");
                printer.pln();

                visit(n);

                printer.pln();

                printer.incr().indent().pln("__" + currentClassName + "_VT()");

                int i = 0;  //counter variable to see which node we're on
                for (GNode g : currentVTableFunctions) {
                    i++;

                    //if the current node is __isa, then do specific formatting for it
                    if (AstUtil.getName(g).equals("__isa")) {
                        printer.incr().indent().p(": " + AstUtil.getName(g) +
                                                  "(__" + currentClassName + "::__class())");
                    }
                    //otherwise, do more general formatting for other functions
                    else {
                        printer.incr().incr().indent().p(AstUtil.getName(g) + "(").decr();

                        //if the owner of the function is this class, no need for casting
                        if (AstUtil.getOwnerName(g).equals(currentClassName)) {
                            printer.p("&__" + currentClassName + "::" + AstUtil.getName(g) + ")");
                        }
                        //otherwise, we need to cast it and call the owner's implementation of the function
                        else {
                            String ownerName = AstUtil.getOwnerName(g);                         //the method owner name
                            String ownerReturnType = new String();                              //the owner's method return type
                            GNode ownerVTable = AstUtil.getOwnerVTable(headerAst, ownerName);   //the GNode of the owner's VTable

                            //extracts the return type of parent/superclass implementation of the method
                            for (Object o : ownerVTable) {
                                //if the name of the current node in the owner vtable is equal to the current node in
                                //the working vtable, then we have found our implementation and can extract the return type
                                if (AstUtil.getName((GNode) o).equals(AstUtil.getName(g))) {
                                    ownerReturnType = AstUtil.getType((GNode) o);
                                }
                            }

                            printer.p("(" + ownerReturnType + "(*)(");

                            //print function arguments
                            printArguments(g);

                            printer.p(")) &__" + ownerName + "::" + AstUtil.getName(g) + ")");
                        }
                    }

                    if (i != currentVTableFunctions.size()) {
                        printer.pln(",").decr();
                    } else {
                        printer.pln().decr();
                    }
                }

                printer.indent().pln("{").decr();
                printer.incr().indent().pln("}").decr();

                printer.indent().pln("};");
                printer.pln();
                printer.sep();

                currentVTableFunctions.clear();
            }

            public void visitVTFunctionDeclaration(GNode n) {
                //add all the VTFD nodes
                currentVTableFunctions.add(n);

                //print all the function pointers
                //if the current node is __isa, print it normally
                if (AstUtil.getName(n).equals("__isa")) {
                    printer.incr().indent().pln("Class __isa;").decr();
                }
                //otherwise, print the node in a generic way
                else {
                    printer.incr().indent().p(AstUtil.getType(n) + " (*" + AstUtil.getName(n) + ")(");
                    printArguments(n);
                    printer.pln(");").decr();
                }

                //then go back to VTable and print the VT constructor

            }

            //============================== VTable Print End ==============================//

            private void printArguments(GNode n) {
                ArrayList<String> arguments;

                //you want to print all the arguments for a constructor
                if (n.getName().equals("ConstructorDeclaration")) {
                    arguments = AstUtil.getConstructorArguments(n);

                    for(int i = 0; i < arguments.size(); i++) {
                        printer.p(", " + arguments.get(i));
                    }
                }
                //you want to replace the first argument of functions with "this"
                else {
                    arguments = AstUtil.getArguments(n);

                    //print the implicit "this" class as the first argument always
                    if (arguments.size() == 1) {
                        //only argument, no comma
                        printer.p(currentClassName);
                    }
                    //this shouldn't happen, aside from class(), throw an exception if it does
                    else if (arguments.size() == 0) {
                        //throw new RuntimeException("Found that " + currentClassName + " has a no argument function.");
                    } else {
                        //multiple arguments, add a comma for the next one
                        printer.p(currentClassName + ", ");
                    }
                    //if there is no argument (shouldn't happen) or one argument, we only need "this" so nothing further
                    //is printed. Otherwise, print the otherwise arguments
                    for(int i = 1; i < arguments.size(); i++) {
                        if(i >= arguments.size() - 1) {
                            printer.p(arguments.get(i));
                        } else {
                            printer.p(arguments.get(i) + ", ");
                        }
                    }
                }
            }

            private void printModifiers (GNode n) {
                printer.indent();
                boolean isStatic = false;

                //don't add colon for static, only for public/private/protected
                for (String s : AstUtil.getModifiers(n)) {
                    if (s.equals("static")) {
                        printer.p(s + " ");
                        isStatic = true;
                    } else {
                        //this prints the public/private/protected modifiers
                        //printer.p(s + ": ");
                    }
                }

                if (!isStatic) {
                    //don't print default static for field declarations
                    if (!n.getName().equals("FieldDeclaration")) {
                        printer.p("static ");
                    }
                }
            }

            public void visit (Node n) {
                for (Object o : n) if (o instanceof Node) dispatch((Node) o);
            }

        } .dispatch(headerAst);

        printer.pln();
        printer.decr().indent().pln("}");   //close first namespace
        printer.decr().indent().pln("}");   //close second namespace
        printer.pln();
        //printRt();        //print __rt namespace

        printer.flush();
    }

    /**
     * Prints the initial setup at the beginning of any header file
     */
    public void printInit() {
        if (debug) {
            System.out.println(tab + "in printInit");
        }

        printer.pln("#pragma once");
        printer.pln();
        printer.pln("#include <stdint.h>");
        printer.pln("#include <string>");
        printer.pln("#include \"java_lang.h\"");
        printer.pln("\nusing namespace java::lang;");
        printer.pln();


        /*
        todo
            do this without passing in root to this class
         */
        // Get the PackageDeclaration Node
        // Check if namespace declaration exists.
        // TODO: perhaps redundant if namespace index is always 0.
        // May throw nullpointer exception if compiler does not use short circuiting
        if (((GNode) root.get(0)).hasName("PackageDeclaration")) {

            if (debug) {
                System.out.println("in packagedec");
            }

            GNode namespaceDeclaration = (GNode) root.getNode(0);
            //if (namespaceDeclaration.get())

            // Check QualifiedIdentifier. always index 1?
            if ((namespaceDeclaration.getNode(1)).hasName("QualifiedIdentifier")) {

                // Get the number of namespaces
                GNode qualifiedIdentifier = (GNode) namespaceDeclaration.getNode(1);
                int numNamespaces = qualifiedIdentifier.size();

                // Print each namespace
                for (int i = 0; i < numNamespaces; i++) {
                    printer.indent().incr().p("namespace " + qualifiedIdentifier.get(i).toString());
                    printer.pln(" {");
                }
                ;
            }
        }


        printer.pln();
    }

    /**
     * Prints the forward declarations of the header file, including the structs and typedefs
     */
    public void printForwardDeclaration() {
        if (debug) {
            System.out.println(tab + "in printForwardDeclaration");
        }

        printComment(commentShort, "Forward declarations of data layout and vtable");

        //visitor to print out the forward declarations of every class
        new Visitor() {
            private String currentClassName;


            public void visitHeaderDeclaration(GNode n) {
                currentClassName = AstUtil.getClassName(n);
                if (!(currentClassName.equals("Object") ||
                        currentClassName.equals("String") ||
                        currentClassName.equals("Class"))) {

                    //as an example:
                    //      struct __Object;
                    //      struct __Object_VT;
                    printer.indent().pln("struct __" + currentClassName + ";");
                    printer.indent().pln("struct __" + currentClassName + "_VT;");
                    printer.pln();
                }

            }

            public void visit(Node n) {
                for (Object o : n) if (o instanceof Node) dispatch((Node) o);
            }

        } .dispatch(headerAst);

        printer.pln();
        printComment(commentShort, "Definition of types equivalent to Java semantics");

        //visitor to print out the type definitions of every class
        new Visitor() {
            private String currentClassName;

            public void visitHeaderDeclaration(GNode n) {
                currentClassName = AstUtil.getClassName(n);
                if (!(currentClassName.equals("Object") ||
                        currentClassName.equals("String") ||
                        currentClassName.equals("Class"))) {

                    //as an example:
                    //      typedef __Object* Object;
                    printer.indent().pln("typedef __" + currentClassName + "* " + currentClassName + ";");
                }
            }

            public void visit(Node n) {
                for (Object o : n) if (o instanceof Node) dispatch((Node) o);
            }

        } .dispatch(headerAst);

        printer.pln();
        printer.sep();
        printer.pln();
    }

    /**
     * Prints comments into the file
     */
    /*
    todo
        make this work better after midterm
     */
    public void printComment(int commentType, String comment) {
        if (debug) {
            System.out.println(tab + "in printComment");
        }

        if (commentType == commentShort) {
            printer.indent().pln("//" + comment);
        } else if (commentType == commentLong) {
            printer.indent().pln("/*");
            printer.indent().pln("* " + comment);
            printer.indent().pln("*/");
        }
    }

    /**
     * Prints the __rt namespace at the end of the header file for some helper functions
     */
    public void printRt() {
        if (debug) {
            System.out.println(tab + "in printRt");
        }

        printer.indent().pln("namespace __rt {");
        printer.pln();
        //should print comment on this line
        printer.indent().pln("java::lang::Object null();");
        printer.pln();
        //should print comment on this line
        printer.indent().pln("inline java::lang::String literal(const char * s) {");
        //should print comment on this line
        printer.incr().indent().pln("return new java::lang::__String(s);").decr();
        printer.indent().pln("}");
        printer.pln();
        printer.indent().pln("}");


    }

}
