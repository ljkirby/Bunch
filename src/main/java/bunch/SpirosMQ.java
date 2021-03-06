/****
 *
 *	$Log: SpirosMQ.java,v $
 *	Revision 3.0  2002/02/03 18:41:55  bsmitc
 *	Retag starting at 3.0
 *	
 *	Revision 1.1.1.1  2002/02/03 18:30:04  bsmitc
 *	CVS Import
 *	
 *	Revision 3.0  2000/07/26 22:46:11  bsmitc
 *	*** empty log message ***
 *	
 *	Revision 1.1.1.1  2000/07/26 22:43:34  bsmitc
 *	Imported CVS Sources
 *	
 *
 */

/**
 * Title:        Bunch Version 1.2 Base<p>
 * Description:  Your description<p>
 * Copyright:    Copyright (c) 1999<p>
 * Company:      <p>
 * @author Brian Mitchell
 * @version
 */
package bunch;

public class SpirosMQ implements ObjectiveFunctionCalculator {

private Graph graph_d;
private static int[][] clusterMatrix_d = null;
private Node[] nodes_x;
private int[] clusters_x;
private int numberOfNodes_d;

//*************
//Spiros all you need to do is to override the init and calculate methods with
//your own code.  This code is also found in TurboMQ2.java and is included for
//your reference.  When you calculate MQ you must save the final value back
//into the graph
//**************

  public SpirosMQ() {
  }


public
void
init(Graph g)
{
  graph_d = g;
  numberOfNodes_d = g.getNumberOfNodes();
  nodes_x = g.getNodes();
  clusters_x = g.getClusters();
  clusterMatrix_d = new int[numberOfNodes_d][numberOfNodes_d+1];

  if (clusterMatrix_d == null)
    clusterMatrix_d = new int[numberOfNodes_d][numberOfNodes_d+1];

  for (int i=0; i<clusterMatrix_d.length; ++i) {
    clusterMatrix_d[i][0] = 0;
  }
}

public double calculate(Cluster c)
{
  calculate();
  return graph_d.getObjectiveFunctionValue();
}

/**
 * Calculate the objective function value for the graph passed in the
 * #init(bunch.Graph) method.
 */
public
void
calculate()
{
  int k=0;
  double intra=0.0;
  double inter=0.0;
  double objTalley = 0.0;

  if (clusterMatrix_d.length != numberOfNodes_d)
    clusterMatrix_d = null;
  if (clusterMatrix_d == null)
    clusterMatrix_d = new int[numberOfNodes_d][numberOfNodes_d+1];

  for (int i=0; i<numberOfNodes_d; ++i) {
    clusterMatrix_d[i][0] = 0;
    nodes_x[i].cluster = -1;
  }

  int pos=0;
  for (int i=0; i<numberOfNodes_d; ++i) {
    int numCluster = clusters_x[i];
    clusterMatrix_d[numCluster][(++clusterMatrix_d[numCluster][0])] = i;
    nodes_x[i].cluster = numCluster;
  }

  for (int i=0; i<clusterMatrix_d.length; ++i) {
    if (clusterMatrix_d[i][0] > 0) {
      int[] clust = clusterMatrix_d[i];
      objTalley += calculateIntradependenciesValue(clust, i);
      k++;
    }
  }

  graph_d.setIntradependenciesValue(0);
  graph_d.setInterdependenciesValue(0);
  graph_d.setObjectiveFunctionValue(objTalley);
}

/**
 * Calculates the intradependencies (intraconnectivity) value for the given cluster
 * of the graph.
 */
public
double
calculateIntradependenciesValue(int[] c, int numCluster)
{
  double intradep=0.0;
  double intraEdges=0.0;
  double interEdges=0.0;
  double exitEdges=0.0;
  int k=0;
  for (int i=1; i<=c[0]; ++i) {
    Node node = nodes_x[c[i]];
    k++;
    int[] c2 = node.dependencies;
    int[] w = node.weights;

    if (c2 != null) {
      for (int j=0; j<c2.length; ++j) {
        if (nodes_x[c2[j]].cluster == numCluster) {
//if (w != null)
//System.out.println("(" + node.getName() + "," + nodes_x[c2[j]].getName() + ") = " + w[j]);
//System.out.println("Edge weight = " + w[j]);
          intradep += w[j];
          intraEdges++;
        }
        else
        {
          exitEdges += w[j];
          interEdges++;
        }
      }
    }
  }

  if ((k==0) || (k == 1))
    k=1;
  else
    k = k * (k-1);

//  System.out.println("Cluster = " + numCluster);
//  System.out.println("Num in Cluster = " + k);
//  System.out.println("IntraEdge Weight = " + intradep);
//  System.out.println("InterEdge Weight = " + exitEdges);

  double objValue = 0;

//  if (exitEdges == 0)
//   objValue = (intraEdges / k);
//  else
//   objValue = (intraEdges / k) * (1 / exitEdges);

   //---------------------------------------------
   //GOOD
   //---------------------------------------------
   if ((exitEdges+intradep) == 0)
      objValue = 0;
   else
      objValue = (intraEdges/(intraEdges+interEdges)) * (intradep / (intradep+exitEdges));

//  System.out.println("Obj Cluster Val = " + objValue);
//  System.out.println("***********************************");

  return objValue;
}


/**
 * Calculates the interdependencies (interconnectivity) between to given clusters
 */
public
double
calculateInterdependenciesValue(int[] c1, int[] c2, int nc1, int nc2)
{
  double interdep=0.0;
  for (int i=1; i<=c1[0]; ++i) {
    int[] ca = nodes_x[c1[i]].dependencies;
    int[] w = nodes_x[c1[i]].weights;

    if (ca != null) {
      for (int j=0; j<ca.length; ++j) {
       if (nodes_x[ca[j]].cluster == nc2) {
          interdep += w[j];
        }
      }
    }
  }

  for (int i=1; i<=c2[0]; ++i) {
    int[] ca = nodes_x[c2[i]].dependencies;
    int[] w = nodes_x[c2[i]].weights;

    if (ca != null) {
      for (int j=0; j<ca.length; ++j) {
        if (nodes_x[ca[j]].cluster == nc1) {
          interdep += w[j];
        }
      }
    }
  }
  interdep = ((interdep)/(2.0 * ((double)(c1[0])) * ((double)(c2[0]))));
  return interdep;
}

}