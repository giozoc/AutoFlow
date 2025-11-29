package it.autoflow.commons.service;

import java.io.File;
import java.io.IOException;

/**
 * Servizio per la gestione dello storage dei file nel file system locale.
 * Implementa le operazioni descritte in FileStorageService nell'ODD.
 */
public interface FileStorageService {

    /**
     * Salva un file nella directory logica identificata da folderKey
     * (es. "2025/11/Fatture") e restituisce il path logico salvato
     * che verrà memorizzato nell'entità DocumentoPDF.path.
     *
     * @param file      file da salvare
     * @param folderKey chiave/cartella logica (es. "2025/11/Fatture")
     * @return percorso completo del file salvato
     * @throws IOException in caso di errore di scrittura
     */
    String store(File file, String folderKey) throws IOException;

    /**
     * Carica un file a partire dal path salvato.
     *
     * @param path percorso del file
     * @return istanza File corrispondente
     */
    File load(String path);

    /**
     * Elimina un file a partire dal path.
     *
     * @param path percorso del file
     * @return true se il file è stato eliminato, false altrimenti
     */
    boolean delete(String path);
}