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

import javax.xml.crypto.Data;

public class javalanggenerate {

    //============================ OBJECT HEADER AST GENERATOR ============================//
    public static GNode objectGenerate() {
        GNode object = GNode.create("Object");

        GNode objectHeader = GNode.create("HeaderDeclaration");

        object.addNode(objectHeader);

        //the name of the class
        objectHeader.add("Object");

        //data layout
        objectHeader.addNode(makeObjectDataLayoutNode());

        //vtable
        objectHeader.addNode(makeObjectVTableNode());

        return object;
    }

    //should return a node with the data layout of Object
    private static GNode makeObjectDataLayoutNode() {
        GNode objectDataLayout = GNode.create("DataLayout");

        //2 Field Declarations
        //first field declaration
        //initialize
        GNode oField1 = GNode.create("FieldDeclaration");
        GNode oModifiers1 = GNode.create("Modifiers");
        String oFieldType1 = "__Object_VT*";
        String oFieldName1 = "__vptr";
        GNode oFieldDeclarators1 = GNode.create("Declarators");

        //add
        oField1.addNode(oModifiers1);
        oField1.add(oFieldType1);
        oField1.add(oFieldName1);
        oField1.addNode(oFieldDeclarators1);

        //finish
        objectDataLayout.addNode(oField1);


        //second field declaration
        //initialize
        GNode oField2 = GNode.create("FieldDeclaration");
        GNode oModifiers2 = GNode.create("Modifiers");
        String oFieldType2 = "__Object_VT";
        String oFieldName2 = "__vtable";
        GNode oFieldDeclarators2 = GNode.create("Declarators");

        //modify
        oModifiers2.add("static");

        //add
        oField2.addNode(oModifiers2);
        oField2.add(oFieldType2);
        oField2.add(oFieldName2);
        oField2.addNode(oFieldDeclarators2);

        //finish
        objectDataLayout.addNode(oField2);


        //1 Constructor declaration
        //initialize
        GNode oConstructor = GNode.create("ConstructorDeclaration");
        String oConstructorName = "Object";
        GNode oConstructorParameters = GNode.create("Parameters");

        //add
        oConstructor.add(oConstructorName);
        oConstructor.addNode(oConstructorParameters);

        //finish
        objectDataLayout.addNode(oConstructor);

        //5 DLFunction Declarations

        //initialize
        GNode oFunc2 = GNode.create("DLFunctionDeclaration");
        GNode oFuncModifiers2 = GNode.create("Modifiers");
        String oFuncType2 = "int32_t";
        String oFuncName2 = "hashCode";
        //String oFuncOwner2???
        GNode oFuncParameters2 = GNode.create("Parameters");

        //modify
        oFuncModifiers2.add("static");
        oFuncParameters2.add("Object");

        //add
        oFunc2.addNode(oFuncModifiers2);
        oFunc2.add(oFuncType2);
        oFunc2.add(oFuncName2);
        oFunc2.addNode(oFuncParameters2);

        //finish
        objectDataLayout.addNode(oFunc2);


        //initialize
        GNode oFunc4 = GNode.create("DLFunctionDeclaration");
        GNode oFuncModifiers4 = GNode.create("Modifiers");
        String oFuncType4 = "bool";
        String oFuncName4 = "equals";
        //String oFuncOwner4???
        GNode oFuncParameters4 = GNode.create("Parameters");

        //modify
        oFuncModifiers4.add("static");
        oFuncParameters4.add("Object");
        oFuncParameters4.add("Object");

        //add
        oFunc4.addNode(oFuncModifiers4);
        oFunc4.add(oFuncType4);
        oFunc4.add(oFuncName4);
        oFunc4.addNode(oFuncParameters4);

        //finish
        objectDataLayout.addNode(oFunc4);


        //initialize
        GNode oFunc3 = GNode.create("DLFunctionDeclaration");
        GNode oFuncModifiers3 = GNode.create("Modifiers");
        String oFuncType3 = "Class";
        String oFuncName3 = "getClass";
        //String oFuncOwner3???
        GNode oFuncParameters3 = GNode.create("Parameters");

        //modify
        oFuncModifiers3.add("static");
        oFuncParameters3.add("Object");

        //add
        oFunc3.addNode(oFuncModifiers3);
        oFunc3.add(oFuncType3);
        oFunc3.add(oFuncName3);
        oFunc3.addNode(oFuncParameters3);

        //finish
        objectDataLayout.addNode(oFunc3);


        //initialize
        GNode oFunc1 = GNode.create("DLFunctionDeclaration");
        GNode oFuncModifiers1 = GNode.create("Modifiers");
        String oFuncType1 = "String";
        String oFuncName1 = "toString";
        //String oFuncOwner1???
        GNode oFuncParameters1 = GNode.create("Parameters");

        //modify
        oFuncModifiers1.add("static");
        oFuncParameters1.add("Object");

        //add
        oFunc1.addNode(oFuncModifiers1);
        oFunc1.add(oFuncType1);
        oFunc1.add(oFuncName1);
        oFunc1.addNode(oFuncParameters1);

        //finish
        objectDataLayout.addNode(oFunc1);


        //initialize
        GNode oFunc5 = GNode.create("DLFunctionDeclaration");
        GNode oFuncModifiers5 = GNode.create("Modifiers");
        String oFuncType5 = "Class";
        String oFuncName5 = "__class";
        //String oFuncOwner5???
        GNode oFuncParameters5 = GNode.create("Parameters");

        //modify
        oFuncModifiers5.add("static");

        //add
        oFunc5.addNode(oFuncModifiers5);
        oFunc5.add(oFuncType5);
        oFunc5.add(oFuncName5);
        oFunc5.addNode(oFuncParameters5);

        //finish
        objectDataLayout.addNode(oFunc5);


        return objectDataLayout;
    }

