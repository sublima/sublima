package com.computas.sublima.app.controller.admin;

import com.computas.sublima.app.service.AdminService;
import com.computas.sublima.app.service.Form2SparqlService;
import com.computas.sublima.app.service.IndexService;
import com.computas.sublima.query.SparqlDispatcher;
import com.computas.sublima.query.SparulDispatcher;
import com.computas.sublima.query.service.SettingsService;
import com.computas.sublima.query.service.SearchService;
import static com.computas.sublima.query.service.SettingsService.getProperty;
import com.hp.hpl.jena.query.larq.LARQ;
import com.hp.hpl.jena.sparql.util.StringUtils;
import org.apache.cocoon.auth.ApplicationManager;
import org.apache.cocoon.auth.ApplicationUtil;
import org.apache.cocoon.auth.User;
import org.apache.cocoon.components.flow.apples.AppleRequest;
import org.apache.cocoon.components.flow.apples.AppleResponse;
import org.apache.cocoon.components.flow.apples.StatelessAppleController;
import org.apache.cocoon.environment.Request;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author: mha
 * Date: 31.mar.2008
 */
public class ResourceController implements StatelessAppleController {

  private SparqlDispatcher sparqlDispatcher;
  private SparulDispatcher sparulDispatcher;
  AdminService adminService = new AdminService();
  private ApplicationManager appMan;
  private ApplicationUtil appUtil = new ApplicationUtil();
  private IndexService indexService = new IndexService();
  private SearchService searchService = new SearchService();
  private User user;
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

  private String userPrivileges = "<empty/>";

  private static Logger logger = Logger.getLogger(AdminController.class);

  @SuppressWarnings("unchecked")
  public void process(AppleRequest req, AppleResponse res) throws Exception {

    this.mode = req.getSitemapParameter("mode");
    this.submode = req.getSitemapParameter("submode");

    if (appUtil.getUser() != null) {
      user = appUtil.getUser();
      userPrivileges = adminService.getRolePrivilegesAsXML(user.getAttribute("role").toString());
    }

    if ("ressurser".equalsIgnoreCase(mode)) {
      if ("".equalsIgnoreCase(submode) || submode == null) {
        showResourcesIndex(res, req);
      } else if ("masse".equalsIgnoreCase(submode)) {
        massEditResource(res, req);
      } else if ("ny".equalsIgnoreCase(submode)) {
        editResource(res, req, "ny", null);
      } else if ("edit".equalsIgnoreCase(submode)) {
        editResource(res, req, "edit", null);
      } else if ("checkurl".equalsIgnoreCase(submode)) {
        registerNewResourceURL(req, res);
      }
    } else {
      res.sendStatus(404);
    }
  }

