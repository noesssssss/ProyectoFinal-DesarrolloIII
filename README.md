1. Introducción
Tenemos una placa de programación Arduino UNO que manda datos serializados en formato XYZ. Se requiere desarrollar un sistema en Java que nos permita leer los datos y graficarlos en tiempo real. El programa debe tener una interfaz de usuario diseñada con los colores de la institución y varias vistas que nos permitan monitorear datos o ver datos históricos. También se requiere que exista conexión a un servidor, el cual conecte con la base de datos para mostrar datos históricos.
2. Solución
Con el uso de Java y ciertas librerias, podemos desarrollar un programa con un diseño agradable, que cumpla con los requisitos, y que sea completamente funcional.
El programa se realiza con ayuda de la libreria Swing y la clase JFrame. Con estas herramientas, nosotros podemos crear los botones, modificar sus acciones y cambiar entre vistas, asegurándonos de que todo se quede dentro de una misma ventana, sin necesidad de emergentes. Para la gráfica, utilizaremos la librería JFreeChart, ya que es la mas recomendada y nos permite actualizar la gráfica en cuanto se introducen datos nuevos.
En el caso de la lectura del Arduino, se utilizan listeners y la librería jSerialComm para obtener la comunicación de datos serializados. También se implementa un hilo de modo simulación para poder generar datos en caso de no contar con el dispositivo físico. Al momento que se inicia lectura en la vista "Monitor", los datos se procesa, grafican y se encriptan para ser enviados al servidor por medio de un socket.
El servidor, siendo un programa separado, es un programa de consola que muestra un mensaje con cada interacción del cliente. Varios clientes pueden ingresar a la aplicación de monitoreo sin problema gracias al uso de hilos. Para que el sistema de monitoreo funcione, debe estar iniciado el servidor. Si no lo está se mostrará un mensaje de error.
3. Implementación
* Servidor:
Para que nuestro programa lea los datos se tiene que conectar a un servidor. El servidor lo tenemos como un programa de consola que nos muestra mensajes para cada conexión, almacenamiento y consulta que pasa por medio de el.
El servidor conecta al cliente con el almacenamiento de datos. Al momento que se recibe un dato enviado por el cliente, llega encriptado al servidor, el programa lo desencripta y crea un nuevo registro en la base de datos con la información recibida.
El servidor recibe objetos de tipo mensaje, los cuales contienen el tipo de mensaje y los datos que se envian. Dependiendo del tipo de mensaje, los datos se consultan, agregan, o simplemente confirma que se realizó correctamente algún proceso.
<img src="https://github.com/noesssssss/ProyectoFinal-DesarrolloIII/blob/master/imgs_readme/servidor.png">
* Monitor
- Vista Principal
Es la pantalla que recibe al usuario. Esta pantalla contiene la opción de monitorear datos recibidos o ver datos históricos, guardados en una base de datos.
<img src="https://github.com/noesssssss/ProyectoFinal-DesarrolloIII/blob/master/imgs_readme/inicio.png">
- Monitoreo
Al dar click en “Monitoreo” en la pantalla principal, abre la vista de monitoreo. Aquí, el usuario puede ver datos graficándose en tiempo real. Dependiendo de su selección, los datos pueden ser generados por un hilo dentro del programa, o pueden ser obtenidos por una placa física conectada a un puerto en la computadora.
Para seleccionar los datos a graficar están los botones “Modo Simulación” o “Arduino Real”
- Monitoreo: Modo Simulación
Al dar click en “Modo Simulación”:
Se empieza a correr un hilo que genera datos de la misma forma que lo hace el arduino real
Se desactiva el boton de “Arduino Real” para evitar que el usuario intente obtener datos de dos lugares al mismo tiempo, sobrecargando el programa
El botón de modo simulación se cambia a “Detener Simulación”. Al detener la simulación los datos se quedan en la gráfica hasta que el sistema empiece a recolectar nuevos datos
<img src="https://github.com/noesssssss/ProyectoFinal-DesarrolloIII/blob/master/imgs_readme/simulacion.png">
- Monitoreo: Arduino Real
Al dar click en “Arduino real”, el programa abre un panel donde el usuario selecciona el puerto COM de donde se recibirán los datos. Si no existe un puerto disponible mostrará un mensaje que lo comunique.
El programa comienza a leer las líneas mandadas por el Arduino y y las agrega a la gráfica.
También desactiva el botón de simulación y cambia el botón de “Arduino Real” a un botón para detener la lectura.
<img src="https://github.com/noesssssss/ProyectoFinal-DesarrolloIII/blob/master/imgs_readme/real.png">
- Cómo funciona
El programa tiene una función que detecta cuando el Arduino manda información por el puerto. Cuando esto pasa, el programa lee los datos, los separa y maneja solo los números y los agrega al dataset que utiliza la gráfica para representar los datos. Por el lado de la simulación, en lugar de “escuchar” al Arduino, simplemente genera los números, grafica y manda al servidor.
- Almacenamiento de datos por lado del cliente
Al momento en que el programa recibe los datos, además de graficarlos, manda los datos como un objeto de tipo Mensaje. Antes de mandarlos, encripta la información con uso de AES y la clase Cipher de java. Se convierten a bytes para mandarse serializados y se envían al servidor.
- Almacenamiento de datos por lado del servidor
Al momento en que el servidor recibe los datos encriptados, los convierte a string, desencripta con el mismo sistema de AES, y por último los introduce a la BD. Cuando el usuario quiere consultar datos históricos, se realiza el mismo proceso de encriptación en el envío, solo que esta vez de servidor a monitoreo.
- Histórico
Cuando el usuario entre a ver datos históricos se mostrará una gráfica en blanco y seleccionadores de fecha y hora para la consulta. El usuario debe introducir el rango de fechas que desea consultar y dar click en el botón “consultar”. A continuación el programa mostrará un panel de mensaje con la fecha y hora de rango y los datos que se encontraron en esas fechas, y mostrará la información en la gráfica.
<img src="https://github.com/noesssssss/ProyectoFinal-DesarrolloIII/blob/master/imgs_readme/historico.png">
<img src="https://github.com/noesssssss/ProyectoFinal-DesarrolloIII/blob/master/imgs_readme/mensaje.png">

4. Conclusión
Por medio de los conocimentos obtenidos a lo largo de la clase, aprendimos a utilizar diferentes herramientas para el desarrollo de un programa completo, incluso con asignaciones no vistas anteriormente. Específicamente: aprendimos a manejar bases de datos SQLite y consultas SQL, a leer datos serializados en java con jSerialComm, conexiones a servidor con ayuda de la clase Socket, hilos y concurrencia con el uso de las clases Thread y Runnable, y la creación de interfaces gráficas con Swing. 
