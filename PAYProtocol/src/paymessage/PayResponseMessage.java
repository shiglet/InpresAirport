/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paymessage;

import interfaces.Message;

/**
 *
 * @author Sadik
 */
public class PayResponseMessage implements Message
{
    public static final int SUCCESS = 1;
    public static final int FAILED = 1;
    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public PayResponseMessage(int type) {
        this.type = type;
    }
}
