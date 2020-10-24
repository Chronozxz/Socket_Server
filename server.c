// Server side C/C++ program to demonstrate Socket programming 
#include <unistd.h> 
#include <stdio.h> 
#include <sys/socket.h> 
#include <stdlib.h> 
#include <netinet/in.h> 
#include <string.h>
#define PORT 8080 

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

int main(int argc, char const *argv[]) 
{ 
    int server_fd, new_socket, valread;
    struct sockaddr_in address; 
    int opt = 1; 
    int addrlen = sizeof(address);
    unsigned long long numPaquetes;
    char buffer[1024]={0}; 
    char dir[100]={0}; 
    char Ctrue[1];
    char buf[1024]={0};
  	FILE *archivo;
  	
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

		//ir al principio del archivo
		fseek(archivo, 0, SEEK_SET);

		//mientras haya datos del archivo
		while((valread= fread (buf, 1, 1024, archivo)) != EOF && !(valread == 0))
		{//enviando los datos de valread
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
