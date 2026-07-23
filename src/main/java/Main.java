import com.tree.Cache;
import com.tree.Node;
import com.tree.Node.Action;
import com.tree.Node.Belief;
import com.tree.Tree;
import com.tree.World;

import java.util.Map;

import static com.tree.Status.ACTION;
import static com.tree.Status.FAILURE;
import static java.util.Map.of;

public class Main {
    public static void main(String[] args) {
        Node root = new Node.Sequence(0,
                new Node.Sequence(1,
                        new Action(2),
                        new Action(3),
                        new Action(4)),
                new Action(5));
        World<String> world = new World<>(Map.of(), Map.of(), root);
        world.loop();
    }
}