  /**
   * Method to do the first step in the registration process for new resources.
   * This method checks the given URL and forwards the user to the resource form if valid.
   * Otherwise an error message is displayed.
   *
   * @param req
   * @param res
   */
  private void registerNewResourceURL(AppleRequest req, AppleResponse res) {
    Map<String, Object> bizData = new HashMap<String, Object>();
    StringBuffer messageBuffer = new StringBuffer();
    messageBuffer.append("<c:messages xmlns:c=\"http://xmlns.computas.com/cocoon\" xmlns:i18n=\"http://apache.org/cocoon/i18n/2.1\">\n");
    bizData.put("userprivileges", userPrivileges);

    String tempPrefixes = "<c:tempvalues \n" +
            "xmlns:topic=\"" + getProperty("sublima.base.url") + "topic/\"\n" +
            "xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\"\n" +
            "xmlns:wdr=\"http://www.w3.org/2007/05/powder#\"\n" +
            "xmlns:lingvoj=\"http://www.lingvoj.org/ontology#\"\n" +
            "xmlns:sioc=\"http://rdfs.org/sioc/ns#\"\n" +
            "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
            "xmlns:foaf=\"http://xmlns.com/foaf/0.1/\"\n" +
            "xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n" +
            "xmlns:dct=\"http://purl.org/dc/terms/\"\n" +
            "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n" +
            "xmlns:dcmitype=\"http://purl.org/dc/dcmitype/\"\n" +
            "xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n" +
            "xmlns:c=\"http://xmlns.computas.com/cocoon\"\n" +
            "xmlns:sub=\"http://xmlns.computas.com/sublima#\">\n";

    if (req.getCocoonRequest().getMethod().equalsIgnoreCase("GET")) {

      bizData.put("messages", "<empty/>");
      bizData.put("tempvalues", "<empty/>");
      bizData.put("facets", adminService.getMostOfTheRequestXMLWithPrefix(req) + "</c:request>");
      res.sendPage("xml2/ressurs-prereg", bizData);

    } else if (req.getCocoonRequest().getMethod().equalsIgnoreCase("POST")) {
      String url = req.getCocoonRequest().getParameter("sub:url");
      bizData.put("tempvalues", tempPrefixes + "<sub:url>" + req.getCocoonRequest().getParameter("sub:url") + "</sub:url></c:tempvalues>\n");
      bizData.put("facets", adminService.getMostOfTheRequestXMLWithPrefix(req) + "</c:request>");

      if (!"".equalsIgnoreCase(url)) {
        if (adminService.validateURL(url)) {
          if (adminService.checkForDuplicatesByURI(url)) {
            messageBuffer.append("<c:message><i18n:text key=\"validation.urlexists\" xmlns:i18n=\"http://apache.org/cocoon/i18n/2.1\"/></c:message>\n");
            messageBuffer.append("</c:messages>\n");
            bizData.put("messages", messageBuffer.toString());
            res.sendPage("xml2/ressurs-prereg", bizData);

          } else { // The URL is okay
            messageBuffer.append("<c:message><i18n:text key=\"validation.providedurlok\" xmlns:i18n=\"http://apache.org/cocoon/i18n/2.1\"/></c:message>");
            messageBuffer.append("</c:messages>\n");

            bizData.put("topics", adminService.getAllTopics());
            bizData.put("languages", adminService.getAllLanguages());
            bizData.put("mediatypes", adminService.getAllMediaTypes());
            bizData.put("audience", adminService.getAllAudiences());
            bizData.put("status", adminService.getAllStatuses());
            bizData.put("publishers", adminService.getAllPublishers());
            bizData.put("userprivileges", userPrivileges);
            bizData.put("mode", "edit");
            bizData.put("messages", messageBuffer.toString());
            bizData.put("resource", "<rdf:RDF\n" +
                    "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
                    "    xmlns:sub=\"http://xmlns.computas.com/sublima#\">\n" +
                    "  <sub:Resource>\n" +
                    "    <sub:url rdf:resource=\"" + url + "\"/>\n" +
                    "  </sub:Resource>\n" +
                    "</rdf:RDF>");
            res.sendPage("xml2/ressurs", bizData);
          }
        } else {
          messageBuffer.append("<c:message>Oppgitt URL gir en ugyldig statuskode. Vennligst kontroller URL i en nettleser.</c:message>\n");
          messageBuffer.append("</c:messages>\n");
          bizData.put("messages", messageBuffer.toString());
          res.sendPage("xml2/ressurs-prereg", bizData);
        }

      } else {
        messageBuffer.append("<c:message>URL kan ikke være blank.</c:message>\n");
        messageBuffer.append("</c:messages>\n");
        bizData.put("messages", messageBuffer.toString());
        res.sendPage("xml2/ressurs-prereg", bizData);
      }
    }
  }


