package com.gravitysim;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;

import com.jogamp.opengl.util.FPSAnimator;


public class GravitySimTwoD implements GLEventListener{
	
	private static int n = 10; //number of bodies
	private static int nDim = 2; //number of spatial dimensions
	private double timeInt = 60; //time interval, (s)
	private int updatesPerRefresh = 250;//how many times it calculates before updating the display
	private static double density = 3;//5520 = earth
	private static double width = 5000;//width of space
	private double d = 2*width/3; //width of center square
	private static double G = 6.673e-11;
	private boolean slow = true;//turn methods on and off
	private boolean normal = true;
	
	//Min and max values for random start
	private static double minMass = 1e3;
	private static double maxMass = 1e5;
	private static  double minDist = -width/2;
	private static  double maxDist = width/2;
	private static  double minVel = 0.01*Math.sqrt(G);
	private static  double maxVel = 5*Math.sqrt(G);
	
	//Arrays to track position and velocity for normal method
	private static double[][] currentR = new double[n][nDim];
	private static double[][] nextR = new double[n][nDim];
	private static double[][] currentV = new double[n][nDim];
	private static double[][] nextV = new double[n][nDim];
	
	//duplicate arrays for the slow method
	private static double[][] currentSR = new double[n][nDim];
	private static double[][] nextSR = new double[n][nDim];
	private static double[][] currentSV = new double[n][nDim];
	private static double[][] nextSV = new double[n][nDim];
	
	//arrays for masses and radii
	private static double[] radii = new double[n];
	private static double[] mass = new double[n];

	

	


	public static void main(String[] args){
		//random start
		Random rand = new Random();
		for(int i = 0; i<n; i++){
			mass[i] = minMass+(maxMass-minMass)*rand.nextDouble();
			radii[i] = Math.sqrt(mass[i]/(Math.PI*density));
			for(int j = 0; j<nDim; j++){
				currentR[i][j] = minDist+(maxDist-minDist)*rand.nextDouble();
				currentV[i][j] = minVel+(maxVel-minVel)*rand.nextDouble();
				currentSR[i][j] = currentR[i][j];
				currentSV[i][j] = currentV[i][j];
			}
		}
		//make the window +openGL stuff
		Frame frame = new Frame("Gravity Simulation.");
		GLProfile glp = GLProfile.getDefault();
		GLCapabilities caps = new GLCapabilities(glp);
		GLCanvas canvas = new GLCanvas(caps);
		frame.setSize(700,700);
		frame.add(canvas);
		frame.setVisible(true);  
		canvas.addGLEventListener(new GravitySimTwoD()); 
		FPSAnimator animator = new FPSAnimator(canvas, 60);
		animator.start();
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}

	public void display(GLAutoDrawable drawable) {
	    // make it update and draw
		if(slow == true){
			updateSlow();
			renderSlow(drawable);
		}
		if(normal == true){
			update();
			render(drawable);
		}
	}

	public void init(GLAutoDrawable drawable) {
	    //no idea what this is for
	}

	public void dispose(GLAutoDrawable drawable) {
	    //or this
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
	    //should probably work this out too
	}
	
