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
    ArrayList<String> rules= new ArrayList<>();
    ArrayList<String> cas = new ArrayList<>();

    //read the rules of the firewall
    public void readFirewall() throws IOException{
        File fireRules = new File("firewall_rules.txt");
        try(BufferedReader br = new BufferedReader(new FileReader(fireRules))){
            String rule;
            while ((rule = br.readLine()) != null){
                String[] rls = rule.split("_");
                if (rls.length==2){
                    String key = rls[0];
                    String value = rls[0];
                    
                    rules.add(key);
                    rules.add(value);
                }
            }
            System.out.println("File received and read");
            System.out.println(rules);//for testing purposes
        }catch (IOException e){
            System.out.println("File not found.");
            e.printStackTrace();
        }
    }

    public void setRules(ArrayList<String> rules){
        this.rules = rules;
    }

    public void sendRules(){
        for (String c : cas){
            //recieve rules from CAS file
            c.recieveRules(this.rules);
        }
    }

    public boolean checkRule(Frame frame, ArrayList<String> rules){
        for (String r: rules){
            //need to figure out why source and destination is not working rn b/c there is a squiggle line
            if(frame.source == r.source && frame.destination == r.destination){
                return true;
            }
        }
        return false;
    }

    //frame flooding
    public void flooding(Frame frame){
        for (String c: cas){
            //recieveFrame from CAS file
            c.recieveFrame(frame);
        }
    }

    //send the traffic
    public void sendTraffic(Frame frame, ArrayList<String> cas){
        for(String c: cas){
            //will this work?
            c.recieveTraffic(frame, cas);
        }
        System.out.println("Traffic Sent");
    }

    //traffic handler
    public void trafficHandler(Frame frame){
        if(checkRule(frame,rules)){//idk what to do here yet
            sendTraffic(frame, cas);
            System.out.println("Traffic has been sent to:" + frame.getSource() + "and" + frame.getDest() + "\n");

        }else{
            System.out.println("Error: No permission from the firewall");
        }
    }

}