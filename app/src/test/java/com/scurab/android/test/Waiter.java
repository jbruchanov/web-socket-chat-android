package com.scurab.android.test;

import junit.framework.Assert;

/**
 * Created by JBruchanov on 06/03/2017.
 */

public class Waiter {

    public static final int DEFAULT_WAITING = Integer.MAX_VALUE;

    public void startWaiting() {
        startWaiting(DEFAULT_WAITING);
    }

    public void startWaiting(int timeout) {
        synchronized (this) {
            try {
                this.wait(timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopWaiting() {
        synchronized (this) {
            this.notifyAll();
        }
    }

    public void fail(String msg) {
        stopWaiting();
        Assert.fail(msg);
    }

    public void fail(Exception e) {
        fail(e.getMessage());
    }
}
