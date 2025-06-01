package org.example.citrixcontrolrest.utils;

import org.example.citrixcontrolrest.model.CitrixSiteDTO;
import org.example.citrixcontrolrest.model.Config;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Serializar implements Serializable {

    public void guardarSites(List<Config> granjas){
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("src/main/resources/config/site.ser"))){
            oos.writeObject(granjas);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Config> leerSites (){
        List<Config> site = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("src/main/resources/config/site.ser"))){
            site = (List<Config>) ois.readObject();
        } catch (Exception e) {
        }
        return site;
    }
}
