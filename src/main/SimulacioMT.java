package main;

public class SimulacioMT implements Runnable {
	/**
	 * Tipo de la proteina
	 */
	private int type;
	/**
	 * Orden en la lista de calculos
	 */
	private int calcOrder;
	
	/**
	 * Constructor principal de la clase
	 * @param type Tipo de la proteina
	 * @param calcOrder Orden en la lista de calculos
	 */
	public SimulacioMT(int type, int calcOrder) {
		this.type = type;
		this.calcOrder = calcOrder;
	}
	
	public void run() {
		long start = System.currentTimeMillis();
		double result = SimulacioMT.simulation(type);
		long finish = System.currentTimeMillis();
		
		Simulador.createProteinFile(Simulador.CalcType.multithread, type, calcOrder, start, finish, result);
	}
	
	/**
	 * Metodo que simula la estructura tridimensional de una proteina
	 * @param type Tipo de la proteina
	 * @return Resultado del calculo
	 */
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
