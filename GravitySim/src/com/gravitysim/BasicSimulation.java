package com.gravitysim;


public class BasicSimulation extends Simulation {

    // Constructors taken care of in Simulation Superclass

    public BasicSimulation() { super(); }
    public BasicSimulation(Body[] body) { super(body); }
    public BasicSimulation(int n) { super(n); }
    
    @Override
    void update() {

        double[] rHatMid = new double[2];
        double[] rHatS = new double[2];
        double totalForce = 0;
        double acceleration = 0;
        double modDenom = 0;
        double currentForce = 0;
        for(int z = 0; z<GravitySimTwoD.updatesPerRefresh; z++){
            for(int j = 0; j<n; j++){
                totalForce = 0;
                rHatS[0] = 0;
                rHatS[1] = 0;
                for(int k = 0; k< n;k++){
                    if(j != k){
                        //calc force between
                        rHatMid[0] = body[k].currentR[0]-body[j].currentR[0];
                        rHatMid[1] = body[k].currentR[1]-body[j].currentR[1];
                        modDenom=Math.sqrt(Math.pow(rHatMid[0],2) + Math.pow(rHatMid[1],2));
                        if(modDenom<= body[k].radius + body[j].radius && GravitySimTwoD.collisions == true){
                            body[j].mass = body[j].mass + body[k].mass;
                            body[j].radius = Math.pow((Math.pow(body[j].radius, GravitySimTwoD.nDim)+Math.pow(body[k].radius, GravitySimTwoD.nDim)), (1.0/GravitySimTwoD.nDim));
                            for(int l = 0; l< GravitySimTwoD.nDim; l++){
                                body[j].currentR[l] = ((body[j].mass * body[j].currentR[l])+(body[k].mass*body[k].currentR[l]))/(body[j].mass + body[k].mass);
                                body[j].currentV[l] = ((body[j].mass * body[j].currentV[l])+(body[k].mass*body[k].currentV[l]))/(body[j].mass + body[k].mass);
                            }
                            for(int l = k; l<n-1 ; l++){
                                body[l].mass = body[l+1].mass;
                                body[l].radius = body[l+1].radius;
                                for(int m = 0; m<GravitySimTwoD.nDim; m++){
                                    body[l].currentR[m] = body[l+1].currentR[m];
                                    body[l].currentV[m] = body[l+1].currentV[m];
                                }
                            }
                            n--;
                            k--;
                        }else{
                            rHatMid[0] = rHatMid[0]/modDenom;
                            rHatMid[1] = rHatMid[1]/modDenom;
                            currentForce = (GravitySimTwoD.G*body[j].mass*body[k].mass)/Math.pow(modDenom,2);
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
                for(int k = 0; k<GravitySimTwoD.nDim; k++){
                    body[j].nextV[k] = body[j].currentV[k]+acceleration*rHatS[k]*GravitySimTwoD.timeInt;
                    body[j].nextR[k] = body[j].currentR[k] + body[j].currentV[k]*GravitySimTwoD.timeInt + 0.5*acceleration*rHatS[k]*Math.pow(GravitySimTwoD.timeInt,2); 
                }
            }
            //update the array
            for(int j = 0; j<n; j++){
                for(int k = 0; k<GravitySimTwoD.nDim; k++){
                    body[j].currentV[k] = body[j].nextV[k];
                    body[j].currentR[k] = body[j].nextR[k];
                }
            }
        }
    }

}
