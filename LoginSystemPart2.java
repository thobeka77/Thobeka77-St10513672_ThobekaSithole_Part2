package com.mycompany.LoginSystemPart2;

import java.util.*;
import java.io.*;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class LoginSystemPart2 {

    private static boolean exit = false;
    private static int maxMessages = 0;
    private static int totalMessages = 0;
    private static int messageCounter = 0;

    static final JSONArray messageStorage = new JSONArray();
    static Scanner scanner = new Scanner(System.in);

    // Stored login details
    static String storedUsername = "";
    static String storedPassword = "";

    // ---------------- MAIN METHOD ----------------

    public static void main(String[] args) {

        System.out.println("Welcome to ChatIT");

        // ---------------- REGISTRATION ----------------

        String username;

        while (true) {

            System.out.print("Enter Username (must contain '_' and max 5 chars): ");
            username = scanner.nextLine();

            if (checkUserName(username)) {
                break;
            }

            System.out.println("Invalid username.");
        }

        String password;

        while (true) {

            System.out.print("Enter Password (8+ chars, capital, number, special char): ");
            password = scanner.nextLine();

            if (checkPasswordComplexity(password)) {
                break;
            }

            System.out.println("Invalid password.");
        }

        storedUsername = username;
        storedPassword = password;

        System.out.println(registerUser(username, password));

        // ---------------- CELL NUMBER ----------------

        String cellPhone;

        while (true) {

            System.out.print("Enter Cell Phone (+27 followed by 9 digits): ");
            cellPhone = scanner.nextLine();

            if (checkCellPhoneNumber(cellPhone)) {

                System.out.println("Cell phone number successfully added.");
                break;
            }

            System.out.println("Invalid number.");
        }

        // ---------------- LOGIN ----------------

        boolean status = false;

        while (!status) {

            System.out.print("Enter Username: ");
            String loginUser = scanner.nextLine();

            System.out.print("Enter Password: ");
            String loginPass = scanner.nextLine();

            status = loginUser(
                    loginUser,
                    loginPass,
                    storedUsername,
                    storedPassword
            );

            System.out.println(returnLoginStatus(status));
        }

        // ---------------- MESSAGE LIMIT ----------------

        try {

            System.out.print("How many messages do you wish to send? ");
            maxMessages = Integer.parseInt(scanner.nextLine());

        } catch (NumberFormatException e) {

            System.out.println("Invalid input.");
            return;
        }

        // ---------------- MENU ----------------

        while (!exit) {

            System.out.println("\nSelect an Option:");
            System.out.println("1. Post Message");
            System.out.println("2. Previous Messages");
            System.out.println("3. Exit");
            System.out.print("Choice: ");

            int choice;

            try {

                choice = Integer.parseInt(scanner.nextLine());

            } catch (NumberFormatException e) {

                System.out.println("Invalid choice.");
                continue;
            }

            switch (choice) {

                case 1:

                    if (totalMessages < maxMessages) {

                        sendMessage();

                    } else {

                        System.out.println("Maximum messages reached.");
                    }

                    break;

                case 2:

                    showRecentlySentMessages();
                    break;

                case 3:

                    saveMessagesToJSON();
                    exit = true;

                    System.out.println("Goodbye!");
                    break;

                default:

                    System.out.println("Invalid choice.");
            }
        }
    }

    // ---------------- USERNAME VALIDATION ----------------

    public static boolean checkUserName(String username) {

        return username.contains("_") && username.length() <= 5;
    }

    // ---------------- PASSWORD VALIDATION ----------------

    public static boolean checkPasswordComplexity(String password) {

        String regex =
                "^(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$";

        return Pattern.matches(regex, password);
    }

    // ---------------- CELL NUMBER VALIDATION ----------------

    public static boolean checkCellPhoneNumber(String number) {

        return number.matches("^\\+27\\d{9}$");
    }

    // ---------------- REGISTER USER ----------------

    public static String registerUser(String username, String password) {

        if (!checkUserName(username)) {

            return "Username is incorrect.";
        }

        if (!checkPasswordComplexity(password)) {

            return "Password is incorrect.";
        }

        return "User registered successfully!";
    }

    // ---------------- LOGIN USER ----------------

    public static boolean loginUser(
            String username,
            String password,
            String storedUsername,
            String storedPassword
    ) {

        return username.equals(storedUsername)
                && password.equals(storedPassword);
    }

    // ---------------- LOGIN STATUS ----------------

    public static String returnLoginStatus(boolean status) {

        if (status) {

            return "Login successful! Welcome back!";

        } else {

            return "Username or password incorrect.";
        }
    }

    // ---------------- SEND MESSAGE ----------------

    static void sendMessage() {

        long messageId =
                1000000000L + new Random().nextInt(900000000);

        messageCounter++;

        System.out.print("Input recipient number (+CCXXXXXXXXXXX): ");

        String recipient = scanner.nextLine();

        recipient = checkRecipient(recipient);

        if (recipient == null) {
            return;
        }

        System.out.print("Enter your message (max 250 characters): ");

        String message = scanner.nextLine();

        if (message.trim().isEmpty()) {

            System.out.println("Message cannot be empty.");
            return;
        }

        if (message.length() > 250) {

            System.out.println("Message exceeds 250 characters.");
            return;
        }

        String[] words = message.trim().split("\\s+");

        String hash = String.format(
                "%02d:%d:%s%s",
                Integer.parseInt(
                        Long.toString(messageId).substring(0, 2)),
                messageCounter,
                words[0].toUpperCase(),
                words.length > 1
                        ? words[words.length - 1].toUpperCase()
                        : ""
        );

        System.out.println("\nSelect action:");
        System.out.println("1. Post");
        System.out.println("2. Cancel");
        System.out.println("3. Archive");

        int action;

        try {

            action = Integer.parseInt(scanner.nextLine());

        } catch (NumberFormatException e) {

            System.out.println("Invalid option.");
            return;
        }

        if (action == 2) {

            System.out.println("Message cancelled.");
            return;
        }

        JSONObject jsonMessage = new JSONObject();

        jsonMessage.put("MessageID", messageId);
        jsonMessage.put("MessageHash", hash);
        jsonMessage.put("Recipient", recipient);
        jsonMessage.put("Message", message);

        messageStorage.add(jsonMessage);

        if (action == 3) {

            System.out.println("Message archived.");
            return;
        }

        totalMessages++;

        System.out.println("\nMessage Sent.");
        System.out.println("Message ID: " + messageId);
        System.out.println("Message Hash: " + hash);
        System.out.println("Recipient: " + recipient);
        System.out.println("Message: " + message);
    }

    // ---------------- SAVE JSON ----------------

    static void saveMessagesToJSON() {

        try (FileWriter file =
                     new FileWriter("storedMessages.json")) {

            file.write(messageStorage.toJSONString());

            file.flush();

            System.out.println("Messages saved.");

        } catch (IOException e) {

            System.out.println("Error saving file.");
        }
    }

    // ---------------- RECIPIENT VALIDATION ----------------

    private static String checkRecipient(String recipient) {

        if (recipient == null
                || !recipient.matches("^\\+\\d{9,12}$")) {

            System.out.println("Invalid number.");
            return null;
        }

        return recipient;
    }

    // ---------------- SHOW MESSAGES ----------------

    static void showRecentlySentMessages() {

        if (messageStorage.isEmpty()) {

            System.out.println("No stored messages.");

        } else {

            System.out.println("Stored Messages:");

            for (int i = 0; i < messageStorage.size(); i++) {

                System.out.println(messageStorage.get(i));
            }
        }
    }
}