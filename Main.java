import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Usage: java Main <numNodes> <numCASSwitches> <CCSHost> <CCSPort>");
            return;
        }

        int numNodes = Integer.parseInt(args[0]);
        int numCASSwitches = Integer.parseInt(args[1]);
        String ccsHost = args[2];
        int ccsPort = Integer.parseInt(args[3]);

        // CCS switch
        CCSSwitch ccsSwitch = new CCSSwitch();

        // CCS shadow switch
        CCSShadowSwitch ccsShadowSwitch = new CCSShadowSwitch();

        // CAS switches
        ArrayList<CASSwitch> casSwitches = new ArrayList<>();
        for (int i = 0; i < numCASSwitches; i++) {
            CASSwitch casSwitch = new CASSwitch(5000 + i); 
            casSwitches.add(casSwitch);
        }

        // add CAS switches to the CCS switch
        for (CASSwitch casSwitch : casSwitches) {
            ccsSwitch.addCASSwitch(casSwitch);
        }

        // connect CCS switch to the CAS switches
        try {
            ccsSwitch.readFirewall();
            ccsSwitch.sendRules();
            ccsSwitch.flooding(new Frame((byte) 0, (byte) 0));

            // connect the CCS shadow switch (backup of CCS)
            ccsShadowSwitch.readFirewall();
            ccsShadowSwitch.shadowSendRules();

            //start all CAS switches
            for (CASSwitch casSwitch : casSwitches) {
                Thread casThread = new Thread(casSwitch::start);
                casThread.start();
            }

            // connect the CCS switch to the actual CCS server
            for (CASSwitch casSwitch : casSwitches) {
                casSwitch.connectCCS(ccsHost, ccsPort);
            }

            // start the CCS switch and listen for network traffic
            Thread ccsThread = new Thread(() -> {
                ccsSwitch.start();
            });
            ccsThread.start();

            // waiting for the user to finish or terminate the program
            while (true) {
                try {
                    Thread.sleep(1000); // Simulate program running
                } catch (InterruptedException e) {
                    break; 
                }
            }

            // Clean up (shutdown the network)
            System.out.println("Shutting down the network...");
            for (CASSwitch casSwitch : casSwitches) {
                casSwitch.shutdown();
            }
            ccsSwitch.shutdown();
            ccsShadowSwitch.shutdown();

        } catch (IOException e) {
            System.err.println("Error setting up the network: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
