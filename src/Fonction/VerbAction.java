package Fonction;

public class VerbAction {
    private String method;
    private String verb;

    public VerbAction(String method, String verb) {
        this.method = method;
        this.verb = verb;
    }

    public String geMethod() {
        return method;
    }

    public void seMethod(String method) {
        this.method = method;
    }

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

}