  /**
   * Method to add new resources and edit existing ones
   * Sparql queries for all topics, statuses, languages, media types and audience
   * is done and the results forwarded to the JX Template and XSLT.
   * <p/>
   * A query for the resource is done when the action is "edit". In case of "new" a blank
   * form is presented.
   *
   * @param res      - AppleResponse
   * @param req      - AppleRequest
   * @param type     - String "new" or "edit"
   * @param messages
   */
  private void editResource
          (AppleResponse
                  res, AppleRequest
                  req, String
                  type, String
                  messages) {

    boolean validated = true;
    boolean insertSuccess = false;
    boolean updateDate = false;

    String dctPublisher;
    String dctIdentifier;
    String dateAccepted;
    String committer;

    // Get all list values
    String allTopics = adminService.getAllTopics();
    String allLanguages = adminService.getAllLanguages();
    String allMediatypes = adminService.getAllMediaTypes();
    String allAudiences = adminService.getAllAudiences();
    String allStatuses = adminService.getAllStatuses();

    StringBuffer messageBuffer = new StringBuffer();
    messageBuffer.append("<c:messages xmlns:c=\"http://xmlns.computas.com/cocoon\" xmlns:i18n=\"http://apache.org/cocoon/i18n/2.1\">\n");
    messageBuffer.append(messages);

    Map<String, Object> bizData = new HashMap<String, Object>();
    bizData.put("topics", allTopics);
    bizData.put("languages", allLanguages);
    bizData.put("mediatypes", allMediatypes);
    bizData.put("audience", allAudiences);
    bizData.put("status", allStatuses);
    bizData.put("userprivileges", userPrivileges);

    // When GET present a blank form with listvalues or prefilled with resource
    if (req.getCocoonRequest().getMethod().equalsIgnoreCase("GET")) {
      bizData.put("tempvalues", "<empty></empty>");

      if ("ny".equalsIgnoreCase(type)) {
        registerNewResourceURL(req, res);
      } else {
        bizData.put("resource", adminService.getResourceByURI(req.getCocoonRequest().getParameter("uri")));
        bizData.put("publishers", adminService.getAllPublishers());
        bizData.put("mode", "edit");
        bizData.put("messages", "<empty></empty>");
        bizData.put("facets", adminService.getMostOfTheRequestXMLWithPrefix(req) + "</c:request>");
        res.sendPage("xml2/ressurs", bizData);
      }

      // When POST try to save the resource. Return error messages upon failure, and success message upon great success
    } else if (req.getCocoonRequest().getMethod().equalsIgnoreCase("POST")) {

      if (req.getCocoonRequest().getParameter("actionbuttondelete") != null) {

        String deleteString = "DELETE {\n" +
                "<" + req.getCocoonRequest().getParameter("the-resource") + "> ?a ?o.\n" +
                "} WHERE {\n" +
                "<" + req.getCocoonRequest().getParameter("the-resource") + "> ?a ?o. }";

        boolean deleteResourceSuccess = sparulDispatcher.query(deleteString);

        logger.trace("ResourceController.editResource --> DELETE RESOURCE QUERY:\n" + deleteString);
        logger.trace("ResourceController.editResource --> DELETE RESOURCE QUERY RESULT: " + deleteResourceSuccess);


        if (deleteResourceSuccess) {
          messageBuffer.append("<c:message>Ressursen slettet!</c:message>\n");
        } else {
          messageBuffer.append("<c:message>Feil ved sletting av ressurs</c:message>\n");
        }

      } else {
        Map<String, String[]> parameterMap = new TreeMap<String, String[]>(createParametersMap(req.getCocoonRequest()));

        StringBuffer tempValues = getTempValues(req);

        // Check if all required fields are filled out, if not return error messages
        String validationMessages = validateRequest(req);
        if (!"".equalsIgnoreCase(validationMessages)) {
          messageBuffer.append(validationMessages + "\n");

          bizData.put("resource", tempValues.toString());
          bizData.put("tempvalues", "<empty/>");
          bizData.put("mode", "edit");
          messageBuffer.append("</c:messages>\n");
          bizData.put("messages", messageBuffer.toString());
          bizData.put("userprivileges", userPrivileges);
          bizData.put("publishers", adminService.getAllPublishers());
          bizData.put("facets", adminService.getMostOfTheRequestXMLWithPrefix(req) + "</c:request>");

          res.sendPage("xml2/ressurs", bizData);
          return;
        }

        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateAccepted = dateFormat.format(date); //"2008-18-09T13:39:38";

        // If the user checks that the resource should be marked as new, set updateDate to true
        if (parameterMap.containsKey("markasnew")) {
          updateDate = true;
          parameterMap.remove("markasnew");
        }

        parameterMap.put("sub:committer", new String[]{user.getId()});

        // Generate a dct:identifier if it's a new resource, and set the time and date for approval
        if ("".equalsIgnoreCase(req.getCocoonRequest().getParameter("dct:identifier")) || req.getCocoonRequest().getParameter("dct:identifier") == null) {
          updateDate = true;
          dctIdentifier = searchService.sanitizeStringForURI(req.getCocoonRequest().getParameter("dct:title")); 
          dctIdentifier = getProperty("sublima.base.url") + "resource/" + dctIdentifier + parameterMap.get("the-resource").hashCode();
        } else {
          dctIdentifier = req.getCocoonRequest().getParameter("dct:identifier");
        }

        if (updateDate) {
          parameterMap.put("dct:dateAccepted", new String[]{dateAccepted});
        }

        Form2SparqlService form2SparqlService = new Form2SparqlService(parameterMap.get("prefix"));
        parameterMap.put("sub:url", parameterMap.get("the-resource"));
        parameterMap.remove("dct:identifier");
        parameterMap.put("dct:identifier", new String[]{dctIdentifier});
        parameterMap.remove("prefix"); // The prefixes are magic variables
        parameterMap.remove("actionbutton"); // The name of the submit button
        if (parameterMap.get("subjecturi-prefix") != null) {
          parameterMap.put("subjecturi-prefix", new String[]{getProperty("sublima.base.url") +
                  parameterMap.get("subjecturi-prefix")[0]});
        }

        String sparqlQuery = null;
        try {
          sparqlQuery = form2SparqlService.convertForm2Sparul(parameterMap);
        }
        catch (IOException e) {
          messageBuffer.append("<c:message>Feil ved lagring av ressurs</c:message>\n");
        }

        //String uri = form2SparqlService.getURI();

        insertSuccess = sparulDispatcher.query(sparqlQuery);

        //validated = sparulDispatcher.query(sparqlQuery);
        logger.trace("AdminController.editResource --> INSERT QUERY RESULT: " + insertSuccess);

        if (insertSuccess) {
          if (updateDate) {
            messageBuffer.append("<c:message>Ny ressurs lagt til</c:message>\n");
          }
          else {
            messageBuffer.append("<c:message>Ressurs oppdatert</c:message>\n");            
          }

          indexService.indexResource(req.getCocoonRequest().getParameter("the-resource"), SettingsService.getProperty("sublima.searchfields").split(";"), SettingsService.getProperty("sublima.prefixes").split(";"));
          logger.trace("AdminController.editResource --> Added the resource to the index");
          LARQ.setDefaultIndex(SettingsService.getIndexBuilderNode(null).getIndex());

        } else {
          messageBuffer.append("<c:message>Feil ved lagring av ny ressurs</c:message>\n");
        }
      }

      if (insertSuccess) {
        bizData.put("resource", adminService.getResourceByURI(req.getCocoonRequest().getParameter("the-resource")));
        bizData.put("tempvalues", "<empty></empty>");
        bizData.put("mode", "edit");
      } else {
        bizData.put("resource", adminService.getResourceByURI(req.getCocoonRequest().getParameter("the-resource")));
        bizData.put("tempvalues", "<empty/>");//tempPrefixes + tempValues.toString() + "</c:tempvalues>");
        bizData.put("mode", "edit");
      }

      messageBuffer.append("</c:messages>\n");
      bizData.put("facets", adminService.getMostOfTheRequestXMLWithPrefix(req) + "</c:request>");
      bizData.put("messages", messageBuffer.toString());
      bizData.put("userprivileges", userPrivileges);
      bizData.put("publishers", adminService.getAllPublishers());


      res.sendPage("xml2/ressurs", bizData);

    }
  }

