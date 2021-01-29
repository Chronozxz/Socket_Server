import java.io.*;
import java.net.*;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.zip.Checksum;
import java.util.zip.CRC32;

public class SocketSvRN {
     
    public static void main(String[] arg) throws IOException, ClassNotFoundException 
    {
        BufferedReader in;
        DataOutputStream out;    
        int numeroPuerto = 6161;// Puerto
        String ficheroOriginal = "";
        float ErrorMax = Float.parseFloat( arg[1] );//error maximo
        float nuevoError;
        long CronoMax = Long.parseLong( arg[3] );//tiempo maximo
        
        ServerSocket servidor = new ServerSocket(numeroPuerto);//conecta con el cliente
        System.out.println("Servidor en linea.....");
        Socket cliente = servidor.accept();//espera la conexión del cliente
        //Obtener nombre del archivo
        in = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
        out = new DataOutputStream(cliente.getOutputStream());
        FileInputStream fileInput;
        try{
            char c;
            int tam = in.read();
            for(int index = 0; index < tam; index++)
            {
                // read character
                c = (char) in.read();
                // print
                ficheroOriginal += c;//va guardando char por char del nombre del archivo solicitado
            }
            System.out.println("Archivo solicitado: " + ficheroOriginal);//imprime el archivo solicitado
            fileInput = new FileInputStream(ficheroOriginal);//Abre el archivo si existe
            out.write(1);//existe el fichero
            out.flush();
        }catch (IOException e){//si no existe el archivo o hay un error cierra el programa
            System.out.println("Error: " + e.getMessage());
            out.write(0);//no existe el archivo 
            out.flush();
            return;
        }
        
        try{
            // Se prepara un flujo de salida para objetos
            ObjectOutputStream outObjeto = new ObjectOutputStream( cliente.getOutputStream());
            // Se abre el fichero original para lectura
            BufferedInputStream bufferedInput = new BufferedInputStream(fileInput);//Para leer en el archivo
            
            //Inicializa variables
            Mensaje msj[] = new Mensaje[8];
            Checksum checksum = new CRC32();
            byte seq = -128;//maximo 127
            byte [][] dataAux = new byte[8][1024];//Se crea una data aux para guardar la información hasta que se envien correctamente
            short [] lengthAux = new short[8];	  //todos los datos
            int enviando = 1;
            int paqueteCompleto = 0;
            Random randGen = new Random();
            int numRand;
            int aceptaPaquete = -1;
            int conf = -1;
            Temporizador temz = new Temporizador(cliente, 0);//inicializa el temporizador
            ArrayList<Integer> rand = new ArrayList<Integer>();//lista para guardar numeros de los mensajes enviados aletoriamente 
            													//y no se repitan
            
            in = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
            out = new DataOutputStream(cliente.getOutputStream());
            
            System.out.println("-----------Enviando-----------");//enviando paquetes al cliente
            while(enviando == 1)
            {
	        	if(paqueteCompleto != 2)
	        	{//si aún no se han enviado todos los mensajes, inicia el temporizador
			    	temz = new Temporizador(cliente, obtenTemporizador(CronoMax));//temporizador
					aceptaPaquete = -1;
					in.read();	
					out.write(1);
					out.flush();
					temz.start();
					
					conf = in.read();
					if(temz.timeout == false && conf != 1)
					{//ve si aún no ha acabado el temporizador
						temz.llegoMensaje = true;
						out.write(0);
						out.flush();
					}
					out.write(2);
					out.flush();
					aceptaPaquete = in.read();//espera la ultima confirmación
				}else{
					paqueteCompleto = 2;
				}
	            switch(paqueteCompleto)
	            {
	                case 0:
	                    System.out.println("-----Nuevo paquete-----");
	                    for(int inx = 0; inx < 8; inx ++)
	                    {
	                        msj[inx] = new Mensaje();
	                        msj[inx].numSeq = (byte) (seq + (byte) inx);
	                        lengthAux[inx] = (short) bufferedInput.read(dataAux[inx]);//lee desde el archivo solicitado
	                        msj[inx].length = lengthAux[inx];
	                        msj[inx].data = dataAux[inx];
	                    }

	                    rand.clear();
						if(aceptaPaquete == 1)
			                for(int inxr = 0; inxr < 8; )//Enviando paquetes aletoriamente
			                {
			                    numRand = randGen.nextInt(8);//Genera el numero aletorio
			                    if(! rand.contains(numRand) )
			                    {
			                        rand.add(numRand);
			                        inxr++;
			                        if(msj[numRand].length > 0){//hay datos
			                            msj[numRand].tipo = 0;
			                            checksum.update(msj[numRand].data, 0, msj[numRand].length);//calcula checksum
			                            msj[numRand].CRC32 = checksum.getValue();
			                            if( ErrorMax >= obtenConfirmacion() )//si hay error CRC32 lo iguala a cero
			                                msj[numRand].CRC32 = 0;

				                        outObjeto.writeObject( (Mensaje) msj[numRand]);//envia el paquete
				                        outObjeto.writeObject( new String(msj[numRand].data, "ISO-8859-1"));//envia el string de la data
				                        outObjeto.flush();
			                        }else{//ya no hay datos, ultimos paquetes
			                            msj[numRand].tipo = 2;//tipo = 2, para terminar
				                        outObjeto.writeObject((Mensaje) msj[numRand]);//envia objeto nulo
				                        outObjeto.writeObject(new String(""));//string nulo, no hay data
				                        outObjeto.flush();
			                        }
			                    }
			                }
	                    seq+=8;
	                break;
	                case 1://reenvio
		                System.out.println("-----reenviando paquete-----");
	                    for(int inx = 0; inx < 8; inx ++)//Preparando paquetes para el reenvio
	                    {
	                        msj[inx] = new Mensaje();//instancia los mensajes
	                        msj[inx].tipo = 0;
	                        msj[inx].numSeq =(byte) (seq + (byte) inx - 8);//calcula su secuencia
	                        msj[inx].length = lengthAux[inx];//reasigna el tamaño anterior
	                        msj[inx].data = dataAux[inx];//vuelve a guardar la data
	                        if(msj[inx].length > 0)
	                        {//si hay datos
			                	checksum.update(msj[inx].data, 0, msj[inx].length);
			                    if( ErrorMax >= obtenConfirmacion() )//Generador de errores
			                    {
			                        msj[inx].CRC32 = 0;
			                    }else{
			                        msj[inx].CRC32 = checksum.getValue();
			                    }
		                    }else{//crc32 = 1, no 0 para que no genere error en el cliente
			                    msj[inx].CRC32 = 1;
		                    }
	                    }
	                    rand.clear();//limpia la lista de 0 a 8, para enviarlos aletoriamente
						if(aceptaPaquete == 1)
			                for(int inxr = 0; inxr < 8; )//Enviando paquetes aletoriamente
			                {
			                    numRand = randGen.nextInt(8);//Genera el numero aletorio
			                    if(! rand.contains(numRand) )
			                    {
			                        rand.add(numRand);//guarda el index para que no se repita
			                        inxr++;
				                    outObjeto.writeObject( (Mensaje) msj[numRand]);//envia el mensaje
				                    outObjeto.writeObject( new String(msj[numRand].data, "ISO-8859-1"));//envia el string de la data
				                    outObjeto.flush();
			                    }
			                }
	                break;
	                case 2:
	                    enviando = 0;//termino de enviar todos los paquetes
	                break;
	            }
	            if(enviando == 1 && paqueteCompleto != 2)//si hay paquetes y aún no ha acabado
		            paqueteCompleto = in.read();
            }
            
            System.out.println("Terminado");

            // CERRAR STREAMS Y SOCKETS, ficheros
            in.close();
            out.close();
            bufferedInput.close();
            outObjeto.close();
            cliente.close();   
            servidor.close();
        }catch (Exception e)
        {
            System.out.println("Error: " + e.getMessage());
        }
        in.close();
        out.close();
    }// Fin de main
    
    private static float obtenConfirmacion()
    {//obtiene el error de crc
        return ((float)Math.random()*10000/(float)100);
    }

    private static long obtenTemporizador(float temMax)
    {//obtiene el tiempo del temporizador
        return  (long)((float)(Math.random()* (int)(temMax*100.0f))/100.0f); 
    }
}
