/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package message;

import java.util.Vector;
import models.Fly;

/**
 *
 * @author Sadik
 */
public class FlyMessage implements Message 
{
    private Vector<Fly> flies;

    public FlyMessage(Vector<Fly> flies) {
        this.flies = flies;
    }

    public Vector<Fly> getFlies() {
        return flies;
    }

    public void setFlies(Vector<Fly> flies) {
        this.flies = flies;
    }
    
}
