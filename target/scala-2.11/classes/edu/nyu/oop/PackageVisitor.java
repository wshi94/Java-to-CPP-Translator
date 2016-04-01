package edu.nyu.oop;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;

/**
 * PackageVisitor.java
 * This class handles the mutation of the package node in the
 * java ast.
 */

// Package Visitor
public class PackageVisitor extends Visitor {

    // Fields
    private GNode mutatedPackageAst;    // Ast that will be mutated

    public void visitCompilationUnit(GNode n) {

        // Check if package declaration exists.  Always index 0 in ast?
        // If no package is declared, index 0 contains a null value.
        // TODO: determine if name check is redundant
        // May not be safe if compiler does not use boolean short circuiting
        if(((GNode) n.get(0)) != null && ((GNode) n.get(0)).hasName("PackageDeclaration")) {
            GNode packageDeclaration = (GNode) n.get(0);

            // Check QualifiedIdentifier always index 1?
            if(((GNode) packageDeclaration.get(1)).hasName("QualifiedIdentifier")) {
                GNode qualifiedIdentifier = (GNode) packageDeclaration.get(1);

                // Create namespace GNode
                GNode namespaceDeclaration = GNode.create("NamespaceDeclaration");

                // Add the QualifiedIdentifiers to newly created NamespaceDeclaration GNode
                namespaceDeclaration.add(qualifiedIdentifier);

                // Replace PackageDeclaration GNode with NameSpaceDeclaration GNode
                n.set(0, namespaceDeclaration);
            }
        }
    }

    public void visit(Node n) {
        for(Object o : n) {
            if( o instanceof Node) dispatch( (Node) o);
        }
    }

    /**
     * Takes the root of the ast and mutates the package declaration
     * to a namespace declaration (c++ version of packages)
     * @param root The root of the ast
     * @return The mutated ast with PackageDeclaration mutations
     */
    public GNode getMutatedPackageAst(GNode root) {
        super.dispatch(root);
        return mutatedPackageAst;
    }
}