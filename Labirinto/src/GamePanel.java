import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.awt.image.*;
import javax.imageio.ImageIO;


public class GamePanel extends Canvas implements Runnable
{
private static final int PWIDTH = 960;
private static final int PHEIGHT = 800;
private Thread animator;
private boolean running = false;
private boolean gameOver = false; 


int FPS,SFPS;
int fpscount;

public static Random rnd = new Random();

//BufferedImage imagemcharsets;

boolean LEFT, RIGHT,UP,DOWN;

public static int mousex,mousey; 

public static ArrayList<Agente> listadeagentes = new ArrayList<Agente>();

Mapa_Grid mapa;

double posx,posy;

MeuAgente meuHeroi = null;

//TODO ESSE È O RESULTADO
int caminho[] = null;

float zoom = 1;

int ntileW = 60;
int ntileH = 50;

HashSet<Integer> nodosVisitados = new HashSet<>(); 

public GamePanel()
{

	setBackground(Color.white);
	setPreferredSize( new Dimension(PWIDTH, PHEIGHT));

	// create game components
	setFocusable(true);

	requestFocus(); // JPanel now receives key events	
	
	
	// Adiciona um Key Listner
	addKeyListener( new KeyAdapter() {
		public void keyPressed(KeyEvent e)
			{ 
				int keyCode = e.getKeyCode();
				
				if(keyCode == KeyEvent.VK_LEFT){
					LEFT = true;
				}
				if(keyCode == KeyEvent.VK_RIGHT){
					RIGHT = true;
				}
				if(keyCode == KeyEvent.VK_UP){
					UP = true;
				}
				if(keyCode == KeyEvent.VK_DOWN){
					DOWN = true;
				}	
			}
		@Override
			public void keyReleased(KeyEvent e ) {
				int keyCode = e.getKeyCode();
				
				if(keyCode == KeyEvent.VK_LEFT){
					LEFT = false;
				}
				if(keyCode == KeyEvent.VK_RIGHT){
					RIGHT = false;
				}
				if(keyCode == KeyEvent.VK_UP){
					UP = false;
				}
				if(keyCode == KeyEvent.VK_DOWN){
					DOWN = false;
				}
			}
	});
	
	addMouseMotionListener(new MouseMotionListener() {
		
		@Override
		public void mouseMoved(MouseEvent e) {
			// TODO Auto-generated method stub
			mousex = e.getX(); 
			mousey = e.getY();
			

		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			// TODO Auto-generated method stub
			if(e.getButton()==3){
				int mousex = (int)((e.getX()+mapa.MapX)/zoom);
				int mousey = (int)((e.getY()+mapa.MapY)/zoom);
				
				int mx = mousex/16;
				int my = mousey/16;
				
				if(mx>mapa.Altura) {
					return;
				}
				if(my>mapa.Largura) {
					return;
				}
				
				mapa.mapa[my][mx] = 1;
			}
		}
	});
	
	addMouseListener(new MouseListener() {
		
		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub
			//System.out.println(" "+arg0.getButton());
			int mousex = (int)((arg0.getX()+mapa.MapX)/zoom);
			int mousey = (int)((arg0.getY()+mapa.MapY)/zoom);

			//System.out.println(""+arg0.getX()+" "+mapa.MapX+" "+zoom);
			//System.out.println(""+mousex+" "+mousey);

			int mx = mousex/16;
			int my = mousey/16;

			if(mx>mapa.Altura) {
				return;
			}
			if(my>mapa.Largura) {
				return;
			}

			if(arg0.getButton()==3){


				if(mapa.mapa[my][mx]==0){
					mapa.mapa[my][mx] = 1;
				}else{
					mapa.mapa[my][mx] = 0;
				}
			}
			if(arg0.getButton()==1){
				if(mapa.mapa[my][mx]==0) {
					caminho = null;
					long timeini = System.currentTimeMillis();

					System.out.println(""+my+" "+mx);

					buscaAEstrela(mx,my);
					//buscaEmLArgura(mx,my);



					//mapa.mapa[my][mx]

					long timefin = System.currentTimeMillis() - timeini;
					System.out.println("Tempo Final: "+timefin);
				}else {
					System.out.println("Caminho Final Bloqueado");
				}
			}
		}


		public void buscaAEstrela(int mx, int my) {
			int bhx = (int)(meuHeroi.X / 16);
			int bhy = (int)(meuHeroi.Y / 16);

			Node inicial = new Node(bhx, bhy, null);
			inicial.g = 0;
			inicial.h = calcularHeuristica(bhx, bhy, mx, my);
			inicial.f = inicial.g + inicial.h;

			PriorityQueue<Node> listaDeNodos = new PriorityQueue<>(Comparator.comparingInt(n -> (int) n.f));
			//HashSet<Integer> nodosVisitados = new HashSet<>();

			synchronized (nodosVisitados) {
				nodosVisitados.clear();
			}

			listaDeNodos.add(inicial);
			synchronized (nodosVisitados) {
				nodosVisitados.add(inicial.x + inicial.y * mapa.Largura);
			}
			boolean achou = false;
			Node atual = null;
			int nodosExplorados = 0; // Contador de nós explorados

			while (!listaDeNodos.isEmpty()) {
				atual = listaDeNodos.poll();
				nodosExplorados++; // Incrementa o contador ao explorar um nó

				// Verifica se atingiu o nó destino
				if (atual.x == mx && atual.y == my) {
					achou = true;
					break;
				}

				// Expande os nós vizinhos
				Node[] filhos = new Node[4];
				filhos[0] = new Node(atual.x + 1, atual.y, atual);
				filhos[1] = new Node(atual.x - 1, atual.y, atual);
				filhos[2] = new Node(atual.x, atual.y + 1, atual);
				filhos[3] = new Node(atual.x, atual.y - 1, atual);

				for (Node t : filhos) {
					if (t.x >= 0 && t.y >= 0 && t.x < mapa.Largura && t.y < mapa.Altura && mapa.mapa[t.y][t.x] == 0) {
						if (!nodosVisitados.contains(t.x + t.y * mapa.Largura)) {
							t.g = atual.g + 1;  // custo real é 1 entre nós vizinhos
							t.h = calcularHeuristica(t.x, t.y, mx, my);
							t.f = t.g + t.h;

							listaDeNodos.add(t);
							synchronized (nodosVisitados) {
								nodosVisitados.add(t.x + t.y * mapa.Largura);
							}
						}
					}
				}
			}

			if (achou) {
				System.out.println("Caminho encontrado com A*");

				LinkedList<Node> pathlist = new LinkedList<>();
				pathlist.add(atual);
				while (atual.pai != null) {
					atual = atual.pai;
					pathlist.addFirst(atual);
				}

				caminho = new int[pathlist.size() * 2];
				int pos = 0;
				for (Node node : pathlist) {
					caminho[pos] = node.x;
					caminho[pos + 1] = node.y;
					pos += 2;
				}

				meuHeroi.X = mx * 16 + 10;
				meuHeroi.Y = my * 16 + 10;
				System.out.println("Nodos explorados: " + nodosExplorados);
			} else {
				System.out.println("Caminho não encontrado");
				System.out.println("Nodos explorados: " + nodosExplorados);
			}
		}

		private int calcularHeuristica(int x1, int y1, int x2, int y2) {
			// Distância Manhattan
			return Math.abs(x1 - x2) + Math.abs(y1 - y2);
		}

		public void buscaEmLArgura(int mx,int my) {
			int bhx = (int)(meuHeroi.X/16);
			int bhy = (int)(meuHeroi.Y/16);

			Node inicial = new Node(bhx, bhy, null);

			LinkedList<Node> listaDeNodos = new LinkedList<>();
			//LinkedList<Node> listaDeNodosVisitados = new LinkedList<>();
			synchronized (nodosVisitados) {
				nodosVisitados.clear();
			}


			listaDeNodos.add(inicial);
			synchronized (nodosVisitados) {
				nodosVisitados.add(inicial.x+inicial.y*mapa.Largura);
			}

			boolean achow = false;

			do {
				Node atual = listaDeNodos.remove();
				Node filhos[] = new Node[4];
				filhos[0] = new Node(atual.x+1, atual.y, atual);
				filhos[1] = new Node(atual.x-1, atual.y, atual);
				filhos[2] = new Node(atual.x, atual.y+1, atual);
				filhos[3] = new Node(atual.x, atual.y-1, atual);

				for (int i = 0; i < filhos.length; i++) {
					Node t = filhos[i];
					if(t.x>=0&&t.y>=0&&t.x<mapa.Largura&&t.y<mapa.Altura&&mapa.mapa[t.y][t.x]==0) {
						boolean jaVisitado = false;
						if(nodosVisitados.contains(t.x+t.y*mapa.Largura)) {
							jaVisitado = true;
						}

						if(jaVisitado==false) {
							listaDeNodos.add(t);
							synchronized (nodosVisitados) {
								nodosVisitados.add(t.x+t.y*mapa.Largura);
							}
							if(t.x==mx&&t.y==my) {
								achow = true;
								break;
							}
						}
					}
				}

			}while(achow==false);
			System.out.println("achou "+listaDeNodos.size()+" ja visitados "+nodosVisitados.size());

			Node nfinal = listaDeNodos.removeLast();
			LinkedList<Node> pathlist = new LinkedList<>();
			pathlist.add(nfinal);
			Node atual = nfinal;
			while(atual.pai!=null) {
				pathlist.addFirst(atual.pai);
				atual = atual.pai;
			}

			caminho = new int[pathlist.size()*2];
			int pos = 0;
			for (Iterator iterator = pathlist.iterator(); iterator.hasNext();) {
				Node node = (Node) iterator.next();
				caminho[pos] = node.x;
				caminho[pos+1] = node.y;
				pos+=2;
			}

			meuHeroi.X = nfinal.x*16+10;
			meuHeroi.Y = nfinal.y*16+10;
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void mouseClicked(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}
	});
	
	addMouseWheelListener(new MouseWheelListener() {
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			//System.out.println("w "+e.getWheelRotation());
			if(e.getWheelRotation()>0) {
				zoom= zoom*1.1f;
			}else if(e.getWheelRotation()<0) {
				zoom= zoom*0.90f;
			}
			
			ntileW = (int)((960/zoom)/16)+1;
			ntileH = (int)((800/zoom)/16)+1;
			
			if(ntileW>=1000) {
				ntileW = 1000;
			}
			if(ntileH>=1000) {
				ntileH = 1000;
			}
			mapa.NumeroTilesX = ntileW;
			mapa.NumeroTilesY = ntileH;
		}
	});


