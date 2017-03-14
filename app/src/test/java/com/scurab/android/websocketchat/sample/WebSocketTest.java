package com.scurab.android.websocketchat.sample;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.spdy.SpdyMiddleware;
import com.scurab.android.test.Waiter;
import com.scurab.android.websocketchat.messaging.ChatService;

import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.reactivex.disposables.Disposable;

import static com.ibm.icu.impl.Assert.fail;


/**
 * Created by JBruchanov on 06/03/2017.
 */

@RunWith(RobolectricTestRunner.class)
public class WebSocketTest {

    private Waiter mWaiter = new Waiter();

    @Test
    public void testRX(){
        fakeSSL();
        final ChatService chatService = new ChatService("wss://scurab-websocketchat.herokuapp.com/");
        final Disposable[] ref = new Disposable[1];
        final Disposable contract = chatService.start("test")
                .doOnSubscribe((e) -> ref[0] = e)
                .doOnDispose(() -> System.out.println("Disposed venku"))
                .subscribe((msg) -> {
                    System.out.println(msg);
                    mWaiter.stopWaiting();
                });
        ref[0] = contract;
        mWaiter.startWaiting();
    }

    @Test
    public void testConnect() {
        fakeSSL();
        AsyncHttpClient.getDefaultInstance()
                .websocket("wss://scurab-websocketchat.herokuapp.com/", null, null)
                .then((e, result) -> {
                    if (e != null) {
                        mWaiter.fail(e);
                    } else {
                        result.setDataCallback((emitter, bb) -> {
                            final String s = bb.readString();
                            System.out.println(s);
                        });
                        result.setStringCallback(msg -> {
                            msg.toString();
                            mWaiter.stopWaiting();
                        });
                        result.send("{\"type\":\"login\", \"username\":\"test\" }");
                    }
                });
        mWaiter.startWaiting();
    }

    private static void fakeSSL() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        authType.toLowerCase();
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        authType.toLowerCase();
                    }
                }
        };

// Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            final SpdyMiddleware sslSocketMiddleware = AsyncHttpClient.getDefaultInstance().getSSLSocketMiddleware();
            sslSocketMiddleware.setSSLContext(sc);
            sslSocketMiddleware.setTrustManagers(trustAllCerts);
        } catch (GeneralSecurityException e) {
            fail(e);
        }

    }
}
