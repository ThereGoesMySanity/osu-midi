package Main;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;


public class Main {
	private JFrame frame;
	private String install_dir = "C:\\Program Files (x86)\\osu!\\Songs";
	private Sheets s;
	private Player p;
	public static void main(String[] args){
		new Main();
	}
	public Main(){
		JFileChooser j = new JFileChooser();
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

		JScrollPane scrollPane = new JScrollPane();
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);



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
				s.stop();
				p.close();
				mntmStop.setEnabled(false);
			}
		});
		JMenuItem mntmMidi = new JMenuItem("Midi");
		mntmMidi.setEnabled(false);
		mntmMidi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread t = new Thread() {
					public void run() {
						s.play();
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
							long millis = System.currentTimeMillis();
							p.play(); 
							Thread.sleep(System.currentTimeMillis()-millis+s.getOffset());
							s.play();
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

		frame.setSize(400, 360);
		frame.setResizable(false);
		frame.setVisible(true);
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
