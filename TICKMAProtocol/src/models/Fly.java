/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 *
 * @author Sadik
 */
public class Fly implements Serializable 
{
    private int idVol;
    private String destination;
    private int prix;
    private int placeRestante;

    @Override
    public String toString() {
        return "Fly{" + "idVol=" + idVol + ", destination=" + destination + ", prix=" + prix + ", dateDepart=" + dateDepart + ", depart=" + depart + '}';
    }

    public int getIdVol() {
        return idVol;
    }

    public void setIdVol(int idVol) {
        this.idVol = idVol;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getPrix() {
        return prix;
    }

    public void setPrix(int prix) {
        this.prix = prix;
    }

    public Timestamp getDateDepart() {
        return dateDepart;
    }

    public void setDateDepart(Timestamp dateDepart) {
        this.dateDepart = dateDepart;
    }

    public String getDepart() {
        return depart;
    }

    public void setDepart(String depart) {
        this.depart = depart;
    }

    public Fly(int idVol, String destination, int prix, Timestamp dateDepart, String depart,int placeRestante) {
        this.idVol = idVol;
        this.destination = destination;
        this.prix = prix;
        this.dateDepart = dateDepart;
        this.depart = depart;
        this.placeRestante = placeRestante;
    }

    public int getPlaceRestante() {
        return placeRestante;
    }

    public void setPlaceRestante(int placeRestante) {
        this.placeRestante = placeRestante;
    }
    private Timestamp dateDepart;
    private String depart;
}
