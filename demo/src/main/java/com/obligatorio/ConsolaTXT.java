package com.obligatorio;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class ConsolaTXT {
    private static BufferedWriter writer;
    private static boolean mostrarEnConsola = true;

    public static void iniciar(String nombreArchivo, boolean mostrarMensajesConsola) {
        mostrarEnConsola = mostrarMensajesConsola;
        try {
            writer = new BufferedWriter(new FileWriter(nombreArchivo, false));
        } catch (IOException e) {
            System.out.println("Error al iniciar el logger: " + e.getMessage());
        }
    }

    public static void imprimirYguardar(String mensaje) {
        try {
            if (writer != null) {
                writer.write(mensaje);
                writer.newLine();
                writer.flush();
            }

            if (mostrarEnConsola) {
                System.out.println(mensaje);
            }
        } catch (Exception e) {
        }
    }

    public static void cerrar() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (Exception e) {
            System.out.println("Error al terminar el log: " + e.getMessage());
        }
    }
}
