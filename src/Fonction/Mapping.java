package Fonction;

public class Mapping {
    String className;
    String methodName;
    public Mapping(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
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
