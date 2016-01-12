package Main;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;


public class Main {
	private static final double SPEED = 0.1;
	private static final int DELAY = -10;
	private JFrame frame;
	private String install_dir = "C:\\Program Files (x86)\\osu!\\Songs";
	private Sheets s;
	private ArrayList<Integer[]> beats;
	private Player p;
	private JPanel panel;
	private BufferedImage img;
	private Graphics2D g;
	private Graphics g2;

	private long startTime = Long.MIN_VALUE;
	private boolean playing = false;
	public static void main(String[] args){
		new Main();
	}
	public Main(){
		JukeBox.init();
		JukeBox.load("/soft-hitclap.wav", "hitsound");
		JukeBox.setVolume("hitsound", -3.0f);
		JFileChooser j = new JFileChooser();
		img = new BufferedImage(400, 360, BufferedImage.TYPE_INT_RGB);
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		try {
			UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		HashMap<String, String[]> a = showFiles(new File(install_dir).listFiles());
		ArrayList<String> c = new ArrayList<String>(a.keySet());
		Collections.sort(c);
		String[] b = new String[a.size()];
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {400, 0};
		gridBagLayout.rowHeights = new int[] {120, 120, 120, 0};
		gridBagLayout.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		frame.getContentPane().setLayout(gridBagLayout);


		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridheight = 2;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		frame.getContentPane().add(scrollPane, gbc_scrollPane);



		JMenuBar menuBar = new JMenuBar();
		scrollPane.setColumnHeaderView(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);


		JMenuItem mntmExportMidi = new JMenuItem("Export midi");
		mntmExportMidi.setEnabled(false);
		mntmExportMidi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				j.setDialogTitle("Save midi");
				if(j.showSaveDialog(frame)==JFileChooser.APPROVE_OPTION){
					try {
						s.save(j.getSelectedFile());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		mnFile.add(mntmExportMidi);

		JMenu mnPlay = new JMenu("Play");
		menuBar.add(mnPlay);
		JMenuItem mntmStop = new JMenuItem("Stop");
		mntmStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				p.close();
				playing = false;
				mntmStop.setEnabled(false);
			}
		});
		JMenuItem mntmMidi = new JMenuItem("Midi");
		mntmMidi.setEnabled(false);
		mntmMidi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread t = new Thread() {

					public void run() {
						new ArrayList<Integer[]>();
						playing = true;
						startTime = System.currentTimeMillis();
					}
				};
				t.start();
				mntmStop.setEnabled(true);
			}
		});
		mnPlay.add(mntmMidi);

		JMenuItem mntmMidiAndSong = new JMenuItem("Midi and song");
		mntmMidiAndSong.setEnabled(false);
		mntmMidiAndSong.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread t = new Thread() {
					public void run() {
						try { 
							playing = true;
							startTime = System.currentTimeMillis();
							p.play(); 
							new ArrayList<Integer[]>();

						}
						catch (Exception e) {e.printStackTrace();}
					}
				};
				t.start();


				mntmStop.setEnabled(true);
			}
		});
		mnPlay.add(mntmMidiAndSong);

		mntmStop.setEnabled(false);
		mnPlay.add(mntmStop);

		JList<String> list = new JList<String>();
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				if(!list.isSelectionEmpty()){
					s = new Sheets(a.get(list.getSelectedValue())[0]);

					try {
						p = new Player(new FileInputStream(a.get(list.getSelectedValue())[1]));
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JavaLayerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					s.run();
					beats = s.getBeats();
					
				}
				mntmMidi.setEnabled(!list.isSelectionEmpty());
				mntmMidiAndSong.setEnabled(!list.isSelectionEmpty());
				mntmExportMidi.setEnabled(!list.isSelectionEmpty());
			}
		});
		if(c.size()>0){
			list.setListData(c.toArray(b));
		}else{
			list.setListData(new String[]{"Can't find any songs! Try changing the installation directory."});
			list.setSelectionModel(new DisabledItemSelectionModel());
		}
		scrollPane.setViewportView(list);

		JMenuItem mntmChangeOsuInstall = new JMenuItem("Change osu! install location");
		mntmChangeOsuInstall.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				j.setDialogTitle("Choose the Songs folder");
				j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if(j.showOpenDialog(frame)==JFileChooser.APPROVE_OPTION){
					list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					install_dir = j.getSelectedFile().getAbsolutePath();
				}
			}
		});
		mnFile.add(mntmChangeOsuInstall);

		panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 2;
		frame.getContentPane().add(panel, gbc_panel);
		FlowLayout fl_panel = new FlowLayout(FlowLayout.CENTER, 0, 0);
		panel.setLayout(fl_panel);
		g = (Graphics2D) img.getGraphics();
		g2 = panel.getGraphics();

		frame.setSize(400, 360);
		frame.setResizable(false);
		frame.setVisible(true);
		run();
	}
	public void run(){
		while(true){
			draw();
		}
	}
	public void draw(){
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, 400, 360);
		
		if(playing){
			for(Integer[] i : beats){
				int off = (int)((i[0]+startTime-System.currentTimeMillis())*SPEED);
				if(off>400)break;
				if(off-DELAY==0){
					JukeBox.play("hitsound");
				}
				if(i[1]/2%2==1)g.setColor(Color.BLUE);
				else g.setColor(Color.CYAN);
				g.fillRect(40+off-DELAY, 0, (int) (i[2]*i[3]*SPEED), 120);
				for(int k = 1; k <= i[3]; k++){
					g.setColor(Color.ORANGE);
					g.fillRect((int) (40+off-DELAY+(i[2]*k*SPEED)), 0, 2, 120);
				}
				g.setColor(Color.RED);
				g.fillRect(40+off-DELAY, 0, 2, 120);
				g.setColor(Color.GREEN);
				g.fillRect(36, 0, 4, 120);
			}
		}
		g2 = panel.getGraphics();
		g2.drawImage(img, 0, 0, null);
	}
	public LinkedHashMap<String, String[]> showFiles(File[] file){
		LinkedHashMap<String, String[]> files = new LinkedHashMap<String, String[]>();
		for(File f : file){
			if (f.isDirectory()) {

				files.putAll(showFiles(f.listFiles()));
			} else if(f.getPath().substring(f.getPath().length()-3).equals("osu")){
				List<String> a = null;
				try {
					a = Files.readAllLines(f.toPath());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				int i = 0;
				while(!a.get(i).contains("Title:"))i++;
				String title = a.get(i).substring(6);

				i=0;
				while(!a.get(i).contains("Version:"))i++;
				String version = a.get(i).substring(8);

				String mp3 = "";
				for(File fi : file){
					if(fi.getPath().substring(fi.getPath().length()-3).equals("mp3"))
						mp3=fi.getAbsolutePath();
				}

				files.put(title+ " ["+version+"]", new String[]{f.getPath(),mp3});
			}
		}
		return files;
	}
}
