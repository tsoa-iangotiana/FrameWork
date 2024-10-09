package Fonction;

public class Mapping {
    String className;
    String methodName;
    VerbAction verb;

    public Mapping(String className, String methodName,VerbAction verb) {
        this.className = className;
        this.methodName = methodName;
        this.verb=verb;
    }
    public VerbAction getVerb() {
        return verb;
    }
    public void setVerb(VerbAction verb) {
        this.verb = verb;
    }
    public String getClassName() {
        return className;
    }
    public void setClassName(String className) {
        this.className = className;
    }
    public String getMethodName() {
        return methodName;
    }
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
}
