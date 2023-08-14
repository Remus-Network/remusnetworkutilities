package net.remusnetworkutilities.carpetedition;

import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExecutorServiceManager {
    private static final Logger LOGGER = Logger.getLogger(ExecutorServiceManager.class.getName());
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static ExecutorService getExecutor() {
        return executor;
    }

    public static void shutdownExecutor() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    LOGGER.severe("ExecutorService did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            LOGGER.log(Level.SEVERE, "ExecutorService shutdown interrupted", ie);
        }
    }
}
