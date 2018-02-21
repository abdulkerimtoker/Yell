package yell.client.util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Created by abdulkerim on 25.05.2016.
 */
public class HttpRequester {

    public static final String WEB_CONTEXT = "http://185.19.92.69:8080/YellWebServer";

    public static String register(String username, String password) throws Exception {
        String parameters = String.format("username=%s&password=%s", username, password);
        byte[] parameterBytes = parameters.getBytes(StandardCharsets.UTF_8);

        HttpURLConnection connection = (HttpURLConnection)
                new URL(WEB_CONTEXT + "/Register").openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Charset", "UTF-8");
        connection.setRequestProperty("Content-Length", Integer.toString(parameterBytes.length));

        OutputStream out = connection.getOutputStream();
        out.write(parameterBytes);
        out.flush();

        String response = readStream(connection.getInputStream());

        return response;
    }

    public static String login(String username, String password) throws Exception {
        String parameters = String.format("username=%s&password=%s", username, password);
        byte[] parameterBytes = parameters.getBytes(StandardCharsets.UTF_8);

        HttpURLConnection connection = (HttpURLConnection)
                new URL(WEB_CONTEXT + "/Login").openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Charset", "UTF-8");
        connection.setRequestProperty("Content-Length", Integer.toString(parameterBytes.length));

        OutputStream out = connection.getOutputStream();
        out.write(parameterBytes);
        out.flush();

        String response = readStream(connection.getInputStream());

        return response;
    }

    public static String logout(String sessionKey) throws Exception {
        String parameters = String.format("session_key=%s", sessionKey);
        byte[] parameterBytes = parameters.getBytes(StandardCharsets.UTF_8);

        HttpURLConnection connection = (HttpURLConnection)
                new URL(WEB_CONTEXT + "/Logout").openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Charset", "UTF-8");
        connection.setRequestProperty("Content-Length", Integer.toString(parameterBytes.length));

        OutputStream out = connection.getOutputStream();
        out.write(parameterBytes);
        out.flush();

        String response = readStream(connection.getInputStream());

        return response;
    }

    public static JSONObject profile(String username) throws Exception {
        HttpURLConnection connection = (HttpURLConnection)
                new URL(WEB_CONTEXT + "/Profile").openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Charset", "UTF-8");
        connection.setRequestProperty("Content-Type", "application/json");

        JSONObject request = new JSONObject();

        request.put("username", username);

        DataOutputStream out = new DataOutputStream(connection.getOutputStream());

        out.writeUTF(request.toString());
        out.flush();
        out.close();

        InputStream in = connection.getInputStream();
        byte[] buffer = new byte[10000];
        int len = in.read(buffer);

        String response = new String(buffer,0, len, StandardCharsets.UTF_8);

        if (response.equals("wrong") || response.equals("error")) {
            return null;
        } else {
            return new JSONObject(response);
        }
    }

    public static JSONObject profiles(JSONArray usernames) throws Exception {
        HttpURLConnection connection = (HttpURLConnection)
                new URL(WEB_CONTEXT + "/Profiles").openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Charset", "UTF-8");
        connection.setRequestProperty("Content-Type", "application/json");

        DataOutputStream out = new DataOutputStream(connection.getOutputStream());

        out.writeUTF(usernames.toString());
        out.flush();
        out.close();

        InputStream in = connection.getInputStream();
        byte[] buffer = new byte[10000];
        int len = in.read(buffer);

        String response = new String(buffer,0, len, StandardCharsets.UTF_8);

        if (response.equals("wrong") || response.equals("error")) {
            return null;
        } else {
            return new JSONObject(response);
        }
    }

    public static String changeDescription(String key, String description) throws Exception {
        HttpURLConnection connection = (HttpURLConnection)
                new URL(WEB_CONTEXT + "/ChangeDesc").openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Charset", "UTF-8");
        connection.setRequestProperty("Content-Type", "application/json");

        JSONObject request = new JSONObject();

        request.put("session_key", key);
        request.put("description", description);

        DataOutputStream out = new DataOutputStream(connection.getOutputStream());

        out.writeUTF(request.toString());
        out.flush();
        out.close();

        String response = readStream(connection.getInputStream());

        return response;
    }

    public static JSONArray search(String keyword) throws Exception {
        HttpURLConnection connection = (HttpURLConnection)
                new URL(WEB_CONTEXT + "/Search").openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Charset", "UTF-8");
        connection.setRequestProperty("Content-Type", "application/json");

        JSONObject request = new JSONObject();

        request.put("keyword", keyword);

        DataOutputStream out = new DataOutputStream(connection.getOutputStream());

        out.writeUTF(request.toString());
        out.flush();
        out.close();

        InputStream in = connection.getInputStream();
        byte[] buffer = new byte[10000];
        int len = in.read(buffer);

        String response = new String(buffer,0, len, StandardCharsets.UTF_8);

        if (response.equals("wrong") || response.equals("error")) {
            return null;
        } else {
            return new JSONArray(response);
        }
    }

    public static String sendMessage(String key, String target, String message, String messageType) throws Exception {
        HttpURLConnection connection = (HttpURLConnection)
                new URL(WEB_CONTEXT + "/SendMessage").openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Charset", "UTF-8");
        connection.setRequestProperty("Content-Type", "application/json");

        JSONObject request = new JSONObject();

        request.put("session_key", key);
        request.put("message", message);
        request.put("message_type", messageType);
        request.put("target", target);

        DataOutputStream out = new DataOutputStream(connection.getOutputStream());

        out.writeUTF(request.toString());
        out.flush();
        out.close();

        String response = readStream(connection.getInputStream());

        return response;
    }

    public static String registerGcmToken(String key, String gcmToken) throws Exception {
        if (key == null) {
            throw new Exception("Session key cannot be null");
        }

        if (gcmToken == null) {
            throw new Exception("GCM Token cannot be null");
        }

        String parameters = String.format("session_key=%s&gcm_token=%s",
                key, gcmToken);
        byte[] parameterBytes = parameters.getBytes(StandardCharsets.UTF_8);

        HttpURLConnection connection = (HttpURLConnection)
                new URL(WEB_CONTEXT + "/RegisterGcmToken").openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Charset", "UTF-8");
        connection.setRequestProperty("Content-Length", Integer.toString(parameterBytes.length));

        OutputStream out = connection.getOutputStream();
        out.write(parameterBytes);
        out.flush();

        String response = readStream(connection.getInputStream());

        return response;
    }

    public static String uploadImage(String sessionKey, String path) throws Exception {
        File file = new File(path);
        String extension = FileExtension.getExtension(file);

        HttpURLConnection connection = (HttpURLConnection)
                new URL(WEB_CONTEXT + "/UploadImage").openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Charset", "UTF-8");
        connection.setRequestProperty("session_key", sessionKey);
        connection.setRequestProperty("abc", extension);
        connection.setDoOutput(true);

        FileInputStream fin = new FileInputStream(file);
        OutputStream out = connection.getOutputStream();

        int len;
        byte[] buffer = new byte[4096];
        while ((len = fin.read(buffer)) != -1) {
            out.write(buffer, 0, len);
            out.flush();
        }

        out.close();
        fin.close();

        String response = readStream(connection.getInputStream());

        return response;
    }

    private static String readStream(InputStream in) throws Exception {
        StringBuilder sb = new StringBuilder();

        int x;
        while ((x = in.read()) != -1) {
            sb.append((char) x);
        }

        return sb.toString();
    }
}
