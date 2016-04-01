package edu.nyu.oop;

import edu.nyu.oop.util.NodeUtil;
import xtc.tree.GNode;
import xtc.tree.Node;

import java.util.List;
import java.util.Map;
import java.util.HashMap;


/**
 * Stores the inheritance hierarchy of java source classes as a graph.
 * Each vertex should have one or no outgoing edges pointing to the super class
 */
class ClassInheritanceHierarchy {

    private Map<String, String> adjList;    // Adjacency list of class inheritance hierarchy

    // Constructor
    public ClassInheritanceHierarchy(GNode ast) {

        // Local variables
        String className;       // Class name
        String superClassName;  // Super class name
        Node classExtension;   // Class extension node

        // Initialize class members
        adjList = new HashMap<String, String>();

        // Get all classes implemented in java source language
        List<Node> classes = NodeUtil.dfsAll(ast, "ClassDeclaration");

        // Iterate through each class with the exception of main
        for(Node javaClass : classes) {
            if(!AstUtil.isMainClass((GNode) javaClass)) {

                // Get the class name and super class name
                className = javaClass.getString(1);
                classExtension = javaClass.getNode(3);  // Extension node

                // if class does not have a super class
                if(null == classExtension) {

                    // it will be a source node ( no value for key )
                    adjList.put(className, null);
                }

                else {

                    // we must extract the super class name from the extension node
                    superClassName = classExtension.getGeneric(0).getGeneric(0).getString(0);

                    // add edge between class and super class
                    adjList.put(className, superClassName);
                }
            }
        }
    }

    /**
     * Returns the name of the superclass if it exists, otherwise returns null.
     * @param subClass Sub class we want to find the super class of
     * @return The super class or null
     */
    public String getSuperClass(String subClass) {
        return adjList.get(subClass);
    }

    /**
     * Checks if one class is a subclass of another class
     * @param superClass The super class
     * @param subClass The sub class
     * @return Whether or not the subClass is a subclass of the superClass
     */
    public boolean isSubClass(String subClass, String superClass) {

        String parent = subClass;

        // go up the inheritance hierarchy
        while(parent != null) {
            parent = getSuperClass(parent);

            // if the specified superclass is found then there is a subclass relationship
            if(parent.equals(superClass)) {
                return true;
            }
        }

        // reached top of hierarchy without finding specified super class
        return false;
    }
}