package eu.supersede.mdm.storage.bdi.alignment;

import eu.supersede.mdm.storage.util.ConfigManager;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.Tuple3;
import uk.ac.ox.krr.logmap2.LogMap2_Matcher;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LogMapMatcher {
    private String iri_onto1;
    private String iri_onto2;
    private String alignments_iri;
    private List<Tuple3<String, String, String>> alignmentsTriples = new ArrayList<>();
    private List<Tuple3<String, String, String>> classesAlignments = new ArrayList<>();

    public List<Tuple3<String, String, String>> getAlignmentsTriples() {
        return alignmentsTriples;
    }

    public List<Tuple3<String, String, String>> getClassesAlignments() {
        return classesAlignments;
    }

    private LogMap2_Matcher logMap2_matcher = null;

    public LogMapMatcher(String iri_onto1, String iri_onto2, String alignments_iri) {
        this.iri_onto1 = iri_onto1;
        this.iri_onto2 = iri_onto2;
        this.alignments_iri = alignments_iri;

        startLogMapMatcher();
        extractMappings();
        storeMappingsInTripleStore();
    }

    private void startLogMapMatcher() {
        try {
            logMap2_matcher = new LogMap2_Matcher(
                    "file:" + iri_onto1,
                    "file:" + iri_onto2,
                    ConfigManager.getProperty("logmap_output_path"),
                    true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void extractMappings() {
        try {
            this.iterateOverMappings(logMap2_matcher.getLogmap2_Mappings());
            this.iterateOverMappings(logMap2_matcher.getLogmap2_DiscardedMappings());
            //this.iterateOverMappings(logMap2_matcher.getLogmap2_HardDiscardedMappings());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void storeMappingsInTripleStore() {
        RDFUtil.addBatchOfTriples(alignments_iri, alignmentsTriples);
    }

    private void iterateOverMappings(Set<MappingObjectStr> mappings) throws Exception {

        for (MappingObjectStr mapping : mappings) {
            if (mapping.isClassMapping()) {
                this.alignmentsTriples.add(new Tuple3<>(mapping.getIRIStrEnt1(), mapping.getIRIStrEnt2(), Double.toString(mapping.getConfidence()) + "__CLASS__" + mapping.getLexicalConfidenceMapping() + "__" + mapping.getStructuralConfidenceMapping() + "__" + mapping.getMappingDirection()));
                this.classesAlignments.add(new Tuple3<>(mapping.getIRIStrEnt1(), mapping.getIRIStrEnt2(), Double.toString(mapping.getConfidence())));
                //System.out.println("isClassMapping" + mapping.getIRIStrEnt1() + "* - * " + mapping.getIRIStrEnt2() + "* - * " + mapping.getMappingDirection() + "* - * " + mapping.getConfidence());
            } else if (mapping.isObjectPropertyMapping()) {
                this.alignmentsTriples.add(new Tuple3<>(mapping.getIRIStrEnt1(), mapping.getIRIStrEnt2(), Double.toString(mapping.getConfidence()) + "__OBJECT-PROPERTY__" + mapping.getLexicalConfidenceMapping() + "__" + mapping.getStructuralConfidenceMapping() + "__" + mapping.getMappingDirection()));
                //System.out.println("isObjectPropertyMapping" +mapping.getIRIStrEnt1() + "* - * " + mapping.getIRIStrEnt2() + "* - * " + mapping.getMappingDirection() + "* - * " + mapping.getConfidence());
            } else if (mapping.isDataPropertyMapping()) {
                this.alignmentsTriples.add(new Tuple3<>(mapping.getIRIStrEnt1(), mapping.getIRIStrEnt2(), Double.toString(mapping.getConfidence()) + "__DATA-PROPERTY__" + mapping.getLexicalConfidenceMapping() + "__" + mapping.getStructuralConfidenceMapping() + "__" + mapping.getMappingDirection()));
                //System.out.println("isDataPropertyMapping" +mapping.getIRIStrEnt1() + "* - * " + mapping.getIRIStrEnt2() + "* - * " + mapping.getMappingDirection() + "* - * " + mapping.getConfidence());
            } else if (mapping.isInstanceMapping()) {
                this.alignmentsTriples.add(new Tuple3<>(mapping.getIRIStrEnt1(), mapping.getIRIStrEnt2(), Double.toString(mapping.getConfidence()) + "__INSTANCE-MAPPING__" + mapping.getLexicalConfidenceMapping() + "__" + mapping.getStructuralConfidenceMapping() + "__" + mapping.getMappingDirection()));
                //System.out.println("isInstanceMapping" +mapping.getIRIStrEnt1() + "* - * " + mapping.getIRIStrEnt2() + "* - * " + mapping.getMappingDirection() + "* - * " + mapping.getConfidence());
            }
        }
    }
}
