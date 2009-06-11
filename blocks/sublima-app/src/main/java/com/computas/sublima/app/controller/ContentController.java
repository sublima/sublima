package com.computas.sublima.app.controller;

import com.computas.sublima.app.service.LanguageService;
import org.apache.cocoon.auth.ApplicationManager;
import org.apache.cocoon.components.flow.apples.AppleRequest;
import org.apache.cocoon.components.flow.apples.AppleResponse;
import org.apache.cocoon.components.flow.apples.StatelessAppleController;
import org.apache.log4j.Logger;


public class ContentController implements StatelessAppleController {
  private ApplicationManager appMan;
  boolean loggedIn;

  private static Logger logger = Logger.getLogger(ContentController.class);

  @SuppressWarnings("unchecked")
  public void process(AppleRequest req, AppleResponse res) throws Exception {
    loggedIn = appMan.isLoggedIn("Sublima");

    String mode = req.getSitemapParameter("mode");

    LanguageService langServ = new LanguageService();
    String language = langServ.checkLanguage(req, res);

    logger.trace("ContentController: Language from sitemap is " + req.getSitemapParameter("interface-language"));
    logger.trace("ContentController: Language from service is " + language);

    res.sendPage("static/" + mode, null);

  }

  public void setAppMan(ApplicationManager appMan) {
    this.appMan = appMan;
  }
}