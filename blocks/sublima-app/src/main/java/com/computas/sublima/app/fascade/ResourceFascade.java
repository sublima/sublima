package com.computas.sublima.app.fascade;

import com.computas.sublima.app.controller.admin.ResourceController;
import com.computas.sublima.query.SparulDispatcher;
import org.apache.cocoon.components.flow.apples.AppleRequest;
import org.apache.cocoon.components.flow.apples.AppleResponse;
import org.apache.cocoon.components.flow.apples.StatelessAppleController;

/**
 * @author: mha
 * Date: 11.nov.2008
 */
public class ResourceFascade implements StatelessAppleController {

  private SparulDispatcher sparulDispatcher;

  public void process(AppleRequest appleRequest, AppleResponse appleResponse) throws Exception {
    ResourceController tc = new ResourceController();
    tc.setSparulDispatcher(sparulDispatcher);
    tc.process(appleRequest, appleResponse);
  }

  public void setSparulDispatcher(SparulDispatcher sparulDispatcher) {
    this.sparulDispatcher = sparulDispatcher;
  }
}                                       
