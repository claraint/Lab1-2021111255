import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class MainTest {

    private Main main;

    @Before
    public void setUp() {
        // 初始化测试对象以及相关的图结构
        main = new Main();

        // 示例初始化，具体初始化代码取决于你的类结构
        main.wordIndexMap = new HashMap<>();
        main.adj = new LinkedList[5];  // 假设有5个节点

        // 示例数据
        main.wordIndexMap.put("word1", 0);
        main.wordIndexMap.put("bridge", 1);
        main.wordIndexMap.put("word2", 2);

        main.adj[0] = new LinkedList("word1"); // 对应 word1 的节点
        main.adj[0].addNode("bridge", 1);

        main.adj[1] = new LinkedList("bridge"); // 对应 bridge 的节点
        main.adj[1].addNode("word2", 2);

        main.adj[2] = new LinkedList("word2"); // 对应 word2 的节点
    }

    @Test
    public void testQueryBridgeWords_validWords() {
        String result = main.queryBridgeWords("word1", "word2");
        assertEquals("The bridge words from \"word1\" to \"word2\" are:bridge,", result);
    }

    @Test
    public void testQueryBridgeWords_noWord1() {
        String result = main.queryBridgeWords("nonexistent", "word2");
        assertEquals("No \"nonexistent\" or \"word2\" in the graph!", result);
    }


    @Test
    public void testQueryBridgeWords_noBridgeWords() {
        String result = main.queryBridgeWords("word2", "word1");
        assertEquals("No bridge words from \"word2\" to \"word1\"!", result);
    }
}
