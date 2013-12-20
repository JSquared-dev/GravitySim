package com.gravitysim;

public class Body {
	//Arrays to track position and velocity for normal method
		public double[] currentR;
		public double[] nextR;
		public double[] currentV;
		public double[] nextV;
		
		//duplicate arrays for the slow method
		public double[] currentSR;
		public double[] nextSR;
		public double[] currentSV;
		public double[] nextSV;
		
		//arrays for masses and radius
		public double radius;
		public double mass;
		
		public Body(int dimensions) {
			
			//Arrays to track position and velocity for normal method
			currentR = new double[dimensions];
			nextR = new double[dimensions];
			currentV = new double[dimensions];
			nextV = new double[dimensions];
			
			//duplicate arrays for the slow method
			currentSR = new double[dimensions];
			nextSR = new double[dimensions];
			currentSV = new double[dimensions];
			nextSV = new double[dimensions];
			
			radius = mass = 0;
		}
		
		public void update() {
			
		}
		
		public void updateSlow() {
			
		}
}
