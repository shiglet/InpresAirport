/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import java.util.Vector;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author Sadik
 */
public class TableItemModel extends AbstractTableModel
{
    private Vector<LuggageModel> vLuggages;
    private String[] columNames;
    public TableItemModel(Vector<LuggageModel> v)
    {
        vLuggages = v;
        columNames = new String[] {"Identifiant","Poids","Type","Receptionné (O/N)","Chargé en soute (O/N)","Vérifié par la douane (O/N)","Remarques"};
    }
    @Override
    public int getColumnCount() 
    { 
        return columNames.length;
    }
    @Override
    public int getRowCount() 
    { 
        return vLuggages.size();
    }
    @Override
    public String getColumnName(int col)
    {
        return columNames[col];
    }
    @Override
    public Object getValueAt(int row, int col) 
    {
        switch(col)
        {
            case 0: 
                return vLuggages.elementAt(row).getNumeroBillet()+"/"+vLuggages.elementAt(row).getIdBaggages();
            case 1: 
                return vLuggages.elementAt(row).getPoids();
            case 2: 
                return vLuggages.elementAt(row).getValise();
            case 3: 
                return vLuggages.elementAt(row).getReceptionne();
            case 4: 
                return vLuggages.elementAt(row).getCharge();
            case 5: 
                return vLuggages.elementAt(row).getDouane();
            case 6: 
                return vLuggages.elementAt(row).getRemarques();
        }
        return "??";
    }
}



