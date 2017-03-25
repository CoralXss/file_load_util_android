package com.coral.load.executor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by xss on 2017/3/19.
 */

public class ThreadExecutor implements InteractorExecutor {
    private static final int CORE_POOL_SIZE = 0;
    private static final int MAX_POOL_SIZE = Integer.MAX_VALUE;
    private static final int KEEP_ALIVE_TIME = 60;
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
    private static final BlockingQueue<Runnable> WORK_QUEUE = new LinkedBlockingQueue<>();

    private ExecutorService executorService;

    public ThreadExecutor() {
        if (executorService == null) {
//            Executors.newCachedThreadPool();
            executorService = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
                    KEEP_ALIVE_TIME, TIME_UNIT, WORK_QUEUE);
        }
    }

    public void shutdown() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    public boolean isTerminated() {
        return executorService != null ? executorService.isTerminated() : true;
    }

    @Override
    public void execute(Runnable command) {
        if (command != null) {
            executorService.execute(command);
        }
    }
}
