/*
Loads the firewall rules from the input file
Checks wheather specific traffic should be allowed or blocked.
works as a security measure chekcing the files.
*/

import java.io.*;
import java.util.*;

public class Firewall{
    private final Map<String, Set<String>> rules;

    public Firewall() {
        this.rules = new HashMap<>();
    }

    public void loadrules(String name) throws IOException {
        try (BufferedReader read = new BufferedReader(new FileReader(name))) {
            String line;
            while ((line = read.readLine()) != null){
                line = line.trim();
                if (!line.isEmpty()) {
                    String[] parts = line.split("_");
                    if (parts.length == 2) {
                        String key = parts[0];
                        String value = parts[1];

                        rules.putIfAbsent(key, new HashSet<>());
                        rules.get(key).add(value);
                    }
                }
            }
        }
    }
    
    public boolean isAllowed(String source, String destination) {
        String sourceAS = source.split("_")[0];
        String destinationAS = destination.split("_")[0];
        
        if(rules.containsKey(sourceAS) && rules.get(sourceAS).contains("LOCAL") && !sourceAS.equals(destinationAS)) {
            return false;
        }
        return true;
    }

    public void printRules() {
        System.out.println("Loaded Firewall rules: ");
        for (Map.Entry<String, Set<String>> entry : rules.entrySet()) {
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }
    }
    public static void main(String[] args) {
        try{
            Firewall f = new Firewall();
            f.loadrules("firewall_rules.txt");
            f.printRules();

            System.out.println("Traffic from 1_1 to 2_3: " + f.isAllowed("1_1", "2_3"));
            System.out.println("Traffic from 1_1 to 1_2: " + f.isAllowed("1_1", "1_2"));
            System.out.println("Traffic from 2_3 to 1_2: " + f.isAllowed("2_3", "1_2"));           
        } catch (IOException e) {
            System.out.println("Error occured : " + e.getMessage());
        }
    }
}
