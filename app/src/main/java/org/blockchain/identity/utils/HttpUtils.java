package org.blockchain.identity.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class HttpUtils {

    public static String post(String url, String data, String contentType) {
        OutputStream os = null;
        HttpURLConnection connection = null;
        InputStream in = null;
        try {
            URL urlObject = new URL(url);
            connection = (HttpURLConnection) urlObject.openConnection();
            /*connection.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });*/
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", contentType);
            connection.setDoOutput(true);

            if (data != null && !data.trim().equals("")) {
                os = connection.getOutputStream();
                os.write(data.getBytes());
                os.flush();
            }

            in = new BufferedInputStream(connection.getInputStream());
            InputStreamReader is = new InputStreamReader(in);
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(is);
            String read = br.readLine();

            while (read != null) {
                sb.append(read);
                read = br.readLine();
            }
            return sb.toString();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }
}
