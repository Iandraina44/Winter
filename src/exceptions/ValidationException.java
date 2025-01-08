package exceptions;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ValidationException extends Exception {
    private Map<String, List<String>> errorMap;
    String errorUrl; /* url de destination en cas d'erreur */
    String errorMethod; /* methode de destination en cas d'erreur */
    Map<String, String[]> paramsBeforeError ; /* parametres de la requete avant erreur*/
    public ValidationException(Map<String, List<String>> errorMap) {
        super("Validation failed");
        this.errorMap = errorMap;
    }

    public Map<String, List<String>> getErrorMap() {
        return errorMap;
    }
    public String getErrorMethod() {
        if(this.errorMethod==null){
            return "GET";
        }
        return errorMethod;
    }
    public void setErrorMethod(String errorMethod) {
        this.errorMethod = errorMethod;
    }
    public String getErrorUrl() {
        return errorUrl;
    }
    public void setErrorUrl(String errorUrl) {
        this.errorUrl = errorUrl;
    }

    @Override
    public String getMessage() {
        return errorMap.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + String.join(", ", entry.getValue()))
                .collect(Collectors.joining("; "));
    }

    public void setErrorMap(Map<String, List<String>> errorMap) {
        this.errorMap = errorMap;
    }

    public Map<String, String[]> getParamsBeforeError() {
        return paramsBeforeError;
    }

    public void setParamsBeforeError(Map<String, String[]> paramsBeforeError) {
        this.paramsBeforeError = paramsBeforeError;
    }
}
