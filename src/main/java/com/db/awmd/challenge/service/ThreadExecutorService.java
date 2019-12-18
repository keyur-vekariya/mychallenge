package com.db.awmd.challenge.service;

import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class ThreadExecutorService
{
    private static final int THREAD_POOL_SIZE = 20;
    private ExecutorService threadPool  = null;

    public synchronized ExecutorService getThreadPool()
    {
        if(threadPool == null){
            threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        }
        return threadPool;
    }

    public  int getActiveThreadCount(){
        return ((ThreadPoolExecutor)threadPool).getActiveCount();
    }

    public  int getTotalThreadsFromPool(){
        return ((ThreadPoolExecutor)threadPool).getPoolSize();
    }

    @PreDestroy
    public void shutDownThreadExecutor(){
        ((ThreadPoolExecutor)threadPool).shutdown();
    }
}
