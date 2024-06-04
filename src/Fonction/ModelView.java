package Fonction;

import java.util.HashMap;


public class ModelView {
    String url;
    HashMap<String,Object> data;

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public HashMap<String, Object> getData() {
        return data;
    }
    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }
    public ModelView() {
        this.data = new HashMap<String, Object>();
    }
    public ModelView(String url, HashMap<String, Object> data) {
        this.url = url;
        this.data = data;
    }
    public void add(String cle, Object valeur){
        this.data.put(cle, valeur);
    }
}
