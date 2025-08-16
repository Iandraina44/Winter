package mg.itu.prom16.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.prom16.annotations.Controller;
import mg.itu.prom16.annotations.RestAPI;
import mg.itu.prom16.exceptions.ValidationException;
import mg.itu.prom16.object.Export;
import mg.itu.prom16.object.ModelView;
import mg.itu.prom16.object.ResourceNotFound;
import mg.itu.prom16.object.VerbMethod;
import mg.itu.prom16.utils.CsvConverter;
import mg.itu.prom16.utils.Mapping;
import mg.itu.prom16.utils.Utils;

@MultipartConfig
public class FrontController extends HttpServlet {
    private List<String> controllers;
    private HashMap<String, Mapping> map;
    private String authVarName;
    private String authRoleVarName;

    @Override
    public void init() throws ServletException {
        String packageToScan = this.getInitParameter("package_name");
        if (packageToScan != null) {
            try {
                this.controllers = new Utils().getAllClassesStringAnnotation(packageToScan, Controller.class);
                this.map = new Utils().scanControllersMethods(this.controllers);
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
        this.authVarName = this.getInitParameter("auth_name");
        this.authRoleVarName = this.getInitParameter("auth_role_name");
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
        StringBuffer url = request.getRequestURL();
        /* URL a rechercher dans le map */
        String path = u.getURIWithoutContextPath(request);
        Object res = null;
        try {
            // Prendre les parametres
            Map<String, String[]> params = request.getParameterMap();
            // Recherche methode et verification auth
            VerbMethod meth = u.searchVerbMethod(request, map, path, this.authVarName, this.authRoleVarName);
            // Execution methode
            res = u.execute(request, meth, map, path, params);
            /* verification si methode est rest */
            if (meth.getMethode().isAnnotationPresent(RestAPI.class)) {
                PrintWriter out = response.getWriter();
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
                /*
                 * out.println("L'URL EST :" + url);
                 * out.println("L'URL a chercher dans le map : " + path);
                 * out.print("\n");
                 * out.println("Liste de tous vos controllers : ");
                 * for (String class1 : this.controllers) {
                 * out.println(class1);
                 * }
                 */
                if (res instanceof String) {
                    PrintWriter out = response.getWriter();
                    out.println(res.toString());
                } else if (res instanceof ModelView) {
                    ModelView modelview = (ModelView) res;
                    if (modelview.getUrl().startsWith("redirect:")) {
                        response.sendRedirect(request.getContextPath() + modelview.getUrl().substring(9));
                    } else {
                        String urlDispatch = modelview.getUrl();
                        RequestDispatcher dispatcher = request.getRequestDispatcher(urlDispatch);
                        HashMap<String, Object> data = modelview.getData();
                        for (String key : data.keySet()) {
                            request.setAttribute(key, data.get(key));
                        }
                        dispatcher.forward(request, response);
                    }
                } else if (res instanceof Export) {
                    Export e = (Export) res;
                    response.setContentType(e.getContentType());
                    response.setHeader("Content-Disposition",
                            "attachment; filename=" + e.getFileName() + e.getExtension());
                    if (e.getExtension().equals(".pdf")) {
                        OutputStream out = response.getOutputStream();
                        out.write(e.getBytes());
                    } else if (e.getExtension().equals(".csv")) {
                        OutputStream outputStream = response.getOutputStream();
                        response.setCharacterEncoding("UTF-8");
                            // Utiliser CsvConverter pour écrire dans le fichier temporaire
                            new CsvConverter().writeToCsv(e.getData(),outputStream);
                            
                                outputStream.flush();
                    }
                }

            }
        } catch (ValidationException e) {
            String errorUrl = e.getErrorUrl();
            String errorMethod = e.getErrorMethod();
            if (errorUrl != null) {
                // Create a new HttpServletRequestWrapper to modify the request method
                HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper(request) {
                    @Override
                    public String getMethod() {
                        return errorMethod;
                    }
                };
                wrappedRequest.setAttribute("errors", e.getErrorMap());
                wrappedRequest.setAttribute("params", e.getParamsBeforeError());
                // Dispatch the new request to errorUrl
                RequestDispatcher dispatcher = wrappedRequest.getRequestDispatcher(errorUrl);
                dispatcher.forward(wrappedRequest, response);
            } else {
                // Print the errors directly
                e.printStackTrace();
                writeHtmlError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            }
        } catch (ResourceNotFound e) {
            e.printStackTrace();
            writeHtmlError(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            /* throw new ServletException(e); */
            e.printStackTrace();
            writeHtmlError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    private void writeHtmlError(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setStatus(statusCode);
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"fr\">");
        out.println("<head>");
        out.println("<meta charset=\"UTF-8\">");
        out.println("<title>Erreur</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; background-color: #f8f8f8; margin: 50px; }");
        out.println(".error-box { border: 1px solid #e74c3c; background-color: #fef2f2; color: #c0392b; padding: 20px; border-radius: 5px; }");
        out.println("h1 { font-size: 24px; margin-top: 0; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class=\"error-box\">");
        out.println("<h1>Erreur " + statusCode + "</h1>");
        out.println("<p>" + message + "</p>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }
    
}