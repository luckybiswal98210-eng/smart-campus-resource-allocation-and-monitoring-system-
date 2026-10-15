package com.smartcampus.service;

import com.smartcampus.dao.ResourceDAO;
import com.smartcampus.dao.TelemetryDAO;
import com.smartcampus.model.Resource;
import com.smartcampus.model.Telemetry;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Multithreaded IoT Telemetry and Energy Sensor Simulator
 * Simulates real-time campus sensors and automated eco-energy management
 */
public class SensorSimulator implements Runnable {

    public interface TelemetryListener {
        void onTelemetryUpdated();
    }

    private static SensorSimulator instance;
    private Thread workerThread;
    private volatile boolean running = false;
    private final ResourceDAO resourceDAO = new ResourceDAO();
    private final TelemetryDAO telemetryDAO = new TelemetryDAO();
    private final Random random = new Random();
    private final List<TelemetryListener> listeners = new ArrayList<>();

    private SensorSimulator() {}

    public static synchronized SensorSimulator getInstance() {
        if (instance == null) {
            instance = new SensorSimulator();
        }
        return instance;
    }

    public synchronized void addListener(TelemetryListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public synchronized void removeListener(TelemetryListener listener) {
        listeners.remove(listener);
    }

    public synchronized void start() {
        if (running) return;
        running = true;
        workerThread = new Thread(this, "SmartCampus-SensorSimulator");
        workerThread.setDaemon(true);
        workerThread.start();
        System.out.println("🚀 [SensorSimulator] Background IoT Telemetry Thread Started.");
    }

    public synchronized void stop() {
        running = false;
        if (workerThread != null) {
            workerThread.interrupt();
        }
        System.out.println("🛑 [SensorSimulator] Background IoT Telemetry Thread Stopped.");
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void run() {
        while (running) {
            try {
                // Run simulation cycle
                simulateCycle();

                // Notify UI listeners on Swing Event Dispatch Thread
                SwingUtilities.invokeLater(() -> {
                    for (TelemetryListener l : listeners) {
                        try {
                            l.onTelemetryUpdated();
                        } catch (Exception e) {
                            System.err.println("Error notifying telemetry listener: " + e.getMessage());
                        }
                    }
                });

                // Sleep for 6 seconds between telemetry updates
                Thread.sleep(6000);

            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                System.err.println("SensorSimulator cycle exception: " + e.getMessage());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                    break;
                }
            }
        }
    }

    private void simulateCycle() {
        List<Resource> resources = resourceDAO.getAllResources();
        if (resources.isEmpty()) return;

        for (Resource res : resources) {
            Telemetry t = new Telemetry();
            t.setResourceId(res.getResourceId());
            t.setResourceName(res.getName());

            boolean isOccupied = "ALLOCATED".equalsIgnoreCase(res.getStatus());
            int capacity = Math.max(1, res.getCapacity());
            double ratedPower = res.getPowerRatingKw();

            if (isOccupied) {
                // 40% to 95% occupancy
                int occ = (int) (capacity * (0.45 + (random.nextDouble() * 0.45)));
                t.setOccupancyCount(Math.max(1, Math.min(capacity, occ)));
                
                // Active power draw (60% - 95% of max)
                double power = ratedPower * (0.60 + (random.nextDouble() * 0.35));
                t.setPowerDrawKw(Math.round(power * 10.0) / 10.0);

                // Regulated AC temperature
                double temp = 21.0 + (random.nextDouble() * 2.5);
                t.setTemperatureC(Math.round(temp * 10.0) / 10.0);

                t.setHvacStatus("ON");
                t.setLightStatus("ON");
            } else {
                // Vacant room
                t.setOccupancyCount(0);

                // Minimal standby power draw (0.1 - 0.4 kW)
                double power = Math.min(0.4, ratedPower * 0.05 + (random.nextDouble() * 0.15));
                t.setPowerDrawKw(Math.round(power * 10.0) / 10.0);

                // Ambient temperature
                double temp = 24.5 + (random.nextDouble() * 2.0);
                t.setTemperatureC(Math.round(temp * 10.0) / 10.0);

                t.setHvacStatus("OFF");
                t.setLightStatus("OFF");
            }

            telemetryDAO.logTelemetry(t);
        }
    }
}
