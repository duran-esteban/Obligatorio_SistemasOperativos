package com.obligatorio;

class Paciente implements Runnable, Comparable<Paciente> {
    private final String nombre;
    private final int horaLlegada;
    private final String tipoConsulta;
    private int prioridad;
    private int duracion;
    private boolean atendido = false;

    public Paciente(String nombre, int horaLlegada, String tipoConsulta, int prioridad, int duracion) {
        this.nombre = nombre;
        this.horaLlegada = horaLlegada;
        this.tipoConsulta = tipoConsulta;
        this.prioridad = prioridad;
        this.duracion = duracion;
    }

    public void run() {
        
    }

    public void atender() {
        atendido = true;
        while (duracion > 0) {
            try {
                Thread.sleep(500); // equivale a 5 minutos
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            duracion--;
        }
        // se va del centro
    }

    public void aumentarPrioridadSiEsUrgente(int tiempoActual) {
        if ((tiempoActual - horaLlegada) >= 2 * 12) { // 2 horas
            prioridad = 0; // máxima prioridad
        }
    }

    @Override
    public int compareTo(Paciente otro) {
        return Integer.compare(this.prioridad, otro.prioridad);
    }
}

