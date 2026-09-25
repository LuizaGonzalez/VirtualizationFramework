package co.edu.eci.virtualizationframework;

import co.edu.eci.virtualizationframework.ServidorWebMantenible;
import static co.edu.eci.virtualizationframework.ServidorWebMantenible.*;

/**
 *
 * @author luiza.gonzalez-v
 */

public class Application {

    public static void main(String[] args) throws Exception {

        get("/pi", (request, response) -> String.valueOf(Math.PI));
        get("/e", (request, response) -> String.valueOf(Math.E));
        get("/hello", (request, response) -> {
            String name = request.getValue("name");
            if (name == null || name.isBlank()) {
                name = "world";
            }
            String greetingPrefix = System.getenv()
                    .getOrDefault("GREETING_PREFIX", "Hello, ");
            return greetingPrefix + " " + name + "!";
        });
        
        get("/square", (request, response) -> {
            int number = Integer.parseInt(request.getValue("value"));
            return String.valueOf(number * number);
        });
        String environment = System.getenv().getOrDefault("APP_ENV", "development");
        
        if(environment.equals("development")){
            get("/shutdown", (request, response) -> {
                ServidorWebMantenible.stop();
                return "Server will stop after this response.";
            });
        }
        start();
    }
}
