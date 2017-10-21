/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package response;
import interfaces.Response;
import java.io.Serializable;
import java.util.Vector;


/**
 *
 * @author Sadik
 */
public class LUGAPResponse implements Response,Serializable{

    public static final short LOGIN_FAILED = 2;
    private final int code;
    public static final short LOGIN_SUCCESS = 1;
    public static final short FLYLIST_SUCCESS = 3;
    public String message;
    public Vector<String> vData;
    public LUGAPResponse(int c)
    {
        code = c;
    }
    public LUGAPResponse(int c,String m)
    {
        code = c;
        message = m;
    }
    public LUGAPResponse(int c,String m,Vector<String> v)
    {
        code = c;
        message = m;
        vData = v ;
    }
    
    @Override
    public int getCode() {
        return code;
    }
    
}
