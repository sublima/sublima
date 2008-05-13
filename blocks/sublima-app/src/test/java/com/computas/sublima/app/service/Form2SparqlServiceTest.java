package com.computas.sublima.app.service;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.Arrays;

import com.computas.sublima.app.service.Form2SparqlService;
import com.hp.hpl.jena.sparql.util.StringUtils;
import com.computas.sublima.query.service.SearchService;

/**
 * Form2SparqlService Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>12/03/2007</pre>
 */
public class Form2SparqlServiceTest extends TestCase {
  private Map<String, String[]> testMap;
  private String expectedString;
  private String expectedPrefix;
  private String[] testString;
  private LinkedList emptyList;
  private Form2SparqlService myService;
  static String DCNS = "http://purl.org/dc/terms/";

  public Form2SparqlServiceTest(String name) {
    super(name);
  }

  public void setUp() throws Exception {
    testMap = new TreeMap<String, String[]>();
    expectedString = "DESCRIBE ?resource ?rest WHERE {\n?resource dc:title \"Cirrus Personal Jet\"";
    testString = new String[1];
    testString[0] = "Cirrus Personal Jet";
    List emptyList = new LinkedList();
    expectedPrefix = "PREFIX dct: <http://purl.org/dc/terms/>\nPREFIX foaf: <http://xmlns.com/foaf/0.1/>\n";
    myService = new Form2SparqlService(new String[]{"dct: <http://purl.org/dc/terms/>", "foaf: <http://xmlns.com/foaf/0.1/>"});
  }
  

  public void tearDown() throws Exception {
    testMap = null;
  }

  public static Test suite() {
    return new TestSuite(Form2SparqlServiceTest.class);
  }

  public void testLanguage() {
    myService.setLanguage("en");
    assertEquals("Expected result and actual result not equal", "en",
            myService.getLanguage());

  }

  public void testPrefixes() {

    assertEquals("Expected result and actual result not equal", expectedPrefix, myService.getPrefixString());

  }


  public void testConvertFormField2N3SingleLiteral() {
    String expectS = "\n?resource dc:title \"Cirrus Personal Jet\" .";
    assertEquals("Expected result and actual result not equal", expectS,
            myService.convertFormField2N3("dc:title", testString));
  }

  public void testConvertFormField2N3DualLiteral() {
    String expectS = "\n?resource dc:subject ?var1 .\n?var1 rdfs:label \"Jet\" .";
    testString[0] = "Jet";
    assertEquals("Expected result and actual result not equal", expectS,
            myService.convertFormField2N3("dc:subject/rdfs:label", testString));
  }

  public void testConvertFormField2N3AllSubjectLabels() {
    String expectS = "\n?resource dct:subject ?var1 .\n{ ?var1 skos:prefLabel \"Jet\" . }\nUNION { ?var1 skos:altLabel \"Jet\" . }\nUNION { ?var1 skos:hiddenLabel \"Jet\" . }\n";
    testString[0] = "Jet";
    assertEquals("Expected result and actual result not equal", expectS,
            myService.convertFormField2N3("dct:subject/all-labels", testString));
  }

  public void testConvertFormField2N3AllSubjectLabelsFree() {
    String expectS = "\n?resource dct:subject ?var1 .\n{ ?var1 skos:prefLabel ?free1 .\n?free1 pf:textMatch '+Jet' . }\nUNION { ?var1 skos:altLabel ?free1 .\n?free1 pf:textMatch '+Jet' . }\nUNION { ?var1 skos:hiddenLabel ?free1 .\n?free1 pf:textMatch '+Jet' . }\n";
    testString[0] = "Jet";
    myService.addFreetextField("dct:subject/all-labels");
    assertEquals("Expected result and actual result not equal", expectS,
            myService.convertFormField2N3("dct:subject/all-labels", testString));
  }
    
  public void testConvertFormField2N3SingleFree() {
	  String expectS = "\n?resource dc:title ?free1 .\n?free1 pf:textMatch '+Cirrus Personal Jet' .";
      myService.addFreetextField("dc:title");
      assertEquals("Expected result and actual result not equal", expectS,
			  myService.convertFormField2N3("dc:title", testString));
  }	

  public void testConvertFormField2N3DualFree() {
	  String expectS = "\n?resource dc:subject ?var1 .\n?var1 rdfs:label ?free1 .\n?free1 pf:textMatch '+Jet' .";
	  testString[0] = "Jet";
      myService.addFreetextField("dc:subject/rdfs:label");
      assertEquals("Expected result and actual result not equal", expectS,
			  myService.convertFormField2N3("dc:subject/rdfs:label", testString));
  }	

