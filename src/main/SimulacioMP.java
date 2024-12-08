package main;

public class SimulacioMP {

	public static void main(String[] args) {
		int type = Integer.parseInt(args[0]);
		int calcOrder = Integer.parseInt(args[1]);
		
		long start = System.currentTimeMillis();
		double result = SimulacioMT.simulation(type);
		long finish = System.currentTimeMillis();
		
		Simulador.createProteinFile(Simulador.CalcType.multiprocess, type, calcOrder, start, finish, result);
	}

}
