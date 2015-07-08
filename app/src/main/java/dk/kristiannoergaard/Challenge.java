package dk.kristiannoergaard;

public class Challenge implements Cloneable {
	public int resid;
	public String answer;
	public String avoid;
	public String options;
	
	public Object clone() {
		try {
			return super.clone();
		}
		catch( CloneNotSupportedException e ) {
	         return null;	
		}
	}
}
