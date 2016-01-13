package Main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.jfugue.midi.MidiFileManager;
import org.jfugue.pattern.Pattern;
import org.jfugue.player.Player;

public class Sheets{
	private List<String> s;
	private double msPerBeat;
	private int timeSig;
	private double offset;
	public double getOffset() {
		return offset;
	}
	private Pattern p = new Pattern("I115");
	private String file;

	private int length = 0;
	private ArrayList<Integer[]> beats = new ArrayList<Integer[]>();
	private ArrayList<Integer> timingPoints = new ArrayList<Integer>();
	public ArrayList<Integer[]> getBeats() {
		return beats;
	}
	public Sheets(String s){
		file = s;
	}

	public void run(){
		new Player();
		File f = new File(file);
		try {
			s = Files.readAllLines(f.toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(!s.remove(0).contains("[Difficulty]"));
		int i = -1;
		while(!s.get(++i).contains("SliderMultiplier"));
		double sliderMultiply = Double.parseDouble(s.get(i).substring(17));
		while(!s.remove(0).contains("[TimingPoints]"));
		String[] str = s.remove(0).split(",");
		offset= Double.parseDouble(str[0]);
		msPerBeat = Double.parseDouble(str[1]);
		timeSig = Integer.parseInt(str[2]);

		p.setTempo((int) (60000/msPerBeat));

		while(!s.remove(0).equals("[HitObjects]"));
		str = s.remove(0).split(",");
		while(!str[0].equals("")){
			double pxPerBeat;
			double beatsNumber;
			double velocity = 1;
			int type = Integer.parseInt(str[3]);
			int repeats = 0;
			int duration = 0;

			if(type!=4){
				if(type/2%2==1){
					pxPerBeat = sliderMultiply * 100 * velocity;
					repeats = Integer.parseInt(str[6]);
					beatsNumber = Double.parseDouble(str[7])/ pxPerBeat;
					duration = (int) Math.ceil(beatsNumber * msPerBeat);
				}
				if(type/8==1){
					repeats = 1;
					duration = Integer.parseInt(str[5])-Integer.parseInt(str[2]);
					
				}
				beats.add(new Integer[]{Integer.parseInt(str[2]), type, duration, repeats});
				length=Integer.parseInt(str[2]);
				if(s.size()==0)break;
				str = s.remove(0).split(",");
			}
		}
		for(int i1 = 1; i1 < length; i1++){  //I'm so sorry for these naming conventions
			if(beats.contains(i1)){
				int j=i1+1;
				while(!beats.contains(j))j++;
				p.add("G4/"+(j-i1)/(msPerBeat*timeSig));
			}
		}


	}

	public void save(File f) throws IOException{
		MidiFileManager.savePatternToMidi(p, f);
	}
}