  public void testConvertFormField2N3SingleLiteralLang() {
    myService.setLanguage("en");
    String expectS = "\n?resource dc:title \"Cirrus Personal Jet\"@en .";
    assertEquals("Expected result and actual result not equal", expectS,
            myService.convertFormField2N3("dc:title", testString));
  }

  public void testConvertFormField2N3DualLiteralLang() {
    myService.setLanguage("en");
    String expectS = "\n?resource dc:subject ?var1 .\n?var1 rdfs:label \"Jet\"@en .";
    testString[0] = "Jet";
    assertEquals("Expected result and actual result not equal", expectS,
            myService.convertFormField2N3("dc:subject/rdfs:label", testString));
  }

  public void testConvertFormField2N3TripleLiteral() {
    String expectS = "\n?resource ex:example ?var1 .\n?var1 foo:foobar ?var2 .\n?var2 bar:foo \"Jet\" .";
    testString[0] = "Jet";
    assertEquals("Expected result and actual result not equal", expectS,
            myService.convertFormField2N3("ex:example/foo:foobar/bar:foo", testString));
  }

  public void testConvertFormField2N3SingleURI() {
    String expectS = "\n?resource dc:publisher <http://sublima.computas.com/agent/cirrus-design> .";
    testString[0] = "http://sublima.computas.com/agent/cirrus-design";
    assertEquals("Expected result and actual result not equal", expectS,
            myService.convertFormField2N3("dc:publisher", testString));
  }

  public void testConvertFormField2N3SingleDataType() {
    String expectS = "\n?resource dct:dateAccepted \"2007-12-05T14:23:00\"^^<http://www.w3.org/2001/XMLSchema#dateTime> .";
    testString[0] = "2007-12-05T14:23:00";
    assertEquals("Expected result and actual result not equal", expectS,
            myService.convertFormField2N3("dct:dateAccepted", testString));
  }

  public void testConvertFormField2N3SingleMailTo() {
    String expectS = "\n?resource foaf:mbox <mailto:test@example.org> .";
    testString[0] = "mailto:test@example.org";
    assertEquals("Expected result and actual result not equal", expectS,
            myService.convertFormField2N3("foaf:mbox", testString));
  }

  public void testConvertFormField2N3DualURI() {
    String expectS = "\n?resource dc:publisher ?var1 .\n?var1 foaf:homepage <http://www.cirrusdesign.com/> .";
    testString[0] = "http://www.cirrusdesign.com/";
    assertEquals("Expected result and actual result not equal", expectS,
            myService.convertFormField2N3("dc:publisher/foaf:homepage", testString));
  }

  public void testConvertFormField2N3DoubleDual() {
    String expectS = "\n?resource dc:publisher ?var1 .\n?var1 foaf:homepage <http://www.cirrusdesign.com/> .\n?resource dc:subject ?var2 .\n?var2 rdfs:label \"Jet\" .";
    testString[0] = "http://www.cirrusdesign.com/";
    String actual = myService.convertFormField2N3("dc:publisher/foaf:homepage", testString);
    testString[0] = "Jet";
    actual = actual + myService.convertFormField2N3("dc:subject/rdfs:label", testString);

    assertEquals("Expected result and actual result not equal", expectS, actual);
  }
  
  public void testConvertFormField2N3DoubleDualOneFree() {
	    String expectS = "\n?resource dc:publisher ?var1 .\n?var1 foaf:homepage <http://www.cirrusdesign.com/> .\n?resource dc:subject ?var2 .\n?var2 rdfs:label ?free1 .\n?free1 pf:textMatch '+Jet' .";
	    testString[0] = "http://www.cirrusdesign.com/";
	    String actual = myService.convertFormField2N3("dc:publisher/foaf:homepage", testString);
	    testString[0] = "Jet";
        myService.addFreetextField("dc:subject/rdfs:label");
        actual = actual + myService.convertFormField2N3("dc:subject/rdfs:label", testString);

	    assertEquals("Expected result and actual result not equal", expectS, actual);
  }

