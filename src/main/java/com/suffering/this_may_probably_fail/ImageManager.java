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
        // -------- { Espera la conexión del usuario } -------- //
        try (Socket socket = serverSocket.accept()) {
            System.out.println("Conexión aceptada. Procesando...");

            // Se crea un enlace con el socket para leer su flujo de datos y
            // así traducir los datos envíados por parte del cliente.
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());

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
     * Crea el directorio personal del usuario si no existe.
     */
    private void createPersonalDirectory(String userUUID) {
        File userDir = new File("~/Documents" + "/kotlin_data/" + userUUID);
        if (!userDir.exists() && !userDir.mkdirs()) {
            LOGGER.warning("No se pudo crear el directorio: " + userDir.getAbsolutePath());
        }
    }

    /**
     * Guarda la foto en el directorio del usuario.
     *
     * @param userUUID UUID del usuario.
     * @param fileName Nombre del archivo.
     * @param fileSize Tamaño del archivo en bytes.
     * @param dataInputStream Flujo de entrada con los datos del archivo.
     * @throws IOException En caso de error.
     */
    private void storePhoto(String userUUID, String fileName, long fileSize, DataInputStream dataInputStream) throws IOException {
        File outputFile = new File(System.getProperty("user.home") + "/kotlin_data/" + userUUID + "/" + fileName);
        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[4096];
            long bytesReceived = 0;

            while (bytesReceived < fileSize) {
                int bytesToRead = (int) Math.min(buffer.length, fileSize - bytesReceived);
                int bytesRead = dataInputStream.read(buffer, 0, bytesToRead);

                if (bytesRead == -1) {
                    throw new IOException("Conexión interrumpida antes de completar la transferencia.");
                }

                fileOutputStream.write(buffer, 0, bytesRead);
                bytesReceived += bytesRead;
            }

            System.out.printf("Archivo guardado en: %s%n", outputFile.getAbsolutePath());
        }
    }

}
