package com.afriasdev.cmms.controller;

import com.afriasdev.cmms.dto.response.FileUploadResponse;
import com.afriasdev.cmms.exception.FileStorageException;
import com.afriasdev.cmms.service.FileStorageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Files Storage", description = "Gestión de Archivos y Almacenamiento")
public class FileController {

    private final FileStorageService fileStorageService;

    /**
     * Subir archivo general
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<FileUploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        FileUploadResponse response = fileStorageService.storeFile(file);
        return ResponseEntity.ok(response);
    }

    /**
     * Subir imagen (solo formatos de imagen)
     */
    @PostMapping("/upload/image")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<FileUploadResponse> uploadImage(@RequestParam("file") MultipartFile file) {
        if (!fileStorageService.isImageFile(file)) {
            throw new FileStorageException("El archivo debe ser una imagen (JPEG, PNG, GIF)");
        }

        FileUploadResponse response = fileStorageService.storeFileInDirectory(file, "images");
        return ResponseEntity.ok(response);
    }

    /**
     * Subir documento (PDF, Word, etc.)
     */
    @PostMapping("/upload/document")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<FileUploadResponse> uploadDocument(@RequestParam("file") MultipartFile file) {
        if (!fileStorageService.isDocumentFile(file)) {
            throw new FileStorageException("El archivo debe ser un documento (PDF, Word, Excel, TXT)");
        }

        FileUploadResponse response = fileStorageService.storeFileInDirectory(file, "documents");
        return ResponseEntity.ok(response);
    }

    /**
     * Subir audio
     */
    @PostMapping("/upload/audio")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<FileUploadResponse> uploadAudio(@RequestParam("file") MultipartFile file) {
        if (!fileStorageService.isAudioFile(file)) {
            throw new FileStorageException("El archivo debe ser un audio (MP3, WAV, OGG)");
        }

        FileUploadResponse response = fileStorageService.storeFileInDirectory(file, "audio");
        return ResponseEntity.ok(response);
    }

    /**
     * Subir archivo para Work Order (evidencia)
     */
    @PostMapping("/upload/work-order/{workOrderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<FileUploadResponse> uploadWorkOrderFile(
            @PathVariable Long workOrderId,
            @RequestParam("file") MultipartFile file) {

        String directory = "work-orders/wo-" + workOrderId;
        FileUploadResponse response = fileStorageService.storeFileInDirectory(file, directory);
        return ResponseEntity.ok(response);
    }

    /**
     * Subir archivo para Asset
     */
    @PostMapping("/upload/asset/{assetId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<FileUploadResponse> uploadAssetFile(
            @PathVariable Long assetId,
            @RequestParam("file") MultipartFile file) {

        String directory = "assets/asset-" + assetId;
        FileUploadResponse response = fileStorageService.storeFileInDirectory(file, directory);
        return ResponseEntity.ok(response);
    }

    /**
     * Descargar archivo
     */
    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String fileName,
            HttpServletRequest request) {

        Resource resource = fileStorageService.loadFileAsResource(fileName);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            contentType = "application/octet-stream";
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    /**
     * Descargar archivo desde subdirectorio
     */
    @GetMapping("/{directory}/{fileName:.+}")
    public ResponseEntity<Resource> downloadFileFromDirectory(
            @PathVariable String directory,
            @PathVariable String fileName,
            HttpServletRequest request) {

        Resource resource = fileStorageService.loadFileAsResourceFromDirectory(directory, fileName);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            contentType = "application/octet-stream";
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    /**
     * Eliminar archivo
     */
    @DeleteMapping("/{fileName:.+}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteFile(@PathVariable String fileName) {
        fileStorageService.deleteFile(fileName);
        return ResponseEntity.noContent().build();
    }

    /**
     * Eliminar archivo de subdirectorio
     */
    @DeleteMapping("/{directory}/{fileName:.+}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteFileFromDirectory(
            @PathVariable String directory,
            @PathVariable String fileName) {
        fileStorageService.deleteFileFromDirectory(directory, fileName);
        return ResponseEntity.noContent().build();
    }
}
