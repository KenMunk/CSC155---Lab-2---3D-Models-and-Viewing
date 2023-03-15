package code;

import java.nio.*;
import java.lang.Math;

import javax.swing.*;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.*;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GLContext;
import org.joml.*;

public class Code extends JFrame implements GLEventListener
{	private GLCanvas myCanvas;
	private double startTime = 0.0;
	private double elapsedTime;
	private int renderingProgram;
	private int vao[] = new int[1];
	private int vbo[] = new int[2];
	private float cameraX, cameraY, cameraZ;
	
	// allocate variables for display() function
	private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
	private Matrix4fStack mvStack = new Matrix4fStack(5);
	private Matrix4f viewMatrix = new Matrix4f();
	private Matrix4f pMat = new Matrix4f();
	private int mvLoc, pLoc;
	private float aspect;
	private double tf;
	private float cameraRotation = 0;
	
	private float camYaw = 0f; //Side to side
	private float camPitch = 0f; //up down
	private float camRoll = 0f; //roll screen
	
	private AxisState fwdAxis = new AxisState(0.1f, KeyEvent.VK_W, KeyEvent.VK_S);
	private AxisState sideAxis = new AxisState(0.1f, KeyEvent.VK_D, KeyEvent.VK_A);
	private AxisState verticalAxis = new AxisState(0.1f, KeyEvent.VK_Q, KeyEvent.VK_E);
	private AxisState pitchTurnAxis = new AxisState(0.01f, KeyEvent.VK_UP, KeyEvent.VK_DOWN);
	private AxisState yawTurnAxis = new AxisState(0.01f, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT);
	

	public Code()
	{	setTitle("Chapter 4 - program 4");
		setSize(600, 600);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		this.add(myCanvas);
		this.setVisible(true);
		Animator animator = new Animator(myCanvas);
		animator.start();
		
		this.addKeyListener(new KeyAdapter() {
			
			public void keyPressed(KeyEvent e){
				
				pitchTurnAxis.pressCheck(e.getKeyCode());
				yawTurnAxis.pressCheck(e.getKeyCode());
				
				fwdAxis.pressCheck(e.getKeyCode());
				sideAxis.pressCheck(e.getKeyCode());
				verticalAxis.pressCheck(e.getKeyCode());
				
			}
			
			public void keyReleased(KeyEvent e){
				
				pitchTurnAxis.releaseCheck(e.getKeyCode());
				yawTurnAxis.releaseCheck(e.getKeyCode());
				
				fwdAxis.releaseCheck(e.getKeyCode());
				sideAxis.releaseCheck(e.getKeyCode());
				verticalAxis.releaseCheck(e.getKeyCode());
				
			}
			
		});
		
		/*
			Strategy:
			
			instead of trying to use the key listeners which don't seem to work here, we'll use the KeyStroke system which will 
			
			https://docs.oracle.com/javase/7/docs/api/java/awt/event/KeyAdapter.html
			
			https://docs.oracle.com/javase/8/javafx/api/javafx/scene/input/KeyCode.html
			
			https://docs.oracle.com/javase/7/docs/api/java/awt/AWTKeyStroke.html
			
			https://docs.oracle.com/javase/7/docs/api/javax/swing/KeyStroke.html#getKeyStroke(char,%20boolean)
			
			
		
		
		//*/
		
	}
	
	public void display(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glClear(GL_COLOR_BUFFER_BIT);
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		elapsedTime = System.currentTimeMillis() - startTime;

		gl.glUseProgram(renderingProgram);

		mvLoc = gl.glGetUniformLocation(renderingProgram, "mv_matrix");
		pLoc = gl.glGetUniformLocation(renderingProgram, "p_matrix");

		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		

		// push view matrix onto the stack
		mvStack.pushMatrix();
		//So this translates the camera
		//*
		//Camera rotation radians needs to be done as a mod of 6.28319
		//in order to ensure that the camera completes a full circle
		cameraRotation = 6.28319f;
		
		camYaw = (camYaw + yawTurnAxis.getValue())%cameraRotation; //Side to side
		camPitch = (camPitch + pitchTurnAxis.getValue())%cameraRotation; //up down
		camRoll = 0f; //roll screen
		
		mvStack.rotate(camYaw, 0f, 1f, 0f);
		mvStack.rotate(camPitch, 1f, 0f, 0f);
		//*/
		cameraZ += fwdAxis.getValue();
		cameraY += verticalAxis.getValue();
		cameraX += sideAxis.getValue();
		
		mvStack.translate(-cameraX, -cameraY, -cameraZ);
		
		tf = elapsedTime/1000.0;  // time factor
		
		//*
		// ----------------------  pyramid == sun  
		mvStack.pushMatrix();
		mvStack.translate(0.0f, 0.0f, 0.0f);
		mvStack.pushMatrix();
		mvStack.rotate((float)tf, 1.0f, 0.0f, 0.0f);
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDrawArrays(GL_TRIANGLES, 0, 18); 
		mvStack.popMatrix();
		
		//-----------------------  cube == planet  
		mvStack.pushMatrix();
		mvStack.translate((float)Math.sin(tf)*4.0f, 0.0f, (float)Math.cos(tf)*4.0f);
		mvStack.pushMatrix();
		mvStack.rotate((float)tf, 0.0f, 1.0f, 0.0f);
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
		mvStack.popMatrix();
		/*
		//-----------------------  smaller cube == moon
		mvStack.pushMatrix();
		mvStack.translate(0.0f, (float)Math.sin(tf)*2.0f, (float)Math.cos(tf)*2.0f);
		mvStack.rotate((float)tf, 0.0f, 0.0f, 1.0f);
		mvStack.scale(0.25f, 0.25f, 0.25f);
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
		mvStack.popMatrix();  
		*/
		mvStack.popMatrix();  
		mvStack.popMatrix();
		//*/
		mvStack.popMatrix();
	}

	public void init(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		startTime = System.currentTimeMillis();
		renderingProgram = Utils.createShaderProgram("code/vertShader.glsl", "code/fragShader.glsl");
		setupVertices();
		cameraX = 0.0f; cameraY = 0.0f; cameraZ = 12.0f;
	}

	private void setupVertices()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		float[] cubePositions =
		{	-1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f,
			1.0f, -1.0f, -1.0f, 1.0f,  1.0f, -1.0f, -1.0f,  1.0f, -1.0f,
			1.0f, -1.0f, -1.0f, 1.0f, -1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
			1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
			1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f, -1.0f,  1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f, -1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f, -1.0f,
			1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,
			-1.0f,  1.0f, -1.0f, 1.0f,  1.0f, -1.0f, 1.0f,  1.0f,  1.0f,
			1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f, -1.0f
		};
		
		float[] pyramidPositions =
		{	-1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,    //front
			1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,    //right
			1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,  //back
			-1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,  //left
			-1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, //LF
			1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f  //RR
		};

		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		gl.glGenBuffers(vbo.length, vbo, 0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer cubeBuf = Buffers.newDirectFloatBuffer(cubePositions);
		gl.glBufferData(GL_ARRAY_BUFFER, cubeBuf.limit()*4, cubeBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer pyrBuf = Buffers.newDirectFloatBuffer(pyramidPositions);
		gl.glBufferData(GL_ARRAY_BUFFER, pyrBuf.limit()*4, pyrBuf, GL_STATIC_DRAW);
	}

	public static void main(String[] args) { new Code(); }
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
	public void dispose(GLAutoDrawable drawable) {}
}