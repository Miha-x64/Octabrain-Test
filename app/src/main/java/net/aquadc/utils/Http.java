package net.aquadc.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by miha on 30.10.15.
 *
 */
public abstract class Http {

    /*** HTTP GET ***/
    public static String get(String addr) throws IOException {
        return get(addr, new HashMap<String, String>(0));
    }
    public static String get(String addr, String key, String value) throws IOException {
        return get(addr, new String[]{key}, new String[]{value});
    }
    @SuppressWarnings("WeakerAccess")
    public static String get(String addr, String[] keys, String[] vals) throws IOException {
        HashMap<String,String> hm = new HashMap<>(keys.length);
        for (int i = 0; i < keys.length; i++)
            hm.put(keys[i], vals[i]);
        return get(addr, hm);
    }
    public static String get(String addr, Map<String,String> vars) throws IOException {
        StringBuilder response = new StringBuilder();
        try {
            Set<String> keys = vars.keySet();
            StringBuilder uri = new StringBuilder(addr);
            if (!addr.contains("?"))
                uri.append('?');
            else if (addr.charAt(addr.length()-1) != '&')
                uri.append('&');

            for (String key : keys) {
                uri.append(key);
                uri.append('=');
                uri.append(URLEncoder.encode(vars.get(key),
                        "UTF-8"));
                uri.append('&');
            }

            URL obj = new URL(uri.toString().substring(0, uri.length()-1));
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:42.0) Gecko/20100101 Firefox/42.0");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        } catch (MalformedURLException | ProtocolException | UnsupportedEncodingException e) {
            throw new IOException(e);
        }
        return response.toString();
    }

    public static boolean is200(String URI) throws IOException {
        try {
            URL url = new URL(URI);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            // optional default is GET
            con.setRequestMethod("GET");
            if (con.getResponseCode() == 200)
                return true;
        } catch (MalformedURLException | ProtocolException | UnsupportedEncodingException e) {
            return false;
        }
        return false;
    }
}
