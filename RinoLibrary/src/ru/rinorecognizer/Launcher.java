package ru.rinorecognizer;

public class Launcher {

	public static void main(String[] args) {
		
		ExtendedCommand extCommand = new ExtendedCommand();
		extCommand.curCommand = "позвони васе по номеру 8 917 123";
		extCommand.prevComplete = 1;
		extCommand.prevType = 0;
		
		CommandFeaturesGetter c_getter = new CommandFeaturesGetter();
		int[] c_vector = c_getter.getVector(extCommand);
		
		for (int i = 0; i < c_vector.length; i++)
			System.out.print(c_vector[i] + " ");

		System.out.println();
		System.out.println();

		WordsFeaturesGetter w_getter = new WordsFeaturesGetter();
		int[][] w_vectors = w_getter.getVectors("позвони васе по номеру 8 917 123");
		
		for (int i = 0; i < w_vectors.length; i++) {
			for (int j = 0; j < w_vectors[i].length; j++)
				System.out.print(w_vectors[i][j] + " ");
			System.out.println();
		}
		
		
	}

}
