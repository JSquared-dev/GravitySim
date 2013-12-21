package com.gravitysim;

import java.util.Random;

public class Body {
	//Arrays to track position and velocity for normal method
		public double[] currentR;
		public double[] nextR;
		public double[] currentV;
		public double[] nextV;
		
		//arrays for masses and radius
		public double radius;
		public double mass;
		
		public int dim;
		
		public Body(int dimensions) {
			dim = dimensions;
			//Arrays to track position and velocity for normal method
			currentR = new double[dimensions];
			nextR = new double[dimensions];
			currentV = new double[dimensions];
			nextV = new double[dimensions];
			
			radius = mass = 0;
		}
		
		public Body(Body body) {
		    // duplicate body into this object.
	        this.mass = body.mass;
	        this.radius = body.radius;
	        this.dim = body.dim;
	        
	        //Arrays to track position and velocity for normal method
            currentR = new double[dim];
            nextR = new double[dim];
            currentV = new double[dim];
            nextV = new double[dim];

            System.arraycopy(body.currentR, 0, currentR, 0, body.currentR.length);
            System.arraycopy(body.nextR, 0, nextR, 0, body.nextR.length);
            System.arraycopy(body.currentV, 0, currentV, 0, body.currentV.length);
            System.arraycopy(body.nextV, 0, nextV, 0, body.nextV.length);
		}
		
		public void randomise() {
		    Random rand = new Random();
		    mass = GravitySimTwoD.minMass+(GravitySimTwoD.maxMass-GravitySimTwoD.minMass)*rand.nextDouble();
            radius = Math.sqrt(mass/(Math.PI*GravitySimTwoD.density));
            for(int j = 0; j<GravitySimTwoD.nDim; j++){
                currentR[j] = GravitySimTwoD.minDist+(GravitySimTwoD.maxDist-GravitySimTwoD.minDist)*rand.nextDouble();
                currentV[j] = GravitySimTwoD.minVel+(GravitySimTwoD.maxVel-GravitySimTwoD.minVel)*rand.nextDouble();
            }
		}
}