    private void update() {
    	double force = 0;
    	double accel = 0;
    	double modRicm = 0;
    	double[] rHat = new double[nDim];
    	double[] totalMasses = new double[(int) Math.pow(3, nDim)];
    	double[][] cmPositions = new double[(int) Math.pow(3, nDim)][nDim];
    	double[] rHatMid = new double[2];
    	double totalForce = 0;
    	for(int z = 0; z<updatesPerRefresh; z++){
    		for(int j = 0; j<n; j++){
	    		totalForce = 0;
	    		rHat[0] = 0;
	    		rHat[1] = 0;
    			for(int k = 0; k<9; k++){
    				totalMasses[k] = 0;
    				for(int l = 0; l<nDim; l++){
    					cmPositions[k][l] = 0;
    				}
    			}
    			for(int k = 0; k<n; k++){
    				if(j != k){//if its not the considered particle....
    					//find which section it goes in
    					if(currentR[k][0] <= currentR[j][0] - d  && currentR[k][1] >= currentR[j][1]+d){
    						totalMasses[0] = totalMasses[0]+mass[k];
    						cmPositions[0][0] = cmPositions[0][0] + mass[k]*currentR[k][0];
    						cmPositions[0][1] = cmPositions[0][1] + mass[k]*currentR[k][1];
    						
    					}else if(currentR[k][0] <= currentR[j][0] - d  && currentR[k][1] >= currentR[j][1]-d && currentR[k][1] <= currentR[j][1]+d){
       						totalMasses[1] = totalMasses[1]+mass[k];
    						cmPositions[1][0] = cmPositions[1][0] + mass[k]*currentR[k][0];
    						cmPositions[1][1] = cmPositions[1][1] + mass[k]*currentR[k][1];
    					}else if(currentR[k][0] <= currentR[j][0] - d  && currentR[k][1] <= currentR[j][1]-d){
       						totalMasses[2] = totalMasses[2]+mass[k];
    						cmPositions[2][0] = cmPositions[2][0] + mass[k]*currentR[k][0];
    						cmPositions[2][1] = cmPositions[2][1] + mass[k]*currentR[k][1];
    					}else if(currentR[k][0] >= currentR[j][0] - d  &&currentR[k][0] <= currentR[j][0] + d  && currentR[k][1] >= currentR[j][1]+d){
       						totalMasses[3] = totalMasses[3]+mass[k];
    						cmPositions[3][0] = cmPositions[3][0] + mass[k]*currentR[k][0];
    						cmPositions[3][1] = cmPositions[3][1] + mass[k]*currentR[k][1];
    					}else if(currentR[k][0] >= currentR[j][0] - d  && currentR[k][0] <= currentR[j][0] + d  && currentR[k][1] >= currentR[j][1]-d && currentR[k][1] <= currentR[j][1]+d){
    						//center point with observed particle
    						rHatMid[0] = currentR[k][0]-currentR[j][0];
    						rHatMid[1] = currentR[k][1]-currentR[j][1];
    						modRicm=Math.sqrt(Math.pow(rHatMid[0],2) + Math.pow(rHatMid[1],2));
    						rHatMid[0] = rHatMid[0]/modRicm;
    						rHatMid[1] = rHatMid[1]/modRicm;
    						force = (G*mass[j]*mass[k])/Math.pow(modRicm,2);
    						totalForce = totalForce + force;
    						rHat[0] = rHat[0] + rHatMid[0];
    						rHat[1] = rHat[1] + rHatMid[1];
    						modRicm = Math.sqrt(Math.pow(rHat[0],2) + Math.pow(rHat[1],2));
    						if(modRicm != 0){
    							rHat[0] = rHat[0]/modRicm;
    							rHat[1] = rHat[1]/modRicm;
    						}
    					}else if(currentR[k][0] >= currentR[j][0] - d  && currentR[k][0] <= currentR[j][0] + d  && currentR[k][1] <= currentR[j][1]-d){
       						totalMasses[5] = totalMasses[5]+mass[k];
    						cmPositions[5][0] = cmPositions[5][0] + mass[k]*currentR[k][0];
    						cmPositions[5][1] = cmPositions[5][1] + mass[k]*currentR[k][1];
    					}else if(currentR[k][0] >= currentR[j][0] + d  && currentR[k][1] >= currentR[j][1]+d){
       						totalMasses[6] = totalMasses[6]+mass[k];
    						cmPositions[6][0] = cmPositions[6][0] + mass[k]*currentR[k][0];
    						cmPositions[6][1] = cmPositions[6][1] + mass[k]*currentR[k][1];
    					}else if(currentR[k][0] >= currentR[j][0] + d  && currentR[k][1] >= currentR[j][1]-d && currentR[k][1] <= currentR[j][1]+d){
       						totalMasses[7] = totalMasses[7]+mass[k];
    						cmPositions[7][0] = cmPositions[7][0] + mass[k]*currentR[k][0];
    						cmPositions[7][1] = cmPositions[7][1] + mass[k]*currentR[k][1];
    					}else if(currentR[k][0] >= currentR[j][0] + d  && currentR[k][1] <= currentR[j][1]-d){
       						totalMasses[8] = totalMasses[8]+mass[k];
    						cmPositions[8][0] = cmPositions[8][0] + mass[k]*currentR[k][0];
    						cmPositions[8][1] = cmPositions[8][1] + mass[k]*currentR[k][1];
    					}else{
    						System.out.println("Uh oh!");
    					}	
    				}
    			}
    			//deal with COMs
				for(int l = 0; l<(int) Math.pow(3, nDim); l++){
					if(l != (((int)Math.pow(3, nDim)+1)/2)){
						if(cmPositions[l][0] != 0 && cmPositions[l][1] != 0){
							for(int m = 0; m<nDim; m++){
								if(totalMasses[l] != 0){
									cmPositions[l][m] = cmPositions[l][m]/totalMasses[l];
								}
								
							}
							rHatMid[0] = cmPositions[l][0]-currentR[j][0];
							rHatMid[1] = cmPositions[l][1]-currentR[j][1];
							modRicm=Math.sqrt(Math.pow(rHatMid[0],2) + Math.pow(rHatMid[1],2));
							if(modRicm != 0){
								rHatMid[0] = rHatMid[0]/modRicm;
								rHatMid[1] = rHatMid[1]/modRicm;
							}
							force = (G*mass[j]*totalMasses[l])/Math.pow(modRicm,2);
							totalForce = totalForce + force;
							rHat[0] = rHat[0] + rHatMid[0];
							rHat[1] = rHat[1] + rHatMid[1];
							modRicm = Math.sqrt(Math.pow(rHat[0],2) + Math.pow(rHat[1],2));
							if(modRicm != 0){
								rHat[0] = rHat[0]/modRicm;
								rHat[1] = rHat[1]/modRicm;
							}
						}
					}
				}
				//find accel
				accel=totalForce/mass[j];
				//find v and new ri
				for(int k = 0; k<nDim; k++){
					nextV[j][k] = currentV[j][k]+accel*rHat[k]*timeInt;
					nextR[j][k] = currentR[j][k] + currentV[j][k]*timeInt + 0.5*accel*rHat[k]*Math.pow(timeInt,2);  
				}
    		}
    		//update the array
			for(int j = 0; j<n; j++){
				for(int k = 0; k<nDim; k++){
					currentV[j][k] = nextV[j][k];
					currentR[j][k] = nextR[j][k];
				}
			}
    		
    	}
    }

