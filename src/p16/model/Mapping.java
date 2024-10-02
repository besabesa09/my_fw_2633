package p16.model;

public class Mapping {
    String className;
    String methodName;
    String verb;

    public Mapping(){}
    
    public Mapping(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
    }

    public Mapping(String className, String methodName,String verb) {
        this.className = className;
        this.methodName = methodName;
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
    public String getVerb() {
        return verb;
    }
    
    public void setVerb(String verb) {
        this.verb = verb;
    }
}
