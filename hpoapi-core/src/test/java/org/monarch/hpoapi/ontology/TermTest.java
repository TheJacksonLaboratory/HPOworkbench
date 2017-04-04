package org.monarch.hpoapi.ontology;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Created by peter on 25.01.17.
 */
public class TermTest {

    static Term term=null;

    @BeforeClass
    public static void setup(){
        //Term(TermID id, String name, Namespace namespace, Collection<ParentTermID> parents)
        Prefix pre = new Prefix("HP");
        TermID tid = new TermID(pre,613);
        Namespace ns = new Namespace("human_phenotype");
        ParentTermID ptid1 = new ParentTermID(new TermID(pre,708),TermRelation.IS_A);
        ParentTermID ptid2 = new ParentTermID(new TermID(pre,504),TermRelation.IS_A);
        Collection<ParentTermID> c =  new ArrayList<ParentTermID>(Arrays.asList(ptid1,ptid2));
        TermTest.term = new Term(tid,"Photophobia",ns,c);
    }


    @Test
    public void getIDAsString() throws Exception {
        String id = term.getIDAsString();
        Assert.assertEquals("HP:0000613",id);
    }

    @Test
    public void getID() throws Exception {

    }

    @Test
    public void getName() throws Exception {

    }

    @Test
    public void getNamespace() throws Exception {

    }

    @Test
    public void getParents() throws Exception {

    }

    @Test
    public void toStringTest() throws Exception {

    }

    @Test
    public void equals() throws Exception {

    }

    @Test
    public void setObsolete() throws Exception {

    }

    @Test
    public void isObsolete() throws Exception {

    }

    @Test
    public void getDefinition() throws Exception {

    }

    @Test
    public void setDefinition() throws Exception {

    }

    @Test
    public void setEquivalents() throws Exception {

    }

    @Test
    public void getEquivalents() throws Exception {

    }

    @Test
    public void setAlternatives() throws Exception {

    }

    @Test
    public void getAlternatives() throws Exception {

    }

    @Test
    public void setSubsets() throws Exception {

    }

    @Test
    public void getSubsets() throws Exception {

    }

    @Test
    public void setSynonyms() throws Exception {

    }

    @Test
    public void getSynonyms() throws Exception {

    }

    @Test
    public void setXrefs() throws Exception {

    }

    @Test
    public void getXrefs() throws Exception {

    }

    @Test
    public void setIntersections() throws Exception {

    }

    @Test
    public void addAlternativeId() throws Exception {

    }

    @Test
    public void setInformationContent() throws Exception {

    }

    @Test
    public void getInformationContent() throws Exception {

    }

    @Test
    public void prefixPool() throws Exception {

    }

    @Test
    public void name() throws Exception {

    }

    @Test
    public void name1() throws Exception {

    }

}