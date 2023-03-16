package code;

import java.nio.*;
import java.lang.Math;

import javax.swing.*;

import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.*;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GLContext;
import org.joml.*;

public abstract class DrawableModel{
	
	private Vector3f globalPosition;
	private Vector3f rotation;
	
	private String modelPath;
	private String primaryTexturePath;
	
	private ImportedModel model;
	private int numObjVertices;
	
	private int modelVBO[] = new int[3];
	
	private int primaryModelTexture;
	
	private int renderingProgramBinding;
	
	public DrawableModel(String modelPath, String primaryTexturePath, int renderingProgram){
		
		this.modelPath = modelPath;
		
		this.primaryTexturePath = primaryTexturePath;
		
		this.globalPosition = new Vector3f(0f,0f,0f);
		this.rotation = new Vector3f(0f,0f,0f);
		
		this.renderingProgramBinding = renderingProgram;
	}
	
	public void loadModelData(){
		
		this.model = new ImportedModel(modelPath);
		this.primaryModelTexture = Utils.loadTexture(primaryTexturePath);
		
	}
	
	public void setupVertices(int vao[], int vaoIndex){
		
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		numObjVertices = model.getNumVertices();
		Vector3f[] vertices = model.getVertices();
		Vector2f[] texCoords = model.getTexCoords();
		Vector3f[] normals = model.getNormals();
		
		float[] pvalues = new float[numObjVertices*3];
		float[] tvalues = new float[numObjVertices*2];
		float[] nvalues = new float[numObjVertices*3];
		
		for (int i=0; i<numObjVertices; i++)
		{	pvalues[i*3]   = (float) (vertices[i]).x();
			pvalues[i*3+1] = (float) (vertices[i]).y();
			pvalues[i*3+2] = (float) (vertices[i]).z();
			tvalues[i*2]   = (float) (texCoords[i]).x();
			tvalues[i*2+1] = (float) (texCoords[i]).y();
			nvalues[i*3]   = (float) (normals[i]).x();
			nvalues[i*3+1] = (float) (normals[i]).y();
			nvalues[i*3+2] = (float) (normals[i]).z();
		}
		
		gl.glGenVertexArrays(vao.length, vao, 0);
		//vaoIndex would typically be 0 for this class
		gl.glBindVertexArray(vao[vaoIndex]);
		gl.glGenBuffers(modelVBO.length, modelVBO, 0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, modelVBO[0]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, modelVBO[1]);
		FloatBuffer texBuf = Buffers.newDirectFloatBuffer(tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()*4, texBuf, GL_STATIC_DRAW);
		
	}
	
	public void translate(Vector3f move){
		this.globalPosition.add(move);
	}
	
	public void rotate(Vector3f move){
		this.rotation.add(move);
		
		float x = this.rotation.x();
		float y = this.rotation.y();
		float z = this.rotation.z();
		
		x %= 6.28319f;
		y %= 6.28319f;
		z %= 6.28319f;
		
		this.rotation = new Vector3(x,y,z);
	}
	
	public Vector3f getPosition(){
		return(this.globalPosition);
	}
	
	public Vector3f getRotation(){
		return(this.rotation);
	}
	
	public void startRenderer(){
		
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glUseProgram(this.renderingProgramBinding);
	}
	
	public void render(Matrix4f viewMatrix){
		
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		/*
		
		Referring to the code from Chapter 6 program 3 what we could do
		is completely avoid using the matrix stack and use individual matrices for the model matrix, view matrix, and the perspective matrix and then multiply them all together when rendering before passing them to the rendering program
		
		*/
		
		//Get the matrices 
		int mvLoc = gl.glGetUniformLocation(renderingProgram, "mv_matrix");
		int pLoc = gl.glGetUniformLocation(renderingProgram, "p_matrix");
		
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(mvMatrixBuffer));
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, this.primaryModelTexture);
		
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		gl.glDrawArrays(GL_TRIANGLES, 0, myModel.getNumVertices());
		
	}
}