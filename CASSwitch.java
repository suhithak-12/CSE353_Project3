/*
JACKY
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
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CASSwitch {
        private int port; // port to listen
        private HashMap<Byte, Socket> nodeConnections; //map nodeID to socket
        private ExecutorService threadPool; //handling connections
        private HashMap<Byte, Boolean> firewallRules; // allow or block traffic

        public CASSwitch(int port){
                this.port = port;
                this.nodeConnections = new HashMap<>();
                this.firewallRules = new HashMap<>();
                this.threadPool = Executors.newCachedThreadPool();
        }

        public void start(){
                try(ServerSocket serverSocket = new ServerSocket(port)){
                System.out.println("CAS Switch started on port " + port);

                while(true){
                        Socket socket = serverSocket.accept();
                        threadPool.submit(() -> handleConnection(socket));
                }

                }
        }
}