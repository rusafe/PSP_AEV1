package main;

public class SimulacioMT implements Runnable {
	private int type;
	private int calcOrder;
	
	public SimulacioMT(int type, int calcOrder) {
		this.type = type;
		this.calcOrder = calcOrder;
	}
	
	public void run() {
		
	}
	
	public static double simulation(int type) {
		double calc = 0.0;
		double simulationTime = Math.pow(5, type);
		double startTime = System.currentTimeMillis();
		double endTime = startTime + simulationTime;
		while (System.currentTimeMillis() < endTime) {
			calc = Math.sin(Math.pow(Math.random(), 2));
		}
		return calc;
	}
}
