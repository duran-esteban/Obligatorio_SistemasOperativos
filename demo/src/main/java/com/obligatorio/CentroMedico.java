package com.obligatorio;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;

public class CentroMedico {
    private final PriorityBlockingQueue<Paciente> cola = new PriorityBlockingQueue<>();
    private static final Semaphore medico = new Semaphore(1);
    private static final Semaphore enfermero = new Semaphore(1);
    private static int prioridadMaxActual;
    private Paciente pacienteActual = null;
    

    public void ingresar(Paciente paciente) {
        cola.put(paciente); // se encola según prioridad
    }

    public void atenderPacientes() {
        while (true) {
            Paciente paciente = cola.take();
            // lógica para asignar recurso y atender según especialidad
        }
    }

    public static int getPrioridadMaxActual() {
        return prioridadMaxActual;
    }

    public void reordenarCola() {
        PriorityBlockingQueue<Paciente> nuevaCola = new PriorityBlockingQueue<>();
        while (!cola.isEmpty()) {
            Paciente paciente = cola.poll();
            paciente.aumentarPrioridadSiEsUrgente(RelojSimulado.getHoraActual());
            nuevaCola.put(paciente);
        }
        cola.clear();
        cola.addAll(nuevaCola);
    }
}

