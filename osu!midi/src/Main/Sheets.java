package Main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.jfugue.midi.MidiFileManager;
import org.jfugue.pattern.Pattern;
import org.jfugue.player.Player;
import org.jfugue.theory.Note;

public class Sheets{
	private List<String> s;
	private double msPerBeat;
	private double timeSig;
	private int offset;
	public int getOffset() {
		return offset;
	}
	private Pattern p = new Pattern("I115");
	private String file;
	
	private int length = 0;
	private ArrayList<Integer> beats = new ArrayList<Integer>();
	private Player pl;
	
	public Sheets(String s){
		file = s;
	}
	
	public void run(){
		pl = new Player();
		File f = new File(file);
		try {
			s = Files.readAllLines(f.toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(!s.remove(0).contains("[TimingPoints]"));
		String[] str = s.remove(0).split(",");
		offset= Integer.parseInt(str[0]);
		msPerBeat = Double.parseDouble(str[1]);
		timeSig = Double.parseDouble(str[2]);
		
		p.setTempo((int) (60000/msPerBeat));
		
		while(!s.remove(0).equals("[HitObjects]"));
		str = s.remove(0).split(",");
		offset+=Integer.parseInt(str[2]);
		while(!str[0].equals("")){
			beats.add(Integer.parseInt(str[2]));
			length=Integer.parseInt(str[2]);
			if(s.size()==0)break;
			str = s.remove(0).split(",");
		}
		for(int i = 1; i < length; i++){
			if(beats.contains(i)){
				int j=i+1;
				while(!beats.contains(j))j++;
				p.add("G4/"+(j-i)/(msPerBeat*timeSig));
			}
		}


	}
	public void altPlay(boolean f){
		pl = new Player();
		File f1 = new File(file);
		try {
			s = Files.readAllLines(f1.toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(!s.remove(0).contains("[TimingPoints]"));
		String[] str = s.remove(0).split(",");
		offset= Integer.parseInt(str[0]);
		msPerBeat = Double.parseDouble(str[1]);
		timeSig = Double.parseDouble(str[2]);
		
		p.setTempo((int) (60000/msPerBeat));
		
		while(!s.remove(0).equals("[HitObjects]"));
		str = s.remove(0).split(",");
		offset+=Integer.parseInt(str[2]);
		while(s.size()>0){
			beats.add(Integer.parseInt(str[2]));
			length=Integer.parseInt(str[2]);
			str = s.remove(0).split(",");
		}
		long start = System.nanoTime();
		List<Integer> notes = beats;
		while(notes.size()>0){
			long nanos = (beats.get(0)+offset)*1000000-(System.nanoTime()-start);
			if(nanos<0)nanos=0;
					
			try {
				Thread.sleep(nanos/1000000, (int) (nanos%1000000));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			pl.play("G4x");
		}
	}
	public void play(){
		pl.play(p);
	}
	
	public void save(File f) throws IOException{
		MidiFileManager.savePatternToMidi(p, f);
	}
	public void stop(){
		pl.play("");
	}
}
