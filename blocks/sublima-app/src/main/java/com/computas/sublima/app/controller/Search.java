package com.computas.sublima.app.controller;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.cocoon.components.flow.apples.AppleRequest;
import org.apache.cocoon.components.flow.apples.AppleResponse;
import org.apache.cocoon.components.flow.apples.StatelessAppleController;
import org.apache.cocoon.environment.Request;

import com.computas.sublima.app.Form2SparqlService;
import com.computas.sublima.app.filter.AuthenticationFilter;
import com.computas.sublima.query.SparqlDispatcher;

import org.apache.log4j.Logger;

public class Search implements StatelessAppleController {

	private SparqlDispatcher sparqlDispatcher;

    	private static Logger logger = Logger.getLogger(Search.class);

	@SuppressWarnings("unchecked")
	public void process(AppleRequest req, AppleResponse res) throws Exception {
		// the initial search page
		String mode = req.getSitemapParameter("mode");
		if ("search".equalsIgnoreCase(mode)) {
			res.sendPage("xhtml/search-form", null);
			return;
		}
		// Get all parameteres from the HTML form as Map
		Map<String, String[]> parameterMap = new TreeMap<String, String[]>(
				createParametersMap(req.getCocoonRequest()));
		
		if ("topic-instance".equalsIgnoreCase(mode) || "resource".equalsIgnoreCase(mode)) {
			parameterMap.put("prefix", new String[]{"dct: <http://purl.org/dc/terms/>","rdfs: <http://www.w3.org/2000/01/rdf-schema#>"});
			parameterMap.put("interface-language", new String[]{req.getSitemapParameter("interface-language")});
		}
		
		if ("topic-instance".equalsIgnoreCase(mode)) {
			parameterMap.put("dct:subject/rdfs:label", new String[]{req.getSitemapParameter("topic")});
		}
		
		if ("resource".equalsIgnoreCase(mode)) {
			parameterMap.put("dct:identifier", new String[]{"http://sublima.computas.com/resource/"
															+req.getSitemapParameter("name")});
			parameterMap.put("dct:subject/rdfs:label", new String[]{""});
		}

		
		// sending the result



		String sparqlQuery = null;
		// Check for magic prefixes
		if (parameterMap.get("prefix") != null) {
			// Calls the Form2SPARQL service with the parameterMap which returns
			// a SPARQL as String
			Form2SparqlService form2SparqlService = new Form2SparqlService(
					parameterMap.get("prefix"));
			parameterMap.remove("prefix"); // The prefixes are magic variables
			sparqlQuery = form2SparqlService.convertForm2Sparql(parameterMap);
		} else {
			res.sendStatus(400);
		}
		

		// FIXME hard-wire the query for testing!!!
		// sparqlQuery = "DESCRIBE <http://the-jet.com/>";
		
		logger.trace("SPARQL query sent to dispatcher: " + sparqlQuery);
		Object queryResult = sparqlDispatcher.query(sparqlQuery);
		Map<String, Object> bizData = new HashMap<String, Object>();
		bizData.put("result-list", queryResult);
		bizData.put("configuration", new Object());
		res.sendPage("xml/sparql-result", bizData);
	}

	public void setSparqlDispatcher(SparqlDispatcher sparqlDispatcher) {
		this.sparqlDispatcher = sparqlDispatcher;
	}

	private Map<String, String[]> createParametersMap(Request request) {
		Map<String, String[]> result = new HashMap<String, String[]>();
		Enumeration parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String paramName = (String) parameterNames.nextElement();
			result.put(paramName, request.getParameterValues(paramName));
		}
		return result;
	}

}
