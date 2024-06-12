import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

/**
 * Main class to demonstrate the graph functionalities.
 */
public class Main {
    private static final int MAX = 32767;
    private int vertex;
    LinkedList[] adj;
    Map<String, Integer> wordIndexMap;

    /**
     * Constructs a Main object with initialized adjacency list and word index map.
     */
    public Main() {
        adj = new LinkedList[MAX];
        wordIndexMap = new HashMap<>();
        vertex = 0;
    }

    /**
     * The main method to execute the program.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        String filePath = "input.txt";
        String finalText = readAndProcessText(filePath);

        // 单词数组
        String[] words = finalText.trim().split(" ");

        Main main = new Main();
        // 将单词数组转换为有向图
        for (int i = 0; i < words.length - 1; i++) {
            main.addEdge(words[i], words[i + 1]);
        }

        // 展示有向图
        main.showDirectedGraph();

        Scanner scanner = new Scanner(System.in);

        // 查询桥接词
        System.out.println("请输入需要查询桥接词的两个单词");
        System.out.println("单词一");
        String word1 = scanner.nextLine();
        System.out.println("单词二");
        String word2 = scanner.nextLine();
        System.out.println(main.queryBridgeWords(word1, word2));

        // 功能4：根据bridge word生成新文本
        System.out.println("请输入一行新文本");
        String newText = scanner.nextLine();
        //读取用户输入的一行文本
        System.out.println("生成的新文本：");
        System.out.println(main.generateNewText(newText));

        // 功能5：计算两个单词之间的最短路径
        System.out.println("请输入两个单词，计算它们之间的最短路径：");
        String startWord = scanner.next();
        String endWord = scanner.next();
        main.calcShortestPath(startWord, endWord);

        // 功能6：随机游走
        main.randomWalk();
    }

    /**
     * Reads and processes text from the specified file.
     *
     * @param filePath the path to the input file
     * @return the processed text
     */
    public static String readAndProcessText(String filePath) {
        StringBuilder processedText = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            // 正则表达式，匹配任何非字母字符，包括标点符号、换行符等
            Pattern nonLetterPattern = Pattern.compile("[^a-zA-Z]+");

            while ((line = reader.readLine()) != null) {
                // 转换为小写
                line = line.toLowerCase();

                Matcher matcher = nonLetterPattern.matcher(line);

                // 使用空格替换非字母字符
                String processedLine = matcher.replaceAll(" ");

                // 移除多余空格
                processedLine = processedLine.replaceAll("\\s+", " ").trim();
                if (!processedLine.isEmpty()) {
                    processedText.append(processedLine).append(" ");
                }
            }

            // 移除尾部多余的空格
            return processedText.toString().trim();

        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Adds an edge between two words in the graph.
     *
     * @param word1 the first word
     * @param word2 the second word
     */
    public void addEdge(String word1, String word2) {
        int index1 = wordIndexMap.getOrDefault(word1, -1);
        int index2 = wordIndexMap.getOrDefault(word2, -1);

        if (index1 == -1) {
            adj[vertex] = new LinkedList(word1);
            wordIndexMap.put(word1, vertex);
            index1 = vertex;
            vertex++;
        }

        if (index2 == -1) {
            adj[vertex] = new LinkedList(word2);
            wordIndexMap.put(word2, vertex);
            index2 = vertex++;
        }

        for (Node node = adj[index1].getHead().next; node != null; node = node.next) {
            if (node.word.equals(word2)) {
                node.weight++;
                return;
            }
        }

        adj[index1].addNode(word2, index2);
    }

    /**
     * Visualizes the directed graph.
     */
    public void showDirectedGraph() {
        // 创建带权重的有向图
        DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);

        // 将图中的顶点和边添加到 JGraphT 图中
        for (String word : wordIndexMap.keySet()) {
            if (!word.trim().isEmpty()) {
                graph.addVertex(word);
            }
        }
        for (int i = 0; i < vertex; i++) {
            for (Node node = adj[i].getHead().next; node != null; node = node.next) {
                if (!node.word.trim().isEmpty()) {
                    DefaultWeightedEdge edge = graph.addEdge(adj[i].getHead().word, node.word);
                    if (edge != null) {
                        graph.setEdgeWeight(edge, node.weight); // 设置边的权重
                    }
                }
            }
        }

        // 使用 mxGraph 将 JGraphT 图转换为 JGraphX 图
        mxGraph mxGraph = new mxGraph();
        Object parent = mxGraph.getDefaultParent();
        mxGraph.getModel().beginUpdate();

        try {
            // 将顶点添加到图中
            Map<String, Object> vertexMap = new HashMap<>();
            for (String vertex : graph.vertexSet()) {
                Object v = mxGraph.insertVertex(parent, null, vertex, 100, 100, 30, 30,
                        "shape=ellipse;perimeter=ellipsePerimeter;fillColor=none;strokeColor=black;fontColor=black");
                vertexMap.put(vertex, v);
            }

            // 将边添加到图中
            for (DefaultWeightedEdge edge : graph.edgeSet()) {
                String source = graph.getEdgeSource(edge);
                String target = graph.getEdgeTarget(edge);
                double weight = graph.getEdgeWeight(edge); // 获取边的权重
                mxGraph.insertEdge(parent, null, String.valueOf(weight), vertexMap.get(source), vertexMap.get(target),
                        "endArrow=block;endSize=8;strokeColor=black;fillColor=black;fontSize=12;fontColor=red");
            }
        } finally {
            mxGraph.getModel().endUpdate();
        }

        // 创建一个 Swing 窗口来展示图形
        JFrame frame = new JFrame();
        frame.getContentPane().add(new mxGraphComponent(mxGraph));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setVisible(true);

        // 使用 mxCircleLayout 将图形布局为圆形
        mxCircleLayout layout = new mxCircleLayout(mxGraph);
        layout.execute(mxGraph.getDefaultParent());
    }

