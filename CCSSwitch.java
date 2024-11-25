/*
Central core switch for the SoSs network
creates a bridge bwtween CAS swicthes for global communication
implements fire wall rules (globally)
ACKs for global traffic
sends local firewall rules to CAS swicthes
Handles traffic bw CAS switches
*/

import java.io.*;
import java.util.*;

public class CCSSwitch {
    Map<String, Set<String>> rules = new HashMap<>();
    ArrayList<CASSwitch> cas = new ArrayList<>();

    //read the rules of the firewall
    public void readFirewall() throws IOException{
        File fireRules = new File("firewall_rules.txt");
        try(BufferedReader br = new BufferedReader(new FileReader(fireRules))){
            String rule;
            while ((rule = br.readLine()) != null){
                String[] rls = rule.split("_");
                if (rls.length==2){
                    String key = rls[0];
                    String value = rls[1];

                    rules.putIfAbsent(key, new HashSet<>());
                    rules.get(key).add(value);
                }
            }
            System.out.println("File received and read");
            System.out.println(rules);//for testing purposes
        }catch (IOException e){
            System.out.println("File not found.");
            e.printStackTrace();
        }
    }

    //sends rules to the CAS Switch
    public void sendRules(){
        for (CASSwitch c : cas){

            HashMap<Byte, Boolean> convertedRules = new HashMap<>();
           
            for (Map.Entry<String, Set<String>> entry : rules.entrySet()){
                String source = entry.getKey();
                Set<String> destinations = entry.getValue();

                byte nodeID = (byte) Integer.parseInt(source);
                boolean isAllowedByFirewall = !destinations.isEmpty();
                convertedRules.put(nodeID, isAllowedByFirewall);
            }

            c.recieveRules(convertedRules);
        }
        System.out.println("Firewall rules sent to all CAS switches.");
    }

    //checks if the rules match up
    public boolean checkRule(Frame frame){
        String source = Byte.toString(frame.getSource());
        String destination = Byte.toString(frame.getDest());

        if (rules.containsKey(source)) {
            return rules.get(source).contains(destination);
        }
        return true;
    }

    //frame flooding
    public void flooding(Frame frame){
        for (CASSwitch c: cas){
            //recieveFrame from CAS file
            c.recieveFrame(frame);
        }
        System.out.println("Frame flooded to all CAS switches.");
    }

    //sends the traffic to CAS
    public void sendTraffic(Frame frame){
        Byte destination = frame.getDest();
        boolean found = false; 

        for (CASSwitch c : cas) {
            if (c.hasNode(destination)) {
                c.receiveTraffic(frame);
                System.out.println("Traffic sent to " + destination);
                found = true;
                break;
            }
        }
        if (!found) {
        System.out.println("destinatnion not found for "+frame);
        }
    }

    //traffic handler
    public void trafficHandler(Frame frame){
        if(checkRule(frame)){//idk what to do here yet
            sendTraffic(frame);
            System.out.println("Traffic has been sent to:" + frame.getSource() + "and" + frame.getDest() + "\n");

        }else{
            System.out.println("Error: No permission from the firewall");
        }
    }

    /*public void add(int port) {
        CASSwitch CASSwitch = new CASSwitch(port);
        cas.add(CASSwitch);
        System.out.println("CAS switch added to CCS.");
    }*/

    //shutdowns the CCS switch
    public void shutdown() {
        System.out.println("Shutting down CCS Switch...");
        for (CASSwitch casSwitch : cas) {
            casSwitch.shutdown();
        }
        System.out.println("CCS Switch shut down successfully.");
    }

    //starting the CCS Switch
    public void start() {
        System.out.println("Starting CCS Switch...");
        try {
            // Load firewall rules
            readFirewall();
            sendRules();

            // Start all connected CAS switches
            for (CASSwitch casSwitch : cas) {
                new Thread(casSwitch::start).start();
            }

            System.out.println("CCS Switch is running.");
        } catch (IOException e) {
            System.err.println("Error starting CCS Switch: " + e.getMessage());
        }
    }

    //Adding the CAS Switch
    public void add(int basePort, int index) {
        int port = basePort + index; 
        CASSwitch casSwitch = new CASSwitch(port);
        cas.add(casSwitch);
        System.out.println("CAS switch added to CCS: " + casSwitch);
    }
    }
