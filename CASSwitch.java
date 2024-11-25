/*
Core switch in a star
handles traffic, global traffic forwards to the CCS switch
implements frame forwarding logic, local firewall rules that are recieved from CCS switch
supports multiple connections through threading 
*/ 

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CASSwitch {
        private int port; // port to listen
        private HashMap<Byte, Socket> nodeConnections; //map nodeID to socket
        private ExecutorService threadPool; //handling connections
        private HashMap<Byte, Boolean> firewallRules; // allow or block traffic
        private HashMap<String, Socket> globalconnections; //conenctions to other cas switch or ccs
        ArrayList<CCSSwitch> ccs = new ArrayList<>();

        public CASSwitch(int port){
                this.port = port;
                this.nodeConnections = new HashMap<>();
                this.firewallRules = new HashMap<>();
                this.threadPool = Executors.newCachedThreadPool();
                this.globalconnections = new HashMap<>();
        }

        public void start() {
                try (ServerSocket serverSocket = new ServerSocket(port)) {
                    System.out.println("CAS Switch started on port " + port);
            
                    while (true) {
                        // Accept a new connection
                        Socket socket = serverSocket.accept();
                        System.out.println("Connection received on CAS Switch at port " + port);
            
                        // Submit connection handling to the thread pool
                        threadPool.submit(() -> {
                            try {
                                // Perform tasks for this connection
                                for (int i = 0; i < 3; i++) { // Example: Iterate through tasks
                                    System.out.println("Task " + (i + 1) + " for connection on port " + port);
                                    Thread.sleep(500); // Simulate a task with a small delay
                                }
            
                                // Handle the connection (process frames or data)
                                handleConnection(socket);
            
                            } catch (Exception e) {
                                System.err.println("Error handling connection on port " + port + ": " + e.getMessage());
                            }
                        });
                    }
            
                } catch (IOException e) {
                    System.err.println("Error starting CAS Switch: " + e.getMessage());
                }
            }
            

        private void handleConnection(Socket socket){
                try(DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())){

                        //register node
                        byte nodeID = inputStream.readByte();
                        nodeConnections.put(nodeID, socket);
                        System.out.println("Node " + nodeID + " connected");

                        // listen for frames
                        while(true){
                                byte[] frameBytes = new byte[256];
                                int bytesRead = inputStream.read(frameBytes);

                                if (bytesRead == -1) {
                                Frame frame = Frame.mkFrame(Arrays.copyOf(frameBytes, bytesRead));
                                System.out.println("Received frame from node " + frame.getSource() + " to node " + frame.getDest());

                                if(isAllowedByFirewall(frame.getSource(), frame.getDest())){
                                        forwardFrame(frame);
                                } else {
                                        System.out.println("Blocked frame due to firewall rules: " + frame.getSource() + " -> " + frame.getDest());
                                }
                        } 
                 }
                } catch (IOException e){
                        System.err.println("Error in connection: " + e.getMessage());
                } finally {
                        try {
                                socket.close();
                        } catch (IOException e){
                                System.err.println("Error closing socket: " +  e.getMessage());
                        }
                }
        }

        private boolean isAllowedByFirewall(byte source, byte destination){
                return firewallRules.getOrDefault(destination, true);
        }

        private void forwardFrame(Frame frame) throws IOException{
                byte destination = frame.getDest();

                if(nodeConnections.containsKey(destination)){
                        //local
                        Socket dSocket = nodeConnections.get(destination);
                        DataOutputStream dOutputStream = new DataOutputStream(dSocket.getOutputStream());

                        dOutputStream.write(frame.toBytes());
                        dOutputStream.flush();
                        System.out.println("Forward frame to Node " + destination);
                } else if (globalconnections.containsKey("CCS")) {
                        //gobal so forwarding to ccs
                        Socket ccsSocket = globalconnections.get("CCS");
                        DataOutputStream ccsOutputStream = new DataOutputStream(ccsSocket.getOutputStream());

                        ccsOutputStream.write(frame.toBytes());
                        ccsOutputStream.flush();
                        System.out.println("Forwarded frame to CCSSwitch for global traffic");
                }else {
                        System.out.println("Node " + destination + "not found. Forwarding to ccs...");
                        // forwarding ccs logic here
                }
        }

        public void recieveFrame(Frame frame) {
                System.out.println("Frame recieved from: "+frame.getSource());
                for (Socket nodeSocket: nodeConnections.values()) {
                        try {
                                DataOutputStream outputStream = new DataOutputStream(nodeSocket.getOutputStream());
                                outputStream.write(frame.toBytes());
                                outputStream.flush();
                            } catch (IOException e) {
                                System.err.println("Error flooding frame: " + e.getMessage());
                            }
                }
        }

        public void receiveTraffic(Frame frame) {
                System.out.println("Receiving targeted traffic for frame: " + frame);
                try {
                    forwardFrame(frame);
                } catch (IOException e) {
                    System.err.println("Error forwarding targeted traffic: " + e.getMessage());
                }
            }

        public boolean hasNode(byte node) {
                return nodeConnections.containsKey(node);
        }
        

        public void recieveRules(HashMap<Byte, Boolean> newrules){
                 this.firewallRules = newrules;
                 System.out.println("Firewall rules updated by CCSSwitch");
        }
 
        public void connectCCS(String CCSHost, int port){
                try{
                        Socket CCSsocket = new Socket(CCSHost, port);
                        globalconnections.put("CCS", CCSsocket);
                        System.out.println("Connected to CCS switch at: " +CCSHost + ":" +port);
                } catch (IOException e) {
                        System.out.println("error occured: " + e.getMessage());
                }
        }

        public void shutdown() {
                try {
                        for (Socket socket : nodeConnections.values()) {
                            socket.close();
                        }
                        for (Socket globalSocket : globalconnections.values()) {
                            globalSocket.close();
                        }
                        threadPool.shutdownNow();
                        System.out.println("CAS Switch shut down gracefully.");
                    } catch (IOException e) {
                        System.err.println("Error during shutdown: " + e.getMessage());
                    }
        }

        public void add(CCSSwitch ccsSwitch) {
                ccs.add(ccsSwitch);
                System.out.println("CCS switch added to CAS.");
         }
}
