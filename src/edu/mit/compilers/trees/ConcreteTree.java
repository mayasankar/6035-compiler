package edu.mit.compilers.trees;

import antlr.Token;

public class ConcreteTree {
  private String nodeName;
  private ConcreteTree parent;
  private ConcreteTree firstChild;
  private ConcreteTree lastChild;
  private ConcreteTree leftSibling;
  private ConcreteTree rightSibling;

  // initialized by adding the first child for nontokens.
  protected int lineNumber;
  protected int columnNumber;

  ConcreteTree(String nn) {
    nodeName = nn;
  }

  ConcreteTree(String nn, ConcreteTree p) {
    nodeName = nn;
    parent = p;
  }

  public boolean isRoot() { return parent == null; }

  public boolean isNode() { return false; } // overriden in subclasses
  public boolean isOperator() { return false; } // overriden in subclasses

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
      child.leftSibling = lastChild;
    }
    lastChild = child;
    return lastChild;
  }

  public ConcreteTree getParent() { return parent; }
  public ConcreteTree getLeftSibling() { return leftSibling; }
  public ConcreteTree getRightSibling() { return rightSibling; }
  public ConcreteTree getFirstChild() { return firstChild; }
  public ConcreteTree getLastChild() { return lastChild; }

  public int getLine() { return lineNumber; }
  public int getColumn() { return columnNumber; }

  public String getName() { return nodeName; }

  public Token getToken() { return null; } // overriden in subclasses

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

  // initializes line and column numbers. To be called after tree is built.
  public void initializeLineNumbers() {
    ConcreteTree child = firstChild;
    while (child != null) {
      child.initializeLineNumbers();
      child = child.rightSibling;
    }
    if (firstChild != null) {
        lineNumber = firstChild.lineNumber;
        columnNumber = firstChild.columnNumber;
    }
    else {
        System.out.println("Debugging: name is " + getName());
        // TODO for mayars what should the line/col numbers be assigned to in the case of an empty block? i.e. method(){}
    }
  }

  // deletes any nodes of the given tokentype. TODO test that it is correct
  public void deleteNodes(int tokentype) {
    if (isNode() && tokentype == getToken().getType()) {
      if (leftSibling == null) {
        if (parent != null) {
          parent.firstChild = rightSibling;
        }
      } else {
        leftSibling.rightSibling = rightSibling;
      }
      if (rightSibling == null) {
        if (parent != null) {
          parent.lastChild = leftSibling;
        }
      } else {
        rightSibling.leftSibling = leftSibling;
      }
    } else {
      ConcreteTree child = firstChild;
      while(child != null) {
        child.deleteNodes(tokentype);
        child = child.rightSibling;
      }
    }
  }

  // Any node with the given name will be replaced by its child if it has only
  // one child. Useful for the case of expr -> expr_8 -> ... -> expr_2 when
  // parsing, for example, a * b as an expr.
  public void compressNodes(String name) {
    if (nodeName.equals(name)) {
      if (firstChild != null && firstChild == lastChild) {
        firstChild.parent = parent;
        firstChild.leftSibling = leftSibling;
        firstChild.rightSibling = rightSibling;
        if (leftSibling == null) {
          parent.firstChild = firstChild;
        } else {
          leftSibling.rightSibling = firstChild;
        }
        if (rightSibling == null) {
          parent.lastChild = firstChild;
        } else {
          rightSibling.leftSibling = firstChild;
        }
      }
    }
    ConcreteTree child = firstChild;
    while (child != null) {
      child.compressNodes(name);
      child = child.rightSibling;
    }
  }

  // use for testing only
  public static ConcreteTree testTree() {
    ConcreteTree root = new ConcreteTree("root");
    root.addChild("grandparent");
    root.firstChild.addChild("parent");
    root.firstChild.firstChild.addChild("child1");
    root.firstChild.firstChild.addChild("child2");
    return root;
  }

}
