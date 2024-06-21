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

    @Override
    public void append(LoggingEvent event) {
        if (!eventQueue.offer(event)) {
            resizeBufferUp();
            eventQueue.offer(event);
        } else {
            resizeBufferDown();
        }
    }

    private synchronized void resizeBufferUp() {
        int currentSize = bufferSize.get();
        if (currentSize < maxBufferSize) {
            int newSize = Math.min(currentSize + resizeFactor, maxBufferSize);
            BlockingQueue<LoggingEvent> newQueue = new ArrayBlockingQueue<>(newSize);
            eventQueue.drainTo(newQueue);
            eventQueue = newQueue;
            bufferSize.set(newSize);
            super.setBufferSize(newSize);
            System.out.println("Increased log buffer size to " + newSize);
        } else {
            System.err.println("Log buffer overflow. Dropping log event.");
        }
    }

    private synchronized void resizeBufferDown() {
        int currentSize = bufferSize.get();
        int remainingEvents = eventQueue.size();
        if (currentSize > minBufferSize && remainingEvents < currentSize / 2) {
            int newSize = Math.max(currentSize - resizeFactor, minBufferSize);
            BlockingQueue<LoggingEvent> newQueue = new ArrayBlockingQueue<>(newSize);
            eventQueue.drainTo(newQueue);
            eventQueue = newQueue;
            bufferSize.set(newSize);
            super.setBufferSize(newSize);
            System.out.println("Decreased log buffer size to " + newSize);
        }
    }

    @Override
    public void close() {
        super.close();
        // Flush remaining events in the queue
        while (!eventQueue.isEmpty()) {
            LoggingEvent event = eventQueue.poll();
            if (event != null) {
                super.append(event);
            }
        }
    }
}
