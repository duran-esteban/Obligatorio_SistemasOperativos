package com.obligatorio;

public class MainSimulador {
    // Semáforos, colas y otros atributos

    // Implementación ejecutable
    public static void main(String[] args) {
        System.out.println("Planificador iniciado.");
        RelojSimulado reloj = new RelojSimulado(2000,5);
        GestorPacientes gestor = new GestorPacientes(reloj, 
        "demo\\src\\main\\java\\com\\obligatorio\\Utils\\PacientesSimulación1.txt");

        gestor.start(); // Inicia el hilo del gestor de pacientes
        reloj.start(); // Inicia el hilo del reloj simulado
    }
}
