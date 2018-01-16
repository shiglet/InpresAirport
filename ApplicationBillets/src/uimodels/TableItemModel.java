/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uimodels;

import java.text.SimpleDateFormat;
import java.util.Vector;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import models.Fly;

/**
 *
 * @author Sadik
 */
public class TableItemModel extends AbstractTableModel
{
    private Vector<Fly> vFly;
    private String[] columNames;
    public TableItemModel(Vector<Fly> v)
    {
        vFly = v;
        columNames = new String[] {"Depart - Destination","Heure de départ","Prix par place","Places restantes"};
    }
    public Fly getFlyAt(int i)
    {
        return vFly.elementAt(i);
    }
    @Override
    public int getColumnCount() 
    { 
        return columNames.length;
    }
    @Override
    public int getRowCount() 
    { 
        return vFly.size();
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
                return vFly.elementAt(row).getDepart()+" - "+vFly.elementAt(row).getDestination();
            case 1: 
                return  new SimpleDateFormat("dd/MM/yyyy hh:mm").format(vFly.elementAt(row).getDateDepart());
            case 2: 
                return vFly.elementAt(row).getPrix()+" €";
            case 3: 
                return vFly.elementAt(row).getPlaceRestante();
        }
        return "??";
    }
    @Override
    public boolean isCellEditable(int row, int column) {
       return false;
    }
}



