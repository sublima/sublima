package com.computas.sublima.app.controller;

import com.computas.sublima.query.SparqlDispatcher;
import com.computas.sublima.query.SparulDispatcher;
import com.hp.hpl.jena.sparql.util.StringUtils;
import org.apache.cocoon.components.flow.apples.AppleRequest;
import org.apache.cocoon.components.flow.apples.AppleResponse;
import org.apache.cocoon.components.flow.apples.StatelessAppleController;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: mha
 * Date: 31.mar.2008
 */
public class AdminController implements StatelessAppleController {

  private SparqlDispatcher sparqlDispatcher;
  private SparulDispatcher sparulDispatcher;
  private String mode;
  private String submode;

  private static Logger logger = Logger.getLogger(AdminController.class);

  @SuppressWarnings("unchecked")
  public void process(AppleRequest req, AppleResponse res) throws Exception {

    this.mode = req.getSitemapParameter("mode");
    this.submode = req.getSitemapParameter("submode");

    // Linkcheck. Send the user to a page that displays a list of all resources affected.
    if ("lenkesjekk".equalsIgnoreCase(mode)) {
      showLinkcheckResults(res, req);
      return;
    }
    // Publishers. Send the user to a page that displays a list of all publishers.
    if ("utgivere".equalsIgnoreCase(mode)) {
      if ("".equalsIgnoreCase(submode)) {
        showPublishersIndex(res, req);
      } else {
        showPublisherByURI(res, req);
      }

      return;
    }


    if ("ressurser".equalsIgnoreCase(mode)) {
      if ("".equalsIgnoreCase(submode)) {
        showResourcesIndex(res, req);
      } else if ("foreslaatte".equalsIgnoreCase(submode)) {
        showSuggestedResources(res, req);
      }

      return;
    } else {
      res.sendStatus(404);
      return;
    }
  }

  /**
   * Method to create the individual publisher page based on the publisher name. 
   * The page presents the publisher and all resources from that publisher.
   *
   * @param res - AppleResponse
   * @param req - AppleRequest
   */

  private void showPublisherByURI(AppleResponse res, AppleRequest req) {
    String publisheruri = this.submode;

    //Find the publisher URI based on name
    String findPublisherByURIQuery = StringUtils.join("\n", new String[]{
      "PREFIX foaf: <http://xmlns.com/foaf/0.1/>",
      "PREFIX sub: <http://xmlns.computas.com/sublima#>",
      "DESCRIBE ?resource " + publisheruri, // + " ?rest",
      "WHERE {",
      "?resource dct:publisher " + publisheruri + " .",
      //"?resource dct:su ?rest .",
      "}"});


      logger.trace("AdminController.showPublisherByName() --> SPARQL query sent to dispatcher: \n" + findPublisherByURIQuery);
      Object queryResult = sparqlDispatcher.query(findPublisherByURIQuery);

      Map<String, Object> bizData = new HashMap<String, Object>();
      bizData.put("publisherlist", queryResult);
      res.sendPage("xml2/utgivere", bizData);
  }

  /**
   * Method to display a list of all publishers. These link to a page where each publisher can edited.
   *
   * @param res - AppleResponse
   * @param req - AppleRequest
   */
  private void showPublishersIndex(AppleResponse res, AppleRequest req) {
    String queryString = StringUtils.join("\n", new String[]{
            "PREFIX dct: <http://purl.org/dc/terms/>",
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/>",
            "SELECT DISTINCT ?publisher ?name",
            "WHERE {",
            "?resource dct:publisher ?publisher .",
            "?publisher foaf:name ?name .",
            "}",
            "ORDER BY ?name"});

    logger.trace("AdminController.showPublishersIndex() --> SPARQL query sent to dispatcher: \n" + queryString);
    Object queryResult = sparqlDispatcher.query(queryString);

    Map<String, Object> bizData = new HashMap<String, Object>();
    bizData.put("publisherlist", queryResult);
    res.sendPage("xml2/utgivere", bizData);
  }

