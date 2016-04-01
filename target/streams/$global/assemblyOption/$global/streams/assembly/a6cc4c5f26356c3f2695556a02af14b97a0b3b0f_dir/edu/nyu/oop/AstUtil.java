package edu.nyu.oop;

import java.util.List;
import java.util.ArrayList;
import java.io.*;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.lang.RuntimeException;

import edu.nyu.oop.util.*;

import org.slf4j.Logger;
import xtc.*;
import xtc.lang.cpp.MacroTable;
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



public class AstUtil {

    public static String getClassName(GNode n) {
        if (n.getName().equals("HeaderDeclaration")) {
            return n.getString(0);
        } else {
            throw new RuntimeException("Used getClassName on an unsupported node");
        }
    }

    public static ArrayList<String> getModifiers(GNode n) {
        if (n.getName().equals("FieldDeclaration") || n.getName().equals("DLFunctionDeclaration") ||
                n.getName().equals("VTFunctionDeclaration")) {
            ArrayList<String> modifiers = new ArrayList<String>();
            for (Object o : n.getNode(0)) {
                modifiers.add((String) o);
            }
            return modifiers;
        } else {
            throw new RuntimeException("Used getModifiers on an unsupported node");
        }
    }

    public static ArrayList<String> getConstructorArguments(GNode n) {
        if (n.getName().equals("ConstructorDeclaration")) {
            ArrayList<String> arguments = new ArrayList<String>();
            for (Object o : n.getNode(1)) {
                arguments.add((String) o);
            }
            return arguments;
        } else {
            throw new RuntimeException("Used getConstructorArguments on an unsupported node");
        }
    }

    public static ArrayList<String> getArguments(GNode n) {
        ArrayList<String> arguments = new ArrayList<String>();
        int i = 0;      //counter variable so we know to replace only the first argument with "this"
        if (n.getName().equals("DLFunctionDeclaration")) {
            for (Object o : n.getNode(3)) {
                //if (i == 0){
                //arguments.add(n.getName())
                //}
                //else{
                arguments.add((String) o);
                //}
            }
            return arguments;
        } else if (n.getName().equals("VTFunctionDeclaration")) {
            for (Object o : n.getNode(4)) {
                arguments.add((String) o);
            }
            return arguments;
        } else {
            throw new RuntimeException("Used getArguments on an unsupported node");
        }
    }

    public static String getType(GNode n) {
        if (n.getName().equals("FieldDeclaration") || n.getName().equals("DLFunctionDeclaration") ||
                n.getName().equals("VTFunctionDeclaration")) {
            if (n.getString(1) == null) {
                return "void";
            } else {
                return n.getString(1);
            }
        } else {
            throw new RuntimeException("Used getModifiers on an unsupported node");
        }
    }

    public static String getName(GNode n) {
        if (n.getName().equals("FieldDeclaration") || n.getName().equals("DLFunctionDeclaration") ||
                n.getName().equals("VTFunctionDeclaration")) {
            return n.getString(2);
        } else if (n.getName().equals("ConstructorDeclaration")) {
            return n.getString(0);
        } else {
            throw new RuntimeException("Used getModifiers on an unsupported node");
        }
    }

    public static String getOwnerName(GNode n) {
        if (n.getName().equals("VTFunctionDeclaration")) {
            return n.getString(3);
        } else {
            throw new RuntimeException("Used getParent on an unsupported node");
        }
    }

    public static GNode getOwnerVTable(GNode headerAst, String ownerName) {
        //first find the class node with ownerName
        GNode owner = (GNode) NodeUtil.dfs(headerAst, ownerName);

        //then find the vTable within the owner GNode
        GNode ownerVTable = (GNode) owner.getNode(0).getNode(2);

        return ownerVTable;
    }

