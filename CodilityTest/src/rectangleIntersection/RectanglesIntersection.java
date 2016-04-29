package rectangleIntersection;

public class RectanglesIntersection {
	private static final int A[] = {2, 4, -6, -9, 21};
	
	
	public static void main(String[] args) {
		
		
		float m = mean(A); //helper function mean
		int extrmindx = 0; //index of extreme element
		for (int j = 1; j < A.length; j++) //check each element for greatest deviation
			if ( Math.abs(A[j] - m) > Math.abs(A[extrmindx] - m) )
				extrmindx = j;
		System.out.println(extrmindx);
	}
	
	public static float mean(int[] A) {
		float sum = 0;
		for (int i = 0; i < A.length; i++)
			sum += A[i];
		return sum / (float) A.length;
	}
}
