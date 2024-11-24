/* 
JACKY
connects nodes to their assosiated CAS switch
reads input data and sends them to designated nodes
Sends frames/data to the wanted nodes through CAS and CCS.
writes recieved frames/data as an output file

*/

import java.io.*;
import java.net.*;
import java.util.*;

public class Node extends Thread {
        private Socket socket;
        private BufferedReader fileReader;
        private BufferedWriter fileWriter;
        private DataInputStream inputStream;
        private DataOutputStream outputStream;
        private String address;
        private int port;
        private byte nodeID;
        private byte networkID;
        private Map<String, Frame> frameBuffer; //buffer for sent frames
        private Random random;

        public Node(byte networkID, byte nodeID, String address, int port, String inputFile, String outputFile) {
                this.networkID = networkID;
                this.nodeID = nodeID;
                this.address = address;
                this.port = port;
                this.frameBuffer = new HashMap<>();
                this.random = new Random();

                try {
                        this.fileReader = new BufferedReader(new FileReader(inputFile));
                        this.fileWriter = new BufferedWriter(new FileWriter(outputFile));
                } catch (IOException e) {
                        System.out.println("Error initializing reader/writer" + e.getMessage());
                }

        }

        public void connectToSwitch() {
                try {
                        this.socket = new Socket(this.address, this.port);
                        this.inputStream = new DataInputStream(this.socket.getInputStream());
                        this.outputStream = new DataOutputStream(this.socket.getOutputStream());
                        System.out.println("Connected to switch at " + this.address + ":" + this.port);
                } catch (IOException e) {
                        System.err.println("Could not connect to switch: " + e.getMessage());
                }

        }

        //simulate errors with 5% chance
        private boolean simulateError(){
                return random.nextInt(100) < 5;
        }

        //simulate acknoledgement failure with 5% chance
        private boolean simulateACKFail(){
                return random.nextInt(100) < 5;
        }

        public void send() {
                //impliment reading line
                try{
                        String line;
                        while((line = this.fileReader.readLine()) != null){
                                String[] parts = line.split(":");
                                if(parts.length == 2){
                                        byte destNodeID = Byte.parseByte(parts[0]);
                                        byte[] data = parts[1].getBytes();
                                        byte size = (byte) data.length;
                                        Frame frame = new Frame(this.nodeID, destNodeID /*size, data */);

                                        //simulate error
                                        if(simulateError()){
                                                System.out.println("Frame corrupted while trying to read file");
                                                continue; 
                                        }

                                        //frameBuffer
                                        this.frameBuffer.put(frame.getFrameID(), frame);
                                        this.outputStream.write(frame.toBytes());
                                        this.outputStream.flush();
                                        System.out.println("Data sent to node: " + destNodeID);

                                } 
                        }

                } catch (IOException e){
                        System.out.println("Error in reading file or sending data: " + e.getMessage());
                }
        }

        public void recieve() {
                while(true) {
                        try {
                                byte[] receivedData = new byte[256];
                                int length = this.inputStream.read(receivedData);
                                if (length != -1) {
                                        Frame receivedFrame = Frame.mkFrame(Arrays.copyOf(receivedData, length));
                                        if(simulateError()){
                                                System.out.println("Frame corrupted");
                                        } else{
                                                this.fileWriter.write(receivedFrame.getSource() + ": " + new String(receivedFrame.getData()) + "\n");
                                                this.fileWriter.flush();
                                                System.out.println("Data received from: " + receivedFrame.getSource());

                                                //acknowledgement
                                                if(!simulateACKFail()){
                                                        byte ackSize = 0;
                                                        Frame ackFrame = new Frame(this.nodeID, receivedFrame.getSource() /*ackSize, new byte[0] */);
                                                        this.outputStream.write(ackFrame.toBytes());
                                                        this.outputStream.flush();
                                                        System.out.println("Acknowledgement sent to node: " + receivedFrame.getSource());                                     
                                                } else {
                                                        System.out.println("Failed to acknowledge frame");
                                                }
                                        }  
                                }
                        } catch (IOException var4) {
                                System.err.println("Couldn't recieve data: " + var4.getMessage());
                        }
                }
        }

        public void Close() {
        try {
                if (this.fileReader != null) {
                        this.fileReader.close();
                }

                if (this.fileWriter != null) {
                        this.fileWriter.close();
                }

                if (this.inputStream != null) {
                        this.inputStream.close();
                }

                if (this.outputStream != null) {
                        this.outputStream.close();
                }

                if (this.socket != null && !this.socket.isClosed()) {
                        this.socket.close();
                }

                System.out.println("All functions closed.");
        } catch (IOException var2) {
                System.err.println("Error closing the functions: " + var2.getMessage());
        }

        }

        public void run() {
                this.connectToSwitch();
                this.send();
                this.recieve();
                this.Close();
        }
}