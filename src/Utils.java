package utils;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.rmi.ServerException;
import java.security.spec.ECFieldF2m;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ModelView;
import utils.MySession;
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



    public HashMap<String,Mapping> scanControllersMethods(List<String> controllers) throws Exception,ServletException{

        HashMap<String,Mapping> res=new HashMap<>();
        
        for (String c : controllers) {
                Class classe=Class.forName(c);
                /* Prendre toutes les méthodes de cette classe */
                Method[] meths=classe.getDeclaredMethods();
                for (Method method : meths) {
                    if(method.isAnnotationPresent(GetMapping.class)){
                        
                        if (method.getReturnType()!=String.class && method.getReturnType()!=ModelView.class) {

                            throw new ServletException("le type de retour de la fonction"+method.getName()+"est different de String ou ModelView");
                        
                        }

                        String url=method.getAnnotation(GetMapping.class).url();

                        if(res.containsKey(url)){
                        
                            String existant=res.get(url).className+":"+res.get(url).methodName;
                            String nouveau=classe.getName()+":"+method.getName();
                            throw new ServletException("L'url "+url+" est déja mappé sur "+existant+" et ne peut plus l'être sur "+nouveau);
                        }
                        /* Prendre l'annotation */
                        res.put(url,new Mapping(c,method.getName()));
                    }
                }
            }
        return res;
    }


    public static Method findMethod(String className,String methodName,String path) throws ServletException,Exception{
        Class<?> laclasse=Class.forName(className);

        Method method=null;
        for (Method m : laclasse.getMethods()) {
            if (m.getName().equals(methodName)) {
                    Annotation[] annotations =  m.getAnnotations();
                    if (annotations[0] instanceof GetMapping) {
                        String name = ((GetMapping) annotations[0]).url();
                        if (name.equals(path)) {
                            method = m;
                        }
                   }
            }
        }
        if (method == null) {
            throw new ServletException("No such method" + methodName);
        }
        return  method;
    }

    
    public static Object callMethod(String className,String methodName,String path,HttpServletRequest request) throws ServletException,Exception{
        Class<?> laclasse=Class.forName(className);

        Method method=null;
        for (Method m : laclasse.getMethods()) {
            if (m.getName().equals(methodName)) {
                    Annotation[] annotations =  m.getAnnotations();
                    if (annotations[0] instanceof GetMapping) {
                        String name = ((GetMapping) annotations[0]).url();
                        System.out.println(name);
                        System.out.println(path);
                        if (name.equals(path)) {
                            method = m;
                        }
                   }
            }
        }
        if (method == null) {
            throw new ServletException("No such method" + methodName);
        }
        Object[] paramValues=getParameters(method,request, null);

        Object controller=laclasse.getConstructor().newInstance();

        setSessionAttribut(controller, laclasse, request);

        Object objet=method.invoke(controller, paramValues );
        return objet;

    }


    public static void setSessionAttribut(Object obje,Class<?> laclasse,HttpServletRequest request) throws Exception, ServerException{

        Field[] fld=laclasse.getDeclaredFields();
        for (Field field : fld) {
            if (field.getType().equals(MySession.class)) {
                field.setAccessible(true);
                field.set(obje,new MySession(request.getSession(true)));
            }
        }

    }

    
    public static Object[] getParameters(Method method,HttpServletRequest request ,HttpServletResponse response) throws ServletException, Exception{

        // Get parameter types and values from the request using annotations
        Parameter[] parameters = method.getParameters();
        Object[] parameterValues = new Object[parameters.length];
    
        for (int i = 0; i < parameters.length; i++) {

            if (parameters[i].getType().equals(MySession.class)) {
                parameterValues[i]=new MySession(request.getSession());
            }

            else{
                String paramName = "";
                Annotation[] annotations =  parameters[i].getAnnotations();
                if (annotations.length>0) {
                    for (Annotation annotation : annotations) {
                        if (annotation instanceof RequestParam) {
                            paramName = ((RequestParam) annotation).value();
                        }
                    }
                }else{

                    paramName=parameters[i].getName();
                    // throw new Exception("pas d annotation ETU002453");

                }
                    if (parameters[i].getType() == String.class||
                    parameters[i].getType() == int.class ||
                    parameters[i].getType() == double.class) {

                    String paramValue = request.getParameter(paramName);

                    if (parameters[i].getType() == String.class) {
                        parameterValues[i] = paramValue;
                    } else if (parameters[i].getType() == int.class || parameters[i].getType() == Integer.class) {
                        parameterValues[i] = Integer.parseInt(paramValue);
                    } else if (parameters[i].getType() == double.class || parameters[i].getType() == Double.class) {
                        parameterValues[i] = Double.parseDouble(paramValue);
                    }

                    } else {


                        Class<?> laclasse=Class.forName(parameters[i].getType().getName());
                        Object newInstance = laclasse.getDeclaredConstructor().newInstance();
                        Field[] attributs=(laclasse).getDeclaredFields();
                        Object[] attributsvalue=new Object[attributs.length];

                        for (int j=0 ; j<attributs.length ;j++) {
                            attributs[j].setAccessible(true);
                            String attvalue=request.getParameter(paramName+"."+attributs[j].getName());
                            System.out.println("tonga eto");
                            
                            if (attributs[j].getType() == String.class) {
                                attributsvalue[j] = attvalue;
                            } else if (attributs[j].getType() == int.class || attributs[j].getType() == Integer.class) {
                                attributsvalue[j] = Integer.parseInt(attvalue);
                            } else if (attributs[j].getType() == double.class || attributs[j].getType() == Double.class) {
                                attributsvalue[j] = Double.parseDouble(attvalue);
                            }
                            else {
                                
                                throw new ServletException("l objet ne peut pas avoir d objet en tant que parametre");
                            }
                            attributs[j].set(newInstance, attributsvalue[j]);
                        }
                        parameterValues[i] =newInstance;
                    }
                }
        }
        return parameterValues;
    }


    public static Object findAndCallMethod(HashMap<String,Mapping> map,String path,HttpServletRequest request)throws ServletException,Exception{
        if(map.containsKey(path)){
            Mapping m=map.get(path);
            return Utils.callMethod(m.getClassName(),m.getMethodName(),path,request);
        }
        else{
            throw new ServletException("No Such method "+path);
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
    throws ServletException
    {
        try {
            Object objet=Utils.findAndCallMethod(map, path,request);       

            Method method=Utils.findMethod(map.get(path).getClassName(),map.get(path).getMethodName(), path);
            if (method.isAnnotationPresent(RestApi.class)) {

                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                Gson gson=new  Gson();
                if (!(objet instanceof ModelView)) {
                    gson.toJson(objet,out);
                }
                else{
                    ModelView mv =(ModelView) objet;
                    gson.toJson(mv.getData(),out);
                }
                   
            }

            if (objet instanceof String) {
                out.println(objet.toString());
            }
            else if (objet instanceof ModelView) {

                HashMap<String,Object> hash=((ModelView)objet).getData();
                if (hash != null) {
                    if (!hash.isEmpty()) {
                        for (String string : hash.keySet()) {
                            request.setAttribute(string, hash.get(string));
                            out.println(string);
                        }
                    }
                }
                String view=((ModelView)objet).getUrl();
                out.println(view);
                request.getRequestDispatcher(view).forward(request, response);
            }
            
            else{
                throw new ServletException("type de retour non correcte doit etre String ou ModelView");
            }
            } catch (Exception e) {

                out.print(e.getLocalizedMessage());                    
                
                try {
                        
                request.setAttribute("exception",e.getLocalizedMessage());
                request.getRequestDispatcher("exception.jsp").forward(request, response);
            
                } catch (Exception ex) {
                System.err.println(ex.getLocalizedMessage());    
                }
            

        }
    }
}
