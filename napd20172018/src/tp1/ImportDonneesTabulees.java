package tp1;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.io.fs.FileUtils;
import org.neo4j.graphdb.Result;

public class ImportDonneesTabulees {
	
	private static final String COMMUNE_DB = "target/commune2-db";
    private static GraphDatabaseService graphDb;
	private static String cheminFichier="/home/ymerej/repositories/napd20172018/TP1/";
    
    public ImportDonneesTabulees() {
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
        
        //Import des noeuds et des arcs
        
        try ( Transaction tx = graphDb.beginTx() )
        {
        	String queryCommune = "LOAD CSV WITH HEADERS FROM 'file://"+cheminFichier+"Chargeur/communes.csv' AS communes " + 
        			"CREATE (c:Commune {id : toInt(communes.id), nom : communes.nom});";
        	graphDb.execute(queryCommune);
        	
        	String queryDepartement = "LOAD CSV WITH HEADERS FROM 'file://"+cheminFichier+"Chargeur/departements.csv' AS departements " + 
        			"CREATE (d:Departement {id : toInt(departements.id), nom : departements.nom});";
        	graphDb.execute(queryDepartement);
        	
        	String queryRegion = "LOAD CSV WITH HEADERS FROM 'file://"+cheminFichier+"Chargeur/regions.csv' AS regions " + 
        			"CREATE (r:Region {id : toInt(regions.id), nom : regions.nom});";
        	graphDb.execute(queryRegion);
        	
        	String queryAprDepartement = "LOAD CSV WITH HEADERS FROM 'file://"+cheminFichier+"Chargeur/apr_departement.csv' AS apr_departement " +
        			"MERGE (com:Commune {id:toInt(apr_departement.id_c)}) "+
        			"MERGE (dep:Departement {id:toInt(apr_departement.id_d)}) "+
        			"CREATE (com)-[:APR_DEPARTEMENT]->(dep)";
        	graphDb.execute(queryAprDepartement);
        	
        	String queryAprRegion = "LOAD CSV WITH HEADERS FROM 'file://"+cheminFichier+"Chargeur/apr_region.csv' AS apr_region " +
        			"MERGE (dep:Departement {id:toInt(apr_region.id_d)}) "+
        			"MERGE (reg:Region {id:toInt(apr_region.id_r)}) "+
        			"CREATE (reg)-[:APR_REGION]->(reg)";
        	graphDb.execute(queryAprRegion);
        	
        	String queryChefLieuDepartement = "LOAD CSV WITH HEADERS FROM 'file://"+cheminFichier+"Chargeur/chef_lieu_departement.csv' AS chef_lieu_departement " +
        			"MERGE (dep:Departement {id:toInt(chef_lieu_departement.id_d)}) "+
        			"MERGE (com:Commune {id:toInt(chef_lieu_departement.id_c)}) "+
        			"CREATE (dep)-[:CHEF_LIEU_DEPARTEMENT]->(com)";
        	graphDb.execute(queryChefLieuDepartement);
        	
        	String queryChefLieuRegion = "LOAD CSV WITH HEADERS FROM 'file://"+cheminFichier+"Chargeur/chef_lieu_region.csv' AS chef_lieu_region " +
        			"MERGE (reg:Region {id:toInt(chef_lieu_region.id_r)}) "+
        			"MERGE (com:Commune {id:toInt(chef_lieu_region.id_c)}) "+
        			"CREATE (reg)-[:CHEF_LIEU_REGION]->(com)";
        	graphDb.execute(queryChefLieuRegion);
        	
        	tx.success();
        }
    }
    
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ImportDonneesTabulees i = new ImportDonneesTabulees();
		
		try ( Transaction tx = graphDb.beginTx() )
        {
			System.out.println("@-- Quel est le département dont le chef lieu est le chef "+
        "lieu de la région languedoc ? --@");
			String query = "match (dep:Departement)-[:CHEF_LIEU_DEPARTEMENT]->(com:Commune), "+
					"(reg:Region {nom:\"Languedoc-roussillon\"})-[:CHEF_LIEU_REGION]->(com) "+
					"RETURN dep.nom";
			Result result = graphDb.execute(query);
			while ( result.hasNext() )
    		{
    			 Map<String, Object> map = result.next();
    			 System.out.println(map.get("dep.nom"));
    		}
			tx.success();
        }
		System.out.println("Done");
	}

}
