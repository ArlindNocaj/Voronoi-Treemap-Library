package kn.uni.voronoitreemap.core;

public class VoroSettings {
	
	public boolean cancelAreaError=true;
	public double  errorThreshold=0.03;
	
	public boolean cancelMaxIterat=true;
	public int maxIterat=800;
	
	public boolean cancelOnLocalError=true;
	
	public double boostConvergence=1.0;		
	
	@Override
	public VoroSettings clone(){
		VoroSettings s = new VoroSettings();
		s.boostConvergence=boostConvergence;
		s.cancelAreaError=cancelAreaError;
		s.cancelMaxIterat=cancelMaxIterat;
		s.errorThreshold=errorThreshold;
		s.maxIterat=maxIterat;
		
		return s;
	
	}
}
