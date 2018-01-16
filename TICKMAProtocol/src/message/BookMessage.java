/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package message;

import interfaces.Message;

/**
 *
 * @author Sadik
 */
public class BookMessage implements Message
{
    byte[] idVol;
    byte[] vVoyageur;

    public byte[] getIdVol() {
        return idVol;
    }

    public void setIdVol(byte[] idVol) {
        this.idVol = idVol;
    }

    public byte[] getvVoyageur() {
        return vVoyageur;
    }

    public void setvVoyageur(byte[] vVoyageur) {
        this.vVoyageur = vVoyageur;
    }

    public BookMessage(byte[] idVol, byte[] vVoyageur) {
        this.idVol = idVol;
        this.vVoyageur = vVoyageur;
    }
}
