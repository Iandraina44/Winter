package mg.itu.prom16.utils;
import java.util.Objects;
import java.util.Set;

import mg.itu.prom16.object.VerbMethod;

public class Mapping {
    String className;
    Set<VerbMethod> verbmethods;
    public String getClassName() {
        return className;
    }
    public void setClassName(String className) {
        this.className = className;
    }
    public Set<VerbMethod> getVerbmethods() {
        return verbmethods;
    }
    public void setVerbmethods(Set<VerbMethod> verbmethods) {
        this.verbmethods = verbmethods;
    }
    public Mapping() {
    }
    public Mapping(String className, Set<VerbMethod> verbmethods) {
        setClassName(className);
        setVerbmethods(verbmethods);
    }
}