    //should return a node with the vTable of Object
    private static GNode makeObjectVTableNode() {
        GNode objectVTable = GNode.create("VTable");

        //5 VTFunction Declarations
        //initialize
        GNode oFunc1 = GNode.create("VTFunctionDeclaration");
        GNode oFuncModifiers1 = GNode.create("Modifiers");
        String oFuncType1 = "Class";
        String oFuncName1 = "__isa";
        String oFuncOwner1 = "Object";
        GNode oFuncParameters1 = GNode.create("Parameters");

        //add
        oFunc1.addNode(oFuncModifiers1);
        oFunc1.add(oFuncType1);
        oFunc1.add(oFuncName1);
        oFunc1.add(oFuncOwner1);
        oFunc1.addNode(oFuncParameters1);

        //finish
        objectVTable.addNode(oFunc1);


        //initialize
        GNode oFunc3 = GNode.create("VTFunctionDeclaration");
        GNode oFuncModifiers3 = GNode.create("Modifiers");
        String oFuncType3 = "int32_t";
        String oFuncName3 = "hashCode";
        String oFuncOwner3 = "Object";
        GNode oFuncParameters3 = GNode.create("Parameters");

        //modify
        oFuncParameters3.add("Object");

        //add
        oFunc3.addNode(oFuncModifiers3);
        oFunc3.add(oFuncType3);
        oFunc3.add(oFuncName3);
        oFunc3.add(oFuncOwner3);
        oFunc3.addNode(oFuncParameters3);

        //finish
        objectVTable.addNode(oFunc3);


        //initialize
        GNode oFunc5 = GNode.create("VTFunctionDeclaration");
        GNode oFuncModifiers5 = GNode.create("Modifiers");
        String oFuncType5 = "bool";
        String oFuncName5 = "equals";
        String oFuncOwner5 = "Object";
        GNode oFuncParameters5 = GNode.create("Parameters");

        //modify
        oFuncParameters5.add("Object");
        oFuncParameters5.add("Object");

        //add
        oFunc5.addNode(oFuncModifiers5);
        oFunc5.add(oFuncType5);
        oFunc5.add(oFuncName5);
        oFunc5.add(oFuncOwner5);
        oFunc5.addNode(oFuncParameters5);

        //finish
        objectVTable.addNode(oFunc5);


        //initialize
        GNode oFunc4 = GNode.create("VTFunctionDeclaration");
        GNode oFuncModifiers4 = GNode.create("Modifiers");
        String oFuncType4 = "Class";
        String oFuncName4 = "getClass";
        String oFuncOwner4 = "Object";
        GNode oFuncParameters4 = GNode.create("Parameters");

        //modify
        oFuncParameters4.add("Object");

        //add
        oFunc4.addNode(oFuncModifiers4);
        oFunc4.add(oFuncType4);
        oFunc4.add(oFuncName4);
        oFunc4.add(oFuncOwner4);
        oFunc4.addNode(oFuncParameters4);

        //finish
        objectVTable.addNode(oFunc4);


        //initialize
        GNode oFunc2 = GNode.create("VTFunctionDeclaration");
        GNode oFuncModifiers2 = GNode.create("Modifiers");
        String oFuncType2 = "String";
        String oFuncName2 = "toString";
        String oFuncOwner2 = "Object";
        GNode oFuncParameters2 = GNode.create("Parameters");

        //modify
        oFuncParameters2.add("Object");

        //add
        oFunc2.addNode(oFuncModifiers2);
        oFunc2.add(oFuncType2);
        oFunc2.add(oFuncName2);
        oFunc2.add(oFuncOwner2);
        oFunc2.addNode(oFuncParameters2);

        //finish
        objectVTable.addNode(oFunc2);


        return objectVTable;
    }

