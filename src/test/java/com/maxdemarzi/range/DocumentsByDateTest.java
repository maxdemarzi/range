package com.maxdemarzi.range;

import org.junit.Rule;
import org.junit.Test;
import org.neo4j.function.Function;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static junit.framework.TestCase.assertTrue;

public class DocumentsByDateTest {

    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(new Function<GraphDatabaseService, Void>() {
                @Override
                public Void apply(GraphDatabaseService db) throws RuntimeException {
                    try (Transaction tx = db.beginTx()) {
                        Schema schema = db.schema();
                        schema.indexFor(Labels.Document)
                                .on("publish_date")
                                .create();
                        tx.success();
                    }

                    try (Transaction tx = db.beginTx()) {
                        Node one = createDocument(db, "d1", "blah 1", 1446382722L);
                        Node two = createDocument(db, "d2", "blah 2", 1446486783L);
                        Node three = createDocument(db, "d3", "blah 3", 1446629532L);
                        tx.success();
                    }

                    return null;
                }
            })
            .withExtension("/v1", Service.class);

    private Node createDocument(GraphDatabaseService db, String id, String content, Long date) {
        Node one = db.createNode(Labels.Document);
        one.setProperty("doc_id", id);
        one.setProperty("content", content);
        one.setProperty("publish_date", date);
        return one;
    }

    @Test
    public void shouldGetDocuments() throws IOException {
        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/service/documents_by_date/1446382722/1446486783").toString());
        ArrayList actual = response.content();
        HashSet expectedSet = new HashSet<>(expected);
        HashSet actualSet = new HashSet<>(actual);

        assertTrue(actualSet.equals(expectedSet));
    }

    @Test
    public void shouldGetDocuments2() throws IOException {
        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/service/documents_by_date2/1446382722/1446486783").toString());
        ArrayList actual = response.content();
        HashSet expectedSet = new HashSet<>(expected);
        HashSet actualSet = new HashSet<>(actual);

        assertTrue(actualSet.equals(expectedSet));
    }

    private static final ArrayList expected = new ArrayList() {{
        add(new HashMap<String, Object>() {{
            put("doc_id", "d1");
            put("content", "blah 1");
            put("publish_date", 1446382722 );
        }});
        add(new HashMap<String, Object>() {{
            put("doc_id", "d2");
            put("content", "blah 2");
            put("publish_date", 1446486783 );
        }});
    }};

}
