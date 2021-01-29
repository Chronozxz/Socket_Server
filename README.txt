Programa servidor que hace una simulación de un servidor. 
con el programa parte 2 cliente se comunicarán los dos y así se pedirá un pdf de servidor que será enviado a cliente con una estructura.

Programa cliente versión java, envía paquetes con el protocolo retroceso N.

***Para compilar 
--Servidor
javac SocketSvRN.java

Para ejecutar
--Servidor
java SocketSvRN -e num1 -p num2
num1: número flotante, índice de error, se recomienda uno pequeño, menor a 5
num2: número entero debe ser menor al del cliente, por que si es mayor siempre habrá mensajes perdidos

***Notas: 
1.- Con un archivo de 6.9mb tarda como 10 min.
con 
java SocketSvRN -e 4 -p 70
