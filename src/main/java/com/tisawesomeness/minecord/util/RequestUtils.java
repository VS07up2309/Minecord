package com.tisawesomeness.minecord.util;

import lombok.Cleanup;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.Scanner;
import java.util.stream.Collectors;

public final class RequestUtils {

    private static final String charset = StandardCharsets.UTF_8.name();
    private static final String jsonType = "application/json";
    private static final String plainType = "text/plain";
    private static final String browserAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";

    private static String get(URLConnection conn, String type) throws IOException {
        InputStream response = conn.getInputStream();
        @Cleanup Scanner scanner = new Scanner(response);
        return scanner.hasNext() ? scanner.useDelimiter("\\A").next() : "";
    }

    /**
     * Performs an HTTP GET request.
     *
     * @param url The request URL.
     * @return The response of the request in string form.
     */
    public static String get(String url) {
        return get(url, null);
    }

    /**
     * Performs an HTTP GET request.
     *
     * @param url The request URL.
     * @return The response of the request in string form.
     */
    public static String getPlain(String url) {
        return getPlain(url, null);
    }

    /**
     * Performs an HTTP GET request.
     *
     * @param url  The request URL.
     * @param auth The content of the Authorization header.
     * @return The response of the request in string form.
     */
    public static String get(String url, String auth) {
        if (checkURL(url)) {
            try {
                URLConnection conn = open(url, auth, jsonType);
                return get(conn, jsonType);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Performs an HTTP GET request.
     *
     * @param url  The request URL.
     * @param auth The content of the Authorization header.
     * @return The response of the request in string form.
     */
    public static String getPlain(String url, String auth) {
        try {
            URLConnection conn = open(url, auth, plainType);
            return get(conn, plainType);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Performs an HTTP POST request.
     *
     * @param url   The request URL.
     * @param query The request payload, in string form.
     * @return The response of the request in string form.
     */
    public static String post(String url, String query) {
        return post(url, query, null);
    }

    /**
     * Performs an HTTP POST request.
     *
     * @param url   The request URL.
     * @param query The request payload, in string form.
     * @param auth  The content of the Authorization header.
     * @return The response of the request in string form.
     */
    public static String post(String url, String query, String auth) {
        try {
            URLConnection conn = open(url, auth, jsonType);
            @Cleanup OutputStream output = conn.getOutputStream();
            output.write(query.getBytes(charset));
            return get(conn, jsonType);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static URLConnection open(String url, String auth, String contentType) throws IOException {
        URLConnection conn = new URL(url).openConnection();
        conn.setDoOutput(true);
        conn.setRequestProperty("Accept-Charset", charset);
        conn.setRequestProperty("Content-Type", contentType);
        if (auth != null)
            conn.setRequestProperty("Authorization", auth);
        return conn;
    }

    /**
     * Checks if a URL exists and can respond to an HTTP request.
     *
     * @param url The URL to check.
     * @return True if the URL exists, false if it doesn't or an error occured.
     */
    public static boolean checkURL(String url) {
        return checkURL(url, false);
    }

    /**
     * Checks if a URL exists and can respond to an HTTP request.
     *
     * @param url             The URL to check.
     * @param fakeUserAgent If true, pretends to be a browser
     * @return True if the URL exists, false if it doesn't or an error occured.
     */
    public static boolean checkURL(String url, boolean fakeUserAgent) {
        try {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("HEAD");
            if (fakeUserAgent) {
                con.setRequestProperty("User-Agent", browserAgent);
            }
            return con.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static InputStream downloadImage(String url) throws IOException {
        BufferedImage image = ImageIO.read(new URL(url));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        return new ByteArrayInputStream(os.toByteArray());
    }

    /**
     * Loads a file from the resources folder.
     * @param name The filename with extension
     * @return A string with the contents of the file
     */
    public static String loadResource(String name) {
        InputStream is = openResource(name);
        try (InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr)) {
            return br.lines().collect(Collectors.joining("\n"));
        } catch (IOException ex) {
            throw new AssertionError("An IOException when closing a resource stream should never happen.");
        }
    }
    /**
     * Loads a JSON file from the resources folder.
     * <br>The file must start with a JSON object, not a JSON array.
     * @param name The filename with extension
     * @return The contained JSON object
     */
    public static JSONObject loadJSONResource(String name) {
        return new JSONObject(loadResource(name));
    }
    /**
     * Loads a properties file from the resources folder.
     * @param name The filename with extension
     * @return A properties object with the contents of the file
     */
    public static Properties loadPropertiesResource(String name) {
        InputStream is = openResource(name);
        Properties prop = new Properties();
        try {
            prop.load(is);
        } catch (IOException ex) {
            throw new IllegalArgumentException("The resource was not loaded properly!", ex);
        }
        return prop;
    }
    private static InputStream openResource(String name) {
        InputStream is = RequestUtils.class.getClassLoader().getResourceAsStream(name);
        if (is == null) {
            throw new IllegalArgumentException("The resource was not found!");
        }
        return is;
    }

    // Converts a string to SHA1 (modified from http://www.sha1-online.com/sha1-java/)
    public static String sha1(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] result = md.digest(str.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
            return null;
        }
    }

}
