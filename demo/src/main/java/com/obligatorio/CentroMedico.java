package com.obligatorio;

import java.util.ArrayList;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;

public class CentroMedico extends Thread{
    private static RelojSimulado reloj;

    public static final Semaphore mutexAtencionGeneral = new Semaphore(1); // Exclusión mutua para atención general
    public static final Semaphore mutexAtencionOdontologia = new Semaphore(1); // Exclusión mutua para atención odontológica
    private static final Object lockPrioridadOdontologia = new Object(); // Para proteger prioridadMaxOdontologia
    private static final Object lockPrioridadGeneral = new Object(); // Para proteger prioridadMaxActual
    

    static Semaphore medico;
    static Semaphore enfermero;
    static Semaphore odontologo;
    static boolean odontologoHabilitado;
    
    private PriorityBlockingQueue<Paciente> pacientesOdontologia = null;
    private final PriorityBlockingQueue<Paciente> colaPacientes;
    private static ArrayList<Paciente> pacientesAtendiendo;
    private static int prioridadMaxOdontologia;
    private static int prioridadMaxActual;
    
    // Constructor
    public CentroMedico(int medicos, int enfermeros, int odontologos, RelojSimulado relojReferencia) {
        reloj = relojReferencia;
        medico = new Semaphore(medicos);
        enfermero = new Semaphore(enfermeros);
        odontologo = new Semaphore(odontologos);
        colaPacientes = new PriorityBlockingQueue<>();
        prioridadMaxActual = 0;
        pacientesAtendiendo = new ArrayList<>();

        if (odontologos > 0) {
            odontologoHabilitado = true;
            prioridadMaxOdontologia = 0;
            pacientesOdontologia = new PriorityBlockingQueue<>();
        } else {
            odontologoHabilitado = false;
        }
    }

    // Getters
    public static int getPrioridadMaxActual() {
        synchronized (lockPrioridadGeneral) {
            return prioridadMaxActual;
        }
    }

    private static void setPrioridadMaxActual(int nuevaPrioridad) {
        synchronized (lockPrioridadGeneral) {
            prioridadMaxActual = nuevaPrioridad;
        }
    }

    public static void añadirPacienteAtendiendo(Paciente paciente) {
        pacientesAtendiendo.add(paciente);
    }

    public static void eliminarPacienteAtendiendo(Paciente paciente) {
        pacientesAtendiendo.remove(paciente);
    }

    public PriorityBlockingQueue<Paciente> getColaPacientes() {
        return colaPacientes;
    }

    public static int getPrioridadMaxOdontologia() {
        synchronized (lockPrioridadOdontologia) {
            return prioridadMaxOdontologia;
        }
    }

    private static void setPrioridadMaxOdontología(int nuevaPrioridad) {
        synchronized (lockPrioridadOdontologia) {
            prioridadMaxActual = nuevaPrioridad;
        }
    }

    // Métodos
    @SuppressWarnings("static-access")
    public void ingresar(Paciente paciente) {
        // Chequea las condiciones para el ingreso del paciente
        if (paciente.getHoraLlegada() < 800 || paciente.getHoraLlegada() > 2000) {
            System.out.println("El paciente " + paciente.getNombre() + 
            " llegó cuando el centro ya había cerrado, no puede ingresar.");
            return;
        }
        if (paciente.getEspecialista().equals("Odontólogo")){
            if (!odontologoHabilitado) {
            System.out.println("[" + reloj.formatearHora(reloj.getHoraActual()) + 
            "] El paciente " + paciente.getNombre() + 
            " no puede ingresar porque no hay odontólogos disponibles.");
            return;
            } else {
                pacientesOdontologia.put(paciente); // se encola según prioridad en Odontología
                new Thread(paciente).start(); // Inicia el hilo del paciente
            }
        } else {
            colaPacientes.put(paciente); // se encola según prioridad en la cola general
            new Thread(paciente).start(); // Inicia el hilo del paciente
        }
    }

