/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.util.Date;

/**
 *
 * @author Sadik
 */
public class Fly 
{
    private int idVol;
    private String destination;
    private Date arriveeEventuelle;
    private Date depart;
    private int place;
    private int prix;
    public Fly(int idVol, String destination, Date arriveeEventuelle, Date depart, int place,int prix) {
        this.idVol = idVol;
        this.destination = destination;
        this.arriveeEventuelle = arriveeEventuelle;
        this.depart = depart;
        this.place = place;
        this.prix = prix;
    }

    public int getPrix() {
        return prix;
    }

    public void setPrix(int prix) {
        this.prix = prix;
    }

    public Fly() {
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

    public Date getArriveeEventuelle() {
        return arriveeEventuelle;
    }

    public void setArriveeEventuelle(Date arriveeEventuelle) {
        this.arriveeEventuelle = arriveeEventuelle;
    }

    public Date getDepart() {
        return depart;
    }

    public void setDepart(Date depart) {
        this.depart = depart;
    }

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
    }
}
