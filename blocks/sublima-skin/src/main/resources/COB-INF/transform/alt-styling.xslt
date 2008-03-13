<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
        xmlns:c="http://xmlns.computas.com/cocoon"
        xmlns:skos="http://www.w3.org/2004/02/skos/core#"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:dct="http://purl.org/dc/terms/"
        xmlns="http://www.w3.org/1999/xhtml"
        version="1.0">
  <!-- xsl:output method="html" indent="yes"/ -->

  <xsl:import href="rdfxml2xhtml-deflist.xsl"/>
  <xsl:import href="rdfxml2xhtml-facets.xsl"/>
  <xsl:import href="rdfxml-nav-templates.xsl"/>

  <xsl:param name="mode"/>

  <xsl:template name="advancedsearch"> <!-- I have no idea why it didn't work to have a normal node-based template -->
    <xsl:copy-of select="c:page/c:advancedsearch/*"/>
  </xsl:template>

  <xsl:template name="tips"> <!-- I have no idea why it didn't work to have a normal node-based template -->
    <xsl:copy-of select="c:page/c:tips/*"/>
  </xsl:template>

  <xsl:template match="/">

    <html>
      <head>
        <title>Detektor</title>
        <!-- link rel="stylesheet" type="text/css" href="http://detektor.deichman.no/stylesheet.css"/> -->
        <!-- link rel="stylesheet" type="text/css" href="styles/alt-css.css"/ -->
        <style type="text/css" media="screen">

          /* General styles
          body {
          margin:0;
          padding:0;
          border:0; /* This removes the border around the viewport in old versions of IE */
          width:100%;
          background:#fff;
          min-width:600px; /* Minimum width of layout - remove line if not required */
          /* The min-width property does not work in old versions of Internet Explorer */
          font-size:90%;
          }
          a {
          color:#369;
          }
          a:hover {
          color:#fff;
          background:#369;
          text-decoration:none;
          }
          h1, h2, h3 {
          margin:.8em 0 .2em 0;
          padding:0;
          }
          p {
          margin:.4em 0 .8em 0;
          padding:0;
          }
          img {
          margin:10px 0 5px;
          }
          /* Header styles */
          #header {
          clear:both;
          float:left;
          width:100%;
          }
          #header {
          border-bottom:1px solid #000;
          }
          #header p,
          #header h1,
          #header h2 {
          padding:.4em 15px 0 15px;
          margin:0;
          }
          #header ul {
          clear:left;
          float:left;
          width:100%;
          list-style:none;
          margin:10px 0 0 0;
          padding:0;
          }
          #header ul li {
          display:inline;
          list-style:none;
          margin:0;
          padding:0;
          }
          #header ul li a {
          display:block;
          float:left;
          margin:0 0 0 1px;
          padding:3px 10px;
          text-align:center;
          background:#eee;
          color:#000;
          text-decoration:none;
          position:relative;
          left:15px;
          line-height:1.3em;
          }
          #header ul li a:hover {
          background:#369;
          color:#fff;
          }
          #header ul li a.active,
          #header ul li a.active:hover {
          color:#fff;
          background:#000;
          font-weight:bold;
          }
          #header ul li a span {
          display:block;
          }
          /* 'widths' sub menu */
          #layoutdims {
          clear:both;
          background:#eee;
          border-top:4px solid #000;
          margin:0;
          padding:6px 15px !important;
          text-align:right;
          }
          /* column container */
          .colmask {
          position:relative; /* This fixes the IE7 overflow hidden bug */
          clear:both;
          float:left;
          width:100%; /* width of whole page */
          overflow:hidden; /* This chops off any overhanging divs */
          }
          /* common column settings */
          .colright,
          .colmid,
          .colleft {
          float:left;
          width:100%; /* width of page */
          position:relative;
          }
          .col1,
          .col2,
          .col3 {
          float:left;
          position:relative;
          padding:0 0 1em 0; /* no left and right padding on columns, we just make them narrower instead
          only padding top and bottom is included here, make it whatever value you need */
          overflow:hidden;
          }
          /* 3 Column settings */
          .threecol {
          background:#eee; /* right column background colour */
          }
          .threecol .colmid {
          right:25%; /* width of the right column */
          background:#fff; /* center column background colour */
          }
          .threecol .colleft {
          right:50%; /* width of the middle column */
          background:#f4f4f4; /* left column background colour */
          }
          .threecol .col1 {
          width:46%; /* width of center column content (column width minus padding on either side) */
          left:102%; /* 100% plus left padding of center column */
          }
          .threecol .col2 {
          width:21%; /* Width of left column content (column width minus padding on either side) */
          left:31%; /* width of (right column) plus (center column left and right padding) plus (left column left
          padding) */
          }
          .threecol .col3 {
          width:21%; /* Width of right column content (column width minus padding on either side) */
          left:85%; /* Please make note of the brackets here:
          (100% - left column width) plus (center column left and right padding) plus (left column left and right
          padding) plus (right column left padding) */
          }
          /* Footer styles */
          #footer {
          clear:both;
          float:left;
          width:100%;
          border-top:1px solid #000;
          }
          #footer p {
          padding:10px;
          margin:0;
          }
        </style>
      </head>
      <body>

        <div id="header">
          <!-- h1>Detektor</h1 -->
          <img alt="header logo" src="detektor_beta_header.png"/>
          <!-- img src="images/detektor_beta_header.png"/ -->
          <xsl:value-of select="$mode"/>
          <h2>Demosite for portalverktøyet Sublima</h2>
          <ul>
            <li>
              <a href="home" class="active">Hjem</a>
            </li>
            <li>
              <a href="advancedsearch">Avansert søk
              </a>
            </li>
          </ul>

          <p id="layoutdims">
            Brødsmuler her? |
            <a href="#">Smule 1</a>
            |
            <a href="#">Smule 2</a>
            |
            <strong>Nåværende smule</strong>
          </p>
        </div>
        <div class="colmask threecol">
          <div class="colmid">
            <div class="colleft">
              <div class="col1">

                <!-- Column 1 start -->
                <xsl:if test="not(c:page/c:advancedsearch/node())">
                  <form name="freetextSearch" action="freetext-result" method="get">
                    <table>
                      <tr>
                        <td>
                          <input id="keyword" class="searchbox" type="text"
                                 name="searchstring" size="50"/>
                        </td>
                        <td>
                          <input type="submit" value="Søk"/>
                        </td>
                        <td>
                          <a href="advancedsearch">Avansert søk</a>
                        </td>
                      </tr>
                      <tr>
                        <td>
                          <input type="radio" name="booleanoperator" value="AND" checked="true" />OG <input type="radio" name="booleanoperator" value="OR" /> ELLER
                          <input type="checkbox" name="deepsearch" value="deepsearch"/> Søk også i de eksterne ressursene
                        </td>
                      </tr>
                    </table>
                  </form>
                </xsl:if>

                <!-- xsl:if test="c:page/c:mode = 'topic-instance'" -->

                  <h3>Navigering</h3>
                  <xsl:apply-templates select="c:page/c:navigation/rdf:RDF/skos:Concept"/>

                <!-- /xsl:if -->
                <!-- Her kommer avansert søk dersom denne er angitt, og tipsboksen dersom brukeren har valgt den -->
                <xsl:call-template name="advancedsearch"/>
                <!-- xsl:copy-of select="c:page/c:advancedsearch/*"/ -->
                <xsl:call-template name="tips"/>

                <h3>Ressurser</h3>
                <!-- Søkeresultatene -->
                <xsl:apply-templates select="c:page/c:result-list/rdf:RDF" mode="results"/>
                <!-- Column 1 end -->
              </div>
              <div class="col2">
                <!-- Column 2 start -->
                  <h3>Fasetter</h3>
                  <xsl:if test="c:page/c:facets">
                    <xsl:apply-templates select="c:page/c:result-list/rdf:RDF" mode="facets"/>
                  </xsl:if>



                <!-- Column 2 end -->
              </div>
              <div class="col3">
                <!-- Column 3 start -->
                  <!-- xsl:if test="c:page/c:mode = 'search-result'" -->
                 <h2>Min side osv.</h2>
                 <a href="tips">Tips oss om en ny ressurs</a>


                <!-- /xsl:if -->
                <!-- Column 3 end -->
              </div>
            </div>

          </div>
        </div>
        <div id="footer">
          <p>A Free Software Project supported by
            <a href="http://www.abm-utvikling.no/">ABM Utvikling</a>
            and
            <a href="http://www.computas.com">Computas AS</a>
            , 2008
          </p>
        </div>


      </body>
    </html>
  </xsl:template>


</xsl:stylesheet>