    public void testConvertFormField2N3DoubleDualBothFree() {
        String expectS = "\n?resource dc:title ?free1 .\n?free1 pf:textMatch '+Cirrus Personal Jet' .\n?resource dc:subject ?var1 .\n?var1 rdfs:label ?free2 .\n?free2 pf:textMatch '+Jet' .";
        testString[0] = "Cirrus Personal Jet";
       myService.addFreetextField("dc:title");
        myService.addFreetextField("dc:subject/rdfs:label");
        String actual = myService.convertFormField2N3("dc:title", testString);
        testString[0] = "Jet";
        actual = actual + myService.convertFormField2N3("dc:subject/rdfs:label", testString);

        assertEquals("Expected result and actual result not equal", expectS, actual);
    }

  public void testConvertFormField2N3DualEmpty() {
    String expectS = "\n";
    testString[0] = "";
    assertEquals("Expected result and actual result not equal", expectS,
            myService.convertFormField2N3("dc:publisher/foaf:homepage", testString));
  }


  public void testConvertForm2SparqlSingleValueNoLang() {
    // Single value test
    testMap.put("dc:title", testString);
    String resultString = myService.convertForm2Sparql(testMap);
    assertEquals("Expected result and actual result not equal", expectedPrefix + expectedString + " .\n?resource ?p ?rest .\n}", resultString);
  }

  public void testConvertForm2SparqlSingleValueFreetext() {
      // Single value test, with simple freetext search
      testMap.put("dc:title", testString);
      SearchService searchService = new SearchService("AND");
      testMap.put("searchstring", new String[]{searchService.buildSearchString("engine")});
      myService.addPrefix("pf: <http://jena.hpl.hp.com/ARQ/property#>");
      String resultString = myService.convertForm2Sparql(testMap);
      assertEquals("Expected result and actual result not equal", 
		   StringUtils.join("\n", new String[]{
		       "PREFIX dct: <http://purl.org/dc/terms/>",
		       "PREFIX foaf: <http://xmlns.com/foaf/0.1/>",
		       "PREFIX pf: <http://jena.hpl.hp.com/ARQ/property#>", 
		       "DESCRIBE ?subject ?publisher ?resource ?rest WHERE {",
		       "?resource dc:title \"Cirrus Personal Jet\" .",
		       "  ?lit pf:textMatch ( 'engine*' 100) .",
		       "  {",
		       "    ?resource ?p1 ?lit;",
		       "              dct:subject ?subject ;",
		       "              dct:publisher ?publisher .",
		       "  }",
		       "  UNION",
		       "  {",
		       "      ?resource dct:subject ?subject1 .",
		       "      ?subject1 ?p2 ?lit .",
		       "      ?resource dct:subject ?subject ;",
		       "                dct:publisher ?publisher .",
		       "  }",
		       "  UNION",
		       "  {",
		       "      ?resource dct:publisher ?publisher1 .",
		       "      ?publisher1 ?p2 ?lit .",
		       "      ?resource dct:subject ?subject ;",
		       "                dct:publisher ?publisher .",
		       "  }\n?resource ?p ?rest .\n}"}), resultString);
  }

    public void testConvertForm2SparqlNoValueFreetextDeep() {
      // Single value test, with simple freetext search
      SearchService searchService = new SearchService("AND");
      testMap.put("searchstring", new String[]{searchService.buildSearchString("engine")});
      testMap.put("deepsearch", new String[]{"deepsearch"});

      myService.addPrefix("pf: <http://jena.hpl.hp.com/ARQ/property#>");
      String resultString = myService.convertForm2Sparql(testMap);
      assertEquals("Expected result and actual result not equal",
		   StringUtils.join("\n", new String[]{
		       "PREFIX dct: <http://purl.org/dc/terms/>",
		       "PREFIX foaf: <http://xmlns.com/foaf/0.1/>",
		       "PREFIX pf: <http://jena.hpl.hp.com/ARQ/property#>",
               "PREFIX sub: <http://xmlns.computas.com/sublima#>",
               "PREFIX link: <http://www.w3.org/2007/ont/link#>",
               "DESCRIBE ?subject ?publisher ?request ?resource ?rest WHERE {",
		       "  ?lit pf:textMatch ( 'engine*' 100) .",
		       "  {",
		       "    ?resource ?p1 ?lit;",
		       "              dct:subject ?subject ;",
		       "              dct:publisher ?publisher ;",
               "              link:request ?request .",
               "  }",
		       "  UNION",
		       "  {",
		       "      ?resource dct:subject ?subject1 .",
		       "      ?subject1 ?p2 ?lit .",
		       "      ?resource dct:subject ?subject ;",
		       "                dct:publisher ?publisher ;",
	           "                link:request ?request .",
               "  }",
		       "  UNION",
		       "  {",
		       "      ?resource dct:publisher ?publisher1 .",
		       "      ?publisher1 ?p2 ?lit .",
		       "      ?resource dct:subject ?subject ;",
		       "                dct:publisher ?publisher ;",
               "                link:request ?request .",
		       "  }",
		       "  UNION",
		       "  {",
		       "      ?resource link:request ?request1 .",
		       "      ?request1 sub:stripped ?lit .",
		       "      ?resource dct:subject ?subject ;",
		       "                dct:publisher ?publisher ;",
               "                link:request ?request .",
		       "  }\n?resource ?p ?rest .\n}"}), resultString);
  }

