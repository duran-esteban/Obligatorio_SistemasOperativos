package com.obligatorio;

class Paciente implements Runnable, Comparable<Paciente> {
    private final String nombre;
    private final int horaLlegada;
    private final String tipoConsulta;
    private int prioridad;
    private int prioridadOriginal;
    private int ticksDuracion;
    private boolean atendido = false;

    // Constructor
    public Paciente(String nombre, int horaLlegada, String tipoConsulta, int prioridad, int duracion) {
        this.nombre = nombre;
        this.horaLlegada = horaLlegada;
        this.tipoConsulta = tipoConsulta;
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

    public String getEspecialista() {
        String consultaMinuscula = this.tipoConsulta.toLowerCase();

        if (consultaMinuscula.contains("emergencia") 
            || consultaMinuscula.contains("urgencia")
            || consultaMinuscula.contains("carnet de salud")) {
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

    public void atender() {
        atendido = true;
        while (ticksDuracion > 0) {
            try {
                Thread.sleep(RelojSimulado.getFrecuenciaTick()); // Duerme durante un tick (Por defecto, 1 segundo)
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            ticksDuracion--;
        }
        // se va del centro
    }

    public void recalcularPrioridad(int tiempoActual) {
        int tiempoEsperando = tiempoActual - horaLlegada;

        if (prioridadOriginal == 0) { // Emergencia
            prioridad = 0;
        } else if (prioridadOriginal <= 5 && prioridad != 0) {
            prioridad = 1; // prioridad alta
        } else if (prioridadOriginal <= 10 && prioridad != 5) {
            prioridad = 2; // prioridad media
        }
        if ((tiempoActual - horaLlegada) >= 2 * 12) { // 2 horas
            prioridad = 0; // máxima prioridad
        }
    }

    // Bucle principal
    public void run() {
        // Avisa que llegó al centro médico
        System.out.println("[" + RelojSimulado.formatearHora(this.horaLlegada) + "] Ingresó el paciente: " + this.nombre
                           + ", Tipo de consulta: " + this.tipoConsulta + ", Prioridad: " + this.prioridad
                           + ", Duración: " + this.ticksDuracion * RelojSimulado.getEquivalenciaTick() + " minutos.");

        while (ticksDuracion > 0) {
            while (prioridad < CentroMedico.getPrioridadMaxActual()) {
            // Mientras no sea su turno (no tenga la máxima prioridad), espera un tick
                try {
                    Thread.sleep(RelojSimulado.getFrecuenciaTick()); // Duerme durante un tick (Por defecto, 1 segundo)
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            // Si es su turno, atiende
            if (prioridad >= CentroMedico.getPrioridadMaxActual()) {
                atender();
            } 
        }
    }
    
    public void imprimir() {
        System.out.println("Hora de llegada: " + horaLlegada + ", Paciente: " + nombre + ", Tipo de consulta: " + tipoConsulta +
                           ", Prioridad: " + prioridad + ", Duración: " + ticksDuracion + ", Atendido: " + atendido);
    }
}

