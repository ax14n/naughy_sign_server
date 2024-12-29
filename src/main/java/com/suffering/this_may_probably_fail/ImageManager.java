/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.suffering.this_may_probably_fail;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase encargada de ejecutar un hilo que maneja el almacenamiento de imagenes.
 *
 * @author me
 */
public class ImageManager implements Runnable {

    /**
     * Puerto al que se deberán contectar los clientes para subir sus imagenes.
     */
    private static final int PORT = 23457;

    /**
     * Encargado de registrar logs con distintos niveles.
     */
    private static final Logger LOGGER = Logger.getLogger(ImageManager.class.getName());

    /**
     * Socket abierto del servidor que permite la conexión con los clientes.
     */
    private static ServerSocket serverSocket;

    static {
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error al iniciar el servidor", ex);
        }
    }

    /**
     * Nombre que tendrá el hilo para ser diferenciado de otros.
     */
    private final String threadName;

    /**
     * Constructor del hilo.
     *
     * @param threadName Nombre del hilo.
     */
    public ImageManager(String threadName) {
        Objects.requireNonNull(threadName, "El nombre del hilo no puede ser nulo.");
        this.threadName = threadName;
    }

    /**
     * Ejecución que seguirá el hilo.
     */
    @Override
    public void run() {
        System.out.println("Esperando conexión por parte del cliente");
        try (Socket socket = serverSocket.accept()) {
            System.out.println("Conexión aceptada. Procesando...");

            // Se crea un enlace con el socket para leer su flujo de datos y
            // así traducir los datos envíados por parte del cliente.
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());

            switch (inputStream.readUTF()) {
                case "write":
                    storagePhoto(inputStream, socket);
                    System.out.println("write");
                    break;
                case "read":
                    chargePhotos(inputStream, socket);
                    System.out.println("read");
                    break;
                default:
                    throw new AssertionError();
            }
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
    }

    /**
     * Almacena las fotos enviadas por el cliente en su respectiva carpeta de
     * usuario de forma local. Las imagenes son enviadas en forma bytes y
     * formadas en la parte del servidor.
     *
     * @param inputStream InputStream extraido del puerto con el que se ha hecho
     * conexión.
     */
    private void storagePhoto(DataInputStream inputStream, Socket socket) {
        try {
            // -------- { Identifica el usuario y prepara su entorno. } -------- //
            String userUUID = inputStream.readUTF();
            System.out.printf("Gestionando usuario: %s%n", userUUID);
            createPersonalDirectory(userUUID);

            while (true) {
                // -------- { Lectura del nombre del fichero. } -------- //
                String fileName = inputStream.readUTF();

                // -------- { Verificación de final de transferencia } -------- //
                if ("EOF".equals(fileName)) {
                    System.out.println("Fin de transferencia recibido.");
                    break;
                }

                long fileSize = inputStream.readLong(); // Tamaño del fichero.

                // Crear archivo para almacenar los datos recibidos
                File imageFile = new File(String.format("%s/kotlin_data/%s/%s", System.getProperty("user.home"), userUUID, fileName));
                try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                    byte[] buffer = new byte[8192];
                    long remaining = fileSize;

                    // -------- { Mientras tenga contenido, sigue escribiendo. } -------- //
                    while (remaining > 0) {
                        int bytesRead = inputStream.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                        if (bytesRead == -1) {
                            throw new IOException("Fin inesperado del flujo de datos.");
                        }
                        fos.write(buffer, 0, bytesRead);
                        remaining -= bytesRead;
                    }
                }
                System.out.printf("Archivo guardado en: %s%n", imageFile.getAbsolutePath());
            }
        } catch (IOException ex) {
            Logger.getLogger(ImageManager.class.getName()).log(Level.SEVERE, "Error durante la conexión", ex);
        }
    }

    /**
     * Extrae las imagenes almacenadas por parte de un usuario dentro del
     * servidor y las vuelve a enviar al cliente en forma de flujo de bytes.
     *
     * @param inputStream InputStream extraido del puerto con el que se ha hecho
     * conexión.
     */
    private void chargePhotos(DataInputStream inputStream, Socket socket) {
        try {

            // Se crea un enlace con el socket para escribir datos hacia el cliente
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

            // -------- { Identifica el usuario y prepara su entorno. } -------- //
            String userUUID = inputStream.readUTF();
            System.out.printf("Gestionando usuario: %s%n", userUUID);

            File directory = new File(String.format("%s/kotlin_data/%s/", System.getProperty("user.home"), userUUID));
            String[] images = directory.list();

            for (int image = 0; image < images.length; image++) {

                // -------- { Recepción del nombre del fichero solicitado. } -------- //
                String fileName = images[image];

                // Crear referencia al archivo solicitado
                File imageFile = new File(String.format("%s/kotlin_data/%s/%s", System.getProperty("user.home"), userUUID, fileName));

                // -------- { Enviar el archivo } -------- //
                try (FileInputStream fis = new FileInputStream(imageFile)) {
                    dataOutputStream.writeUTF(fileName); // Nombre del archivo
                    dataOutputStream.writeLong(imageFile.length()); // Tamaño del archivo

                    byte[] buffer = new byte[8192];
                    int bytesRead;

                    // -------- { Mientras haya contenido, envíalo. } -------- //
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        dataOutputStream.write(buffer, 0, bytesRead);
                    }
                    System.out.printf("Archivo enviado: %s%n", imageFile.getAbsolutePath());
                }
            }
            dataOutputStream.writeUTF("EOF"); // Notifica al cliente que ha terminado.
        } catch (IOException ex) {
            Logger.getLogger(ImageManager.class.getName()).log(Level.SEVERE, "Error durante la transferencia", ex);
        }
    }

    /**
     * Crea el directorio personal del usuario si no existe.
     */
    private void createPersonalDirectory(String userUUID) {
        File userDir = new File("%s/kotlin_data/%s".formatted(System.getProperty("user.home"), userUUID));
        if (!userDir.exists() && !userDir.mkdirs()) {
            LOGGER.warning("No se pudo crear el directorio: " + userDir.getAbsolutePath());
        }
    }
}
