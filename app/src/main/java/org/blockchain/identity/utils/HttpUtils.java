package org.blockchain.identity.utils;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.nio.charset.Charset;

import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.StringEntity;

public class HttpUtils {

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void post(String url, String data, AsyncHttpResponseHandler responseHandler, Context context) {
        StringEntity entity = new StringEntity(data, Charset.defaultCharset());
        client.post(context, url, entity, ContentType.APPLICATION_JSON.getMimeType(), responseHandler);
    }
}
