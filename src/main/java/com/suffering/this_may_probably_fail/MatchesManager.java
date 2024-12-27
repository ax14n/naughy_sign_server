/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.suffering.this_may_probably_fail;

// ----- { Importaciones de paquete Java. } ----- //
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author me
 */
public class MatchesManager implements Runnable {

    /**
     * Puerto que será usado por el hilo.
     */
    private static final int PORT = 23458;

    /**
     * Encargado de registrar logs con distintos niveles.
     */
    private static final Logger LOGGER = Logger.getLogger(MatchesManager.class.getName());

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

    public MatchesManager(String threadName) {
        Objects.requireNonNull(threadName, "El nombre del hilo no puede ser nulo.");
        this.threadName = threadName;
    }

    @Override
    public void run() {
        System.out.println("Esperando conexión por parte del cliente");
        // -------- { Espera la conexión del usuario } -------- //
        try (Socket socket = serverSocket.accept()) {
            System.out.println("Conexión aceptada. Procesando...");
            // TODO: Rellenar mentalidad del hilo.
        } catch (IOException ex) {
            Logger.getLogger(MatchesManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
