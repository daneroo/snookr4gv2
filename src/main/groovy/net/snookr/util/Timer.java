/*
 * Timer.java
 *
 * Created on August 27, 2007, 1:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.snookr.util;

/**
 *
 * @author daniel
 */

public class Timer { //measures things in seconds.
    private long startTime;
    /** Creates a new instance of Timer */
    public Timer() { restart(); }
    public void restart() { 
	startTime = System.currentTimeMillis(); 
    }
    public float diff() {
	return (System.currentTimeMillis()-startTime)/1000f;  
    }
    public float rate(int n){ 
	return n/diff(); 
    }
}
