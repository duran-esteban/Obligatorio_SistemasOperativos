package com.obligatorio;

class RelojSimulado extends Thread {
    private int horaActual = 800;
    private final Object lock = new Object();

    public void run() {
        while (horaActual < 2000) { // hasta las 20:00
            try {
                Thread.sleep(1000); // 1000 ms = 5 minutos simulados
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            synchronized (lock) {
                horaActual += 5;
                if (horaActual % 100 >= 60) {
                    horaActual -= 60;
                    horaActual += 100; // pasar de 08:55 a 09:00
                }
                lock.notifyAll(); // avisamos a los hilos que avanzó el tiempo
            }
        }
    }

    public int getHoraActual() {
        synchronized (lock) {
            return horaActual;
        }
    }
}