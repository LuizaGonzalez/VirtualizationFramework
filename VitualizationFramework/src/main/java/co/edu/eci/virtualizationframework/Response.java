package co.edu.eci.virtualizationframework;
/**
 * Representa la respuesta HTTP que una lambda puede construir.
 * @author luiza Gonzalez
 */
public class Response {
    private int statusCode = 200;
    private String contentType = "text/plain; charset=UTF-8";
    
    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
