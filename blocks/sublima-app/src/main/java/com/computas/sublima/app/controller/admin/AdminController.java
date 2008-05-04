package com.computas.sublima.app.controller.admin;

import com.computas.sublima.app.adhoc.ImportData;
import com.computas.sublima.query.SparqlDispatcher;
import com.computas.sublima.query.SparulDispatcher;
import com.computas.sublima.query.service.AdminService;
import static com.computas.sublima.query.service.SettingsService.*;
import com.hp.hpl.jena.sparql.util.StringUtils;
import org.apache.cocoon.components.flow.apples.AppleRequest;
import org.apache.cocoon.components.flow.apples.AppleResponse;
import org.apache.cocoon.components.flow.apples.StatelessAppleController;
import org.apache.cocoon.environment.Request;
import org.apache.log4j.Logger;

import java.net.URLEncoder;
import java.util.*;

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
            uploadForm(res, req);
        } else {
            res.sendStatus(404);
            return;
        }
    }

    private void uploadForm(AppleResponse res, AppleRequest req) {
        if (req.getCocoonRequest().getMethod().equalsIgnoreCase("GET")) {
            res.sendPage("xml2/upload", null);
        } else if (req.getCocoonRequest().getMethod().equalsIgnoreCase("POST")) {
            String location = req.getCocoonRequest().getParameter("location");
            String type = req.getCocoonRequest().getParameter("type");

            ImportData importData = new ImportData();
            importData.load(location, type);
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

