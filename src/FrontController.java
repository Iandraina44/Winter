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
        try {
            this.controllers=new Utils().getAllClassesStringAnnotation(packageToScan,Controller.class);
            this.map=new Utils().scanControllersMethods(this.controllers);
        } catch (Exception e) {
            e.printStackTrace();
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


        // appelle la methode
        try {
            Object objet=Utils.findAndCallMethod(map, path);
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
                RequestDispatcher dispat = request.getRequestDispatcher(view);
                dispat.forward(request, response);
            }
        
        } catch (Exception e) {
            out.println(e.getLocalizedMessage());
        }
      
        
        
        /* Printer tous les controllers */
        out.print("\n");
        out.println("Liste de tous vos controllers : ");


        for (String class1 : this.controllers) {
            out.println(class1);
        }
        
    }
}