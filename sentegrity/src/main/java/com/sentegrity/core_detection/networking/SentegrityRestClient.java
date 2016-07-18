package com.sentegrity.core_detection.networking;

import android.content.Context;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;
import com.sentegrity.android.R;

import java.io.InputStream;
import java.security.KeyStore;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.conn.ssl.SSLConnectionSocketFactory;
import cz.msebera.android.httpclient.conn.ssl.SSLSocketFactory;
import cz.msebera.android.httpclient.entity.StringEntity;

/**
 * Created by dmestrov on 24/05/16.
 */
public class SentegrityRestClient {

    private static final String CONTENT_TYPE = "application/json";
    private static final String URL = "https://sentegrity-staging-lb-163070367.us-west-2.elb.amazonaws.com/app.php/api/";

    private static AsyncHttpClient client;

    private static void initClient(Context context) {
        if (client == null) {
            try {
                //TODO: disabled SSL for testing purposes
                client = new AsyncHttpClient(true, 80, 443);
                //client = new AsyncHttpClient();
                //client.setSSLSocketFactory(getSSLSocketFactory(context));

                client.setConnectTimeout(10000);
                client.setResponseTimeout(10000);
                client.setMaxRetriesAndTimeout(5, 1000);

                //client.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static SSLSocketFactory getSSLSocketFactory(Context context) {

        /*
        update when async http gets updated
        final KeyStore keyStore;
        keyStore = KeyStore.getInstance("BKS");

        final InputStream inputStream = context.getResources().openRawResource(R.raw.certs);

        keyStore.load(inputStream, context.getString(R.string.store_pass).toCharArray());

        String algorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
        tmf.init(keyStore);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);*/

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

    public static void uploadReport(Context context, Object object, final NetworkCallback callback) {
        try {
            initClient(context);

            StringEntity entity = new StringEntity(new Gson().toJson(object));
            client.post(context, getUrlForService("check-in"), entity, CONTENT_TYPE, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    callback.onFinish(false, responseString);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    callback.onFinish(true, responseString);
                }
            });

        } catch (Exception ex) {
        } finally {
        }
    }

    private static String getUrlForService(String service) {
        return URL + service;
    }
}
