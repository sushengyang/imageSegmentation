package trifonov.stanislav.ml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KMeansClustering {

	private final int _k;
	private List<DataPoint> _dataPoints;
	private Set<DataPoint> _centroids;
	
	public KMeansClustering(int k) {
		_k = k;
	}

	public void setData(List<DataPoint> points) {
		_dataPoints = points;
	}
	
	public void cluster() {
		Set<DataPoint> centroids = new HashSet<DataPoint>(_k);
		Set<DataPoint> oldCentroids = new HashSet<DataPoint>(_k);
		List<DataPoint> labels = new ArrayList<DataPoint>(_dataPoints.size());
		
		//initialize random centroids
		while(centroids.size() < _k) {
			int index = (int) (Math.random() * (double)_dataPoints.size());
			centroids.add( _dataPoints.get(index) );
		}

		for(int i=0; i<_dataPoints.size(); ++i)
			labels.add(_dataPoints.get(i));
		
		
		while( !oldCentroids.equals(centroids) ) {
			
			oldCentroids = centroids;
			
			//assign each point to a centroid/cluster
			for(int i=0; i<_dataPoints.size(); ++i) {
				double minDistance = Double.MAX_VALUE;
				for(DataPoint centroid : centroids) {
					double d = distance( _dataPoints.get(i), centroid );
					if(d < minDistance) {
						minDistance = d;
						labels.set(i, centroid);
					}
				}
			}
			
			//create the new average feature-vectors for new centroids
			centroids.clear();
			for(DataPoint centroid : oldCentroids) {
				List<DataPoint> clusterPoints = new ArrayList<>();
				for(int i=0; i<labels.size(); ++i) {
					if( labels.get(i).equals(centroid) ) {
						clusterPoints.add(_dataPoints.get(i));
					}
				}

				double avgR = 0;
				double avgG = 0;
				double avgB = 0;
				for(DataPoint p : clusterPoints) {
					avgR += p._r;
					avgG += p._g;
					avgB += p._b;
				}
				avgR /= clusterPoints.size();
				avgG /= clusterPoints.size();
				avgB /= clusterPoints.size();
				
				centroids.add( new DataPoint((int)avgR, (int)avgG, (int)avgB) );
			}
		}
		
		_centroids = centroids;
	}
	
	public DataPoint clusterAffiliation(DataPoint p) {
		DataPoint closestCentroid = null;
		double minDistance = Double.MAX_VALUE;
		
		for(DataPoint c : _centroids) {
			double d = distance(p, c);
			if(d < minDistance) {
				minDistance = d;
				closestCentroid = c;
			}
		}
		
		return closestCentroid;
	}
	
	private double distance(DataPoint p1, DataPoint p2) {
		int r = p1._r - p2._r;
		int g = p1._g - p2._g;
		int b = p1._b - p2._b;
		
		return Math.sqrt( r*r + g*g + b*b );
	}
}