    //==========================End of Java.Lang.Object AST======================//

    //============================ STRING HEADER AST GENERATOR ============================//
    public static GNode stringGenerate() {
        final GNode string = GNode.create("String");

        // new Visitor() {
        //   public void visitClassDeclaration(GNode classNode) {

        //string = GNode.create("String");
        GNode headerDeclaration = GNode.create("HeaderDeclaration");
        string.addNode(headerDeclaration);
        headerDeclaration.add("String");
        //Add "String as child to header dec
        //headerDeclaration.add("String");

        //Create datalayout GNode and set as child to header dec
        GNode DataLayout = GNode.create("DataLayout");
        headerDeclaration.add(DataLayout);

        //==========================FieldDeclaration 1======================

        //Create field dec GNode and set as child to datalayout
        GNode FieldDeclaration1 = GNode.create("FieldDeclaration");
        DataLayout.add(FieldDeclaration1);

        //Create Data_mod1 GNode and set as child to field dec
        GNode Data_Modifiers1 = GNode.create("Modifiers");
        FieldDeclaration1.add(Data_Modifiers1);

        FieldDeclaration1.add("__String_VT*");
        FieldDeclaration1.add("__vptr");

        //Create GNode
        GNode Data_Declarators1 = GNode.create("Declarators");
        FieldDeclaration1.add(Data_Declarators1);

        //==========================FieldDeclaration 2======================

        //Create field dec 2 and set as child to DataLayout
        GNode FieldDeclaration2 = GNode.create("FieldDeclaration");
        DataLayout.add(FieldDeclaration2);

        //Create Data_mod2 and set as child to field dec 2
        GNode Data_Modifiers2 = GNode.create("Modifiers");
        FieldDeclaration2.add(Data_Modifiers2);

        //set "static" as child to data_mod2
        Data_Modifiers2.add("static");

        FieldDeclaration2.add("__String_VT");
        FieldDeclaration2.add("__vtable");
        GNode Data_Declarators2 = GNode.create("Declarators");
        FieldDeclaration2.add(Data_Declarators2);

        //==========================FieldDeclaration 3======================
        //Create field dec GNode and set as child to datalayout
        GNode FieldDeclaration3 = GNode.create("FieldDeclaration");
        DataLayout.add(FieldDeclaration3);

        //Create Data_mod1 GNode and set as child to field dec
        GNode Data_Modifiers3 = GNode.create("Modifiers");
        FieldDeclaration3.add(Data_Modifiers1);

        FieldDeclaration3.add("std::string");
        FieldDeclaration3.add("data");

        //Create GNode
        GNode Data_Declarators3 = GNode.create("Declarators");
        FieldDeclaration3.add(Data_Declarators3);

        //==========================FieldDeclaration 3======================

        //==========================ConstructorDeclaration======================
        GNode ConstructorDeclaration = GNode.create("ConstructorDeclaration");
        DataLayout.add(ConstructorDeclaration);

        ConstructorDeclaration.add("String");
        GNode Constructor_Parameters = GNode.create("Parameters");
        ConstructorDeclaration.add(Constructor_Parameters);
        Constructor_Parameters.add("std::string data");



        //==========================DATA METH 2======================

        GNode DataLayoutMethodDeclaration2 = GNode.create("DLFunctionDeclaration");
        DataLayout.add(DataLayoutMethodDeclaration2);

        GNode Data_Declaration_Modifiers2 = GNode.create("Modifiers");
        DataLayoutMethodDeclaration2.add(Data_Declaration_Modifiers2);
        Data_Declaration_Modifiers2.add("static");

        DataLayoutMethodDeclaration2.add("int32_t");
        DataLayoutMethodDeclaration2.add("hashCode");
        //DataLayoutMethodDeclaration2.add("String");
        GNode Data_Dec_Parameters2 = GNode.create("Parameters");
        DataLayoutMethodDeclaration2.add(Data_Dec_Parameters2);
        Data_Dec_Parameters2.add("String");
        /*
                //==========================DATA METH 3======================

                GNode DataLayoutMethodDeclaration3 = GNode.create("DLFunctionDeclaration");
                DataLayout.add(DataLayoutMethodDeclaration3);

                GNode Data_Declaration_Modifiers3 = GNode.create("Modifiers");
                DataLayoutMethodDeclaration3.add(Data_Declaration_Modifiers3);
                Data_Declaration_Modifiers3.add("static");

                DataLayoutMethodDeclaration3.add("Class");
                DataLayoutMethodDeclaration3.add("getClass");
                //DataLayoutMethodDeclaration3.add("String");
                GNode Data_Dec_Parameters3 = GNode.create("Parameters");
                DataLayoutMethodDeclaration3.add(Data_Dec_Parameters3);
                Data_Dec_Parameters3.add("String");
        */
        //==========================DATA METH 4======================

        GNode DataLayoutMethodDeclaration4 = GNode.create("DLFunctionDeclaration");
        DataLayout.add(DataLayoutMethodDeclaration4);

        GNode Data_Declaration_Modifiers4 = GNode.create("Modifiers");
        DataLayoutMethodDeclaration4.add(Data_Declaration_Modifiers4);
        Data_Declaration_Modifiers4.add("static");

        DataLayoutMethodDeclaration4.add("bool");
        DataLayoutMethodDeclaration4.add("equals");
        //DataLayoutMethodDeclaration4.add("String");
        GNode Data_Dec_Parameters4 = GNode.create("Parameters");
        DataLayoutMethodDeclaration4.add(Data_Dec_Parameters4);
        Data_Dec_Parameters4.add("String");
        Data_Dec_Parameters4.add("Object");

        //==========================DATA METH 1======================

        GNode DataLayoutMethodDeclaration1 = GNode.create("DLFunctionDeclaration");
        DataLayout.add(DataLayoutMethodDeclaration1);

        GNode Data_Declaration_Modifiers1 = GNode.create("Modifiers");
        DataLayoutMethodDeclaration1.add(Data_Declaration_Modifiers1);
        Data_Declaration_Modifiers1.add("static");

        DataLayoutMethodDeclaration1.add("String");
        DataLayoutMethodDeclaration1.add("toString");
        //DataLayoutMethodDeclaration1.add("String");
        GNode Data_Dec_Parameters1 = GNode.create("Parameters");
        DataLayoutMethodDeclaration1.add(Data_Dec_Parameters1);
        Data_Dec_Parameters1.add("String");

        //==========================DATA METH 6======================

        GNode DataLayoutMethodDeclaration6 = GNode.create("DLFunctionDeclaration");
        DataLayout.add(DataLayoutMethodDeclaration6);

        GNode Data_Declaration_Modifiers6 = GNode.create("Modifiers");
        DataLayoutMethodDeclaration6.add(Data_Declaration_Modifiers6);
        Data_Declaration_Modifiers6.add("static");

        DataLayoutMethodDeclaration6.add("uint32_t");
        DataLayoutMethodDeclaration6.add("length");
        //DataLayoutMethodDeclaration6.add("String");
        GNode Data_Dec_Parameters6 = GNode.create("Parameters");
        DataLayoutMethodDeclaration6.add(Data_Dec_Parameters6);
        Data_Dec_Parameters6.add("String");

        //==========================DATA METH 7======================

        GNode DataLayoutMethodDeclaration7 = GNode.create("DLFunctionDeclaration");
        DataLayout.add(DataLayoutMethodDeclaration7);

        GNode Data_Declaration_Modifiers7 = GNode.create("Modifiers");
        DataLayoutMethodDeclaration7.add(Data_Declaration_Modifiers7);
        Data_Declaration_Modifiers7.add("static");

        DataLayoutMethodDeclaration7.add("char");
        DataLayoutMethodDeclaration7.add("charAt");
        //DataLayoutMethodDeclaration7.add("String");
        GNode Data_Dec_Parameters7 = GNode.create("Parameters");
        DataLayoutMethodDeclaration7.add(Data_Dec_Parameters7);
        Data_Dec_Parameters7.add("String");
        Data_Dec_Parameters7.add("int32_t");

        //==========================DATA METH 5======================

        GNode DataLayoutMethodDeclaration5 = GNode.create("DLFunctionDeclaration");
        DataLayout.add(DataLayoutMethodDeclaration5);

        GNode Data_Declaration_Modifiers5 = GNode.create("Modifiers");
        DataLayoutMethodDeclaration5.add(Data_Declaration_Modifiers5);
        Data_Declaration_Modifiers5.add("static");

        DataLayoutMethodDeclaration5.add("Class");
        DataLayoutMethodDeclaration5.add("__class");
        //DataLayoutMethodDeclaration5.add("String");
        GNode Data_Dec_Parameters5 = GNode.create("Parameters");
        DataLayoutMethodDeclaration5.add(Data_Dec_Parameters5);


        //==========================VTable 1======================

        GNode VTable = GNode.create("VTable");
        headerDeclaration.add(VTable);

        GNode VTableMethodDeclaration_1 = GNode.create("VTFunctionDeclaration");
        VTable.add(VTableMethodDeclaration_1);
        GNode Modifiers_1 = GNode.create("Modifiers");
        GNode Parameters_1 = GNode.create("Parameters");

        VTableMethodDeclaration_1.add(Modifiers_1);
        VTableMethodDeclaration_1.add("Class");
        VTableMethodDeclaration_1.add("__isa");
        VTableMethodDeclaration_1.add("String");
        VTableMethodDeclaration_1.add(Parameters_1);

        //==========================VTable 3======================

        GNode VTableMethodDeclaration_3 = GNode.create("VTFunctionDeclaration");
        VTable.add(VTableMethodDeclaration_3);

        GNode Modifiers_3 = GNode.create("Modifiers");
        GNode Parameters_3 = GNode.create("Parameters");

        VTableMethodDeclaration_3.add(Modifiers_3);
        VTableMethodDeclaration_3.add("int32_t");
        VTableMethodDeclaration_3.add("hashCode");
        VTableMethodDeclaration_3.add("String");
        VTableMethodDeclaration_3.add(Parameters_3);
        Parameters_3.add("String");

        //==========================VTable 5======================

        GNode VTableMethodDeclaration_5 = GNode.create("VTFunctionDeclaration");
        VTable.add(VTableMethodDeclaration_5);

        GNode Modifiers_5 = GNode.create("Modifiers");
        GNode Parameters_5 = GNode.create("Parameters");

        VTableMethodDeclaration_5.add(Modifiers_5);
        VTableMethodDeclaration_5.add("bool");
        VTableMethodDeclaration_5.add("equals");
        VTableMethodDeclaration_5.add("String");
        VTableMethodDeclaration_5.add(Parameters_5);
        Parameters_5.add("String");
        Parameters_5.add("Object");

        //==========================VTable 4======================

        GNode VTableMethodDeclaration_4 = GNode.create("VTFunctionDeclaration");
        VTable.add(VTableMethodDeclaration_4);

        GNode Modifiers_4 = GNode.create("Modifiers");
        GNode Parameters_4 = GNode.create("Parameters");

        VTableMethodDeclaration_4.add(Modifiers_4);
        VTableMethodDeclaration_4.add("Class");
        VTableMethodDeclaration_4.add("getClass");
        VTableMethodDeclaration_4.add("Object");
        VTableMethodDeclaration_4.add(Parameters_4);
        Parameters_4.add("String");

        //==========================VTable 2======================

        GNode VTableMethodDeclaration_2 = GNode.create("VTFunctionDeclaration");
        VTable.add(VTableMethodDeclaration_2);
        GNode Modifiers_2 = GNode.create("Modifiers");
        GNode Parameters_2 = GNode.create("Parameters");

        VTableMethodDeclaration_2.add(Modifiers_2);
        VTableMethodDeclaration_2.add("String");
        VTableMethodDeclaration_2.add("toString");
        VTableMethodDeclaration_2.add("String");
        VTableMethodDeclaration_2.add(Parameters_2);
        Parameters_2.add("String");

        //==========================VTable 6======================

        GNode VTableMethodDeclaration_6 = GNode.create("VTFunctionDeclaration");
        VTable.add(VTableMethodDeclaration_6);

        GNode Modifiers_6 = GNode.create("Modifiers");
        GNode Parameters_6 = GNode.create("Parameters");

        VTableMethodDeclaration_6.add(Modifiers_6);
        VTableMethodDeclaration_6.add("int32_t");
        VTableMethodDeclaration_6.add("length");
        VTableMethodDeclaration_6.add("String");
        VTableMethodDeclaration_6.add(Parameters_6);
        Parameters_6.add("String");

        //==========================VTable 7======================

        GNode VTableMethodDeclaration_7 = GNode.create("VTFunctionDeclaration");
        VTable.add(VTableMethodDeclaration_7);

        GNode Modifiers_7 = GNode.create("Modifiers");
        GNode Parameters_7 = GNode.create("Parameters");

        VTableMethodDeclaration_7.add(Modifiers_7);
        VTableMethodDeclaration_7.add("char");
        VTableMethodDeclaration_7.add("charAt");
        VTableMethodDeclaration_7.add("String");
        VTableMethodDeclaration_7.add(Parameters_7);
        Parameters_7.add("String");
        Parameters_7.add("int32_t");

        return string;
    }

