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
public class Voyageur implements Serializable
{
    private int numeroID;
    private String nom;
    private String prenom;

    public Voyageur(int numeroID, String nom, String prenom) {
        this.numeroID = numeroID;
        this.nom = nom;
        this.prenom = prenom;
    }

    public int getNumeroID() {
        return numeroID;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }
    
}
