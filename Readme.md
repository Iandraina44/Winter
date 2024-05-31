tutorial to use the framework:

-ajouter le Winter.jar dans le lib 
-creer web.xml
-mapper un servlet comme suis 
<servlet>
    <servlet-name>FrontController</servlet-name>
    <servlet-class>mg.itu.prom16.controller.FrontController</servlet-class>
    <init-param>
        <param-name>package_name</param-name>
        <param-value>wcontroller</param-value>
    </init-param>
</servlet>
<servlet-mapping>
    <servlet-name>FrontController</servlet-name>
    <url-pattern>/</url-pattern>
</servlet-mapping>

-creer un classe dans un package wcontroller et mapper @Controller
importer les calsse mg.itu.prom16.annotations
-mapper les methode souhaitez avec @GetMapping