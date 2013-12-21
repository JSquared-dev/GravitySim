package com.gravitysim;

public class Body implements Cloneable {
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
}
