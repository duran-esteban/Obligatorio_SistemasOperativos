package com.obligatorio;

import java.util.concurrent.Semaphore;

class Paciente implements Runnable, Comparable<Paciente> {
    private final String nombre;
    private final int horaLlegada;
    private final String tipoConsulta;
    private final RelojSimulado reloj;

    private int horaUltimaActualizacion;    // Hora de la última actualización de prioridad
    private int prioridad;
    private int prioridadOriginal;
    private int ticksDuracion;      // Ticks restantes para completar la consulta

    Semaphore actualizado = new Semaphore(0);
    private boolean atendiendo = false;
    private boolean interrumpido = false;
    private boolean vivo = true;

    // Constructor
    public Paciente(String nombre, int horaLlegada, String tipoConsulta, int prioridad, int duracion, RelojSimulado reloj) {
        this.nombre = nombre;
        this.horaLlegada = horaLlegada;
        this.tipoConsulta = tipoConsulta;
        this.reloj = reloj;
        this.horaUltimaActualizacion = horaLlegada;
        this.prioridadOriginal = prioridad;
        this.prioridad = prioridad;
        this.ticksDuracion = duracion;
    }

    // Getters
    public String getNombre() {
        return nombre;
    }

    public String getTipoConsulta() {
        return tipoConsulta;
    }

