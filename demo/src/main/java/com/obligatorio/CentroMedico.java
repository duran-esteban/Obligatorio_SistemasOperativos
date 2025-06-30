package com.obligatorio;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;

public class CentroMedico {
    private static RelojSimulado reloj;

    static Semaphore medico;
    static Semaphore enfermero;
    static Semaphore odontologo;
    static boolean odontologoHabilitado;
    
    private final PriorityBlockingQueue<Paciente> colaPacientes;
    private static Paciente pacienteActualMedico;
    private static Paciente pacienteActualEnfermero;
    private static Paciente pacienteActualOdontologo;
    private static int prioridadMaxActual;
    
    // Constructor
    public CentroMedico(int medicos, int enfermeros, int odontologos, RelojSimulado relojReferencia) {
        reloj = relojReferencia;
        medico = new Semaphore(medicos);
        enfermero = new Semaphore(enfermeros);
        odontologo = new Semaphore(odontologos);
        colaPacientes = new PriorityBlockingQueue<>();
        prioridadMaxActual = 0;
        pacienteActualMedico = null;
        pacienteActualEnfermero = null;
        pacienteActualOdontologo = null;

        if (odontologos > 0) {
            odontologoHabilitado = true;
        } else {
            odontologoHabilitado = false;
        }
    }

    // Getters
    public static int getPrioridadMaxActual() {
        return prioridadMaxActual;
    }

    public PriorityBlockingQueue<Paciente> getColaPacientes() {
        return colaPacientes;
    }

    public static void setPacienteActual(String especialista, Paciente paciente) {
        switch (especialista) {
            case "Médico y Enfermero":
                pacienteActualMedico = paciente;
                pacienteActualEnfermero = paciente;
                break;
            case "Enfermero":
                pacienteActualEnfermero = paciente;
                break;
            case "Odontólogo":
                pacienteActualOdontologo = paciente;
                break;
            default:
                System.out.println("Especialista desconocido: " + especialista);
        }
    }

    // Métodos
    public void ingresar(Paciente paciente) {
        // Chequea las condiciones para el ingreso del paciente
        if (paciente.getHoraLlegada() < 800 || paciente.getHoraLlegada() > 2000) {
            System.out.println("El paciente " + paciente.getNombre() + 
            " llegó cuando el centro ya había cerrado, no puede ingresar.");
            return;
        }
        if (paciente.getEspecialista().equals("Odontólogo") && !odontologoHabilitado) {
            System.out.println("[" + reloj.formatearHora(reloj.getHoraActual()) + 
            "] No hay odontólogos disponibles, el paciente " + paciente.getNombre() + 
            " no puede ingresar.");
            return;
        }
        colaPacientes.put(paciente); // se encola según prioridad
        new Thread(paciente).start(); // Inicia el hilo del paciente
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

        actualizarPrioridadMaxActual();
    }

    private void actualizarPrioridadMaxActual() {
        // Toma como prioridad máxima la del paciente con mayor prioridad en la cola
        if (!colaPacientes.isEmpty()) {
            this.prioridadMaxActual = colaPacientes.peek().getPrioridad();
        }
    }

    private void chequearPrioridadIngresoVsActual() {
        // Si el paciente que ingresa tiene una prioridad mayor a la máxima actual, 
        // interrumpe la consulta del paciente actual
        int prioridadPacienteTopCola = colaPacientes.peek().getPrioridad();
        if (prioridadPacienteTopCola > pacienteActualMedico.getPrioridad()) {
            pacienteActualMedico.interrumpirConsulta();
        }
    }

    public void pacientesNoAtendidos() {
        // Imprime los pacientes que no fueron atendidos al finalizar la simulación
        System.out.println("Pacientes no atendidos:");
        for (Paciente paciente : colaPacientes) {
            System.out.println("- " + paciente.getNombre() + " (Prioridad: " + paciente.getPrioridad() + 
            ", Hora de llegada: " + RelojSimulado.formatearHora(paciente.getHoraLlegada()) + 
            ", Tipo de consulta: " + paciente.getTipoConsulta() + ")");
        }
    }
}

