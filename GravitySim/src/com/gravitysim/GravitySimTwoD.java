package com.gravitysim;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
	
	private int n = 10; //number of bodies
	private int nDim = 2; //number of spatial dimensions
	private double timeInt = 60; //time interval, (s)
	private int updatesPerRefresh = 250;//how many times it calculates before updating the display
	private double density = 3;//5520 = earth
	private double width = 5000;//width of space
	private double d = 2*width/3; //width of center square
	private double G = 6.673e-11;
	private boolean slow = true;//turn methods on and off
	private boolean normal = true;
	private boolean collisions = false;
	
	//Min and max values for random start
	private double minMass = 1e3;
	private double maxMass = 1e5;
	private double minDist = -width/4;
	private double maxDist = width/4;
	private double minVel = 0.01*Math.sqrt(G);
	private double maxVel = 5*Math.sqrt(G);
	
	private Body[] body;

	public GravitySimTwoD() {
		//random start
				Random rand = new Random();
				body = new Body[n];
				for(int i = 0; i<n; i++){
					body[i] = new Body(nDim);
					body[i].mass = minMass+(maxMass-minMass)*rand.nextDouble();
					body[i].radius = Math.sqrt(body[i].mass/(Math.PI*density));
					for(int j = 0; j<nDim; j++){
						body[i].currentR[j] = minDist+(maxDist-minDist)*rand.nextDouble();
						body[i].currentV[j] = minVel+(maxVel-minVel)*rand.nextDouble();
						body[i].currentSR[j] = body[i].currentR[j];
						body[i].currentSV[j] = body[i].currentV[j];
					}
				}
	}

	


	public static void main(String[] args){
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
    					if(body[k].currentR[0] <= body[j].currentR[0] - d  && body[k].currentR[1] >= body[j].currentR[1]+d){
    						totalMasses[0] = totalMasses[0]+body[k].mass;
    						cmPositions[0][0] = cmPositions[0][0] + body[k].mass*body[k].currentR[0];
    						cmPositions[0][1] = cmPositions[0][1] + body[k].mass*body[k].currentR[1];
    						
    					}else if(body[k].currentR[0] <= body[j].currentR[0] - d  && body[k].currentR[1] >= body[j].currentR[1]-d && body[k].currentR[1] <= body[j].currentR[1]+d){
       						totalMasses[1] = totalMasses[1]+body[k].mass;
    						cmPositions[1][0] = cmPositions[1][0] + body[k].mass*body[k].currentR[0];
    						cmPositions[1][1] = cmPositions[1][1] + body[k].mass*body[k].currentR[1];
    					}else if(body[k].currentR[0] <= body[j].currentR[0] - d  && body[k].currentR[1] <= body[j].currentR[1]-d){
       						totalMasses[2] = totalMasses[2]+body[k].mass;
    						cmPositions[2][0] = cmPositions[2][0] + body[k].mass*body[k].currentR[0];
    						cmPositions[2][1] = cmPositions[2][1] + body[k].mass*body[k].currentR[1];
    					}else if(body[k].currentR[0] >= body[j].currentR[0] - d  &&body[k].currentR[0] <= body[j].currentR[0] + d  && body[k].currentR[1] >= body[j].currentR[1]+d){
       						totalMasses[3] = totalMasses[3]+body[k].mass;
    						cmPositions[3][0] = cmPositions[3][0] + body[k].mass*body[k].currentR[0];
    						cmPositions[3][1] = cmPositions[3][1] + body[k].mass*body[k].currentR[1];
    					}else if(body[k].currentR[0] >= body[j].currentR[0] - d  && body[k].currentR[0] <= body[j].currentR[0] + d  && body[k].currentR[1] >= body[j].currentR[1]-d && body[k].currentR[1] <= body[j].currentR[1]+d){
    						//center point with observed particle
    						rHatMid[0] = body[k].currentR[0]-body[j].currentR[0];
    						rHatMid[1] = body[k].currentR[1]-body[j].currentR[1];
    						modRicm=Math.sqrt(Math.pow(rHatMid[0],2) + Math.pow(rHatMid[1],2));
    						if(modRicm<= body[k].radius + body[j].radius && collisions == true){
    							body[j].mass = body[j].mass + body[k].mass;
    							body[j].radius = Math.pow((Math.pow(body[j].radius, nDim)+Math.pow(body[k].radius, nDim)), (1.0/nDim));
    							for(int l = 0; l< nDim; l++){
    								body[j].currentR[l] = ((body[j].mass * body[j].currentR[l])+(body[k].mass*body[k].currentR[l]))/(body[j].mass + body[k].mass);
    								body[j].currentV[l] = ((body[j].mass * body[j].currentV[l])+(body[k].mass*body[k].currentV[l]))/(body[j].mass + body[k].mass);
    							}
    							for(int l = k; l<n-1 ; l++){
    								body[l].mass = body[l+1].mass;
    								body[l].radius = body[l+1].radius;
    								for(int m = 0; m<nDim; m++){
    									body[l].currentR[m] = body[l+1].currentR[m];
    									body[l].currentV[m] = body[l+1].currentV[m];
    								}
    							}
    							n--;
    							k--;
    						}else{
	    						rHatMid[0] = rHatMid[0]/modRicm;
	    						rHatMid[1] = rHatMid[1]/modRicm;
	    						force = (G*body[j].mass*body[k].mass)/Math.pow(modRicm,2);
	    						totalForce = totalForce + force;
	    						rHat[0] = rHat[0] + rHatMid[0];
	    						rHat[1] = rHat[1] + rHatMid[1];
	    						modRicm = Math.sqrt(Math.pow(rHat[0],2) + Math.pow(rHat[1],2));
	    						if(modRicm != 0){
	    							rHat[0] = rHat[0]/modRicm;
	    							rHat[1] = rHat[1]/modRicm;
	    						}
    						}
    					}else if(body[k].currentR[0] >= body[j].currentR[0] - d  && body[k].currentR[0] <= body[j].currentR[0] + d  && body[k].currentR[1] <= body[j].currentR[1]-d){
       						totalMasses[5] = totalMasses[5]+body[k].mass;
    						cmPositions[5][0] = cmPositions[5][0] + body[k].mass*body[k].currentR[0];
    						cmPositions[5][1] = cmPositions[5][1] + body[k].mass*body[k].currentR[1];
    					}else if(body[k].currentR[0] >= body[j].currentR[0] + d  && body[k].currentR[1] >= body[j].currentR[1]+d){
       						totalMasses[6] = totalMasses[6]+body[k].mass;
    						cmPositions[6][0] = cmPositions[6][0] + body[k].mass*body[k].currentR[0];
    						cmPositions[6][1] = cmPositions[6][1] + body[k].mass*body[k].currentR[1];
    					}else if(body[k].currentR[0] >= body[j].currentR[0] + d  && body[k].currentR[1] >= body[j].currentR[1]-d && body[k].currentR[1] <= body[j].currentR[1]+d){
       						totalMasses[7] = totalMasses[7]+body[k].mass;
    						cmPositions[7][0] = cmPositions[7][0] + body[k].mass*body[k].currentR[0];
    						cmPositions[7][1] = cmPositions[7][1] + body[k].mass*body[k].currentR[1];
    					}else if(body[k].currentR[0] >= body[j].currentR[0] + d  && body[k].currentR[1] <= body[j].currentR[1]-d){
       						totalMasses[8] = totalMasses[8]+body[k].mass;
    						cmPositions[8][0] = cmPositions[8][0] + body[k].mass*body[k].currentR[0];
    						cmPositions[8][1] = cmPositions[8][1] + body[k].mass*body[k].currentR[1];
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
							rHatMid[0] = cmPositions[l][0]-body[j].currentR[0];
							rHatMid[1] = cmPositions[l][1]-body[j].currentR[1];
							modRicm=Math.sqrt(Math.pow(rHatMid[0],2) + Math.pow(rHatMid[1],2));
							if(modRicm != 0){
								rHatMid[0] = rHatMid[0]/modRicm;
								rHatMid[1] = rHatMid[1]/modRicm;
							}
							force = (G*body[j].mass*totalMasses[l])/Math.pow(modRicm,2);
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
				accel=totalForce/body[j].mass;
				//find v and new ri
				for(int k = 0; k<nDim; k++){
					body[j].nextV[k] = body[j].currentV[k]+accel*rHat[k]*timeInt;
					body[j].nextR[k] = body[j].currentR[k] + body[j].currentV[k]*timeInt + 0.5*accel*rHat[k]*Math.pow(timeInt,2);  
				}
    		}
    		//update the array
			for(int j = 0; j<n; j++){
				for(int k = 0; k<nDim; k++){
					body[j].currentV[k] = body[j].nextV[k];
					body[j].currentR[k] = body[j].nextR[k];
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
						rHatMid[0] = body[k].currentSR[0]-body[j].currentSR[0];
						rHatMid[1] = body[k].currentSR[1]-body[j].currentSR[1];
						modDenom=Math.sqrt(Math.pow(rHatMid[0],2) + Math.pow(rHatMid[1],2));
						if(modDenom<= body[k].radius + body[j].radius && collisions == true){
							body[j].mass = body[j].mass + body[k].mass;
							body[j].radius = Math.pow((Math.pow(body[j].radius, nDim)+Math.pow(body[k].radius, nDim)), (1.0/nDim));
							for(int l = 0; l< nDim; l++){
								body[j].currentSR[l] = ((body[j].mass * body[j].currentSR[l])+(body[k].mass*body[k].currentSR[l]))/(body[j].mass + body[k].mass);
								body[j].currentSV[l] = ((body[j].mass * body[j].currentSV[l])+(body[k].mass*body[k].currentSV[l]))/(body[j].mass + body[k].mass);
							}
							for(int l = k; l<n-1 ; l++){
								body[l].mass = body[l+1].mass;
								body[l].radius = body[l+1].radius;
								for(int m = 0; m<nDim; m++){
									body[l].currentSR[m] = body[l+1].currentSR[m];
									body[l].currentSV[m] = body[l+1].currentSV[m];
								}
							}
							n--;
							k--;
						}else{
							rHatMid[0] = rHatMid[0]/modDenom;
							rHatMid[1] = rHatMid[1]/modDenom;
							currentForce = (G*body[j].mass*body[k].mass)/Math.pow(modDenom,2);
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
				}
				//find accel
				acceleration=totalForce/body[j].mass;
				//find v and new ri
				for(int k = 0; k<nDim; k++){
					body[j].nextSV[k] = body[j].currentSV[k]+acceleration*rHatS[k]*timeInt;
					body[j].nextSR[k] = body[j].currentSR[k] + body[j].currentSV[k]*timeInt + 0.5*acceleration*rHatS[k]*Math.pow(timeInt,2); 
				}
			}
			//update the array
			for(int j = 0; j<n; j++){
				for(int k = 0; k<nDim; k++){
					body[j].currentSV[k] = body[j].nextSV[k];
					body[j].currentSR[k] = body[j].nextSR[k];
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
        for(int j = 0; j<n; j++){
        	x = (2*body[j].currentR[0])/width;
        	y = (2*body[j].currentR[1])/width;
        	radius = (2*body[j].radius)/width;
            gl.glBegin(GL2.GL_POLYGON);
            gl.glColor3f(0,0,1);
			for(int i =0; i <= 300; i++){
				angle = 2 * Math.PI * i / 300;
				q = radius*Math.cos(angle);
				r = radius*Math.sin(angle);
				gl.glVertex2d(q+x,r+y);
	        }
	        gl.glEnd();
        }
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
        for(int j = 0; j<n; j++){
        	x = (2*body[j].currentSR[0])/width;
        	y = (2*body[j].currentSR[1])/width;
        	radius = (2*body[j].radius)/width;
        	gl.glBegin(GL2.GL_POLYGON);
            gl.glColor3f(1,0,0);        	
			for(int i =0; i <= 300; i++){
				angle = 2 * Math.PI * i / 300;
				q = radius*Math.cos(angle);
				r = radius*Math.sin(angle);
				gl.glVertex2d(q+x,r+y);
			}
			gl.glEnd();
        }
    }

}

