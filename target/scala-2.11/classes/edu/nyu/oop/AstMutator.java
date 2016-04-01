package edu.nyu.oop;

import xtc.lang.JavaAstSimplifier;
import xtc.tree.GNode;

/**
 * Phase 4 of translation.
 * Mutates and decorates the AST
 * with C++ code to print out in phase 5.
 */

public class AstMutator {

    // Fields
    private GNode root;             // Root node of Ast

    // Constructor
    public AstMutator(GNode ast) {
        root = ast;

        // Simplify the ast
        new JavaAstSimplifier().dispatch(root);

        // Mutate package declaration
        PackageVisitor packageVisitor = new PackageVisitor();
        packageVisitor.getMutatedPackageAst(root);

        // Mutate class declaration
        ClassVisitor classVisitor = new ClassVisitor();
        classVisitor.getMutatedClassAst(root);

    }

    public GNode getMutatedAst() {
        return root;
    }
}