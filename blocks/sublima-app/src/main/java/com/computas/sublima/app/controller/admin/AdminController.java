package com.computas.sublima.app.controller.admin;

import com.computas.sublima.app.adhoc.ConvertSublimaResources;
import com.computas.sublima.app.adhoc.ImportData;
import com.computas.sublima.app.service.AdminService;
import com.computas.sublima.query.SparqlDispatcher;
import com.computas.sublima.query.SparulDispatcher;
import com.computas.sublima.query.service.DatabaseService;
import com.hp.hpl.jena.sparql.util.StringUtils;
import org.apache.cocoon.components.flow.apples.AppleRequest;
import org.apache.cocoon.components.flow.apples.AppleResponse;
import org.apache.cocoon.components.flow.apples.StatelessAppleController;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;

/**
 * @author: mha
 * Date: 31.mar.2008
 */
public class AdminController implements StatelessAppleController {

  private SparqlDispatcher sparqlDispatcher;
  private SparulDispatcher sparulDispatcher;
  AdminService adminService = new AdminService();
  private String mode;
  private String submode;
  String[] completePrefixArray = {
          "PREFIX dct: <http://purl.org/dc/terms/>",
          "PREFIX foaf: <http://xmlns.com/foaf/0.1/>",
          "PREFIX sub: <http://xmlns.computas.com/sublima#>",
          "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
          "PREFIX wdr: <http://www.w3.org/2007/05/powder#>",
          "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>",
          "PREFIX lingvoj: <http://www.lingvoj.org/ontology#>"};

  String completePrefixes = StringUtils.join("\n", completePrefixArray);
  String[] prefixArray = {
          "dct: <http://purl.org/dc/terms/>",
          "foaf: <http://xmlns.com/foaf/0.1/>",
          "sub: <http://xmlns.computas.com/sublima#>",
          "rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
          "wdr: <http://www.w3.org/2007/05/powder#>",
          "skos: <http://www.w3.org/2004/02/skos/core#>",
          "PREFIX lingvoj: <http://www.lingvoj.org/ontology#>"};
  String prefixes = StringUtils.join("\n", prefixArray);

  private static Logger logger = Logger.getLogger(AdminController.class);
  ConvertSublimaResources convert = new ConvertSublimaResources();

  @SuppressWarnings("unchecked")
  public void process(AppleRequest req, AppleResponse res) throws Exception {

    this.mode = req.getSitemapParameter("mode");
    this.submode = req.getSitemapParameter("submode");

    if ("".equalsIgnoreCase(mode)) {
      res.sendPage("xml2/admin", null);
    } else if ("testsparql".equalsIgnoreCase(mode)) {
      if ("".equalsIgnoreCase(submode)) {
        res.sendPage("xhtml/testsparql", null);
      } else {
        String query = req.getCocoonRequest().getParameter("query");
        res.redirectTo(req.getCocoonRequest().getContextPath() + "/sparql?query=" + URLEncoder.encode(query, "UTF-8"));
      }
    } else if ("database".equalsIgnoreCase(mode)) {
      if ("".equalsIgnoreCase(submode)) {
        uploadForm(res, req);
      } else if ("upload".equalsIgnoreCase(submode)) {
        uploadForm(res, req);
      } else if ("export".equalsIgnoreCase(submode)) {
        exportOntology(res, req);
      }
    } else {
      res.sendStatus(404);
      return;
    }
  }

  private void exportOntology(AppleResponse res, AppleRequest req) {
    if (req.getCocoonRequest().getMethod().equalsIgnoreCase("GET")) {
      res.sendPage("xml2/upload", null);
    } else if (req.getCocoonRequest().getMethod().equalsIgnoreCase("POST")) {
      DatabaseService databaseService = new DatabaseService();

      String type = req.getCocoonRequest().getParameter("type");
      File file = new File(req.getCocoonRequest().getParameter("location"));

      String replaceResourceWith = null;
      if (req.getCocoonRequest().getParameterValues("replacement") != null) {
        replaceResourceWith = "uri";
      }
      databaseService.writeModelToFile(file.toString(), type);
      try {
        ConvertSublimaResources.convert(file.toURL().toString(), type, file.getCanonicalPath(), type, replaceResourceWith);
      } catch (IOException e) {
        logger.trace("AdminController.uploadForm --> Error during convertion of resource URIs to URLs.");
        e.printStackTrace();
      }


      res.sendPage("xml2/upload", null);
    }
  }

  private void uploadForm(AppleResponse res, AppleRequest req) {
    if (req.getCocoonRequest().getMethod().equalsIgnoreCase("GET")) {
      res.sendPage("xml2/upload", null);
    } else if (req.getCocoonRequest().getMethod().equalsIgnoreCase("POST")) {

      String type = req.getCocoonRequest().getParameter("type");
      File file = new File(req.getCocoonRequest().getParameter("location"));

      try {
        ConvertSublimaResources.convert(file.toURL().toString(), type, file.getCanonicalPath(), type, "url");
        ImportData.load(file.toURL().toString(), type);
      } catch (MalformedURLException e) {
        logger.trace("AdminController.uploadForm --> Error during convertion of resource URIs to URLs.");
        e.printStackTrace();
      } catch (IOException e) {
        logger.trace("AdminController.uploadForm --> Error during convertion of resource URIs to URLs.");
        e.printStackTrace();
      }

      res.sendPage("xml2/upload", null);
    }
  }

  public void setSparqlDispatcher
          (SparqlDispatcher
                  sparqlDispatcher) {
    this.sparqlDispatcher = sparqlDispatcher;
  }

  public void setSparulDispatcher
          (SparulDispatcher
                  sparulDispatcher) {
    this.sparulDispatcher = sparulDispatcher;
  }
}

