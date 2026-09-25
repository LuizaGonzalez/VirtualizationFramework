package co.edu.eci.virtualizationframework;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author luiza.gonzalez-v
 */
public class HttpServer {

    private static final int DEFAULT_PORT = 8080;
    private static final StaticFileService staticFileService = new StaticFileService();

    /** Puerto de entrada del servidor, resuelve el puerto que se va a usar y 
     *  acepta conexiones
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        int port = resolvePort(args);
        runServer(port);
    }
    
    /** 
     * Abre el puerto y mantiene las conexiones de clientes
     */
    private static void runServer(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Ready to receive on port " + port + "...");
        
        while(ServidorWebMantenible.isRunning()){
            try (Socket clientSocket = serverSocket.accept()){
                handleRequest(clientSocket);
            }catch (IOException e) {
                System.out.println("Error atendiendo una solicitud: " + e.getMessage());
            }catch (RuntimeException e) {
                System.out.println("Error inesperado atendiendo una solicitud: " + e.getMessage());
            }
        }
        serverSocket.close();
        System.out.println("Server stopped gracefully.");
    }
    
    /**Determina el puerto que usara el servidor, siguiendo la prioridad de
     * argumento en linea de comandos, variable de entorno y por defecto
     * @param args argumentos de línea de comandos recibidos 
     * @return el número de puerto a usar
     */
    private static int resolvePort(String[] args) 
    {
        Integer fromArgs = parsePortArg(args);
        if(fromArgs != null) {
            return fromArgs;
        }
        Integer fromEnv = parsePortEnv();
        if(fromEnv != null) {
            return fromEnv;
        }
        return DEFAULT_PORT;
    }
    
    /**
     * Obtiene el puerto desde el argumento de línea de comandos.
     * 
     * @param args argumentos de línea de comandos
     * @return el puerto si es un número válido, o null si
     *  no se pasó ningún argumento o el valor no es numérico
     */
    private static Integer parsePortArg(String[] args){
        if(args.length == 0) {
            return null;
        }
        try {
            return Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Argumento de puerto inválido, usando valor por defecto " + DEFAULT_PORT);
            return null;
        }
    }
    /**
     * Intenta obtener el puerto desde la variable de entorno
     * @return el puerto si la variable existe y es un número válidoo, o null 
     * si no está definida o su valor no es numérico
     */
    private static Integer parsePortEnv() {
        String envPort = System.getenv("PORT");
        if (envPort == null) {
            return null;
        }
        try {
            return Integer.parseInt(envPort);
        } catch (NumberFormatException e) {
            System.out.println("Variable de entorno PORT inválida, usando valor por defecto " + DEFAULT_PORT);
            return null;
        }
    }
    
    /**
     * Coordina todo el proceso de una peticion del cliente, lee la solicitud, 
     * ignora los encabezados, identifica el metodo, la ruta y los parametros
     * @param clientSocket el socket de la conexión aceptada
     * @throws IOException si ocurre un error de lectura o escritura en el socket
     */
    private static void handleRequest(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
        OutputStream out = new BufferedOutputStream(clientSocket.getOutputStream());

        String requestLine = readRequestLine(in);
        if (requestLine == null || requestLine.isEmpty()) {
            return;
        }
        
        consumeHeaders(in);
        
        ParsedRequest request = parseRequestLine(requestLine, out);
        if (request == null) {
            return; 
        }
        
        route(request, out);

    }
    
    /**
     * Lee la primera línea de la petición HTTP
     *
     * @param in el lector conectado al stream de entrada del cliente
     * @return la línea de solicitud, o  null si la conexión no envió nada
     * @throws IOException si ocurre un error de lectura
     */
    private static String readRequestLine(BufferedReader in)throws IOException {
        String requestLine = in.readLine();
        if (requestLine == null || requestLine.isEmpty()){
            return null;
        }
        System.out.println("Request: " + requestLine);
        return requestLine;
    }
    
