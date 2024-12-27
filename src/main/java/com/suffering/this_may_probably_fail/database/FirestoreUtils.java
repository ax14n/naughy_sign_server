/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.suffering.this_may_probably_fail.database;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author me
 */
public class FirestoreUtils {

    /**
     * API de la base de datos.
     */
    private Firestore db;

    /**
     * Constructor de la clase de utilidades de Firestore.
     *
     * @throws IOException En caso de no encontrar google.services.json en la
     * carpeta resources del proyecto.
     */
    public FirestoreUtils() throws IOException {

        // ------- { Obtención de la private key desde resources. } ------- // 
        InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream("google-services.json");
        if (serviceAccount == null) {
            throw new IOException("No se pudo encontrar el archivo google-services.json");
        }

        // ------- { Formación de la conexión con la BBDD. } ------- // 
        FirebaseOptions builder = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
        FirebaseApp.initializeApp(builder);

        // ------- {Se devuelve una referencia viva de la BBDD. } ------- // 
        db = FirestoreClient.getFirestore();
    }

    /**
     * Añade un nuevo documento a la base de datos.
     *
     * @param collectionName Nombre de la colección donde se agregará el
     * documento.
     * @param documentId Identificador que se le dará al documento.
     * @param data KV con el identificador y sus datos correspondientes.
     * @throws ExecutionException Debido a fallo al ejecutar.
     * @throws InterruptedException En caso de interrupción durante el proceso.
     */
    public void addDocument(String collectionName, String documentId, Map<String, Object> data) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection(collectionName).document(documentId);
        WriteResult result = docRef.set(data).get();  // Bloquea hasta que la operación termine
        System.out.println("Documento añadido con éxito en el tiempo: " + result.getUpdateTime());
    }

    /**
     * Obtiene un documento de la base de datos.
     *
     * @param collectionName Nombre de la colección.
     * @param documentId Identificador que se le dará al documento.
     * @throws ExecutionException Debido a fallo al ejecutar.
     * @throws InterruptedException En caso de interrupción durante el proceso.
     */
    public void getDocument(String collectionName, String documentId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection(collectionName).document(documentId);
        DocumentSnapshot document = docRef.get().get();  // Bloquea hasta obtener el documento
        if (document.exists()) {
            System.out.println("Documento obtenido: " + document.getData());
        } else {
            System.out.println("El documento no existe");
        }
    }

    /**
     * Obtiene todos los documentos de una colección especificada.
     *
     * @param collectionName Nombre de la colección.
     * @throws ExecutionException Debido a fallo al ejecutar.
     * @throws InterruptedException En caso de interrupción durante el proceso.
     */
    public void getAllDocuments(String collectionName) throws ExecutionException, InterruptedException {
        CollectionReference collectionRef = db.collection(collectionName);
        QuerySnapshot querySnapshot = collectionRef.get().get();  // Bloquea hasta obtener todos los documentos
        querySnapshot.getDocuments().forEach(document -> {
            System.out.println("Documento: " + document.getData());
        });
    }

    /**
     * Actualiza un documento.
     *
     * @param collectionName Nombre de la colección.
     * @param documentId Identificador que se le dará al documento.
     * @param updates KV con identificadores y los valores por los que se
     * sustituirán.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void updateDocument(String collectionName, String documentId, Map<String, Object> updates) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection(collectionName).document(documentId);
        WriteResult result = docRef.update(updates).get();  // Bloquea hasta que se complete la actualización
        System.out.println("Documento actualizado con éxito en el tiempo: " + result.getUpdateTime());
    }

    /**
     * Elimina un documento de la base de datos.
     *
     * @param collectionName Nombre de la colección a eliminar.
     * @param documentId Identificador del documento.
     * @throws ExecutionException Debido a fallo al ejecutar.
     * @throws InterruptedException En caso de interrupción durante el proceso.
     */
    public void deleteDocument(String collectionName, String documentId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection(collectionName).document(documentId);
        docRef.delete().get();  // Bloquea hasta que se elimine el documento
        System.out.println("Documento eliminado con éxito");
    }

}
