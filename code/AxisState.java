package code;

public class AxisState{
	
	private boolean upState;
	private boolean downState;
	private float multiplier;
	
	public AxisState(float multiplier){
		this.upState = false;
		this.downState = false;
		this.multiplier = multiplier;
	}
	
	public void pressUp(){
		this.upState = true;
	}
	
	public void releaseUp(){
		this.upState = false;
	}
	
	public void pressDown(){
		this.downState = true;
	}
	
	public void releaseDown(){
		this.downState = false;
	}
	
	public float getValue(){
		float value = (this.downState ^ this.upState) ? this.multiplier : 0;
		
		value *= this.downState ? -1 : 1;
		
		return(value);
		
	}
	
}