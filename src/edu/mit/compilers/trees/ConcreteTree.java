package edu.mit.compilers.trees;

import antlr.Token;

public class ConcreteTree {
  private String nodeName;
  private ConcreteTree parent;
  private ConcreteTree firstChild;
  private ConcreteTree lastChild;
  private ConcreteTree rightSibling;

  ConcreteTree(String nn) {
    nodeName = nn;
  }

  ConcreteTree(String nn, ConcreteTree p) {
    nodeName = nn;
    parent = p;
  }

  public boolean isRoot() {
    return parent == null;
  }

  public boolean isNode() { // overriden in subclasses
    return false;
  }

  public ConcreteTree addChild(String newName) {
    ConcreteTree child = new ConcreteTree(newName, this);
    return addChild(child);
  }

  public void addNode(Token t) {
    addChild(new ConcreteTreeNode(t, this));
  }

  private ConcreteTree addChild(ConcreteTree child) {
    if (firstChild == null) {
      firstChild = child;
    } else {
      lastChild.rightSibling = child;
    }
    lastChild = child;
    return lastChild;
  }

  public ConcreteTree getParent() { return parent; }

  public ConcreteTree getNextSibling() { return rightSibling; }

  public ConcreteTree getFirstChild() { return firstChild; }

  public ConcreteTree getLastChild() { return lastChild; }

  public String getName() { return nodeName; }

  public static ConcreteTree root() {
    return new ConcreteTree("__PROGRAM_ROOT__");
  }

  public void print() { print(0); }

  private void print(int indent) {
    for (int i = 0; i < indent; ++i) {
      System.out.print("  ");
    }
    System.out.println(nodeName);
    if (firstChild != null) {
      firstChild.print(indent + 1);
    }
    if (rightSibling != null) {
      rightSibling.print(indent);
    }
  }

}