  /**
   * Method to validate the request upon insert of new resource.
   * Checks all parameters and gives error message if one or more required values are null
   * Also chekcs for duplicates, and gives an error message when the resource URI is already registered.
   *
   * @param req
   * @return
   */
  private String validateRequest
          (AppleRequest
                  req) {
    StringBuffer validationMessages = new StringBuffer();

    if ("".equalsIgnoreCase(req.getCocoonRequest().getParameter("dct:title")) || req.getCocoonRequest().getParameter("dct:title") == null) {
      validationMessages.append("<c:message><i18n:text key=\"validation.topic.notitle\">uoversatt</i18n:text></c:message>\n");
    }

    if (req.getCocoonRequest().getParameter("the-resource") == null || "".equalsIgnoreCase(req.getCocoonRequest().getParameter("the-resource"))) {
      validationMessages.append("<c:message>URL kan ikke være blank</c:message>\n");
    }

    if (req.getCocoonRequest().getParameter("dct:identifier") == null || "".equalsIgnoreCase(req.getCocoonRequest().getParameter("dct:identifier").trim())) {
      // if the uri is empty, then it's a new resource and we do a already-exists check
      if (adminService.checkForDuplicatesByURI(req.getCocoonRequest().getParameter("the-resource"))) {
        validationMessages.append("<c:message>En ressurs med denne URI finnes fra før</c:message>\n");
      }
    }

    if (req.getCocoonRequest().getParameter("the-resource") == null && !adminService.validateURL(req.getCocoonRequest().getParameter("the-resource").trim())) {
      validationMessages.append("<c:message>Denne ressursens URI gir en statuskode som tilsier at den ikke er OK. Vennligst sjekk ressursens nettside og prøv igjen.</c:message>\n");
    }

    // Check if the user tries to save it with an approved status, but without a subject
    if (req.getCocoonRequest().getParameterValues("dct:subject") == null && "http://sublima.computas.com/status/godkjent_av_administrator".equalsIgnoreCase(req.getCocoonRequest().getParameter("wdr:describedBy"))) {
      validationMessages.append("<c:message><i18n:text key=\"validation.resource.notopic\">At least one subject must be chosen</i18n:text></c:message>\n");
    }

    if ("".equalsIgnoreCase(req.getCocoonRequest().getParameter("wdr:describedBy")) || req.getCocoonRequest().getParameter("wdr:describedBy") == null) {
      validationMessages.append("<c:message>En status må velges</c:message>\n");
    } else if (!userPrivileges.contains(req.getCocoonRequest().getParameter("wdr:describedBy"))) {
      validationMessages.append("<c:message>Rollen du har tillater ikke å lagre et emne med den valgte statusen.</c:message>\n");
    }

    return validationMessages.toString();
  }

