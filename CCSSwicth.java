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
                rules.add(rule);
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
            //waiting for recieve rules from CAS file
            //c.recieveRules(this.rules);
        }
    }

    public boolean checkRule(Frame frame, ArrayList<String> rules){
        for (String r: rules){
            //need to figure out why source and destination is not working rn b/c there is a squiggle line
            if(frame.source.equals(r.source) && frame.destination.equals(r.destination)){
                return true;
            }
        }
    }

    //frame flooding
    public void flooding(Frame frame){
        for (String c: cas){
            //recieveFrame from CAS file
            //c.recieveFrame(frame);
        }
    }

    //send the traffic
    public void sendTraffic(Frame frame, ArrayList<String> cas){
        //unsure what to do here either
    }

    //traffic handler
    public void trafficHandler(Frame frame){
        if(setRules.checkRule(frame,cas)){//idk what to do here yet
            sendTraffic(frame, cas);

        }else{
            System.out.println("Error: No permission from the firewall");
        }
    }

}
