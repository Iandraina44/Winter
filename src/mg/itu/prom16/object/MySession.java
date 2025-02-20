package mg.itu.prom16.object;

import jakarta.servlet.http.HttpSession;

public class MySession {
    HttpSession session;

    public HttpSession getSession() {
        return session;
    }

    public void setSession(HttpSession session) {
        this.session = session;
    }

    

    public MySession(HttpSession session) {
        this.setSession(session);
    }

    public MySession() {
    }

    public Object get(String key){
        return session.getAttribute(key);
    }

    public void add(String key , Object o){
        this.getSession().setAttribute(key, o);
    }

    public void delete(String key){
        this.getSession().removeAttribute(key);
    }
}
