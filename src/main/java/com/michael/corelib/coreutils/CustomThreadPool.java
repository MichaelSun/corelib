package com.michael.corelib.coreutils;

import android.content.Context;
import android.os.*;
import android.os.Process;
import android.text.TextUtils;
import com.michael.corelib.config.CoreConfig;

import java.util.HashMap;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * //usage for RRThreadPool RRThreadPool.getInstance().excute(new
 * RRTaskWrapper(new Runnable(){ public void run() { try { Thread.sleep(5000); }
 * catch (Exception e) { e.printStackTrace(); } } }));
 **/

public final class CustomThreadPool extends SingleInstanceBase implements Destroyable {
    private static final String TAG = "RRThreadPool";

    private static final boolean USING_CUSTOM_THREADPOOL = true;

    private static final int RETURN_RESULT = -40000;

    public static abstract class TaskWrapperNew<T> extends TaskWrapper {
        private Handler mMessageHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case RETURN_RESULT:
                    onResult((T) msg.obj);
                    break;
                }
            }
        };

        public abstract T doRealJob();

        public abstract void onResult(T result);

        public TaskWrapperNew() {
            super(null);
        }

        public TaskWrapperNew(String taskName) {
            super(null, taskName);
        }

        @Override
        public void run() {
            if (!cancel) {
                T ret = doRealJob();
                if (mMessageHandler != null) {
                    Message msg = Message.obtain();
                    msg.what = RETURN_RESULT;
                    msg.obj = ret;
                    mMessageHandler.sendMessage(msg);
                }
            } else {
            }
        }
    }

    public static class TaskWrapper implements Runnable {

        protected final Runnable runnable;
        protected boolean cancel;
        protected String mTaskName;

        public TaskWrapper(Runnable runnable) {
            this(runnable, null);
        }

        public TaskWrapper(Runnable runnable, String taskName) {
            mTaskName = taskName;
            this.runnable = runnable;
            cancel = false;
        }

        public void cancel() {
            cancel = true;
        }

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            
            if (!cancel) {
                runnable.run();
            } else {
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            TaskWrapper that = (TaskWrapper) o;
            if (runnable != null ? !runnable.equals(that.runnable) : that.runnable != null)
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            return runnable != null ? runnable.hashCode() : 0;
        }

        @Override
        public String toString() {
            return runnable.toString();
        }
    }

    public static final class ThreadPoolSnapShot {
        public int taskCount;

        public int coreTreadCount;

        public final int ALLOWED_MAX_TAKS;

        ThreadPoolSnapShot(int taskCount, int coreThreadCount, int max) {
            this.taskCount = taskCount;
            this.coreTreadCount = coreThreadCount;
            this.ALLOWED_MAX_TAKS = max;
        }
    }

    private static class InternalAsyncTask extends AsyncTask<TaskWrapper, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(TaskWrapper... params) {
            if (params != null && params.length > 0) {
                params[0].run();
            }

            return true;
        }
    }

    private static class IncrementInteger {
        private final int MAX_SIZE = Integer.MAX_VALUE / 2;

        private final AtomicInteger mInt = new AtomicInteger(1);

        public int getAndIncrement() {
            if (mInt.get() >= MAX_SIZE) {
                mInt.set(1);
            }

            return mInt.getAndIncrement();
        }
    }

    private final static IncrementInteger sIncrementInteger = new IncrementInteger();

    private static class PriorityThreadFactory implements ThreadFactory {
        private final int mPriority;
        private final String mName;

        public PriorityThreadFactory(String name, int priority) {
            mName = name;
            mPriority = priority;
        }

        public Thread newThread(Runnable r) {
            return new Thread(r, mName + '-' + sIncrementInteger.getAndIncrement()) {
                @Override
                public void run() {
                    Process.setThreadPriority(mPriority);
                    super.run();
                }
            };
        }
    }

    private static final int CORE_THREAD_COUNT = 5;
    private static final int MAX_THREAD_COUNT = 64;
    private static final long KEEP_ALIVE_DELAY = 5 * 1000;

    private static final int SPECIAL_CORE_THREAD_COUNT = 3;

    private ThreadPoolExecutor mExecutorService;
    private HashMap<String, ThreadPoolExecutor> mSpecialExectorMap;

    public static CustomThreadPool getInstance() {
        return SingleInstanceBase.getInstance(CustomThreadPool.class);
    }

    public static void asyncWork(Runnable run) {
        if (run != null) {
            CustomThreadPool.getInstance().excute(new TaskWrapper(run));
        }
    }

    @Override
    public void onDestroy() {
        if (USING_CUSTOM_THREADPOOL) {
            try {
                mExecutorService.shutdown();
                for (ThreadPoolExecutor e : mSpecialExectorMap.values()) {
                    e.shutdown();
                }
                mSpecialExectorMap.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected CustomThreadPool() {
        super();

        if (USING_CUSTOM_THREADPOOL) {
            mExecutorService = new ThreadPoolExecutor(CORE_THREAD_COUNT, MAX_THREAD_COUNT, KEEP_ALIVE_DELAY,
                    TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>(true), new PriorityThreadFactory(
//                            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new PriorityThreadFactory(
                            "custom-tpool", Process.THREAD_PRIORITY_BACKGROUND),
                    new ThreadPoolExecutor.DiscardPolicy() {
                        @Override
                        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                            super.rejectedExecution(r, e);

                            CoreConfig.LOGD(TAG, "one thread is rejected!!!");
                        }
                    });

            mSpecialExectorMap = new HashMap<String, ThreadPoolExecutor>();
        }
    }

    private ThreadPoolExecutor createSpecialThreadPoolExecutor(String specialName) {
        return new ThreadPoolExecutor(SPECIAL_CORE_THREAD_COUNT, SPECIAL_CORE_THREAD_COUNT, KEEP_ALIVE_DELAY,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new PriorityThreadFactory(specialName,
                        Process.THREAD_PRIORITY_BACKGROUND), new ThreadPoolExecutor.DiscardPolicy() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                        super.rejectedExecution(r, e);

                        CoreConfig.LOGD(TAG, "one thread is rejected!!!");
                    }
                });
    }

    @Override
    protected void init(Context context) {
    }

    public boolean excute(TaskWrapper task) {
        if (USING_CUSTOM_THREADPOOL) {
            return internalCustomExcute(task, 0);
        } else {
            if (task != null) {
                // new InternalAsyncTask().execute(task);
                InternalAsyncTask.execute(task);
            }
        }

        return true;
    }

    public boolean excuteWithSpecialThread(String specialWorkName, TaskWrapper task) {
        if (TextUtils.isEmpty(specialWorkName)) {
            return excute(task);
        } else {
            if (!mSpecialExectorMap.containsKey(specialWorkName)) {
                mSpecialExectorMap.put(specialWorkName, createSpecialThreadPoolExecutor(specialWorkName));
            }

            ThreadPoolExecutor e = mSpecialExectorMap.get(specialWorkName);
            if (e != null && !e.isShutdown()) {
                e.execute(task);
            } else {
                excute(task);
            }

            return false;
        }
    }

    public ThreadPoolSnapShot getSpecialThreadSnapShot(String name) {
        if (TextUtils.isEmpty(name)) {
            return getThreadSnapShot(mExecutorService);
        } else {
            if (!mSpecialExectorMap.containsKey(name)) {
                mSpecialExectorMap.put(name, createSpecialThreadPoolExecutor(name));
            }

            return getThreadSnapShot(mSpecialExectorMap.get(name));
        }
    }

    private ThreadPoolSnapShot getThreadSnapShot(ThreadPoolExecutor e) {
        if (e != null) {
            return new ThreadPoolSnapShot(e.getQueue().size(), e.getCorePoolSize(), e.getMaximumPoolSize());
        }

        return null;
    }

    public boolean excuteDelay(TaskWrapper task, long delay) {
        if (USING_CUSTOM_THREADPOOL) {
            return internalCustomExcute(task, delay);
        } else {
            if (task != null) {
                // new InternalAsyncTask().execute(task);
                InternalAsyncTask.execute(task);
            }
        }

        return true;
    }

    private boolean internalCustomExcute(final TaskWrapper task, long delay) {
        if (task == null) {
            return false;
        }

        if (mExecutorService.isShutdown()) {
            return false;
        }

        delay = delay < 0 ? 0 : delay;
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                task.run();
            }
        });

        return true;
    }

}