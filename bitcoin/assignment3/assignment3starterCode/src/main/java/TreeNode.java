import sun.reflect.generics.tree.Tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marco on 10/02/17.
 */
public class TreeNode {
    private int value;
    private TreeNode parent;
    private List<TreeNode> children = new ArrayList<>();

    public TreeNode(int v) {
        this.value = v;
    }

    public int getValue() {
        return value;
    }

    public TreeNode addChild(TreeNode child) {
        child.setParent(this);
        children.add(child);
        return child;
    }

    public TreeNode getParent() {
        return parent;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public List<TreeNode> getChildren() {
        return children;
    }
}
