package com.gravitysim;

import java.awt.Color;
import java.util.concurrent.Semaphore;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

public abstract class Simulation implements Runnable {

    public Body[] body;
    public Semaphore simCount;
    public Thread simulationThread;
    public int n; //number of bodies

    public Simulation() {
        this(10); // make 10 random bodies by default
    }
    
    public Simulation(int nBodies) {
        n = nBodies;
        simCount = new Semaphore(5);
        body = new Body[n];
        for(int i = 0; i<n; i++){
            body[i] = new Body(GravitySimTwoD.nDim);
            body[i].randomise();
        }
        simulationThread = new Thread(null, this, "simulation");
        simulationThread.start();
    }
    
    public Simulation(Body[] toDupe) {
        n = toDupe.length;
        simCount = new Semaphore(5);
        this.body = new Body[n];
        for (int i = 0; i < n; i++) {
            body[i] = new Body(toDupe[i]);
        }
        simulationThread = new Thread(null, this, "simulation");
        simulationThread.start();
    }
    
    abstract void update();
    
    public void render(GLAutoDrawable drawable, Color colour) {
        double x = 0;
        double y = 0;
        double radius = 0;
        double q = 0;
        double r = 0;
        double angle = 0;
        int red = colour.getRed(), blue = colour.getBlue(), green = colour.getGreen();
        GL2 gl = drawable.getGL().getGL2();
        for(int j = 0; j<n; j++){
            x = (2*body[j].currentR[0])/GravitySimTwoD.width;
            y = (2*body[j].currentR[1])/GravitySimTwoD.width;
            radius = (2*body[j].radius)/GravitySimTwoD.width;
            gl.glBegin(GL2.GL_POLYGON);
            gl.glColor3d(red/255, green/255, blue/255);
            for(int i =0; i <= 300; i++){
                angle = 2 * Math.PI * i / 300;
                q = radius*Math.cos(angle);
                r = radius*Math.sin(angle);
                gl.glVertex2d(q+x,r+y);
            }
            gl.glEnd();
        }
    }
    
    public void run() {
        while (true) {
            while (simCount.availablePermits() < 5) {
                update();
                simCount.release();
            }
        }
    }
    
}
