package com.computas.sublima.app.controller;

import com.computas.sublima.app.service.URLActions;
import com.computas.sublima.query.SparulDispatcher;
import org.apache.cocoon.components.flow.apples.AppleRequest;
import org.apache.cocoon.components.flow.apples.AppleResponse;
import org.apache.cocoon.components.flow.apples.StatelessAppleController;
import org.apache.log4j.Logger;

public class FeedbackController implements StatelessAppleController {

  private static Logger logger = Logger.getLogger(FeedbackController.class);
  private String mode;
  private SparulDispatcher sparulDispatcher;
  boolean success = false;

  //todo Check how to send error messages with Cocoon (like Struts 2's s:actionmessage)
  @SuppressWarnings("unchecked")
  public void process(AppleRequest req, AppleResponse res) throws Exception {

    this.mode = req.getSitemapParameter("mode");

    if ("resourcecomment".equalsIgnoreCase(mode)) {
      String uri = req.getCocoonRequest().getParameter("uri");
      String email = req.getCocoonRequest().getParameter("email");
      String comment = req.getCocoonRequest().getParameter("comment");

      String commentString =
              "PREFIX dct: <http://purl.org/dc/terms/>\n" +
                      "PREFIX wdr: <http://www.w3.org/2007/05/powder#>\n" +
                      "PREFIX sub: <http://xmlns.computas.com/sublima#>\n" +
                      "INSERT\n" +
                      "{\n" +
                      "<" + uri + ">" + " sub:comment " + "\"" + comment + "\"@no ; \n" +
                      "}";

      success = sparulDispatcher.query(commentString);
      logger.trace("FeedbackController.java --> Comment on resource: " + commentString + "\nResult: " + success);

      //todo Back to the resource details page
      if (success) {
        res.redirectTo(req.getCocoonRequest().getParameter("resource"));
        return;
      } else {
        res.redirectTo(req.getCocoonRequest().getParameter("resource"));
        return;
      }
    }

    if ("visTipsForm".equalsIgnoreCase(mode)) {
      res.sendPage("xhtml/tips", null);
      return;
    }

    if ("sendtips".equalsIgnoreCase(mode)) {
      String url = req.getCocoonRequest().getParameter("url");
      String tittel = req.getCocoonRequest().getParameter("tittel");
      String beskrivelse = req.getCocoonRequest().getParameter("beskrivelse");
      String[] stikkord = req.getCocoonRequest().getParameter("stikkord").split(",");

      // Do a URL check so that we know we have a valid URL
      URLActions urlAction = new URLActions(url);
      String status = urlAction.getCode();

      //todo We have to get the interface-language @no from somewhere
      if ("200".equals(status)) {
        String partialUpdateString =
                "PREFIX dct: <http://purl.org/dc/terms/>\n" +
                        "PREFIX wdr: <http://www.w3.org/2007/05/powder#>\n" +
                        "PREFIX sub: <http://xmlns.computas.com/sublima#>\n" +
                        "INSERT\n" +
                        "{\n" +
                        "<" + url + "> a sub:Resource .\n" +
                        "<" + url + "> dct:title " + "\"" + tittel + "\"@no . \n" +
                        "<" + url + "> dct:description " + "\"" + beskrivelse + "\"@no . \n" +
                        "<" + url + "> dct:keywords " + "\"" + stikkord.toString() + "\"@no . \n" +
                        "<" + url + "> wdr:DR <http://sublima.computas.com/status/til_godkjenning> .\n";

        StringBuffer partialUpdateStringBuffer = new StringBuffer();

        partialUpdateStringBuffer.append(partialUpdateString);
        //for each keyword, add a sub:keyword
        for (String s : stikkord) {
          partialUpdateStringBuffer.append("<" + url + "> sub:keyword \"" + s + "\"@no .\n");
        }
        partialUpdateStringBuffer.append("}");

        success = sparulDispatcher.query(partialUpdateStringBuffer.toString());
        logger.trace("sendTips --> RESULT: " + success);

        if (success) {
          res.sendPage("takk", null);
          return;
        } else {
          res.sendPage("xhtml/tips", null);
          return;
        }

      } else {
        res.sendPage("xhtml/tips", null);
        return;
      }
    }

    return;
  }


  public void setSparulDispatcher(SparulDispatcher sparulDispatcher) {
    this.sparulDispatcher = sparulDispatcher;
  }

}
