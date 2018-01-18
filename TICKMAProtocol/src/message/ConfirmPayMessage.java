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
public class ConfirmPayMessage implements Message
{
    public static final int SUCCESS=1;
    public static final int FAILED =2;
    private int type;
    private byte[] hmac;
    private byte[] carte;
    private String login;

    public String getLogin() {
        return login;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public byte[] getHmac() {
        return hmac;
    }

    public void setHmac(byte[] hmac) {
        this.hmac = hmac;
    }

    public byte[] getCarte() {
        return carte;
    }

    public void setCarte(byte[] carte) {
        this.carte = carte;
    }

    public ConfirmPayMessage(int type, byte[] hmac, byte[] carte) {
        this.type = type;
        this.hmac = hmac;
        this.carte = carte;
    }
    public ConfirmPayMessage(int type, byte[] hmac, byte[] carte,String login) {
        this.type = type;
        this.hmac = hmac;
        this.carte = carte;
        this.login = login;
    }
}
