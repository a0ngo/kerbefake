package kerbefake.client;

import java.util.Scanner;
import kerbefake.MessageReceiver;
import kerbefake.MessageSender;

public class ClientMain {
    private static final Scanner scanner = new Scanner(System.in);
    private ClientConfigLoader configLoader;
    private ClientNetworkHandler networkHandler;
    private ClientAuthenticator authenticator;
    private MessageSender messageSender;
    private MessageReceiver messageReceiver;

    public ClientMain() {
        configLoader = new ClientConfigLoader();
        networkHandler = new ClientNetworkHandler(configLoader.getServerAddress(), configLoader.getServerPort());
        authenticator = new ClientAuthenticator(networkHandler);
        messageSender = new MessageSender(networkHandler);
        messageReceiver = new MessageReceiver(networkHandler);

        messageReceiver.startListening();
    }

    private void displayMenu() {
        System.out.println("\nPlease select an action:");
        System.out.println("1. Register");
        System.out.println("2. Send Message");
        System.out.println("3. Exit");
        System.out.print("Your choice: ");
    }

    private void registerClient() {
        if (authenticator.isRegistered()) {
            System.out.println("Client already registered.");
            return;
        }
        
        String username = promptForUsername();
        String password = promptForPassword();
        
        boolean success = authenticator.register(username, password);
        if (success) {
            System.out.println("Registration successful.");
        } else {
            System.out.println("Registration failed.");
        }
    }

    private void sendMessage() {
        System.out.print("Enter recipient ID: ");
        String recipientId = scanner.next();
        System.out.print("Enter your message: ");
        scanner.nextLine();
        String message = scanner.nextLine();
        
        boolean success = messageSender.sendMessage(recipientId, message);
        if (success) {
            System.out.println("Message sent successfully.");
        } else {
            System.out.println("Failed to send message.");
        }
    }

    private String promptForUsername() {
        System.out.print("Enter username: ");
        return scanner.next();
    }

    private String promptForPassword() {
        System.out.print("Enter password: ");
        return scanner.next();
    }

    public static void main(String[] args) {
        ClientMain clientMain = new ClientMain();
        
        while (true) {
            clientMain.displayMenu();
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    clientMain.registerClient();
                    break;
                case 2:
                    clientMain.sendMessage();
                    break;
                case 3:
                    System.out.println("Exiting client.");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please select a valid option.");
            }
        }
    }
}
