package com.obligatorio;
import java.util.ArrayList;
import java.util.List;

import com.obligatorio.Utils.ManejadorArchivosGenerico;

class GestorPacientes implements Runnable {
    private final RelojSimulado reloj;
    private final String archivoSimulacion;
    private final List<Paciente> pacientes;

    public GestorPacientes(RelojSimulado reloj, String archivoSimulacion) {
        this.reloj = reloj;
        this.archivoSimulacion = archivoSimulacion;
        this.pacientes = new ArrayList<>();
        ProcesarPacientes();
        imprimirPacientes();
    }

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
                Paciente paciente = new Paciente(nombre, horaLlegada, tipoConsulta, prioridad, duracion);
                pacientes.add(paciente);
            }
            else { // Si hay alguna línea mal escrita, avisa por consola 
                System.out.println("Falló el formato de líneas, en la línea: " + renglonPaciente);
            }
        };
    }

    private void imprimirPacientes() {
        for (Paciente p : pacientes) {
            p.imprimir();;
        }
    }   

    @Override
    public void run() {
        /* 
        int tiempoAnterior = -1;
        while (!pacientes.isEmpty()) {
            int ahora = reloj.getTiempoActual();
            if (ahora != tiempoAnterior) {
                Iterator<Paciente> it = pacientes.iterator();
                while (it.hasNext()) {
                    Paciente p = it.next();
                    if (p.getHoraLlegada() == ahora) {
                        new Thread(p).start(); // crear el hilo del paciente
                        it.remove(); // ya fue creado
                    }
                }
                tiempoAnterior = ahora;
            }
        }
            */
    }
}

