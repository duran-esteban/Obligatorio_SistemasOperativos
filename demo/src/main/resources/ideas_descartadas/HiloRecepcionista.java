package com.obligatorio;

import java.util.concurrent.Semaphore;

public class HiloRecepcionista extends Thread {
    Semaphore disponible;

    public HiloRecepcionista(String identificador) {
        super(identificador);
        disponible = new Semaphore(1);
    }

    // IMPLEMENTACIONES PROVISORIAS, BURDAS, DEL COMPORTAMIENTO GENERAL
    public boolean registrarPaciente(Thread paciente) {
        if (/* Condición que tenga que cumplirse para registrar a un paciente*/ true) {
            try {
                disponible.acquire();   // Equivalente a wait()
                Thread.sleep(/* Tiempo que dure la consulta */ 500);
                System.out.println("[Recepcionista " +this.getId()+ "]:" + "Paciente registrado: " + paciente.getName());
            }
            catch (InterruptedException e) {
                System.out.println("Error por interrupción");
            }
            finally {
                disponible.release();   // Equivalente a signal()
            }
            return true;
        }
        // Si no se cumple la condición para registrar al paciente
        System.out.println("[Odontólogo " +this.getId()+ "]:" + "No se pudo atender al paciente: " + paciente.getName());
        return false;
    }

    public void finDeTurno(int escalaReloj, int horasDescanso) {
        // Si no está registrando a alguien (está disponible),
        // Termina su turno y duerme por el resto del día
        try {
            disponible.acquire();   // wait() a que esté disponible	
            Thread.sleep(escalaReloj * horasDescanso * 1000);
        } 
        catch (InterruptedException e) {
            System.out.println("Error por interrupción");
        } 
        finally {
            disponible.release();   // signal() para que pueda volver a atender pacientes
        }
    }

    public void run() {
       // Implementación
    } 
}
