import com.tree.Cache;
import com.tree.Node;
import com.tree.Node.Action;
import com.tree.Node.Belief;
import com.tree.Tree;

import static com.tree.Status.FAILURE;
import static java.util.Map.of;

public class Main {
    public static void main(String[] args) {
        var c = new Cache(
                of(
                        1, FAILURE
                )
        );
        var a = Tree.tick(new Node.Fallback(0,
                new Action(1),
                new Action(2),
                new Belief(3)
        ), c);
        System.out.println(a);
    }
}
