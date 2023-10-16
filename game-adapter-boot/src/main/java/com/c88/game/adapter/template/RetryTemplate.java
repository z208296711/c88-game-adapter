package com.c88.game.adapter.template;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/*
 * @Author Terry
 * @Date 2023/4/16 11:43 AM
 * @Description retry method template
 */

@Slf4j
public abstract class RetryTemplate {

    private static final int DEFAULT_RETRY_TIME = 3;

    private int retryTime = DEFAULT_RETRY_TIME;

    private int sleepTime = 15*1000;

    public int getSleepTime() {
        return sleepTime;
    }

    public RetryTemplate setSleepTime(int sleepTime) {
        if(sleepTime < 0) {
            throw new IllegalArgumentException("sleepTime should equal or bigger than 0");
        }

        this.sleepTime = sleepTime;
        return this;
    }

    public int getRetryTime() {
        return retryTime;
    }

    public RetryTemplate setRetryTime(int retryTime) {
        if (retryTime <= 0) {
            throw new IllegalArgumentException("retryTime should bigger than 0");
        }

        this.retryTime = retryTime;
        return this;
    }


    protected abstract Object doBiz() throws Exception;


    public Object execute() throws InterruptedException {
        for (int i = 0; i < retryTime; i++) {
            try {
                return doBiz();
            } catch (Exception e) {
                log.error("重試執行異常，e: {}", e.toString());
                Thread.sleep(sleepTime);
            }
        }
        return null;
    }


    public Object submit(ExecutorService executorService) {
        if (executorService == null) {
            throw new IllegalArgumentException("please choose executorService!");
        }

        return executorService.submit((Callable) () -> execute());
    }

}
