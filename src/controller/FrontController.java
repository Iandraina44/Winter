package controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import annotations.Controller;
import annotations.RestAPI;
import object.ModelView;
import object.ResourceNotFound;
import object.VerbMethod;
import utils.Mapping;
import utils.Utils;

public class FrontController extends HttpServlet {
    private List<String> controllers;
    private HashMap<String, Mapping> map;

    @Override
    public void init() throws ServletException {
        String packageToScan = this.getInitParameter("package_name");
        try {
            this.controllers = new Utils().getAllClassesStringAnnotation(packageToScan, Controller.class);
            this.map = new Utils().scanControllersMethods(this.controllers);
        } catch (Exception e) {
            throw new ServletException(e);
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
        Utils u = new Utils();
        PrintWriter out = response.getWriter();
        StringBuffer url = request.getRequestURL();
        /* URL a rechercher dans le map */
        String path = u.getURIWithoutContextPath(request);
        /* Prendre le mapping correspondant a l'url */
        try {
            // Prendre les parametres
            Map<String, String[]> params = request.getParameterMap();
            // Recherche methode
            VerbMethod meth = u.searchVerbMethod(request, map, path);
            // Execution methode
            Object res = u.execute(request, meth, map, path, params);
            /* verification si methode est rest */
            if (meth.getMethode().isAnnotationPresent(RestAPI.class)) {
                /* Changer le type du response en json */
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                Gson gson = new Gson();
                /* si le type de retour nest pas modelview on return le json directement */
                if (!(res instanceof ModelView)) {
                    gson.toJson(res, out);
                }
                /* si c'est model view */
                else {
                    ModelView mv = (ModelView) res;
                    gson.toJson(mv.getData(), out);
                }

            }
            /* si methode NON REST */
            else {
                out.println("L'URL EST :" + url);
                out.println("L'URL a chercher dans le map : " + path);
                /* Printer tous les controllers */
                out.print("\n");
                out.println("Liste de tous vos controllers : ");
                for (String class1 : this.controllers) {
                    out.println(class1);
                }
                if (res instanceof String) {
                    out.println(res.toString());
                } else if (res instanceof ModelView) {
                    ModelView modelview = (ModelView) res;
                    String urlDispatch = modelview.getUrl();
                    RequestDispatcher dispatcher = request.getRequestDispatcher(urlDispatch);
                    HashMap<String, Object> data = modelview.getData();
                    for (String key : data.keySet()) {
                        request.setAttribute(key, data.get(key));
                    }
                    dispatcher.forward(request, response);
                }
            }
        } 
        catch(ResourceNotFound e){
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(e.getMessage());
            e.printStackTrace();;
        }catch (Exception e) {
            // TODO Auto-generated catch block
            /* throw new ServletException(e); */
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(e.getMessage());
            e.printStackTrace();


        }
    }

}