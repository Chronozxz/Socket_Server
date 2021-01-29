import java.net.*;
import java.io.*;

public class Temporizador extends Thread{//temporizador del servidor

    long tiempo;
    boolean timeout;//si se acabó el tiempo 
    boolean llegoMensaje;
    DataOutputStream out;
    Socket cliente;

    public Temporizador(Socket cliente, long tiempo)
    {//solicita el socket y el tiempo
    	this.cliente = cliente;
    	try{
			out = new DataOutputStream(cliente.getOutputStream());
		}catch (Exception e){
            System.out.println("Error: " + e.getMessage());
        }
        this.tiempo = tiempo;
        timeout=false;
        llegoMensaje = false;
    }

    public void run(){
        try {
    	    sleep(tiempo);//se espera el tiempo establecido
            timeout=true;//tiempo terminado
            if( ! llegoMensaje)
            {//ve si llego mensaje en el server
            	out.write(1);
            	out.flush();
            }
        }catch (Exception e){
            System.out.println("Error: " + e.getMessage());
        }
    }
}
