package co.edu.eci.virtualizationframework;

import java.io.IOException;
import java.util.logging.Logger;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author luiza.gonzalez-v
 */
public class ServidorWebMantenible {
    
    private static final Router router = new Router();
    private static boolean running = true;
    
    //Registramos la ruta y su lambda
    public static void get(String route, WebService ws)
    {    
        router.addRoute(route,ws);
    }
    //Ejecutar la lambda cuando se necesite
    public static String invoke(String route, String query) 
    {
        return router.invoke(route, query);
    }
    /**
     * Marca el servidor como detenido. No interrumpe la petición actual 
     * solo indica que el ciclo principal debe terminar después de responder.
     */
    public static void stop(){
        running = false;
    }
    public static boolean isRunning(){
        return running;
    }
    public static void start()
    {
        String[] args = {};
        try{
            HttpServer.main(args); 
        } catch (IOException ex){
            Logger.getLogger(ServidorWebMantenible.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