	meuHeroi = new MeuAgente(10, 10, Color.blue);
	
	listadeagentes.add(meuHeroi);
	
	mousex = mousey = 0;
	
	mapa = new Mapa_Grid(100,100,ntileW, ntileH);
	mapa.loadmapfromimage("/imagemlabirinto1000.png");
	
} // end of GamePanel()


public void startGame()
// initialise and start the thread
{
	if (animator == null || !running) {
		animator = new Thread(this);
		animator.start();
	}
} // end of startGame()

public void stopGame()
// called by the user to stop execution
{ running = false; }


public void run()
/* Repeatedly update, render, sleep */
{
	running = true;
	
	long DifTime,TempoAnterior;
	
	int segundo = 0;
	DifTime = 0;
	TempoAnterior = System.currentTimeMillis();
	
	this.createBufferStrategy(2);
	BufferStrategy strategy = this.getBufferStrategy();
	
	while(running) {
	
		gameUpdate(DifTime); // game state is updated
		Graphics g = strategy.getDrawGraphics();
		gameRender((Graphics2D)g); // render to a buffer
		strategy.show();
	
		try {
			Thread.sleep(0); // sleep a bit
		}	
		catch(InterruptedException ex){}
		
		DifTime = System.currentTimeMillis() - TempoAnterior;
		TempoAnterior = System.currentTimeMillis();
		
		if(segundo!=((int)(TempoAnterior/1000))){
			FPS = SFPS;
			SFPS = 1;
			segundo = ((int)(TempoAnterior/1000));
		}else{
			SFPS++;
		}
	
	}
System.exit(0); // so enclosing JFrame/JApplet exits
} // end of run()