    /**
     * Finds all descendents which are of type String
     *
     * @param n - GNode to search for all String descendents
     * @return ArrayList of String types that will hold an expression
     */
    public static ArrayList<String> getExpression(GNode n) {

        ArrayList<String> returnExpression = new ArrayList<String>();

        for (int i = 0; i < n.size(); i++) {
            try {
                if (n.getGeneric(i).hasName("PrimaryIdentifier")) {

                    returnExpression.add(n.getGeneric(i).get(0).toString() + " ");

                } else if (n.getGeneric(i).hasName("IntegerLiteral")) {

                    returnExpression.add(n.getGeneric(i).get(0).toString() + " ");

                }
            }
            //child is String so just add it to the returnExpression ArrayList
            catch (Exception e) {
                returnExpression.add(n.get(i).toString() + " ");
            }

        }

        return returnExpression;
    }

    /**
     * Determines whether or not this class contains the main method
     *
     * @param classDeclaration The classDeclaration node
     * @return True if Class contains main function otherwise False
     */
    public static boolean isMainClass(GNode classDeclaration) {

        // First we must get the ClassBody
        GNode classBody = classDeclaration.getGeneric(5);

        // We are guranteed that the main function class will only contain
        // the main function.  So we only need to check the first index
        if(classBody.size() > 0) {
            if (classBody.getGeneric(0).hasName("MethodDeclaration")) {
                GNode methodDeclaration = classBody.getGeneric(0);

                // Check method name
                if (methodDeclaration.get(3).equals("main"))
                    return true;
            }
        }

        return false;
    }

    /**
     * Determines whether or not the expression is an assignment
     *
     * @param expression The expression node
     * @return True if the expression is an assignement otherwise false
     */
    public static boolean isAssignmentExpression(GNode expression) {
        if (null != expression.get(1))
            return expression.get(1).toString().equals("=");
        else
            return false;
    }

    /**
     * Gets the variable type from the Type node
     * @param type The GNode containing the variable type
     * @return The variable type as a string
     */
    public static String getVarType(GNode type) {

        return type.getGeneric(0).get(0).toString();
    }


    //========================================================================================================
    //Only use these with the mutated AST

    /**
     *
     */
    public static String parseCallExpression(GNode callExpression) {
        String parsedString = "";
        String __this;
        //hardcode "this" for now

        if (callExpression.getNode(0).hasName("PrimaryIdentifier")) {
            __this = callExpression.getNode(0).getString(0);
        } else if (callExpression.getNode(0).hasName("SelectionExpression")) {
            __this = parseSelectionExpression((GNode) callExpression.getNode(0));
        }

        //expression is the callExpression node

        //parse the information in the first child -> can be primary identifier, selection expression, call expression
        if (callExpression.getNode(0) != null) {
            GNode firstChild = callExpression.getGeneric(0);

            /*
            todo
                can we do this with new Visitor?
             */

            if (firstChild.hasName("PrimaryIdentifier")) {
                //could there be a primary identifier without a method name in the third child?
                //added ->__vptr here just in case
                parsedString += parsePrimaryIdentifier(firstChild) + "->__vptr";
            } else if (firstChild.hasName("SelectionExpression")) {
                parsedString += parseSelectionExpression(firstChild) + "->__vptr";
            } else if (firstChild.hasName("CallExpression")) {
                parsedString += parseCallExpression(firstChild);
            } else if (firstChild.hasName("CastExpression")) {
                String type = firstChild.getGeneric(0).getGeneric(0).getString(0);
                String identifier = firstChild.getGeneric(1).getGeneric(0).getString(0);
                String subscript = firstChild.getGeneric(1).getGeneric(1).getString(0);


                parsedString += "((" + type + ") " + identifier + "->__data[" + subscript + "])->__vptr";
            } else {
                //throw an error
                System.out.println("We found a new node type in parseCallExpression");
            }
        }

        //we don't know what the second node is
        if (callExpression.getNode(1) != null) {
            //throw an error
            //we don't know what this does yet
            System.out.println("We know what the second callExpression node does now");
        }

        //the third child is just the method name
        if (callExpression.getString(2) != null) {
            parsedString += "->" + callExpression.getString(2);
        }


        /*shit code
        ***
        ***
         */
        GNode arguments = (GNode) callExpression.getNode(3);
        GNode newArguments = GNode.create("Arguments");

        for (Object o : arguments) {
            newArguments.add(o);
        }

        if (callExpression.getNode(0).hasName("SelectionExpression")) {
            newArguments.add(parseSelectionExpression((GNode) callExpression.getNode(0)));
        }

        callExpression.set(3, newArguments);
        //***********************//


        //the fourth child is the arguments node
        if (callExpression.getNode(3) != null) {
            parsedString += "(" + parseArguments((GNode) callExpression.getNode(3)) + ")";
        }


        return parsedString;
    }