    /**
     * Lee las líneas de los encabezados HTTP y las ignora hasta encontrar una 
     * línea vacía. Los encabezados no se utilizan en este servidor.
     *
     * @param in el lector conectado al stream de entrada del cliente
     * @throws IOException si ocurre un error de lectura
     */
    private static void consumeHeaders(BufferedReader in) throws IOException {
        String headerLine;
        while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
            // ignorado intencionalmente
        }
    }
    /**
     * Analiza la línea de la petición para identificar el método HTTP, la ruta
     * y los parámetros. Si la petición está mal escrita, no utiliza el método 
     * GET o la dirección no es válida, envía un mensaje de error y detiene 
     * el procesamiento.
     * 
     * @param requestLine la línea de solicitud 
     * @param out el stream de salida por el que envia una respuesta de error
     * @return la ruta y el query string,
     * @throws IOException si ocurre un error al escribir la respuesta de error
     */
    private static ParsedRequest parseRequestLine(String requestLine, OutputStream out) throws IOException {
        String[] parts = requestLine.split(" ");
        if (parts.length < 2) {
            sendError(out, 400, "Bad Request");
            return null;
        }
        String method = parts[0];
        if (!method.equals("GET")) {
            sendError(out, 405, "Method Not Allowed");
            return null;
        }
        URI uri;
        try {
            uri = new URI(parts[1]);
        } catch (URISyntaxException e) {
            sendError(out, 400, "Bad Request");
            return null;
        }
        
        return new ParsedRequest(uri.getPath(), uri.getQuery());
    }
    /**
     * Decide cómo resolver la petición: primero intenta invocar una ruta
     * dinámica registrada vía {@code get()}; si no existe ninguna para esta
     * ruta, intenta servir un recurso estático.
     *
     * @param request la petición ya parseada (ruta y query string)
     * @param out     el stream de salida por el que se enviará la respuesta
     * @throws IOException si ocurre un error al escribir la respuesta
     */
    private static void route(ParsedRequest request, OutputStream out) throws IOException {
        try {
            String result = ServidorWebMantenible.invoke(request.path(), request.query());

            if (result != null) {
                sendText(out, result);
            } else {
                handleStaticResource(out, request.path());
            }
        } catch (RuntimeException e) {
            System.out.println("Error procesando la ruta " + request.path() + ": " + e.getMessage());
            sendError(out, 400, "Bad Request");
        }
    }
    
    // Recursos estáticos
     /**
     * Intenta servir un archivo estático correspondiente a la ruta dada.
     * Valida que la ruta sea segura (sin path traversal) antes de buscarla
     * en el classpath.
     *
     * @param out  el stream de salida por el que se enviará la respuesta
     * @param path la ruta solicitada (ej. {@code "/index.html"}, {@code "/images/logo.png"})
     * @throws IOException si ocurre un error al leer el recurso o escribir la respuesta
     */
    private static void handleStaticResource(OutputStream out, String path) throws IOException {
        StaticFileService.StaticResource resource = staticFileService.serve(path);

        switch (resource.status()) {
            case OK -> sendBytes(out, 200, "OK", resource.contentType(), resource.content());
            case INVALID_PATH -> sendError(out, 400, "Bad Request");
            case NOT_FOUND -> sendError(out, 404, "Not Found");
        }
    } 
    //Utilidades de respuesta 

    private static void sendText(OutputStream out, String body) throws IOException {
        sendBytes(out, 200, "OK", "text/plain; charset=UTF-8",
                body.getBytes(StandardCharsets.UTF_8));
    }

    private static void sendError(OutputStream out, int statusCode, String statusText) throws IOException {
        String body = statusCode + " " + statusText;
        sendBytes(out, statusCode, statusText, "text/plain; charset=UTF-8",
                body.getBytes(StandardCharsets.UTF_8));
    }

    private static void sendBytes(OutputStream out, int statusCode, String statusText,
                                   String contentType, byte[] body) throws IOException {
        String header = "HTTP/1.1 " + statusCode + " " + statusText + "\r\n"
                + "Content-Type: " + contentType + "\r\n"
                + "Content-Length: " + body.length + "\r\n"
                + "\r\n";
        out.write(header.getBytes(StandardCharsets.UTF_8));
        out.write(body);
        out.flush();
    }

    /**
     * Representa una petición HTTP ya parseada: la ruta solicitada y su
     * query string asociado (si lo tiene).
     *
     * @param path  la ruta de la petición 
     * @param query el query string crudo, o null si no hay parámetros
     */
    private record ParsedRequest(String path, String query) {}
}
