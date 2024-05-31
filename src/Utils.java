package mg.itu.prom16.utils;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import mg.itu.prom16.annotations.Controller;
import mg.itu.prom16.annotations.GetMapping;

public class Utils {
    boolean isController(Class<?> c) {
        return c.isAnnotationPresent(Controller.class);
    }

    public List<String> getAllClassesStringAnnotation(String packageName,Class annotation) throws Exception {
        List<String> res=new ArrayList<String>();
        //répertoire racine du package
        String path = this.getClass().getClassLoader().getResource(packageName.replace('.', '/')).getPath();
        String decodedPath = URLDecoder.decode(path, "UTF-8");
        File packageDir = new File(decodedPath);

        // parcourir tous les fichiers dans le répertoire du package
        File[] files = packageDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".class")) {
                    String className = packageName + "." + file.getName().replace(".class", "");
                    Class<?> classe = Class.forName(className);
                    if (classe.isAnnotationPresent(annotation)) {
                        res.add(classe.getName());
                    }
                }
            }
        }
        return res;

    }
    public HashMap<String,Mapping> scanControllersMethods(List<String> controllers) throws Exception{
        HashMap<String,Mapping> res=new HashMap<>();
        for (String c : controllers) {
                Class classe=Class.forName(c);
                /* Prendre toutes les méthodes de cette classe */
                Method[] meths=classe.getDeclaredMethods();
                for (Method method : meths) {
                    if(method.isAnnotationPresent(GetMapping.class)){
                        String url=method.getAnnotation(GetMapping.class).url();
                        if(res.containsKey(url)){
                            String existant=res.get(url).className+":"+res.get(url).methodName;
                            String nouveau=classe.getName()+":"+method.getName();
                            throw new Exception("L'url "+url+" est déja mappé sur "+existant+" et ne peut plus l'être sur "+nouveau);
                        }
                        /* Prendre l'annotation */
                        res.put(url,new Mapping(c,method.getName()));
                    }
                }
            }
        return res;
    }
    
    public static String callMethod(String className,String methodName) throws Exception{
        Class<?> laclasse=Class.forName(className);
        Method method=laclasse.getMethod(methodName, (Class<?>[])null);
        Object objet=method.invoke(laclasse.getConstructor().newInstance(), (Object[])null);
        return objet.toString();
    }

    public static String findAndCallMethod(HashMap<String,Mapping> map,String path){
        if(map.containsKey(path)){
            Mapping m=map.get(path);
            try {
                return Utils.callMethod(m.getClassName(),m.getMethodName());
            } catch (Exception e) {
                return e.getMessage();
            }
        }
        else{
            return "Aucune méthode associé a cette url";
        }

    }

    public String getURIWithoutContextPath(HttpServletRequest request){
        return  request.getRequestURI().substring(request.getContextPath().length());
    }
}
