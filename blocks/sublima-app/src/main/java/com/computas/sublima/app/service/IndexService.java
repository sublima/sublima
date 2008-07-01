package com.computas.sublima.app.service;

import com.computas.sublima.query.impl.DefaultSparulDispatcher;
import com.computas.sublima.query.service.DatabaseService;
import com.computas.sublima.query.service.SearchService;
import com.computas.sublima.query.service.SettingsService;
import com.computas.sublima.app.service.Form2SparqlService;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.ModelRDB;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.larq.IndexBuilderString;
import com.hp.hpl.jena.query.larq.IndexLARQ;
import com.hp.hpl.jena.query.larq.LARQ;
import com.hp.hpl.jena.shared.DoesNotExistException;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.sparql.util.StringUtils;
import org.apache.log4j.Logger;
import org.postgresql.util.PSQLException;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.sql.SQLException;
import java.util.*;

/**
 * A class to support Lucene/LARQ indexing in the web app
 * Has methods for creating and accessing indexes
 *
 * @author: mha
 * Date: 13.mar.2008
 */
public class IndexService {

  private static Logger logger = Logger.getLogger(IndexService.class);
  private DefaultSparulDispatcher sparulDispatcher;
  private SearchService searchService = new SearchService();
  
  /**
   * Method to create an index based on the internal content
   */
  public void createInternalResourcesMemoryIndex() {

    DatabaseService myDbService = new DatabaseService();
    IDBConnection connection = myDbService.getConnection();
    ResultSet resultSet;
    IndexBuilderString larqBuilder;

    logger.info("SUBLIMA: createInternalResourcesMemoryIndex() --> Indexing - Created database connection " + connection.getDatabaseType());

    // -- Read and index all literal strings.
    File indexDir = new File(SettingsService.getProperty("sublima.index.directory"));
    logger.info("SUBLIMA: createInternalResourcesMemoryIndex() --> Indexing - Read and index all literal strings");
    if ("memory".equals(SettingsService.getProperty("sublima.index.type"))) {
      larqBuilder = new IndexBuilderString();
    } else {
      larqBuilder = new IndexBuilderString(indexDir);
    }

    //IndexBuilderSubject larqBuilder = new IndexBuilderSubject();

    //Create a model based on the one in the DB
    try {
      ModelRDB model = ModelRDB.open(connection);
      // -- Create an index based on existing statements
      larqBuilder.indexStatements(model.listStatements());

      logger.info("SUBLIMA: createInternalResourcesMemoryIndex() --> Indexing - Indexed all model statements");
      // -- Finish indexing
      larqBuilder.closeForWriting();
      logger.info("SUBLIMA: createInternalResourcesMemoryIndex() --> Indexing - Closed index for writing");
      // -- Create the access index
      IndexLARQ index = larqBuilder.getIndex();
      model.close();

      // -- Make globally available
      LARQ.setDefaultIndex(index);
      logger.info("SUBLIMA: createInternalResourcesMemoryIndex() --> Indexing - Index now globally available");
    }
    catch (DoesNotExistException e) {
      logger.info("SUBLIMA: createInternalResourcesMemoryIndex() --> Indexing - NO CONTENT IN DATABASE. Please fill DB from Admin/Database and restart Tomcat.");
    }

    logger.info("SUBLIMA: createInternalResourcesMemoryIndex() --> Indexing - Created RDF model from database");
    try {
      connection.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }


  /**
   * Method to extract all URLs of the resources in the model
   *
   * @return ResultSet containing all resource URLS from the model
   */
  // todo Use SparqlDispatcher (needs to return ResultSet)
  private ResultSet getAllExternalResourcesURLs() {
    DatabaseService myDbService = new DatabaseService();
    IDBConnection connection = myDbService.getConnection();
    ModelRDB model = ModelRDB.open(connection);

    String queryString = StringUtils.join("\n", new String[]{
            "PREFIX dct: <http://purl.org/dc/terms/>",
            "SELECT ?url",
            "WHERE {",
            "        ?url dct:title ?title }"});

    Query query = QueryFactory.create(queryString);
    QueryExecution qExec = QueryExecutionFactory.create(query, model);
    ResultSet resultSet = qExec.execSelect();
    //model.close();

    try {
      connection.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    logger.info("SUBLIMA: getAllExternalResourcesURLs() --> Indexing - Fetched all resource URLs from the model");
    return resultSet;
  }

  /**
   * A method to validate all urls on the resources. Adds the URL to the list along with
   * the http code.
   *
   * @return A map containing the URL and its HTTP Code. In case of exceptions a String
   *         representation of the exception is used.
   */
  public void validateURLs() {
    ResultSet resultSet;
    resultSet = getAllExternalResourcesURLs();
    HashMap<String, HashMap<String, String>> urlCodeMap = new HashMap<String, HashMap<String, String>>();

    // For each URL, do a HTTP GET and check the HTTP code
    URL u = null;
    HashMap<String, String> result;

    while (resultSet.hasNext()) {
      String resultURL = resultSet.next().toString();
      String url = resultURL.substring(10, resultURL.length() - 3).trim();

      URLActions urlAction = new URLActions(url);
        try {
            urlAction.updateResourceStatus();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        catch (PSQLException e) {
            logger.warn("SUBLIMA: validateURLs() --> Indexing - Could not index " + url
                    + " due to database malfunction, probably caused by invalid characters in resource.");
            e.printStackTrace();
        }
        catch (JenaException e) {
            logger.warn("SUBLIMA: validateURLs() --> Indexing - Could not index " + url
                    + " due to backend storage malfunction, probably caused by invalid characters in resource.");
            e.printStackTrace();
        }
    }
  }

  public String getQueryForIndex(String[] fieldsToIndex, String[] prefixes) {
	  Form2SparqlService form2SparqlService = new Form2SparqlService(prefixes);
	  return getTheIndexQuery(fieldsToIndex, form2SparqlService);
  }
 
  public String getQueryForIndex(String[] fieldsToIndex, String[] prefixes, String resource) {
	  Form2SparqlService form2SparqlService = new Form2SparqlService(prefixes);
	  form2SparqlService.setResourceSubject(resource);
	  return getTheIndexQuery(fieldsToIndex, form2SparqlService);
  }
  
  private String getTheIndexQuery(String[] fieldsToIndex, Form2SparqlService form2SparqlService) {
	  StringBuffer queryBuffer = new StringBuffer();
	  queryBuffer.append(form2SparqlService.getPrefixString());
	  queryBuffer.append("SELECT");
	  if (form2SparqlService.getResourceSubject().equals("?resource")) {
		  queryBuffer.append(" ?resource");  
 	  }
	  for (int i=1;i<=fieldsToIndex.length;i++) {
		  queryBuffer.append(" ?object");
		  queryBuffer.append(i);
	  }
	  queryBuffer.append(" WHERE {");
	  ArrayList nullValues = new ArrayList<String>();
	  for (String field : fieldsToIndex) {
		  nullValues.add(null);
		  queryBuffer.append(form2SparqlService.convertFormField2N3(field, 
				  (String[]) nullValues.toArray(new String [nullValues.size ()]) // Don't ask me why
				  ));
	  }
	  queryBuffer.append("\n}");
      if (form2SparqlService.getResourceSubject().equals("?resource")) {
		  queryBuffer.append("\nGROUP BY ?resource\n");  
 	  }

      return queryBuffer.toString();
  }
  
  
  public String getFreetextToIndex(String[] fieldsToIndex, String[] prefixes, String resource) {
	  String queryString = getQueryForIndex(fieldsToIndex, prefixes, resource);
	  return getTheFreetextToIndex(queryString, resource);
  }
  public String getFreetextToIndex(String[] fieldsToIndex, String[] prefixes) {
	  String queryString = getQueryForIndex(fieldsToIndex, prefixes);
	  return getTheFreetextToIndex(queryString, "resource");  
  }
  
  private String getTheFreetextToIndex(String queryString, String resourceOrVarName) {
	  DatabaseService myDbService = new DatabaseService();
	  IDBConnection connection = myDbService.getConnection();
	  ModelRDB model = ModelRDB.open(connection);
	  
	  Query query = QueryFactory.create(queryString);
	  QueryExecution qExec = QueryExecutionFactory.create(query, model);
	  ResultSet resultSet = qExec.execSelect();
	  //model.close();

	  try {
		  connection.close();
	  } catch (SQLException e) {
		  e.printStackTrace();
	  }

	  logger.info("SUBLIMA: getFreetextToIndex() --> Indexing - Fetched all literals that we need to index");
	  StringBuffer resultBuffer = new StringBuffer();
      Set literals = new HashSet<String>();
      while (resultSet.hasNext()) {
          QuerySolution soln = resultSet.nextSolution();
          String subprop = resourceOrVarName + " sub:literals \"\"\"";
          Iterator<String> it = soln.varNames();
          while (it.hasNext()) {
              String var = it.next();
              if (soln.get(var).isResource()) {
                  // This means two things: 1) Right now, we have the a URI of a Resource, which stands out from the
                  //                           literals we need to index
                  //                        2) We are retrieving multiple Resources, if there is a single Resource,
                  //                           the URI will not be in the data returned.
                  Resource r = soln.getResource(var);
                  subprop = "<" + r.getURI() + "> sub:literals \"\"\"";
                  resultBuffer.append("<" + r.getURI() + "> sub:literals \"\"\"");
              } else if (soln.get(var).isLiteral()) {
                  Literal l = soln.getLiteral(var);
                  String literal = l.getString().replace("\\","\\\\");
                  resultBuffer.append(literal);
                  resultBuffer.append("\n");
              } else {
                  logger.warn("SUBLIMA: Indexing - variable " + var + " contained neither the resource name or a literal. Verify that sublima.searchfields config is correct.");
              }
          }

          resultBuffer.append("\"\"\" .\n");

      }
	  return resultBuffer.toString();
	}
}
