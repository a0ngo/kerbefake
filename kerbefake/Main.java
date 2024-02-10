package kerbefake;

import java.util.Scanner;

import static kerbefake.Logger.error;
import static kerbefake.Logger.print;

public class Main {

    public static void main(String[] args) {
        int modeSelection = -1;

        Scanner userInputScanner = new Scanner(System.in);
        print("Please select mode of operation:\n1) Client mode\n2) Auth Server\n3) Messaging Server");

        do {
            try {
                modeSelection = userInputScanner.nextInt();
                if (modeSelection > 3 || modeSelection < 1) {
                    throw new Exception();
                }
                break;
            } catch (Exception e) {
                error("Invalid input, please provide a number (1/2/3).");
            }
        } while (true);

        switch (modeSelection) {
            case Constants.MODE_CLIENT:
//                new Client();
                break;
            case Constants.MODE_AUTH:
                new AuthServer();
                break;
            case Constants.MODE_SERVER:
                new MessageServer();
                break;
        }

    }
}
