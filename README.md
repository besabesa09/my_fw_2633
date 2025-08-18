# FRAMEWORK PERSO- ETU002621

Framework Java Web développé par l'étudiant **ETU002621**, basé sur le principe de Front Controller et l’utilisation de réflexions Java.

## 📦 Méthodes d'intégration dans un projet

### 🔹Utiliser le script de génération JAR
- Pour **Windows** : exécutez le script `script.bat`

Cela générera un fichier `.jar` que vous pourrez placer dans le dossier `WEB-INF/lib` de votre projet.

---

## 📁 Dépendances requises

Placez ces fichiers `.jar` dans le dossier `WEB-INF/lib/` de votre projet :

- [`gson.jar`](https://github.com/google/gson) — pour la sérialisation JSON
- `paranamer-2.8.jar` — pour la lecture des noms de paramètres en réflexion
- `servlet-api.jar` — pour le support des servlets Java

---

## ⚙️ Configuration `web.xml`

Ajoutez ce bloc dans le fichier `WEB-INF/web.xml` de votre application :

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
                             http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0">

  <!-- Configuration du DispatcherServlet -->
  <servlet>
    <servlet-name>dispatcherServlet</servlet-name>
    <servlet-class>p16.controller.FrontController</servlet-class>
    <!-- Emplacement du fichier de configuration Spring -->
    <init-param>
      <param-name>package_name</param-name>
      <param-value>p16.controller</param-value>
    </init-param>
    <load-on-startup>1</load-on-startup>
  </servlet>

  <!-- Mapping des URLs pour le DispatcherServlet -->
  <servlet-mapping>
    <servlet-name>dispatcherServlet</servlet-name>
    <url-pattern>/</url-pattern>
  </servlet-mapping>

</web-app>


### 
    SPRINT_1

Réferencer dans le classpath ou dans le lib le fichier jar (sprint_1.jar)

Mettre dans un même package toutes les classes controllers

Annoter au niveau de la classe toutes les classes controllers (@Controller)

Crée un fichier de configuration web.xml

    dans une balise servlet

* servlet-name
* servlet-class :
  p16.controller.FrontController

  ajouter un init-param
* param-name : package_name
* p16.controller

    dans un servlet-mapping

* servlet-name
* url-pattern : /
