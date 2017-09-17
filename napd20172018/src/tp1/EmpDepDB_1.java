package tp1;

/*
 * Licensed to Neo Technology under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Neo Technology licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;
import java.util.List;


import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.io.fs.FileUtils;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;



public class EmpDepDB_1
{
    public enum RelTypes implements RelationshipType
    {
        SOC_NODE,
        WORKS_WITH,
        WORKS_FOR
    }

    private static final String SOCIETY_DB = "target/societe-db";
    private static GraphDatabaseService graphDb;
    private long socNodeId;

    public static void main( String[] args ) throws IOException
    {
        EmpDepDB_1 societe = new EmpDepDB_1();
        societe.setUp();
        System.out.println( societe.printThomasFriends() );
        Label eLabel = DynamicLabel.label("Employee");
     //   findAllNodesByTypes();
        findWithCypher();
       societe.shutdown();
    }

    public void setUp() throws IOException
    {
        FileUtils.deleteRecursively( new File( SOCIETY_DB ) );
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( new File(SOCIETY_DB) );
        registerShutdownHook();
        createNodespace();
    }

    public void shutdown()
    {
        graphDb.shutdown();
    }

    public void createNodespace()
    {
        try ( Transaction tx = graphDb.beginTx() )
        {
            // Create society node
            Node societe = graphDb.createNode();
            societe.setProperty( "name", "Soc SA" );
            socNodeId = societe.getId();

            Label empLabel = DynamicLabel.label("Employee");
            Label depLabel = DynamicLabel.label("Department");
            
            // Create Employee Thomas
            Node thomas = graphDb.createNode();
            thomas.addLabel(empLabel);
            thomas.setProperty( "name", "Thomas Dubois" );
            thomas.setProperty( "age", 29 );

            // connect Thomas to the society
            societe.createRelationshipTo( thomas, RelTypes.SOC_NODE );

            // Create Employee Sophie
            Node sophie = graphDb.createNode();
            sophie.addLabel(empLabel);
            sophie.setProperty( "name", "Sophie Martin" );
            sophie.setProperty( "job", "scientist" );
            Relationship rel = thomas.createRelationshipTo( sophie,
                    RelTypes.WORKS_WITH );
            rel.setProperty( "since", "3 years" );
            
         // Create Employee Pierre
            Node pierre = graphDb.createNode();
            pierre.addLabel(empLabel);
            pierre.setProperty( "name", "Pierre Claudel" );
            pierre.setProperty( "salary", "high" );
            thomas.createRelationshipTo( pierre, RelTypes.WORKS_WITH );
            rel = pierre.createRelationshipTo( sophie, RelTypes.WORKS_WITH );
            rel.setProperty( "since", "12 years" );
            
          // Create Department   
            Node production = graphDb.createNode();
            production.addLabel(depLabel);
            production.setProperty( "nom", "production" );
            production.setProperty( "localisation", "Usine A" );
            pierre.createRelationshipTo( production, RelTypes.WORKS_FOR );
            rel = thomas.createRelationshipTo( production, RelTypes.WORKS_FOR );
            rel.setProperty( "role", "supervisor" );
            
            tx.success();
        }
    }

    /**
     * Get the Society node
     *
     */
    private Node getSocieteNode()
    {
        return graphDb.getNodeById( socNodeId )
                .getSingleRelationship( RelTypes.SOC_NODE, Direction.OUTGOING )
                .getEndNode();
    }

    public String printThomasFriends()
    {
        try ( Transaction tx = graphDb.beginTx() )
        {
            Node tomNode = getSocieteNode();
            // START SNIPPET: friends-usage
            int numberOfFriends = 0;
            String output = tomNode.getProperty( "name" ) + "'s colleagues:\n";
            Traverser friendsTraverser = getColleagues( tomNode );
            for ( Path friendPath : friendsTraverser )
            {
                output += "At depth " + friendPath.length() + " => "
                          + friendPath.endNode()
                                  .getProperty( "name" ) + "\n";
                numberOfFriends++;
            }
            output += "Number of friends found: " + numberOfFriends + "\n";
            // END SNIPPET: friends-usage
            return output;
        }
    }

    // START SNIPPET: getColleagues
    private Traverser getColleagues(
            final Node person )
    {
        TraversalDescription td = graphDb.traversalDescription()
                .breadthFirst()
                .relationships( RelTypes.WORKS_WITH, Direction.OUTGOING)
                .evaluator( Evaluators.excludeStartPosition() );
        return td.traverse( person );
    }
    // END SNIPPET: getColleagues

  
    public static void findAllNodesByTypes()
    {
    	try ( Transaction tx = graphDb.beginTx() )
    	{
    Label eLabel =	DynamicLabel.label("Employee");
    ResourceIterator<Node> employees = graphDb.findNodes(eLabel);
    System.out.println( "Employees:" );
    while( employees.hasNext() )
    {
        Node employee = employees.next();
        System.out.println( "\t" + employee.getProperty( "name" ) );
    }
}}
    
    // ajout person cypher
    
    public static void findWithCypher()
    {
    	try ( Transaction tx = graphDb.beginTx() ;
    			Result result = graphDb.execute( "match (n {name: 'Sophie Martin'}) return n, n.job" )  )
    	{ 
    		 while ( result.hasNext() )
    		 {
    			 Map<String, Object> map = result.next();
 			for (Map.Entry<String, Object> entry : map.entrySet()) {
 				System.out.println("key: " + entry.getKey() + ", value " + entry.getValue());
 			} 
    		 }  			 }
    			 
  }
      
    
    private void registerShutdownHook()
    {
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
    }
}
