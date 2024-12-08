package main;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class Simulador extends JFrame {
	/**
	 * Enum con los tipos de calculos (multiproceso, multihilo)
	 */
	public static enum CalcType {multiprocess, multithread}
	/**
	 * Array que relaciona los tipos de calculos del enum con su string
	 */
	public static final String[] CALC_TYPE_STRING = new String[] {"MP", "MT"};
	/**
	 * Donde se guardan los ficheros de las proteinas
	 */
	private static final String PROTEIN_STORAGE_DIRECTORY = "resources/proteins";
	
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JButton btnSimulate;
	private JSpinner spnTypeOne;
	private JSpinner spnTypeTwo;
	private JSpinner spnTypeThree;
	private JSpinner spnTypeFour;
	private JLabel lblMultiprocess;
	private JLabel lblMultithread;

	/**
	 * Metodo principal de la aplicacion
	 * @param args Argumentos
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Simulador frame = new Simulador();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Constructor que inicializa la interfaz y los manejadores de eventos
	 */
	public Simulador() {
		setTitle("AlphaFold");
		initComponents();
		initEventHandlers();
	}
	
	/**
	 * Metodo que crea un fichero en base a los datos de la proteina
	 * @param calcType Si es multiproceso o multihilo
	 * @param proteinType Tipo de la proteina
	 * @param order Orden en la lista de calculos
	 * @param start Cuando se empieza a calcular
	 * @param finish Cuando se termina de calcular
	 * @param result El resultado del calculo
	 */
	public static void createProteinFile(CalcType calcType, int proteinType, int order, long start, long finish, double result) {		
		String calcTime = String.format("%.2f", (finish - start) / 1000f).replace(',', '_');
		String startDate = formatMilliToDate(start);
		String finishDate = formatMilliToDate(finish);
		
		String fileName = String.format("PROT_%s_%d_n%d_%s", CALC_TYPE_STRING[calcType.ordinal()], proteinType, order, startDate);
		fileName = String.format("%s.sim", fileName.substring(0, fileName.length() - 1));
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(String.format("%s/%s", PROTEIN_STORAGE_DIRECTORY, fileName)))){
			bw.write(startDate);
			bw.newLine();
			bw.write(finishDate);
			bw.newLine();
			bw.write(calcTime);
			bw.newLine();
			bw.write(String.valueOf(result));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Metodo que crea una fecha con el formato especifico en base a los milisegundos introducidos
	 * @param millisecondsFromEpoch Los milisegundos de los que sacar la fecha
	 * @return La fecha con el formato adecuado
	 */
	public static String formatMilliToDate(long millisecondsFromEpoch) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss_SS");
		return dateFormat.format(millisecondsFromEpoch);
	}
	
	/**
	 * Metodo que prepara el directorio donde se van a almacenar los ficheros con los datos de las proteinas
	 */
	private static void prepareProteinStorageDirectory() {
		File storageDirectory = new File(PROTEIN_STORAGE_DIRECTORY);
		if(!storageDirectory.exists())
			storageDirectory.mkdirs();
		else {
			for(File file : storageDirectory.listFiles()) {
				file.delete();
			}
		}
	}
	
	/**
	 * Metodo que ejecuta un proceso paralelo con la simulacion de una proteina
	 * @param type Tipo de la proteina
	 * @param calcOrder Orden en la lista de procesos
	 * @return El proceso que se crea
	 */
	private Process executeMp(int type, int calcOrder) {
		try {
			String className = "main.SimulacioMP";
			String javaHome = System.getProperty("java.home");
			String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
			String classpath = System.getProperty("java.class.path");
			
			ArrayList<String> command = new ArrayList<>();
			command.add(javaBin);
			command.add("-cp");
			command.add(classpath);
			command.add(className);
			command.add(String.valueOf(type));
			command.add(String.valueOf(calcOrder));
			
			ProcessBuilder builder = new ProcessBuilder(command);
			Process process = builder.start();
			return process;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Metodo que espera a que todos los procesos terminen de ejecutarse
	 * @param processes Array con los procesos
	 */
	private void waitForProcessesToFinish(ArrayList<Process> processes) {
		for(Process process : processes) {
			try {
				process.waitFor();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Metodo que espera a que todos los hilos terminen de ejecutarse
	 * @param threads Array con los hilos
	 */
	private void waitForThreadsToFinish(ArrayList<Thread> threads) {
		for(Thread thread : threads) {
			try {
				thread.join();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Metodo que inicia los manejadores de eventos de los componentes visuales
	 */
	private void initEventHandlers() {
		btnSimulate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				prepareProteinStorageDirectory();
				
				long start, finish;
				
				JSpinner[] proteinSpinners = new JSpinner[] { spnTypeOne, spnTypeTwo, spnTypeThree, spnTypeFour };
				
				ArrayList<Process> processes = new ArrayList<Process>();
				start = System.currentTimeMillis();
				for(int i = 0; i < proteinSpinners.length; i++) {
					for(int j = 1; j <= (int)proteinSpinners[i].getValue(); j++) {
						processes.add(executeMp(i + 1, j));
					}
				}
				waitForProcessesToFinish(processes);
				finish = System.currentTimeMillis();
				double durationMp = (finish - start) / 1000f;
				
				ArrayList<Thread> threads = new ArrayList<Thread>();
				start = System.currentTimeMillis();
				for(int i = 0; i < proteinSpinners.length; i++) {
					for(int j = 1; j <= (int)proteinSpinners[i].getValue(); j++) {
						Thread thread = new Thread(new SimulacioMT(i + 1, j));
						thread.start();
						threads.add(thread);
					}
				}
				waitForThreadsToFinish(threads);
				finish = System.currentTimeMillis();
				double durationMt = (finish - start) / 1000f;
				
				lblMultiprocess.setText(String.format("Multiproces: %.2fs", durationMp));
				lblMultithread.setText(String.format("Multifil: %.2fs", durationMt));
				lblMultiprocess.setVisible(true);
				lblMultithread.setVisible(true);
			}
		});
	}
	
	/**
	 * Metodo que inicia los componentes visuales
	 */
	private void initComponents() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 399);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblTitleName = new JLabel("AlphaFold");
		lblTitleName.setFont(new Font("Tahoma", Font.PLAIN, 24));
		lblTitleName.setBounds(148, 34, 116, 29);
		contentPane.add(lblTitleName);
		
		JLabel lblTitleDescription = new JLabel("Simular Proteines");
		lblTitleDescription.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lblTitleDescription.setBounds(126, 67, 175, 29);
		contentPane.add(lblTitleDescription);
		
		btnSimulate = new JButton("Simular");
		btnSimulate.setFont(new Font("Tahoma", Font.PLAIN, 16));
		btnSimulate.setBounds(157, 288, 97, 34);
		contentPane.add(btnSimulate);
		
		spnTypeOne = new JSpinner();
		spnTypeOne.setModel(new SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
		spnTypeOne.setBounds(100, 143, 42, 20);
		contentPane.add(spnTypeOne);
		
		spnTypeTwo = new JSpinner();
		spnTypeTwo.setModel(new SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
		spnTypeTwo.setBounds(263, 143, 46, 20);
		contentPane.add(spnTypeTwo);
		
		spnTypeThree = new JSpinner();
		spnTypeThree.setModel(new SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
		spnTypeThree.setBounds(100, 214, 42, 20);
		contentPane.add(spnTypeThree);
		
		spnTypeFour = new JSpinner();
		spnTypeFour.setModel(new SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
		spnTypeFour.setBounds(263, 214, 46, 20);
		contentPane.add(spnTypeFour);
		
		JLabel lblTypeOne = new JLabel("Estructura Primaria");
		lblTypeOne.setBounds(67, 117, 126, 14);
		contentPane.add(lblTypeOne);
		
		JLabel lblTypeTwo = new JLabel("Estructura Secundaria");
		lblTypeTwo.setBounds(226, 117, 139, 14);
		contentPane.add(lblTypeTwo);
		
		JLabel lblTypeThree = new JLabel("Estructura Terciaria");
		lblTypeThree.setBounds(69, 189, 124, 14);
		contentPane.add(lblTypeThree);
		
		JLabel lblTypeFour = new JLabel("Estructura Quaternaria");
		lblTypeFour.setBounds(226, 189, 139, 14);
		contentPane.add(lblTypeFour);
		
		lblMultiprocess = new JLabel("Multiproceso");
		lblMultiprocess.setBounds(25, 300, 122, 14);
		contentPane.add(lblMultiprocess);
		lblMultiprocess.setVisible(false);
		
		lblMultithread = new JLabel("Multihilo");
		lblMultithread.setBounds(276, 300, 122, 14);
		contentPane.add(lblMultithread);
		lblMultithread.setVisible(false);
		
		setVisible(true);		
	}
}
