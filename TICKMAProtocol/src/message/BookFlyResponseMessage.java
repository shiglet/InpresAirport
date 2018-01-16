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
public class BookFlyResponseMessage implements Message
{
    byte[] data;

    public BookFlyResponseMessage(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

   
}
