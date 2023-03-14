package code;

import java.awt.event.*;
import java.lang.String;

public class AxisKeyState implements KeyListener{
	
	private float multiplier;
	String upKey;
	boolean upState;
	String downKey;
	boolean downState;
	
	public AxisKeyState(float multiplier, String upKey, String downKey){
		
		this.multiplier = multiplier;
		this.upKey = upKey;
		this.upState = false;
		this.downKey = downKey;
		this.downState = false;
		
	}
	
	public void keyPressed(KeyEvent e){
		String keyString = KeyEvent.getKeyText(e.getKeyCode()).toLowerCase();
		
		if(keyString == this.downKey.toLowerCase()){
			this.downState = true;
		}
		if(keyString == this.upKey.toLowerCase()){
			this.upState = true;
		}
	}
	
	public void keyReleased(KeyEvent e){
		String keyString = KeyEvent.getKeyText(e.getKeyCode()).toLowerCase();
		
		
		if((keyString == this.downKey.toLowerCase())){
			this.downState = false;
		}
		if((keyString == this.upKey.toLowerCase())){
			this.upState = false;
		}
		
	}
	
	public void keyTyped(KeyEvent e){
		
	}
	
	public float getValue(){
		float value = (this.downState ^ this.upState) ? this.multiplier : 0;
		
		value *= this.downState ? -1 : 1;
		
		return(value);
		
	}
}