    public void testConvertForm2SparqlNoValueFreetext() {
      // Single value test, with simple freetext search
      SearchService searchService = new SearchService("AND");
      testMap.put("searchstring", new String[]{searchService.buildSearchString("engine")});
      myService.addPrefix("pf: <http://jena.hpl.hp.com/ARQ/property#>");
      String resultString = myService.convertForm2Sparql(testMap);
      assertEquals("Expected result and actual result not equal",
		   StringUtils.join("\n", new String[]{
		       "PREFIX dct: <http://purl.org/dc/terms/>",
		       "PREFIX foaf: <http://xmlns.com/foaf/0.1/>",
		       "PREFIX pf: <http://jena.hpl.hp.com/ARQ/property#>",
		       "DESCRIBE ?subject ?publisher ?resource ?rest WHERE {",
		       "  ?lit pf:textMatch ( 'engine*' 100) .",
		       "  {",
		       "    ?resource ?p1 ?lit;",
		       "              dct:subject ?subject ;",
		       "              dct:publisher ?publisher .",
		       "  }",
		       "  UNION",
		       "  {",
		       "      ?resource dct:subject ?subject1 .",
		       "      ?subject1 ?p2 ?lit .",
		       "      ?resource dct:subject ?subject ;",
		       "                dct:publisher ?publisher .",
		       "  }",
		       "  UNION",
		       "  {",
		       "      ?resource dct:publisher ?publisher1 .",
		       "      ?publisher1 ?p2 ?lit .",
		       "      ?resource dct:subject ?subject ;",
		       "                dct:publisher ?publisher .",
		       "  }\n?resource ?p ?rest .\n}"}), resultString);
  }


  public void testConvertForm2SparqlSingleValue() {
    // Single value test
    testMap.put("dc:title", testString);
    testMap.put("interface-language", new String[]{"en"}); // this parameter is a magic string
    String resultString = myService.convertForm2Sparql(testMap);
    assertEquals("Expected result and actual result not equal", expectedPrefix + expectedString + "@en .\n?resource ?p ?rest .\n}", resultString);
  }

  public void testConvertForm2SparqlTwoValues() {
    // Single value test
    testMap.put("dc:title", testString);
    testMap.put("dc:description", new String[]{"A Very Light Jet Aircraft under construction."});
    testMap.put("interface-language", new String[]{"en"}); // this parameter is a magic string
    String resultString = myService.convertForm2Sparql(testMap);
    assertEquals("Expected result and actual result not equal", expectedPrefix + "DESCRIBE ?resource ?rest WHERE {\n?resource dc:title \"Cirrus Personal Jet\"@en .\n?resource dc:description \"A Very Light Jet Aircraft under construction.\"@en .\n?resource ?p ?rest .\n}", resultString);
  }
  
  public void testConvertForm2SparqlTwoValuesOneFree() {
	    // Single value test
        Form2SparqlService myServicefree = new Form2SparqlService(new String[]{"dct: <http://purl.org/dc/terms/>", "foaf: <http://xmlns.com/foaf/0.1/>"}, new String[]{"dc:title"});
	    testMap.put("dc:title", testString);
	    testMap.put("dc:description", new String[]{"A Very Light Jet Aircraft under construction."});
	    testMap.put("interface-language", new String[]{"en"}); // this parameter is a magic string
	    String resultString = myServicefree.convertForm2Sparql(testMap);
	    assertEquals("Expected result and actual result not equal", expectedPrefix + "PREFIX pf: <http://jena.hpl.hp.com/ARQ/property#>\n" +"DESCRIBE ?resource ?rest WHERE {\n?resource dc:title ?free1 .\n?free1 pf:textMatch '+Cirrus Personal Jet' .\n?resource dc:description \"A Very Light Jet Aircraft under construction.\"@en .\n?resource ?p ?rest .\n}", resultString);
	  }

