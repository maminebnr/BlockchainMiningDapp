package ca.pfv.spmf.patterns.cluster;

import java.util.List;

import ca.pfv.spmf.algorithms.clustering.dbscan.AlgoDBSCAN;
import ca.pfv.spmf.algorithms.clustering.distanceFunctions.DistanceFunction;
import ca.pfv.spmf.algorithms.clustering.kmeans.AlgoBisectingKMeans;
import ca.pfv.spmf.algorithms.clustering.kmeans.AlgoKMeans;

/* This file is copyright (c) 2008-2012 Philippe Fournier-Viger
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/
/**
 * This class provides methods for evaluating a set of clusters such as the Sum
 * of Squared Error (SSE). These methods are used to evaluate clusters generated
 * by clustering algorithms such as KMeans, etc.
 * 
 * @see AlgoKMeans
 * @see AlgoDBSCAN
 * @see AlgoBisectingKMeans
 * @author Philippe Fournier-Viger
 */
public class ClustersEvaluation {

	/**
	 * Calculate the sum of squared error metrics for evaluating a set of clusters.
	 * This method is optimized for clusters where the mean has been already
	 * calculated.
	 * 
	 * @param clusters         the list of clusters
	 * @param distanceFunction a distance function
	 * @return the sum of square errors (in general, lower is better)
	 */
	public static double calculateSSE(List<ClusterWithMean> clusters, DistanceFunction distanceFunction) {
		double sse = 0;
		// for each cluster
		for (ClusterWithMean cluster : clusters) {
			// for each instance in that cluster
			for (DoubleArray vector : cluster.getVectors()) {
				sse += Math.pow(distanceFunction.calculateDistance(vector, cluster.getmean()), 2);
			}
		}
		return sse;
	}

	/**
	 * Calculate the sum of squared error metrics for evaluating a set of clusters.
	 * This method is for clusters where the mean has not been calculated
	 * previously.
	 * 
	 * @param clusters         the list of clusters
	 * @param distanceFunction a distance function
	 * @return the sum of square errors (in general, lower is better)
	 */
	public static double getSSE(List<Cluster> clusters, DistanceFunction distanceFunction) {
		double sse = 0;
		// for each cluster
		for (Cluster cluster : clusters) {
			// if the cluster is not empty
			if (cluster.getVectors().size() > 0) {
				// calculate the mean of the cluster
				DoubleArray mean = calculateClusterMeans(cluster);
				// for each instance in that cluster
				for (DoubleArray vector : cluster.getVectors()) {
					sse += Math.pow(distanceFunction.calculateDistance(vector, mean), 2);
				}
			}
		}
		return sse;
	}

	/**
	 * This method calculated the mean of a cluster
	 * 
	 * @param cluster a non-empty cluster
	 * @return a vector representing the mean
	 */
	public static DoubleArray calculateClusterMeans(Cluster cluster) {
		int dimensionCount = cluster.getVectors().get(0).data.length;
		double mean[] = new double[dimensionCount];
		// for each vector
		for (DoubleArray vector : cluster.getVectors()) {
			// for each dimension, we add the value
			for (int i = 0; i < dimensionCount; i++) {
				mean[i] += vector.data[i];
			}
		}
		// finally, fo each dimension, we divide by the number of vectors
		for (int i = 0; i < dimensionCount; i++) {
			mean[i] = mean[i] / cluster.getVectors().size();
		}
		return new DoubleArray(mean);
	}

}