package com.obligatorio;

import java.util.concurrent.Semaphore;

// Esta clase se encarga de simular el paso del tiempo, avisando a los hilos cuando pasa un tick

class RelojSimulado extends Thread {
    private int horaActual = 800;   // inicia el día a las 08:00
    final Semaphore pasoTick = new Semaphore(0); // Semáforo para avisar el paso de un tick
    private static int frecuenciaTick; // Cada cuántos milisegundos avanza un tick/unidad de tiempo (Por defecto, 1seg)
    private static int equivalenciaTick; // A cuántos minutos virtuales equivale un tick (Por defecto, 5 min)
    private final Object lock = new Object();

    // Constructores
    public RelojSimulado() {
        super("Reloj Simulado");
        this.frecuenciaTick = 1000; // 1 segundo por defecto
        this.equivalenciaTick = 5; // 5 minutos por defecto
    }

    public RelojSimulado(int frecuenciaTick, int equivalenciaTick) {
        super("Reloj Simulado");
        this.frecuenciaTick = frecuenciaTick;
        this.equivalenciaTick = equivalenciaTick;
    }

    // Getters
    public int getHoraActual() {
        synchronized (lock) {
            return horaActual;
        }
    }

    public static int getFrecuenciaTick() {
        return frecuenciaTick;
    }

    public static int getEquivalenciaTick() {
        return equivalenciaTick;
    }

    // Loop principal
    public void run() {
        System.out.println("[" + formatearHora(horaActual) + "] Reloj simulado iniciado. Abre el Centro Médico" );
        while (horaActual < 2000) { // hasta las 20:00
            try {
                Thread.sleep(frecuenciaTick); // Pasa un tick
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            synchronized (lock) {
                horaActual += equivalenciaTick; // Avanza el reloj la cantidad de minutos que dura un tick
                if (horaActual % 100 >= 60) {
                    horaActual -= 60;
                    horaActual += 100; // Cuando pasan 60 min, transforma la hora de de 08:60 a 09:00
                }
                lock.notifyAll(); // avisamos a los hilos que avanzó el tiempo
            }
            pasoTick.release(); // Avisa que pasó un tick (GestorPacientes debería estar esperando este semáforo)
        }
    }

    // Devuelve la hora en formato HH:MM para mostrar en consola
    public static String formatearHora(int hora) {
        int horas = hora / 100;
        int minutos = hora % 100;
        String resultado;

        // Agrega el cero cuando la hora tiene solo 1 digito (ej. 900 -> 09:00)
        if (horas < 10) {
            resultado = "0" + horas;
        }
        else{
            resultado = "" + horas;
        }

        // Agrega el cero cuando los minutos tienen solo 1 dígito (ej. 1001 -> 10:01, en vez de 10:1)
        if (minutos < 10) {
            resultado += ":0" + minutos; 
        } else {
            resultado += ":" + minutos;
        }
        return resultado;
    }
}
