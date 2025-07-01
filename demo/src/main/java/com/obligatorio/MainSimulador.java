package com.obligatorio;

public class MainSimulador {

    // Implementación ejecutable
    public static void main(String[] args) {
        System.out.println("Planificador iniciado.");

        // Cada 2 segundos avanza 5 minutos virtuales (enlentecido a proposito para visualizar mejor con los pocos ejemplos)
        RelojSimulado reloj = new RelojSimulado(500,5);
        CentroMedico centroMedico = new CentroMedico(1, 1, 0, reloj);
        GestorPacientes gestor = new GestorPacientes(reloj, 
<<<<<<< HEAD
        "demo\\src\\main\\java\\com\\obligatorio\\Utils\\SimulacionEscenario2.txt", centroMedico);
=======
        "demo\\src\\main\\java\\com\\obligatorio\\Utils\\PacientesSimulación1.txt", centroMedico);
        ConsolaTXT.iniciar("demo\\src\\main\\java\\com\\obligatorio\\Utils\\registro_simulacion2.txt", true);
>>>>>>> 1fa4dc210ae920338075cb81f42cf939414244c5

        centroMedico.start();
        gestor.start(); // Inicia el hilo del gestor de pacientes
        reloj.start(); // Inicia el hilo del reloj simulado 
    }
}
