public class Aresta {
	GNodo A;
	GNodo B;
	float size;  // Esse já é o "custo" da aresta

	public Aresta(GNodo A, GNodo B) {
		this.A = A;
		this.B = B;

		double dlat = A.lat - B.lat;
		double dlon = A.lon - B.lon;

		double dsize = Math.sqrt(dlat * dlat + dlon * dlon);
		size = (float) dsize * 1000000;  // Custo da aresta baseado na distância entre os nodos
	}

	public GNodo obterOutroNodo(GNodo nodoAtual) {
		if (nodoAtual.equals(A)) {
			return B;
		} else if (nodoAtual.equals(B)) {
			return A;
		} else {
			return null;  // Caso o nodo fornecido não esteja presente na aresta
		}
	}

	public float getCusto() {
		return size;  // Retorna o tamanho da aresta, que representa o custo
	}
}