  private StringBuffer getTempValues
          (AppleRequest
                  req) {
    //Keep all selected values in case of validation error
    String temp_title = req.getCocoonRequest().getParameter("dct:title");
    String temp_uri = req.getCocoonRequest().getParameter("the-resource");
    String temp_identifier = req.getCocoonRequest().getParameter("dct:identifier");
    String temp_description = req.getCocoonRequest().getParameter("dct:description");
    String temp_publisher = req.getCocoonRequest().getParameter("dct:publisher");
    String[] temp_languages = req.getCocoonRequest().getParameterValues("dct:language");
    String[] temp_mediatypes = req.getCocoonRequest().getParameterValues("dct:format");
    String[] temp_audiences = req.getCocoonRequest().getParameterValues("dct:audience");
    String[] temp_subjects = req.getCocoonRequest().getParameterValues("dct:subject");
    String temp_comment = req.getCocoonRequest().getParameter("rdfs:comment");
    String temp_status = req.getCocoonRequest().getParameter("wdr:describedBy");

//Create an XML structure for the selected values, to use in the JX template
    StringBuffer xmlStructureBuffer = new StringBuffer();
    xmlStructureBuffer.append("<rdf:RDF  xmlns:topic=\"" + getProperty("sublima.base.url") + "topic/\"\n" +
            "xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\"\n" +
            "xmlns:wdr=\"http://www.w3.org/2007/05/powder#\"\n" +
            "xmlns:lingvoj=\"http://www.lingvoj.org/ontology#\"\n" +
            "xmlns:sioc=\"http://rdfs.org/sioc/ns#\"\n" +
            "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
            "xmlns:foaf=\"http://xmlns.com/foaf/0.1/\"\n" +
            "xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n" +
            "xmlns:dct=\"http://purl.org/dc/terms/\"\n" +
            "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n" +
            "xmlns:dcmitype=\"http://purl.org/dc/dcmitype/\"\n" +
            "xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n" +
            "xmlns:c=\"http://xmlns.computas.com/cocoon\"\n" +
            "xmlns:i18n=\"http://apache.org/cocoon/i18n/2.1\"\n" +
            "xmlns:sub=\"http://xmlns.computas.com/sublima#\">\n");
    xmlStructureBuffer.append("<sub:Resource>\n");
    xmlStructureBuffer.append("<dct:title>" + temp_title + "</dct:title>\n");
    xmlStructureBuffer.append("<sub:url rdf:resource=\"" + temp_uri + "\"/>\n");
    xmlStructureBuffer.append("<dct:identifier rdf:resource=\"" + temp_identifier + "\"/>\n");
    xmlStructureBuffer.append("<dct:description>" + temp_description + "</dct:description>\n");
    xmlStructureBuffer.append("<dct:publisher rdf:resource=\"" + temp_publisher + "\"/>\n");

    if (temp_languages != null) {
      for (String s : temp_languages) {
        //xmlStructureBuffer.append("<language>" + s + "</language>\n");
        xmlStructureBuffer.append("<dct:language rdf:resource=\"" + s + "\"/>\n");
      }
    }

    if (temp_mediatypes != null) {

      for (String s : temp_mediatypes) {
        xmlStructureBuffer.append("<dct:format rdf:resource=\"" + s + "\"/>\n");
      }

    }

    if (temp_audiences != null) {

      for (String s : temp_audiences) {
        xmlStructureBuffer.append("<dct:audience rdf:resource=\"" + s + "\"/>\n");
      }

    }

    if (temp_subjects != null) {
      for (String s : temp_subjects) {
        xmlStructureBuffer.append("<dct:subject rdf:resource=\"" + s + "\"/>\n");
      }
    }

    xmlStructureBuffer.append("<rdfs:comment>" + temp_comment + "</rdfs:comment>\n");
    xmlStructureBuffer.append("<wdr:describedBy rdf:resource=\"" + temp_status + "\"/>\n");
    xmlStructureBuffer.append("</sub:Resource>\n</rdf:RDF>\n");

    return xmlStructureBuffer;
  }

