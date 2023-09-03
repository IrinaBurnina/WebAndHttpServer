package ru.netology;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Settings {
    public static int port = 9999;
    public static String host = "localhost";

    public static void writeToSettings(String fileName) {
        File settings = new File(fileName);
        if (!settings.exists()) {
            try (FileOutputStream fos = new FileOutputStream(fileName)) {
                settings.createNewFile();
                String portNumberAndHost = (port + " " + host);
                byte[] bytes = portNumberAndHost.getBytes();
                fos.write(bytes, 0, bytes.length);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static String[] settingsFromFile(String fileName) {
        StringBuilder sb = new StringBuilder();
        try (FileInputStream fileInputStream = new FileInputStream(fileName)) {
            int i;
            while ((i = fileInputStream.read()) != -1) {
                sb.append((char) i);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return String.valueOf(sb).split(" ");
    }

    public static String portNumberFromFile(String fileName) {
        return settingsFromFile(fileName)[0];
    }

    public static String hostFromFile(String fileName) {
        return settingsFromFile(fileName)[1];
    }
}
