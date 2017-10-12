/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.util.LinkedList;

/**
 *
 * @author Sadik
 */
public class TasksList implements TasksSource
{
    private LinkedList listeTaches;
    public TasksList()
    {
        listeTaches = new LinkedList();
    }
    
    public synchronized Runnable getTache() throws InterruptedException
    {
        while (!existTaches()) wait();
        return (Runnable)listeTaches.remove();
    }
    
    public synchronized boolean existTaches()
    {
        return !listeTaches.isEmpty();
    }
    public synchronized void recordTache (Runnable r)
    {
        listeTaches.addLast(r);
        notify();
    }
}
