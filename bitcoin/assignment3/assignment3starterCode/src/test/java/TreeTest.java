import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by marco on 10/02/17.
 */
public class TreeTest {
    private TreeNode root = new TreeNode(1);

    @Test
    public void root() {
        assertEquals(1, root.getValue());
    }

    @Test
    public void addOneChild() {
        root.addChild(new TreeNode(2));
        List<TreeNode> children = root.getChildren();
        assertEquals(1, children.size());
        TreeNode child = children.get(0);
        assertEquals(2, child.getValue());
        assertEquals(this.root, child.getParent());
    }

    @Test
    public void addTwoChildren() {
        root.addChild(new TreeNode(2));
        root.addChild(new TreeNode(3));

        List<TreeNode> children = root.getChildren();
        assertEquals(2, children.size());

        TreeNode child1 = children.get(0);
        assertEquals(2, child1.getValue());
        assertEquals(this.root, child1.getParent());

        TreeNode child2 = children.get(1);
        assertEquals(3, child2.getValue());
        assertEquals(this.root, child2.getParent());
    }

    @Test
    public void thirdLevel() {
        root.addChild(new TreeNode(2)).addChild(new TreeNode(3));

        TreeNode secondLevel = root.getChildren().get(0);
        assertEquals(2, secondLevel.getValue());

        TreeNode thirdLevel = secondLevel.getChildren().get(0);
        assertEquals(3, thirdLevel.getValue());

        assertEquals(secondLevel, thirdLevel.getParent());
        assertEquals(root, secondLevel.getParent());
    }
}
