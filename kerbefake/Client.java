package kerbefake;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {

  private static final String AUTH_SERVER_ADDRESS = "127.0.0.1";
  private static final int AUTH_SERVER_PORT = 1234;
  private static final String MESSAGE_SERVER_ADDRESS = "message_server_address";
  private static final int MESSAGE_SERVER_PORT = 1256;
  private static final int CLIENT_VERSION = 24;
  private static final String CLIENT_INFO_FILE = "info.me"; // File name for client details
  private static final String MESSAGE_SERVER_INFO_FILE = "info.msg";

  private String ipAddress;
  private int portNumber;
  private String name;
  private String uniqueIdentifier;

  public static void main(String[] args) {
    Client client = new Client();
    client.run();
  }

  public void run() {
    try {
      // Connect to the authentication server
      Socket authSocket = new Socket(AUTH_SERVER_ADDRESS, AUTH_SERVER_PORT);
      BufferedReader authIn = new BufferedReader(new InputStreamReader(authSocket.getInputStream()));
      PrintWriter authOut = new PrintWriter(authSocket.getOutputStream(), true);

      // Register with the authentication server if not already registered
      if (!isRegistered(authIn, authOut)) {
        register(authIn, authOut);
      }

      // Connect to the message server
      Socket messageSocket = new Socket(MESSAGE_SERVER_ADDRESS, MESSAGE_SERVER_PORT);
      BufferedReader messageIn = new BufferedReader(new InputStreamReader(messageSocket.getInputStream()));
      PrintWriter messageOut = new PrintWriter(messageSocket.getOutputStream(), true);

      // Perform operations in fixed order for batch mode
      performOperations(messageOut);

      // Read client info from file explicitly
      readClientInfoFromFile();
      // Print the loaded client details
      System.out.println("Loaded client details:");
      System.out.println("IP Address: " + ipAddress);
      System.out.println("Port Number: " + portNumber);
      System.out.println("Name: " + name);
      System.out.println("Unique Identifier: " + uniqueIdentifier);

      // Request a Service Ticket for a specific service
      String clientIdentity = "username";
      String service = "message_server";
      String serviceTicketRequest = createServiceTicketRequest(clientIdentity, service);
      messageOut.println(serviceTicketRequest); // Send the request to the message server

      // Receive the Service Ticket from the message server
      String serviceTicket = messageIn.readLine();
      System.out.println("Service Ticket received: " + serviceTicket);

      // Close connections
      authSocket.close();
      messageSocket.close();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Method to create a Service Ticket request
  private static String createServiceTicketRequest(String clientIdentity, String service) {
    // Construct a service ticket request with client identity and service name
    return "SERVICE_TICKET_REQUEST|" + clientIdentity + "|" + service;
  }

  private static boolean isRegistered(BufferedReader authIn, PrintWriter authOut) throws Exception {
    // Check if client is already registered with the authentication server
    authOut.println("CHECK_REGISTRATION " + CLIENT_VERSION);
    String response = authIn.readLine();
    return response.equals("REGISTERED");
  }

  private static void register(BufferedReader authIn, PrintWriter authOut) throws Exception {
    // Register with the authentication server
    authOut.println("REGISTER " + CLIENT_VERSION);
    String response = authIn.readLine();
    if (response.equals("SUCCESS")) {
      System.out.println("Registration successful.");
    } else {
      throw new Exception("Registration failed.");
    }
  }

  private static void performOperations(PrintWriter messageOut) {
    // Perform operations in fixed order for batch mode
    messageOut.println("SEND_MESSAGE Hello, Message Server!");
    // Add more operations as needed
  }

  // Method to read client details from the file
  private void readClientInfoFromFile() {
    try (BufferedReader reader = new BufferedReader(new FileReader(CLIENT_INFO_FILE))) {
      // Read IP address and port number from the first line
      String[] addressPort = reader.readLine().split(":");
      ipAddress = addressPort[0];
      portNumber = Integer.parseInt(addressPort[1]);

      // Read name from the second line
      name = reader.readLine();

      // Read unique identifier from the third line
      uniqueIdentifier = reader.readLine();
    } catch (IOException | ArrayIndexOutOfBoundsException | NumberFormatException e) {
      System.err.println("Error reading client info file: " + e.getMessage());
    }
  }

  private String getClientInfoFilePath() {
     String executionDirectory = System.getProperty("user.dir");

    // Construct the absolute path to the info.me file
    return executionDirectory + File.separator + CLIENT_INFO_FILE;
  }

  // Save client details to the info.me file
  public void saveClientInfoToFile() {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(CLIENT_INFO_FILE))) {
      // Write IP address and port number to the first line
      writer.write(ipAddress + ":" + portNumber);
      writer.newLine();

      // Write name to the second line
      writer.write(name);
      writer.newLine();

      // Write unique identifier to the third line
      writer.write(uniqueIdentifier);
    } catch (IOException e) {
      System.err.println("Error saving client info file: " + e.getMessage());
    }
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public int getPortNumber() {
    return portNumber;
  }

  public void setPortNumber(int portNumber) {
    this.portNumber = portNumber;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUniqueIdentifier() {
    return uniqueIdentifier;
  }

  public void setUniqueIdentifier(String uniqueIdentifier) {
    this.uniqueIdentifier = uniqueIdentifier;
  }
}