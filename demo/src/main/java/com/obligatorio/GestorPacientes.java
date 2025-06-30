package com.obligatorio;
import java.util.ArrayList;
import java.util.List;

import com.obligatorio.Utils.ManejadorArchivosGenerico;

// Esta clase se encarga de procesar el archivo con los pacientes de
// la simulación e iniciar sus hilos cuando es su hora de llegada

class GestorPacientes extends Thread {
    private final RelojSimulado reloj;
    private final String archivoSimulacion;
    private final CentroMedico centroMedico;
    private final List<Paciente> pacientes;

    // Constructor
    public GestorPacientes(RelojSimulado reloj, String archivoSimulacion, CentroMedico centroMedico) {
        this.reloj = reloj;
        this.archivoSimulacion = archivoSimulacion;
        this.centroMedico = centroMedico;
        this.pacientes = new ArrayList<>();
        ProcesarPacientes();
    }

    // Métodos
    private void ProcesarPacientes() {
        // Lee el archivo de la simulación según la ruta indicada y trasnforma sus líneas en una colección de strings
        String[] lineas = ManejadorArchivosGenerico.leerArchivo(archivoSimulacion);

        // Procesa cada línea del archivo, separando los datos por el delimitador ';'
        for (String renglonPaciente : lineas) {
            String[] datos = renglonPaciente.split(";");
            if (datos.length == 5) {
                int horaLlegada = Integer.parseInt(datos[0]);
                String nombre = datos[1];
                String tipoConsulta = datos[2];
                int prioridad = Integer.parseInt(datos[3]);
                int duracion = Integer.parseInt(datos[4]);

                // Teniendo todos los datos, crea un nuevo objeto Paciente y lo agrega a la lista "pacientes"
                // Los archivos de simulación tienen a los pacientes ordenados por hora de llegada, 
                // por lo que la lista mantiene ese orden
                Paciente paciente = new Paciente(nombre, horaLlegada, tipoConsulta, prioridad, duracion, this.reloj);
                pacientes.add(paciente);
            }
            else { // Si hay alguna línea mal escrita, avisa por consola 
                ConsolaTXT.imprimirYguardar("Falló el formato de líneas, en la línea: " + renglonPaciente);
            }
        }
    }  

    // Bucle principal
    public void run() {
        while (!pacientes.isEmpty()) {
            // Espera a que el reloj avance un tick
            try {
                reloj.pasoTick.acquire();
            } catch (Exception e) {
                e.printStackTrace();
            } 

            // Revisa si hay pacientes que llegaron en este tick
            int tiempoActual = reloj.getHoraActual();
            List<Paciente> bufferPacientesAEliminar = new ArrayList<Paciente>(); // Para evitar ConcurrentModificationException
            for (Paciente p : pacientes) {
                if (p.getHoraLlegada() == tiempoActual) {
                    centroMedico.ingresar(p); // Ingresa el paciente al centro médico
                    bufferPacientesAEliminar.add(p); // Agrega el paciente al buffer para eliminarlo después
                }
            }

            // Elimina los pacientes que ya fueron creados de la lista "pacientes"
            for (Paciente p : bufferPacientesAEliminar) {
                pacientes.remove(p);
            }
            bufferPacientesAEliminar.clear(); // Limpia el buffer para la próxima iteración
        }
    }
}