    //==========================End of Java.Lang.String AST======================//

    //============================ CLASS HEADER AST GENERATOR ============================//
    public static GNode classGenerate() {
        final GNode p2Class = GNode.create("Class");

        //string = GNode.create("String");
        GNode headerDeclaration = GNode.create("HeaderDeclaration");
        p2Class.addNode(headerDeclaration);

        //Add "String as child to header dec
        //headerDeclaration.add("null");
        headerDeclaration.add("Class");

        //Create datalayout GNode and set as child to header dec
        GNode DataLayout = GNode.create("DataLayout");
        headerDeclaration.add(DataLayout);

        //Create field dec GNode and set as child to datalayout
        GNode FieldDeclaration1 = GNode.create("FieldDeclaration");
        DataLayout.add(FieldDeclaration1);

        //Create Data_mod1 GNode and set as child to field dec
        GNode Data_Modifiers1 = GNode.create("Modifiers");
        FieldDeclaration1.add(Data_Modifiers1);
        FieldDeclaration1.add("__Class_VT*");
        FieldDeclaration1.add("__vptr");

        //Create GNode
        GNode Data_Declarators1 = GNode.create("Declarators");
        FieldDeclaration1.addNode(Data_Declarators1);

        //Create field dec 2 and set as child to DataLayout
        GNode FieldDeclaration2 = GNode.create("FieldDeclaration");
        DataLayout.add(FieldDeclaration2);

        GNode Data_Modifiers2 = GNode.create("Modifiers");
        FieldDeclaration2.add(Data_Modifiers2);

        //replicate with Sting and add to the parameter to the constructor
        FieldDeclaration2.add("String");
        FieldDeclaration2.add("name");
        GNode Data_Declarators2 = GNode.create("Declarators");
        FieldDeclaration2.add(Data_Declarators2);


        GNode FieldDeclaration3 = GNode.create("FieldDeclaration");
        DataLayout.add(FieldDeclaration3);

        GNode Data_Modifiers3 = GNode.create("Modifiers");
        FieldDeclaration3.add(Data_Modifiers3);

        FieldDeclaration3.add("Class");
        FieldDeclaration3.add("parent");
        GNode Data_Declarators3 = GNode.create("Declarators");
        FieldDeclaration3.add(Data_Declarators3);


        GNode FieldDeclaration4 = GNode.create("FieldDeclaration");
        DataLayout.add(FieldDeclaration4);
        //Create Data_mod2 and set as child to field dec 2
        GNode Data_Modifiers4 = GNode.create("Modifiers");
        FieldDeclaration4.add(Data_Modifiers4);
        //set "static" as child to data_mod2
        Data_Modifiers4.add("static");

        FieldDeclaration4.add("__Class_VT");
        FieldDeclaration4.add("__vtable");
        GNode Data_Declarators4 = GNode.create("Declarators");
        FieldDeclaration4.add(Data_Declarators4);

        //========================Constructor Declaration================================
        GNode ConstructorDeclaration = GNode.create("ConstructorDeclaration");
        DataLayout.add(ConstructorDeclaration);

        ConstructorDeclaration.add("Class");
        GNode Constructor_Parameters = GNode.create("Parameters");
        ConstructorDeclaration.add(Constructor_Parameters);
        Constructor_Parameters.add("String name");
        Constructor_Parameters.add("Class parent");

        //==========================DATA METH 1======================

        GNode DataLayoutMethodDeclaration1 = GNode.create("DLFunctionDeclaration");
        DataLayout.add(DataLayoutMethodDeclaration1);

        GNode Data_Declaration_Modifiers1 = GNode.create("Modifiers");
        DataLayoutMethodDeclaration1.add(Data_Declaration_Modifiers1);
        Data_Declaration_Modifiers1.add("static");

        DataLayoutMethodDeclaration1.add("String");
        DataLayoutMethodDeclaration1.add("toString");
        //DataLayoutMethodDeclaration1.add("Class");
        GNode Data_Dec_Parameters1 = GNode.create("Parameters");
        DataLayoutMethodDeclaration1.add(Data_Dec_Parameters1);
        Data_Dec_Parameters1.add("Class");

        //==========================DATA METH 2======================

        GNode DataLayoutMethodDeclaration2 = GNode.create("DLFunctionDeclaration");
        DataLayout.add(DataLayoutMethodDeclaration2);

        GNode Data_Declaration_Modifiers2 = GNode.create("Modifiers");
        DataLayoutMethodDeclaration2.add(Data_Declaration_Modifiers2);
        Data_Declaration_Modifiers2.add("static");

        DataLayoutMethodDeclaration2.add("String");
        DataLayoutMethodDeclaration2.add("getName");
        //DataLayoutMethodDeclaration2.add("Class");
        GNode Data_Dec_Parameters2 = GNode.create("Parameters");
        DataLayoutMethodDeclaration2.addNode(Data_Dec_Parameters2);
        Data_Dec_Parameters2.add("Class");

        //==========================DATA METH 3======================

        GNode DataLayoutMethodDeclaration3 = GNode.create("DLFunctionDeclaration");
        DataLayout.add(DataLayoutMethodDeclaration3);

        GNode Data_Declaration_Modifiers3 = GNode.create("Modifiers");
        DataLayoutMethodDeclaration3.add(Data_Declaration_Modifiers3);
        Data_Declaration_Modifiers3.add("static");

        DataLayoutMethodDeclaration3.add("Class");
        DataLayoutMethodDeclaration3.add("getSuperclass");
        //DataLayoutMethodDeclaration3.add("Class");
        GNode Data_Dec_Parameters3 = GNode.create("Parameters");
        DataLayoutMethodDeclaration3.addNode(Data_Dec_Parameters3);
        Data_Dec_Parameters3.add("Class");

        //==========================DATA METH 4======================

        GNode DataLayoutMethodDeclaration4 = GNode.create("DLFunctionDeclaration");
        DataLayout.add(DataLayoutMethodDeclaration4);

        GNode Data_Declaration_Modifiers4 = GNode.create("Modifiers");
        DataLayoutMethodDeclaration4.add(Data_Declaration_Modifiers4);
        Data_Declaration_Modifiers4.add("static");

        DataLayoutMethodDeclaration4.add("bool");
        DataLayoutMethodDeclaration4.add("isInstance");
        //DataLayoutMethodDeclaration4.add("Class");
        GNode Data_Dec_Parameters4 = GNode.create("Parameters");
        DataLayoutMethodDeclaration4.add(Data_Dec_Parameters4);
        Data_Dec_Parameters4.add("Class");
        Data_Dec_Parameters4.add("Object");


        //==========================DATA METH 5======================

        GNode DataLayoutMethodDeclaration5 = GNode.create("DLFunctionDeclaration");
        DataLayout.add(DataLayoutMethodDeclaration5);

        GNode Data_Declaration_Modifiers5 = GNode.create("Modifiers");
        DataLayoutMethodDeclaration5.add(Data_Declaration_Modifiers5);
        Data_Declaration_Modifiers5.add("static");

        DataLayoutMethodDeclaration5.add("Class");
        DataLayoutMethodDeclaration5.add("__class");
        //DataLayoutMethodDeclaration5.add("Class");
        GNode Data_Dec_Parameters5 = GNode.create("Parameters");
        DataLayoutMethodDeclaration5.add(Data_Dec_Parameters5);

        GNode VTable = GNode.create("VTable");
        headerDeclaration.add(VTable);

        //==========================VTable 1======================
        GNode VTableMethodDeclaration_1 = GNode.create("VTFunctionDeclaration");
        VTable.add(VTableMethodDeclaration_1);
        GNode Modifiers_1 = GNode.create("Modifiers");
        GNode Parameters_1 = GNode.create("Parameters");
        VTableMethodDeclaration_1.add(Modifiers_1);
        VTableMethodDeclaration_1.add("Class");
        VTableMethodDeclaration_1.add("__isa");
        VTableMethodDeclaration_1.add("Class");
        VTableMethodDeclaration_1.add(Parameters_1);

        //==========================VTable 3======================
        GNode VTableMethodDeclaration_3 = GNode.create("VTFunctionDeclaration");
        VTable.add(VTableMethodDeclaration_3);
        GNode Modifiers_3 = GNode.create("Modifiers");
        GNode Parameters_3 = GNode.create("Parameters");
        VTableMethodDeclaration_3.add(Modifiers_3);
        VTableMethodDeclaration_3.add("int32_t");
        VTableMethodDeclaration_3.add("hashCode");
        VTableMethodDeclaration_3.add("Object");
        VTableMethodDeclaration_3.add(Parameters_3);
        Parameters_3.add("Class");

        //==========================VTable 5======================
        GNode VTableMethodDeclaration_5 = GNode.create("VTFunctionDeclaration");
        VTable.add(VTableMethodDeclaration_5);
        GNode Modifiers_5 = GNode.create("Modifiers");
        GNode Parameters_5 = GNode.create("Parameters");
        VTableMethodDeclaration_5.add(Modifiers_5);
        VTableMethodDeclaration_5.add("bool");
        VTableMethodDeclaration_5.add("equals");
        VTableMethodDeclaration_5.add("Object");
        VTableMethodDeclaration_5.add(Parameters_5);
        Parameters_5.add("Class");
        Parameters_5.add("Object");

        //==========================VTable 4======================
        GNode VTableMethodDeclaration_4 = GNode.create("VTFunctionDeclaration");
        VTable.add(VTableMethodDeclaration_4);
        GNode Modifiers_4 = GNode.create("Modifiers");
        GNode Parameters_4 = GNode.create("Parameters");
        VTableMethodDeclaration_4.add(Modifiers_4);
        VTableMethodDeclaration_4.add("Class");
        VTableMethodDeclaration_4.add("getClass");
        VTableMethodDeclaration_4.add("Object");
        VTableMethodDeclaration_4.add(Parameters_4);
        Parameters_4.add("Class");

        //==========================VTable 2======================
        GNode VTableMethodDeclaration_2 = GNode.create("VTFunctionDeclaration");
        VTable.add(VTableMethodDeclaration_2);
        GNode Modifiers_2 = GNode.create("Modifiers");
        GNode Parameters_2 = GNode.create("Parameters");
        VTableMethodDeclaration_2.add(Modifiers_2);
        VTableMethodDeclaration_2.add("String");
        VTableMethodDeclaration_2.add("toString");
        VTableMethodDeclaration_2.add("Class");
        VTableMethodDeclaration_2.add(Parameters_2);
        Parameters_2.add("Class");

        //==========================VTable 6======================
        GNode VTableMethodDeclaration_6 = GNode.create("VTFunctionDeclaration");
        VTable.add(VTableMethodDeclaration_6);
        GNode Modifiers_6 = GNode.create("Modifiers");
        GNode Parameters_6 = GNode.create("Parameters");
        VTableMethodDeclaration_6.add(Modifiers_6);
        VTableMethodDeclaration_6.add("String");
        VTableMethodDeclaration_6.add("getName");
        VTableMethodDeclaration_6.add("Class");
        VTableMethodDeclaration_6.add(Parameters_6);
        Parameters_6.add("Class");

        //==========================VTable 7======================
        GNode VTableMethodDeclaration_7 = GNode.create("VTFunctionDeclaration");
        VTable.add(VTableMethodDeclaration_7);
        GNode Modifiers_7 = GNode.create("Modifiers");
        GNode Parameters_7 = GNode.create("Parameters");
        VTableMethodDeclaration_7.add(Modifiers_7);
        VTableMethodDeclaration_7.add("Class");
        VTableMethodDeclaration_7.add("getSuperclass");
        VTableMethodDeclaration_7.add("Class");
        VTableMethodDeclaration_7.add(Parameters_7);
        Parameters_7.add("Class");

        //==========================VTable 8======================
        GNode VTableMethodDeclaration_8 = GNode.create("VTFunctionDeclaration");
        VTable.add(VTableMethodDeclaration_8);
        GNode Modifiers_8 = GNode.create("Modifiers");
        GNode Parameters_8 = GNode.create("Parameters");
        VTableMethodDeclaration_8.add(Modifiers_8);
        VTableMethodDeclaration_8.add("bool");
        VTableMethodDeclaration_8.add("isInstance");
        VTableMethodDeclaration_8.add("Class");
        VTableMethodDeclaration_8.add(Parameters_8);
        Parameters_8.add("Class");
        Parameters_8.add("Object");

        return p2Class;
    }
    //==========================End of Java.Lang.Class AST======================//
}