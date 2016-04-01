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

/**
 * Phase 2 of translation.
 * Generates AST schema
 * i hope we dont die
 * to do
 */


public GNode AstGenerate(GNode n){
        GNode header=GNode.create("HeaderDeclaration");
        header.add(packageName);
        header.add(node.getString(1));
        header.add(makeDataLayoutNode(header,n));
        header.add(makeVTableNode(header,n));

        return header;
        }

public List<GNode> makeDataLayoutNode(GNode n,GNode parent){
        GNode data=GNode.create("DataLayout");
        List<String> fieldDecProperties = new List<String>();
        fieldDecProperties.add("Modifiers");
        fieldDecProperties.add("__Object_VT*");
        fieldDecProperties.add("__vptr");
        fieldDecProperties.add("Declarators");
        /*
        n.add(data);
        List <GNode> poop=dfsAll(parent,"FieldDeclaration");
        for(GNode g: poop){
            data.add(g);
        }

        List <GNode> poop2 = dfsAll(parent, "ConstructorDeclaration")

        List <GNode> poop3 = dfsAll(parent, "")
        }*/

public List<GNode> makeVTableNode(GNode n,GNode parent){
        GNode vTable=GNode.create("VTable");
        n.add(data);
        List <GNode> poopy=dfsAll(parent,"MethodDeclaration");
        for(GNode j:poopy){
            data.add(j);
        }
        }
