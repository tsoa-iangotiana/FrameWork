package Controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;

import Annotation.Get;
import Annotation.Post;
import Annotation.RestAPI;
import Annotation.Url;
import Fonction.ListClasse;
import Fonction.Mapping;
import Fonction.ModelView;
import Fonction.VerbAction;
import SprintException.ExceptionVerb;
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

    public void setUrlMappings(HashMap<String, Mapping> urlMappings) {
        this.urlMappings = urlMappings;
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
                    if(method.isAnnotationPresent(Url.class)){
                        String className = controller.getName();
                        String methodName = method.getName();
                        // Get verb= method.getAnnotation(Get.class);
                        // VerbAction verb =new VerbAction(url, "GET" );
                        String verb = "GET";
                        if (method.isAnnotationPresent(Get.class)) {
                            verb = "POST";
                        }
                        else if(method.isAnnotationPresent(Post.class)){
                            verb = "GET";
                        }
                        Url getAnnotation = method.getAnnotation(Url.class);
                        String url = getAnnotation.value();
                          
                        //   if (urlMappings.containsKey(url)) {
                        //         throw new ServletException("URL en double détectée: " + url + " pour " + className + "#" + methodName);
                        //     }
                        Mapping mapping = urlMappings.get(url);
                        if (mapping == null) {
                            Mapping map = new Mapping(className);
                            map.addVerbAction(new VerbAction(methodName, verb));
                            urlMappings.put(url, map);
                        }
                        else{
                            boolean verbExists = false;
                            for (VerbAction existingVerbAction : mapping.getVerbAction()) {
                                if (existingVerbAction.getVerb().compareToIgnoreCase(verb) == 0) {
                                    verbExists = true;
                                    break;
                                }
                            }

                            if (!verbExists) {
                                // Ajouter un nouveau Verbe/Action s'il n'existe pas
                                VerbAction verbAction = new VerbAction(methodName, verb);
                                mapping.addVerbAction(verbAction);
                            } else {
                                throw new Exception("Erreur: L'URL " + url + " avec le verbe " + verb + " est déjà utilisée");
                            }
                        }
                        }
                }
            }
            this.setUrlMappings(urlMappings);
        } catch (ClassNotFoundException | IOException e) {
            throw new ServletException("Erreur lors du scan des contrôleurs", e);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
    
    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String url = req.getServletPath();
        PrintWriter out = resp.getWriter();
        String requestedVerb = req.getMethod();
        PrintWriter aff= resp.getWriter();
        // VerbAction verb = new VerbAction(url, requestedVerb);
        Mapping mapping = urlMappings.get(url);
        if (mapping == null) {
           String error = "404 Not found: URL indisponible hihi";
           aff.println(error);
           resp.sendError(HttpServletResponse.SC_NOT_FOUND,error);
        }
        VerbAction verbAction = null;
        for(VerbAction va : mapping.getVerbAction()){
            if(va.getVerb().compareToIgnoreCase(requestedVerb)==0 ){
                verbAction =va;
                break;
            }
        }
        if(verbAction == null) {
            String error = "Erreur le verbe" +requestedVerb+"n'est pas disponible";
            aff.println(error);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND,error);
            return;
        }
        String controllerName = mapping.getClassName();
        String methodName = verbAction.geMethod();

        try {
            Class<?> controllerClass = Class.forName(controllerName);
            Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
            Method method = null;
            for (Method m : controllerClass.getMethods()) {
                if (m.getName().equals(methodName)) {
                    method = m;
                    break;
                }
            }
            if (method == null) {
                throw new NoSuchMethodException(controllerClass.getName() + "." + methodName + "()");
            }
    
            // Parameter[] parameters = method.getParameters();
            // if (parameters.length > 0) {
            //     ArrayList<Object> values = ListClasse.ParameterMethod(method, req);
            //     if (values.size() != parameters.length) {
            //         throw new IllegalArgumentException("Nombre d'arguments incorrect pour la méthode " + method);
            //     }
            //     result = method.invoke(controllerInstance, values.toArray());
            // } else {
            //     result = method.invoke(controllerInstance);
            // }
    
            Object result;
            if(method.getParameterCount() > 0){
                ArrayList<Object> parameterValues = ListClasse.getParameterValuesCombined(method, req);
                if (parameterValues.size() != method.getParameterCount()) {
                    throw new IllegalArgumentException("Nombre d'argument incorrect pour la methode" + method);
                }
                result = method.invoke(controllerInstance,parameterValues.toArray());
            }else{
                result = method.invoke(controllerInstance);
            }
            boolean isRestApi = method.isAnnotationPresent(RestAPI.class);
         if (isRestApi) {
            // La méthode est annotée avec @RestApi, on traite le résultat en JSON
            resp.setContentType("application/json");

            if (result instanceof ModelView) {
                // Transformer le 'data' du ModelView en JSON
                ModelView modelView = (ModelView) result;
                HashMap<String, Object> data = modelView.getData();
                String json = new Gson().toJson(data);
                out.print(json);
            } else {
                // Transformer directement le résultat en JSON
                String json = new Gson().toJson(result);
                out.print(json);
            }
        } else {
            if (result instanceof ModelView) {
                ModelView modelView = (ModelView) result;
                String viewUrl = modelView.getUrl();
                HashMap<String, Object> data = modelView.getData();
                for (String key : data.keySet()) {
                    req.setAttribute(key, data.get(key));
                }
                req.getRequestDispatcher(viewUrl).forward(req, resp);
            } else if (result instanceof String) {
                resp.setContentType("text/html");
                out.println("<h2>Sprint 2 </h2><br>");
                out.println("<p>Lien : " + url + "</p>");
                out.println("<p>Contrôleur : " + controllerName + "</p>");
                out.println("<p>Méthode : " + methodName + "</p>");
                out.println("<p>Résultat : " + result.toString() + "</p>");
            } else {
                throw new ServletException("Le type de retour de la méthode est invalide");
            }
        } 
    }catch(ExceptionVerb e){
        resp.sendError(HttpServletResponse.SC_NOT_FOUND,e.getLocalizedMessage());
    }
    catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new ServletException("Erreur lors de l'exécution de la méthode", e);
        }catch (Exception e){
            out.println(e.getLocalizedMessage());
        }
    }
    

}
