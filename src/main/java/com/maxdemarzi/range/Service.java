package com.maxdemarzi.range;

import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.cursor.Cursor;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.kernel.api.ReadOperations;
import org.neo4j.kernel.api.cursor.NodeItem;
import org.neo4j.kernel.api.cursor.PropertyItem;
import org.neo4j.kernel.api.exceptions.PropertyKeyIdNotFoundKernelException;
import org.neo4j.kernel.api.exceptions.index.IndexNotFoundKernelException;
import org.neo4j.kernel.api.exceptions.schema.SchemaRuleNotFoundException;
import org.neo4j.kernel.api.index.IndexDescriptor;
import org.neo4j.kernel.impl.core.ThreadToStatementContextBridge;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/service")
public class Service {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @GET
    @Path("/documents_by_date/{from}/{to}")
    @Produces({"application/json"})
    public Response DocumentsByDate(
            @PathParam("from") final Long from,
            @PathParam("to") final Long to,
            @Context final GraphDatabaseService db) throws SchemaRuleNotFoundException, IndexNotFoundKernelException, IOException {

        List<Map<String, Object>> results = new ArrayList<>();

        try (Transaction tx = db.beginTx()) {

            ArrayList<Node> nodes = getDocumentsByRange(from, to, db);

            for (Node doc : nodes) {
                HashMap properties = new HashMap();
                for (String key : doc.getPropertyKeys()) {
                    properties.put(key, doc.getProperty(key));
                }
                results.add(properties);
            }
        }

        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    private ArrayList<Node> getDocumentsByRange(Long from, Long to, GraphDatabaseService db) throws SchemaRuleNotFoundException, IndexNotFoundKernelException {
        ThreadToStatementContextBridge ctx = ((GraphDatabaseAPI)db).getDependencyResolver().resolveDependency(ThreadToStatementContextBridge.class);
        ReadOperations ops = ctx.get().readOperations();
        int labelId = ops.labelGetForName("Document");
        int publishDatePropertyId = ops.propertyKeyGetForName("publish_date");

        IndexDescriptor indexDescriptor = ops.indexesGetForLabelAndPropertyKey(labelId, publishDatePropertyId);
        Cursor<NodeItem> nodeCursor = ops.nodeCursorGetFromIndexRangeSeekByNumber(indexDescriptor, from, true, to, true);
        ArrayList<Node> nodes = new ArrayList<>();

        while(nodeCursor.next()) {
            nodes.add(db.getNodeById(nodeCursor.get().id()));
        }
        return nodes;
    }

    @GET
    @Path("/documents_by_date2/{from}/{to}")
    @Produces({"application/json"})
    public Response DocumentsByDate2(
            @PathParam("from") final Long from,
            @PathParam("to") final Long to,
            @Context final GraphDatabaseService db) throws SchemaRuleNotFoundException, IndexNotFoundKernelException, IOException, PropertyKeyIdNotFoundKernelException {

        List<Map<String, Object>> results = new ArrayList<>();

        try (Transaction tx = db.beginTx()) {
            ThreadToStatementContextBridge ctx = ((GraphDatabaseAPI)db).getDependencyResolver().resolveDependency(ThreadToStatementContextBridge.class);
            ReadOperations ops = ctx.get().readOperations();
            int labelId = ops.labelGetForName("Document");
            int publishDatePropertyId = ops.propertyKeyGetForName("publish_date");

            IndexDescriptor indexDescriptor = ops.indexesGetForLabelAndPropertyKey(labelId, publishDatePropertyId);
            Cursor<NodeItem> nodeCursor = ops.nodeCursorGetFromIndexRangeSeekByNumber(indexDescriptor, from, true, to, true);
            ArrayList<Node> nodes = new ArrayList<>();

            while(nodeCursor.next()) {
                HashMap properties = new HashMap();
                NodeItem nodeItem = nodeCursor.get();
                Cursor<PropertyItem> propertyItemCursor = nodeItem.properties();
                while(propertyItemCursor.next()) {
                    properties.put(ops.propertyKeyGetName(propertyItemCursor.get().propertyKeyId()), propertyItemCursor.get().value());
                }
                results.add(properties);
            }
        }

        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

}
