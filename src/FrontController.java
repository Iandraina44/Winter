package controller;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import annotations.Controller;
import utils.Mapping;
import utils.ModelView;
import utils.Utils;

public class FrontController extends HttpServlet {
    private List<String> controllers;   
    private HashMap<String,Mapping> map;


    
    @Override
    public void init() throws ServletException {

        String packageToScan = this.getInitParameter("package_name");
        
        if (packageToScan == null || packageToScan.isEmpty()) {
            throw new ServletException("Le paramètre 'package_name' est manquant ou vide.");
        }
        
        try {

            this.controllers = new Utils().getAllClassesStringAnnotation(packageToScan, Controller.class);

            if (this.controllers.isEmpty()) {
                throw new ServletException("Aucune classe trouvée dans le package spécifié : " + packageToScan);
            }

            this.map = new Utils().scanControllersMethods(this.controllers);

            if (Utils.hasDuplicateKeys(map)) {
                throw new ServletException("vous avez deux get mapping similaires dans votre classe");
            }


        } catch (ServletException e) {
            throw e;  // Relancer l'exception ServletException
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException("Une erreur est survenue lors de l'initialisation des contrôleurs.", e);
        }
    }
    

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        PrintWriter out = response.getWriter();
        
        StringBuffer url = request.getRequestURL();
        
        /* URL a rechercher dans le map */
        String path =new Utils().getURIWithoutContextPath(request);
        
        out.println("L'URL EST :" + url);
        out.println("L'URL a chercher dans le map : " + path);


        // process la methode 
        Utils.ProcessMethod(map,path,request,response,out);
        
      
        
        
        /* Printer tous les controllers */
        out.print("\n");
        out.println("Liste de tous vos controllers : ");


        for (String class1 : this.controllers) {
            out.println(class1);
        }
        
    }
}