package com.michael.corelib.coreutils;

import android.os.Handler;
import android.os.Message;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class NotifyHandlerObserver {
    private static final String TAG = "NotifyHandler";

    private final int mFlag;
    private Set<Handler> mHandlerSet = new LinkedHashSet<Handler>();

    private Set<Handler> mHandlerSetExchanged = new LinkedHashSet<Handler>();
    
    private AtomicBoolean mNotifying = new AtomicBoolean(false);
    
    private Object lock = new Object();

    public NotifyHandlerObserver(int flag) {
        mFlag = flag;
    }

    public void registeObserver(Handler handler) {
        mHandlerSet.add(handler);
    }

    public void unRegisteObserver(Handler handler) {
        mHandlerSet.remove(handler);
    }

    public boolean hasObserver() {
        return mHandlerSet.size() > 0;
    }

    public void removeAllObserver() {
        mHandlerSet.clear();
        synchronized (lock) {
            mHandlerSetExchanged.clear();
        }
    }

    private void copyHandler() {
        try {
            synchronized (lock) {
                mHandlerSetExchanged.addAll(mHandlerSet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public void notifyAll(Object obj) {
//        // copyHandler();
//        // for (Handler handler : mHandlerSetExchanged) {
//        // if (!mHandlerSet.contains(handler)) {
//        // continue;
//        // }
//        //
//        // Message message = Message.obtain();
//        // message.what = mFlag;
//        // message.obj = obj;
//        // handler.sendMessage(message);
//        // }
//        // mHandlerSetExchanged.clear();
//
//        notifyAll(-1, -1, obj);
//    }

//    public void notifyAll(int arg1) {
//        // copyHandler();
//        // for (Handler handler : mHandlerSetExchanged) {
//        // if (!mHandlerSet.contains(handler)) {
//        // continue;
//        // }
//        //
//        // Message message = Message.obtain();
//        // message.what = mFlag;
//        // message.arg1 = arg1;
//        // handler.sendMessage(message);
//        // }
//        // mHandlerSetExchanged.clear();
//
//        notifyAll(arg1, -1, null);
//    }

    public void notifyAll(int arg1, int arg2, Object obj) {
        if (!mNotifying.get()) {
            mNotifying.set(true);
            copyHandler();
            synchronized (lock) {
                handleNotifyAll(mHandlerSetExchanged, arg1, arg2, obj);
                mHandlerSetExchanged.clear();
            }
            mNotifying.set(false);
        } else {
            Set<Handler> newTempSet = new LinkedHashSet<Handler>();
            try {
                newTempSet.addAll(mHandlerSet);
            } catch (Exception e) {
                e.printStackTrace();
            }
            handleNotifyAll(newTempSet, arg1, arg2, obj);
            newTempSet.clear();
            newTempSet = null;
        }
    }
    
    private void handleNotifyAll(Set<Handler> handlerList, int arg1, int arg2, Object obj) {
        for (Handler handler : handlerList) {
            if (!mHandlerSet.contains(handler)) {
                continue;
            }

            Message message = Message.obtain();
            message.what = mFlag;
            message.arg1 = arg1;
            message.arg2 = arg2;
            message.obj = obj;
            handler.sendMessage(message);
        }
    }

    public boolean isEmpty() {
        if (mHandlerSet.size() == 0) {
            return true;
        }
        return false;
    }
}
