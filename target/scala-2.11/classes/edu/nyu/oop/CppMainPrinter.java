package edu.nyu.oop;

import java.io.*;

import edu.nyu.oop.util.*;
import xtc.lang.JavaEntities;
import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;
import xtc.tree.Printer;
import org.slf4j.Logger;
import xtc.type.AST;
import xtc.type.*;
import xtc.type.Type;
import xtc.util.*;
import xtc.util.SymbolTable.Scope;
import xtc.util.Runtime;

import java.util.Iterator;

import java.lang.reflect.Array;
import java.util.ArrayList;

import java.util.List;
import java.util.ListIterator;

/**
 * Phase 5 of translation.
 * Takes the mutated / decorated ast from phase 4
 * and prints the c++ main function to main.cpp
 */
public class CppMainPrinter extends Visitor {

    // Fields
    private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());
    private Printer printer;
    private String outputLocation = XtcProps.get("output.location");
    private Runtime runtime;

    private GNode root; // Root of c++ ast
    private GNode main; // Main method GNode

    private SymbolTable symbolTable;

    // Constructor
    public CppMainPrinter(GNode srcNode, Runtime runtime) {
        root = srcNode;
        this.runtime = runtime;
        Writer w = null;

        // Try opening i/o to the main.cpp file
        try {
            FileOutputStream fos = new FileOutputStream(outputLocation + "/main.cpp");
            OutputStreamWriter ows = new OutputStreamWriter(fos, "utf-8");
            w = new BufferedWriter(ows);
            this.printer = new Printer(w);
        } catch (Exception e) {
            throw new RuntimeException("Output location not found.  Create the /output directory.");
        }

        // Register the visitor with the printer
        printer.register(this);

        // Find main method and set the main method node to it
        List<Node> methods = NodeUtil.dfsAll(root, "MethodDeclaration");
        for (Node n : methods) {
            if (n.get(3).equals("main")) {
                main = (GNode) n;
            }
        }

        // create a new symbol table with root node of ast
        symbolTable = new SymbolTable(root, runtime);
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
     * Handles printing of inclusions and namespaces
     */
    private void headOfFile() {

        // Include header of dependency files
        printer.pln("#include <iostream>");
        printer.pln("#include <iomanip>");
        printer.pln("#include <limits>");
        printer.pln("#include \"output.h\"");
        printer.pln("#include \"java_lang.h\"");
        printer.pln("\nusing namespace std;");
        printer.pln("using namespace java::lang;");

        /**
         * This currently only handles printing 1 namespace,
         * TODO: change to print multiple namespaces if specified
         */
        // Get the PackageDeclaration Node
        if(((GNode) root.get(0)).hasName("NamespaceDeclaration")) {

            GNode namespaceDeclaration = (GNode) root.get(0);
            String usingNamespace = "using namespace ";

            // Check QualifiedIdentifier. always index 1?
            if(((GNode) namespaceDeclaration.get(0)).hasName("QualifiedIdentifier")) {

                // Get the number of namespaces
                GNode qualifiedIdentifier = (GNode) namespaceDeclaration.get(0);
                int numNamespaces = qualifiedIdentifier.size();

                // Get namespace
                for(int i = 0; i < numNamespaces; i++) {
                    usingNamespace += qualifiedIdentifier.get(i).toString();

                    // if not last identifier, add scope
                    if(i != numNamespaces - 1)
                        usingNamespace += "::";
                }

                // Add semicolon
                usingNamespace += ";";

                // Print namespace
                printer.pln(usingNamespace);
            }
        }
        printer.pln();

        // Start of main method
        printer.incr().pln("int main(int argc, char* args[]) {\n");
    }

    /**
     * Handles the heavy duty work.
     * Prints the rest of main method
     */
    private void bodyOfFile() {

        // enter scope
        symbolTable.enterScope(main);

        // Dispatch main method's body
        dispatch(main.getGeneric(7));

        // exit scope
        symbolTable.exitScope(main);
    }

    /**
     * Handles printing of any closing braces and returns
     */
    private void tailOfFile() {

        // Return 0
        printer.indent().pln("return 0;\n");

        // Closing brace for main method
        printer.decr().indent().pln("}");
    }

    //------------------------------------------------------------
    // Visitor methods

    public void visitBlock(GNode block) {

        // enter block scope
        symbolTable.enterScope(block);

        // visit
        visit(block);

        // exit block scope
        symbolTable.exitScope(block);
    }

    public void visitExpressionStatement(GNode n) {
        visit(n);
    }

    public void visitCallExpression(GNode n) {

        // local variables
        String method = "";

        // Check if calling a print / cout expression
        if (n.getString(2).equals("cout")) {

            // Pass the arguments for printing
            printer.indent().pln("cout << " + cout(n.getGeneric(3)));
            printer.pln();
        } else {

            String translatedString = "";

            // Translate each argument
            for (int i = 0; i < n.size(); i++) {
                if (n.get(i) instanceof GNode) {
                    GNode argument = (GNode) n.getNode(i);

                    //check for object name
                    if (argument.hasName("PrimaryIdentifier")) {
                        translatedString += AstUtil.parsePrimaryIdentifier(argument) + "->__vptr";
                    }
                    //check for method arguments
                    else if (argument.hasName("Arguments")) {
                        translatedString += "(" + parseArguments(argument, method) + ")";
                    } else if (argument.hasName("CastExpression")) {
                        String type = argument.getGeneric(0).getGeneric(0).getString(0);
                        String identifier = argument.getGeneric(1).getGeneric(0).getString(0);
                        String subscript = argument.getGeneric(1).getGeneric(1).getString(0);


                        translatedString += "(" + type + ") " + identifier + "->__data[" + subscript + "]";
                    }
                } else if (n.get(i) instanceof String) {
                    //most likely the method name
                    translatedString += "->" + n.getString(i);
                    method = n.getString(i);

                }
            }

            // Add semicolon
            translatedString += ";";

            printer.indent().pln(translatedString);
        }
    }

    public void visitExpression(GNode n) {

        String translatedString = "";
        String leftSide = "";
        String rightSide = "";
        String leftIdentifier = "";

        //left side of the expression
        GNode left = n.getGeneric(0);

        if (left.isGeneric()) {
            //if it is a string literal, we shouldn't append a method on it
            if (left.hasName("PrimaryIdentifier") || left.hasName("StringLiteral")) {
                leftSide = left.getString(0);
            } else if (left.hasName("CallExpression")) {
                leftSide = AstUtil.parseCallExpression(left);
            } else if (left.hasName("SelectionExpression")) {
                leftSide = AstUtil.parseSelectionExpression(left);
            } else if (left.hasName("SubscriptExpression")) {
                String identifier = left.getGeneric(0).getString(0);
                String subscript = left.getGeneric(1).getString(0);

                leftSide = identifier + "->__data[" + subscript + "]";

                leftIdentifier = identifier;

                //checking functions for arrays
                printer.indent().pln("__rt::checkNotNull(" + identifier + ");");
                printer.indent().pln("__rt::checkIndex(" + identifier + ", " + subscript + ");");
            }
            //else if(n.getGeneric(0).hasName("SubscriptExpression")){
            //    translatedString += AstUtil.parseSubscriptExpression(n.getGeneric(0));
            //}
            else {
                System.out.println("New left operand type found in visitExpression in CppMainPrinter");
            }
        }

        //the assignment operator
        String assignment = n.getString(1);

        //translatedString += " " + assignment + " ";

        //right side of the expression
        GNode right = n.getGeneric(2);

        if (right.isGeneric()) {
            //if it is a string literal, we shouldn't append a method on it
            if (right.hasName("PrimaryIdentifier") || right.hasName("StringLiteral")) {
                rightSide = right.getString(0);
            } else if (right.hasName("CallExpression")) {
                rightSide = AstUtil.parseCallExpression(right);
            } else if (right.hasName("SelectionExpression")) {
                rightSide = AstUtil.parseSelectionExpression(right);
            } else if (right.hasName("CastExpression")) {
                String castExpression = "";
                String cast = "";
                String castee = "";
                String delims = "[() ]+";
                String[] tokens;
                castExpression = AstUtil.parseCastExpression(right);
                translatedString += castExpression;
                tokens = castExpression.split(delims);
                cast = tokens[1];
                castee = tokens[2];
                //translatedString =
                printer.indent().pln("ClassCastException::check_legal_cast(" + leftSide + "->__class()" + ", " + castee + "->__class());");
                //Does throw an exception as long as the remainder of the file compiles.

                rightSide = AstUtil.parseCastExpression(right);

                //Needs to throw a classcastexception for input016 line 25

            } else if (right.hasName("NewClassExpression")) {
                String tmpVarName = "";

                //maybe do a random thing here
                tmpVarName = "a";

                String type = right.getGeneric(2).getString(0);
                String leftType = "";

                boolean cast = false;

                //print the temporarily created object
                printer.indent().pln(type + " " + tmpVarName + " = new __" + type + "();");

                //init string
                String init = tmpVarName + "->__init(" + tmpVarName;

                Node arguments = NodeUtil.dfs(right, "Arguments");

                if (arguments != null) {
                    for (int j = 0; j < arguments.size(); j++) {
                        GNode argument = arguments.getGeneric(j);

                        if (argument.hasName("StringLiteral")) {
                            init += ", new __String(" + argument.getString(0) + ")";
                        } else if (argument.hasName("IntegerLiteral")) {
                            init += ", " + argument.getString(0);
                        } else if (argument.hasName("FloatingPointLiteral")) {
                            init += ", " + AstUtil.parseFloatingPointLiteral(argument);
                        } else if (argument.hasName("PrimaryIdentifier")) {
                            init += ", " + argument.getString(0);
                        }
                    }
                }
                init += ");";

                printer.indent().pln(init);

                printer.indent().pln("__rt::checkStore(" + leftIdentifier + ", " + tmpVarName + ");");


                List<Node> fieldDeclarations = NodeUtil.dfsAll(main, "FieldDeclaration");

                for (Node m : fieldDeclarations) {

                    if (leftIdentifier.equals(m.getGeneric(2).getGeneric(0).getString(0))) {
                        String tmpLeftType = m.getGeneric(1).getGeneric(0).getString(0);

                        if (!tmpLeftType.equals(type)) {
                            leftType = tmpLeftType;
                            cast = true;
                        }
                    }
                }


                if (cast) {
                    rightSide = "(" + leftType + ") " + tmpVarName;
                } else {
                    rightSide = tmpVarName;
                }
            } else if (right.hasName("IntegerLiteral")) {
                rightSide = right.getString(0);
            } else if (right.hasName("FloatingPointLiteral")){
                rightSide = AstUtil.parseFloatingPointLiteral(right);
            } else if (right.hasName("AdditiveExpression")) {
                rightSide = right.getGeneric(0).getString(0) + " " + right.getString(1) + " " + right.getGeneric(2).getString(0);
            }
            //else if(right.hasName("AdditiveExpression")){
            //    translatedString += AstUtil.parseAdditiveExpression(right);
            //}
            else {
                System.out.println("New right operand type found in visitExpression in CppMainPrinter");
            }
        }

        translatedString = leftSide + " " + assignment + " " + rightSide + ";";

        printer.indent().pln(translatedString);


        /*
        ArrayList<String> expression = AstUtil.getExpression(n);

        printer.indent();

        for(int i = 0; i < expression.size(); i++){
            if (expression.get)

            printer.p(expression.get(i));
            if(i < expression.size() - 1){
                printer.p(" ");
            }
        }
        printer.pln(";");
        */

    }

    public void visitFieldDeclaration(GNode n) {
        GNode child = n;
        printer.indent().pln(constructorDec(child));
        //printer.indent().pln(initDec(child));
    }

    //------------------------------------------------------------

    /**
     * Converts a java println statement to c++ equivalent
     *
     * @param args The arguments to be printed
     * @return The translated c++ string
     */
    /*
        public String cout(GNode args) {

            String translated = "cout << ";

            for(int i = 0; i < args.size(); i++){
                try{
                    GNode child = args.getGeneric(i);

                }
                //argument not a GNode, already a string so just print it
                catch(Exception e){

                }

            }

            translated += ";";

            return translated;
        }*/

    public String cout(GNode args) {

        String translatedString = "";

        //get rid of for loop for now, haven't seen Arguments with size greater than 2
        //for (int i = 0; i < args.size(); i++){
        GNode child = args.getGeneric(0);

        //check for all the possible kinds of nodes
        if (child.isGeneric()) {
            //if it is a string literal, we shouldn't append a method on it
            if (child.hasName("PrimaryIdentifier") || child.hasName("StringLiteral")) {
                translatedString += child.getString(0);
            }
            else if (child.hasName("CallExpression")) {
                if (child.getGeneric(0).hasName("PrimaryIdentifier")) {

                    String name = child.getGeneric(0).getString(0);

                    boolean isStatic = isItStatic(name);

                    if (isStatic) {
                        translatedString += "__" + AstUtil.parsePrimaryIdentifier(child.getGeneric(0)) +
                                            "::" +
                                            child.getString(2) +
                                            "(";

                        for (int i = 0; i < child.getGeneric(3).size(); i++) {
                            translatedString += child.getGeneric(3).getString(i);

                            if (!(i + 1 >= child.getGeneric(3).size())) {
                                translatedString += " ,";
                            }
                        }
                        //System.out.println("In cout in CppMainPrinter, there might be something else besides PrimaryIdentifier");
                        translatedString += ")";
                    }
                    else {
                        translatedString += AstUtil.parseCallExpression(child);
                    }
                }
                else if (child.getGeneric(0).hasName("CastExpression")){
                    translatedString += AstUtil.parseCallExpression(child);

                    if (translatedString.contains("data")){
                        printer.indent().pln("__rt::checkIndex(" + child.getGeneric(0).getGeneric(1).getGeneric(0).getString(0)
                                                     + ", " + child.getGeneric(0).getGeneric(1).getGeneric(1).getString(0) + ");");
                    }
                }
                else {
                    translatedString += AstUtil.parseCallExpression(child);
                }
            } else if (child.hasName("SelectionExpression")) {

                //check for primary identifier so we can see if its a static variable
                if (child.getGeneric(0).hasName("PrimaryIdentifier")) {

                    String name = child.getGeneric(0).getString(0);                     //name of primary identifier

                    boolean isStatic = isItStatic(name);

                    //if this is a static call, do a separate implementation of parseSelectionExpression
                    if (isStatic) {
                        translatedString += "__" + AstUtil.parsePrimaryIdentifier(child.getGeneric(0)) +     //the class object
                                            "::" +                                                         //dereference then dot operator
                                            child.getString(1);                              //the field? of the object
                    }
                    //otherwise, just do it normally
                    else {
                        translatedString += AstUtil.parseSelectionExpression(child);
                    }

                }
                //
                else {
                    //System.out.println("In cout in CppMainPrinter, there might be something else besides PrimaryIdentifier");
                    translatedString += AstUtil.parseSelectionExpression(child);
                }
            }
            //signifies an array
            else if (child.hasName("SubscriptExpression")) {

                boolean isArgs = false;
                String identifier = "";
                String index = "";

                //possible first nodes in subscript expression
                if (child.getGeneric(0).hasName("PrimaryIdentifier")) {
                    identifier = child.getGeneric(0).getString(0);
                } else {
                    System.out.println("found new first node in subscript expression in cout CppMainPrinter");
                }

                if (identifier.equals("args")) {
                    isArgs = true;
                }

                //possible second nodes in subscript expression
                if (child.getGeneric(1).hasName("IntegerLiteral") || child.getGeneric(1).hasName("PrimaryIdentifier")) {
                    index = child.getGeneric(1).getString(0);
                } else {
                    System.out.println("found new second node in subscript expression in cout CppMainPrinter");
                }

                if (isArgs) {
                    translatedString += identifier + "[" + index + "]";
                } else {
                    translatedString += identifier + "->__data[" + index + "]";
                }

            } else if (child.hasName("CallExpression")) {
                visitCallExpression(child);
            }
        }
        //}

        //second/last argument is "endl"
        translatedString += " << " + args.getString(args.size()-1) + ";";

        /*
                // Translate each argument
                for (int i = 0; i < args.size(); i++) {

                    // Try getting a GNode
                    try {
                        GNode child = args.getGeneric(i);

                        // Check for literal string
                        if (child.hasName("StringLiteral")) {
                            translatedString += child.get(0);
                        }
                        if (child.hasName("SelectionExpression")){
                            translatedString += child.getGeneric(0).get(0)+ " -> " +child.get(1) + " -> data";
                        }
                        if (child.hasName("CallExpression")) {
                            try {
                                GNode cc=child.getGeneric(0);
                                if (cc.hasName("PrimaryIdentifier")) {
                                    translatedString+=cc.get(0)+" ->__vptr-> "+child.get(2)+"("+cc.get(0)+") -> data";
                                }

                                else {
                                    String __this = child.getGeneric(0).getGeneric(0).getString(0);
                                    translatedString += __this;
                                    for(int j = 1; j < child.getGeneric(0).size(); j++){
                                        if(child.getGeneric(0).get(j) != null){
                                            translatedString += " -> " + child.getGeneric(0).get(j);
                                            break;
                                        }
                                    }
                                    translatedString += " -> __vptr -> " + child.get(2) + "(" + __this + ") -> data";
                                }
                            } catch(Exception e) {

                            }
                        }
                        if(child.hasName("PrimaryIdentifier")){
                            translatedString += child.getString(0);
                        }
                    } catch (Exception e) {
                        // It is not a generic so we just print concatenate it normally
                        if (args.get(i)!=null){
                            translatedString += args.get(i);
                        }
                    }
                    // Print extraction operator unless its the last arg
                    if (i != args.size() - 1)
                        translatedString += " << ";
                }
                // Add semicolon
                translatedString += ";";
        */

        return translatedString;
    }

    private boolean isItStatic(String methodName) {
        List<Node> classes = NodeUtil.dfsAll(root, "ClassDeclaration");     //all class declarations
        boolean isStatic = false;                                           //boolean to keep track of static

        //go through all class declarations and see if the name is a class name
        for (Node n : classes) {
            //if it is, we're doing a static call
            if (n.getString(1).equals(methodName)) {
                isStatic = true;
            }
        }

        return isStatic;
    }



    public String fout(GNode args) {
        String translatedString = "";

        //SymbolTable table = Translator.mutatedSymbolTable;

        //table.enter("method(printOther)(A other)");

        //Object o = table.current().lookupLocally("other");

        //System.out.println(o.toString());
        //table.setScope(table.root());

        //Iterator<String> s = table.root().nested();

        //String s = JavaEntities.methodSymbolFromAst(args);

        //Object s = table.lookupScope());

        //table.setScope(table.root());
        //table.setScope(table.getScope("a"));
        //table.enter("");
        //JavaEntities.enterScopeByQualifiedName(table, "printOther");
        /*
                for (Iterator<String> iter = table.current().nested(); iter.hasNext(); ) {

                    String s = iter.next();
                    System.out.println(s);

                    Object o = table.current().lookupLocally(s);
                    boolean b = (null == o);
                    //System.out.println(b);

                    SymbolTable.Scope nestedScope = table.current().getNested(s);
                    List<VariableT> t = SymbolTableUtil.extractFormalParams(nestedScope);
                    String z = t.get(0).getName();
                    //Tag x = t.get(0).getType().tag();
                    System.out.println("name of param is " + z);
                    //System.out.println("type of param is " + x);
                    table.enter(s);
                    //table.enter("A");

                    for (Iterator<String> iter2 = table.current().symbols(); iter2.hasNext(); ){
                        String a = iter2.next();
                        System.out.println(a);
                    }
                    table.exit();
                    /*String nestedScopeName = iter.next();
                    SymbolTable.Scope nestedScope = scope.getNested(nestedScopeName);
                    table.enter(nestedScopeName);
                    try {
                        simple(nestedScope, printer, typePrinter);
                    } catch (Exception e) { }
                    table.exit();

                }
        */
        //System.out.println(Translator.mutatedSymbolTable.current().lookupScope("method(printOther)(A other)"));
        //SymbolTableUtil.printNestedScopes(Translator.mutatedSymbolTable.current());
        //JavaEntities.canonicalName()
        // Translate each argument
        for (int i = 0; i < args.size(); i++) {
            if (args.get(i) instanceof GNode) {
                GNode argument = (GNode) args.getNode(i);

                //check for object name
                if (argument.hasName("PrimaryIdentifier")) {
                    translatedString += AstUtil.parsePrimaryIdentifier(argument) + "->__vptr";
                }
                //check for method arguments
                else if (argument.hasName("Arguments")) {
                    translatedString += "(" + AstUtil.parseArguments(argument) + ")";
                } else if (argument.hasName("CastExpression")) {
                    String type = argument.getGeneric(0).getGeneric(0).getString(0);
                    String identifier = argument.getGeneric(1).getGeneric(0).getString(0);
                    String subscript = argument.getGeneric(1).getGeneric(1).getString(0);


                    translatedString += "(" + type + ") " + identifier + "->__data[" + subscript + "]";
                }
            } else if (args.get(i) instanceof String) {
                //most likely the method name
                translatedString += "->" + args.getString(i);
            }
        }

        // Add semicolon
        translatedString += ";";

        return translatedString;
    }

    /**
     * Prints the first line for constructor declarations
     *
     * @param args
     * @return
     */
    public String constructorDec(GNode fieldDec) {
        GNode type;
        GNode declarators;
        GNode declarator;
        GNode newClassExpression = GNode.create("NewClassExpression");      //do this for now
        GNode arguments;
        GNode argument;
        String dimensions = "";

        boolean isArray = false;
        boolean hasNewClassExpression = false;
        int arraySize=0;

        String thisName = "";
        String leftTypeName = "";
        String rightTypeName = "";

        String array= "";
        String constructorLine = "";

        //get the Type node
        type = (GNode) NodeUtil.dfs(fieldDec, "Type");

        //set the type
        leftTypeName += type.getGeneric(0).getString(0);

        //this means it's probably an array
        if (null != type.get(1)) {
            dimensions = type.getGeneric(1).getString(0);

            //if the dimensions is an array dimension, then add it
            //      i.e. int becomes int[]
            if (dimensions.equals("[")) {
                leftTypeName = AstUtil.convertToCppArray(leftTypeName);
            }
        }

        //add the type name to the line
        constructorLine += leftTypeName;


        //get the Declarators node
        declarators = (GNode) NodeUtil.dfs(fieldDec, "Declarators");

        //the declarator containing the class declaration
        declarator = declarators.getGeneric(0);

        //the name of "this"
        thisName += declarator.getString(0);

        //the name we give to the object
        constructorLine += " " + declarator.getString(0);

        //null at declarator.getGeneric(1)

        if (null != declarator.getGeneric(2)) {
            //if there is a newClassExpression node present
            if (declarator.getGeneric(2).hasName("NewClassExpression")) {
                //there are nulls in getGeneric(0) and getGeneric(1) in new class expression

                newClassExpression = declarator.getGeneric(2);
                hasNewClassExpression = true;

                //get the qualified identifier type
                constructorLine += " = new __" + newClassExpression.getGeneric(2).getString(0) + "()";
            } else if (declarator.getGeneric(2).hasName("PrimaryIdentifier")) {
                constructorLine += " = " + "(" + leftTypeName + ") " + declarator.getGeneric(2).getString(0);
            } else if (declarator.getGeneric(2).hasName("SelectionExpression")) {
                constructorLine += " = " + AstUtil.parseSelectionExpression(declarator.getGeneric(2)) + ";\n __rt::checkNotNull(" + AstUtil.parseSelectionExpression(declarator.getGeneric(2)) +")";
            } else if (declarator.getGeneric(2).hasName("NewArrayExpression")) {
                isArray = true;

                rightTypeName = declarator.getGeneric(2).getGeneric(0).getString(0);

                rightTypeName = AstUtil.convertToCppArray(rightTypeName);



                if (leftTypeName.equals(rightTypeName)) {
                    //need the new for arrays, as we're instantiating a new object
                    constructorLine += " = new " + AstUtil.parseNewArrayExpression(declarator.getGeneric(2));
                } else {
                    constructorLine += " = " + "(" + leftTypeName + ") new " + AstUtil.parseNewArrayExpression(declarator.getGeneric(2));
                }

                constructorLine = "NegativeArraySizeException::Check_Not_Negative( " + AstUtil.parseNewArrayExpression(declarator.getGeneric(2), 1) +" );\n  " + constructorLine;

            } else if (declarator.getGeneric(2).hasName("IntegerLiteral")) {
                constructorLine += " = " + declarator.getGeneric(2).getString(0);
            }
            //else if(declarator.getGeneric(2).hasName("IntegerLiteral")){
            //    constructorLine += " = " + AstUtil.parseIntegerLiteral(declarator.getGeneric(2));
            //}
            else if (declarator.getGeneric(2).hasName("FloatingPointLiteral")){
                constructorLine += " = " + AstUtil.parseFloatingPointLiteral(declarator.getGeneric(2));
            }
            else {
                System.out.println("found new field declarator in main");
            }
        }

        constructorLine += ";\n  ";



        //next two lines for Array declarations

        if (isArray) {
            constructorLine += "__rt::checkNotNull(" + thisName + ");\n";

            Node dimensionSize = NodeUtil.dfs(declarator, "ConcreteDimensions");

            int dim = Integer.parseInt(dimensionSize.getNode(0).getString(0));
            dim -= 1;

            //System.out.println(dimensionSize);
            constructorLine += "  __rt::checkIndex(" + thisName + ", " + dim + ");\n";
        }




        //next line for constructors

        if (hasNewClassExpression) {
            //
            constructorLine += thisName + "->__init(" + thisName;

            //
            arguments = (GNode) NodeUtil.dfs(newClassExpression, "Arguments");

            if (arguments != null) {
                for (int j = 0; j < arguments.size(); j++) {
                    argument = arguments.getGeneric(j);

                    if (argument.hasName("StringLiteral")) {
                        constructorLine += ", new __String(" + argument.getString(0) + ")";
                    } else if (argument.hasName("IntegerLiteral")) {
                        constructorLine += ", " + argument.getString(0);
                    } else if (argument.hasName("FloatingPointLiteral")){
                        constructorLine += ", " + AstUtil.parseFloatingPointLiteral(argument);
                    }
                }
            }
            constructorLine += ");";
        }

        return constructorLine;
    }

    //visits a for loop
    public void visitForStatement(GNode forStatement) {
        printer.indent().pln(AstUtil.parseForStatement(forStatement)).incr();
        printer.incr().pln();
        visit(forStatement);

        printer.decr();

        printer.indent().pln("}");
        printer.pln();
    }

    //visits a while loop
    public void visitWhileStatement(GNode whileStatement) {
        printer.indent().pln(AstUtil.parseWhileStatement(whileStatement)).incr();
        visit(whileStatement);
        printer.decr().indent().pln().indent().pln("}\n").decr().incr();

    }



    /**
     * Prints the second line of constructor declarations (using __init)
     *
     * @param args
     * @return
     */
    /*
        public String initDec(GNode fieldDec) {

            String cast = "";
            String castee="";
            for (int i = 0; i < args.size(); i++) {

                try {
                    GNode child = args.getGeneric(i);

                    if (child.hasName("Type")) {
                        try {
                            GNode typeChild = child.getGeneric(0);
                            cast += typeChild.get(0);
                            castee+=typeChild.get(0);
                        } catch (Exception e) {
                            cast += child.get(0);
                            castee+=child.get(0);
                        }

                    }

                    if (child.hasName("Declarators")) {
                        try {
                            GNode childschild = child.getGeneric(0);
                            if (childschild.hasName("Declarator")) {
                                cast += " " + childschild.get(0);
                                try {
                                    GNode ccc= childschild.getGeneric(2);
                                    if(ccc.hasName("PrimaryIdentifier")) {
                                        cast+= " = ("+castee+") "+ccc.get(0);
                                    }
                                    if(ccc.hasName("NewClassExpression")) {
                                        try {
                                            GNode classType = newClassExpression.getGeneric(2);
                                            cast += " = new __" + classType.get(0) + "()";
                                            arguments = newClassExpression.getGeneric(3);
                                            if (arguments.hasName("Arguments")) {
                                                try {
                                                    GNode ccccc = cccc.getGeneric(0);

                                                    //This block prints the arguments for the constructor
                                                    if (ccccc.hasName("StringLiteral")) {
                                                        /*
                                                        This inserts new __String("A")) into the constructor like below

                                                                A a = new __A(new __String ("A"));
                                                         */

    /*
    cast+="new __String (";
    try {
        GNode cccccc = ccccc.getGeneric(0);
        cast += cccccc.get(0);
    } catch (Exception e) {
        cast += ccccc.get(0);
    }
    cast+=")";
    */

    /*
                                                    }
                                                    else if (ccccc.hasName("IntegerLiteral")) {
                                                        cast += ccccc.get(0).toString();
                                                    }
                                                } catch (Exception e) {
                                                }
                                            }
                                        } catch(Exception e) {
                                            cast+=ccc.get(0);
                                        }
                                        //cast+=")";
                                    }
                                } catch(Exception e) {
                                }
                            }
                        } catch (Exception e) {
                            // It is not a generic so we just print concatenate it normally
                            cast += child.get(i);
                        }


                    }
                } catch (Exception e) {
                    // It is not a generic so we just print concatenate it normally
                    cast += args.get(i);
                }

            }
            cast += ";";
            return cast;
        }
    /*
        //fieldDec using if/else statements
        public String fieldDec(GNode args) {
            String cast = "";
            String castee="";
            for (int i = 0; i < args.size(); i++) {
                if (args.getGeneric(i) instanceof GNode) {
                    GNode child = args.getGeneric(i);
                    if (child.hasName("Type")) {
                        if(child.size() > 0) {
                            if (child.getGeneric(0) instanceof GNode) {
                                GNode typeChild = child.getGeneric(0);
                                cast += typeChild.get(0);
                                castee += typeChild.get(0);
                            } else {
                                cast += child.get(0);
                                castee += child.get(0);
                            }
                        }
                    }
                    if (child.hasName("Declarators")) {
                        if(child.size() > 0){
                            if (child.getGeneric(0) instanceof GNode) {
                                GNode childschild = child.getGeneric(0);
                                if (childschild.hasName("Declarator")) {
                                    cast += " " + childschild.get(0);
                                    if(childschild.size() > 2) {
                                        if (childschild.getGeneric(2) instanceof GNode) {
                                            GNode ccc = childschild.getGeneric(2);
                                            if (ccc.hasName("PrimaryIdentifier")) {
                                                cast += " = (" + castee + ") " + ccc.get(0);
                                            }
                                            else if (ccc.hasName("NewClassExpression")) {
                                                if(ccc.size() > 3){
                                                    if (ccc.getGeneric(2) instanceof GNode) {
                                                        GNode cccc = ccc.getGeneric(2);
                                                        cast += " = new __" + cccc.get(0) + "(";
                                                        cccc = ccc.getGeneric(3);
                                                        if (cccc.hasName("Arguments")) {
                                                            if(cccc.size() > 0){
                                                                if (cccc.getGeneric(0) instanceof GNode) {
                                                                    GNode ccccc = cccc.getGeneric(0);
                                                                    if (ccccc.hasName("StringLiteral")) {
                                                                        cast += "new __String (";
                                                                        if(ccccc.size() > 0){
                                                                            if (ccccc.getGeneric(0) instanceof GNode) {
                                                                                GNode cccccc = ccccc.getGeneric(0);
                                                                                if(cccccc.size() > 0){
                                                                                    cast += cccccc.get(0);
                                                                                }
                                                                            }
                                                                            else {
                                                                                cast += ccccc.get(0);
                                                                            }
                                                                            cast += ")";
                                                                        }
                                                                    }
                                                                    else if (ccccc.hasName("IntegerLiteral")) {
                                                                        if(ccccc.size() > 0){
                                                                            cast += ccccc.get(0).toString();
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                    else {
                                                        cast += ccc.get(0);
                                                    }
                                                    cast += ")";
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            else {
                                cast += child.get(i);
                            }
                        }
                    }
                }
                else {
                    cast += args.get(i);
                }
            }
            cast += ";";
            return cast;
        }
    */

    public String parseArguments(GNode arguments, String methodName) {

        // local variables

        List<VariableT> formalParameters = symbolTable.getFormalParameters(methodName);
        ListIterator<VariableT> iter = formalParameters.listIterator();
        boolean processedThis = false;  // did we process the __this parameter?


        String parsedString = "";
        boolean reqcast=false;

        for (int i = 0; i < arguments.size(); i++) {
            //for just Strings, we can add them directly
            if (arguments.get(i) instanceof String) {
                parsedString += arguments.getString(i);
            }
            //GNodes generally should be StringLiteral or IntegerLiteral
            else if (arguments.get(i) instanceof GNode) {
                if (arguments.getNode(i).hasName("StringLiteral")) {
                    //create a new java.lang.String object
                    parsedString += "new __String(" + arguments.getNode(i).getString(0) + ")";
                } else if (arguments.getNode(i).hasName("IntegerLiteral")) {
                    parsedString += arguments.getNode(i).getString(0);
                } else if (arguments.getGeneric(i).hasName("FloatingPointLiteral")){
                    parsedString += AstUtil.parseFloatingPointLiteral(arguments.getGeneric(i));
                }
                else if (arguments.getNode(i).hasName("PrimaryIdentifier")) {
                    if(reqcast) {
                        //check if cast actually needed
                        //lookup argument type expected from symbol table


                    }

                    // we're going to cast EVERY thing

                    VariableT param = iter.next();
                    String type = param.getType().getName();

                    parsedString += "(" + type + ") ";



                    parsedString += arguments.getNode(i).getString(0);
                }
            }
            //other types possible???
            else {
                System.out.println("Found a new type in Arguments() in parseArguments of AstUtil");
            }

            //parsedString = CppFilePrinter.checkLocalOrThis(parsedString);


            //if it's not the last argument, print a comma
            if ((i + 1) != arguments.size()) {
                parsedString += ", ";
                reqcast=true;
            }
        }

        return parsedString;
    }

    //------------------------------------------------------------------------
    // Accessors

    /**
     * Access the main method node
     *
     * @return The main method node
     */
    public GNode getMain() {
        return main;
    }
}