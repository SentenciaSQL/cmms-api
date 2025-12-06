package com.afriasdev.cmms.service;

import com.afriasdev.cmms.dto.response.FileUploadResponse;
import com.afriasdev.cmms.exception.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${file.upload-dir:./uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("No se pudo crear el directorio de almacenamiento", ex);
        }
    }

    /**
     * Almacena un archivo en el sistema de archivos
     */
    public FileUploadResponse storeFile(MultipartFile file) {
        // Validar archivo
        if (file.isEmpty()) {
            throw new FileStorageException("El archivo está vacío");
        }

        // Validar tamaño (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new FileStorageException("El archivo excede el tamaño máximo permitido de 10MB");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Validar nombre de archivo
            if (originalFileName.contains("..")) {
                throw new FileStorageException("El nombre del archivo contiene una secuencia de ruta no válida: " + originalFileName);
            }

            // Generar nombre único para el archivo
            String fileExtension = getFileExtension(originalFileName);
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            // Copiar archivo al directorio de almacenamiento
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Construir URL del archivo
            String fileUrl = "/api/files/" + uniqueFileName;

            log.info("Archivo almacenado exitosamente: {}", uniqueFileName);

            return new FileUploadResponse(
                    uniqueFileName,
                    fileUrl,
                    file.getContentType(),
                    file.getSize()
            );

        } catch (IOException ex) {
            throw new FileStorageException("No se pudo almacenar el archivo " + originalFileName, ex);
        }
    }

    /**
     * Almacena un archivo en un subdirectorio específico (ej: work-orders, assets)
     */
    public FileUploadResponse storeFileInDirectory(MultipartFile file, String directory) {
        Path directoryPath = this.fileStorageLocation.resolve(directory);

        try {
            Files.createDirectories(directoryPath);
        } catch (IOException ex) {
            throw new FileStorageException("No se pudo crear el directorio: " + directory, ex);
        }

        if (file.isEmpty()) {
            throw new FileStorageException("El archivo está vacío");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            if (originalFileName.contains("..")) {
                throw new FileStorageException("El nombre del archivo contiene una secuencia de ruta no válida: " + originalFileName);
            }

            String fileExtension = getFileExtension(originalFileName);
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            Path targetLocation = directoryPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "/api/files/" + directory + "/" + uniqueFileName;

            log.info("Archivo almacenado exitosamente en {}: {}", directory, uniqueFileName);

            return new FileUploadResponse(
                    uniqueFileName,
                    fileUrl,
                    file.getContentType(),
                    file.getSize()
            );

        } catch (IOException ex) {
            throw new FileStorageException("No se pudo almacenar el archivo " + originalFileName, ex);
        }
    }

    /**
     * Carga un archivo como Resource
     */
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new FileStorageException("Archivo no encontrado: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException("Archivo no encontrado: " + fileName, ex);
        }
    }

    /**
     * Carga un archivo desde un subdirectorio
     */
    public Resource loadFileAsResourceFromDirectory(String directory, String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(directory).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new FileStorageException("Archivo no encontrado: " + directory + "/" + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException("Archivo no encontrado: " + directory + "/" + fileName, ex);
        }
    }

    /**
     * Elimina un archivo
     */
    public void deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
            log.info("Archivo eliminado: {}", fileName);
        } catch (IOException ex) {
            throw new FileStorageException("No se pudo eliminar el archivo: " + fileName, ex);
        }
    }

    /**
     * Elimina un archivo de un subdirectorio
     */
    public void deleteFileFromDirectory(String directory, String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(directory).resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
            log.info("Archivo eliminado de {}: {}", directory, fileName);
        } catch (IOException ex) {
            throw new FileStorageException("No se pudo eliminar el archivo: " + directory + "/" + fileName, ex);
        }
    }

    /**
     * Valida el tipo de archivo
     */
    public boolean isValidFileType(MultipartFile file, String... allowedTypes) {
        String contentType = file.getContentType();

        if (contentType == null) {
            return false;
        }

        for (String allowedType : allowedTypes) {
            if (contentType.contains(allowedType)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Valida si es una imagen
     */
    public boolean isImageFile(MultipartFile file) {
        return isValidFileType(file, "image/jpeg", "image/png", "image/gif", "image/jpg");
    }

    /**
     * Valida si es un documento
     */
    public boolean isDocumentFile(MultipartFile file) {
        return isValidFileType(file, "application/pdf", "application/msword",
                "application/vnd.openxmlformats-officedocument", "text/plain");
    }

    /**
     * Valida si es un audio
     */
    public boolean isAudioFile(MultipartFile file) {
        return isValidFileType(file, "audio/mpeg", "audio/mp3", "audio/wav", "audio/ogg");
    }

    /**
     * Obtiene la extensión del archivo
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }

        int dotIndex = fileName.lastIndexOf('.');

        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex);
        }

        return "";
    }
}
