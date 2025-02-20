package mg.itu.prom16.utils;

import java.util.HashMap;
import java.util.List;

public class UserUtils {
    
    public static String[] getErrors(HashMap<String, List<String>> errorMap, String errorName) {
        // Vérifie si le HashMap contient la clé spécifiée
        if (errorMap.containsKey(errorName)) {
            // Récupère la liste d'erreurs et la convertit en tableau de String
            List<String> errors = errorMap.get(errorName);
            return errors.toArray(new String[0]);
        }
        // Retourne un tableau vide si la clé n'existe pas
        return new String[0];
    }
}
