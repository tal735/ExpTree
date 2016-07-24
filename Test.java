package test;

public class Test {
	
	public static void main(String[] args) {
		boolean x,y,z;
		
		for (int i=0; i<2 ; i++){
			x=(i!=0);
			for (int j=0; j<2 ; j++){
				y=(j!=0);
				for (int k=0; k<2 ; k++){
					z=(k!=0);
					System.out.println("x=" + x + ", y=" + y + ", z=" + z + ", x||y&&z==" + (x||y&&z));
				}
			}
		}
		
		
		
		System.out.println("-------------------------------------------");
		
		for (int i=0; i<2 ; i++){
			x=(i!=0);
			for (int j=0; j<2 ; j++){
				y=(j!=0);
				for (int k=0; k<2 ; k++){
					z=(k!=0);
					System.out.println("x=" + x + ", y=" + y + ", z=" + z + ", x&z||y==" + (x&&z||y));
				}
			}
		}
	}
	
}
