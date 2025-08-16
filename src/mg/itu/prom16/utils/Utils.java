package mg.itu.prom16.utils;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
import mg.itu.prom16.annotations.Auth;
import mg.itu.prom16.annotations.AuthC;
import mg.itu.prom16.annotations.Controller;
import mg.itu.prom16.annotations.ErrorUrl;
import mg.itu.prom16.annotations.FieldParam;
import mg.itu.prom16.annotations.Get;
import mg.itu.prom16.annotations.Numeric;
import mg.itu.prom16.annotations.ObjectParam;
import mg.itu.prom16.annotations.Param;
import mg.itu.prom16.annotations.Post;
import mg.itu.prom16.annotations.Range;
import mg.itu.prom16.annotations.RestAPI;
import mg.itu.prom16.annotations.UrlMapping;
import mg.itu.prom16.exceptions.NumericException;
import mg.itu.prom16.exceptions.RangeException;
import mg.itu.prom16.exceptions.ValidationException;
import mg.itu.prom16.object.ModelView;
import mg.itu.prom16.object.MyMultiPart;
import mg.itu.prom16.object.MySession;
import mg.itu.prom16.object.ResourceNotFound;
import mg.itu.prom16.object.VerbMethod;

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


    public java.sql.Timestamp parseTimestamp(String datetime) {
        try {
            return java.sql.Timestamp.valueOf(datetime.replace("T", " ") + ":00");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Object parse(Object o, Class<?> typage) {
    try {
        if (typage.equals(int.class) || typage.equals(Integer.class)) {
            return o != null ? Integer.valueOf((String) o) : 0;
        } else if (typage.equals(double.class) || typage.equals(Double.class)) {
            return o != null ? Double.valueOf((String) o) : 0.0;
        } else if (typage.equals(boolean.class) || typage.equals(Boolean.class)) {
            return o != null ? Boolean.valueOf((String) o) : false;
        } else if (typage.equals(byte.class) || typage.equals(Byte.class)) {
            return o != null ? Byte.valueOf((String) o) : (byte) 0;
        } else if (typage.equals(float.class) || typage.equals(Float.class)) {
            return o != null ? Float.valueOf((String) o) : 0.0f;
        } else if (typage.equals(short.class) || typage.equals(Short.class)) {
            return o != null ? Short.valueOf((String) o) : (short) 0;
        } else if (typage.equals(long.class) || typage.equals(Long.class)) {
            return o != null ? Long.valueOf((String) o) : 0L;
        } else if (typage.equals(String.class)) {
            return o != null ? o : "";
        } 
        else if (typage.equals(java.sql.Timestamp.class)) {
            return o != null ? this.parseTimestamp((String) o) : null;
        }
        else {
            // Try to use valueOf method if available
            try {
                Method valueOfMethod = typage.getMethod("valueOf", typage);
                return valueOfMethod.invoke(null, o);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                // If valueOf method is not available, fallback to typage.cast(o)
                return typage.cast(o);
            }
        }
    } catch (NumberFormatException e) {
        // Handle the exception as needed, for example, log it or rethrow it
        e.printStackTrace();
    }
    return null;
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

    public List<String> validateField(Map<String, String[]> params, Field field, String key) {
        List<String> errors = new ArrayList<>();
        // Check if the field has a Numeric annotation
        if (field.isAnnotationPresent(Numeric.class)) {
            if (params.get(key) != null) {
                try {
                    Double.parseDouble(params.get(key)[0]);
                } catch (Exception e) {
                    errors.add(new NumericException(key).getMessage());
                }
            }
        }
        // Check if the field has a Range annotation
        if (field.isAnnotationPresent(Range.class)) {
            if (params.get(key) != null) {
                try {
                    Range range = field.getAnnotation(Range.class);
                    double value = Double.parseDouble(params.get(key)[0]);
                    if (value < range.min() || value > range.max()) {
                        errors.add(new RangeException(key, range).getMessage());
                    }
                } catch (NumberFormatException e) {
                    errors.add(new NumericException(key).getMessage());
                }
            }
        }
        return errors;
    }

    public void processObject(Map<String, String[]> params, Parameter param, List<Object> ls) throws Exception {
        Map<String, List<String>> errorMap = new HashMap<>();
        String key = null;
        Class<?> c = param.getType();
        String nomObjet = null;
        nomObjet = param.isAnnotationPresent(ObjectParam.class) ? param.getAnnotation(ObjectParam.class).objName()
                : param.getName();
        Object o = c.getConstructor((Class[]) null).newInstance((Object[]) null);
        /// prendre les attributs
        Field[] f = c.getDeclaredFields();
        /// ATOMBOKA eto sprint 13
        /// validation des fields
        for (Field field : f) {
            String attributObjet = null;
            attributObjet = field.isAnnotationPresent(FieldParam.class)
                    ? field.getAnnotation(FieldParam.class).paramName()
                    : field.getName();
            key = nomObjet + "." + attributObjet;
            if (validateField(params, field, key).size() > 0) {
                errorMap.put(key, validateField(params, field, key));
            }
        }
        if (!errorMap.isEmpty()) {
            throw new ValidationException(errorMap);
        }
        for (Field field : f) {
            String attributObjet = null;
            attributObjet = field.isAnnotationPresent(FieldParam.class)
                    ? field.getAnnotation(FieldParam.class).paramName()
                    : field.getName();
            key = nomObjet + "." + attributObjet;
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

            // fix tableau
            else if (typage.isArray()) {
                processArray(typage, key, param, params, ls);
            } else if (!typage.isPrimitive() && !typage.equals(String.class)) {
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

    public void processArray(Class<?> typage, String key, Parameter param, Map<String, String[]> params,
            List<Object> ls) {
        if (typage.getComponentType().isPrimitive() || typage.getComponentType().equals(String.class)) {
            if (param.isAnnotationPresent(Param.class)
                    && params.containsKey(param.getAnnotation(Param.class).paramName())) {
                key = param.getAnnotation(Param.class).paramName();
            } else {
                key = param.getName();
            }
            if (typage.equals(int[].class)) {
                String[] values = params.get(key);
                int[] tab = new int[values.length];
                for (int i = 0; i < values.length; i++) {
                    tab[i] = Integer.parseInt(values[i]);
                }
                ls.add(tab);
            }
            if (typage.equals(double[].class)) {
                String[] values = params.get(key);
                double[] tab = new double[values.length];
                for (int i = 0; i < values.length; i++) {
                    tab[i] = Double.parseDouble(values[i]);
                }
                ls.add(tab);
            }
            if (typage.equals(String[].class)) {
                ls.add(params.get(key));
            }
        }
    }

    public VerbMethod searchVerbMethod(HttpServletRequest req, HashMap<String, Mapping> map, String path,
            String authVar, String authRole)
            throws Exception {
        if (map.containsKey(path)) {
            boolean did = verifAuthController(map.get(path), req, authVar, authRole);
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
            if (!did) {
                verifAuthMethode(m, req, authVar, authRole);
            }
            return m;
        } else {
            throw new Exception("Aucune méthode associé a cette url : " + path);
        }
    }

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

            Object[] args = null;
            try {
                args = this.getArgs(req, params, methode);
            } catch (ValidationException ve) {
                if (methode.isAnnotationPresent(ErrorUrl.class)) {
                    ve.setErrorUrl(methode.getAnnotation(ErrorUrl.class).url());
                    ve.setErrorMethod(methode.getAnnotation(ErrorUrl.class).method());
                }
                ve.setParamsBeforeError(params);
                throw ve;
            }
            res = methode.invoke(appelant, args);

        } else {
            throw new Exception(
                    "La requete est de type " + req.getMethod() + " alors que la methode est de type "
                            + verbmethode.getVerb());
        }
        return res;
    }

    public void verifAuthMethode(VerbMethod meth, HttpServletRequest request, String authVarName,
            String authRoleVarName) throws ResourceNotFound {
        if (meth.getMethode().isAnnotationPresent(Auth.class)) {
            if (request.getSession().getAttribute(authVarName) == null) {
                throw new ResourceNotFound("Vous n'etes pas connecte");
            }
            if (!meth.getMethode().getAnnotation(Auth.class).authRole().equals("")) {
                if (request.getSession().getAttribute(authRoleVarName) == null ||
                        !request.getSession().getAttribute(authRoleVarName)
                                .equals(meth.getMethode().getAnnotation(Auth.class).authRole())) {
                    throw new ResourceNotFound("Vous n'avez pas le role necessaire");
                }
            }
        }
    }

    public boolean verifAuthController(Mapping m, HttpServletRequest request, String authVarName,
            String authRoleVarName)
            throws ResourceNotFound, ClassNotFoundException {
        Class<?> meth = Class.forName(m.getClassName());
        if (meth.isAnnotationPresent(AuthC.class)) {
            if (request.getSession().getAttribute(authVarName) == null) {
                throw new ResourceNotFound("Vous n'etes pas connecte");
            }
            if (!meth.getAnnotation(AuthC.class).authRole().equals("")) {
                if (request.getSession().getAttribute(authRoleVarName) == null ||
                        !request.getSession().getAttribute(authRoleVarName)
                                .equals(meth.getAnnotation(AuthC.class).authRole())) {
                    throw new ResourceNotFound("Vous n'avez pas le role necessaire");
                }
            }
            return true;
        }
        return false;
    }

}
