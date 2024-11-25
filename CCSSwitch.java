/*
NANCY
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
                    // rules.add(key);
                    // rules.add(value);
                }
            }
            System.out.println("File received and read");
            System.out.println(rules);//for testing purposes
        }catch (IOException e){
            System.out.println("File not found.");
            e.printStackTrace();
        }
    }

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

    public void addCASSwitch(int port) {
        CASSwitch CASSwitch = new CASSwitch(port);
        cas.add(CASSwitch);
        System.out.println("CAS switch added to CCS.");
    }

    public void shutdown() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'shutdown'");
    }

    public void start() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'start'");
    }

}