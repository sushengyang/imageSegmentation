package trifonov.stanislav.ml;

public class DataPoint {
	public int _r;
	public int _g;
	public int _b;
	
	public DataPoint(int r, int g, int b) {
		_r = r;
		_g = g;
		_b = b;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof DataPoint)
			return false;
		else {
			DataPoint other = (DataPoint) obj;
			return this == other || (_r == other._r && _g == other._g && _b == other._b);
		}
	}
}