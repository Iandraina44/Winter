package utils;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import jakarta.servlet.http.HttpServletRequest;
import annotations.Controller;
import annotations.FieldParam;
import annotations.Get;
import annotations.Numeric;
import annotations.ObjectParam;
import annotations.Param;
import annotations.Post;
import annotations.Range;
import annotations.RestAPI;
import annotations.UrlMapping;
import exceptions.NumericException;
import exceptions.RangeException;
import object.ModelView;
import object.MyMultiPart;
import object.MySession;
import object.ResourceNotFound;
import object.VerbMethod;

public class Utils {
    static public String getCatMethodName(String attributeName) {
        String get = "get";
        String firstLetter = attributeName.substring(0, 1).toUpperCase();
        String rest = attributeName.substring(1);
        String res = firstLetter.concat(rest);
        String methodName = get.concat(res);
        return methodName;
    }

    static public String setCatMethodName(String attributeName) {
        String set = "set";
        String firstLetter = attributeName.substring(0, 1).toUpperCase();
        String rest = attributeName.substring(1);
        String res = firstLetter.concat(rest);
        String methodName = set.concat(res);
        return methodName;
    }

    boolean isController(Class<?> c) {
        return c.isAnnotationPresent(Controller.class);
    }

    public Object parse(Object o, Class<?> typage) {
        if (typage.equals(int.class)) {
            return o != null ? Integer.parseInt((String) o) : 0;
        } else if (typage.equals(double.class)) {
            return o != null ? Double.parseDouble((String) o) : 0;
        } else if (typage.equals(boolean.class)) {
            return o != null ? Boolean.parseBoolean((String) o) : false;

        } else if (typage.equals(byte.class)) {
            return o != null ? Byte.parseByte((String) o) : 0;

        } else if (typage.equals(float.class)) {
            return o != null ? Float.parseFloat((String) o) : 0;

        } else if (typage.equals(short.class)) {
            return o != null ? Short.parseShort((String) o) : 0;

        } else if (typage.equals(long.class)) {
            return o != null ? Long.parseLong((String) o) : 0;

        }
        return typage.cast(o);
    }