    //
    public static String parsePrimaryIdentifier(GNode primaryIdentifier) {
        return primaryIdentifier.getString(0);
    }

    public static String parseSelectionExpression(GNode selectionExpression) {
        //assuming that selection expression only has 2 nodes ever
        //TODO change this -> to :: for static references
        return parsePrimaryIdentifier(selectionExpression.getGeneric(0)) +    //the class object
               "->" +                                                         //dereference then dot operator
               selectionExpression.getString(1);                              //the field? of the object
    }

    public static String parseIntegerLiteral(GNode integerLiteral) {
        return integerLiteral.getString(0);
    }

    public static String parseFloatingPointLiteral(GNode floatingLiteral){
        return floatingLiteral.getString(0);
    }

    public static String parseCastExpression(GNode CastExpression) {
        String B = CastExpression.getGeneric(0).getGeneric(0).getString(0);
        String a = CastExpression.getGeneric(1).getString(0);
        //(B) a
        String returnThis = "(" + B + ") " + a;
        return returnThis;

    }

    public static String parseNewArrayExpression(GNode NewArrayExpression, int retType) {
        //returns full expression if retType=0, size if retType=1
        String A= NewArrayExpression.getGeneric(0).getString(0);
        String size="";
        if (NewArrayExpression.getGeneric(1).hasName("ConcreteDimensions")) {
            if(NewArrayExpression.getGeneric(1).getGeneric(0).hasName("UnaryExpression")) {
                size+=NewArrayExpression.getGeneric(1).getGeneric(0).getString(0);
                size+=NewArrayExpression.getGeneric(1).getGeneric(0).getGeneric(1).getString(0);
            } else if (NewArrayExpression.getGeneric(1).getGeneric(0).hasName("IntegerLiteral")) {
                size+=NewArrayExpression.getGeneric(1).getGeneric(0).getString(0);
            }
        }
        if (retType==0) {
            return (A + "[" + size + "]");
        } else if (retType==1) {
            return size;
        }
        return "0";
    }

    /*public static String getArraySize(GNode NewArrayExpression){
        String size="";
        if (NewArrayExpression.getGeneric(1).hasName("ConcreteDimensions")){
            if(NewArrayExpression.getGeneric(1).getGeneric(0).hasName("UnaryExpression")){
                size+=NewArrayExpression.getGeneric(1).getGeneric(0).getString(0);
                size+=NewArrayExpression.getGeneric(1).getGeneric(0).getGeneric(1).getString(0);
            }
            else if (NewArrayExpression.getGeneric(1).getGeneric(0).hasName("IntegerLiteral")) {
                size+=NewArrayExpression.getGeneric(1).getGeneric(0).getGeneric(0);
            }
        }
    }*/


