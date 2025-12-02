package it.autoflow.commons.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    /**
     * Cartella radice dove salvare i file.
     * Puoi configurarla in application.yml con:
     *
     * autoflow:
     *   storage:
     *     base-path: ./autoflow-storage
     */
    @Value("${autoflow.storage.base-path:./autoflow-storage}")
    private String basePath;

    @Override
    public String store(File file, String folderKey) {
        if (file == null) {
            throw new IllegalArgumentException("File nullo");
        }
        if (folderKey == null || folderKey.isBlank()) {
            throw new IllegalArgumentException("folderKey obbligatoria");
        }

        try {
            Path rootDir = Paths.get(basePath).toAbsolutePath().normalize();
            Path targetDir = rootDir.resolve(folderKey).normalize();

            // crea le directory se non esistono
            Files.createDirectories(targetDir);

            String originalName = file.getName();
            if (originalName == null || originalName.isBlank()) {
                originalName = "documento.pdf";
            }

            int dot = originalName.lastIndexOf('.');
            String baseName = (dot > 0 ? originalName.substring(0, dot) : originalName);
            String ext = (dot > 0 ? originalName.substring(dot) : "");

            Path targetFile = targetDir.resolve(originalName);
            int counter = 1;
            // se esiste già, aggiungo un suffisso -1, -2, ...
            while (Files.exists(targetFile)) {
                String newName = baseName + "-" + counter + ext;
                targetFile = targetDir.resolve(newName);
                counter++;
            }

            Files.copy(file.toPath(), targetFile, StandardCopyOption.REPLACE_EXISTING);

            return targetFile.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException("Errore nel salvataggio del file su disco", e);
        }
    }

    @Override
    public File load(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("path obbligatorio");
        }
        return Paths.get(path).toFile();
    }

    @Override
    public boolean delete(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        try {
            return Files.deleteIfExists(Paths.get(path));
        } catch (IOException e) {
            // Se fallisce la delete non butto giù l’app
            return false;
        }
    }

    /**
     * Metodo comodo in più (non obbligatorio nell'interfaccia):
     * salva direttamente un array di byte.
     */
    public String store(byte[] bytes, String fileName, String folderKey) {
        try {
            Path temp = Files.createTempFile("autoflow-", "-" + fileName);
            Files.write(temp, bytes);
            return store(temp.toFile(), folderKey);
        } catch (IOException e) {
            throw new RuntimeException("Errore nel salvataggio da byte[]", e);
        }
    }
}