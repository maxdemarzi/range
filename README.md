# Range Queries
Example Neo4j Extension using Range Queries


# Instructions

1. Build it:

        mvn clean package

2. Copy target/range-1.0.jar to the plugins/ directory of your Neo4j server.

3. Configure Neo4j by adding a line to conf/neo4j-server.properties:

        org.neo4j.server.thirdparty_jaxrs_classes=com.maxdemarzi=/v1
        
4. Start Neo4j server.

5. Create some test data:

        CREATE INDEX ON :Document(publish_date)
        CREATE (doc1:Document {doc_id:'d1', content:'blah 1', publish_date:1446382722})
        CREATE (doc2:Document {doc_id:'d2', content:'blah 2', publish_date:1446486783})
        CREATE (doc3:Document {doc_id:'d3', content:'blah 3', publish_date:1446629532})
        
6. Try it:
        
        :GET /v1/service/documents_by_date/1446382722/1446486783
        :GET /v1/service/documents_by_date2/1446382722/1446486783

7. Test it.

You can follow the same idea for nodeCursorGetFromIndexRangeSeekByString and nodeCursorGetFromIndexRangeSeekByPrefix