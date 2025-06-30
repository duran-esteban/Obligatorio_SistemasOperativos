package com.obligatorio;

public class MainSimulador {

    // Implementación ejecutable
    public static void main(String[] args) {
        System.out.println("Planificador iniciado.");

        // Cada 2 segundos avanza 5 minutos virtuales (enlentecido a proposito para visualizar mejor con los pocos ejemplos)
        RelojSimulado reloj = new RelojSimulado(1000,5);
        CentroMedico centroMedico = new CentroMedico(1, 1, 0, reloj);
        GestorPacientes gestor = new GestorPacientes(reloj, 
        "demo\\src\\main\\java\\com\\obligatorio\\Utils\\PacientesSimulación1.txt", centroMedico);

        centroMedico.start();
        gestor.start(); // Inicia el hilo del gestor de pacientes
        reloj.start(); // Inicia el hilo del reloj simulado 
    }
}
