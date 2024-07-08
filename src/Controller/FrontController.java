package Controller;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import Annotation.Get;
import Fonction.ListClasse;
import Fonction.Mapping;
import Fonction.ModelView;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {
HashMap<String, Mapping> urlMappings = new HashMap<>();
ArrayList<Class<?>> controllers;

    // getter et setter
    public ArrayList<Class<?>> getControllers() {
        return controllers;
    }

    public void setControllers(ArrayList<Class<?>> controllers) {
        this.controllers = controllers;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        String packageName = this.getInitParameter("Controller_Package");
        if (packageName == null || packageName.isEmpty()) {
            throw new ServletException("Le paramètre 'Controller_Package' est manquant ou vide");
        }
    
        try {
            this.setControllers(ListClasse.getAllClasses(packageName));
    
            for (Class<?> controller : this.getControllers()) {
                for (Method method : controller.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(Get.class)) {
                        String className = controller.getName();
                        String methodName = method.getName();
                        Get getAnnotation = method.getAnnotation(Get.class);
                        String url = getAnnotation.value();
    
                        if (urlMappings.containsKey(url)) {
                            throw new ServletException("URL en double détectée: " + url + " pour " + className + "#" + methodName);
                        }
    
                        Mapping mapping = new Mapping(className, methodName);
                        urlMappings.put(url, mapping);
                    }
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            throw new ServletException("Erreur lors du scan des contrôleurs", e);
        }
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            processRequest(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            processRequest(req, resp);
        } catch (Exception e) {
            
            e.printStackTrace();
        }
    }
    
    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        PrintWriter out = resp.getWriter();
        String url = req.getServletPath();
        try {
            // Check URL
            Mapping mapping = urlMappings.get(url);
            if (mapping == null) {
                // L'URL n'est pas dans le mapping, afficher un message d'erreur
                resp.setContentType("text/html");
                throw new ServletException("404 Not Found . Url indisponible");
            }
    
            // Récupération nom_contrôleur et méthode
            String controllerName = mapping.getClassName();
            String methodName = mapping.getMethodName();
            // Créer une instance du contrôleur
            Class<?> controllerClass = Class.forName(controllerName);
            Object controllerInstance = controllerClass.newInstance();
            // Récuperation de la méthode à appeler
            Method method = null;
            for (Method m : controllerClass.getMethods()){
                if (m.getName().equals(methodName)) {
                    method = m;
                    break;
                }
            }
            if (method == null) {
                throw new NoSuchMethodException(controllerClass.getName() + "." + methodName + "()");
            }
            Object result;
            // Vérifier si la méthode possède des paramètres
            if (method.getParameterCount() > 0) {
                ArrayList<Object> parameterValues = ListClasse.getParameterValuesCombined(method, req);
                if (parameterValues.size() != method.getParameterCount()) {
                    throw new IllegalArgumentException("nombre de paramètres envoyés différents des paramètres de la méthode");
                }
                result = method.invoke(controllerInstance, parameterValues.toArray());
            } else {
                result = method.invoke(controllerInstance);
            }
    
            // Vérifier le type d'objet retourné
            if (result instanceof String || result instanceof ModelView) {
                // Le type d'objet retourné est valide, continuer le traitement
                if (result instanceof ModelView) {
                    ModelView modelView = (ModelView) result;
                    String viewUrl = modelView.getUrl();
                    HashMap<String, Object> data = modelView.getData();
    
                    // Définir les données en tant qu'attributs de requête
                    for (String key : data.keySet()) {
                        req.setAttribute(key, data.get(key));
                    }
    
                    RequestDispatcher dispat = req.getRequestDispatcher(viewUrl);
                    dispat.forward(req, resp);
    
                } else { // Si le résultat n'est pas une instance de ModelView, utiliser le code existant
                    resp.setContentType("text/html");
                    out.println("<h2>Test sprint 3 </h2>");
                    out.println("<p><strong>Contrôleur</strong> : " + controllerName + "</p>");
                    out.println("<p><strong>Méthode</strong> : " + methodName + "</p>");
                    out.println("<p><strong>Execution de la fonction "+ methodName +" :</strong></p>");
                    out.println(result.toString());
                }
            } else {
                // Le type d'objet retourné n'est pas valide, lancer une exception TypeException
                throw new ServletException("Erreur: Le type d'objet retourné n'est pas valide (String ou ModelView attendu)");
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new ServletException("Erreur lors de l'exécution de la méthode", e);
        } catch (Exception e) {
            out.println(e.getLocalizedMessage());
        }
    }    

}
