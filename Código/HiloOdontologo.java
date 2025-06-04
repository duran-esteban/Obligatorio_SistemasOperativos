import java.util.concurrent.Semaphore;

public class HiloOdontologo extends Thread {
    Semaphore disponible;

    public HiloOdontologo(String identificador) {
        super(identificador);
        disponible = new Semaphore(1);
    }

    // IMPLEMENTACIONES PROVISORIAS, BURDAS, DEL COMPORTAMIENTO GENERAL
    public boolean atenderPaciente(Thread paciente) {
        if (/* Condición que tenga que cumplirse para atender a un paciente*/ true) {
            try {
                disponible.acquire();   // Equivalente a wait()
                System.out.println("[Odontólogo " +this.getId()+ "]:" + "Atendiendo paciente: " + paciente.getName());
                Thread.sleep(/* Tiempo que dure la consulta */ 3000);
                System.out.println("[Odontólogo " +this.getId()+ "]:" + "Consulta finalizada");
            }
            catch (InterruptedException e) {
                System.out.println("Error por interrupción");
            }
            finally {
                disponible.release();   // Equivalente a signal()
            }
            return true;
        }
        // Si no se cumple la condición para atender al paciente
        System.out.println("[Odontólogo " +this.getId()+ "]:" + "No se pudo atender al paciente: " + paciente.getName());
        return false;
    }

    public void finDeTurno(int escalaReloj, int horasDescanso) {
        // Si no está atendiendo a alguien (está disponible),
        // Procede a tomarse el palo y dormir por el resto del día
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

