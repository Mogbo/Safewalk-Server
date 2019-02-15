/**
 * Project 5
 * @author Eshamogbo Ojuba, 0025119841, Y01
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class SafeWalkServer extends ServerSocket implements Runnable {
    /**
     * Construct the server, and create a server socket,
     * bound to the specified port.
     * 
     * @throws IOException IO error when opening the socket.
     */
    
    private ArrayList<Socket> clients;
    private ArrayList<String> name;
    private ArrayList<String> to;
    private ArrayList<String> from;
    
    public SafeWalkServer(int port) throws IOException {
        super(port);
        clients = new ArrayList<Socket>();
        name = new ArrayList<String>();
        to = new ArrayList<String>();
        from = new ArrayList<String>();
    }
    
    /**
     * Construct the server, and create a server socket, 
     * bound to a port that is automatically allocated.
     * 
     * @throws IOException IO error when opening the socket.
     */
    public SafeWalkServer() throws IOException {
        super(0);
        System.out.println("Port not specified. Using free port " + this.getLocalPort());
        clients = new ArrayList<Socket>();
        name = new ArrayList<String>();
        to = new ArrayList<String>();
        from = new ArrayList<String>();
    }
    
    /**
     * Start a loop to accept incoming connections.
     */
    public void run() {
        while (true) {
            try {
                Socket socket = this.accept();
                
                PrintWriter pw;
                BufferedReader bfr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                
                String instruction = bfr.readLine();
                //bfr.close();
                
                // command
                if (instruction.charAt(0) == ':') {
                    if (instruction.equals(":RESET")) {
                        pw = new PrintWriter(socket.getOutputStream(), true);
                        pw.println("RESPONSE: success");
                        pw.close();
                        socket.close();
                        
                        for (int i = 0; i < clients.size(); i++) {
                            socket = clients.get(i);
                            pw = new PrintWriter(socket.getOutputStream(), true);
                            pw.println("ERROR: connection reset");
                            pw.close();
                            socket.close();
                        }
                        
                        clients.clear();
                        name.clear();
                        to.clear();
                        from.clear();
                    }
                    
                    else if (instruction.equals(":SHUTDOWN")) {
                        pw = new PrintWriter(socket.getOutputStream(), true);
                        pw.println("RESPONSE: success");
                        pw.close();
                        socket.close();
                        
                        for (int i = 0; i < clients.size(); i++) {
                            socket = clients.get(i);
                            
                            pw = new PrintWriter(socket.getOutputStream(), true);
                            pw.println("ERROR: connection reset");
                            pw.close();
                            socket.close();
                        }
                        
                        clients.clear();
                        name.clear();
                        to.clear();
                        from.clear();
                        
                        this.close();
                        return;
                    }
                    
                    else if (instruction.substring(0,instruction.indexOf(',')).equals(":PENDING_REQUESTS")) {
                        int firstComma = instruction.indexOf(',');
                        int secondComma = instruction.indexOf(',', firstComma+1);
                        int thirdComma = instruction.indexOf(',', secondComma+1);
                        
                        if (firstComma == -1 || secondComma == -1 || thirdComma == -1) {
                            pw = new PrintWriter(socket.getOutputStream(), true);
                            pw.println("ERROR: invalid command");
                            pw.close();
                            socket.close();
                        }
                        
                        else {
                            
                            String task = instruction.substring(firstComma+1, secondComma);
                            String from = instruction.substring(secondComma+1, thirdComma);
                            String to = instruction.substring(thirdComma+1);
                            
                            
                            if (isCommandValid(task, to, from)) {
                                
                                pw = new PrintWriter(socket.getOutputStream(), true);
                                String message = "";
                                
                                if (task.equals("*")) {
                                    // list all the pending requests
                                    for (int i = 0; i < clients.size(); i++) {
                                        message = message + "[" + this.name.get(i) + ", " + this.from.get(i) + ", " + this.to.get(i) + "]";
                                        if (i != clients.size() - 1) {
                                            message = message + ", ";
                                        }
                                    }  
                                    
                                    System.out.println(this.name.size());
                                    pw.println("[" + message + "]");
                                    pw.close();
                                    socket.close();
                                }
                                
                                // task is #
                                else {
                                    if (to.equals("*") && from.equals("*")) {
                                        // print total number of requests
                                        pw.println("RESPONSE: # of pending requests = " + clients.size());
                                    }
                                    
                                    else if (to.equals("*")) {
                                        int count = 0;
                                        for (int i = 0; i < clients.size(); i++) {
                                            if (this.from.get(i).equals(from)) {
                                                count++;
                                            }
                                        }
                                        pw.println("RESPONSE: # of pending requests from " + from + " = " + count);
                                    }
                                    
                                    else if (from.equals("*")) {
                                        int count = 0;
                                        for (int i = 0; i < clients.size(); i++) {
                                            if (this.to.get(i).equals(to)) {
                                                count++;
                                            }
                                        }
                                        //System.out.println("Ok");
                                        pw.println("RESPONSE: # of pending requests to " + to + " = " + count);
                                    }
                                    
                                    else {
                                        pw.println("ERROR: invalid command");
                                    }
                                }
                                
                                pw.close();
                                socket.close();
                            }
                            
                            else {
                                pw = new PrintWriter(socket.getOutputStream(), true);
                                pw.println("ERROR: invalid command");
                                pw.close();
                                socket.close();
                            }
                            
                        } // end of all three commas being found
                        
                    } // end of pending requests
                    
                    else {
                        pw = new PrintWriter(socket.getOutputStream(), true);
                        pw.println("ERROR: invalid command");
                        socket.close();
                    }
                }
                
                // user input
                else {
                    int firstComma = instruction.indexOf(',');
                    int secondComma = instruction.indexOf(',', firstComma+1);
                    
                    if (firstComma == -1 || secondComma == -1) {
                        pw = new PrintWriter(socket.getOutputStream(), true); 
                        pw.println("ERROR: invalid request");
                        socket.close();
                    }
                    
                    else {
                        
                        String name = instruction.substring(0, firstComma);
                        String from = instruction.substring(firstComma+1, secondComma);
                        String to = instruction.substring(secondComma+1);
                        int i;
                        boolean flag = false;
                        
                        if (isRequestValid(name, from, to)) {
                            
                            for (i = 0; i < clients.size(); i++) {
                                if (from.equals(this.from.get(i)) && (to.equals(this.to.get(i)) || to.equals("*") || this.to.get(i).equals("*")) && !(to.equals("*") && this.to.get(i).equals("*"))) {
                                    // match found, send messages to both, close both sockets, then exit loop
                                    
                                    pw = new PrintWriter(socket.getOutputStream(), true); 
                                    pw.println("RESPONSE: " + this.name.get(i) + ',' + this.from.get(i) + ',' + this.to.get(i));
                                    pw.close();
                                    socket.close();
                                    
                                    Socket socket2 = clients.get(i);
                                    pw = new PrintWriter(socket2.getOutputStream(), true);
                                    pw.println("RESPONSE: " + name + ',' + from + ',' + to);
                                    pw.close();
                                    socket2.close();
                                    
                                    this.from.remove(i);
                                    this.to.remove(i);
                                    this.name.remove(i);
                                    this.clients.remove(i);
                                    
                                    // reinitialize variables
                                    flag = true;
                                    
                                    break;
                                }
                                
                                }
                            
                            // if no match was found we add the client to list of sockets
                            if (flag == false) {
                                System.out.println("flag");
                                clients.add(socket);
                                this.from.add(from);
                                this.to.add(to);
                                this.name.add(name);
                            }  
                            }
                        
                        // if isRequestValid() returns false
                        else {
                            pw = new PrintWriter(socket.getOutputStream(), true);
                            pw.println("ERROR: invalid request");
                            pw.close();
                            socket.close();
                        }
                        }
                }
            }  // end of user input
            catch (IOException e) {
                e.printStackTrace();
            }
            }
    }
    
    /**
     * Return true if the port entered by the user is valid. Else return false. 
     * Return false if you get a NumberFormatException while parsing the parameter port
     * Call this method from main() before creating SafeWalkServer object 
     * Note that you do not have to check for validity of automatically assigned port
     */
    public static boolean isPortValid(String port) {
        
        try {
            int p = Integer.parseInt(port);
            return (p >= 1025 && p <= 65535);
        }
        catch (NumberFormatException e) {
            return false;
        }
    }
    
    public static boolean isRequestValid(String name, String from, String to) {
        if (name == null || from == null || to == null || name.equals(""))
            return false;
        if (from.equals("*"))
            return false;
        if (!(from.equals("CL50") || from.equals("EE") || from.equals("LWSN") || from.equals("PMU") || from.equals("PUSH")))
            return false;
        if (!(to.equals("CL50") || to.equals("EE") || to.equals("LWSN") || to.equals("PMU") || to.equals("PUSH") || to.equals("*")))
            return false;
        return true;
    }
    
    // Only checks to see if allowed values are passed for each task # or *
    // Does not check everything.
    public static boolean isCommandValid(String task, String from, String to) {
        if (!(task.equals("#") || task.equals("*")))
            return false;
        if (!(from.equals("CL50") || from.equals("EE") || from.equals("LWSN") || from.equals("PMU") || from.equals("PUSH") || from.equals("*")))
            return false;
        if (!(to.equals("CL50") || to.equals("EE") || to.equals("LWSN") || to.equals("PMU") || to.equals("PUSH") || to.equals("*")))
            return false;
        if (task.equals("*") && !(to.equals("*") && from.equals("*")))
            return false;
        return true;
    }
    
    public static void main(String args[]) {
        if (args.length == 0) {
            try { 
                SafeWalkServer server = new SafeWalkServer();
                server.setReuseAddress(true);
                Thread t1 = new Thread(server);
                t1.start();
            }
            
            catch (IOException e) {
                System.out.println("Port already in use");
            }            
        }
        
        else if (isPortValid(args[0])) {
            try {
                SafeWalkServer server = new SafeWalkServer(Integer.parseInt(args[0]));
                server.setReuseAddress(true);
                Thread t1 = new Thread(server);
                t1.start();
            }
            
            catch (IOException e) {
                System.out.println("Port already in use");
            }
        }
        
        else {
            System.out.println("Invalid Port Number");
        }
    }
    }