  /**
   * Method to displaty a list of all resources suggested by users
   *
   * @param res - AppleResponse
   * @param req - AppleRequest
   */
  private void showSuggestedResources(AppleResponse res, AppleRequest req) {
    String queryString = StringUtils.join("\n", new String[]{
            "PREFIX dct: <http://purl.org/dc/terms/>",
            "PREFIX sub: <http://xmlns.computas.com/sublima#>",
            "PREFIX wdr: <http://www.w3.org/2007/05/powder#>",
            "CONSTRUCT {",
            "    ?resource dct:title ?title ;" +
                    //"              dct:identifier ?identifier ;" +
                    "              a sub:Resource . }",
            "    WHERE {",
            "        ?resource wdr:describedBy <http://sublima.computas.com/status/til_godkjenning> ;",
            "                  dct:title ?title .",
            //"                  dct:identifier ?identifier .",
            "}"});

    logger.trace("AdminController.showSuggestedResources() --> SPARQL query sent to dispatcher: \n" + queryString);
    Object queryResult = sparqlDispatcher.query(queryString);

    Map<String, Object> bizData = new HashMap<String, Object>();
    bizData.put("suggestedresourceslist", queryResult);
    res.sendPage("xml2/foreslaatte", bizData);
  }

  /**
   * Method to display the initial page for administrating resources
   *
   * @param res - AppleResponse
   * @param req - AppleRequest
   */
  private void showResourcesIndex(AppleResponse res, AppleRequest req) {
    res.sendPage("xml2/ressurser", null);
  }

  /**
   * Method that displays a list of all resources tagged to be checked
   *
   * @param res - AppleResponse
   * @param req - AppleRequest
   */
  private void showLinkcheckResults(AppleResponse res, AppleRequest req) {

    // CHECK
    String queryStringCHECK = StringUtils.join("\n", new String[]{
            "PREFIX dct: <http://purl.org/dc/terms/>",
            "PREFIX sub: <http://xmlns.computas.com/sublima#>",
            "CONSTRUCT {",
            "    ?resource dct:title ?title ;" +
                    "              dct:identifier ?identifier ;" +
                    "              a sub:Resource . }",
            "    WHERE {",
            "        ?resource sub:status <http://sublima.computas.com/status/CHECK> ;",
            "                  dct:title ?title ;",
            "                  dct:identifier ?identifier .",
            "}"});

    logger.trace("AdminController.showLinkcheckResults() --> SPARQL query sent to dispatcher: \n" + queryStringCHECK);
    Object queryResultCHECK = sparqlDispatcher.query(queryStringCHECK);

    // INACTIVE
    String queryStringINACTIVE = StringUtils.join("\n", new String[]{
            "PREFIX dct: <http://purl.org/dc/terms/>",
            "PREFIX sub: <http://xmlns.computas.com/sublima#>",
            "CONSTRUCT {",
            "    ?resource dct:title ?title ;" +
                    "              dct:identifier ?identifier ;" +
                    "              a sub:Resource . }",
            "    WHERE {",
            "        ?resource sub:status <http://sublima.computas.com/status/IANCTIVE> ;",
            "                  dct:title ?title ;",
            "                  dct:identifier ?identifier .",
            "}"});

    logger.trace("AdminController.showLinkcheckResults() --> SPARQL query sent to dispatcher: \n" + queryStringINACTIVE);
    Object queryResultINACTIVE = sparqlDispatcher.query(queryStringINACTIVE);

    // GONE
    String queryStringGONE = StringUtils.join("\n", new String[]{
            "PREFIX dct: <http://purl.org/dc/terms/>",
            "PREFIX sub: <http://xmlns.computas.com/sublima#>",
            "CONSTRUCT {",
            "    ?resource dct:title ?title ;" +
                    "              dct:identifier ?identifier ;" +
                    "              a sub:Resource . }",
            "    WHERE {",
            "        ?resource sub:status <http://sublima.computas.com/status/GONE> ;",
            "                  dct:title ?title ;",
            "                  dct:identifier ?identifier .",
            "}"});

    logger.trace("AdminController.showLinkcheckResults() --> SPARQL query sent to dispatcher: \n" + queryStringGONE);
    Object queryResultGONE = sparqlDispatcher.query(queryStringGONE);

    Map<String, Object> bizData = new HashMap<String, Object>();
    bizData.put("lenkesjekklist_check", queryResultCHECK);
    bizData.put("lenkesjekklist_inactive", queryResultINACTIVE);
    bizData.put("lenkesjekklist_gone", queryResultGONE);
    res.sendPage("xml2/lenkesjekk", bizData);

  }

  public void setSparqlDispatcher(SparqlDispatcher sparqlDispatcher) {
    this.sparqlDispatcher = sparqlDispatcher;
  }

  public void setSparulDispatcher(SparulDispatcher sparulDispatcher) {
    this.sparulDispatcher = sparulDispatcher;
  }
}

