package tp1;
import org.neo4j.graphdb.RelationshipType;


public enum Relations implements RelationshipType {

    WORKS_WITH, WORKS_FOR, FRIEND, OWNS
}