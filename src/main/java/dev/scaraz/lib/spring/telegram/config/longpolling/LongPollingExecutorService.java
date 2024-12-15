package dev.scaraz.lib.spring.telegram.config.longpolling;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class LongPollingExecutorService extends ScheduledThreadPoolExecutor {

    private final ObservationRegistry observationRegistry;

    public LongPollingExecutorService(int corePoolSize, ObservationRegistry observationRegistry) {
        super(corePoolSize, Thread.ofVirtual().factory());
        this.observationRegistry = observationRegistry;
    }

    @NotNull
    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(@NotNull Runnable command, long initialDelay, long period, @NotNull TimeUnit unit) {
        return super.scheduleAtFixedRate(() -> {
            Observation observation = Observation.createNotStarted("telegram", observationRegistry);
            observation.observe(command);
        }, initialDelay, period, unit);
    }
}
