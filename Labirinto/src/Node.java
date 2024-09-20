public class Node {
	int x; // posição x no grid
	int y; // posição y no grid
	Node pai; // nó pai para reconstruir o caminho
	float g; // custo do início até este nó
	float h; // heurística (custo estimado até o objetivo)
	float f; // custo total (g + h)

	public Node(int x, int y, Node pai) {
		this.x = x;
		this.y = y;
		this.pai = pai;
		this.g = 0; // Inicializa o custo g
		this.h = 0; // Inicializa a heurística h
		this.f = 0; // Inicializa o custo total f
	}

	// Método para calcular a heurística (distância Manhattan, por exemplo)
	public void calcularHeuristica(int targetX, int targetY) {
		this.h = Math.abs(targetX - this.x) + Math.abs(targetY - this.y);
	}

	// Método para calcular o custo total f
	public void calcularCustoTotal() {
		this.f = this.g + this.h;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Node)) return false;
		Node other = (Node) obj;
		return this.x == other.x && this.y == other.y;
	}

	@Override
	public int hashCode() {
		return Integer.hashCode(x) * 31 + Integer.hashCode(y);
	}
}

