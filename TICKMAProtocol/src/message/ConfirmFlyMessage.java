/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package message;

import interfaces.Message;
import java.io.Serializable;

/**
 *
 * @author Sadik
 */
public class ConfirmFlyMessage implements Message
{
    byte[] hmac;

    public ConfirmFlyMessage(byte[] hmac, String message) {
        this.hmac = hmac;
        this.message = message;
    }

    public byte[] getHmac() {
        return hmac;
    }

    public void setHmac(byte[] hmac) {
        this.hmac = hmac;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    String message;
}
