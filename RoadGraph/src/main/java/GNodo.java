import java.util.ArrayList;

public class GNodo implements Comparable<GNodo>{
	long id;
	double lat, lon;
	boolean ativo = false;

	ArrayList<Aresta> listaArestas = new ArrayList<>();

	double g = Double.MAX_VALUE; // Custo real do nodo
	double h = 0;                // Heurística (estimativa) do nodo até o destino
	double f = Double.MAX_VALUE;  // Custo total (f = g + h)
	GNodo pai = null;             // Para traçar o caminho de volta ao final

	public GNodo() {}

	// Função para calcular a distância (heurística) entre dois nodos
	public double distancia(GNodo outroNodo) {
		double dlat = this.lat - outroNodo.lat;
		double dlon = this.lon - outroNodo.lon;
		return Math.sqrt(dlat * dlat + dlon * dlon);
	}

	// Metodo para obter a lista de arestas conectadas a este nó
	public ArrayList<Aresta> getArestas() {
		return listaArestas;
	}

	@Override
	public int compareTo(GNodo outroNodo) {
		// Compara com base no custo total (f)
		return Double.compare(this.f, outroNodo.f);
	}
}


