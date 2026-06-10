import java.io.IOException;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
            // 5001 , 5002 , 5003 , 5004 , 5005
            Node node1 = new Node(5001,"Node1", new int[]{5002, 5003, 5004, 5005});
            Node node2 = new Node(5002,"Node2", new int[]{5001,5003, 5004, 5005});
            Node node3 = new Node(5003,"Node3", new int[]{5001,5002,5004, 5005});
            Node node4 = new Node(5004,"Node4", new int[]{5001,5002,5003,5005});
            Node node5 = new Node(5005,"Node5", new int[]{5001,5002,5003,5004});
            node1.startServer();
            node2.startServer();
            node3.startServer();
            node4.startServer();
            node5.startServer();


           node1.confirmServerCreation();
           node2.confirmServerCreation();
           node3.confirmServerCreation();
           node4.confirmServerCreation();
           node5.confirmServerCreation();

           /* Might be possible that client tries
              to establish connection but server has
              not yet been created as creating a server handled by different thread */


           node1.startClient();
           node2.startClient();
           node3.startClient();
           node4.startClient();
           node5.startClient();

        node1.waitForConnections();
        node2.waitForConnections();
        node3.waitForConnections();
        node4.waitForConnections();
        node5.waitForConnections();

        InputHandler input = new InputHandler();
        input.addNode(node1);
        input.addNode(node2);
        input.addNode(node3);
        input.addNode(node4);
        input.addNode(node5);
        input.initializeInput();

        }

}