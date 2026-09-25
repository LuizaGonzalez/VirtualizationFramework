package co.edu.eci.virtualizationframework;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Luiza Gonzalez
 */
public class Request {
    private final Map<String, String> queryParams;
    
    public Request(Map<String, String> queryParams){
        this.queryParams = queryParams;
    }
    
    public static Request fromQuery(String query){
        Map<String, String> params = new HashMap<>();
        if (query != null && !query.isEmpty()) {
            for (String pair : query.split("&")) {
                String[] kv = pair.split("=", 2);
                String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
                params.put(key, value);
            }
        }
        return new Request(params);
    }
    /**
     * Devuelve el valor de un parametro de la URL
     * @param Key es el nombre del parámetro que quieres buscar
     * @return null si no existe el parametro, value si si esta presente
     */
    public String getValue(String Key) {
        return queryParams.get(Key);
    }
}
