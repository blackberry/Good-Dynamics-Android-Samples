/*
 * Copyright (c) 2020 BlackBerry Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.good.gd.webview_V2.bbwebview;

import android.util.Log;
import android.util.Pair;

import com.good.gd.apache.http.Header;
import com.good.gd.apache.http.HttpEntity;
import com.good.gd.apache.http.HttpResponse;
import com.good.gd.apache.http.StatusLine;
import com.good.gd.apache.http.protocol.HttpContext;
import com.good.gd.webview_V2.bbwebview.tasks.http.GDHttpClientProvider;
import com.good.gd.webview_V2.bbwebview.tasks.http.HttpResponseParser;
import com.good.gd.webview_V2.bbwebview.utils.IOhelper;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class BBResponseInputStream extends InputStream {

    private static final String TAG = "GDWebView-" + BBResponseInputStream.class.getSimpleName();

    private Future<Pair<HttpResponse, HttpContext>> futureIS;
    private InputStream contentStream;
    private Pair<HttpResponse, HttpContext> response;
    private final AtomicBoolean reponseIsAvailable = new AtomicBoolean(false);
    private String clientId;

    public BBResponseInputStream(Future<Pair<HttpResponse, HttpContext>> futureIS, String clientId) {
        this.clientId = clientId;
        this.futureIS = futureIS;
    }

    @Override
    public int read() {
        try {

            if (!reponseIsAvailable.get()) {
                try {
                    response = futureIS.get();
                } catch (Exception e) {
                    Log.w(TAG, "response promise failed");
                }

                reponseIsAvailable.set(true);

                Log.i(TAG, "read() " + clientId + " IN ");

                if (response != null && response.first != null) {

                    HttpResponse httpResp = response.first;
                    HttpContext httpContext = response.second;

                    clientId = (String) httpContext.getAttribute("webview.connectionId");
                    
                    StatusLine statusLine = httpResp.getStatusLine();
                    Header[] allHeaders = httpResp.getAllHeaders();

                    Log.i(TAG, "read() " + clientId + " initial status line: " + statusLine);
                    Log.i(TAG, "read() " + clientId + " initial headers: " + Arrays.toString(allHeaders));

                    HttpEntity responseEntity = httpResp.getEntity();
                    if (responseEntity != null) {
                        long contentLength = responseEntity.getContentLength();
                        contentStream = responseEntity.getContent();

                        Log.i(TAG, "-> read() " + clientId + " responseEntity PRESENT");
                        Log.i(TAG, "   read() " + clientId + " contentLength " + contentLength);
                        Log.i(TAG, "   read() " + clientId + " isChunked " + responseEntity.isChunked());
                        Log.i(TAG, "   read() " + clientId + " contentStream " + contentStream);

                        String contentEncoding = HttpResponseParser.parseContentEncoding(allHeaders, responseEntity);

                        contentStream = IOhelper.inputStreamDecorator(contentStream, contentEncoding, responseEntity.isChunked());

                        Log.i(TAG, "-< read() " + clientId + " contentLength " + contentLength);

                    } else {
                        Log.w(TAG, "read() " + clientId + " initial NO response entity PRESENT");
                        GDHttpClientProvider.getInstance().releasePooledClient(clientId);
                    }

                } else {
                    Log.w(TAG, "read() " + clientId + " NO response");
                    GDHttpClientProvider.getInstance().releasePooledClient(clientId);
                }
            }

            if (contentStream == null) {
                Log.w(TAG, "read() contentStream == NULL");
                return -1;
            }

            return contentStream.read();
        } catch (IOException e) {
            Log.e(TAG, "read() " + clientId + " IOException: ", e);
        } catch (Exception e) {
            Log.e(TAG, "read() " + clientId + " Exception: ", e);
        }

        Log.i(TAG, "read() " + clientId + " OUT ");

        return -1;
    }

    @Override
    public void close() throws IOException {
        Log.i(TAG, "close()");
        super.close();

        if (contentStream != null) {
            try {
                contentStream.close();

                Log.i(TAG, "+close(), id " + clientId);

            } catch (SocketException e) {
                Log.e(TAG, "close() contentStream call exception",e);
            } catch (IOException e) {
                Log.e(TAG, "close() contentStream call exception",e);
            } finally {
                GDHttpClientProvider.getInstance().releasePooledClient(clientId);
            }
        } else {
            Log.w(TAG, "close() no contentStream");
            GDHttpClientProvider.getInstance().releasePooledClient(clientId);
        }

    }

}
