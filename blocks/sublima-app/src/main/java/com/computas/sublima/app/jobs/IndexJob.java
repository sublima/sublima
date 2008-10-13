package com.computas.sublima.app.jobs;

import com.computas.sublima.app.service.IndexService;

/**
 * @author: mha
 * Date: 10.aug.2008
 */
public class IndexJob {

  public static void main(String[] args) {

    String usageString = "usage: IndexJob -searchfields dct:title;dct:description -prefixes dct:<http://purl.org/dc/terms/;foaf:<http://xmlns.com/foaf/0.1/> -indexdir /usr/local/index/ -indexexternal true|false";

    String[] searchfields = null;
    String[] prefixes = null;
    String indexDir = ""; //"/tmp/june";
    String indexType = "file"; //"file";
    boolean indexexternal = false;

    String argument;

    for (int i = 0; i < args.length; i++) {
      argument = args[i];

      if (argument.equals("-searchfields")) {
        if (i < args.length + 1) {
          searchfields = args[i + 1].split(";");
        } else {
          System.err.println("-searchfields requires a semicolon separated string of searchfields");
          System.err.println(usageString);
          return;
        }
      }


      if (argument.equals("-prefixes")) {
        if (i < args.length + 1) {
          prefixes = args[i + 1].split(";");
        } else {
          System.err.println("-prefixes requires a semicolon separated string of prefixes");
          System.err.println(usageString);
          return;
        }
      }

      if (argument.equals("-indexdir")) {
        if (i < args.length + 1) {
          indexDir = args[i + 1];
        } else {
          System.err.println("-indexdir required");
          System.err.println(usageString);
          return;
        }
      }

      if (argument.equals("-indexexternal")) {
        if (i < args.length + 1) {
          if ("true".equalsIgnoreCase(args[i + 1]) || "false".equalsIgnoreCase(args[i + 1])) {
            indexexternal = Boolean.valueOf(args[i + 1]);
          } else {
            System.err.println("-indexexternal must be true or false");
            System.err.println(usageString);
            return;
          }
        } else {
          System.err.println("-indexexternal must be true or false");
          System.err.println(usageString);
          return;
        }
      }
    }

    if (searchfields == null || prefixes == null || indexDir == null) {
      System.err.println(usageString);
      return;
    }

    IndexService indexService = new IndexService();

    indexService.createIndex(indexDir, indexType, searchfields, prefixes, indexexternal);
  }
}
