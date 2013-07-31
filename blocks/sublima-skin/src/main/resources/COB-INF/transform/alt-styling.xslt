<?xml version="1.0" encoding="UTF-8"?>


<!--
This stylesheet is the main stylesheet that is called for all pages.

-->

<!--
PVJ: Made the file UTF-8
-->

<xsl:stylesheet
    xmlns:c="http://xmlns.computas.com/cocoon"
    xmlns:skos="http://www.w3.org/2004/02/skos/core#"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:dct="http://purl.org/dc/terms/"
    xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
    xmlns:sub="http://xmlns.computas.com/sublima#"
    xmlns="http://www.w3.org/1999/xhtml"
    version="1.0">
  <!-- xsl:output method="html" indent="yes"/ -->

  <xsl:import href="rdfxml2xhtml-deflist.xsl"/>
  <xsl:import href="rdfxml2xhtml-deflist-full.xsl"/>
  <xsl:import href="rdfxml2xhtml-deflist-short.xsl"/>
  <xsl:import href="rdfxml2xhtml-facets.xsl"/>
  <xsl:import href="rdfxml2xhtml-table.xsl"/>
  <xsl:import href="rdfxml-nav-templates.xsl"/>
  <xsl:import href="browse.xsl"/>
  <xsl:import href="headers.xsl"/>
  <xsl:import href="tipsform.xsl"/>
  <xsl:import href="loginform.xsl"/>
  <xsl:import href="a-z.xsl"/>
  <xsl:import href="set-lang.xsl"/>
  <xsl:import href="advancedsearch.xsl"/>
  <xsl:import href="statiskinnhold.xsl"/>
  <xsl:import href="nyeste.xsl"/>

  <xsl:param name="baseurl"/>
  <xsl:param name="querystring"/>
  <xsl:param name="interface-language"/>


  <xsl:param name="qloc">
    <xsl:if test="contains(/c:page/c:facets/c:request/@requesturl, 'locale=')">
      <xsl:text>?locale=</xsl:text>
      <xsl:value-of select="$interface-language"/>
    </xsl:if>
  </xsl:param>

  <xsl:param name="aloc">
    <xsl:if test="contains(/c:page/c:facets/c:request/@requesturl, 'locale=')">
      <xsl:text>&amp;locale=</xsl:text>
      <xsl:value-of select="$interface-language"/>
    </xsl:if>
  </xsl:param>

  <xsl:param name="rss-url">
    <xsl:if test="c:page/c:mode = 'topic' or c:page/c:mode = 'search-result'">
      <xsl:choose>
	<xsl:when test="contains(/c:page/c:facets/c:request/@requesturl, '?')">
	  <xsl:value-of select="concat(substring-before(/c:page/c:facets/c:request/@requesturl, '.html?'), '.rss?', substring-after(/c:page/c:facets/c:request/@requesturl, '?'))"/>
	</xsl:when>
	<xsl:otherwise>
	  <xsl:value-of select="concat(substring-before(/c:page/c:facets/c:request/@requesturl, '.html'), '.rss')"/>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:param>



  <xsl:param name="numberofhits" select="count(c:page/c:result-list/rdf:RDF/sub:Resource)"/>
  <xsl:param name="numberoftopics" select="count(c:page/c:navigation/rdf:RDF//skos:Concept)"/>

  <xsl:param name="res-view">
    <xsl:if test="/c:page/c:facets/c:request/c:param[@key='res-view'] !=''">
        <xsl:value-of select="/c:page/c:facets/c:request/c:param[@key='res-view']/c:value"/>
    </xsl:if>
  </xsl:param>

    <!-- views -->
    <!-- "just" remove the res-view attribute -->
    <!-- issue: a & is left.... -->
	<xsl:param name="gen-req">
		<xsl:choose>
			<!--xsl:when test="contains(/c:page/c:facets/c:request/@requesturl, 'res-view=short')">
	  <xsl:value-of select="concat(substring-before(/c:page/c:facets/c:request/@requesturl, 'res-view=short'),  substring-after(/c:page/c:facets/c:request/@requesturl, 'res-view=short'))"/>
	</xsl:when-->
			<xsl:when test="contains(/c:page/c:facets/c:request/@requesturl, 'res-view=full')">
				<xsl:value-of select="concat(substring-before(/c:page/c:facets/c:request/@requesturl, 'res-view=full'),  substring-after(/c:page/c:facets/c:request/@requesturl, 'res-view=full'))"/>
			</xsl:when>
			<xsl:when test="contains(/c:page/c:facets/c:request/@requesturl, 'res-view=medium')">
				<xsl:value-of select="concat(substring-before(/c:page/c:facets/c:request/@requesturl, 'res-view=medium'),  substring-after(/c:page/c:facets/c:request/@requesturl, 'res-view=medium'))"/>
			</xsl:when>

			<xsl:otherwise>
				<xsl:value-of select="/c:page/c:facets/c:request/@requesturl"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:param>

	<!-- sort by title by default -->
	<xsl:variable name="sortby">
		<xsl:choose>
			<xsl:when test="/c:page/c:searchparams/c:searchparams/c:sortby">
				<xsl:value-of select="/c:page/c:searchparams/c:searchparams/c:sortby"/>
			</xsl:when>		
			<xsl:otherwise>title</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>

  <!-- A debug template that dumps the source tree. Do not remove
       this, just comment out the call-template -->
  <xsl:template name="debug">
    <div id="debug">
      <xsl:copy-of select="*"/>
    </div>
  </xsl:template>

  <xsl:template name="messages">
    <xsl:if test="c:page/c:content/c:messages/c:messages/c:message">
      <ul>
        <xsl:for-each select="c:page/c:content/c:messages/c:messages/c:message">
          <li>
            <xsl:value-of select="."/>
            <br/>
          </li>
        </xsl:for-each>
      </ul>
    </xsl:if>
  </xsl:template>

  <xsl:template name="advancedsearch"> <!-- I have no idea why it didn't work to have a normal node-based template -->
    <xsl:copy-of select="c:page/c:advancedsearch/*"/>
  </xsl:template>

  <xsl:template match="/">

    <html>
      <xsl:call-template name="head">
	<xsl:with-param name="title">
	  <xsl:choose>
	    <xsl:when test="c:page/c:mode = 'topic'">
	      <xsl:value-of
		  select="c:page/c:navigation/rdf:RDF/skos:Concept/skos:prefLabel[@xml:lang=$interface-language]"/>
	      <xsl:if test="c:page/c:navigation/rdf:RDF/skos:Concept/skos:altLabel[@xml:lang=$interface-language]">
		<xsl:text>/</xsl:text>
		<xsl:value-of
		    select="c:page/c:navigation/rdf:RDF/skos:Concept/skos:altLabel[@xml:lang=$interface-language]"/>
	      </xsl:if>

	    </xsl:when>
	    <xsl:when test="c:page/c:mode = 'resource'">
	      <xsl:value-of select="c:page/c:result-list/rdf:RDF/sub:Resource/dct:title"/>
	    </xsl:when>
	  </xsl:choose>
	</xsl:with-param>
     <xsl:with-param name="baseurl" select="$baseurl"/>
      </xsl:call-template>

