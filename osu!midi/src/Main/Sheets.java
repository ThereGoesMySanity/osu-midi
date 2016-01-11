package Main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import org.jfugue.midi.MidiFileManager;
import org.jfugue.pattern.Pattern;
import org.jfugue.player.Player;
import org.jfugue.theory.Note;

public class Sheets{
	private Scanner s;
	private double msPerBeat;
	private double timeSig;
	private int offset;
	private Pattern p = new Pattern("I115");
	private String file;
	
	private int length = 0;
	private HashMap<Integer,Integer> beats = new HashMap<Integer,Integer>();
	private Player pl;
	
	public Sheets(String s){
		file = s;
	}
	
	public void run(){
		try {
			pl = new Player();
			File f = new File(file);
			s = new Scanner(f);
			while(!s.nextLine().equals("[TimingPoints]"));
			String[] str = s.nextLine().split(",");
			offset= Integer.parseInt(str[0]);
			msPerBeat = Double.parseDouble(str[1]);
			timeSig = Double.parseDouble(str[2]);
			
			p.setTempo((int) (60000/msPerBeat));
			
			while(!s.nextLine().equals("[HitObjects]"));
			str = s.nextLine().split(",");
			while(!str[0].equals("")){
				beats.put(Integer.parseInt(str[2]), Integer.parseInt(str[3]));
				length=Integer.parseInt(str[2]);
				if(!s.hasNextLine())break;
				str = s.nextLine().split(",");
			}
			for(int i = 1; i < length; i++){
				if(beats.containsKey(i)){
					int j=i+1;
					while(!beats.containsKey(j))j++;
					p.add("G4"+Note.getDurationString((j-i)/(msPerBeat*timeSig)));
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	public void play(){
		pl.delayPlay(offset, p);
	}
	
	public void save(File f) throws IOException{
		MidiFileManager.savePatternToMidi(p, f);
	}
	public void stop(){
		pl.play("");
	}
}
