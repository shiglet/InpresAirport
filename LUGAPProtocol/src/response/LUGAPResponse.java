/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package response;
import interfaces.Response;
import java.io.Serializable;
import java.util.Vector;
import models.LuggageModel;


/**
 *
 * @author Sadik
 */
public class LUGAPResponse implements Response,Serializable{

    public static final short LOGIN_FAILED = 2;
    private final int code;
    public static final short LOGIN_SUCCESS = 1;
    public static final short FLYLIST_SUCCESS = 3;
    public static final short LUGGAGE_SUCCESS = 4;
    public static final short UPDATE_SUCCESS = 5;
    public static final short UPDATE_FAILED = 6;
    public static final short LOGOUT_SUCCESS = 7;
    public static final short LOGOUT_FAILED = 8;
    public String message;
    public Vector<String> vData;
    private Vector<LuggageModel> vLuggages;

    public Vector<LuggageModel> getvLuggages() {
        return vLuggages;
    }

    public void setvLuggages(Vector<LuggageModel> vLuggages) {
        this.vLuggages = vLuggages;
    }
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
