package utils;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import annotations.Controller;
import annotations.GetMapping;
import annotations.*;

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
    
    public static Object callMethod(String className,String methodName,HttpServletRequest request) throws Exception{
        Class<?> laclasse=Class.forName(className);
        Method method=null;
        for (Method m : laclasse.getMethods()) {
            if (m.getName().equals(methodName)) {
                method = m;
            }
        }
        if (method == null) {
            throw new Exception("No such method" + methodName);
        }
        Object[] paramValues=getParameters(method,request);
        Object objet=method.invoke(laclasse.getConstructor().newInstance(), paramValues );
        
        return objet;
    }

    public static Object[] getParameters(Method method,HttpServletRequest request ) throws Exception{
        // Get parameter types and values from the request using annotations
        Parameter[] parameters = method.getParameters();
        Object[] parameterValues = new Object[parameters.length];
    
        for (int i = 0; i < parameters.length; i++) {

            
            Annotation[] annotations =  parameters[i].getAnnotations();

            for (Annotation annotation : annotations) {

                if (annotation instanceof RequestParam) {
                    String paramName = ((RequestParam) annotation).value();
                    String paramValue = request.getParameter(paramName);
                    if (parameters[i].getType() == String.class) {

                    parameterValues[i] = paramValue;
                    } else if (parameters[i].getType() == int.class || parameters[i].getType() == Integer.class) {
                        parameterValues[i] = Integer.parseInt(paramValue);
                    } else if (parameters[i].getType() == double.class || parameters[i].getType() == Double.class) {
                        parameterValues[i] = Double.parseDouble(paramValue);
                    } else{
                        throw new Exception("Can't not take a parameters other than basic type like Sting int or double");
                    }
                }
            }
        }
        return parameterValues;
    }

    public static Object findAndCallMethod(HashMap<String,Mapping> map,String path,HttpServletRequest request)throws Exception{
        if(map.containsKey(path)){
            Mapping m=map.get(path);
            return Utils.callMethod(m.getClassName(),m.getMethodName(),request);
        }
        else{
            throw new Exception("No Such method "+path);
        }

    }

    public String getURIWithoutContextPath(HttpServletRequest request){
        return  request.getRequestURI().substring(request.getContextPath().length()).split("\\?")[0];
    }

    public static boolean hasDuplicateKeys(HashMap<String, Mapping> map) {
        Set<String> keysSet = new HashSet<>();
        for (String key : map.keySet()) {
            if (!keysSet.add(key)) {
                return true;
            }
        }
        return false;
    }



    public static void ProcessMethod(HashMap<String,Mapping> map,String path,HttpServletRequest request,HttpServletResponse response,PrintWriter out)
    {
        try {
            Object objet=Utils.findAndCallMethod(map, path,request);       
            if (objet instanceof String) {
                out.println(objet.toString());
            }
            else if (objet instanceof ModelView) {
                HashMap<String,Object> hash=((ModelView)objet).getData();
                for (String string : hash.keySet()) {
                    request.setAttribute(string, hash.get(string));
                    out.println(string);
                }
                String view=((ModelView)objet).getUrl();
                out.println(view);
                request.getRequestDispatcher(view).forward(request, response);
            }
            else{
                throw new ServletException("type de retour non correcte doit etre String ou ModelView");
            }
            } catch (Exception e) {
            out.println(e.getLocalizedMessage());
        }
    }
}
