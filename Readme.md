# winter-framework
Framework comme Spring 

## Paramétrage :

    - Dans le web.xml de votre projet , définir le servlet FrontController , avec la classe mg.itu.prom16.controller.FrontController
    - Définir son init-param avec le param-name package_name et avec le param-value qui sera le package ou se trouvera vos controllers.
    - Définir l'urlmapping du FrontController , qui sera /
	- Définir aussi dans le FrontController , le nom de la variable d'authorisation dans la session , avec comme param-name auth_name et si on veut gérer les role on ajoute param-name auth_role_name

### Exemple :
    <servlet>
		<servlet-name>FrontController</servlet-name>
		<servlet-class>mg.itu.prom16.controller.FrontController</servlet-class>
		<init-param>
			<param-name>package_name</param-name>
			<param-value>wcontrollers</param-value>
			<description>Package ou se trouve tous les controllers</description>
		</init-param>
	</servlet>
	<servlet-mapping>
		<servlet-name>FrontController</servlet-name>
		<url-pattern>/</url-pattern>
	</servlet-mapping>

## Fonctionnalités :

    - Annotations de Controllers avec @Controller
    - Annotations de méthodes avec @GetMapping(String url)
    - Pour prendre des données depuis une vue , utiliser @Param sur les parametres de la fonction
    de votre controller ou bien par convention , matcher les params de la requete avec 
    les noms des parametres
	- Les données depuis une vue peuvent être aussi prises en tant qu'objet.
		-Utiliser @ObjectParam sur l'objet en question(ses attributs doivent avoir le même nom que les parametres ,
		ou bien annoter avec @FieldParam)
	- Utiliser l'annotation @RestAPI sur une méthode pour en faire un api rest.
    