  /**
   * Method to display the initial page for administrating resources
   *
   * @param res - AppleResponse
   */
  private void showResourcesIndex(AppleResponse res, AppleRequest req) {
    Map<String, Object> bizData = new HashMap<String, Object>();
    AdminService adminservice = new AdminService();
    bizData.put("statuses", adminservice.getDistinctAndUsedLabels("<http://www.w3.org/2007/05/powder#DR>",
            "<http://www.w3.org/2007/05/powder#describedBy>"));
    bizData.put("facets", adminService.getMostOfTheRequestXMLWithPrefix(req) + "</c:request>");
    res.sendPage("xml2/ressurser", bizData);
  }

  public void setSparqlDispatcher(SparqlDispatcher sparqlDispatcher) {
    this.sparqlDispatcher = sparqlDispatcher;
  }

  public void setSparulDispatcher
          (SparulDispatcher
                  sparulDispatcher) {
    this.sparulDispatcher = sparulDispatcher;
  }

  //todo Move to a Service-class
  private Map<String, String[]> createParametersMap
          (Request
                  request) {
    Map<String, String[]> result = new HashMap<String, String[]>();
    Enumeration parameterNames = request.getParameterNames();
    while (parameterNames.hasMoreElements()) {
      String paramName = (String) parameterNames.nextElement();
      result.put(paramName, request.getParameterValues(paramName));
    }
    return result;
  }

  private void massEditResource(AppleResponse res, AppleRequest req) {
    Map<String, Object> bizData = new HashMap<String, Object>();

    // When GET present a blank form with listvalues or prefilled with resource
    if (req.getCocoonRequest().getMethod().equalsIgnoreCase("POST")) {
      int i = 0;
      Enumeration fo = req.getCocoonRequest().getParameterNames();
      while (req.getCocoonRequest().getParameterValues("new-" + i) != null) {
        String newurl = req.getCocoonRequest().getParameterValues("new-" + i)[0];
        String oldurl = req.getCocoonRequest().getParameterValues("old-" + i)[0];
        String sparulQuery = "MODIFY\nDELETE { <" + oldurl + "> ?p ?o }\nINSERT { <" + newurl + "> ?p ?o }\nWHERE { <" + oldurl + "> ?p ?o }\n";
        logger.trace("Changing " + oldurl + " to " + newurl + " in subjects.");
        boolean updateSuccess = sparulDispatcher.query(sparulQuery);
        logger.debug("Subject edit status: " + updateSuccess);
        sparulQuery = "MODIFY\nDELETE { ?s ?p <" + oldurl + "> }\nINSERT { ?s ?p <" + newurl + "> }\nWHERE { ?s ?p <" + oldurl + "> }\n";
        logger.trace("Changing " + oldurl + " to " + newurl + " in objects.");
        updateSuccess = sparulDispatcher.query(sparulQuery);
        logger.debug("Object edit status: " + updateSuccess);
        i++;
      }
      logger.debug("Mass updated " + i + " records.");
    }
    bizData.put("facets", adminService.getMostOfTheRequestXMLWithPrefix(req) + "</c:request>");
    res.sendPage("xml2/massedit", bizData);
  }
}