package eu.supersede.mdm.storage.parsers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import eu.supersede.mdm.storage.model.metamodel.*;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import eu.supersede.mdm.storage.util.NamespaceFiles;
import scala.Tuple3;

import java.util.List;
import java.util.Map;

/**
 * Created by snadal on 2/06/16.
 */
public class OWLtoD3 {

    private static ImmutableMap<String,String> colorMap = ImmutableMap.<String, String>builder()
            // Source graph colors
            .put(SourceGraph.DATA_SOURCE.val(), "#FF3300")
            .put(SourceGraph.WRAPPER.val(), "#FECB98")
            .put(SourceGraph.ATTRIBUTE.val(), "#00CCFF")
            .put(SourceGraph.HAS_WRAPPER.val(), "#FF3300")
            .put(SourceGraph.HAS_ATTRIBUTE.val(), "#00CCFF")

            // Global graph colors
            .put(GlobalGraph.CONCEPT.val(), "#33CCCC")
            .put(GlobalGraph.FEATURE.val(), "#D7DF01")
            .put(GlobalGraph.HAS_FEATURE.val(), "#D7DF01")
            .put(GlobalGraph.INTEGRITY_CONSTRAINT.val(), "#CC99FF")
            .put(GlobalGraph.HAS_INTEGRITY_CONSTRAINT.val(), "#CC99FF")
            .put(GlobalGraph.DATATYPE.val(), "#FF6600")
            .put(GlobalGraph.HAS_DATATYPE.val(), "#FF6600")

            .put(Mappings.MAPS_TO.val(), "white")
            .build();

    public static String parse(String artifactType, List<Tuple3<Resource,Property,Resource>> triples) {
        NamespaceFiles ns = new NamespaceFiles();

        List<Tuple3<Resource,Property,Resource>> elementsToShow = Lists.newArrayList();

        triples.iterator().forEachRemaining(triple -> {
            // Check that not (s,p,o) are from external namespaces at the same time
            // Check that s is not in one of the ignored namespaces
            // Check that s is not part of the BDI ontology
            if ((!ns.getNamespaces().contains(triple._1().getNameSpace()) ||
                    !ns.getNamespaces().contains(triple._2().getNameSpace()) ||
                    !ns.getNamespaces().contains(triple._3().getNameSpace())) &&
                    !ns.getIgnoredNamespaces().contains(triple._1().getNameSpace()) &&
                    !Metamodel.contains(artifactType,triple._1().getURI()) &&
                    !triple._3().toString().equals("http://www.w3.org/2000/01/rdf-schema#Resource")) {

                elementsToShow.add(triple);
            }
        });

        Map<String,Integer> nodesMap = Maps.newHashMap();
        Integer i = 0;
        JSONArray d3Nodes = new JSONArray();
        // Add classes as nodes
        for (Tuple3<Resource,Property,Resource> triple : elementsToShow) {
            if (!nodesMap.containsKey(triple._1().getURI())) {
                nodesMap.put(triple._1().getURI(), i);
                ++i;
                JSONObject d3Node = new JSONObject();
                //d3Node.put("name", triple._1().getURI().substring(triple._1().getURI().lastIndexOf("/")+1));
                d3Node.put("name", triple._1().getURI());
                d3Node.put("iri", triple._1().getURI());
                d3Node.put("namespace",
                        triple._3().getURI() == null ? triple._1().getURI() : triple._3().getURI());
                // Get the color from the namespace of the element
                d3Node.put("color", colorMap.get(triple._3().getURI()) == null ? colorMap.get(triple._1().getURI()) : colorMap.get(triple._3().getURI()));
                d3Nodes.add(d3Node);
            }
        }
        JSONArray d3Links = new JSONArray();
        // Add links
        for (Tuple3<Resource,Property,Resource> triple : elementsToShow) {
            if (!ns.getIgnoredNamespaces().contains(triple._2().getNameSpace())) {
                JSONObject d3Link = new JSONObject();
                d3Link.put("source",nodesMap.get(triple._1().getURI()));
                d3Link.put("target",nodesMap.get(triple._3().getURI()));
                d3Link.put("iri", triple._2().getURI());
                d3Link.put("name", triple._2().getLocalName());
                if (!colorMap.containsKey(triple._2().getURI())) {
                    d3Link.put("color", colorMap.get(GlobalGraph.CONCEPT.val()));
                } else {
                    d3Link.put("color", colorMap.get(triple._2().getURI()));
                }

                d3Links.add(d3Link);
            }
        }
        JSONObject d3 = new JSONObject();
        d3.put("nodes",d3Nodes);
        d3.put("links",d3Links);
        return d3.toJSONString();
    }

}
