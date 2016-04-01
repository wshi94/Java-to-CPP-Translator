package edu.nyu.oop;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import edu.nyu.oop.util.*;
//import sun.jvm.hotspot.debugger.cdbg.Sym;
import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;
import xtc.tree.Printer;
import org.slf4j.Logger;
import xtc.util.*;
import xtc.util.SymbolTable.Scope;
import xtc.type.*;
import xtc.util.Runtime;

/**
 * Phase 5 of translation.
 * Takes the header ast from phase 2 and prints the
 * function declarations and scope then,
 * Takes the mutated / decorated ast from phase 4 and
 * prints the c++ implementation of functions to
 * output.cpp
 */
public class CppFilePrinter extends Visitor {

    // Fields
    private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());
    private Printer printer;
    private String outputLocation = XtcProps.get("output.location");

    private GNode root;         // Root of mutated c++ ast
    private GNode headerRoot;   // Root of header ast
    private Runtime runtime;    // current runtime
    private int numNamespaces;  // The number of namespaces
    private boolean hasArray = false;   // If there is a array or not
    private boolean hasConstant = false;// If there are constant members
    private List<String> arrayQ = new ArrayList<String>();
    private List<Node> constantQ = new ArrayList<Node>();
    private List<Node> FieldDecs = new ArrayList<Node>();

    private String currentClass;        // The current class we are printing : __Class
    private String className;           // The current class name            : Class
    private String superClassName;      // The name of the current class' super class
    private boolean constructorDefined; // Was a constructor defined for the class in the source language?
    private String currentNamespace;    // The current namespace we are working in

    private Map<String, GNode> fieldDeclarationMap;     // Stores all class field declarations with assignments <Field, Value>
    private ClassInheritanceHierarchy inheritanceGraph; // Graph of class inheritance hierarchy

    private SymbolTable symbolTable;    // Symbol table

    // Array Check todo

    // Constant Check todo

    // Constructor
    public CppFilePrinter(GNode srcNode, Runtime runtime) {

        // initialize variables
        root = srcNode;
        this.runtime = runtime;
        numNamespaces = 0;
        currentClass = "";
        className = "";
        currentNamespace = "";
        inheritanceGraph = new ClassInheritanceHierarchy(root);
        symbolTable = new SymbolTable(root, this.runtime);
        Writer w = null;

        // Try opening i/o to the output file
        try {
            FileOutputStream fos = new FileOutputStream(outputLocation + "/output.cpp");
            OutputStreamWriter ows = new OutputStreamWriter(fos, "utf-8");
            w = new BufferedWriter(ows);
            this.printer = new Printer(w);
        } catch (Exception e) {
            throw new RuntimeException("Output location not found.  Create the /output directory.");
        }

        // Register the visitor with the printer
        printer.register(this);
    }

    public void visit(Node n) {
        for (Object o : n) if (o instanceof Node) dispatch((Node) o);
    }

    /**
     * Prints the entire c++ implementation file
     */
    public void print() {
        headOfFile();
        bodyOfFile();
        tailOfFile();
        printer.flush();
    }

    /**
     * Handles the printing of inclusions and namespaces
     */
    private void headOfFile() {

        // Include headers of dependency files
        printer.pln("#include \"output.h\"");
        printer.pln("#include \"java_lang.h\"");
        printer.pln("#include <iostream>");
        printer.pln();

        // Get the PackageDeclaration Node
        if (((GNode) root.get(0)).hasName("NamespaceDeclaration")) {

            GNode namespaceDeclaration = (GNode) root.get(0);

            // Check QualifiedIdentifier. always index 1?
            if (((GNode) namespaceDeclaration.get(0)).hasName("QualifiedIdentifier")) {

                // Get the number of namespaces
                GNode qualifiedIdentifier = (GNode) namespaceDeclaration.get(0);
                numNamespaces = qualifiedIdentifier.size();

                // Print each namespace
                for (int i = 0; i < numNamespaces; i++) {
                    printer.indent().incr().p("namespace " + qualifiedIdentifier.get(i).toString());
                    printer.pln(" {");

                    // Add namespace to our current namespace we are working in
                    currentNamespace += qualifiedIdentifier.get(i).toString();

                    // Don't add new scope if last namespace
                    if (i != numNamespaces - 1) {
                        currentNamespace += ".";
                    }
                }
                printer.pln();
                printer.indent().pln("using namespace std;");
                printer.pln();
            }
        }
    }

    /**
     * Handles the printing of closing braces
     */
    private void tailOfFile() {

        printer.pln();

        // Closing brace for each namespace
        for (int i = 0; i < numNamespaces; i++) {
            printer.decr().indent().pln("}");
        }

        printer.pln();

        if (!className.equals("")) {
            printer.pln("namespace __rt");
            printer.pln("{");
            String[] cNames= arrayQ.toArray(new String[arrayQ.size()]);
            for(int i=0; i<arrayQ.size(); i++) {
                printer.incr().indent().pln("template<>");
                //extract namespaces
                String[] nameSpaceslol = currentNamespace.split("[.]");
                //NEEED TO FIND PARENT

                String outsideClass = "";
                if (outsideClass.length() > 3) outsideClass = className.substring(2);
                else outsideClass = className;
                outsideClass=cNames[i];
                printer.indent().pln("java::lang::Class Array<" + nameSpaceslol[0] + "::" + nameSpaceslol[1]
                                     + "::" + outsideClass + ">::" + "__class" + "()");
                //^^Object should be changed to parent of class

                printer.indent().pln("{");
                printer.incr().indent().pln("static java::lang::Class k =");

                //Need to define currentNamespace and className just like below
                printer.incr().indent().pln(" new java::lang::__Class(literal(\"[L" + currentNamespace +
                                            "." + outsideClass + ";\"),");
                printer.incr().incr().incr().incr().incr().incr().indent().pln("java::lang::__Object::__class(),");

                //line below should look like:
                //inputs::test001::__A::__class(); -- note the colons after inputs

                //inputs.test001
                //inputs test001
                //change object to parent aka extended
                printer.indent().pln(nameSpaceslol[0] + "::" + nameSpaceslol[1] + "::" + currentClass + "::__class());");
                printer.decr().decr().decr().decr().decr().decr().decr().indent().pln("return k;");
                printer.decr().indent().pln("}");
            }
            printer.decr().indent().pln("}");
        }

    }

    /**
     * Handles the heavy duty work
     * Prints the Class methods
     */
    private void bodyOfFile() {

        // output.cpp only contains method implementations, so we only need
        // to look at methodDeclarations inside every class except the class with main method
        List<Node> classDeclarations = NodeUtil.dfsAll(root, "ClassDeclaration");

        // Iterate through each class
        for (Node classDeclaration : classDeclarations) {

            // Do not process main method class
            if (!AstUtil.isMainClass((GNode) classDeclaration)) {

                dispatch(classDeclaration);
            }
        }
    }

    public void visitClassDeclaration(GNode classDeclaration) {

        // Initialize variables
        constructorDefined = false;
        fieldDeclarationMap = new HashMap<String, GNode>();

        // enter the class scope
        symbolTable.enterScope(classDeclaration);

        // We will use this to print the (__this) parameter in methods
        className = classDeclaration.get(1).toString();

        // We will use this to print the scopes easily
        currentClass = "__" + className;
        arrayQ.add(className);

        // Get super class if it exist
        superClassName = inheritanceGraph.getSuperClass(className);

        //------------------------------------------------------------------------------

        // pretty print comments
        printer.indent().pln("//--------------------------------------------------------");
        printer.indent().pln("// Class " + className + " implementation");
        printer.indent().pln("//--------------------------------------------------------");
        printer.pln();

        // Handle any static member initialization

        // check for constants
        if (NodeUtil.dfs(classDeclaration, "FieldDeclaration") != null) {
            FieldDecs = NodeUtil.dfsAll(classDeclaration, "FieldDeclaration");
            for (int i = 0; i < FieldDecs.size(); i++) {
                if (NodeUtil.dfs(FieldDecs.get(i), "IntegerLiteral") != null) {
                    hasConstant = true;
                    constantQ.add(FieldDecs.get(i));
                }
            }

            // Store all fields in a map for later use in __init methods ONLY if it is not static
            for( Node fieldDeclaration : FieldDecs) {

                boolean staticField = false;

                // try to find modifiers for field
                try {
                    String modifier = fieldDeclaration.getGeneric(0).getGeneric(0).getString(0);

                    // determine whether it is static or not
                    if(modifier.equals("static")) {
                        staticField = true;
                    }

                    else {
                        staticField = false;
                    }
                } catch (Exception e) {
                    staticField = false;
                }

                // only process non-static fields for __init
                if(!staticField) {
                    // Get field declarator
                    GNode declarator = fieldDeclaration.getGeneric(2).getGeneric(0);

                    // Get field identifier
                    String fieldIdentifier = declarator.getString(0);


                    // Store any field declarations with assignments in a map for later use with all __init methods
                    if (AstUtil.isFieldDeclarationWithAssignment((GNode) fieldDeclaration)) {

                        // Get assigned field value
                        GNode fieldValue = declarator.getGeneric(2);

                        // Store value for later use in __init methods
                        fieldDeclarationMap.put(fieldIdentifier, fieldValue);
                    }

                    // Store any field declarations without assignments
                    // HACK: test006 has fields twice in the map, it overwrites initial value
                    else if (!fieldDeclarationMap.containsKey(fieldIdentifier)) {


                        fieldDeclarationMap.put(fieldIdentifier, null);
                    }
                }
            }
        }

        // Print static constant members
        if (hasConstant == true) {
            String Type = constantQ.get(0).getGeneric(1).getGeneric(0).get(0).toString();
            String Variable = constantQ.get(0).getGeneric(2).getGeneric(0).get(0).toString();
            String Literal = constantQ.get(0).getGeneric(2).getGeneric(0).getGeneric(2).get(0).toString();
        }

        // Default constructor
        printer.indent().pln(currentClass + "::" + currentClass + "() : __vptr(&__vtable) {}");
        printer.pln();

        // only visit method declarations
        List<Node> methodDeclarations = NodeUtil.dfsAll(classDeclaration, "MethodDeclaration");

        // also visit static field declarations
        List<Node> fieldDeclarations = NodeUtil.dfsAll(classDeclaration, "FieldDeclaration");

        // check for constructor in the source language
        for (Node n : methodDeclarations) {
            if (n.getString(3).equals("__init")) {
                constructorDefined = true;
            }
        }

        // if no constructor is defined, print a default constructor
        if (!constructorDefined) {
            printer.indent().pln("void " + currentClass + "::" + "__init (" + className + " __this) {");

            // for every field initialize to null value
            for(Map.Entry<String, GNode> entry : fieldDeclarationMap.entrySet()) {

                // get field and value
                String field = entry.getKey();
                GNode value = entry.getValue();

                // field declaration line to be printed
                String fieldDeclarationLine = "__this->" + field;

                // get cast type
                VariableT classField = (VariableT) symbolTable.current().lookupLocally(field);
                Type fieldType = classField.getType();

                // initialize all fields to null
                fieldDeclarationLine +=  " = (" + fieldType.getName() + ") __rt::null();";

                // print the field dec line
                printer.indentMore().pln(fieldDeclarationLine);
            }


            printer.indent().pln("}");
            printer.pln();
        }

        // visit each method declaration
        for (Node n : methodDeclarations) {
            dispatch(n);
        }

        // visit field declarations
        // we only want to visit class fields that are static
        for (Node m : fieldDeclarations) {
            GNode fieldModifiers = m.getGeneric(0);     //for readability

            //if there are modifiers
            if (fieldModifiers.size() > 0) {

                //get the modifier string
                String modifier = fieldModifiers.getGeneric(0).getString(0);

                //if it is static, we want to dispatch
                if (modifier.equals("static")) {
                    dispatch(m);
                }
            }
        }

        // Print __class() method
        printer.indent().incr().pln("Class " + currentClass + "::__class() {");
        printer.indent().pln("static Class k = new __Class(__rt::literal(\"" + currentNamespace +
                             "." + className + "\"), (Class) __rt::null());");
        printer.indent().pln("return k;");
        printer.decr().indent().pln("}");
        printer.pln();

        //__A_VT __A::__vtable;
        String vtInitializer = currentClass + "_VT " + currentClass + "::__vtable;";
        printer.indent().pln(vtInitializer);

        // pretty printing comments
        printer.pln();
        printer.indent().pln("//--------------------------------------------------------");
        printer.indent().pln("// End of Class " + className + " implementation");
        printer.indent().pln("//--------------------------------------------------------");


        // exit class scope
        symbolTable.exitScope(classDeclaration);
    }

    public void visitFieldDeclaration(GNode fieldDeclaration) {

        // Local variables
        GNode fieldModifiers = fieldDeclaration.getGeneric(0);   // Modifiers
        GNode fieldDeclarators = fieldDeclaration.getGeneric(2); // Field declarators
        String fieldModifier = "";      // Individual modifiers
        String fieldType = "";          // Field type
        String fieldIdentifier = "";    // Field identifier
        String fieldDeclarator = "= ";  // Field declarator
        String declaration = "";        // Field declaration that will be printed
        boolean isStatic = false;       // Holds whether we are dealing with static field or not
        //GNode Type;
        //--------------------------------------------------------------------


        if (fieldModifiers.size() > 0) {

            fieldModifier = fieldModifiers.getGeneric(0).get(0).toString() + " ";

            if (fieldModifier.equals("static ")) {
                isStatic = true;
            }
        }

        // check for field modifiers
        if (fieldModifiers.size() > 0) {

            // TODO: handle multiple modifiers
            fieldModifier = fieldModifiers.getGeneric(0).get(0).toString() + " ";
        }

        // Field type
        fieldType = fieldDeclaration.getGeneric(1).getGeneric(0).get(0).toString();

        // Field identifier
        fieldIdentifier = fieldDeclarators.getGeneric(0).get(0).toString();

        // if the field was assigned a value, it would have declarators
        if (fieldDeclarators.size() > 0) {

            for (int i = 0; i < fieldDeclarators.size(); i++) {

                // check if it's a GNode, otherwise it could be null (leads to null pointer exception)
                if (fieldDeclarators.getNode(i).get(2) instanceof GNode) {
                    //make a declarator node for easier readability
                    GNode declarator = (GNode) fieldDeclarators.getNode(i).getGeneric(2);

                    //for string literals, just add the string as a declarator
                    if (declarator.hasName("StringLiteral")) {
                        //always declare a new java.lang.String object with StringLiterals
                        fieldDeclarator += "new __String(" + declarator.getString(0) + ")";
                    } else if (declarator.hasName("IntegerLiteral")) {
                        fieldDeclarator += declarator.getString(0);
                    } else if (declarator.hasName("FloatingPointLiteral")){
                        fieldDeclarator += AstUtil.parseFloatingPointLiteral(declarator);
                    }
                }
            }
        }

        // add field type, current class, and identifier
        if (isStatic) {
            //      int __A::x
            declaration += fieldType + " " + currentClass + "::" + fieldIdentifier;
        } else {
            declaration += fieldType + " " + fieldIdentifier;
        }

        // if there was an assignment to the field
        if (fieldDeclarator.length() > 2) {

            // concatenate the assignment after the field identifier
            declaration += " " + fieldDeclarator;
        }

        // add semicolon
        declaration += ";";

        // print the field declaration
        printer.indent().pln(declaration);
        printer.pln();

        visit(fieldDeclaration);

    }

    public void visitMethodDeclaration(GNode methodDeclaration) {

        // local variables
        GNode returnType = methodDeclaration.getGeneric(2);         // Method return type node
        GNode qualifiedIdentifier;                                  // Method type qualified id
        GNode formalParameters = methodDeclaration.getGeneric(4);   // Method parameters node
        GNode formalParameter;                                      // Method single formal parameter
        GNode formalParameterType;                                  // Method parameter type
        GNode modifiers = methodDeclaration.getGeneric(0);
        String methodReturnType;                                    // Method return type
        String methodName = methodDeclaration.get(3).toString();    // Method name
        String parameters = "(" + className + " __this";            // Method parameters, needs an explicit "this"
        String parameterType;                                       // Type of a single formal parameter
        String formalParameterIdentifier;                           // Formal parameter identifier
        boolean isStatic = false;

        // enter method scope
        symbolTable.enterScope(methodDeclaration);

        // Get return type
        // constructors have no return type
        if (null == returnType) {

            // void
            methodReturnType = "void";
        }

        // if return type is void
        else if (returnType.hasName("VoidType")) {

            methodReturnType = "void";
        }

        // return type that isn't void
        else {

            // Get return type
            methodReturnType = AstUtil.getVarType(returnType);
        }

        //see if the method is static
        for (int j = 0; j < modifiers.size(); j++) {
            if (modifiers.getGeneric(j).getString(0).equals("static")) {
                isStatic = true;
            }
        }

        //if the method is static, we don't want explicit this in there
        if (isStatic) {
            parameters = "(";
        }

        // Iterate through each parameter
        for (int i = 0; i < formalParameters.size(); i++) {

            // Get a single parameter and its type node
            formalParameter = formalParameters.getGeneric(i);
            formalParameterType = formalParameter.getGeneric(1);

            // Get return type
            parameterType = AstUtil.getVarType(formalParameterType);

            // Get parameter identifier
            formalParameterIdentifier = formalParameter.get(3).toString();

            // Concatenate the parameter to the end of the parameter list
            parameters += ", " + parameterType + " " + formalParameterIdentifier;
        }

        // End of parameter list
        parameters += ")";

        // Print function declaration
        printer.indent().pln(methodReturnType + " " + currentClass + "::" + methodName + parameters + " {");

        // for __init methods we must initialize any class level declared fields
        if(methodName.equals("__init")) {

            // save scopes so we do not produce side effects to symbol table
            Scope parentScope = symbolTable.current().getParent();
            Scope currentScope = symbolTable.current();

            // if there is a super class
            if(null != superClassName) {

                // we need a super init in each init method
                printer.indentMore().p("__" + superClassName + "::__init((" + superClassName + ") __this");

                GNode block = methodDeclaration.getGeneric(7);

                boolean isSuper = false;

                Node call = NodeUtil.dfs(block, "CallExpression");

                if (null != call) {
                    if (call.getString(2).equals("__init")) {
                        isSuper = true;
                    }
                }

                if (isSuper) {
                    Node args = NodeUtil.dfs(block, "Arguments");

                    if (null != args) {
                        for (int i = 1; i < args.size(); i++) {
                            if (args.get(i) instanceof GNode) {
                                if (args.getGeneric(i).hasName("PrimaryIdentifier")) {
                                    printer.p(", " + args.getGeneric(i).getString(0));
                                } else {
                                    System.out.println("new formal parameter type for init");
                                }
                            } else {
                                printer.p(", " + args.get(i));
                            }
                        }
                    }
                }
                printer.pln(");");
            }

            // iterate through each class level declared field
            for(Map.Entry<String, GNode> entry : fieldDeclarationMap.entrySet()) {

                // get field and value
                String field = entry.getKey();
                GNode value = entry.getValue();

                // field declaration line to be printed
                String fieldDeclarationLine = "__this->" + field;

                // if value is defined in source language, assign the field to a value
                if(null != value) {

                    // Allocate memory for the object being assigned based on object type
                    fieldDeclarationLine +=  " = " + parseFieldAssignment(value) + ";";
                }

                // otherwise just declare the variable with a null value
                else {

                    // enter the class scope so we can get field types
                    symbolTable.setScope(parentScope);

                    // we must cast the object first
                    VariableT classField = (VariableT) symbolTable.current().lookupLocally(field);
                    Type fieldType = classField.getType();
                    String type = fieldType.toString();

                    // if type is primitive int do not cast, just set to 0
                    if(type.equals("int")) {

                        // primitive types
                        fieldDeclarationLine += " = 0;";
                    }

                    else {

                        // class types
                        fieldDeclarationLine += " = (" + fieldType.getName() + ") ";

                        // set field to null
                        fieldDeclarationLine += "__rt::null();";
                    }



                    // go back to original scope
                    symbolTable.setScope(currentScope);
                }

                // print the field dec line
                printer.indentMore().pln(fieldDeclarationLine);
            }


        }

        visit(methodDeclaration);

        // exit method scope
        symbolTable.exitScope(methodDeclaration);
    }

    public void visitExpressionStatement(GNode expressionStatement) {
        GNode expression = expressionStatement.getGeneric(0);

        // assignment expression
        if (AstUtil.isAssignmentExpression(expression)) {

            String lhs = "", rhs = "";  // left and right hand side
            GNode rhsNode;

            // check for __this member
            if (expression.getGeneric(0).hasName("SelectionExpression")) {

                // selection expression
                GNode selectionExpression = expression.getGeneric(0);

                // if __this selector
                if (selectionExpression.getGeneric(0).hasName("ThisExpression")) {

                    // __this.field
                    lhs = "__this->" + expression.getGeneric(0).get(1).toString();
                }
            } else if (expression.getGeneric(0).hasName("PrimaryIdentifier")) {

                // lhs identifier
                lhs = expression.getGeneric(0).get(0).toString();

                // check if refering to local variable or __this member
                if (null == symbolTable.current().lookupLocally(lhs)) {

                    // not a local variable, it is a member of __this
                    lhs = "__this->" + lhs;
                }

            } else if(expression.getGeneric(0).hasName("SubscriptExpression")) {
                lhs += AstUtil.parseSubscriptExpression(expression.getGeneric(0));
            }

            // TODO handle this.field and field like lhs
            rhsNode = expression.getGeneric(2);

            if (rhsNode.hasName("StringLiteral")) {
                rhs = "new __String(" + rhsNode.getString(0) + ")";
            } else if (rhsNode.hasName("PrimaryIdentifier")) {
                rhs = rhsNode.getString(0);
            } else if (rhsNode.hasName("ThisExpression")) {

                //no __this in the mutated AST so hardcoding it
                rhs = "__this";
            } else if (rhsNode.hasName("IntegerLiteral")) {
                rhs = rhsNode.getString(0);
            } else if(rhsNode.hasName("FloatingPointLiteral")){
                rhs = AstUtil.parseFloatingPointLiteral(rhsNode);
            }

            // print the expression
            printer.indent().pln(lhs + " = " + rhs + ";");
        } else if (expression.hasName("CallExpression")) {
            if (null == expression.get(0)) {
                GNode arguments = expression.getGeneric(3);

                String variable = "";
                String translatedString = "cout << ";

                //possibly have more than one argument?
                //getGeneric(3) gets arguments
                //getGeneric(0) gets the first argument
                GNode child = arguments.getGeneric(0);

                //check for all the possible kinds of nodes
                if (child.isGeneric()) {
                    //if it is a string literal, we shouldn't append a method on it
                    if (child.hasName("PrimaryIdentifier") || child.hasName("StringLiteral")) {
                        variable = child.getString(0);
                    } else if (child.hasName("CallExpression")) {
                        variable = AstUtil.parseCallExpression(child);
                    } else if (child.hasName("SelectionExpression")) {
                        variable = AstUtil.parseSelectionExpression(child);
                    }

                    //either __this->variable or variable depending on symbolTable
                    translatedString += checkLocalOrThis(variable);
                }

                if (expression.getString(2).equals("super")) {

                } else {
                    //second/last argument is "endl"
                    translatedString += " << " + arguments.getString(arguments.size() - 1) + ";";
                }

                printer.indent().pln(translatedString);
            }
            //non constructor init (super call)
            else if (expression.getString(2).equals("__init")) {
                //((A) __this)->__init((A) __this, i)
                //System.out.println("hi");

            }
        }
    }

    public String checkLocalOrThis(String variable) {
        String variableActual = "";

        for (int i = 0; i < variable.length(); i++) {
            if (variable.charAt(i) != '-') {
                variableActual += variable.charAt(i);
            } else {
                break;
            }
        }

        // check if referring to local variable or __this member
        if (null == symbolTable.current().lookupLocally(variableActual) &&
                null == symbolTable.current().getParent().lookupLocally(variableActual)) {
            // not a local variable, it is a member of __this
            variable = "__this->" + variable;
        }

        return variable;
    }

    public void visitBlock(GNode block) {

        // Enter block scope
        symbolTable.enterScope(block);

        // Create a new block//
        printer.incr();
        visit(block);
        printer.decr().indent().pln("}");
        printer.pln();

        // Exit block scope
        symbolTable.exitScope(block);
    }

    public void visitReturnStatement(GNode returnStatement) {

        // We will always start with "return "
        String returnString = "return ";

        // TODO handle multiple arguments
        // for now we will just handle string literals

        // Try getting a GNode
        try {
            GNode stringLiteral = returnStatement.getGeneric(0);

            // Check for literal string
            if (stringLiteral.hasName("StringLiteral")) {
                returnString += "new __String(" + stringLiteral.get(0) + ")";
            }
        } catch (Exception e) {
            // It is not a generic so we just print concatenate it normally
        }
        try {
            GNode primaryIdentifier = returnStatement.getGeneric(0);

            if (primaryIdentifier.hasName("PrimaryIdentifier")) {
                returnString += "__this->" + primaryIdentifier.get(0);
            }
        } catch (Exception e) {

        }
        try {
            GNode callExpression = returnStatement.getGeneric(0);
            if (callExpression.hasName("CallExpression")) {

                //this uses parseCallExpression2, change it later to decouple main and file printers
                String tmpReturnString = AstUtil.parseCallExpression2(callExpression, symbolTable);
                String __this = checkLocalOrThis(tmpReturnString);
                returnString += __this;
                //returnString += "__this->" + primaryIdentifier.get(0);
            }
        } catch (Exception e) {

        }
        try {
            GNode integerLiteral = returnStatement.getGeneric(0);

            if (integerLiteral.hasName("IntegerLiteral")) {
                returnString += integerLiteral.get(0);
            }
        } catch (Exception e) {

        }
        try {
            GNode additiveExpression = returnStatement.getGeneric(0);

            if (additiveExpression.hasName("AdditiveExpression")) {
                returnString += additiveExpression.getGeneric(0).getString(0) +
                                additiveExpression.getString(1) +
                                checkLocalOrThis(additiveExpression.getGeneric(2).getString(0));
            }
        } catch (Exception e) {

        }


        // Add semicolon
        returnString += ";";

        printer.indent().pln(returnString);
    }

    //visits a for loop
    public void visitForStatement(GNode forStatement) {
        printer.indent().pln(AstUtil.parseForStatement(forStatement)).incr();
        visit(forStatement);
        printer.decr().indent().pln("}\n");
    }

    //visits a while loop
    public void visitWhileStatement(GNode whileStatement) {
        printer.indent().pln(AstUtil.parseWhileStatement(whileStatement)).incr();
        visit(whileStatement);
        printer.decr().indent().pln("}\n").incr();

    }

    /**
     * Parses the value of the fieldDeclarationMap and returns a string of the parsed object
     * @param value Value of a field declaration in the fieldDeclarationMap
     * @return the parsed string
     */
    private String parseFieldAssignment(GNode value) {

        String fieldValue = "";
        String type = value.getName();

        // Assign value depending on type
        switch (type) {
        case "StringLiteral": {
            fieldValue = "new __String(" + value.getString(0) + ")";
        }
        break;

        // Default case; should never get to this point
        default: {

        }
        }

        return fieldValue;
    }
}
