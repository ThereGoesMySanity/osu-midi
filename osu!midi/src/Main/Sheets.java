package Main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class Sheets{
	private List<String> s;
	private double msPerBeat;
	private double offset;
	public double getOffset() {
		return offset;
	}
	private String file;

	private LinkedList<Integer[]> beats = new LinkedList<Integer[]>();
	private ArrayList<Double[]> timingPoints = new ArrayList<Double[]>();
	public LinkedList<Integer[]> getBeats() {
		return beats;
	}
	public Sheets(String s){
		file = s;
	}

	public void run(){
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
		str = s.remove(0).split(",");
		while(str.length>1){
			timingPoints.add(new Double[]{Double.parseDouble(str[0]),Double.parseDouble(str[1])});
			str = s.remove(0).split(",");
		}
		

		while(!s.remove(0).equals("[HitObjects]"));
		str = s.remove(0).split(",");
		while(str.length>1){
			int time = Integer.parseInt(str[2]);
			int type = Integer.parseInt(str[3]);
			int repeats = 0;
			int duration = 0;
			
			
			if(timingPoints.get(0)[0]<=time){
				if(timingPoints.get(0)[1]>0){
					msPerBeat = timingPoints.get(0)[1];
				}
			}
			
			if(type!=4){
				if(type/2%2==1){
					duration = (int) (msPerBeat * (Float.parseFloat(str[7]) / sliderMultiply) / 100);
					repeats = Integer.parseInt(str[6]);
				}
				if(type/8==1){
					repeats = 1;
					duration = Integer.parseInt(str[5])-Integer.parseInt(str[2]);
					
				}
				beats.add(new Integer[]{time, type, duration, repeats});
				if(s.size()==0)break;
				str = s.remove(0).split(",");
			}
		}
	}
}
