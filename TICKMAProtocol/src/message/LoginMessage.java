/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package message;

/**
 *
 * @author Sadik
 */
public class LoginMessage implements Message
{
    private double random;
    private byte[] digest;
    private String login;
    private long time;
    
    public double getRandom() {
        return random;
    }

    public void setRandom(double random) {
        this.random = random;
    }

    public byte[] getDigest() {
        return digest;
    }

    public void setDigest(byte[] digest) {
        this.digest = digest;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public LoginMessage(double random, byte[] digest, String login, long time) {
        this.random = random;
        this.digest = digest;
        this.login = login;
        this.time = time;
    }
    
    
}
