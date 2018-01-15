/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package message;

import javax.crypto.SecretKey;

/**
 *
 * @author Sadik
 */
public class HandshakeMessage implements Message
{
    private byte[] authenticationK;
    private byte[] cipherK;

    public byte[] getAuthenticationK() {
        return authenticationK;
    }

    public void setAuthenticationK(byte[] authenticationK) {
        this.authenticationK = authenticationK;
    }

    public byte[] getCipherK() {
        return cipherK;
    }

    public void setCipherK(byte[] cipherK) {
        this.cipherK = cipherK;
    }

    public HandshakeMessage(byte[] authenticationK, byte[] cipherK) {
        this.authenticationK = authenticationK;
        this.cipherK = cipherK;
    }
}
