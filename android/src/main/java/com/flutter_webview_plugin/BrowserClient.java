package com.flutter_webview_plugin;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by lejard_h on 20/12/2017.
 */

public class BrowserClient extends WebViewClient {
    private Pattern invalidUrlPattern = null;
    private Map<String, String> headers = null;

    public BrowserClient() {
        this(null);
    }

    public BrowserClient(String invalidUrlRegex) {
        super();
        if (invalidUrlRegex != null) {
            invalidUrlPattern = Pattern.compile(invalidUrlRegex);
        }
    }

    public void updateInvalidUrlRegex(String invalidUrlRegex) {
        if (invalidUrlRegex != null) {
            invalidUrlPattern = Pattern.compile(invalidUrlRegex);
        } else {
            invalidUrlPattern = null;
        }
    }

    public void setHeaders(Map<String, String> value) {
        headers = value;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        Map<String, Object> data = new HashMap<>();
        data.put("url", url);
        data.put("type", "startLoad");
        FlutterWebviewPlugin.channel.invokeMethod("onState", data);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        Map<String, Object> data = new HashMap<>();
        data.put("url", url);

        FlutterWebviewPlugin.channel.invokeMethod("onUrlChanged", data);

        data.put("type", "finishLoad");
        FlutterWebviewPlugin.channel.invokeMethod("onState", data);

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        // returning true causes the current WebView to abort loading the URL,
        // while returning false causes the WebView to continue loading the URL as usual.
        String url = request.getUrl().toString();
        boolean isInvalid = checkInvalidUrl(url);
        Map<String, Object> data = new HashMap<>();
        data.put("url", url);
        data.put("type", isInvalid ? "abortLoad" : "shouldStart");

        FlutterWebviewPlugin.channel.invokeMethod("onState", data);

        if (!isInvalid) {
            view.loadUrl(request.getUrl().toString(), headers);

            return true;
        }

        return isInvalid;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        // returning true causes the current WebView to abort loading the URL,
        // while returning false causes the WebView to continue loading the URL as usual.
        boolean isInvalid = checkInvalidUrl(url);
        Map<String, Object> data = new HashMap<>();
        data.put("url", url);
        data.put("type", isInvalid ? "abortLoad" : "shouldStart");

        FlutterWebviewPlugin.channel.invokeMethod("onState", data);
        return isInvalid;
    }

//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
//        try {
//            Log.d("URL", request.getUrl().toString());
//
//            final OkHttpClient client = new OkHttpClient().newBuilder()
//                    .followRedirects(false)
//                    .followSslRedirects(false)
//                    .build();
//
//            final Call call = client.newCall(new Request.Builder()
//                    .url(request.getUrl().toString())
//                    .method(request.getMethod(), null)
//                    .cacheControl(CacheControl.FORCE_NETWORK)
//                    .build()
//            );
//
//            final Response response = call.execute();
//
//            String contentType = response.header("content-type");
//            String encoding = response.header("content-encoding");
//            String contentTypeValue = null;
//            String encodingValue = null;
//
//            if (contentType != null) {
//                contentTypeValue = contentType;
//            }
//
//            if (contentTypeValue != null && contentTypeValue.startsWith("text/html")) {
//                //contentTypeValue = "text/html";
//                encodingValue = "utf-8";
//            }
//
//            if (encoding != null) {
//                encodingValue = encoding;
//            }
//
//            return new WebResourceResponse(contentTypeValue, encodingValue, response.body().byteStream());
//        } catch (Exception e) {
//            return null;
//        }
//
////        Log.d("URL", request.getUrl().toString());
////        return super.shouldInterceptRequest(view, request);
//    }

//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
//        try {
//            //CloseableHttpClient client = HttpClientBuilder.create().build();
//            DefaultHttpClient client = new DefaultHttpClient();
//            HttpRequestBase httpRequest;
//
//            switch (request.getMethod()) {
//                case "POST": {
//                    httpRequest = new HttpPost(request.getUrl().toString());
//
//                    break;
//                }
//                default: {
//                    httpRequest = new HttpGet(request.getUrl().toString());
//                }
//            }
//
//            httpRequest.setHeader("header1", "header1_value");
//            httpRequest.setHeader("header2", "header2_value");
//            httpRequest.setHeader("header3", "header3_value");
//            httpRequest.setHeader("header4", "header4_value");
//
//            HttpResponse httpReponse = client.execute(httpRequest);
//            Header contentType = httpReponse.getEntity().getContentType();
//            Header encoding = httpReponse.getEntity().getContentEncoding();
//            InputStream responseInputStream = httpReponse.getEntity().getContent();
//
//            String contentTypeValue = null;
//            String encodingValue = null;
//
//            if (contentType != null) {
//                contentTypeValue = contentType.getValue();
//            }
//
//            if (contentTypeValue != null && contentTypeValue.startsWith("text/html")) {
//                contentTypeValue = "text/html";
//                encodingValue = "utf-8";
//            }
//
//            if (encoding != null) {
//                encodingValue = encoding.getValue();
//            }
//
//            return new WebResourceResponse(contentTypeValue, encodingValue, responseInputStream);
//        } catch (ClientProtocolException e) {
//            return null;
//        } catch (IOException e) {
//            return null;
//        }
//    }
//
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
        Map<String, Object> data = new HashMap<>();
        data.put("url", request.getUrl().toString());
        data.put("code", Integer.toString(errorResponse.getStatusCode()));
        FlutterWebviewPlugin.channel.invokeMethod("onHttpError", data);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        Map<String, Object> data = new HashMap<>();
        data.put("url", failingUrl);
        data.put("code", errorCode);
        FlutterWebviewPlugin.channel.invokeMethod("onHttpError", data);
    }

    private boolean checkInvalidUrl(String url) {
        if (invalidUrlPattern == null) {
            return false;
        } else {
            Matcher matcher = invalidUrlPattern.matcher(url);
            return matcher.lookingAt();
        }
    }
}