package com.sentegrity.core_detection.networking;

import android.content.Context;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;
import com.sentegrity.android.R;

import java.io.InputStream;
import java.security.KeyStore;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.conn.ssl.SSLSocketFactory;
import cz.msebera.android.httpclient.entity.StringEntity;

/**
 * Created by dmestrov on 24/05/16.
 */
public class SentegrityHTTPSessionManager {

    private static final String CONTENT_TYPE = "application/json";
    private static AsyncHttpClient client;

    private static void initClient(Context context) {
        if (client == null) {
            try {
                client = new AsyncHttpClient();
                client.setConnectTimeout(10000);
                client.setResponseTimeout(10000);
                client.setMaxRetriesAndTimeout(5, 1000);

                /*final KeyStore keyStore;
                keyStore = KeyStore.getInstance("BKS");

                final InputStream inputStream = context.getResources().openRawResource(R.raw.certs);

                keyStore.load(inputStream, context.getString(R.string.store_pass).toCharArray());

                String algorithm = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
                tmf.init(keyStore);
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, tmf.getTrustManagers(), null);*/

                client.setSSLSocketFactory(getSSLSocketFactory(context));

                //client.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static SSLSocketFactory getSSLSocketFactory(Context context) {
        SSLSocketFactory ret = null;
        try {
            final KeyStore ks = KeyStore.getInstance("BKS");

            final InputStream inputStream = context.getResources().openRawResource(R.raw.keystore);

            ks.load(inputStream, context.getString(R.string.store_pass).toCharArray());
            inputStream.close();

            ret = new SSLSocketFactory(ks);
        } catch (Exception ex) {
        } finally {
            return ret;
        }
    }

    public static void postData(Context context, Object object, final NetworkCallback callback) {
        try {
            initClient(context);

            StringEntity entity = new StringEntity(new Gson().toJson(object));
            client.post(context, getUrl(), entity, CONTENT_TYPE, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    callback.onFinish(false, null);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    callback.onFinish(true, responseString);
                }
            });


            /*OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
            outputStreamWriter.write(jsonObject.toString());
            outputStreamWriter.flush();

            StringBuilder sb = new StringBuilder();
            int HttpResult = urlConnection.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();

                System.out.println("" + sb.toString());
            } else {
                System.out.println(urlConnection.getResponseMessage());
            }

            outputStreamWriter.close();
            inputStream.close();*/

        } catch (Exception ex) {
        } finally {
        }
    }

    private static String getUrl(){
        return "https://cloud.sentegrity.com/app_dev.php/checkin";
    }
}
