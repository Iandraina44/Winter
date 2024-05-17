package controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import controller.*;
import utils.*;

public class FrontController extends HttpServlet {
    private static List<String> controllers;
    private static boolean isChecked;


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
        out.println("L'URL EST :" + url);
        if(!FrontController.isChecked){
            String packageToScan = this.getInitParameter("package_name");
            try {
                FrontController.controllers=Utils.getAllControllers(this,packageToScan);
                FrontController.isChecked=true;
                out.println("Premier et dernier scan");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        /* Printer tous les controllers */
        for (String class1 : controllers) {
            out.println(class1);
        }
    }



    
}