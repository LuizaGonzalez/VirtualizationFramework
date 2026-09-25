package co.edu.eci.virtualizationframework;

/**
 * Representa una lambda registrada con get(). Recibe la petición y la
 * respuesta, y devuelve el cuerpo como String 
 * @author luiza.gonzalez-v
 */
public interface WebService {
    
    public String call(Request request, Response response);
}
