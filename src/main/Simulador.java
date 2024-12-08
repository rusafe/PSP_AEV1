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

public class Simulador extends JFrame {

	public static enum CalcType {multiprocess, multithread}
	public static final String[] CALC_TYPE_STRING = new String[] {"MP", "MT"};
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

	public Simulador() {
		setTitle("AlphaFold");
		initComponents();
		initEventHandlers();
	}
	
	public static void createProteinFile(CalcType calcType, int proteinType, int order, long start, long finish, double result) {		
		String calcTime = String.format("%.2f", (finish - start) / 1000f).replace('.', '_');
		String startDate = formatMilliToDate(start);
		String finishDate = formatMilliToDate(finish);
		
		String fileName = String.format("PROT_%s_%d_n%d_%s.sim", CALC_TYPE_STRING[calcType.ordinal()], proteinType, order, startDate);
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
	
	public static String formatMilliToDate(long millisecondsFromEpoch) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss_SS");
		return dateFormat.format(millisecondsFromEpoch);
	}
	
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
	
	private void executeMp(int type, int calcOrder) {
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
			builder.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void waitForFilesToBeCreated(File directory, int expectedFileCount) {
		while(directory.listFiles().length != expectedFileCount) {}
	}
	
	private void waitForFilesToHaveContent(File[] files) {
		boolean anyFileEmpty;
		do {
			anyFileEmpty = false;
			for(File file : files) {
				try (BufferedReader br = new BufferedReader(new FileReader(file))) {
					String line = br.readLine();
					if(line == null) {
						anyFileEmpty = true;
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} while(anyFileEmpty);
	}
	
	private void waitForThreadsToFinish(ArrayList<Thread> threads) {
		for(Thread thread : threads) {
			try {
				thread.join();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void initEventHandlers() {
		btnSimulate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				prepareProteinStorageDirectory();
				
				long start, finish;
				
				JSpinner[] proteinSpinners = new JSpinner[] { spnTypeOne, spnTypeTwo, spnTypeThree, spnTypeFour };
				
				int expectedFileCount = 0;
				start = System.currentTimeMillis();
				for(int i = 0; i < proteinSpinners.length; i++) {
					for(int j = 1; j <= (int)proteinSpinners[i].getValue(); j++) {
						executeMp(i + 1, j);
						expectedFileCount++;
					}
				}
				File proteinStorageDirectory = new File(PROTEIN_STORAGE_DIRECTORY);
				waitForFilesToBeCreated(proteinStorageDirectory, expectedFileCount);
				waitForFilesToHaveContent(proteinStorageDirectory.listFiles());
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
		spnTypeOne.setBounds(100, 143, 42, 20);
		contentPane.add(spnTypeOne);
		
		spnTypeTwo = new JSpinner();
		spnTypeTwo.setBounds(263, 143, 46, 20);
		contentPane.add(spnTypeTwo);
		
		spnTypeThree = new JSpinner();
		spnTypeThree.setBounds(100, 214, 42, 20);
		contentPane.add(spnTypeThree);
		
		spnTypeFour = new JSpinner();
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
		lblMultiprocess.setBounds(18, 300, 122, 14);
		contentPane.add(lblMultiprocess);
		lblMultiprocess.setVisible(false);
		
		lblMultithread = new JLabel("Multihilo");
		lblMultithread.setBounds(304, 300, 122, 14);
		contentPane.add(lblMultithread);
		lblMultithread.setVisible(false);
		
		setVisible(true);		
	}
}
