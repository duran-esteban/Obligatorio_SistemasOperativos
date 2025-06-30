package com.obligatorio;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;

public class CentroMedico {
    private static RelojSimulado reloj;

    static Semaphore medico;
    static Semaphore enfermero;
    static Semaphore odontologo;
    
    private final PriorityBlockingQueue<Paciente> colaPacientes;
    private static Paciente pacienteActual;
    private static int prioridadMaxActual;
    
    // Constructor
    public CentroMedico(int medicos, int enfermeros, int odontologos, RelojSimulado relojReferencia) {
        reloj = relojReferencia;
        medico = new Semaphore(medicos);
        enfermero = new Semaphore(enfermeros);
        odontologo = new Semaphore(odontologos);
        colaPacientes = new PriorityBlockingQueue<>();
        prioridadMaxActual = 0;
        pacienteActual = null;
    }

    // Getters
    public static int getPrioridadMaxActual() {
        return prioridadMaxActual;
    }

    // Métodos
    public void ingresar(Paciente paciente) {
        colaPacientes.put(paciente); // se encola según prioridad
    }

    public void reordenarCola() {
        PriorityBlockingQueue<Paciente> nuevaCola = new PriorityBlockingQueue<>();
        while (!colaPacientes.isEmpty()) {
            Paciente paciente = colaPacientes.poll();
            paciente.recalcularPrioridad(reloj.getHoraActual());
            nuevaCola.put(paciente);
        }
        colaPacientes.clear();
        colaPacientes.addAll(nuevaCola);
    }
}

