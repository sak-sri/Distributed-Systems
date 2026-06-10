import com.google.gson.Gson;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class Node {
    private int myPort;
    private String nodeName;
    private int[] otherNodesPort;
    private volatile int myClock =0 ;   /* This is declared Volatile so
                                           that every thread reads the last
                                           updated value from Main Memory.
                                           This deals with Java Visibility issues.*/
    private final String SEND="SEND";
    private final String RECEIVE="RECEIVE";
    Map<Integer,PrintStream> outConnections;
    Map<Integer,BufferedReader> inConnections;
    private CountDownLatch latch; /*
                                     We are using latch as
                                     each node creates n-1 client  threads
                                     to communicate with remaining n-1 nodes.
                                     We want to make sure that the node establishes connection with n-1 nodes
                                     before it tries to communicate with them using take input happens.
                                     The value of latch is initialized with n-1.
                                     Whenever a thread finishes establishing Client connection
                                     it executes latch.countDown() which decrements value of latch by 1.

                                   */
    private CountDownLatch serverConfirmationLatch;

    public Node(int port, String nodeName , int[] otherNodesPort) {
        this.myPort = port;
        this.nodeName = nodeName;
        this.otherNodesPort = otherNodesPort;
        outConnections=new HashMap<>();
        inConnections=new HashMap<>();
        latch=new CountDownLatch(otherNodesPort.length);
        this.serverConfirmationLatch=new CountDownLatch(1);

    }
    /*
        The updateClock Method is declared Synchronized to Prevent Race Condition
        Only One Thread can enter this method at a time
     */
    public synchronized void updateClock(int myClock,String operation){
        System.out.println("Node Name : " + nodeName);
        System.out.println("Node Port : " + myPort);
        System.out.println("Operation : " + operation);
        System.out.println("Current Clock : " + this.myClock);
        if(SEND.equals(operation)){
            this.myClock++;
        }else if(RECEIVE.equals(operation)){
            this.myClock = Math.max(this.myClock,myClock)+1;
        }
        System.out.println("Updated Clock : " + this.myClock);


    }
    public class ServerThread implements Runnable {
        private Socket clientSocket;
        public ServerThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }
        @Override
        public void run(){

            BufferedReader in = null;
            PrintStream out = null;
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintStream(clientSocket.getOutputStream(), true);
                String message;
                Gson gson = new Gson();

                 /* in.readline
                    stores message received from client in message ,when we are not sending
                    anything to it then it just blocks the thread
                    if connection is terminated then it returns null
                 */
                while ((message = in.readLine()) != null) {
                    System.out.println("Message Received : " + message);
                    Message msg = gson.fromJson(message, Message.class);
                    /*Putting below in synchronized so that
                       only one thread can update clock and ack that value to
                       client
                    */
                    synchronized (Node.this) {
                        updateClock(msg.getMyClock(), RECEIVE);
                        String ack = "I have received your message";
                        Message ackObj = new Message(nodeName, ack, myClock);
                        out.println(gson.toJson(ackObj));
                    }
                }
                clientSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void serverSide() throws IOException {
        ServerSocket serverSocket = new ServerSocket(myPort);
        System.out.println(nodeName + " Listening on Port " +  myPort);
        serverConfirmationLatch.countDown();
        while(true) {
            Socket clientSocket = serverSocket.accept();
            /*this line accepts
            connection request
            from client nodes
            */

            Thread thread = new Thread(new ServerThread(clientSocket));
            /*
                creating a new thread to handle connection with client.
                creating a new thread ensures that our server is able to
                handle multiple connection with clients.
                If there was only one thread to establish connection then it would remain stuck in
                below loop and the thread would never have reached to Socket clientSocket = serverSocket.accept();
                Even if it reached somehow then the old connection would be lost for creating new connection.

                while ((message = in.readLine()) != null) {
                    System.out.println("Message Received : " + message);
                    Message msg = gson.fromJson(message, Message.class);
                    synchronized (Node.this) {
                        updateClock(msg.getMyClock(), RECEIVE);
                        String ack = "I have received your message";
                        Message ackObj = new Message(nodeName, ack, myClock);
                        out.println(gson.toJson(ackObj));
                    }
                }

             */


            thread.start();
        }
    }
    private class Message{
        private String message;
        private int myClock;
        private String sender;
        public Message(String sender,String message,int myClock){
            this.message = message;
            this.myClock = myClock;
            this.sender = sender;
        }

        public String getMessage() {
            return message;
        }

        public int getMyClock() {
            return myClock;
        }

        public String getSender() {
            return sender;
        }
    }
    public void clientSide(int serverPort) throws IOException {
        Socket socket = new Socket("localhost", serverPort);
        System.out.println(nodeName +"("+ myPort + ")" + " created Connection with Port "+ serverPort);
        PrintStream out = new PrintStream(socket.getOutputStream(),true);
        outConnections.put(serverPort,out);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        inConnections.put(serverPort,in);
        latch.countDown();
    }


    public void startServer() throws IOException {

        /*
            Handing over the responsibility of creating server to separate thread
            so that main thread can move on to create other node servers
         */
        Thread serverHandler = new Thread(()->{
            try {
                serverSide();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        serverHandler.start();


    }
    public void startClient() throws InterruptedException {
        for(int i = 0; i<otherNodesPort.length; i++) {
            final int portNumber = otherNodesPort[i];

            /*
                Here Giving the responsibility of creating a socket
                connection to different threads so that
                my main thread doesn't have to wait for one socket
                connection to establish before moving on to create next socket connection.
                By Doing this we are ensuring parallel connection establishment with other nodes.

             */
            Thread clientHandler = new Thread(() -> {
                try {
                    clientSide(portNumber);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            clientHandler.start();
        }
    }
    // takeInput handled by a separate thread
    public void takeInput(String nodePort,String message) throws IOException {
        PrintStream out = outConnections.get(Integer.parseInt(nodePort));
        Gson gson = new Gson();
        synchronized (Node.this) {
            updateClock(myClock, SEND);
            Message msg = new Message(nodeName, message, myClock);
            out.println(gson.toJson(msg));
        }
        String response = inConnections.get(Integer.parseInt(nodePort)).readLine();
        Message responseMsg = gson.fromJson(response, Message.class);
        System.out.println("Response: " + responseMsg.getMessage());
    }
    public void waitForConnections() throws InterruptedException {
        latch.await(); // wait until all clients connected
        /*
         If we do not use latch.await then main thread may move to taking input too quickly
         and it is possible that connections have not been established yet
         */
        System.out.println(nodeName + " all connections ready!");
    }

    public int getMyPort() {
        return myPort;
    }

    public void confirmServerCreation() throws InterruptedException {
        serverConfirmationLatch.await();
        System.out.println(nodeName + " Server Created");
    }

}
