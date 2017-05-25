package encryptionlayer.dag;

/**
 *  The <tt>Digraph</tt> class represents an directed graph of vertices
 *  named 0 through V-1.
 *  It supports the following operations: add an edge to the graph,
 *  iterate over all of the neighbors incident to a vertex.
 *  Parallel edges and self-loops are permitted.
 *  <p>
 *  For additional documentation,
 *  see <a href="http://algs4.cs.princeton.edu/52directed">Section 5.2</a> of
 *  <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 */

public class Digraph {
    private final int V;
    private int E;
    private Bag<Integer>[] adj;

   /**
     * Create an empty digraph with V vertices.
     */
    public Digraph(int V) {
        if (V < 0) throw new RuntimeException("Number of vertices must be nonnegative");
        this.V = V;
        this.E = 0;
        adj = (Bag<Integer>[]) new Bag[V];
        for (int v = 0; v < V; v++) {
            adj[v] = new Bag<Integer>();
        }
    }

   /**
     * Create a digraph from input stream.
     */
    public Digraph(int[] edges) {
        this(edges[0]);
        int E = edges.length;
        int i = 1;
        while (i<E) {
            int v = edges[i++];
            int w = edges[i++];
           System.out.println(v + " " + w );
            addEdge(v, w);
        }
    }

   /**
     * Copy constructor.
     */
    public Digraph(Digraph G) {
        this(G.V());
        this.E = G.E();
        for (int v = 0; v < G.V(); v++) {
            // reverse so that adjacency list is in same order as original
            Stack<Integer> reverse = new Stack<Integer>();
            for (int w : G.adj[v]) {
                reverse.push(w);
            }
            for (int w : reverse) {
                adj[v].add(w);
            }
        }
    }

   /**
     * Return the number of vertices in the digraph.
     */
    public int V() {
        return V;
    }

   /**
     * Return the number of edges in the digraph.
     */
    public int E() {
        return E;
    }

   /**
     * Add the directed edge v-w to the digraph.
     */
    public void addEdge(int v, int w) {
        adj[v].add(w);
        E++;
    }

   /**
     * Return the list of neighbors of vertex v as in Iterable.
     */
    public Iterable<Integer> adj(int v) {
        return adj[v];
    }

   /**
     * Return the reverse of the digraph.
     */
    public Digraph reverse() {
        Digraph R = new Digraph(V);
        for (int v = 0; v < V; v++) {
            for (int w : adj(v)) {
                R.addEdge(w, v);
            }
        }
        return R;
    }

   /**
     * Return a string representation of the digraph.
     */
    public String toString() {
        StringBuilder s = new StringBuilder();
        String NEWLINE = System.getProperty("line.separator");
        s.append(V + " " + E + NEWLINE);
        for (int v = 0; v < V; v++) {
            s.append(v + ": ");
            for (int w : adj[v]) {
                s.append(w + " ");
            }
            s.append(NEWLINE);
        }
        return s.toString();
    }


}