  public void testConvertForm2SparqlTwoValuesSubjectFree() {
	    // Single value test
        String expectS = StringUtils.join("\n", new String[]{"PREFIX dct: <http://purl.org/dc/terms/>",
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/>",
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>",
                "PREFIX pf: <http://jena.hpl.hp.com/ARQ/property#>",
                "DESCRIBE ?resource ?var1 ?var2 ?rest WHERE {",
                "?resource dct:subject ?var1 .",
                "{ ?var1 skos:prefLabel ?free2 .",
                "?free2 pf:textMatch '+Dansk' . }",
                "UNION { ?var1 skos:altLabel ?free2 .",
                "?free2 pf:textMatch '+Dansk' . }",
                "UNION { ?var1 skos:hiddenLabel ?free2 .",
                "?free2 pf:textMatch '+Dansk' . }",
                "",
                "?resource dct:audience ?var2 .",
                "?var2 rdfs:label \"Detektor\"@no .",
                "?resource ?p ?rest .",
                "}"});
      //                                     http://rabbit.computas.int:8180/sublima-webapp-1.0-SNAPSHOT/search-result?freetext-field=dct:title&freetext-field=dct:subject/all-labels&freetext-field=dct:description&freetext-field=dct:publisher/foaf:name&prefix=dct:%20%3Chttp://purl.org/dc/terms/%3E&prefix=foaf:%20%3Chttp://xmlns.com/foaf/0.1/%3E&prefix=sub:%20%3Chttp://xmlns.computas.com/sublima%23%3E&prefix=rdfs:%20%3Chttp://www.w3.org/2000/01/rdf-schema%23%3E&prefix=skos:%20%3Chttp://www.w3.org/2004/02/skos/core%23%3E&interface-language=no&dct:subject/all-labels=Dansk&dct:audience/rdfs:label=Detektor
   //  prefix=dct%3A+%3Chttp%3A%2F%2Fpurl.org%2Fdc%2Fterms%2F%3E&prefix=foaf%3A+%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2F%3E&prefix=sub%3A+%3Chttp%3A%2F%2Fxmlns.computas.com%2Fsublima%23%3E&prefix=rdfs%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F2000%2F01%2Frdf-schema%23%3E&prefix=skos%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F2004%2F02%2Fskos%2Fcore%23%3E&dct%3Atitle=&dct%3Asubject%2Fall-labels=Dansk&dct%3Adescription=&dct%3Apublisher%2Ffoaf%3Aname=&dct%3AdateAccepted=&dct%3AdateSubmitted=&dct%3Aformat=&dct%3Aidentifier=&dct%3Alanguage%2Frdfs%3Alabel=&dct%3Aaudience%2Frdfs%3Alabel=&sub%3Acommitter%2Frdfs%3Alabel=
        testMap.put("dct:subject/all-labels", new String[]{"Dansk"});
	    testMap.put("dct:audience/rdfs:label", new String[]{"Detektor"});
	    testMap.put("interface-language", new String[]{"no"}); // this parameter is a magic string
	    myService.addFreetextField("dct:title"); // this parameter is a magic string
        myService.addFreetextField("dct:subject/all-labels"); // this parameter is a magic string
        myService.addPrefix("rdfs: <http://www.w3.org/2000/01/rdf-schema#>");
        String resultString = myService.convertForm2Sparql(testMap);
	    assertEquals("Expected result and actual result not equal", expectS, resultString);
	  }

  public void testConvertForm2SparqlTwoValuesBothFree() {
	    // Single value test
        Form2SparqlService myServicefree = new Form2SparqlService(new String[]{"dct: <http://purl.org/dc/terms/>", "foaf: <http://xmlns.com/foaf/0.1/>"}, new String[]{"dc:title"});
	    testMap.put("dc:title", testString);
	    testMap.put("dc:description", new String[]{"A Very Light Jet Aircraft under construction."});
        myServicefree.addFreetextField("dc:description"); 
	    String resultString = myServicefree.convertForm2Sparql(testMap);
	    assertEquals("Expected result and actual result not equal", expectedPrefix + "PREFIX pf: <http://jena.hpl.hp.com/ARQ/property#>\n" +  "DESCRIBE ?resource ?rest WHERE {\n?resource dc:title ?free1 .\n?free1 pf:textMatch '+Cirrus Personal Jet' .\n?resource dc:description ?free2 .\n?free2 pf:textMatch '+A Very Light Jet Aircraft under construction.' .\n?resource ?p ?rest .\n}", resultString);
	  }