    /**
     * Queries bridge words between two words.
     *
     * @param word1 the first word
     * @param word2 the second word
     * @return the bridge words between word1 and word2
     */
    public String queryBridgeWords(String word1, String word2) {
        int index1 = wordIndexMap.getOrDefault(word1, -1);
        int index2 = wordIndexMap.getOrDefault(word2, -1);

        if (index1 == -1 || index2 == -1) {
            return "No \"" + word1 + "\" or \"" + word2 + "\" in the graph!";
        }

        StringBuilder words = new StringBuilder();
        Node node3 = adj[index1].getHead().next;
        while (node3 != null) {
            int index3 = node3.num;
            Node node4 = adj[index3].getHead().next;
            while (node4 != null) {
                if (node4.word.equals(word2)) {
                    words.append(node3.word).append(",");
                }
                node4 = node4.next;
            }
            node3 = node3.next;
        }
        if (words.length() == 0) {
            return "No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!";
        } else {
            return "The bridge words from \"" + word1 + "\" to \"" + word2 + "\" are:" + words.toString();
        }
    }

    /**
     * Generates new text based on bridge words.
     *
     * @param inputText the input text
     * @return the generated text
     */
    public String generateNewText(String inputText) {
        String[] newWords = inputText.split(" ");
        StringBuilder resultText = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < newWords.length - 1; i++) {
            String word1 = newWords[i];
            String word2 = newWords[i + 1];
            boolean word1Exists = wordIndexMap.containsKey(word1);
            boolean word2Exists = wordIndexMap.containsKey(word2);
            // 检查单词是否存在于图中
            if (!word1Exists || !word2Exists) {
                // 如果单词不存在，将其原样输出
                resultText.append(word1).append(" ");
                continue; // 继续处理下一个单词
            }

            String bridgeWords = queryBridgeWords(word1, word2);

            resultText.append(word1).append(" ");

            if (!bridgeWords.startsWith("No bridge words")) {
                String[] bridges = bridgeWords.replace("The bridge words from \"" + word1 + "\" to \"" + word2 + "\" are:", "").split(",");
                String bridgeWord = bridges[random.nextInt(bridges.length)].trim();
                resultText.append(bridgeWord).append(" ");
            }
        }
        // 处理最后一个单词
        resultText.append(newWords[newWords.length - 1]);

