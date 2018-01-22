package org.uw.adc.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

public class ParkingServer extends Thread {

    HashMap<Integer, Boolean> parkingLot;

    public ParkingServer() {
        this.parkingLot = BuildEmptyParkingLot();
    }
    
    
    public void run(){
        try{
            ServerSocket listener = new ServerSocket(9090);

            try {
                while (true) {

                    //System.out.println("Waiting for client connections ...");

                    Socket socket = listener.accept();

                    new Thread(() -> serveRequest(socket)).start();
                }
            }
            finally {
                listener.close();
            }
        }catch(Exception ex){
            System.out.println(ex);
        }
    }

    private void serveRequest(Socket socket){

        String dateStr= new Date().toString();

        //System.out.println("Log: "+dateStr+": Got connection from client.");

        try {

            // read request type
            BufferedReader input =
                    new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String request = input.readLine();
            String[] parameters = request.split(",");
            int requestType = Integer.parseInt(parameters[0]);


            switch(requestType){
                case 1 : getEmptySpots(socket);
                         break;

                case 2 : int requestSpotId = Integer.parseInt(parameters[1]);
                         assignParkingSpot(socket, requestSpotId);
                         break;

                case 3 : int freeSpotId = Integer.parseInt(parameters[1]);
                    makeParkingSpotAvailable(socket, freeSpotId);
                    break;

                default: System.out.println("RequestType '"+requestType+"' not supported.");
            }

        }catch(Exception ex){
            System.out.println(ex);
        }
    }

    private void makeParkingSpotAvailable(Socket socket, int spotId){
        synchronized (this.parkingLot){
            this.parkingLot.put(spotId, true);
        }

        try {
            try {
                PrintWriter out =
                        new PrintWriter(socket.getOutputStream(), true);
                out.println("YES");
            } catch (Exception ex) {
                System.out.println(ex);
            } finally {
                socket.close();
            }
        }catch(Exception ex){
            System.out.println(ex);
        }
    }

    private void assignParkingSpot(Socket socket, int spotId){
        boolean result = false;

        synchronized (this.parkingLot){

            // get current status
            boolean isSpotFree = this.parkingLot.get(spotId);

            // spot taken
            if(isSpotFree == false) result = false;
            else{
                this.parkingLot.put(spotId, false);
                result = true;
            }
        }

        try {
            try {
                PrintWriter out =
                        new PrintWriter(socket.getOutputStream(), true);
                out.println(result == true ? "YES" : "NO");
            } catch (Exception ex) {
                System.out.println(ex);
            } finally {
                socket.close();
            }
        }catch(Exception ex){
            System.out.println(ex);
        }
    }

    private void getEmptySpots(Socket socket){

        List<Integer> emptySpots = new LinkedList<>();

        synchronized (this.parkingLot) {
            Set<Integer> keys = this.parkingLot.keySet();
            for (int spotId : keys) {

                // if free
                if (this.parkingLot.get(spotId)) {
                    emptySpots.add(spotId);
                }
            }
        }

        String emptySpotsStr = emptySpots.stream().map(Object::toString)
                .collect(Collectors.joining(", "));

        try {
            try {
                PrintWriter out =
                        new PrintWriter(socket.getOutputStream(), true);
                out.println(emptySpotsStr);
            } catch (Exception ex) {
                System.out.println(ex);
            } finally {
                socket.close();
            }
        }catch(Exception ex){
            System.out.println(ex);
        }
    }

    private HashMap<Integer, Boolean> BuildEmptyParkingLot(){
        HashMap<Integer, Boolean> parking = new HashMap<>();
        for(int i = 0; i < 4; ++i){
            parking.put(i + 1, true);
        }

        return parking;
    }
}
