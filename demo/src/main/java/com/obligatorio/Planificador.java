package com.obligatorio;

public class Planificador {
    // Semáforos, colas y otros atributos

    // Implementación ejecutable
    public static void main(String[] args) {
        System.out.println("Planificador iniciado.");
        GestorPacientes gestor = new GestorPacientes(new RelojSimulado(), 
        "demo\\src\\main\\java\\com\\obligatorio\\Utils\\PacientesSimulación1.txt");
    }
}
