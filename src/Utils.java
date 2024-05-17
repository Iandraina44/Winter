package mg.utils;

import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import mg.annotation.*;
import mg.controller.*;

public class Utils {

    public static boolean isController(Class<?> c) {
        return c.isAnnotationPresent(Controller.class);
    }


    public static List<String> getAllControllers(FrontController frontcontroller,String packageName) throws Exception {
        List<String> res=new ArrayList<String>();
        //répertoire racine du package
        String path = frontcontroller.getClass().getClassLoader().getResource(packageName.replace('.', '/')).getPath();
        String decodedPath = URLDecoder.decode(path, "UTF-8");
        File packageDir = new File(decodedPath);

        // parcourir tous les fichiers dans le répertoire du package
        File[] files = packageDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".class")) {
                    String className = packageName + "." + file.getName().replace(".class", "");
                    Class<?> classe = Class.forName(className);
                    if (Utils.isController(classe)) {
                        res.add(className);
                    }
                }
            }
        }
        return res;

    }    
}
