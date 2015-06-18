package succursale;

import java.io.IOException;

public class Console {

	public static void main(String[] args) throws IOException {	
		String test = "0.0.0.0|localhost|hello";
		String part[] = test.split("\\|");
		for (String string : part) {
			System.out.println("part="+string);
		}
	}
}
