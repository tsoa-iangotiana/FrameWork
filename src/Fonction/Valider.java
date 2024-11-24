package Fonction;

import java.lang.reflect.Field;

import Annotation.validation.Email;
import Annotation.validation.Numerique;
import Annotation.validation.Required;

public class Valider {
    public boolean isValidate (Field attr,String valeur) throws Exception {
        boolean result = true;
        if (attr.isAnnotationPresent(Numerique.class)) {
            if (!valeur.matches("-?\\d+(\\.\\d+)?")){
                result = false;
                throw new Exception("L'input de l'attribut "+attr.getName()+" doit etre de type numerique ");
            }
        }
        if (attr.isAnnotationPresent(Email.class)){
            if (!valeur.contains("@")){
                result = false;
                throw new Exception("L'input de l'attribut "+attr.getName()+" doit etre de type email ");
            }
        }
        
        if (attr.isAnnotationPresent(Required.class)){
            if (valeur != null || !valeur.equals("")) {
                result = false;
                throw new Exception("L'input de l'attribut "+attr.getName()+" doit etre completer ");
            }
        }

        return result;
    }
}
