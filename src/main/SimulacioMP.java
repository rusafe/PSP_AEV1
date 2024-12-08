package main;

public class SimulacioMP {

	public static void main(String[] args) {
		int type = Integer.parseInt(args[0]);
		
		SimulacioMT.simulation(type);
	}

}
