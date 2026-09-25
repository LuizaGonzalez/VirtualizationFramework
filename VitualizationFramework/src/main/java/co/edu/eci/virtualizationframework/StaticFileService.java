/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.edu.eci.virtualizationframework;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Administrador
 */
public class StaticFileService {
    
    private static final String RESOURCE_ROOT = "/webroot";

    public enum Status { OK, INVALID_PATH, NOT_FOUND }

    public record StaticResource(Status status, byte[] content, String contentType) {}
    
    /**
     * Intenta servir el recurso correspondiente a la ruta dada.
     *
     * @param path la ruta solicitada (ej. "/index.html", "/images/logo.png")
     * @return el resultado con su Status correspondiente (OK, INVALID_PATH o NOT_FOUND)
     * @throws IOException si ocurre un error al leer el recurso
     */
    public StaticResource serve(String path) throws IOException {
        String normalized = normalizePath(path);

        if (!isSafePath(normalized)) {
            return new StaticResource(Status.INVALID_PATH, null, null);
        }

        byte[] content = readResource(normalized);
        if (content == null) {
            return new StaticResource(Status.NOT_FOUND, null, null);
        }

        return new StaticResource(Status.OK, content, contentTypeFor(normalized));
    }

    private String normalizePath(String path) {
        String target = (path == null || path.equals("/")) ? "/index.html" : path;
        return java.nio.file.Paths.get(target).normalize().toString().replace("\\", "/");
    }

    private boolean isSafePath(String normalizedPath) {
        return !normalizedPath.contains("..") && normalizedPath.startsWith("/");
    }
    
    private byte[] readResource(String normalizedPath) throws IOException {
        String resourcePath = RESOURCE_ROOT + normalizedPath;
        try (InputStream resourceStream = StaticFileService.class.getResourceAsStream(resourcePath)) {
            return resourceStream == null ? null : resourceStream.readAllBytes();
        }
    }
    
    static String contentTypeFor(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".html")) return "text/html; charset=UTF-8";
        if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }
}
