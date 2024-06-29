package com.example.demo;

import org.apache.log4j.AsyncAppender;
import org.apache.log4j.spi.LoggingEvent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class AutoResizeAsyncAppender extends AsyncAppender {
    private volatile BlockingQueue<LoggingEvent> eventQueue;
    private AtomicInteger bufferSize;
    private int maxBufferSize;
    private int minBufferSize;
    private int resizeFactor;
    private boolean blockOnFull;

    public AutoResizeAsyncAppender() {
        super();
    }

    public void setInitialBufferSize(int size) {
        this.bufferSize = new AtomicInteger(size);
        this.eventQueue = new ArrayBlockingQueue<>(size);
        super.setBufferSize(size);
    }

    public void setMaxBufferSize(int maxSize) {
        this.maxBufferSize = maxSize;
    }

    public void setMinBufferSize(int minSize) {
        this.minBufferSize = minSize;
    }

    public void setResizeFactor(int factor) {
        this.resizeFactor = factor;
    }

    public void setBlockOnFull(boolean blockOnFull) {
        this.blockOnFull = blockOnFull;
    }

    @Override
    public void append(LoggingEvent event) {
        if (!eventQueue.offer(event)) {
            resizeBufferUp();
            if (blockOnFull) {
                try {
                    eventQueue.put(event);  // 블록 방식으로 로그 이벤트 추가
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Failed to append log event: " + e.getMessage());
                }
            } else {
                System.err.println("Log buffer overflow. Dropping log event.");
            }
        } else {
            resizeBufferDown();
        }
    }

    private synchronized void
