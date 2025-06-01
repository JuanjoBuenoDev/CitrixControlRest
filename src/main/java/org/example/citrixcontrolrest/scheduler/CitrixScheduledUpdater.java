package org.example.citrixcontrolrest.scheduler;

import org.example.citrixcontrolrest.model.DDCDTO;
import org.example.citrixcontrolrest.service.CitrixService;
import org.example.citrixcontrolrest.utils.ToastNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;


public class CitrixScheduledUpdater {
    private static final Logger logger = LoggerFactory.getLogger(CitrixScheduledUpdater.class);
    private static final int DEFAULT_THREAD_POOL_SIZE = 6;
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 30;


    private ScheduledExecutorService scheduler;
    private ExecutorService executor;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private UpdateListener updateListener;
    private volatile List<DDCDTO> ddcList;
    private final ToastNotifier toastNotifier;

    // Funciones para realizar las actualizaciones (inyectadas desde el servicio)
    private Function<String, Void> refreshDGsFunction;
    private Function<String, Void> refreshVDAsFunction;
    private Function<String, Void> refreshAppsFunction;
    private Function<String, Void> refreshActiveUsersFunction;
    private Function<String, Void> refreshCitrixSiteFunction;
    private Function<String, Void> refreshDDCsFunction;

    public CitrixScheduledUpdater(int threadPoolSize, ToastNotifier toastNotifier) {
        this.toastNotifier = toastNotifier;
        this.scheduler = null; // Se creará en start()
        this.executor = null;
    }

    // Setters para las funciones de actualización
    public void setRefreshDGsFunction(Function<String, Void> refreshDGsFunction) {
        this.refreshDGsFunction = refreshDGsFunction;
    }

    public void setRefreshVDAsFunction(Function<String, Void> refreshVDAsFunction) {
        this.refreshVDAsFunction = refreshVDAsFunction;
    }

    public void setRefreshAppsFunction(Function<String, Void> refreshAppsFunction) {
        this.refreshAppsFunction = refreshAppsFunction;
    }

    public void setRefreshActiveUsersFunction(Function<String, Void> refreshActiveUsersFunction) {
        this.refreshActiveUsersFunction = refreshActiveUsersFunction;
    }

    public void setRefreshCitrixSiteFunction(Function<String, Void> refreshCitrixSiteFunction) {
        this.refreshCitrixSiteFunction = refreshCitrixSiteFunction;
    }

    public void setRefreshDDCsFunction(Function<String, Void> refreshDDCsFunction) {
        this.refreshDDCsFunction = refreshDDCsFunction;
    }

    public void setUpdateListener(UpdateListener listener) {
        this.updateListener = listener;
    }

    public synchronized void setDdcList(List<DDCDTO> ddcs) {
        this.ddcList = Objects.requireNonNull(ddcs, "La lista de DDCs no puede ser nula");
        logger.info("Lista de DDCs configurada. Cantidad: {}", this.ddcList.size());
    }

    public synchronized void start() {
        if (isRunning.compareAndSet(false, true)) {

            if (scheduler == null || scheduler.isShutdown() || scheduler.isTerminated()) {
                scheduler = Executors.newSingleThreadScheduledExecutor();
            }

            if (executor == null || executor.isShutdown() || executor.isTerminated()) {
                executor = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
            }

            scheduler.scheduleAtFixedRate(
                    this::runCitrixRefresh,
                    0,
                    2,
                    TimeUnit.MINUTES
            );
            logger.info("Programador Citrix iniciado");
        } else {
            logger.warn("El programador ya está en ejecución");
        }
    }


    private void runCitrixRefresh() {
        if (ddcList == null || ddcList.isEmpty()) {
            logger.error("No hay DDCs activos disponibles. No se puede lanzar actualización.");
            if (updateListener != null) {
                updateListener.onUpdateFinished(false);
            }
            return;
        }

        toastNotifier.showToast("🔄 Iniciando carga de datos Citrix...", ToastNotifier.ToastType.INFO, 1000);

        try {
            boolean success = performCitrixRefresh();
            if (success) {
                toastNotifier.showToast("✅ Carga de datos Citrix finalizada correctamente.", ToastNotifier.ToastType.SUCCESS, 1000);
            } else {
                toastNotifier.showToast("❌ Error durante la carga de datos Citrix.", ToastNotifier.ToastType.ERROR, 2000);
            }
            logger.info("Actualización Citrix completada: {}", success ? "OK" : "FALLÓ");
            if (updateListener != null) {
                updateListener.onUpdateFinished(success);
            }
        } catch (Exception e) {
            logger.error("Error en la actualización programada", e);
            toastNotifier.showToast("❌ Error durante la carga de datos Citrix.", ToastNotifier.ToastType.ERROR, 2000);

            if (updateListener != null) {
                updateListener.onUpdateFinished(false);
            }
        }
    }


    public boolean performCitrixRefresh() throws InterruptedException {
        List<DDCDTO> currentDdcs = this.ddcList;
        if (currentDdcs == null || currentDdcs.isEmpty()) {
            throw new IllegalStateException("No hay DDCs activos configurados");
        }

        Callable<Void>[] tasks = new Callable[]{
                () -> { refreshDGsFunction.apply(getActiveDdcForIndex(0)); return null; },
                () -> { refreshVDAsFunction.apply(getActiveDdcForIndex(1)); return null; },
                () -> { refreshAppsFunction.apply(getActiveDdcForIndex(2)); return null; },
                () -> { refreshActiveUsersFunction.apply(getActiveDdcForIndex(3)); return null; },
                () -> { refreshCitrixSiteFunction.apply(getActiveDdcForIndex(4)); return null; },
                () -> { refreshDDCsFunction.apply(getActiveDdcForIndex(5)); return null; }
        };

        boolean success = true;

        try {
            List<Future<Void>> futures = executor.invokeAll(List.of(tasks));

            for (Future<Void> future : futures) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    logger.error("Error en tarea de actualización", e.getCause());
                    success = false;
                }
            }
        } catch (InterruptedException e) {
            logger.warn("Actualización interrumpida");
            Thread.currentThread().interrupt();
            success = false;
            throw e;
        } finally {
            if (updateListener != null) {
                updateListener.onUpdateFinished(success);
            }
        }

        return success;
    }

    private String getActiveDdcForIndex(int index) {
        List<DDCDTO> currentDdcs = this.ddcList;
        if (currentDdcs == null || currentDdcs.isEmpty()) {
            throw new IllegalStateException("No hay DDCs activos disponibles");
        }
        return currentDdcs.get(index % currentDdcs.size()).getDnsName();
    }

    public void stop() {
        if (isRunning.compareAndSet(true, false)) {
            try {
                if (scheduler != null) {
                    scheduler.shutdown();
                    if (!scheduler.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                        scheduler.shutdownNow();
                    }
                }

                if (executor != null) {
                    executor.shutdown();
                    if (!executor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                    }
                }

                logger.info("Programador Citrix detenido correctamente");
            } catch (InterruptedException e) {
                logger.warn("Interrupción durante el apagado");
                Thread.currentThread().interrupt();
                if (scheduler != null) scheduler.shutdownNow();
                if (executor != null) executor.shutdownNow();
            } finally {
                scheduler = null;
                executor = null;
            }
        }
    }


    public boolean isRunning() {
        return isRunning.get();
    }
}