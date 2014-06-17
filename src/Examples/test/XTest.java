package test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.objectweb.proactive.core.util.CircularArrayList;

public class XTest {

	public static class Cosa implements Serializable {
		private static final long serialVersionUID = 1L;
		public String str;
		
		public Cosa(String s) { str = s; }

		public String toString() {
			return str;
		}
	}

	public static void main(String[] args) throws Exception {
		CircularArrayList<Cosa> cal = new CircularArrayList<Cosa>();
		Map<Integer, Cosa> map = new HashMap<Integer, Cosa>();
		
		
		for (int i = 0; i < 5; i++) {
			Cosa c = new Cosa("AAAAA");
			cal.add(c);
			map.put(i, c);
		}
		
		map.get(2).str = "BBBBB";
		System.out.println(cal);
	}

}
