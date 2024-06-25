package utils;

import java.util.HashMap;

public class ModelView {
    
    String url;
    HashMap <String,Object> data;

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }
    public HashMap<String,Object> getData() {
        return data;
    }

    public void add(String nomData,Object data){
        if (this.getData()==null) {
          this.setData(new HashMap<String , Object>());
        }
        HashMap <String , Object> map = this.getData();
        map.put(nomData,data);
    } 

}
