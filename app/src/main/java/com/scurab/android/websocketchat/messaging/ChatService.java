package com.scurab.android.websocketchat.messaging;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashSet;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.MainThreadDisposable;

/**
 * Created by JBruchanov on 07/03/2017.
 */

public class ChatService {


    private String mUrl;
    private WebSocket mWebSocket;
    private String mUsername;

    private final HashSet<Observer<? super Boolean>> mSubscribers;


    public ChatService() {
        this("wss://scurab-websocketchat.herokuapp.com/");
//        this("ws://192.168.168.11:3000/");
    }

    public ChatService(String url) {
        mUrl = url;
        mSubscribers = new HashSet<>();
    }

    public Observable<JSONObject> start(String username) {
        assertTrue(mWebSocket == null, "Already started");
        return Observable.create((ObservableOnSubscribe<JSONObject>) emitter ->
                AsyncHttpClient.getDefaultInstance()
                        .websocket(mUrl, null, null)
                        .then((err, result) -> {
                            try {
                                if (err != null || result == null) {
                                    emitter.onError(err);
                                    return;
                                }
                                mWebSocket = result;
                                dispatchConnectionChanged(true);
                                mWebSocket.setStringCallback((json) -> {
                                    try {
                                        JSONObject msg = new JSONObject(json);
                                        emitter.onNext(msg);
                                    } catch (Throwable t) {
                                        emitter.onError(t);
                                    }
                                });
                                mWebSocket.setClosedCallback((callback) -> {
                                    dispatchConnectionChanged(false);
                                    emitter.onError(new Error("Closed by error"));
                                });
                                final JSONObject obj = new JSONObject()
                                        .put("type", "login")
                                        .put("username", username);
                                mUsername = username;
                                mWebSocket.send(obj.toString());
                            } catch (JSONException e) {
                                throw new Error(e);
                            }
                        }))
                .doOnDispose(() -> {
                    if (mWebSocket != null) {
                        //be sure it's nulled before closing call
                        mWebSocket.setClosedCallback(null);
                        mWebSocket.send("{\"type\":\"logout\"}");
                        mWebSocket.close();
                        dispatchConnectionChanged(false);
                    }
                    mUsername = null;
                    mWebSocket = null;
                });
    }

    public Observable<Boolean> onConnectionChanged() {
        return new Observable<Boolean>() {
            @Override
            protected void subscribeActual(Observer<? super Boolean> observer) {
                mSubscribers.add(observer);
                observer.onSubscribe(new MainThreadDisposable() {
                    @Override
                    protected void onDispose() {
                        mSubscribers.remove(observer);
                    }
                });
            }
        };
    }

    void dispatchConnectionChanged(boolean isConnected) {
        for (Observer<? super Boolean> subscriber : mSubscribers) {
            subscriber.onNext(isConnected);
        }
    }

    private static void assertTrue(boolean value, String errMsg) {
        if (!value) {
            throw new AssertionError(errMsg);
        }
    }

    public JSONObject sendMessage(String msg, String to) {
        if (mWebSocket != null) {
            try {
                JSONObject obj = new JSONObject()
                        .put("type", "msg")
                        .put("from", mUsername)
                        .put("data", msg)
                        .put("to", to);
                String json = obj.toString();
                mWebSocket.send(json);
                obj.put("received", new Date().toString());
                return obj;
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        }
        return null;
    }
}
