package edu.nyu.oop.util;

import xtc.lang.JavaFiveParser;
import xtc.parser.Result;
import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Token;
import xtc.tree.Visitor;

import java.io.*;
import java.util.List;
import java.util.LinkedList;

public class NodeUtil {

    // TODO - see xtc.lang.JavaEntities

    // A class representing a location inside the Ast.
    // Useful when trying to extract a location out of the Ast from inside a Visitor.
    public static class AstIndex {
        public GNode node;
        public int childIdx;

        public AstIndex(GNode n, int idx) {
            this.node = n;
            this.childIdx = idx;
        }
    }

    // Takes a node and concatenates all its children into a string with the specified delimiter
    public static String mkString(Node node, String delim) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < node.size() ; i++) {
            buf.append(Token.cast(node.get(i)));
            if (i < node.size() - 1) buf.append(delim);
        }
        return buf.toString();
    }

    // Searches Ast for a node with specified name. Returns first that it finds.
    public static Node dfs(Node node, String nodeName) {
        if (node.size() == 0) {
            return null;
        } else if (node.hasName(nodeName)) {
            return node;
        } else {
            for (Object o : node) {
                if (o instanceof Node) {
                    Node casted = (Node) o;
                    Node target = dfs(casted, nodeName);
                    if (target != null) return target;
                }
            }
        }
        return null;
    }

    // Searches Ast for a node with specified name. Returns all that it finds.
    public static List<Node> dfsAll(Node root, final String nodeName) {
        final List<Node> nodes = new LinkedList<Node>();
        new Visitor() {
            public void visit(Node n) {
                if(nodeName.equals(n.getName())) {
                    nodes.add(n);
                }
                for (Object o : n) {
                    if (o instanceof Node) dispatch((Node) o);
                }
            }
        } .dispatch(root);

        return nodes;
    }

    // Parses a Java source file into an Xtc Ast
    public static Node parseJavaFile(File file) {
        try {
            InputStream instream = new FileInputStream(file);
            Reader in = new BufferedReader(new InputStreamReader(instream));
            JavaFiveParser parser = new JavaFiveParser(in, file.toString(), (int)file.length());
            Result result = parser.pCompilationUnit(0);
            return (Node) parser.value(result);
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse Java file " + file.getName(), e);
        }
    }
}