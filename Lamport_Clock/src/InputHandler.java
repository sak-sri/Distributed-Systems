import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class InputHandler {
    Map<Integer,Node> nodes;
    public InputHandler(){
        nodes=new HashMap<>();
    }
    public void addNode(Node node){
        nodes.put(node.getMyPort(),node);
    }
    public void takeInput(){
        Scanner sc=new Scanner(System.in);
        while(true){
            try{
                System.out.println("Enter port from which you want to send");
                int port= Integer.parseInt(sc.nextLine());
                System.out.println("Enter port to which you want to send");
                int recipientPort= Integer.parseInt(sc.nextLine());
                Node node=nodes.get(port);
                System.out.println("Enter Message");
                String message=sc.nextLine();
                node.takeInput(String.valueOf(recipientPort),message);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    // assigning input responsibility to separate thread
    public void initializeInput(){
        Thread inputThread=new Thread(()->{takeInput();});
        inputThread.start();
    }
}
