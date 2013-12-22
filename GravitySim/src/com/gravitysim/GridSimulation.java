package com.gravitysim;

public class GridSimulation extends Simulation {

    // Constructors taken care of in Simulation Superclass

    public GridSimulation() { super(); }
    public GridSimulation(Body[] body) { super(body); }
    public GridSimulation(int n) { super(n); }
    
    private int numPerRow = (2*GravitySimTwoD.numLines) + 1;
    private int numGrid = (int) Math.pow(numPerRow, 2);
    private int midPoint = (numGrid - 1)/2;
    private int currentGridPos;
    double[] totalMasses = new double[numGrid];
    double[][] cmPositions = new double[numGrid][GravitySimTwoD.nDim];
    
    
    @Override
    void update() {
        double force = 0;
        double accel = 0;
        double modRicm = 0;
        double[] rHat = new double[GravitySimTwoD.nDim];
        double[] rHatMid = new double[2];
        double totalForce = 0;
        int sectionX = 0;
        int sectionY = 0;
        for(int z = 0; z<GravitySimTwoD.updatesPerRefresh; z++){
            for(int j = 0; j<GravitySimTwoD.n; j++){
                totalForce = 0;
                rHat[0] = 0;
                rHat[1] = 0;
                for(int k = 0; k<numGrid; k++){
                    totalMasses[k] = 0;
                    for(int l = 0; l<GravitySimTwoD.nDim; l++){
                        cmPositions[k][l] = 0;
                    }
                }
                for(int k = 0; k<GravitySimTwoD.n; k++){
                    if(j != k){//if its not the considered particle....
                        //find which section it goes in
                    	currentGridPos = midPoint;
                    	sectionX = (int) ((body[k].currentR[0] - body[j].currentR[0])/GravitySimTwoD.d);
                    	sectionY = (int) ((body[k].currentR[1] - body[j].currentR[1])/GravitySimTwoD.d);
                    	if(sectionX > GravitySimTwoD.numLines){
                    		sectionX = GravitySimTwoD.numLines;
                    	}else if(sectionX < -GravitySimTwoD.numLines){
                    		sectionX = -GravitySimTwoD.numLines;
                    	}
                    	if(sectionY > GravitySimTwoD.numLines){
                    		sectionY = GravitySimTwoD.numLines;
                    	}else if(sectionY < -GravitySimTwoD.numLines){
                    		sectionY = -GravitySimTwoD.numLines;
                    	}
                    	currentGridPos+= sectionX;
                    	currentGridPos+= sectionY*numPerRow;
                    	//System.out.println(currentGridPos);
                    	
                    	if(currentGridPos == midPoint){
                            rHatMid[0] = body[k].currentR[0]-body[j].currentR[0];
                            rHatMid[1] = body[k].currentR[1]-body[j].currentR[1];
                            modRicm=Math.sqrt(Math.pow(rHatMid[0],2) + Math.pow(rHatMid[1],2));
                            if(modRicm<= body[k].radius + body[j].radius && GravitySimTwoD.collisions == true){
                                body[j].mass = body[j].mass + body[k].mass;
                                body[j].radius = Math.pow((Math.pow(body[j].radius, GravitySimTwoD.nDim)+Math.pow(body[k].radius, GravitySimTwoD.nDim)), (1.0/GravitySimTwoD.nDim));
                                for(int l = 0; l< GravitySimTwoD.nDim; l++){
                                    body[j].currentR[l] = ((body[j].mass * body[j].currentR[l])+(body[k].mass*body[k].currentR[l]))/(body[j].mass + body[k].mass);
                                    body[j].currentV[l] = ((body[j].mass * body[j].currentV[l])+(body[k].mass*body[k].currentV[l]))/(body[j].mass + body[k].mass);
                                }
                                for(int l = k; l<GravitySimTwoD.n-1 ; l++){
                                    body[l].mass = body[l+1].mass;
                                    body[l].radius = body[l+1].radius;
                                    for(int m = 0; m<GravitySimTwoD.nDim; m++){
                                        body[l].currentR[m] = body[l+1].currentR[m];
                                        body[l].currentV[m] = body[l+1].currentV[m];
                                    }
                                }
                                GravitySimTwoD.n--;
                                k--;
                            }else{
                                rHatMid[0] = rHatMid[0]/modRicm;
                                rHatMid[1] = rHatMid[1]/modRicm;
                                force = (GravitySimTwoD.G*body[j].mass*body[k].mass)/Math.pow(modRicm,2);
                                totalForce = totalForce + force;
                                rHat[0] = rHat[0] + rHatMid[0];
                                rHat[1] = rHat[1] + rHatMid[1];
                                modRicm = Math.sqrt(Math.pow(rHat[0],2) + Math.pow(rHat[1],2));
                                if(modRicm != 0){
                                    rHat[0] = rHat[0]/modRicm;
                                    rHat[1] = rHat[1]/modRicm;
                                }
                            }
                    	}else{
                            totalMasses[currentGridPos] = totalMasses[currentGridPos]+body[k].mass;
                            cmPositions[currentGridPos][0] = cmPositions[currentGridPos][0] + body[k].mass*body[k].currentR[0];
                            cmPositions[currentGridPos][1] = cmPositions[currentGridPos][1] + body[k].mass*body[k].currentR[1];
                    	}
                    	  
                    }
                }
                //deal with COMs
                for(int l = 0; l<numGrid; l++){
                    if(l != midPoint){
                        if(totalMasses[l] != 0){
                            for(int m = 0; m<GravitySimTwoD.nDim; m++){
                                cmPositions[l][m] = cmPositions[l][m]/totalMasses[l];                            
                            }
                            rHatMid[0] = cmPositions[l][0]-body[j].currentR[0];
                            rHatMid[1] = cmPositions[l][1]-body[j].currentR[1];
                            modRicm=Math.sqrt(Math.pow(rHatMid[0],2) + Math.pow(rHatMid[1],2));
                            if(modRicm != 0){
                                rHatMid[0] = rHatMid[0]/modRicm;
                                rHatMid[1] = rHatMid[1]/modRicm;
                            }
                            force = (GravitySimTwoD.G*body[j].mass*totalMasses[l])/Math.pow(modRicm,2);
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
                for(int k = 0; k<GravitySimTwoD.nDim; k++){
                    body[j].nextV[k] = body[j].currentV[k]+accel*rHat[k]*GravitySimTwoD.timeInt;
                    body[j].nextR[k] = body[j].currentR[k] + body[j].currentV[k]*GravitySimTwoD.timeInt + 0.5*accel*rHat[k]*Math.pow(GravitySimTwoD.timeInt,2);  
                }
            }
            //update the array
            for(int j = 0; j<GravitySimTwoD.n; j++){
                for(int k = 0; k<GravitySimTwoD.nDim; k++){
                    body[j].currentV[k] = body[j].nextV[k];
                    body[j].currentR[k] = body[j].nextR[k];
                }
            }
            
        }
    }

}
