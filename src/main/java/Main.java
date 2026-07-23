import com.tree.Node;
import com.tree.Node.Action;
import com.tree.Node.Belief;
import com.tree.World;

import java.util.Map;
import java.util.function.Function;

public class Main {
    public static void main(String[] args) {
        String board = "";
        Node root = new Node.Sequence(0,
                new Node.Sequence(1,
                        new Action(2),
                        new Action(3),
                        new Belief(4)),
                new Action(5));
        Map<Integer, Function<String, Boolean>> believes = Map.of(
                4, (String b) -> Boolean.FALSE
        );
        World<String> world = new World<>(board, believes, Map.of(), root);
        world.loop();
    }
}
