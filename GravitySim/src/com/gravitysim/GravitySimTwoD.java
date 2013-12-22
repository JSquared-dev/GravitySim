package com.gravitysim;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;


public class GravitySimTwoD implements GLEventListener, KeyListener{
	
	public static int n = 10; //number of bodies
	public static int nDim = 2; //number of spatial dimensions
	public static double timeInt = 60; //time interval, (s)
	public static int updatesPerRefresh = 50;//how many times it calculates before updating the display
	public static double density = 3;//5520 = earth
	public static double width = 5000;//width of space
	public static double d = 2.5*width/3; //width of center square
	public static final double G = 6.673e-11;
	public static boolean collisions = false;
	
	public static TextRenderer textRenderer;
	public static DecimalFormat decFormat = new DecimalFormat("###0.00");
	public static int textPosX; 
	public static int textPosY; 
	public static int count = 0;
	//Min and max values for random start
	public static double minMass = 1e3;
	public static double maxMass = 1e5;
	public static double minDist = -width/4;
	public static double maxDist = width/4;
	public static double minVel = 0.01*Math.sqrt(G);
	public static double maxVel = 5*Math.sqrt(G);
	
	private double cameraX, cameraY;
	private Body[] body = null;
	private BasicSimulation basicSim = null;
	private GridSimulation gridSim = null;
	private GridSimulationRevised gridSimRevised = null;
	
	public GravitySimTwoD() {
	    body = new Body[n];
	    for (int i = 0; i < n; i++) {
	        body[i] = new Body(nDim);
	        body[i].randomise();
	    }
	    gridSim = new GridSimulation(body);
        basicSim = new BasicSimulation(body);
        gridSimRevised = new GridSimulationRevised(body);
        cameraX = cameraY = 0;
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
		GravitySimTwoD gravSim = new GravitySimTwoD();
		canvas.addGLEventListener(gravSim);
		canvas.addKeyListener(gravSim);
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
		count++;
		GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        
        GravitySimTwoD.textRenderer.beginRendering(drawable.getWidth(), drawable.getHeight());
        GravitySimTwoD.textRenderer.setColor(0,1,1,1); 
        double currentTime = GravitySimTwoD.timeInt*GravitySimTwoD.count;
        String units = " Secs";
        if(currentTime > 60 && currentTime < 3600){
            currentTime = currentTime/60;
            units = " Mins";
            
        }else if(currentTime > 3600 && currentTime < 86400){
            currentTime = currentTime/3600;
            units = " Hrs";
        }else if(currentTime > 86400 && currentTime < 31557600){
            currentTime = currentTime/86400;
            units = " Days";
        }else if(currentTime > 31557600){
            currentTime = currentTime/31557600;
            units = " Yrs";
        }
        GravitySimTwoD.textRenderer.draw("Time: "+GravitySimTwoD.decFormat.format(currentTime)+units,GravitySimTwoD.textPosX, GravitySimTwoD.textPosY);

        GravitySimTwoD.textRenderer.endRendering();
        gl.glPushMatrix();
        gl.glTranslated(cameraX, cameraY, 0);
        try {
            if(basicSim != null){
                basicSim.simCount.acquire();
                basicSim.render(drawable, new Color(255, 0, 0));
            }
            if(gridSim != null){
                gridSim.simCount.acquire();
                gridSim.render(drawable, new Color(0,0,255));
            }
            if(gridSimRevised != null){
                gridSimRevised.simCount.acquire();
                gridSimRevised.render(drawable, new Color(0,255,0));
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        gl.glPopMatrix();
		gl.glFlush();
	}

	public void init(GLAutoDrawable drawable) {
	    //no idea what this is for
	    GravitySimTwoD.textRenderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, 14));

        Rectangle2D bounds = textRenderer.getBounds("Time: " + "0000.00" + "Secs");
        int textWidth = (int)bounds.getWidth();
	      int textHeight = (int)bounds.getHeight();
	      textPosX = drawable.getWidth() - textWidth;
	      textPosY = drawable.getHeight() - textHeight;
	}

	public void dispose(GLAutoDrawable drawable) {
	    //or this
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
	    //should probably work this out too
	}

    @Override
    public void keyPressed(KeyEvent event) {
        // camera movements are inverted - we move the world, not the camera
        char key = event.getKeyChar();
        if (key == 'a') {
            cameraX += 0.1;
        }
        else if (key == 'd') {
            cameraX -= 0.1;
        }
        else if (key == 'w') {
            cameraY -= 0.1;
        }
        else if (key == 's') {
            cameraY += 0.1;
        }
    }

    @Override
    public void keyReleased(KeyEvent event) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void keyTyped(KeyEvent event) {
        // TODO Auto-generated method stub
        
    }

}

