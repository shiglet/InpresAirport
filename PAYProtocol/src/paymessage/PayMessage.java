package paymessage;


import interfaces.Message;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Sadik
 */
public class PayMessage implements Message
{
    public static final int REQUEST_PAYEMENT = 1;
    private int type;
    private byte[] carte;
    private byte[] nom; 
    private byte[] total;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public byte[] getCarte() {
        return carte;
    }

    public void setCarte(byte[] carte) {
        this.carte = carte;
    }

    public byte[] getNom() {
        return nom;
    }

    public void setNom(byte[] nom) {
        this.nom = nom;
    }

    public byte[] getTotal() {
        return total;
    }

    public void setTotal(byte[] total) {
        this.total = total;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public PayMessage(int type, byte[] carte, byte[] nom, byte[] total, byte[] signature) {
        this.type = type;
        this.carte = carte;
        this.nom = nom;
        this.total = total;
        this.signature = signature;
    }
    private byte[] signature;
   
}