  public void testConvertFor2SPARQLDoubleDual() {
    String expectS = "DESCRIBE ?resource ?var1 ?var2 ?rest WHERE {\n?resource dc:subject ?var2 .\n?var2 rdfs:label \"Jet\" .\n?resource dc:publisher ?var1 .\n?var1 foaf:homepage <http://www.cirrusdesign.com/> .\n?resource ?p ?rest .\n}";
    testMap.put("dc:subject/rdfs:label", new String[]{"Jet"});
    testMap.put("dc:publisher/foaf:homepage", new String[]{"http://www.cirrusdesign.com/"});
    String actual = myService.convertForm2Sparql(testMap);
    assertEquals("Expected result and actual result not equal", expectedPrefix + expectS, actual);
  }

  public void testConvertFor2SPARQLDoubleEmpty() {
    String expectS = "DESCRIBE ?resource ?var1 ?rest WHERE {\n?resource dc:subject ?var1 .\n?var1 rdfs:label \"Jet\" .\n\n?resource ?p ?rest .\n}";
    testMap.put("dc:subject/rdfs:label", new String[]{"Jet"});
    testMap.put("dc:publisher", new String[]{""});
    String actual = myService.convertForm2Sparql(testMap);
    assertEquals("Expected result and actual result not equal", expectedPrefix + expectS, actual);
  }
  public void testConvertForm2SPARULSingleValue() throws IOException {
	 // Single value test
	 String expectS = "INSERT {\n<http://sublima.computas.com/agent/ife> foaf:name \"Institute for Energy Technology\"@en .\n}";
	 testMap.put("foaf:name", new String[]{"Institute for Energy Technology"});
	 testMap.put("interface-language", new String[]{"en"}); // this parameter is a magic string
	 testMap.put("the-resource", new String[]{"http://sublima.computas.com/agent/ife"}); // this parameter is a magic string
	 String resultString = myService.convertForm2Sparul(testMap);
	 assertEquals("Expected result and actual result not equal", expectedPrefix + expectS, resultString);
  }

  public void testConvertForm2SPARULTwoValuesEmpty() throws IOException {
		 String expectS = "INSERT {\n<http://the-jet.com/> dc:title \"Cirrus personlig jetfly\"@no .\n}";
		 testMap.put("dc:description", null);
		 testMap.put("dc:title", new String[]{"Cirrus personlig jetfly"});
		 testMap.put("interface-language", new String[]{"no"}); // this parameter is a magic string
		 testMap.put("the-resource", new String[]{"http://the-jet.com/"}); // this parameter is a magic string
		 String resultString = myService.convertForm2Sparul(testMap);
		 assertEquals("Expected result and actual result not equal", expectedPrefix + expectS, resultString);
 }	

  public void testConvertForm2SPARULTwoValues() throws IOException {
		 String expectS = "INSERT {\n<http://the-jet.com/> dc:description \"Et veldig lett jetfly (VLJ) som er under utarbeidelse.\"@no .\n<http://the-jet.com/> dc:title \"Cirrus personlig jetfly\"@no .\n}";
		 testMap.put("dc:description", new String[]{"Et veldig lett jetfly (VLJ) som er under utarbeidelse."});
		 testMap.put("dc:title", new String[]{"Cirrus personlig jetfly"});
		 testMap.put("interface-language", new String[]{"no"}); // this parameter is a magic string
		 testMap.put("the-resource", new String[]{"http://the-jet.com/"}); // this parameter is a magic string
		 String resultString = myService.convertForm2Sparul(testMap);
		 assertEquals("Expected result and actual result not equal", expectedPrefix + expectS, resultString);
}	

  public void testAppendPrefixes() {
	  myService.addPrefix("rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>");
	  assertEquals("Expected result and actual result not equal",
			  	"PREFIX dct: <http://purl.org/dc/terms/>\nPREFIX foaf: <http://xmlns.com/foaf/0.1/>\nPREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n", myService.getPrefixString());
	  
  }
  
  public void testConvertForm2SPARULEmpty() {
		 testMap.put("foaf:name", new String[]{"Institute for Energy Technology"});
		 testMap.put("interface-language", new String[]{"en"}); // this parameter is a magic string
		 try {
			 String resultString = myService.convertForm2Sparul(testMap);
		 }	catch (IOException tmp) {
			 assertTrue(true);
		 }
  }	 
  
  
}