    public int getHoraLlegada() {
        return horaLlegada;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public int getTicksDuracion() {
        return ticksDuracion;
    }

    public boolean getEstaVivo() {
        return vivo;
    }

    public boolean getAtendiendo() {
        return atendiendo;
    }

    public String getEspecialista() {
        String consultaMinuscula = this.tipoConsulta.toLowerCase();

        if (consultaMinuscula.contains("emergencia") 
            || consultaMinuscula.contains("urgencia")
            || consultaMinuscula.contains("carné de salud")) {
            return "Médico y Enfermero";
        } 
        else if (consultaMinuscula.contains("análisis") 
            || consultaMinuscula.contains("curación") 
            || consultaMinuscula.contains("examen") ) {
            return "Enfermero";
        } 
        else if (consultaMinuscula.contains("odontológica")) {
            return "Odontólogo";
        } 
        else {
            return "Desconocido";
        }
    }

    // Métodos
    @Override
    public int compareTo(Paciente otro) {
        return Integer.compare(this.prioridad, otro.prioridad);
    }

    private int convertirHoraAMinutos(int hora) {
        int horas = hora / 100;
        int minutos = hora % 100;
        return horas * 60 + minutos;
    }

    private int calcularDiferenciaTiempo(int horaActual, int horaPrevia) {
        return convertirHoraAMinutos(horaActual) - convertirHoraAMinutos(horaPrevia);
    }

    @SuppressWarnings("static-access")
    public void recalcularPrioridad(int horaActual) {
        if (atendiendo) {
            actualizado.release(); 
            return;
        }
        int esperaTotal = calcularDiferenciaTiempo(horaActual, horaLlegada);
        int minutosEsperando = calcularDiferenciaTiempo(horaActual, horaUltimaActualizacion);

        if (prioridadOriginal == 0) {   // Emergencia (prioridad 0)
            // Si lleva más de 10 minutos esperando, el paciente se considera fallecido
            if (minutosEsperando >= 10) {
                prioridad = 0; // Emergencia, no cambia
                this.vivo = false;
                System.out.println("[" + reloj.formatearHora(reloj.getHoraActual()) + "] El paciente " + this.nombre +
                                   " ha fallecido tras esperar más de 10 minutos por su emergencia.");
            } 
        } 
        else if (prioridadOriginal <= 5) {  // Urgencia (prioridad 1 a 5)
            // Si lleva más de 110 minutos esperando, se convierte en Emergencia
            if (esperaTotal >= 110) {
                prioridadOriginal = 0;
                prioridad = 0; // Emergencia
                System.out.println("[" + reloj.formatearHora(reloj.getHoraActual()) + "] El paciente " + this.nombre +
                                   " lleva esperando 110 minutos o más. Ahora es una emergencia.");
            } 
            // Cada 15 minutos, aumenta la prioridad, con prioridad máxima 1
            if (minutosEsperando >= 15 && prioridad != 1) {
                prioridad -= 1; 
                this.horaUltimaActualizacion = horaActual;
                System.out.println("[" + reloj.formatearHora(reloj.getHoraActual()) + "] El paciente " + this.nombre +
                                   " ahora tiene prioridad: " + prioridad);
            }
        } 
        else if (prioridadOriginal <= 10) {   // Consulta normal (prioridad 6 a 10)
            // Cada 30 minutos, aumenta la prioridad, con prioridad máxima 6
            if (minutosEsperando >= 30 && prioridad != 6) {
                prioridad -= 1;
                this.horaUltimaActualizacion = horaActual;
                System.out.println("[" + reloj.formatearHora(reloj.getHoraActual()) + "] El paciente " + this.nombre +
                                   " ahora tiene prioridad: " + prioridad);
            } 
        }
        // Avisa a Centro Médico que terminó de actualizar su prioridad
        actualizado.release(); 
    }

    @SuppressWarnings("static-access")
    public void serAtendido() {
        if (!this.vivo) {
        return; // Si el paciente no está vivo, no puede ser atendido
        }
        atendiendo = true;

        System.out.println("[" + reloj.formatearHora(reloj.getHoraActual()) + "] El paciente " + this.nombre +
                           " está siendo atendido por: " + this.getEspecialista() +
                           ", Tipo de consulta: " + this.tipoConsulta);

        while (ticksDuracion > 0 && atendiendo) {
            if (interrumpido) {
                System.out.println("[" + reloj.formatearHora(reloj.getHoraActual()) + "] La consulta del paciente " + this.nombre +
                                   " ha sido interrumpida.");
                this.atendiendo = false; // Marca que ya no está siendo atendido
                this.interrumpido = false; // Resetea el estado de interrumpido
                return; // Sale del método
            }
            else {
                try {
                    Thread.sleep(RelojSimulado.getFrecuenciaTick()); // Duerme durante un tick (Por defecto, 1 segundo)
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                ticksDuracion--; // Disminuye el contador de ticks restantes
            }
        }
        if (ticksDuracion == 0) {
            System.out.println("[" + reloj.formatearHora(reloj.getHoraActual()) + "] El paciente " + this.nombre +
                               " finalizó su consulta.");
        }
    }

    public void interrumpirConsulta() {
        interrumpido = true;
    }

    // Bucle principal
    @SuppressWarnings("static-access")
    public void run() {
        // Avisa que llegó al centro médico
        System.out.println("[" + RelojSimulado.formatearHora(this.horaLlegada) + "] Ingresó el paciente: " + this.nombre
                           + ", Tipo de consulta: " + this.tipoConsulta + ", Prioridad: " + this.prioridad
                           + ", Duración: " + this.ticksDuracion * RelojSimulado.getEquivalenciaTick() + " minutos.");

        while (ticksDuracion > 0 && this.vivo) {
            // Decide a qué cola de pacientes ir (general u odontología)
            if (this.getEspecialista().equals("Odontólogo")) {  // Cola odontología
                // Mientras no sea su turno (no sea el más prioritario, el valor más bajo), espera un tick
                while (prioridad > CentroMedico.getPrioridadMaxOdontologia()) {
                    try {
                        Thread.sleep(RelojSimulado.getFrecuenciaTick()); // Duerme durante un tick (Por defecto, 1 segundo)
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                // Si cumple los requisitos para ser atendido, intenta ser atendido
                try {
                    CentroMedico.mutexAtencionOdontologia.acquire();
                // Cuando sí es su turno con el odontólogo, pasa a ser atendido
                if (prioridad <= CentroMedico.getPrioridadMaxOdontologia()) {
                    try {
                        CentroMedico.odontologo.acquire();
                        CentroMedico.mutexAtencionOdontologia.release(); // Libera mutex apenas ya tomó el turno
                        serAtendido();
                        CentroMedico.odontologo.release();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }else {
                        CentroMedico.mutexAtencionOdontologia.release(); // No le toca, libera el mutex
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } 
            else {  // Cola general
                // Mientras no sea su turno (no sea el más prioritario, el valor más bajo), espera un tick
                while (prioridad > CentroMedico.getPrioridadMaxActual()) {
                    try {
                        Thread.sleep(RelojSimulado.getFrecuenciaTick()); // Duerme durante un tick (Por defecto, 1 segundo)
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                // Si cumple los requisitos para ser atendido, intenta ser atendido
                try {
                    CentroMedico.mutexAtencionGeneral.acquire();

                    if (this.getPrioridad() <= CentroMedico.getPrioridadMaxActual()) {
                        switch (this.getEspecialista()) {
                            case "Médico y Enfermero":
                                CentroMedico.medico.acquire(); 
                                CentroMedico.enfermero.acquire();
                                CentroMedico.añadirPacienteAtendiendo(this);
                                CentroMedico.mutexAtencionGeneral.release(); // Libera mutex apenas ya tomó el turno

                                serAtendido();

                                CentroMedico.eliminarPacienteAtendiendo(this);
                                CentroMedico.medico.release();
                                CentroMedico.enfermero.release();
                                break;

                            case "Enfermero":
                                CentroMedico.enfermero.acquire();
                                CentroMedico.añadirPacienteAtendiendo(this);
                                CentroMedico.mutexAtencionGeneral.release(); // Libera mutex apenas ya tomó el turno

                                serAtendido();

                                CentroMedico.eliminarPacienteAtendiendo(this);
                                CentroMedico.enfermero.release();
                                break;

                            case "Desconocido":
                                System.out.println("[" + reloj.formatearHora(reloj.getHoraActual()) + "] El paciente " + this.nombre +
                                        " no tiene un especialista asignado, no puede ser atendido.");
                                CentroMedico.mutexAtencionGeneral.release();
                                return;
                        }
                    } else {
                        CentroMedico.mutexAtencionGeneral.release(); // No le toca, libera el mutex
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}

