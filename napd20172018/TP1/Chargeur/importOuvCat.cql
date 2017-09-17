USING PERIODIC COMMIT
LOAD CSV WITH HEADERS FROM 'file:///Ouv.csv' AS ouvrages
CREATE (o:Ouvrage {id : toInt(ouvrages.id), title : ouvrages.title, date : toInt(ouvrages.date)});

USING PERIODIC COMMIT
LOAD CSV WITH HEADERS FROM 'file:///Cat.csv' AS categories
CREATE (c:Categorie {id : toInt(categories.id), name : categories.name});

USING PERIODIC COMMIT
LOAD CSV WITH HEADERS FROM 'file:///Ouv_Cat.csv' AS ouv_cat
MERGE (o : Ouvrage { id : toInt(ouv_cat.ouv_id) })
MERGE (c : Categorie { id : toInt(ouv_cat.cat) })
CREATE (o)-[:CATEGORIZED_AS]->(c);

