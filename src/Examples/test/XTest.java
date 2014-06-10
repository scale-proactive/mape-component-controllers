package test;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class XTest {

	public static void main(String[] args) throws Exception {
		List<Integer> list = new ArrayList<Integer>();
		Object obj = list;
		System.out.println(obj.getClass());
	}
}
