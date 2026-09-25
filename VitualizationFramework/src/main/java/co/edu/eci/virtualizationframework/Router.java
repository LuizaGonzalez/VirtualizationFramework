/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.edu.eci.virtualizationframework;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Administrador
 */
public class Router {
    private final Map<String, WebService> routes = new HashMap<>();
    
    /** Registra una lambda para una ruta. */
    public void addRoute(String route, WebService ws) {
        routes.put(route, ws);
    }
    
    /**
     * Ejecuta la lambda registrada para una ruta, si existe.
     *
     * @param route la ruta solicitada
     * @param query el query string crudo, o null
     * @return el resultado de la lambda, o null si no hay ruta registrada
     */
    public String invoke(String route, String query) {
        WebService ws = routes.get(route);
        if (ws == null) {
            return null;
        }
        Request req = Request.fromQuery(query);
        Response resp = new Response();
        return ws.call(req, resp);
    }
    
}
