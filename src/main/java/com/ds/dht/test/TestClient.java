package com.ds.dht.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class TestClient {

    static String ctTEXTPLAIN = "text/plain";
    static String ctAPPJSON = "application/json";

    private static final List<String> urls = Arrays.asList("http://localhost:8081", "http://localhost:8082",
            "http://localhost:8083");

    public static void main(String[] args) throws Exception {

        HashMap<String, String> testMap = new HashMap<>();
        for (int i = 1; i < 2; i++) {
            String uuid = UUID.randomUUID().toString();
            testMap.put(uuid, "val-" + uuid);
        }

        for (String k : testMap.keySet()) {
            sendPUT(urls.get(getRandom(0, urls.size() - 1)) + "/" + k, testMap.get(k));
        }

        List<String> keys = new ArrayList<>(testMap.keySet());
        for (String k : keys) {
            String val = sendGET(urls.get(getRandom(0, urls.size() - 1)) + "/" + k, ctTEXTPLAIN, ctTEXTPLAIN);
            if (!testMap.get(k).equals(val)) {
                System.out.println("Error " + k);
            }
        }

        for (String url : urls) {
            String rs = sendGET(url + "/table/keys", ctTEXTPLAIN, ctAPPJSON);
            System.out.println(url + " = " + rs.substring(1, rs.length() - 1).split(",").length);
        }

    }

    static int getRandom(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }

    private static String sendGET(String url, String contentType, String accept) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", contentType);
        con.setRequestProperty("Accept", accept);
        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } else {
            System.out.println("GET Response Code :: " + responseCode);
            return null;
        }
    }

    private static String sendPUT(String url, String data) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("PUT");
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
        con.setRequestProperty("Accept", "text/plain");
        OutputStream os = con.getOutputStream();
        os.write(data.getBytes());
        os.flush();
        os.close();
        int responseCode = con.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } else {
            System.out.println("PUT Response Code :: " + responseCode);
            return null;
        }
    }

}