    public void reordenarCola() {
        // Actualiza la cola de pacientes, recalculando prioridades y eliminando
        // pacientes que ya no deberían estar (pacientes ya atendidos por completo o fallecidos)
        PriorityBlockingQueue<Paciente> nuevaCola = new PriorityBlockingQueue<>();
        while (!colaPacientes.isEmpty()) {
            Paciente paciente = colaPacientes.poll();
            if (paciente.getTicksDuracion() > 0 && paciente.getEstaVivo()) {
                paciente.recalcularPrioridad(reloj.getHoraActual());
                
                // Espera a que el paciente haya actualizado su prioridad 
                // antes de añadirlo ordenadamente a la cola
                try {
                    paciente.actualizado.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                nuevaCola.put(paciente);
            }
        }
        colaPacientes.clear();
        colaPacientes.addAll(nuevaCola);

        actualizarPrioridadMaxActual();

        chequearInterrupcionNuevoPrioritario();
    }

    @SuppressWarnings("static-access")
    private void actualizarPrioridadMaxActual() {
        // Toma como prioridad máxima la del paciente con mayor prioridad en la cola
        if (!colaPacientes.isEmpty()) {
            setPrioridadMaxActual(colaPacientes.peek().getPrioridad());
        }
    }

    private void chequearInterrupcionNuevoPrioritario() {
        if (colaPacientes.isEmpty()) {
            return;
        }
        PriorityBlockingQueue<Paciente> copiaCola = new PriorityBlockingQueue<>(this.colaPacientes);
        @SuppressWarnings("static-access")
        int cantidadIteraciones = this.pacientesAtendiendo.size();

        // Toma los primeros X pacientes de la cola (donde X es la cantidad de pacientes que están siendo atendidos)
        for (int i = 0; i < cantidadIteraciones; i++) {
            if (!copiaCola.isEmpty()) {
                Paciente paPrioritario = copiaCola.poll();

                // Si el paciente con mayor prioridad en la cola no está siendo atendido,
                if (!paPrioritario.getAtendiendo()) {
                    Paciente paMenosPrioritario = getMinPrioridadPacientesAtendiendo();

                    // y a su vez hay un paciente siendo atendido menos prioritario,
                    if (paMenosPrioritario !=null && paPrioritario.getPrioridad() < paMenosPrioritario.getPrioridad()) {
                        
                        // Interrumple la consulta del paciente menos prioritario,
                        // lo quita de la lista de atendidos y lo devuelve a la cola de espera
                        paMenosPrioritario.interrumpirConsulta();
                        pacientesAtendiendo.remove(paMenosPrioritario);
                        colaPacientes.add(paMenosPrioritario);
                    }
                }
            }
        }
        copiaCola.clear();
    }

    private Paciente getMinPrioridadPacientesAtendiendo() {
        // Devuelve el paciente con la menor prioridad que está siendo atendido 
        // (el número más alto, 0 = máxima prioridad, 10 = mínima prioridad)
        if (pacientesAtendiendo.isEmpty()) {
            return null; 
        }
        Paciente pacienteMenosPrioritario = pacientesAtendiendo.get(0);
        for (Paciente paciente : pacientesAtendiendo) {
            if (paciente.getPrioridad() > pacienteMenosPrioritario.getPrioridad()) {
                pacienteMenosPrioritario = paciente;
            }
        }
        return pacienteMenosPrioritario;
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
    
    // Métodos Odontología
    public void reordenarPacientesOdontologia() {
        // Actualiza la cola de pacientes del odontólogo, recalculando prioridades y eliminando
        // pacientes que ya no deberían estar (pacientes ya atendidos por completo)
        PriorityBlockingQueue<Paciente> nuevaCola = new PriorityBlockingQueue<>();
        while (!pacientesOdontologia.isEmpty()) {
            Paciente paciente = pacientesOdontologia.poll();
            if (paciente.getTicksDuracion() > 0) {
                paciente.recalcularPrioridad(reloj.getHoraActual());
                
                // Espera a que el paciente haya actualizado su prioridad 
                // antes de añadirlo ordenadamente a la cola
                try {
                    paciente.actualizado.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                nuevaCola.put(paciente);
            }
        }
        pacientesOdontologia.clear();
        pacientesOdontologia.addAll(nuevaCola);

        actualizarPrioridadMaxOdontologia();
        // No hace interrupciones porque las consultas odontológicas deberían ser cortas y no urgentes
    }

    @SuppressWarnings("static-access")
    private void actualizarPrioridadMaxOdontologia() {
        // Toma como prioridad máxima la del paciente con mayor prioridad en la cola
        if (!pacientesOdontologia.isEmpty()) {
            setPrioridadMaxOdontología(pacientesOdontologia.peek().getPrioridad());
        }
    }

    // Bucle principal
    @SuppressWarnings("static-access")
    @Override
    public void run() {
        System.out.println("[08:00] Abre el Centro Médico" );
        while (reloj.getHoraActual() < 2000) { // Mientras el reloj no llegue a las 20:00
            reordenarCola();
            if (odontologoHabilitado) {
                reordenarPacientesOdontologia();
            }
            try {
                Thread.sleep(reloj.getFrecuenciaTick()); // Reordena la cola cada tick
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        pacientesNoAtendidos(); // Imprime los pacientes que no fueron atendidos al finalizar la simulación
    }
}

