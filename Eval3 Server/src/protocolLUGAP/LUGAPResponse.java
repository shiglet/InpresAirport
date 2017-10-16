/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package protocolLUGAP;

import messageinterface.Response;

/**
 *
 * @author Sadik
 */
public class LUGAPResponse implements Response{
    private final int code;
    public LUGAPResponse(int c)
    {
        code = c;
    }
    
    @Override
    public int getCode() {
        return code;
    }
    
}
