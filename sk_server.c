// Server side C/C++ program to demonstrate Socket programming 
#include <unistd.h> 
#include <stdio.h> 
#include <sys/socket.h> 
#include <stdlib.h> 
#include <netinet/in.h> 
#include <string.h>
#include <stdint.h>
#include <time.h>
#define PORT 8080 
#define DATA_LEN 1024

struct msg
{
	uint8_t numSeq;
	int CRC8;
	char data[DATA_LEN];
	uint16_t length;
	uint8_t tipo;
};

int getSocket()
{// Creating socket file descriptor 
	int server_fd;
    if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) == 0) 
    { 
        perror("socket failed"); 
        exit(EXIT_FAILURE); 
    } 
    return server_fd;
}

int configSocket(int server_fd, int opt)
{// Forcefully attaching socket to the port 8080 
    if (setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR |SO_REUSEPORT, &opt, sizeof(opt)) ) 
    { 
        perror("setsockopt"); 
        exit(EXIT_FAILURE); 
    } 
    return opt;
}

struct sockaddr_in configAddress()
{
	struct sockaddr_in address; 
	address.sin_family = AF_INET; 
    address.sin_addr.s_addr = INADDR_ANY; 
    address.sin_port = htons( PORT ); 
    return address;
}

void setPort(int server_fd, struct sockaddr_in address)
{// Forcefully attaching socket to the port 8080 
    if (bind(server_fd, (struct sockaddr *)&address, sizeof(address))<0) 
    { 
        perror("bind failed"); 
        exit(EXIT_FAILURE); 
    } 
}

void setListen(int server_fd)
{
	if (listen(server_fd, 3) < 0) 
    { 
        perror("listen"); 
        exit(EXIT_FAILURE); 
    }
}

float getError()
{//Error aletorio
	return ((rand() % 10000) / 100.0f);//máximo 99.99%
}

float getTemporizador()
{//Temporizador aletorio
	return ((rand() % 6000) / 100.0f);//maximo 60.00seg o sea 1min
}

float getValor(char *str, char *e, char *t)
{
	float valor;
	if (strcmp(str, "-e") == 0)
	{
		valor =  atof( e );
	}else if(strcmp(str, "-p") == 0)
	{
		valor =  atof(t);
	}else{
		valor = 0.0;
	}
	return valor;
}

int main(int argc, char const *argv[]) 
{ 
	struct sockaddr_in address;
    int server_fd, new_socket, valread, opt = 1; 
    int addrlen = sizeof(address);
    float error_max = 0.0, nuevo_error, temz_max = 0.0, nuevo_temz;
    unsigned long long numPaquetes;
    char buffer[DATA_LEN]={0}; 
    char dir[100]={0}, Ctrue[1];
    char *err = strdup(argv[1]), *porErr= strdup(argv[2]);
    char *temz = strdup(argv[3]), *porTemz = strdup(argv[4]);
    char buf[DATA_LEN]={0};
  	FILE *archivo;

	error_max = getValor(err, porErr, porTemz);
	temz_max = getValor(temz, porErr, porTemz);
  	
	server_fd = getSocket();
	opt = configSocket(server_fd, opt);
	address = configAddress();
	setPort(server_fd, address);
	setListen(server_fd);
    
	printf("Servidor funcionando \n");
	
	
    if ((new_socket = accept(server_fd, (struct sockaddr*)&address, (socklen_t*)&addrlen))<0) 
    { 
        perror("accept"); 
        exit(EXIT_FAILURE); 
    } 
    
    //leer el nombre del archivo
    read( new_socket , dir, 100);
    archivo = fopen (dir, "rb");
    
    
	if (archivo == NULL)
    {
        printf("Error de apertura del archivo o no existe.\n");
        //enviar ctrue = '0' si no existe
        Ctrue[0] = '0';
        send( new_socket, Ctrue, 1, 0);
        return 0;
    }
	else{
		//enviar Ctrue = '1' si existe
		Ctrue[0] = '1';
		send( new_socket, Ctrue, 1, 0);
		
		//generar la semilla de random para error 
		srand(time(NULL));

		//ir al principio del archivo
		fseek(archivo, 0, SEEK_SET);

		//mientras haya datos del archivo
		while((valread= fread (buf, 1, DATA_LEN, archivo)) != EOF && !(valread == 0))
		{//enviando los datos de valread
			nuevo_error = getError();
			nuevo_temz = getTemporizador();
			printf("Error %f --",nuevo_error);
			printf("Temp %f \n",nuevo_temz);
			
			if(nuevo_error <= error_max){
				//cambiar mensaje
			}
			if(nuevo_temz <= temz_max){
				sleep(nuevo_temz);
			}	
			send(new_socket, buf, valread, 0 ); 
		}
		
		//avisar que ya no hay datos para enviar
		char end[1] = "";
		send(new_socket, end, 1, 0 );
		
		printf("Archivo enviado \n");
	    fclose (archivo);
	}
    
    //valread = read( new_socket , buffer, 1024);  
    //send(new_socket , hello , strlen(hello) , 0 ); 
    return 0; 
} 