    private void updateSlow(){
    	double[] rHatMid = new double[2];
    	double[] rHatS = new double[2];
    	double totalForce = 0;
    	double acceleration = 0;
    	double modDenom = 0;
    	double currentForce = 0;
    	for(int z = 0; z<updatesPerRefresh; z++){
			for(int j = 0; j<n; j++){
	    		totalForce = 0;
	    		rHatS[0] = 0;
	    		rHatS[1] = 0;
				for(int k = 0; k< n;k++){
					if(j != k){
						//calc force between
						rHatMid[0] = currentSR[k][0]-currentSR[j][0];
						rHatMid[1] = currentSR[k][1]-currentSR[j][1];
						modDenom=Math.sqrt(Math.pow(rHatMid[0],2) + Math.pow(rHatMid[1],2));
						rHatMid[0] = rHatMid[0]/modDenom;
						rHatMid[1] = rHatMid[1]/modDenom;
						currentForce = (G*mass[j]*mass[k])/Math.pow(modDenom,2);
						totalForce = totalForce + currentForce;
						rHatS[0] = rHatS[0] + rHatMid[0];
						rHatS[1] = rHatS[1] + rHatMid[1];
						modDenom = Math.sqrt(Math.pow(rHatS[0],2) + Math.pow(rHatS[1],2));
						if(modDenom != 0){
							rHatS[0] = rHatS[0]/modDenom;
							rHatS[1] = rHatS[1]/modDenom;
						}
					}
				}
				//find accel
				acceleration=totalForce/mass[j];
				//find v and new ri
				for(int k = 0; k<nDim; k++){
					nextSV[j][k] = currentSV[j][k]+acceleration*rHatS[k]*timeInt;
					nextSR[j][k] = currentSR[j][k] + currentSV[j][k]*timeInt + 0.5*acceleration*rHatS[k]*Math.pow(timeInt,2); 
				}
			}
			//update the array
			for(int j = 0; j<n; j++){
				for(int k = 0; k<nDim; k++){
					currentSV[j][k] = nextSV[j][k];
					currentSR[j][k] = nextSR[j][k];
				}
			}
    	}
    }
    
    private void render(GLAutoDrawable drawable) {
    	double x = 0;
    	double y = 0;
    	double radius = 0;
        double q = 0;
        double r = 0;
        double angle = 0;
        GL2 gl = drawable.getGL().getGL2();
        if(slow == false){
        	gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        }
        gl.glBegin(GL.GL_TRIANGLES);
        for(int j = 0; j<n; j++){
        	x = (2*currentR[j][0])/width;
        	y = (2*currentR[j][1])/width;
        	radius = (2*radii[j])/width;
            gl.glColor3f(0,0,1);
			for(int i =0; i <= 300; i++){
				angle = 2 * Math.PI * i / 300;
				q = radius*Math.cos(angle);
				r = radius*Math.sin(angle);
				gl.glVertex2d(q+x,r+y);
				gl.glVertex2d(q+x,0+y);
				gl.glVertex2d(radius+x,0+y);
	        }
	        
        }
        gl.glEnd();
    }

    private void renderSlow(GLAutoDrawable drawable) {
    	double x = 0;
    	double y = 0;
    	double radius = 0;
        double q = 0;
        double r = 0;
        double angle = 0;
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glBegin(GL.GL_TRIANGLES);
        for(int j = 0; j<n; j++){
        	x = (2*currentSR[j][0])/width;
        	y = (2*currentSR[j][1])/width;
        	radius = (2*radii[j])/width;
            gl.glColor3f(1,0,0);        	
			for(int i =0; i <= 300; i++){
				angle = 2 * Math.PI * i / 300;
				q = radius*Math.cos(angle);
				r = radius*Math.sin(angle);
				gl.glVertex2d(q+x,r+y);
				gl.glVertex2d(q+x,0+y);
				gl.glVertex2d(radius+x,0+y);
	        }
	        
        }
        gl.glEnd();
    }

}

