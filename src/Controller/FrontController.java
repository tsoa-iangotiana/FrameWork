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
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {
HashMap<VerbAction, Mapping> urlMappings = new HashMap<>();
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
                    if(method.isAnnotationPresent(Url.class)){
                        String className = controller.getName();
                        String methodName = method.getName();
                        // Get verb= method.getAnnotation(Get.class);
                        Url getAnnotation = method.getAnnotation(Url.class);
                        String url = getAnnotation.value();
                        // VerbAction verb =new VerbAction(url, "GET" );
                        VerbAction verb =null;
                        if (method.isAnnotationPresent(Get.class)) {
                            verb = new VerbAction(url, "GET");
                    }
                    else if(method.isAnnotationPresent(Post.class)){
                        verb = new VerbAction(url, "POST");
                    }
                          
                        //   if (urlMappings.containsKey(url)) {
                        //         throw new ServletException("URL en double détectée: " + url + " pour " + className + "#" + methodName);
                        //     }
                        if (verb!= null) {
                            Mapping mapping = new Mapping(className, methodName,verb);
                            urlMappings.put(verb, mapping);
                        }
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
    
    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String url = req.getServletPath();
        PrintWriter out = resp.getWriter();
        String requestedVerb = req.getMethod();
        VerbAction verb = new VerbAction(url, requestedVerb);
        Mapping mapping = urlMappings.get(verb);
        if (mapping == null) {
            resp.setContentType("text/html");
            // out.println(mapping.getMethodName());
            out.println("<h2>Erreur: L'URL demandée n'est pas disponible!</h2>");
            return;
        }
        String controllerName = mapping.getClassName();
        String methodName = mapping.getMethodName();
        if (!requestedVerb.equalsIgnoreCase(verb.getVerb())) {
            resp.setContentType("text/html");
            out.println("<h2>Erreur: Le verbe HTTP " + requestedVerb + " ne correspond pas à l'annotation " + mapping.getVerb() + " pour " + mapping.getClassName() + "#" + mapping.getMethodName() + "</h2>");
            return;
        }
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
    }
    catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new ServletException("Erreur lors de l'exécution de la méthode", e);
        }catch (Exception e){
            out.println(e.getLocalizedMessage());
        }
    }
    

}