int timerfps = 0;
private void gameUpdate(long DiffTime)
{ 
	
	if(LEFT){
		posx-=1000*DiffTime/1000.0;
	}
	if(RIGHT){
		posx+=1000*DiffTime/1000.0;
	}	
	if(UP){
		posy-=1000*DiffTime/1000.0;
	}
	if(DOWN){
		posy+=1000*DiffTime/1000.0;
	}
	
	if(posx>mapa.Largura*16) {
		posx=mapa.Largura*16;
	}
	if(posy>mapa.Altura*16) {
		posy=mapa.Altura*16;
	}
	if(posx<0) {
		posx=0;
	}
	if(posy<0) {
		posy=0;
	}
	
	mapa.Posiciona((int)posx,(int)posy);
	
	for(int i = 0;i < listadeagentes.size();i++){
		  listadeagentes.get(i).SimulaSe((int)DiffTime);
	}
}

Color corPath = new Color(70,70,255);
private void gameRender(Graphics2D dbg)
// draw the current frame to an image buffer
{
	// clear the background
	dbg.setColor(Color.white);
	dbg.fillRect (0, 0, PWIDTH, PHEIGHT);

	AffineTransform trans = dbg.getTransform();
	dbg.scale(zoom, zoom);
	
	try {
		mapa.DesenhaSe(dbg);
	}catch (Exception e) {
		System.out.println("Erro ao desenhar mapa");
	}
	synchronized (nodosVisitados) {
		if(nodosVisitados.size()>0) {
		
			for (Iterator iterator = nodosVisitados.iterator(); iterator.hasNext();) {
				Integer nodoint = (Integer) iterator.next();
				
				int nx = nodoint%mapa.Largura;
				int ny = nodoint/mapa.Largura;
				
				dbg.setColor(Color.red);
				dbg.fillRect(nx*16-mapa.MapX, ny*16-mapa.MapY, 16, 16);
			}
		}
	}
	
	if(caminho!=null){
		
		try {
			if(caminho!=null){
				for(int i = 0; i < caminho.length/2;i++){
					int nx = caminho[i*2];
					int ny = caminho[i*2+1];
					
					dbg.setColor(corPath);
					dbg.fillRect(nx*16-mapa.MapX, ny*16-mapa.MapY, 16, 16);
				}
			}
		}catch (Exception e) {
		}
	}
	

	
	for(int i = 0;i < listadeagentes.size();i++){
		  listadeagentes.get(i).DesenhaSe(dbg, mapa.MapX, mapa.MapY);
		}
	
	dbg.setTransform(trans);
	
	dbg.setColor(Color.BLUE);	
	dbg.drawString("FPS: "+FPS, 10, 10);	
	
	//System.out.println("left "+LEFT);
		
}

}

