package org.uw.adc.main;

import org.uw.adc.client.ParkingClient;
import org.uw.adc.server.ParkingServer;


public class EntryPoint {
    public static void main(String[] args) {
        try { 

            // start server
            new ParkingServer().start();

            System.out.println("Started server....");
            Thread.sleep(2000);

            // start client
            new ParkingClient().start();

            System.in.read();
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }
}


