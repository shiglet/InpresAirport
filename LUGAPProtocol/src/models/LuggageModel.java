/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import java.io.Serializable;

/**
 *
 * @author Sadik
 */
public class LuggageModel implements Serializable
{
    private String idBaggages;
    private double poids;
    private String valise;
    private String numeroBillet;
    private String receptionne;
    private String charge;
    private String douane;
    private String remarques;

    public String getIdBaggages() {
        return idBaggages;
    }

    public void setIdBaggages(String idBaggages) {
        this.idBaggages = idBaggages;
    }

    public double getPoids() {
        return poids;
    }

    public void setPoids(double poids) {
        this.poids = poids;
    }

    public String getValise() {
        return valise;
    }

    public void setValise(String valise) {
        this.valise = valise;
    }

    public String getNumeroBillet() {
        return numeroBillet;
    }

    public void setNumeroBillet(String numeroBillet) {
        this.numeroBillet = numeroBillet;
    }

    public String getReceptionne() {
        return receptionne;
    }

    public void setReceptionne(String receptionne) {
        this.receptionne = receptionne;
    }

    public String getCharge() {
        return charge;
    }

    public void setCharge(String charge) {
        this.charge = charge;
    }

    public String getDouane() {
        return douane;
    }

    public void setDouane(String douane) {
        this.douane = douane;
    }

    public String getRemarques() {
        return remarques;
    }

    public void setRemarques(String remarques) {
        this.remarques = remarques;
    }
    public LuggageModel(String id,double p,String v,String d, String r, String c,String b,String recep)
    {
        idBaggages = id;
        poids = p;
        valise = v;
        douane = d;
        remarques = r;
        numeroBillet = b;
        charge = c;
        receptionne = recep;
    }
}