    public List<String> getAllClassesStringAnnotation(String packageName, Class annotation) throws Exception {
        List<String> res = new ArrayList<String>();
        // répertoire racine du package
        if (this.getClass().getClassLoader().getResource(packageName.replace('.', '/')) == null) {
            throw new Exception("Le package " + packageName + " n'existe pas");
        }
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

    public HashMap<String, Mapping> scanControllersMethods(List<String> controllers) throws Exception {
        HashMap<String, Mapping> res = new HashMap<>();
        for (String c : controllers) {
            Class classe = Class.forName(c);
            /* Prendre toutes les méthodes de cette classe */
            Method[] meths = classe.getDeclaredMethods();
            for (Method method : meths) {
                if (method.isAnnotationPresent(UrlMapping.class)) {
                    String url = method.getAnnotation(UrlMapping.class).url();
                    /* Prendre l'annotation URL */
                    String valeurAnnotationUrl = Get.value;
                    if (method.isAnnotationPresent(Get.class)) {
                        valeurAnnotationUrl = Get.value;
                    } else if (method.isAnnotationPresent(Post.class)) {
                        valeurAnnotationUrl = Post.value;
                    }
                    if (url.contains("?")) {
                        url = url.split("?")[0];
                    }
                    if (res.containsKey(url)) {
                        if (!res.get(url).getVerbmethods().add(new VerbMethod(valeurAnnotationUrl, method))) {
                            System.out.println("tsy mety scan");
                            throw new Exception(
                                    "Il ya deja un verb " + valeurAnnotationUrl + " sur l'url " + url);
                        }
                    } else {
                        Set<VerbMethod> set = new HashSet<VerbMethod>();
                        set.add(new VerbMethod(valeurAnnotationUrl, method));
                        res.put(url, new Mapping(c, set));
                    }
                }
            }
        }
        return res;
    }

    public String getURIWithoutContextPath(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }

    public void validateField(Map<String, String[]> params, Field field, String key) throws Exception {
        // Check if the field has a Numeric annotation
        if (field.isAnnotationPresent(Numeric.class)) {
            if (params.get(key) != null) {
                try {
                    Double.parseDouble(params.get(key)[0]);
                } catch (Exception e) {
                    throw new NumericException(key);
                }
            }
        }
        // Check if the field has a Range annotation
        if (field.isAnnotationPresent(Range.class)) {
            if (params.get(key) != null) {
                try {
                    Double.parseDouble(params.get(key)[0]);
                } catch (Exception e) {
                    throw new NumericException(key);
                }
                Range range = field.getAnnotation(Range.class);
                double value = Double.parseDouble(params.get(key)[0]);
                if (value < range.min() || value > range.max()) {
                    throw new RangeException(key, range);
                }

            }
        }
    }

    public void processObject(Map<String, String[]> params, Parameter param, List<Object> ls) throws Exception {
        String key = null;
        Class<?> c = param.getType();
        String nomObjet = null;
        nomObjet = param.isAnnotationPresent(ObjectParam.class) ? param.getAnnotation(ObjectParam.class).objName()
                : param.getName();
        Object o = c.getConstructor((Class[]) null).newInstance((Object[]) null);
        /// prendre les attributs
        Field[] f = c.getDeclaredFields();
        for (Field field : f) {
            String attributObjet = null;
            attributObjet = field.isAnnotationPresent(FieldParam.class)
                    ? field.getAnnotation(FieldParam.class).paramName()
                    : field.getName();
            key = nomObjet + "." + attributObjet;
            /// ATOMBOKA eto sprint 13
            validateField(params, field, key);
            Method setters = c.getDeclaredMethod(setCatMethodName(attributObjet), field.getType());
            if (key == null || params.get(key) == null) {
                setters.invoke(o, this.parse(null, field.getType()));
            } else if (params.get(key).length == 1) {
                setters.invoke(o, this.parse(params.get(key)[0], field.getType()));
            } else if (params.get(key).length > 1) {
                setters.invoke(o, this.parse(params.get(key), field.getType()));
            }
        }
        ls.add(o);
    }

    public Object[] getArgs(HttpServletRequest req, Map<String, String[]> params, Method method) throws Exception {
        List<Object> ls = new ArrayList<Object>();
        for (Parameter param : method.getParameters()) {
            String key = null;
            /// Traitement type
            Class<?> typage = param.getType();
            if (typage.equals(MySession.class)) {
                ls.add(new MySession(req.getSession()));
            }

            // teto spint 12
            else if (typage.equals(MyMultiPart.class)) {
                if (param.isAnnotationPresent(Param.class)
                        && params.containsKey(param.getAnnotation(Param.class).paramName())) {
                    key = param.getAnnotation(Param.class).paramName();
                } else {
                    key = param.getName();
                }
                ls.add(new MyMultiPart(req.getPart(key)));
            }

            else if (!typage.isPrimitive() && !typage.equals(String.class)) {
                this.processObject(params, param, ls);
            } else {
                if (params.containsKey(param.getName())) {
                    key = param.getName();
                } else if (param.isAnnotationPresent(Param.class)
                        && params.containsKey(param.getAnnotation(Param.class).paramName())) {
                    key = param.getAnnotation(Param.class).paramName();
                }
                /// Traitement values
                if (key == null || params.get(key) == null) {
                    ls.add(this.parse(null, typage));
                } else if (params.get(key).length == 1) {
                    ls.add(this.parse(params.get(key)[0], typage));
                } else if (params.get(key).length > 1) {
                    ls.add(this.parse(params.get(key), typage));
                }
            }

        }
        return ls.toArray();
    }

    public VerbMethod searchVerbMethod(HttpServletRequest req, HashMap<String, Mapping> map, String path)
            throws Exception {
        if (map.containsKey(path)) {
            VerbMethod[] verb_meths = (VerbMethod[]) map.get(path).getVerbmethods().toArray(new VerbMethod[0]);
            VerbMethod m = null;
            for (VerbMethod verbMethod : verb_meths) {
                if (verbMethod.getVerb().equals(req.getMethod())) {
                    m = verbMethod;
                    break;
                }
            }
            if (m == null) {
                throw new ResourceNotFound("L'url ne supporte pas la méthode " + req.getMethod());
            }
            return m;
        } else {
            throw new Exception("Aucune méthode associé a cette url");
        }
    }

    // public Object searchExecute(HttpServletRequest req, HashMap<String, Mapping>
    // map, String path,
    // Map<String, String[]> params)
    // throws Exception {
    // Method methode = this.searchMethod(map, path);
    // Mapping m = map.get(path);
    // Class<?> classe = Class.forName(m.getClassName());
    // Object appelant = classe.getDeclaredConstructor().newInstance((Object[])
    // null);
    // for (Field field : classe.getDeclaredFields()) {
    // if (field.getType().equals(MySession.class)) {
    // classe.getMethod(setCatMethodName(field.getName()),
    // MySession.class).invoke(appelant,
    // new MySession(req.getSession()));
    // }
    // }
    // Object res = methode.invoke(appelant, this.getArgs(req, params, methode));
    // if (!(res instanceof String) && !(res instanceof ModelView)) {
    // throw new Exception("La méthode " + methode.getName() + " ne retourne ni
    // String ni ModelView");
    // }
    // return res;
    // }

    public Object execute(HttpServletRequest req, VerbMethod verbmethode, HashMap<String, Mapping> map, String path,
            Map<String, String[]> params)
            throws Exception {
        Object res = null;
        Mapping m = map.get(path);
        // Verification REQUETE VERB
        if (req.getMethod().equals(verbmethode.getVerb())) {
            Method methode = verbmethode.getMethode();
            Class<?> classe = Class.forName(m.getClassName());
            Object appelant = classe.getDeclaredConstructor().newInstance((Object[]) null);
            for (Field field : classe.getDeclaredFields()) {
                if (field.getType().equals(MySession.class)) {
                    classe.getMethod(setCatMethodName(field.getName()), MySession.class).invoke(appelant,
                            new MySession(req.getSession()));
                }
            }
            res = methode.invoke(appelant, this.getArgs(req, params, methode));

        } else {
            throw new Exception(
                    "La requete est de type " + req.getMethod() + " alors que la methode est de type "
                            + verbmethode.getVerb());
        }
        return res;

    }
}
