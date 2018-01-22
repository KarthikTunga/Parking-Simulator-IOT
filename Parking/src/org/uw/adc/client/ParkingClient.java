package org.uw.adc.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ParkingClient extends Thread {
    public void run() {
        try{
            for(int i = 1; i <= 10; ++i){
                final int threadId = i;
                new Thread(() -> test(threadId)).start();
            }

        } catch(Exception ex){
            System.out.println(ex);
        }
    }

    private void test(final int threadId) {
        try {
            while (true) {

                // get all free spots
                String freeSpots = getFreeSpots();
                System.out.println("Client: Thread-" + threadId + "#Empty spots: " + freeSpots);

                if(freeSpots == null || freeSpots.isEmpty()){
                    
                    System.out.println("Client: Thread-" + threadId + "#No free parking spots,waiting for the next free spot");
                    Thread.sleep(100);
                    continue;
                }

                // request to assign first free parking spot
                int spotId = Integer.parseInt(freeSpots.split(",")[0]);
                boolean assignSuccess = requestToAssisgnParkingSpot(spotId);

                if (assignSuccess) {
                    System.out.println("Client: Thread-" + threadId + "#SUCCESS. Car assigned to spotId(" + spotId + ")");

                    Thread.sleep(10);

                    releaseParkingSpot(spotId);
                    System.out.println("Client: Thread-" + threadId + "#RELEASED. spotId(" + spotId + ") now free" );

                    break;
                } else {
                    System.out.println("Client: Thread-" + threadId + "#Failed to assign spotId(" + spotId + ")");
                }
            }
        }catch(Exception ex){
            System.out.println("Thread-"+threadId+"#ERROR : "+ex);
        }
    }

    private String getFreeSpots() throws  IOException{
        Socket s = new Socket("127.0.0.1", 9090);

        // request all free parking spots
        PrintWriter out =
                new PrintWriter(s.getOutputStream(), true);
        out.println("1");
        BufferedReader input =
                new BufferedReader(new InputStreamReader(s.getInputStream()));

        String emptySpots = input.readLine();

        return emptySpots;
    }

    private boolean requestToAssisgnParkingSpot(int spotId) throws Exception{
        // request to assign parking spot
        Socket socket = new Socket("127.0.0.1", 9090);
        PrintWriter out2 =
                new PrintWriter(socket.getOutputStream(), true);
        out2.println("2," + spotId);

        BufferedReader input =
                new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String result = input.readLine();

        return "YES".equals(result) ? true : false;

    }

    private boolean releaseParkingSpot(int spotId) throws IOException {

        // request to release parking spot
        Socket socket = new Socket("127.0.0.1", 9090);
        PrintWriter out2 =
                new PrintWriter(socket.getOutputStream(), true);
        out2.println("3," + spotId);

        BufferedReader input =
                new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String result = input.readLine();

        return "YES".equals(result) ? true : false;
    }
}
