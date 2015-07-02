package trifonov.stanislav.ml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KMeansClustering {

	
	public static class Cluster {
		private final DataPoint _center;
		private final List<DataPoint> _points;
		
		public Cluster(DataPoint center) {
			_center = center;
			_points = new ArrayList<>();
		}
		
		public DataPoint getCenter() {
			return _center;
		}
		
		public void addPoint(DataPoint point) {
	        _points.add(point);
	    }
		
	    public List<DataPoint> getPoints() {
	        return _points;
	    }
	}
	
	
	private final int _k;
	private List<DataPoint> _dataPoints;
	
	public KMeansClustering(int k) {
		_k = k;
	}

	public void setData(List<DataPoint> points) {
		_dataPoints = points;
	}
	
	public List<Cluster> cluster() {
		//initialize random centroids
		Set<DataPoint> centroids = new HashSet<>(_k);
		while(centroids.size() < _k) {
			int index = (int) (Math.random() * (double)_dataPoints.size());
			centroids.add( _dataPoints.get(index) );
		}

		
		List<Cluster> clusters = new ArrayList<>(_k);
		for(DataPoint centroid : centroids)
			clusters.add( new Cluster(centroid) );

		int[] pointToClusterAssignments = new int[_dataPoints.size()];
		assignPointsToCluster(clusters, _dataPoints, pointToClusterAssignments);
		
		
		for(int iterations=0; iterations < Integer.MAX_VALUE; ++iterations) {
			boolean emptyCluster = false;
			
			List<Cluster> newClusters = new ArrayList<>();
			for(Cluster cluster : clusters) {
				DataPoint newCentroid;
				if(cluster.getPoints().isEmpty()) {
					newCentroid = getPointFromLargestNumberCluster(clusters);
					emptyCluster = true;
				}
				else
					newCentroid = centroidOf( cluster.getPoints() );
				
				newClusters.add( new Cluster(newCentroid) );
			}
			
			int newAssignments = assignPointsToCluster(newClusters, _dataPoints, pointToClusterAssignments);
			clusters = newClusters;
			
			if(newAssignments == 0 && !emptyCluster) {
				return clusters;
			}
		}
		
		return clusters;
	}
	
	private DataPoint centroidOf(Collection<DataPoint> points) {
		DataPoint centroid = new DataPoint(0, 0, 0);
		for(DataPoint p : points) {
			centroid._r += p._r;
			centroid._g += p._g;
			centroid._b += p._b;
		}
		
		centroid._r /= points.size();
		centroid._g /= points.size();
		centroid._b /= points.size();
		
		return centroid;
	}
	
	private DataPoint getPointFromLargestNumberCluster(final Collection<Cluster> clusters) {
        int maxNumber = 0;
        Cluster selected = null;
        for (Cluster cluster : clusters) {
            final int number = cluster.getPoints().size();
            if (number > maxNumber) {
                maxNumber = number;
                selected = cluster;
            }
        }

        List<DataPoint> selectedPoints = selected.getPoints();
        return selectedPoints.remove( (int) (Math.random() * (double)selectedPoints.size()) );

    }
	
	private int assignPointsToCluster(List<Cluster> clusters, Collection<DataPoint> dataPoints, int[] assignments) {
		int newAssignments = 0;
		int pointIndex=0;
		
		for(DataPoint p : dataPoints) {
			int nearestClusterIndex = getNearestCluster(p, clusters);
			Cluster cluster = clusters.get(nearestClusterIndex);
			cluster.addPoint(p);
			
			if(assignments[pointIndex] != nearestClusterIndex) {
				newAssignments++;
				assignments[pointIndex] = nearestClusterIndex;
			}
			++pointIndex;
		}

		return newAssignments;
	}
	
	private static int getNearestCluster(DataPoint p, List<Cluster> clusters) {
		double minDistance = Double.MAX_VALUE;
		int nearestClusterIndex = 0;
		
		for(int i=0; i<clusters.size(); ++i) {
			double d = distance(p, clusters.get(i).getCenter());
			if(d < minDistance) {
				minDistance = d;
				nearestClusterIndex = i;
			}
		}
	
		return nearestClusterIndex;
	}
	
	public static DataPoint clusterAffiliation(DataPoint p, List<Cluster> clusters) {
		return clusters.get( getNearestCluster(p, clusters) ).getCenter();
	}
	
	private static double distance(DataPoint p1, DataPoint p2) {
		int r = p1._r - p2._r;
		int g = p1._g - p2._g;
		int b = p1._b - p2._b;
		
		return Math.sqrt( r*r + g*g + b*b );
	}
}
