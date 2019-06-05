package it.polito.tdp.ufo.model;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.DepthFirstIterator;

import it.polito.tdp.ufo.db.SightingsDAO;

public class Model {

	private SightingsDAO dao;
	private List<String>stati;
		
	private Graph<String, DefaultEdge>grafo;
	
	//PER LA RICORSIONE PUNTO 2)
	
	//1) struttura dati per il risultato finale: conterrà lo stato iniziale e una lista di altri stati purchè non siano ripetuti.
	private List<String> ottima;//è una lista di stati (String) in cui c'è lo stato di partanza e un insieme di altri stati non ripetuti.
	
	//2) struttura dati parziale che è una lista definita nel mwtodo ricorsivo
	
	//3)condizione di terminazione-> quando sono arrivato ad un determinato nodo e non ci sono più successori che io non abbia già considerato.
	
	//4) generare una nuova soluzione a partire dalla soluzione parziale
	//dato l'ultimo nodo inserito in parziale, 
	//considero tutti i successori di quel nodo che non ho ancora considerato.
	
	//5) filtro
	//alla fine ritornerò una sola soluzione
	//quella per cui la size è massima!
	
	//6) livello di ricorsione-> lunghezza del percorso parziale
	//7) il caso iniziale-> parziale contiene il mio stato di partenza
	
	
	public Model() {
		this.dao=new SightingsDAO();
	}
	
	public List<AnnoCount>getAnni(){
		return dao.getAnni();
	}
	
	public void creaGrafo(Year anno) {
		this.grafo= new SimpleDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		this.stati=this.dao.getStati(anno);
		
		Graphs.addAllVertices(this.grafo, stati);
		
		//soluzione "semplice"-> doppio ciclo, contorllo esistenza arco
		for( String s1: this.grafo.vertexSet()) {
			for(String s2: this.grafo.vertexSet()) {
				if(!s1.equals(s2)) {
					if(this.dao.esisteArco(s1,s2, anno)) {
						this.grafo.addEdge(s1, s2);//l'ordine conta perchè il grafo è ordintato.
					}
				}
			}
		}
		System.out.println("Grafo creato!");
		System.out.println("# vertici: "+this.grafo.vertexSet().size());
		System.out.println("# archi: "+this.grafo.edgeSet().size());

	}

	public int getNvertici() {
		return this.grafo.vertexSet().size();
	}

	public int getNarchi() {
		return this.grafo.edgeSet().size();
	}

	public List<String> getStati() {
		return this.stati;
	}
	
	public List<String>getSuccessori(String stato){
		return Graphs.successorListOf(this.grafo, stato);
	}
	
	public List<String>getPredecessori(String stato){
		return Graphs.predecessorListOf(this.grafo, stato);
	}
	
	public List<String>getRaggiungibili(String stato){
		List<String>raggiungibili= new ArrayList<String>();
		DepthFirstIterator<String, DefaultEdge> dp= 
				new DepthFirstIterator<String, DefaultEdge>(grafo, stato);
		
		//dobbiamo scorrere l'iteratore
		//nella lista ci sono tutti gli stati ragigungibili, e non voglio che nella lista ci sia lo stato di partenza
		dp.next();//questo next a vuoto scarta il primo elemento, quindi me stesso.
		while(dp.hasNext()) {
			raggiungibili.add(dp.next());			
		}

		return raggiungibili;	
	}
	
	//PUNTO 2 RICORSIONE
	public List<String> getPercorsoMassimo(String partenza){
		this.ottima= new ArrayList<String>();//la creo nel metodo così ogni volta che richaimo il metodo la ricreo buttando i dati precedenti e riempendola con i nuovi appena trovati 
		
		//per la soluzione parziale
		List<String>parziale= new ArrayList<String>();
		//7)
		parziale.add(partenza);
		
		cercaPercorso(parziale);
	
		return this.ottima;
	}

	private void cercaPercorso(List<String> parziale) {
		
		
		//ottengo tutti i candidati
		List<String>candidati= this.getSuccessori(parziale.get(parziale.size()-1)); //prendo l'ultimo elemento, ci va il -1 poichè le listae parono sempre da 0.
		for( String candidato: candidati) {
			if(!parziale.contains(candidato)) {
				//è un candidato che non ho ancora considerato
				parziale.add(candidato);
				
				//rilancio il metodo con la nuova soluzione
				this.cercaPercorso(parziale);
				
				//siccome voglio provare tutte le soluzioni possibili, allora tolgo la città appena inserita, che si troverà sempre nell'ultima posizione.
				parziale.remove(parziale.size()-1);
				
			}
		}
		//verifico se la soluzione corrente è migliore della ottima corrente
				if(parziale.size()>ottima.size()) {
					this.ottima=new ArrayList<String>(parziale);
				}
		
	}
	

	
	
	
	
	
	
}
