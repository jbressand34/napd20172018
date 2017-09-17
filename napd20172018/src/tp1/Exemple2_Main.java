package tp1;


import java.io.File;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.ResourceIterator;

public class Exemple2_Main {
	 private static final String Soc_Db = "/home/isa/Bureau/BD/Docs_BDNoR/Neo4J_17/neo4j-community-3.2.3/data/graph.db";
	 static GraphDatabaseFactory graphDbFactory = new GraphDatabaseFactory();
	 static GraphDatabaseService graphDb = graphDbFactory.newEmbeddedDatabase(
			  new File("data/soc"));
	 
	 public static void main(String[] args) {
		 Transaction tx = graphDb.beginTx();

	        try {
		 Node car = graphDb.createNode(Labels.CAR);
		 car.setProperty("brand", "citroen");
		 car.setProperty("model", "2cv");
		  
		 Node owner = graphDb.createNode(Labels.EMPLOYEE);
		 owner.setProperty("firstName", "Isa");
		 owner.setProperty("lastName", "M");
		 owner.setProperty("job", "teacher");
		 owner.createRelationshipTo(car, Relations.OWNS);
		 Result result = graphDb.execute(
				  "MATCH (c:CAR) <-[OWNS]- (p:EMPLOYEE) " +
				  "WHERE c.brand = 'citroen'" +
				  "RETURN p.firstName, p.lastName");
	//	 System.out.println(result.toString());
		 while ( result.hasNext() )
	     {
	         Map<String, Object> row = result.next();
	         for ( String key : result.columns() )
	         {
	             System.out.printf( "%s = %s%n", key, row.get( key ) );
	         }
	     }
		 findWithCypher();
		 findAllNodesByTypes();
		 findAllOwners();
		 tx.success();
	        } catch (Exception e) {
	            tx.failure();
	        } finally {
	          //  tx.finish();
	        }
    }
	 
	 public static void findWithCypher()
	    {
	    	try ( Transaction tx = graphDb.beginTx() ;
	    			Result result = graphDb.execute( "match (n {firstName: 'Isa'}) return n, n.job" )  )
	    	{ 
	    		 while ( result.hasNext() )
	    		 {
	    			 Map<String, Object> map = result.next();
	 			for (Map.Entry<String, Object> entry : map.entrySet()) {
	 				System.out.println("key: " + entry.getKey() + ", value " + entry.getValue());
	 			} 
	    		 }  			 }
	    			 
	  }

	 public static void findAllNodesByTypes()
	    {
	    	try ( Transaction tx = graphDb.beginTx() )
	    	{
	   
	    ResourceIterator<Node> employees = graphDb.findNodes(Labels.EMPLOYEE);
	    System.out.println( "Employees:" );
	    while( employees.hasNext() )
	    {
	        Node employee = employees.next();
	        System.out.println( "\t" + employee.getProperty( "firstName" ) );
	    }
	    	}}
	 
	 public static void findAllOwners()
	    {
	    	try ( Transaction tx = graphDb.beginTx() )
	    	{
	    		ResourceIterator<Node> owners = graphDb.findNodes( Labels.EMPLOYEE );
	    		System.out.println("Owners:");
	 while( owners.hasNext() )
	 {
	     Node owner = owners.next();
	     System.out.print( "\t" + owner.getProperty( "firstName" ) + " owns " );
	     for( Relationship relationship : owner.getRelationships(
	          Relations.OWNS ) )
	     {
	         Node car = relationship.getOtherNode( owner );
	         System.out.print( "\t" + car.getProperty( "brand" ) );
	     }
	     System.out.println();
	 }
	    	}}
	 
}
	    
