package object;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import utils.Mapping;

public class VerbMethod {
    String verb;
    Method methode;
    public String getVerb() {
        return verb;
    }
    public void setVerb(String verb) {
        this.verb = verb;
    }
    public Method getMethode() {
        return methode;
    }
    public void setMethode(Method methode) {
        this.methode = methode;
    }
    public VerbMethod() {
    }
    public VerbMethod(String verb, Method methode) {
        setVerb(verb);
        setMethode(methode);
    }

    @Override
    public boolean equals(Object o) {
        if(this.getClass().equals(o.getClass())){
            VerbMethod v=(VerbMethod) o;
            if(this.getVerb().equals(v.getVerb())){
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(verb);
    }


    /* public static void main(String[] args) throws NoSuchMethodException, SecurityException, ClassNotFoundException {
        Set<VerbMethod> sets=new HashSet<VerbMethod>();
        VerbMethod v1=new VerbMethod();
        v1.setVerb("GET");
        sets.add(v1);
        VerbMethod v2=new VerbMethod();
        v2.setVerb("GET");
        v2.setMethode(Class.forName("java.util.List").getMethod("size", (Class[])null));
        System.out.println(sets.add(v2));
    } */
}
