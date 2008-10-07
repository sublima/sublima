package com.computas.sublima.app.controller.admin;

import com.computas.sublima.app.service.URLActions;
import com.computas.sublima.app.service.AdminService;
import com.computas.sublima.query.SparulDispatcher;
import static com.computas.sublima.query.service.SettingsService.getProperty;
import com.computas.sublima.query.service.SettingsService;
import com.computas.sublima.query.service.SearchService;
import com.hp.hpl.jena.sparql.util.StringUtils;
import org.apache.cocoon.components.flow.apples.AppleRequest;
import org.apache.cocoon.components.flow.apples.AppleResponse;
import org.apache.cocoon.components.flow.apples.StatelessAppleController;
import org.apache.cocoon.auth.ApplicationManager;
import org.apache.log4j.Logger;
import net.sf.akismet.Akismet;
import java.util.Map;
import java.util.HashMap;

public class FeedbackController implements StatelessAppleController {

  private static Logger logger = Logger.getLogger(FeedbackController.class);
  private String mode;
  private SparulDispatcher sparulDispatcher;
  boolean success = false;
  private ApplicationManager appMan;
  private AdminService adminService = new AdminService();
  private SearchService searchService = new SearchService();

  //todo Check how to send error messages with Cocoon (like Struts 2's s:actionmessage)
  @SuppressWarnings("unchecked")
  public void process(AppleRequest req, AppleResponse res) throws Exception {

    this.mode = req.getSitemapParameter("mode");
    boolean loggedIn = appMan.isLoggedIn("Sublima");

    if ("resourcecomment".equalsIgnoreCase(mode)) {
      String uri = req.getCocoonRequest().getParameter("uri");
      String email = req.getCocoonRequest().getParameter("email");
      String comment = req.getCocoonRequest().getParameter("comment");

      String generatedUserURI = getProperty("sublima.base.url") + "user/visitor/" + email.hashCode();
      String generatedCommentURI = getProperty("sublima.base.url") + "comment/resource/" + email.hashCode() + comment.hashCode();

      if (isSpam(req, loggedIn, "", comment, email, uri)) {
          logger.info("FeedbackController.java --> Akismet said comment from " + email + " was spam. Silently ignored.");
          res.redirectTo(req.getCocoonRequest().getParameter("resource") + ".html");
          return;
      } else {
          logger.trace("FeedbackController.java --> Akismet said comment from " + email + " is ham.");
      }

      String commentString = StringUtils.join("\n", new String[]{
              "PREFIX dct: <http://purl.org/dc/terms/>",
              "PREFIX wdr: <http://www.w3.org/2007/05/powder#>",
              "PREFIX sub: <http://xmlns.computas.com/sublima#>",
              "PREFIX sioc:<http://rdfs.org/sioc/ns#>",
              "INSERT",
              "{",
              "    <" + uri + ">" + " sub:comment <" + generatedCommentURI + "> .",
              "        <" + generatedCommentURI + "> a sioc:Item ;",
              "            sioc:content \"\"\"" + comment + "\"\"\" ;",
              "        sioc:has_creator <" + generatedUserURI + "> .",
              "        <" + generatedUserURI + "> a sioc:User ;",
              "            sioc:email <mailto:" + email + "> .",
              "}"});

      success = sparulDispatcher.query(commentString);
      logger.trace("FeedbackController.java --> Comment on resource: " + commentString + "\nResult: " + success);

      //todo Back to the resource details page
      if (success) {
        res.redirectTo(req.getCocoonRequest().getParameter("resource") + ".html");
        return;
      } else {
        res.redirectTo(req.getCocoonRequest().getParameter("resource") + ".html");
        return;
      }
    }

    if ("visTipsForm".equalsIgnoreCase(mode)) {
      res.sendPage("xhtml/tips", null);
      return;
    }

    if ("tips".equalsIgnoreCase(mode)) {
      Map<String, Object> bizData = new HashMap<String, Object>();

      StringBuffer messageBuffer = new StringBuffer();
      messageBuffer.append("<c:messages xmlns:c=\"http://xmlns.computas.com/cocoon\"></c:messages>");
      bizData.put("messages", messageBuffer.toString());
      bizData.put("mode", "form");
      bizData.put("loggedin", loggedIn);
      res.sendPage("xml/tips", bizData);
      return;
    }

    if ("sendtips".equalsIgnoreCase(mode)) {

      Map<String, Object> bizData = new HashMap<String, Object>();

      StringBuffer messageBuffer = new StringBuffer();
      messageBuffer.append("<c:messages xmlns:c=\"http://xmlns.computas.com/cocoon\">\n");

      String url = req.getCocoonRequest().getParameter("url");
      String tittel = req.getCocoonRequest().getParameter("tittel");
      String beskrivelse = req.getCocoonRequest().getParameter("beskrivelse");
      String stikkord = req.getCocoonRequest().getParameter("stikkord");
      String status;

      if (isSpam(req, loggedIn, url, tittel + "\n" + beskrivelse, "", "")) {
          logger.debug("FeedbackController.java --> Akismet said " + url + " was spam.");
          messageBuffer.append("<c:message>Ressursen du sendte inn er markert i vår spam-database. Vennligst kontakt administrator!</c:message>");
          messageBuffer.append("</c:messages>\n");
          bizData.put("messages", messageBuffer.toString());
          bizData.put("mode", "form");
          bizData.put("loggedin", loggedIn);
          res.sendPage("xml/tips", bizData);
          return;
      } else {
          logger.trace("FeedbackController.java --> Akismet said " + url + " is ham.");
      }

        if ((!"".equals(url)) && adminService.checkForDuplicatesByURI(url)) {
        messageBuffer.append("<c:message>Ressursen du tipset om finnes allerede.</c:message>");
        messageBuffer.append("</c:messages>\n");
        bizData.put("messages", messageBuffer.toString());
        bizData.put("mode", "form");
        bizData.put("loggedin", loggedIn);
        res.sendPage("xml/tips", bizData);
        return;
      }

      try {
        // Do a URL check so that we know we have a valid URL
        URLActions urlAction = new URLActions(url);
        status = urlAction.getCode();
      }
      catch (Exception e) {
        e.printStackTrace();
        messageBuffer.append("<c:message>Feil ved angitt URL. Vennligst kontroller at linken du oppga fungerer.</c:message>");
        messageBuffer.append("</c:messages>\n");
        bizData.put("messages", messageBuffer.toString());
        bizData.put("mode", "form");
        bizData.put("loggedin", loggedIn);
        res.sendPage("xml/tips", bizData);
        return;  
      }

      //todo We have to get the interface-language @no from somewhere
      //todo Move the 30x check to a private static method 
      if ("302".equals(status) ||
            "303".equals(status) ||
            "304".equals(status) ||
            "305".equals(status) ||
            "307".equals(status) ||
            status.startsWith("2")) {

        String dctidentifier = searchService.sanitizeStringForURI(tittel);
        String insertTipString =
                "PREFIX dct: <http://purl.org/dc/terms/>\n" +
                        "PREFIX wdr: <http://www.w3.org/2007/05/powder#>\n" +
                        "PREFIX sub: <http://xmlns.computas.com/sublima#>\n" +
                        "INSERT\n" +
                        "{\n" +
                        "<" + url + "> a sub:Resource .\n" +
                        "<" + url + "> dct:title " + "\"\"\"" + tittel + "\"\"\"@no . \n" +
                        "<" + url + "> dct:description " + "\"\"\"" + beskrivelse.replace("\\", "\\\\") + "\"\"\"@no . \n" +
                        "<" + url + "> sub:keywords " + "\"\"\"" + stikkord + "\"\"\"@no . \n" +
                        "<" + url + "> wdr:describedBy <http://sublima.computas.com/status/nytt_forslag> .\n" +
                        "<" + url + "> sub:url <" + url + "> . \n" +
                        "<" + url + "> dct:identifier <" + SettingsService.getProperty("sublima.base.url") + "resource/" + dctidentifier + url.hashCode() + "> . }";


        success = sparulDispatcher.query(insertTipString);
        logger.trace("sendTips --> RESULT: " + success);

        if (success) {
          messageBuffer.append("<c:message>Ditt tips er mottatt. Tusen takk :)</c:message>");
          messageBuffer.append("</c:messages>\n");
          bizData.put("messages", messageBuffer.toString());
          bizData.put("mode", "ok");
          bizData.put("loggedin", loggedIn);
          res.sendPage("xml/tips", bizData);
        } else {
          messageBuffer.append("<c:message>Det skjedde noe galt. Kontroller alle feltene og prøv igjen</c:message>");
          messageBuffer.append("</c:messages>\n");
          bizData.put("mode", "form");
          bizData.put("loggedin", loggedIn);
          bizData.put("messages", messageBuffer.toString());
          res.sendPage("xml/tips", bizData);
        }

      } else {
        messageBuffer.append("<c:message>Feil ved angitt URL. Vennligst kontroller at linken du oppga fungerer.</c:message>");
        messageBuffer.append("</c:messages>\n");
        bizData.put("mode", "form");
        bizData.put("loggedin", loggedIn);
        bizData.put("messages", messageBuffer.toString());
        res.sendPage("xml/tips", bizData);
      }
    }
  }

    private boolean isSpam(AppleRequest req, boolean loggedIn, String url, String content, String email, String resource) {
        // If the user is not logged in, do a spam check if configured
        boolean spam = false;
        if (! loggedIn && getProperty("sublima.akismet.key") != null) {
            logger.info("FeedbackController.java --> Akismet key set");
            Akismet akismet = new Akismet(getProperty("sublima.akismet.key"),
                                          getProperty("sublima.base.url"));
            if (akismet.verifyAPIKey()) {
                spam = akismet.commentCheck(
                        req.getCocoonRequest().getRemoteAddr(),
                        "",
                        "",
                        resource,
                        "comment",
                        "",
                        email,
                        url,
                        content,
                        null
                );

            } else {
                logger.error("FeedbackController.java --> Akismet key not valid, disabling.");
            }
        }
        return spam;
    }


    public void setSparulDispatcher(SparulDispatcher sparulDispatcher) {
    this.sparulDispatcher = sparulDispatcher;
  }

  public void setAppMan(ApplicationManager appMan) {
    this.appMan = appMan;
  }

}