<body>

	<xsl:value-of select="$querystring"/>
	<xsl:call-template name="headers">
	  <xsl:with-param name="baseurl" select="$baseurl"/>
	</xsl:call-template>
	<div class="spacer">&#160;</div>
	<div class="spacer">
		<div id="contentLeftMiddleHeader">
			<div id="contentLeftHeader">
					&#160;
			</div>
			<div id="contentMiddleHeader">
				<!-- A-Z -->
					<div id="panel-az">
						<xsl:apply-templates select="c:page" mode="a-z"/>
					</div>
			</div>
		</div>
		<div id="contentRightHeader">
				&#160;
		</div>
	</div>
	<div id="content">
	  <div id="colmidleft">
	    <div id="colleft">
				<!-- ######################################################################
	     LEFT COLUMN (col2)

	     contains: facets
	     ###################################################################### -->
				<div class="col2">
					<!-- Facets
    	  precondition:
	        page is topic or search-result
	        there is at least one hit
		    facets exists in the results (c:/page/c:facets exists in the XML)
		-->
					<xsl:if test="c:page/c:mode = 'topic' or c:page/c:mode = 'search-result'">
						<xsl:if test="$numberofhits &gt; 0">
							<xsl:if test="c:page/c:facets">
								<div id="panel-facets">
									<!-- <h3><i18n:text key="facets.heading">Velg avgrensning</i18n:text></h3> -->
									<xsl:apply-templates select="c:page/c:result-list/rdf:RDF" mode="facets"/>
								</div>
							</xsl:if>
						</xsl:if>
					</xsl:if>
					<xsl:text> </xsl:text>
				</div>
				&#160;
				<!-- Column 2 end -->
			</div>
			<div id="colmid">
				<!-- ######################################################################
     CENTER COLUMN (col1)
	 contains: search panel, topic description panel, resource hits
	 ###################################################################### -->
				<div id="innerMidCol" class="col1" style="border:0px dotted red;">

					<!--xsl:call-template name="debug"/-->

					<!-- Search -->
					<!-- Search is shown when advanced search is not, and when there are no static content -->
					<xsl:if test="not(c:page/c:advancedsearch)">

						<div id="panel-search" style="border:0px solid brown">
							<div id="searchBox">
								<xsl:call-template name="autocompletion">
									<xsl:with-param name="baseurl">
										<xsl:value-of select="$baseurl"/>
									</xsl:with-param>
									<xsl:with-param name="interface-language">
										<xsl:value-of select="$interface-language"/>
									</xsl:with-param>
								</xsl:call-template>

								<form action="{$baseurl}/search-result.html" method="get">

									<fieldset>
										<input type="hidden" name="prefix" value="dct: &lt;http://purl.org/dc/terms/&gt;"/>
										<input type="hidden" name="prefix" value="foaf: &lt;http://xmlns.com/foaf/0.1/&gt;"/>
										<input type="hidden" name="prefix" value="sub: &lt;http://xmlns.computas.com/sublima#&gt;"/>
										<input type="hidden" name="prefix" value="rdfs: &lt;http://www.w3.org/2000/01/rdf-schema#&gt;"/>
										<input type="hidden" name="prefix" value="wdr: &lt;http://www.w3.org/2007/05/powder#&gt;"/>
										<input type="hidden" name="prefix" value="skos: &lt;http://www.w3.org/2004/02/skos/core#&gt;"/>
										<input type="hidden" name="prefix" value="pf: &lt;http://jena.hpl.hp.com/ARQ/property#&gt;"/>
										<xsl:call-template name="hidden-locale-field"/>
										<input type="hidden" name="wdr:describedBy" value="http://sublima.computas.com/status/godkjent_av_administrator"/>


										<input id="keyword" class="searchbox" type="text"
										 name="searchstring" value="{c:page/c:searchparams/c:searchparams/c:searchstring}"/>
										<input type="submit" id="btnSearch" value="search.submit" i18n:attr="value"/>
										<a href="{$baseurl}/advancedsearch{$qloc}">
											<i18n:text key="menu.advancedsearch">Avansert søk</i18n:text>
										</a>
										<br/>
										<br/>

										<xsl:choose>
											<xsl:when test="c:page/c:searchparams/c:searchparams/c:operator = 'OR'">
												<input type="radio" class="radio" name="booleanoperator" value="AND"/>
												<i18n:text key="search.boolean.and">og</i18n:text>
												<input type="radio" class="radio" name="booleanoperator" value="OR" checked="checked"/>
												<i18n:text key="search.boolean.or">eller</i18n:text>
											</xsl:when>
											<xsl:otherwise>
												<input type="radio" class="radio" name="booleanoperator" value="AND" checked="checked"/>
												<i18n:text key="search.boolean.and">og</i18n:text>
												<input type="radio" class="radio" name="booleanoperator" value="OR" />
												<i18n:text key="search.boolean.or">eller</i18n:text>
											</xsl:otherwise>
										</xsl:choose>
										<!-- sorting panel -->
										<!-- updates and submits the search form -->
										<span id="sortingpanel">
											<i18n:text key="search.sortby">Sorter etter</i18n:text>

											<select id="sort" name="sort">
												<option value="title">
													<xsl:if test="c:page/c:searchparams/c:searchparams/c:sortby = 'title'">
														<xsl:attribute name="selected">selected</xsl:attribute>
													</xsl:if>
													<i18n:text key="title">Tittel</i18n:text>
												</option>
												<option value="dateAccepted">
													<xsl:if test="c:page/c:searchparams/c:searchparams/c:sortby = 'dateAccepted'">
														<xsl:attribute name="selected">selected</xsl:attribute>
													</xsl:if>
													<i18n:text key="search.sortby.date">Dato</i18n:text>
												</option>
											</select>
										</span>
										<br/>
										<xsl:choose>
											<xsl:when test="c:page/c:searchparams/c:searchparams/c:exactmatch = 'exactmatch'">
												<input type="checkbox" class="radio" name="exactmatch" value="exactmatch" checked="checked"/>
											</xsl:when>
											<xsl:otherwise>
												<input type="checkbox" class="radio" name="exactmatch" value="exactmatch"/>
											</xsl:otherwise>
										</xsl:choose>
										<i18n:text key="search.exactmatch">Eksakt ord</i18n:text>

										<!--xsl:choose>
										<xsl:when test="c:page/c:searchparams/c:searchparams/c:deepsearch = 'deepsearch'">
											<input type="checkbox" name="deepsearch" value="deepsearch" checked="checked"/>
										</xsl:when>
										<xsl:otherwise>
											<input type="checkbox" name="deepsearch" value="deepsearch"/>
										</xsl:otherwise>
									</xsl:choose>
									<i18n:text key="search.externalresources">Inkluder søk i ressursinnhold</i18n:text>
									<br/-->



									</fieldset>
								</form>
							</div>
						</div>
						<!--
		  Link to RSS representation of search result
		  precondition:
		    more than zero hits
		    rss-url has been created
		-->

						<div id="panel-rss">
							<xsl:if test="$numberofhits &gt; 0">
								<xsl:if test="not($rss-url = '')">
									<a>
										<xsl:attribute name="href">
											<xsl:value-of select="$rss-url"/>
										</xsl:attribute>
										<img alt="RSS" class="rssImg"  src="{$baseurl}/images/rss.gif"/>
									</a>
									<br/>
								</xsl:if>
							</xsl:if>
							<xsl:text> </xsl:text>
							<!-- avoid empty div tags -->
						</div>
						<br/>
						<br/>
						<br/>
					</xsl:if>

					<!-- xsl:call-template name="messages"/ -->

                    <xsl:if test="c:page/c:navigation/rdf:RDF/skos:Concept/@rdf:about and c:page/c:mode = 'topic'">

								<div id="topicdescription">
									<!--h3><i18n:text key="topic.heading">Emne</i18n:text></h3-->
									<h3>
										<xsl:value-of select="/c:page/c:navigation/rdf:RDF/skos:Concept/skos:prefLabel[@xml:lang=$interface-language]"/>
										<xsl:if test="/c:page/c:loggedin = 'true'">
											- <a href="{$baseurl}/admin/emner/emne?uri={/c:page/c:navigation/rdf:RDF/skos:Concept/@rdf:about}{$aloc}">[Edit]</a>
										</xsl:if>
									</h3>
									<xsl:if test="/c:page/c:navigation/rdf:RDF/skos:Concept/skos:altLabel[@xml:lang=$interface-language]">
										<p>
											<xsl:text>(</xsl:text>
											<xsl:for-each select="/c:page/c:navigation/rdf:RDF/skos:Concept/skos:altLabel[@xml:lang=$interface-language]">
												<xsl:value-of select="."/>
												<xsl:if test="position() !=last()">
													<xsl:text>, </xsl:text>
												</xsl:if>
											</xsl:for-each>
											<xsl:text>)</xsl:text>
										</p>
									</xsl:if>
									<p>
										<xsl:value-of disable-output-escaping="yes" select="/c:page/c:navigation/rdf:RDF/skos:Concept/skos:definition"/>
									</p>
								</div>
							</xsl:if>



					<!--
         Number of hits
            precondition:
             page is topic, or search-result

        -->
					<xsl:if test="c:page/c:mode = 'topic' or c:page/c:mode = 'search-result'">
					<div id="resourceHeading">
						<div id="numberOfHits">
							<xsl:if test="c:page/c:mode = 'topic' or c:page/c:mode = 'search-result'">
                                <xsl:choose>
                                    <xsl:when test="$numberofhits = 0">
                                        <i18n:text key="search.numberofhits">Antall treff</i18n:text>: -<br/>
                                    </xsl:when>
                                    <xsl:when test="not($numberofhits = 0 and c:page/c:navigation/rdf:RDF//skos:Concept)">
                                        <i18n:text key="search.numberofhits">Antall treff</i18n:text>: <xsl:value-of select="$numberofhits"/><br/>
                                    </xsl:when>
                                </xsl:choose>
							</xsl:if>
							<xsl:text> </xsl:text>
							<!-- avoid empty div tags -->
						</div>
						<div id="describtion">
							<xsl:if test="c:page/c:mode = 'topic' or c:page/c:mode = 'search-result'">
								<xsl:if test="$numberofhits &gt; 0">
									<xsl:choose>
										<xsl:when test="$res-view='full'">
											<a>
												<xsl:attribute name="href">
													<xsl:choose>
														<xsl:when test="not(contains(/c:page/c:facets/c:request/@requesturl, '?'))">
															<xsl:value-of select="concat($gen-req, '?res-view=medium')"/>
														</xsl:when>
														<xsl:otherwise>
															<xsl:value-of select="concat($gen-req, '&amp;res-view=medium')"/>
														</xsl:otherwise>
													</xsl:choose>
												</xsl:attribute>
												<i18n:text key="mediumdescription">medium description</i18n:text>
											</a>
										</xsl:when>

										<xsl:when test="$res-view='short'">

										</xsl:when>

										<xsl:otherwise>
											<a>
												<xsl:attribute name="href">
													<xsl:choose>
														<xsl:when test="not(contains(/c:page/c:facets/c:request/@requesturl, '?'))">
															<xsl:value-of select="concat($gen-req, '?res-view=full')"/>
														</xsl:when>
														<xsl:otherwise>
															<xsl:value-of select="concat($gen-req, '&amp;res-view=full')"/>
														</xsl:otherwise>
													</xsl:choose>
												</xsl:attribute>
												<i18n:text key="fulldescription">full description</i18n:text>
											</a>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:if>
							</xsl:if>
							<xsl:text> </xsl:text>
							<!-- avoid empty results -->
							&#160;
						</div>
						<div class="clearer">&#160;</div>
					</div>
					</xsl:if>
					<!--
		 Advanced search
		    Her kommer avansert søk dersom denne er angitt
		-->
					<xsl:apply-templates select="c:page/c:advancedsearch" mode="advancedsearch"/>


					<!--
		 Tips
		  precondition:
		  vises dersom brukeren har valgt den
		-->
					<xsl:apply-templates select="c:page/c:tips" mode="form"/>

                     <xsl:apply-templates select="c:page/c:nyeste" mode="list"/>


					<!--
		 Login
		  precondition:
		-->
					<xsl:apply-templates select="c:page/c:login" mode="login"/>

					<!-- Static content -->
					<xsl:if test="/c:page/c:static">
						<div>
							<xsl:apply-templates select="/c:page/c:static"/>
						</div>
					</xsl:if>


					<!--
		 Resource Description (details)
		     precondition:
		      page is 'resource'
		-->
					<div id="panel-resource-details" style="border:0px solid pink;">
						<xsl:if test="c:page/c:mode = 'resource'">
							<xsl:apply-templates select="c:page/c:result-list/rdf:RDF" mode="resource"/>
						</xsl:if>
						<xsl:text> </xsl:text>
						<!-- avoid empty div -->
					</div>


					<!--
		 Topic description (details)
		     precondition:
		      page is 'browse'
		-->
					<div id="panel-topic-details" style="border:0px solid purple;">
						<xsl:if test="c:page/c:mode = 'browse'">
							<xsl:apply-templates select="c:page/c:browse" mode="browse"/>
						</xsl:if>
						<xsl:text> </xsl:text>
						<!-- avoid empty div -->
					</div>


					<!--
	    No-hits
		 precondition:
		   page is topic or search-result
		   no hits
		-->
					<div id="panel-zero-hits" style="1px solid brown;">
						<xsl:if test="c:page/c:mode = 'topic' or c:page/c:mode = 'search-result'">

							<xsl:if test="$numberofhits &lt; 1 and not(c:page/c:navigation/rdf:RDF//skos:Concept)">
								<br/>
								<!-- Generer et bibsys-søk -->
								<a>
									<xsl:attribute name="href">
										<xsl:text>http://ask.bibsys.no/ask/action/result?cmd=&amp;kilde=biblio&amp;q=</xsl:text>
										<xsl:value-of select="c:page/c:facets/c:request/c:param[@key='searchstring']/c:value"/>
									</xsl:attribute>
									<i18n:text key="search.for">Søk etter</i18n:text>
									<xsl:text> '</xsl:text>
									<xsl:value-of select="c:page/c:facets/c:request/c:param[@key='searchstring']/c:value"/>
									<xsl:text>' i Bibsys</xsl:text>
								</a>
								<br/>
								<!-- Generer et google scholar søk -->
								<a>
									<xsl:attribute name="href">
										<xsl:text>http://scholar.google.com/scholar?hl=en&amp;btnG=Search&amp;as_sdt=1%2C5&amp;as_sdtp=on&amp;q=</xsl:text>
										<xsl:value-of select="c:page/c:facets/c:request/c:param[@key='searchstring']/c:value"/>
									</xsl:attribute>
									<i18n:text key="search.for">Søk etter</i18n:text>
									<xsl:text> '</xsl:text>
									<xsl:value-of select="c:page/c:facets/c:request/c:param[@key='searchstring']/c:value"/>
									<xsl:text>' i Google Scholar</xsl:text>
								</a>
								<br/>
								<!-- Generer et google søk -->
								<a>
									<xsl:attribute name="href">
										<xsl:text>http://no.wikipedia.org/w/index.php?title=Spesial%3ASearch&amp;search=</xsl:text>
										<xsl:value-of select="c:page/c:facets/c:request/c:param[@key='searchstring']/c:value"/>
										<xsl:text>&amp;fulltext=Søk</xsl:text>
									</xsl:attribute>
									<i18n:text key="search.for">Søk etter</i18n:text>
									<xsl:text> '</xsl:text>
									<xsl:value-of select="c:page/c:facets/c:request/c:param[@key='searchstring']/c:value"/>
									<xsl:text>' </xsl:text>
									<i18n:text key="in.wikipedia">i Wikipedia</i18n:text>
								</a>
								<br/>
								<!-- Generer et SNL-søk -->
								<a>
									<xsl:attribute name="href">
										<xsl:text>http://www.snl.no/.search?query=</xsl:text>
										<xsl:value-of select="c:page/c:facets/c:request/c:param[@key='searchstring']/c:value"/>
									</xsl:attribute>
									<i18n:text key="search.for">Søk etter</i18n:text>
									<xsl:text> '</xsl:text>
									<xsl:value-of select="c:page/c:facets/c:request/c:param[@key='searchstring']/c:value"/>
									<xsl:text>' </xsl:text>
									<i18n:text key="in.snl">i Store norske leksikon</i18n:text>
								</a>
								<br/>
							</xsl:if>
						</xsl:if>
						<xsl:text> </xsl:text>
						<!-- avoid empty div tags -->
					</div>
					<!-- no hits -->



					<!--
		 Search results
		  precondition:
		   page-mode is topic or search-results
		   hits is more than 0
		-->
					<xsl:if test="c:page/c:mode = 'topic' or c:page/c:mode = 'search-result'">
					<div id="panel-results">
                        <xsl:call-template name="messages"/>

							<xsl:if test="$numberofhits &gt; 0">

								<!--h3>
									<i18n:text key="resources.heading">Ressurser</i18n:text>
								</h3-->

								<xsl:choose>
									<xsl:when test="$res-view='full'">

										<xsl:apply-templates select="c:page/c:result-list/rdf:RDF" mode="results-full">
											<xsl:with-param name="sorting">
												<xsl:value-of select="$sortby"/>
											</xsl:with-param>
										</xsl:apply-templates>
									</xsl:when>

									<xsl:when test="$res-view='short'">

										<xsl:apply-templates select="c:page/c:result-list/rdf:RDF" mode="results-short">
											<xsl:with-param name="sorting">
												<xsl:value-of select="$sortby"/>
											</xsl:with-param>
										</xsl:apply-templates>
									</xsl:when>

									<xsl:otherwise>
										<xsl:apply-templates select="c:page/c:result-list/rdf:RDF" mode="results">
											<xsl:with-param name="sorting">
												<xsl:value-of select="$sortby"/>
											</xsl:with-param>
										</xsl:apply-templates>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:if>

						<xsl:text> </xsl:text>
						<!-- avoid empty results -->
					</div>
					</xsl:if>
					<!-- panel-results-->

					<!--
        If there are 0 hits in resources, but we have hits in the topics
     -->
					<xsl:if test="$numberofhits = 0 and c:page/c:navigation/rdf:RDF//skos:Concept and c:page/c:mode = 'search-result' and c:page/c:abovemaxnumberofhits = 'true'">
						<ul>
							<li>
								<i18n:text key="search.refine">Søket ditt gir for mange treff i ressurser. Velg eventuelt heller et emne fra navigasjonslisten til høyre eller forsøk å søke på mer enn ett ord.</i18n:text>
							</li>
						</ul>
					</xsl:if>

          <xsl:if test="$numberofhits = 0 and not(c:page/c:navigation/rdf:RDF//skos:Concept) and c:page/c:mode = 'search-result' and c:page/c:abovemaxnumberofhits = 'true'">
						<ul>
							<li>
								<i18n:text key="search.refine.notopics">Søket ditt gir for mange treff i ressurser. Velg eventuelt heller et emne fra navigasjonslisten til høyre eller forsøk å søke på mer enn ett ord.</i18n:text>
							</li>
						</ul>
					</xsl:if>

					<xsl:if test="$numberofhits = 0 and c:page/c:navigation/rdf:RDF//skos:Concept and c:page/c:mode = 'search-result' and c:page/c:abovemaxnumberofhits = 'false'">
						<ul>
							<li>
								<i18n:text key="search.nohits">Dette søket ga ingen treff i ressurser. Treff i emner vises eventuelt i navigasjonslisten til høyre.</i18n:text>
							</li>
						</ul>
					</xsl:if>

					<!--
      If we came here from clicking a specific topic
      -->
					<xsl:if test="$numberofhits = 0 and c:page/c:navigation/rdf:RDF//skos:Concept and c:page/c:mode = 'topic'">
						<ul>
							<li>
								<i18n:text key="search.noresources">Vi har ikke registrert noen ressurser med dette emnet, men relaterte emner finnes eventuelt i listen til høyre.</i18n:text>
							</li>
						</ul>
					</xsl:if>

				</div>
			</div>
	  </div>
		<div id="colright">
			<!-- ######################################################################
	     RIGHT COLUMN (col3)

	     contains:
	     ###################################################################### -->
			<div class="col3" style="border:0px dotted green;">


				<!-- Navigation
		  precondition:
		   page is either topic or search result
		-->

				<!--when one topic is in focus-->
				<div id="panel-nav" style="border:0px solid black">
					<xsl:if test="c:page/c:mode = 'topic'">
					  <xsl:apply-templates select="c:page/c:navigation/rdf:RDF/skos:Concept"/>

							<xsl:text> </xsl:text>
							<!-- avoid empty div -->

                            <!-- Videresøk for SMIL -->
                        <!--div class="panel-tasks">
                            <h2><i18n:text key="videresok">Videresøk</i18n:text></h2>

                            <xsl:choose>
                                <xsl:when test="$interface-language = 'no'">
                                    <a href="http://no.wikipedia.org/wiki/{/c:page/c:navigation/rdf:RDF/skos:Concept/skos:prefLabel[@xml:lang=$interface-language]}" title="Wikipedia">Wikipedia</a><br/>
                                    <a href="http://www.snl.no/.search?query={/c:page/c:navigation/rdf:RDF/skos:Concept/skos:prefLabel[@xml:lang=$interface-language]}" title="Store norske leksikon">Store norske leksikon</a><br/>
                                    <a href="http://www.pasienthandboka.no/default.asp?mode=search&amp;searchstring={/c:page/c:navigation/rdf:RDF/skos:Concept/skos:prefLabel[@xml:lang=$interface-language]}" title="Pasienthåndboka">Pasienthåndboka</a><br/>
                                    <a href="http://www.ub.uio.no/umh/bibsys_sok/?run={/c:page/c:navigation/rdf:RDF/skos:Concept/skos:prefLabel[@xml:lang=$interface-language]}" title="Bøker og artikler">Bøker og artikler</a>
                               </xsl:when>

                               <xsl:when test="$interface-language = 'sv'">
                                    <a href="http://sv.wikipedia.org/wiki/{/c:page/c:navigation/rdf:RDF/skos:Concept/skos:prefLabel[@xml:lang=$interface-language]}" title="Wikipedia">Wikipedia</a>
                               </xsl:when>

                                <xsl:when test="$interface-language = 'da'">
                                    <a href="http://da.wikipedia.org/wiki/{/c:page/c:navigation/rdf:RDF/skos:Concept/skos:prefLabel[@xml:lang=$interface-language]}" title="Wikipedia">Wikipedia</a><br/>
                                    <a href="https://www.sundhed.dk/Soeg.aspx?SoegeOrd={/c:page/c:navigation/rdf:RDF/skos:Concept/skos:prefLabel[@xml:lang=$interface-language]}" title="Sundhed.dk">Sundhed.dk</a>
                               </xsl:when>
                            </xsl:choose>
                        </div-->


					</xsl:if>

					<!--more than one topic-->

					<xsl:if test="c:page/c:mode = 'search-result'">
						<xsl:if test="c:page/c:navigation/rdf:RDF//skos:Concept">
							<h2 class="subHeading">
								<i18n:text key="topics">Emner</i18n:text>
							</h2>
							<ul>
								<xsl:for-each select="c:page/c:navigation/rdf:RDF//skos:Concept">
									<xsl:sort lang="{$interface-language}" select="skos:prefLabel[@xml:lang=$interface-language]"/>
									<li>
										<a href="{./@rdf:about}.html{$qloc}">
											<xsl:value-of select="./skos:prefLabel[@xml:lang=$interface-language]"/>
										</a>
									</li>
								</xsl:for-each>
							</ul>
						</xsl:if>
					</xsl:if>
					<xsl:text> </xsl:text>
					<!-- avoid empty div -->
				</div>


				<!--
         Tasks
          precondition:
            none
        -->
				<div id="panel-tasks">
					<xsl:choose>
						<xsl:when test="c:page/c:loggedin = 'true' ">
							<h3 style="font-size: 25px;">
								<i18n:text key="menu.adminMeny">Meny</i18n:text>
							</h3>
							<a href="{$baseurl}/admin/emner/{$qloc}">
								<i18n:text key="menu.topic">Emner</i18n:text>
							</a>
							<br/>
							<a href="{$baseurl}/admin/ressurser/{$qloc}">
								<i18n:text key="menu.resource">Ressurser</i18n:text>
							</a>
							<br/>
							<a href="{$baseurl}/admin/brukere/{$qloc}">
								<i18n:text key="menu.user">Brukere</i18n:text>
							</a>
							<br/>
							<a href="{$baseurl}/admin/utgivere/{$qloc}">
								<i18n:text key="menu.publisher">Utgivere</i18n:text>
							</a>
							<br/>
							<a href="{$baseurl}/admin/lenkesjekk/{$qloc}">
								<i18n:text key="menu.link">Lenkesjekk</i18n:text>
							</a>
							<br/>
							<a href="{$baseurl}/admin/database/{$qloc}">
								<i18n:text key="menu.database">Database</i18n:text>
							</a>
							<br/>
							<a href="/stats/">
								<i18n:text key="menu.stats">Statistikk</i18n:text>
							</a>
							<br/>
							<br/>
							<a href="{$baseurl}/do-logout{$qloc}">
								<i18n:text key="admin.logout">Logg ut</i18n:text>
							</a>
							<br/>
						</xsl:when>
					</xsl:choose>
					&#160;
				</div>



			</div><!-- Column 3 end -->
			&#160;
		</div>
		<div class="clearer">&#160;</div>
	</div> <!-- three column layout end -->
	<div class="clearer">&#160;</div>

