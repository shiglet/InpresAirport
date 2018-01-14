/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package response;

import interfaces.Response;
import java.io.Serializable;

/**
 *
 * @author Sadik
 */
public class TICKMAPResponse implements Response, Serializable
{

    public static int SUCCESS = 1;
    public static int FAILED = 2;

    private int code;
    private String message;
    
    public String getMessage()
    {
        return message;
    }
    
    public TICKMAPResponse(int code)
    {
        this.code = code;
    }
    
    public TICKMAPResponse(int code,String message)
    {
        this.code = code;
        this.message = message;
    }
    
    @Override
    public int getCode() 
    {
        return code;
    }
    
}