    public static String parseArguments(GNode arguments) {

        // local variables
        //List<VariableT> formalParameters;

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
                } else if (arguments.getNode(i).hasName("PrimaryIdentifier")) {
                    if(reqcast) {
                        //check if cast actually needed
                        //lookup argument type expected from symbol table
                        // we're going to cast EVERY thing

                    }
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

    /**
     * Determines whether or not a field declaration has a declaration.
     * "String fld" would return false
     * "String fld = "A"" would return true
     * @param fieldDeclaration The field declaration node
     * @return True if field contains declarator otherwise False
     */
    public static boolean isFieldDeclarationWithAssignment(GNode fieldDeclaration) {

        // Get declarator
        GNode declarator = fieldDeclaration.getGeneric(2).getGeneric(0);

        // Check if a value is assigned to field declaration
        return (null == declarator.get(2)) ? false : true;
    }

    /**
     * converts a Java array to a translator C++ array
     *      i.e. int[] -> __rt::Array<int32_t>*
     *
     * @param the string of the type of the Java array
     * @return the string containing the converted translator C++ array
     */
    public static String convertToCppArray(String typeName) {
        String returnArray = "";

        if (typeName.equals("int")) {
            returnArray = "__rt::Array<int32_t>*";
        } else if (typeName.equals("float")) {
            returnArray = "__rt::Array<float>*";
        } else {
            returnArray = "__rt::Array<" + typeName + ">*";
        }

        return returnArray;
    }

    /**
     * parses a NewArrayExpression node
     *
     * @param newArrayExpression
     * @return a string containing the parsed array expression
     */
    public static String parseNewArrayExpression(GNode newArrayExpression) {
        String arrayType = "";          //holds the type (__rt::Array(int32_t>)
        String arrayDimension = "";

        //go through each node in the NewArrayExpression
        for (int i = 0; i < newArrayExpression.size(); i++) {

            //if it is a GNode
            if (newArrayExpression.get(i) instanceof GNode) {
                GNode arrayNode = newArrayExpression.getGeneric(i);

                //extract the primitive type of the array
                if (arrayNode.hasName("PrimitiveType") || arrayNode.hasName("QualifiedIdentifier")) {
                    arrayType = convertToCppArray(arrayNode.getString(0));

                    //convertToCppArray adds a "*", we need to delete it
                    arrayType = arrayType.substring(0, arrayType.length()-1);
                }
                //extract the size of the array
                else if (arrayNode.hasName("ConcreteDimensions")) {
                    if (arrayNode.getNode(0).hasName("IntegerLiteral")) {
                        //for integer literal of 2, you get (2)
                        arrayDimension = "(" + arrayNode.getNode(0).getString(0) + ")";
                    }
                } else {
                    System.out.println("new array node in parseNewArrayExpression");
                }
            } else if (null != newArrayExpression.get(i)) {
                System.out.println("found non-GNode in parseNewArrayExpression");
            }
        }

        return (arrayType + arrayDimension);
    }

    /**
     * parses the for control
     *
     * @param forStatement The GNode that is the ForStatement in the phase 4 ast
     * @return the full parsed for statement
     */
    public static String parseForStatement(GNode forStatement) {
        String parsedForStatement = "for(";

        GNode forControl = forStatement.getGeneric(0);
        GNode relationStatement = forControl.getGeneric(3);
        //count declaration section of for statement
        GNode type = forControl.getGeneric(1);
        parsedForStatement += type.getGeneric(0).getString(0) + " ";
        GNode declarators = forControl.getGeneric(2);
        if (relationStatement.getGeneric(2).getGeneric(0).getString(0).equals("args")) {
            int dec = Integer.valueOf(declarators.getGeneric(0).getGeneric(2).getString(0));;
            parsedForStatement += declarators.getGeneric(0).getString(0) + " = " + (dec + 1);
        } else {
            parsedForStatement += parseDeclarator(declarators.getGeneric(0));
        }
        parsedForStatement += "; ";

        //compare section of for statement
        parsedForStatement += parseRelationalExpression(relationStatement);
        parsedForStatement += "; ";

        //increment section of for statement
        GNode expressionList = forControl.getGeneric(4);
        parsedForStatement += parseExpressionList(expressionList);
        parsedForStatement += "){";

        return parsedForStatement;
    }

    public static String parseWhileStatement(GNode whileStatement) {
        String parsedWhileStatement = "while(";

        GNode relationStatement = whileStatement.getGeneric(0);
        parsedWhileStatement += parseRelationalExpression(relationStatement);

        parsedWhileStatement += "){\n";

        return parsedWhileStatement;
    }

    /**
     * parses a relationalExpression
     * TODO: support && and || ... right now it only expects a left side, operator, and right side
     *
     * @param relationalExpression the GNode that is a RelationalExpression
     * @return the parsed relational expression
     */
    public static String parseRelationalExpression(GNode relationalExpression) {
        String parsedRelationalExpression = "";

        GNode leftOperand = relationalExpression.getGeneric(0);
        String operator = relationalExpression.getString(1);
        GNode rightOperand = relationalExpression.getGeneric(2);



        if(leftOperand.getName() == "PrimaryIdentifier") {
            parsedRelationalExpression += parsePrimaryIdentifier(leftOperand);
        } else if(leftOperand.getName() == "SelectionExpression") {
            if (leftOperand.getGeneric(0).getString(0).equals("args")) {
                parsedRelationalExpression += "argc";
            } else {
                parsedRelationalExpression += parseSelectionExpression(leftOperand);
            }
        } else if(leftOperand.getName() == "IntegerLiteral") {
            parsedRelationalExpression += parseIntegerLiteral(leftOperand);
        } else {
            System.out.println("New operand type found in parseRelationalExpression in AstUtil");
        }

        parsedRelationalExpression += " " + operator + " ";

        if(rightOperand.getName() == "PrimaryIdentifier") {
            parsedRelationalExpression += parsePrimaryIdentifier(rightOperand);
        } else if(rightOperand.getName() == "SelectionExpression") {
            if (rightOperand.getGeneric(0).getString(0).equals("args")) {
                parsedRelationalExpression += "argc";
            } else {
                parsedRelationalExpression += parseSelectionExpression(rightOperand);
            }
        } else if(rightOperand.getName() == "IntegerLiteral") {
            parsedRelationalExpression += parseIntegerLiteral(rightOperand);
        } else {
            System.out.println("New operand type found in parseRelationalExpression in AstUtil");
        }

        return parsedRelationalExpression;
    }

    /**
     * parses an expression list
     * WARNING - function made for use with parseForStatement - may not yet work in all cases
     *
     * @param expressionList - the GNode ExpressionList to parse
     * @return - the parsed ExpressionList
     */
    public static String parseExpressionList(GNode expressionList) {
        String parsedExpressionList = "";
        if(expressionList.getGeneric(0).getName() == "PostfixExpression") {
            GNode postfixExpression = expressionList.getGeneric(0);
            parsedExpressionList += parsePrimaryIdentifier(postfixExpression.getGeneric(0));
            parsedExpressionList += postfixExpression.getString(1);
        }
        return parsedExpressionList;

    }

    /**
     * parses a Declarator GNode
     * WARNING - function made for use with parseForExpression - may not yet work in all cases
     *
     * @param declarator
     * @return
     */
    public static String parseDeclarator(GNode declarator) {
        String parsedDeclarator = "";
        parsedDeclarator += declarator.getString(0);
        parsedDeclarator += " = ";
        //this line below assumes assignmnet is to an IntegerLiteral GNode - may break in some later cases
        parsedDeclarator += declarator.getGeneric(2).getString(0);
        return parsedDeclarator;
    }


    /**
     * parses an additive expression
     * only tested with input23 - not 100% of accuracy yet
     *
     * @param additiveExpression the GNode AdditiveExpression to parse
     * @return the parsed additive expression
     */
    public static String parseAdditiveExpression(GNode additiveExpression) {

        String parsedAdditiveExpression = "";

        GNode leftOperand = additiveExpression.getGeneric(0);
        String operator = additiveExpression.getString(1);
        GNode rightOperand = additiveExpression.getGeneric(2);

        if(leftOperand.getName() == "PrimaryIdentifier") {
            parsedAdditiveExpression += parsePrimaryIdentifier(leftOperand);
        } else if(leftOperand.getName() == "IntegerLiteral") {
            parsedAdditiveExpression += parseIntegerLiteral(leftOperand);
        } else {
            System.out.println("New operand type found in AstUtil parseAdditiveExpression");
        }

        parsedAdditiveExpression += " " + operator + " ";

        if(rightOperand.getName() == "PrimaryIdentifier") {
            parsedAdditiveExpression += parsePrimaryIdentifier(rightOperand);
        } else if(rightOperand.getName() == "IntegerLiteral") {
            parsedAdditiveExpression += parseIntegerLiteral(rightOperand);
        } else {
            System.out.println("New operand type found in AstUtil parseAdditiveExpression");
        }

        return parsedAdditiveExpression;

    }

    public static String parseSubscriptExpression(GNode subscriptExpression) {
        String parsedSubscriptExpression = "";

        GNode arrayObjectNode = subscriptExpression.getGeneric(0);
        String arrayObjectString = "";
        GNode indexNode = subscriptExpression.getGeneric(1);
        String indexString = "";

        if(arrayObjectNode.getName() == "PrimaryIdentifier") {
            arrayObjectString += arrayObjectNode.getString(0);
        } else {
            System.out.println("a new type found in parseSubscriptExpression");
        }
        if(indexNode.getName() == "PrimaryIdentifier") {
            indexString += indexNode.getString(0);
        } else {
            System.out.println("a new type found in parseSubscriptExpression");
        }

        parsedSubscriptExpression += arrayObjectString + "[" + indexString + "]";

        return parsedSubscriptExpression;
    }

    //===================================================================================//
    //Bad Stuff Below


    /**
     *  This is temporary, find a better way later
     *      We need to decouple these different parses when FilePrinter and MainPrinter calls it
     */
    public static String parseCallExpression2(GNode callExpression, SymbolTable symbolTable) {
        String parsedString = "";
        String __this;
        //hardcode "this" for now

        if (callExpression.getNode(0).hasName("PrimaryIdentifier")) {
            __this = callExpression.getNode(0).getString(0);
        } else if (callExpression.getNode(0).hasName("SelectionExpression")) {
            __this = parseSelectionExpression((GNode) callExpression.getNode(0));
        }

        //expression is the callExpression node

        //parse the information in the first child -> can be primary identifier, selection expression, call expression
        if (callExpression.getNode(0) != null) {
            GNode firstChild = callExpression.getGeneric(0);

            /*
            todo
                can we do this with new Visitor?
             */

            if (firstChild.hasName("PrimaryIdentifier")) {
                //could there be a primary identifier without a method name in the third child?
                //added ->__vptr here just in case
                parsedString += parsePrimaryIdentifier(firstChild) + "->__vptr";
            } else if (firstChild.hasName("SelectionExpression")) {
                parsedString += parseSelectionExpression(firstChild) + "->__vptr";
            } else if (firstChild.hasName("CallExpression")) {
                parsedString += parseCallExpression(firstChild);
            } else {
                //throw an error
                System.out.println("We found a new node type");
            }
        }

        //we don't know what the second node is
        if (callExpression.getNode(1) != null) {
            //throw an error
            //we don't know what this does yet
            System.out.println("We know what the second callExpression node does now");
        }

        //the third child is just the method name
        if (callExpression.getString(2) != null) {
            parsedString += "->" + callExpression.getString(2);
        }


        /*shit code
        ***
        ***
         */
        GNode arguments = (GNode) callExpression.getNode(3);
        GNode newArguments = GNode.create("Arguments");

        for (Object o : arguments) {
            newArguments.add(o);
        }

        if (callExpression.getNode(0).hasName("SelectionExpression")) {
            newArguments.add(parseSelectionExpression((GNode) callExpression.getNode(0)));
        }

        callExpression.set(3, newArguments);
        //***********************//


        //the fourth child is the arguments node
        if (callExpression.getNode(3) != null) {
            parsedString += "(" + parseArguments2((GNode) callExpression.getNode(3), symbolTable) + ")";
        }


        return parsedString;
    }

    public static String parseArguments2(GNode arguments, SymbolTable symbolTable) {
        String parsedString = "";

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
                } else if (arguments.getNode(i).hasName("PrimaryIdentifier")) {
                    parsedString += arguments.getNode(i).getString(0);
                }
            }
            //other types possible???
            else {
                System.out.println("Found a new type in Arguments() in parseArguments of AstUtil");
            }

            parsedString = checkLocalOrThis(parsedString, symbolTable);


            //if it's not the last argument, print a comma
            if ((i + 1) != arguments.size()) {
                parsedString += ", ";
            }
        }

        return parsedString;
    }

    public static String checkLocalOrThis(String variable, SymbolTable symbolTable) {
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
}