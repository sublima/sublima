package com.computas.sublima.app.controller.form;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.components.flow.apples.AppleController;
import org.apache.cocoon.components.flow.apples.AppleRequest;
import org.apache.cocoon.components.flow.apples.AppleResponse;
import org.apache.cocoon.forms.formmodel.Form;
import org.apache.cocoon.forms.FormContext;
import org.apache.cocoon.environment.Request;

import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.net.URLEncoder;

/**
 * @author: mha
 * Date: 11.apr.2008
 */
public class ResourceFormController implements AppleController, Serviceable {
  private ServiceManager serviceManager;
  private boolean init = false;
  private Form form;

  //todo Get locale from user settings
  //private Locale locale = Locale.US;

  public void process(AppleRequest appleRequest, AppleResponse appleResponse) throws Exception {
    if (!init) {
            form = FormHelper.createForm(serviceManager,
                           "resources/form/firstform_definition.xml");
            init = true;
        }

        Request request = appleRequest.getCocoonRequest();
        if (request.getMethod().equals("GET")) {
            showForm(appleResponse);
        } else if (request.getMethod().equals("POST")) {
            boolean finished = form.process(new FormContext(request /*, locale*/));
            if (!finished) {
                showForm(appleResponse);
            } else {
                String name = (String)form.getChild("name").getValue();
                appleResponse.redirectTo("http://google.com/search?q="
                                         + URLEncoder.encode(name, "UTF-8"));
            }
        } else {
            throw new Exception("Unexpected HTTP method: " + request.getMethod());
        }
  }

   private void showForm(AppleResponse appleResponse /*, Locale locale */) {
        Map viewData = new HashMap();
        viewData.put("CocoonFormsInstance", form);
        //viewData.put("locale", locale);
        appleResponse.sendPage("ResourceForm", viewData);
    }

  public void service(ServiceManager serviceManager) throws ServiceException {
    this.serviceManager = serviceManager;  
  }
}
