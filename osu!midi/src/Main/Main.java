package Main;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;


public class Main {
	private static double speed = 0.1;
	private static int delay = -90;
	private static float volume = 0;
	private Preferences prefs = Preferences.userRoot().node("osu!midi");
	private JFrame frame;
	private String install_dir = "C:\\Program Files (x86)\\osu!";
	private Sheets sheet;
	private LinkedList<Integer[]> beats;
	private Player player;
	private Clip clip;
	private FloatControl vol;
	private JPanel panel;
	private BufferedImage img;
	private Graphics2D g;
	private Graphics g2;
	private long startTime = Long.MIN_VALUE;
	private boolean playing = false;
	private JCheckBoxMenuItem sliderRepeat;
	public static void main(String[] args){
		new Main();
	}
	public Main(){
		volume = prefs.getFloat("volume", volume);
		speed = prefs.getDouble("speed", speed);
		delay = prefs.getInt("delay", delay);
		
		try {
			AudioInputStream ais =
				AudioSystem.getAudioInputStream(
					new BufferedInputStream(getClass().getResourceAsStream("/soft-hitclap.wav"))
				);
			AudioFormat baseFormat = ais.getFormat();
			AudioFormat decodeFormat = new AudioFormat(
				AudioFormat.Encoding.PCM_SIGNED,
				baseFormat.getSampleRate(),
				16,
				baseFormat.getChannels(),
				baseFormat.getChannels() * 2,
				baseFormat.getSampleRate(),
				false
			);
			AudioInputStream dais = AudioSystem.getAudioInputStream(decodeFormat, ais);
			clip = AudioSystem.getClip();
			clip.open(dais);
			vol = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
			setVolume(volume);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		setVolume(-3.0f);
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

		JMenu mnPlay = new JMenu("Play");
		menuBar.add(mnPlay);
		JMenuItem mntmStop = new JMenuItem("Stop");
		mntmStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				player.close();
				playing = false;
				mntmStop.setEnabled(false);
			}
		});
		JMenuItem mntmMidi = new JMenuItem("Midi");
		mntmMidi.setEnabled(false);
		mntmMidi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Thread t = new Thread() {

					@Override
					public void run() {
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
			@Override
			public void actionPerformed(ActionEvent e) {
				Thread t = new Thread() {
					@Override
					public void run() {
						try { 
							playing = true;
							startTime = System.currentTimeMillis();
							player.play(); 

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

		JMenuItem mntmSetSpeed = new JMenuItem("Set speed...");
		mntmSetSpeed.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				speed = sliderDialog(0, 50, (int) (speed*100), 5, "Set speed")/100.0;
				prefs.putDouble("speed", speed);
				try {
					prefs.flush();
				} catch (BackingStoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		JSeparator separator = new JSeparator();
		mnPlay.add(separator);
		mnPlay.add(mntmSetSpeed);

		JMenuItem mntmSetVolume = new JMenuItem("Set volume...");
		mntmSetVolume.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				volume = sliderDialog(-6, 6, (int) volume, 2, "Set volume");
				setVolume(volume);
				prefs.putFloat("volume", volume);
				try {
					prefs.flush();
				} catch (BackingStoreException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		mnPlay.add(mntmSetVolume);

		JMenuItem mntmSetDelay = new JMenuItem("Set delay...");
		mntmSetDelay.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				delay = sliderDialog(-200, 200, delay, 50,"Set delay" );
				prefs.putInt("delay", delay);
				try {
					prefs.flush();
				} catch (BackingStoreException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		mnPlay.add(mntmSetDelay);

		sliderRepeat = new JCheckBoxMenuItem("Enable slider repeat hitsounds");

		mnPlay.add(sliderRepeat);

		JList<String> list = new JList<String>();
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				if(!list.isSelectionEmpty()){
					sheet = new Sheets(a.get(list.getSelectedValue())[0]);

					try {
						player = new Player(new FileInputStream(a.get(list.getSelectedValue())[1]));
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JavaLayerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					sheet.run();
					beats = sheet.getBeats();
				}
				mntmMidi.setEnabled(!list.isSelectionEmpty());
				mntmMidiAndSong.setEnabled(!list.isSelectionEmpty());
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
			@Override
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

		JMenuItem mntmRunOsu = new JMenuItem("Run osu!");
		mntmRunOsu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(System.getProperty("os.name").toLowerCase().contains("windows")){
					try {
						Runtime.getRuntime().exec(install_dir+"\\osu!.exe");
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		mnFile.add(mntmRunOsu);

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
				int off = (int) (i[0]+startTime-System.currentTimeMillis());
				if(off*speed>400)break;
				if(off-delay<=0&&off-delay>-5){
					playSound();
				}
				if(i[1]/2%2==1)g.setColor(Color.BLUE);
				else g.setColor(Color.CYAN);
				g.fillRect((int) (40+(off-delay)*speed), 0, (int) (i[2]*i[3]*speed), 120);
				for(int k = 1; k <= i[3]; k++){
					if(sliderRepeat.isSelected()&&off-delay+i[2]*k<=0&&off-delay+i[2]*k>-5){
						playSound();
					}
					g.setColor(Color.ORANGE);
					g.fillRect((int) (39+(off-delay+i[2]*k)*speed), 0, 2, 120);
				}
				g.setColor(Color.RED);
				g.fillRect((int) (40+(off-delay)*speed), 0, 2, 120);
				g.setColor(Color.GREEN);
				g.fillRect(36, 0, 4, 120);
			}
		}
		g2 = panel.getGraphics();
		g2.drawImage(img, 0, 0, null);
	}
	public HashMap<String, String[]> showFiles(File[] file){
		HashMap<String, String[]> files = new HashMap<String, String[]>();
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
	private void playSound(){
		if(clip == null) return;
		if(clip.isRunning()) clip.stop();
		clip.setFramePosition(0);
		while(!clip.isRunning()) clip.start();
	}
	private void setVolume(float f){
		vol.setValue(f);
	}
	
	private int sliderDialog(int min, int max, int val, int tickSpacing, String text){
		JSlider s = new JSlider();
		s.setMaximum(max);
		s.setMinimum(min);
		s.setValue(val);
		s.setPaintTicks(true);
		s.setPaintLabels(true);
		s.setMajorTickSpacing(tickSpacing);
		JOptionPane.showOptionDialog(frame, text, text, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Component[]{s}, null);
		return s.getValue();
	}
}
