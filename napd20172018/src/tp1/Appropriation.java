package tp1;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.io.fs.FileUtils;

import tp1.EmpDepDB_1.RelTypes;

public class Appropriation {
	
	public enum TypesRel implements RelationshipType
    {
        APPARTIENT_A_DEPARTEMENT,
        APPARTIENT_A_REGION
    }
	
	public enum Labels implements Label {
	    Commune,
	    Departement,
	    Region
	}
	
	private static final String COMMUNE_DB = "target/commune-db";
    private static GraphDatabaseService graphDb;


	public Appropriation() {
		try {
			FileUtils.deleteRecursively( new File( COMMUNE_DB ) );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( new File(COMMUNE_DB) );
        
     // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running example before it's completed)
        Runtime.getRuntime()
                .addShutdownHook( new Thread()
                {
                    @Override
                    public void run()
                    {
                        graphDb.shutdown();
                    }
                } );
        
        //Création des noeuds et des arcs
        try ( Transaction tx = graphDb.beginTx() )
        {
        	//Création des noeuds
        	
        	Node montpellier = graphDb.createNode();
        	montpellier.addLabel(Labels.Commune);
        	montpellier.setProperty("nom", "Montpellier");
        	montpellier.setProperty("population1975", 100000);
        	montpellier.setProperty("population2010", 1000000);
        	montpellier.setProperty("chefLieu", "La comédie");
        	
        	
        	Node castelnau = graphDb.createNode();
        	castelnau.addLabel(Labels.Commune);
        	castelnau.setProperty("nom", "Castelnau-le-lez");
        	castelnau.setProperty("population1975", 1000);
        	castelnau.setProperty("population2010", 10000);
        	castelnau.setProperty("chefLieu", "La mairie");
        	
        	
        	Node herault = graphDb.createNode();
        	herault.setProperty("nom","Herault");
        	herault.addLabel(Labels.Departement);
        	
        	Node languedoc = graphDb.createNode();
        	languedoc.setProperty("nom","Languedoc-roussillon");
        	languedoc.addLabel(Labels.Region);
        	
        	Node nimes = graphDb.createNode();
        	nimes.addLabel(Labels.Commune);
        	nimes.setProperty("nom","Nîmes");
        	nimes.setProperty("population1975", 50000);
        	nimes.setProperty("population2010", 500000);
        	nimes.setProperty("chefLieu", "Les arênes");
        	
        	Node bouillargues = graphDb.createNode();
        	bouillargues.addLabel(Labels.Commune);
        	bouillargues.setProperty("nom", "Bouillargues");
        	bouillargues.setProperty("population1975", 2000);
        	bouillargues.setProperty("population2010", 20000);
        	bouillargues.setProperty("chefLieu", "La bergerie");
        	
        	Node gard = graphDb.createNode();
        	gard.addLabel(Labels.Departement);
        	gard.setProperty("nom", "Gard");
        	
            // Création des arcs
        	
        	montpellier.createRelationshipTo(herault, TypesRel.APPARTIENT_A_DEPARTEMENT);
        	castelnau.createRelationshipTo(herault, TypesRel.APPARTIENT_A_DEPARTEMENT);
        	
        	herault.createRelationshipTo(languedoc, TypesRel.APPARTIENT_A_REGION);
        	
        	nimes.createRelationshipTo(gard, TypesRel.APPARTIENT_A_DEPARTEMENT);
        	bouillargues.createRelationshipTo(gard, TypesRel.APPARTIENT_A_DEPARTEMENT);
        	
        	gard.createRelationshipTo(languedoc, TypesRel.APPARTIENT_A_REGION);

            
            tx.success();
            
            System.out.println("Creation communes done.");
        }
        
	}
	
	public void afficherListeCommuneAvecNavigation() {
		try {
			Transaction tx = graphDb.beginTx();
			ResourceIterator<Node> communes = graphDb.findNodes(Labels.Commune);
			System.out.println( "Communes :" );
			while( communes.hasNext() )
			{
				Node commune = communes.next();
				System.out.println( "\t" + commune.getProperty( "nom" ) );
			}
			tx.success();
		}
		finally {
			System.out.println("Done");
		}
	}
	
	public void afficherListeCommuneAvecCypher() {
		try { 
			Transaction tx = graphDb.beginTx() ;
    		Result result = graphDb.execute( "match (n:Commune) return n.nom" );  
    		System.out.println( "Communes :" );
    		while ( result.hasNext() )
    		{
    			 Map<String, Object> map = result.next();
    			 for (Map.Entry<String, Object> entry : map.entrySet()) {
    				 System.out.println("\t" + entry.getValue());
    			 } 
    		}
    		tx.success();
		}
		finally {
			System.out.println("Done");
		}
	}
	
	public void afficherEnsembleCommunesPourDepartementDonne(String departement) {
		try {
			Transaction tx = graphDb.beginTx();
			String query = "match (n:Commune)-[:APPARTIENT_A_DEPARTEMENT]"+
			"->(:Departement {nom:\""+departement+"\"}) RETURN n.nom";
			Result result = graphDb.execute(query);
			System.out.println( "Communes appartenant à "+departement+" :" );
			while ( result.hasNext() )
    		{
    			 Map<String, Object> map = result.next();
    			 for (Map.Entry<String, Object> entry : map.entrySet()) {
    				 System.out.println("\t" + entry.getValue());
    			 } 
    		}
    		tx.success();
		}
		finally {
			System.out.println("Done");
		}
	}
	
	public void afficherChefLieuChaqueCommune() {
		try {
			Transaction tx = graphDb.beginTx();
			String query = "match (n:Commune) RETURN n.nom, n.chefLieu";
			Result result = graphDb.execute(query);
			while(result.hasNext()) {
				Map<String,Object> map = result.next();
				System.out.println("Commune : "+map.get("n.nom")+", Chef lieu : "+
				map.get("n.chefLieu"));
			}
			tx.success();
		}
		finally {
			System.out.println("Done");
		}
	}
	
	public void afficherPopulation1975ChaqueCommune() {
		try {
			Transaction tx = graphDb.beginTx();
			String query = "match (n:Commune) RETURN n.nom, n.population1975";
			Result result = graphDb.execute(query);
			while(result.hasNext()) {
				Map<String,Object> map = result.next();
				System.out.println("Commune : "+map.get("n.nom")+", population1975 : "+
				map.get("n.population1975").toString());
			}
			tx.success();
		}
		finally {
			System.out.println("Done");
		}
	}
	
	public void afficherInfoCommunePlusPeuplee2010() {
		try {
			Transaction tx = graphDb.beginTx();
			String query = "match (n:Commune) RETURN n.population2010 AS pop,"+
			"n ORDER BY pop DESC LIMIT 1";
			Result result = graphDb.execute(query);
			while(result.hasNext()) {
				Map<String,Object> map = result.next();
				Node n = (Node) map.get("n");
				Map<String,Object> m = n.getAllProperties();
				for (Map.Entry<String, Object> entry : m.entrySet()) {
	 				System.out.println(entry.getKey() + " : " + entry.getValue());
	 			}
			}
			tx.success();
		}
		finally {
			System.out.println("Done");
		}
	}
	
	public static void main(String[] args) {
		Appropriation app = new Appropriation();
		/*
		System.out.println("@--Affichage liste communes avec navigation--@");
		app.afficherListeCommuneAvecNavigation();
		
		System.out.println("@--Affichage liste communes avec cypher--@");
		app.afficherListeCommuneAvecCypher();
		
		System.out.println("@--Affichage liste communes pour un département donné--@");
		app.afficherEnsembleCommunesPourDepartementDonne("Herault");
		app.afficherEnsembleCommunesPourDepartementDonne("Gard");
		
		
		System.out.println("@--Affichage chef lieu pour chaque commune--@");
		app.afficherChefLieuChaqueCommune();
		
		System.out.println("@--Affichage population 1975 chaque commune--@");
		app.afficherPopulation1975ChaqueCommune();
		
		System.out.println("@--Affichage info commune plus peuplée 2010--@");
		app.afficherInfoCommunePlusPeuplee2010();
		*/
		
	}

}
