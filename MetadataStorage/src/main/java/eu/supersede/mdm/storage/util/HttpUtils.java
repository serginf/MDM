package eu.supersede.mdm.storage.util;

import net.minidev.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.apache.jena.ext.com.google.common.net.HttpHeaders.USER_AGENT;

public class HttpUtils {
    // HTTP GET request
    public static void sendGet(String requestUrl) throws Exception {
        URL obj = new URL(requestUrl);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        // optional default is GET
        con.setRequestMethod("GET");
        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);
        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + requestUrl);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        //print result
        System.out.println(response.toString());
    }

    // HTTP POST request
    public static String sendPost(JSONObject object, String requestUrl) {
        String result = "";
        try {
            String json = object.toString();
            System.out.println(json);
            URL url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //conn.setConnectTimeout(5000);
            conn.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");

            OutputStream os = conn.getOutputStream();
            os.write(json.getBytes(StandardCharsets.UTF_8));
            os.close();
            if (conn.getResponseCode() == 200) {
                System.out.println(conn.getResponseMessage());
                // read the response
                InputStream in = new BufferedInputStream(conn.getInputStream());
                result = org.apache.commons.io.IOUtils.toString(in, "UTF-8");
                System.out.println(result);
                System.out.println(conn.getResponseCode());
                in.close();
            } else {
                System.out.println(conn.getResponseCode() + " - " + conn.getResponseMessage());
            }
            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