        return resultText.toString();
    }

    /**
     * Calculates the shortest path between two words using Dijkstra's algorithm.
     *
     * @param word1 the start word
     * @param word2 the end word
     */
    public void calcShortestPath(String word1, String word2) {
        int start = wordIndexMap.getOrDefault(word1, -1);
        int end = wordIndexMap.getOrDefault(word2, -1);

        if (start == -1 || end == -1) {
            System.out.println("输入的单词不存在！");
            return;
        }

        int[] distance = new int[vertex];
        Arrays.fill(distance, Integer.MAX_VALUE);
        distance[start] = 0;

        boolean[] visited = new boolean[vertex];
        int[] prev = new int[vertex];
        Arrays.fill(prev, -1);

        for (int i = 0; i < vertex; i++) {
            int minDistance = Integer.MAX_VALUE;
            int minIndex = -1;

            // 选择未访问的节点中距离最小的节点
            for (int j = 0; j < vertex; j++) {
                if (!visited[j] && distance[j] < minDistance) {
                    minDistance = distance[j];
                    minIndex = j;
                }
            }

            if (minIndex == -1) {
                break;
            }

            visited[minIndex] = true;

            // 更新与当前节点相邻的节点的距离
            for (Node node = adj[minIndex].getHead().next; node != null; node = node.next) {
                int neighborIndex = node.num;
                int weight = node.weight;
                if (!visited[neighborIndex] && distance[minIndex] + weight < distance[neighborIndex]) {
                    distance[neighborIndex] = distance[minIndex] + weight;
                    prev[neighborIndex] = minIndex;
                }
            }
        }

        if (distance[end] == Integer.MAX_VALUE) {
            System.out.println("输入的单词不可达！");
            return;
        }

        // 构造最短路径
        List<String> shortestPath = new ArrayList<>();
        int current = end;
        while (current != -1) {
            shortestPath.add(adj[current].getHead().word);
            current = prev[current];
        }

        // 输出最短路径
        Collections.reverse(shortestPath);
        System.out.print("最短路径为：");
        for (int i = 0; i < shortestPath.size() - 1; i++) {
            System.out.print(shortestPath.get(i) + "→");
        }
        System.out.println(shortestPath.get(shortestPath.size() - 1));
        System.out.println("路径长度为：" + distance[end]);

        //标记路径
        markShortestPath(shortestPath);
    }

    /**
     * Performs a random walk on the graph.
     */
    public void randomWalk() {
        Random random = new Random();
        int start = random.nextInt(vertex); // 随机选择起始节点
        StringBuilder result = new StringBuilder();

        boolean[][] visitedEdges = new boolean[vertex][vertex]; // 记录边是否被访问过

        int current = start;
        result.append(adj[current].getHead().word).append(" ");

        while (true) {
            Node nextNode = selectNextNode(current, visitedEdges);
            if (nextNode == null) {
                break; // 没有可访问的下一个节点，游走结束
            }
            result.append(nextNode.word).append(" ");

            // 获取下一个节点的索引
            int nextIndex = nextNode.num;

            // 检查当前边是否已经访问过
            if (visitedEdges[current][nextIndex]) {
                break; // 当前边已经被访问过，游走结束
            }

            // 标记当前边为已访问
            visitedEdges[current][nextIndex] = true;

            // 移动到下一个节点
            current = nextIndex;
        }

        // 输出随机游走结果到控制台
        System.out.println("随机游走结果：");
        System.out.println(result);

        // 将结果写入文件
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("randomWalk_result.txt"))) {
            writer.write(result.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Selects the next node to visit in a random walk.
     *
     * @param current       the current node index
     * @param visitedEdges the visited edges matrix
     * @return the next node to visit
     */
    private Node selectNextNode(int current, boolean[][] visitedEdges) {
        List<Node> availableNodes = new ArrayList<>();
        for (Node node = adj[current].getHead().next; node != null; node = node.next) {
            if (!visitedEdges[current][node.num]) {
                availableNodes.add(node);
            }
        }
        if (availableNodes.isEmpty()) {
            return null;
        }
        Random random = new Random();
        return availableNodes.get(random.nextInt(availableNodes.size()));
    }

    /**
     * Marks the shortest path on the graph.
     *
     * @param shortestPath the list of nodes in the shortest path
     */
    @SuppressWarnings("checkstyle:LineLength")
    private void markShortestPath(List<String> shortestPath) {
        // 使用 mxGraph 将 JGraphT 图转换为 JGraphX 图
        mxGraph mxGraph = new mxGraph();
        Object parent = mxGraph.getDefaultParent();
        mxGraph.getModel().beginUpdate();

        try {
            // 将顶点添加到图中
            Map<String, Object> vertexMap = new HashMap<>();
            for (String vertex : wordIndexMap.keySet()) {
                Object v = mxGraph.insertVertex(parent, null, vertex, 100, 100, 30, 30,
                        "shape=ellipse;perimeter=ellipsePerimeter;fillColor=none;strokeColor=black;fontColor=black");
                vertexMap.put(vertex, v);
            }

            // 将边添加到图中
            for (int i = 0; i < vertex; i++) {
                for (Node node = adj[i].getHead().next; node != null; node = node.next) {
                    String source = adj[i].getHead().word;
                    String target = node.word;
                    double weight = node.weight;

                    String style = "endArrow=block;endSize=8;strokeColor=black;fillColor=black;fontSize=12;fontColor=red";
                    if (shortestPath.contains(source) && shortestPath.contains(target) &&
                            shortestPath.indexOf(source) == shortestPath.indexOf(target) - 1) {
                        style = "endArrow=block;endSize=8;strokeColor=blue;fillColor=blue;fontSize=12;fontColor=blue";
                    }

                    mxGraph.insertEdge(parent, null, String.valueOf(weight), vertexMap.get(source), vertexMap.get(target), style);
                }
            }
        } finally {
            mxGraph.getModel().endUpdate();
        }

        // 创建一个 Swing 窗口来展示图形
        JFrame frame = new JFrame();
        frame.getContentPane().add(new mxGraphComponent(mxGraph));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setVisible(true);

        // 使用 mxCircleLayout 将图形布局为圆形
        mxCircleLayout layout = new mxCircleLayout(mxGraph);
        layout.execute(mxGraph.getDefaultParent());
    }
}
/**
 * Represents a node in a linked list.
 */
class Node {
    public String word;
    public int weight;
    public Node next;
    public int num;
    public boolean painted;

    public Node() {
        word = null;
        next = null;
        num = weight = 0;
    }

    public Node(String w, int n) {
        word = w;
        weight = 1;
        next = null;
        num = n;
    }
}
/**
 * Represents a linked list.
 */
class LinkedList {
    private Node head = null;
    private Node tail = null;
    public int nodeNum;

    public LinkedList() {
        nodeNum = -1;
    }

    public LinkedList(String w) {
        nodeNum = 0;
        Node newNode = new Node(w, -1);
        head = newNode;
        tail = newNode;
    }

    public boolean isEmpty() {
        return head == null;
    }

    public void addNode(String w, int n) {
        Node newNode = new Node(w, n);
        if (isEmpty()) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        nodeNum++;
    }

    public Node getHead() {
        return head;
    }

    public Node getTail() {
        return tail;
    }
}