<!-- UiO Page footer starts -->
<div id="app-footer-wrapper"> 
   <div id="app-footer">
     <div id="app-responsible"> 
       <span class="vrtx-label">Ansvarlig for denne tjenesten</span> 
       <span><a href="http://www.ub.uio.no/fag/jus/jus/">Juridisk bibliotek</a></span> 
     </div>
     <div id="contact-info"> 
       <div class="phone-fax-email"> 
         <span class="vrtx-label">Kontaktinformasjon</span>
         <span class="email">E-post: <a href="mailto:ujur-nettred@ub.uio.no">ujur-nettred@ub.uio.no</a></span>  
       </div> 
     </div>
   </div> 
</div>
<!-- UiO Page footer end -->


<!-- Original sublima footer
	<div id="footer">
		<div id="leftFooter">
			<p>
				Sublima kontaktinfomasjon, Adresse, telfonnummer, faks, mail adresse etc...
			</p>
		</div>
		<div id="rightFooter">
			<p>
				<i18n:text key="sublima.footer">
					An Open Source Software Project supported by
					<a href="http://www.abm-utvikling.no/">ABM Utvikling</a>
					and
					<a href="http://www.computas.com/">Computas AS</a>
					, 2008
				</i18n:text>
				(Sublima 1.1.2)
			</p>
		</div>
		<div class="clearer">&#160;</div>
	</div>
-->

      </body>
    </html>
  </xsl:template>


</xsl:stylesheet>
