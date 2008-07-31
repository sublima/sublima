<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" 
  xmlns:dct="http://purl.org/dc/terms/" 
  xmlns:foaf="http://xmlns.com/foaf/0.1/" 
  xmlns:sub="http://xmlns.computas.com/sublima#"
  xmlns:sioc="http://rdfs.org/sioc/ns#"
  xmlns:lingvoj="http://www.lingvoj.org/ontology#"
  xmlns:skos="http://www.w3.org/2004/02/skos/core#"
  xmlns:wdr="http://www.w3.org/2007/05/powder#"
  xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
  xmlns="http://www.w3.org/1999/xhtml" 
  exclude-result-prefixes="rdf rdfs dct foaf sub sioc lingvoj wdr skos"
  >


  <xsl:template match="foaf:Agent|foaf:Person|foaf:Group|foaf:Organization" mode="external-link">
    <xsl:choose>
      <xsl:when test="foaf:homepage">
	<a href="{foaf:homepage/@rdf:resource}"><xsl:value-of select="foaf:name"/></a>
      </xsl:when>
      <xsl:otherwise>
	<xsl:value-of select="foaf:name"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="dct:title" mode="internal-link">
    <a href="{../dct:identifier/@rdf:resource}{$qloc}"><xsl:value-of select="."/></a>
  </xsl:template>

  <xsl:template match="dct:title" mode="external-link">
    <a href="{../@rdf:about}"><xsl:value-of select="."/></a>
  </xsl:template>
  
  <xsl:template match="dct:description">
    <xsl:value-of select="." disable-output-escaping="yes"/>
  </xsl:template>

  <xsl:template match="sub:committer">
    <xsl:value-of select="./sioc:User/rdfs:label"/>
  </xsl:template>
  
  <xsl:template match="dct:audience">
    <xsl:value-of select="./dct:AgentClass/rdfs:label"/>
  </xsl:template>

  <xsl:template match="dct:dateAccepted|dct:dateSubmitted">
    <xsl:value-of select="substring-before(., 'T')"/>
  </xsl:template>

  <xsl:template match="dct:publisher">
   <xsl:choose>
      <xsl:when test="./@rdf:resource">
	<xsl:variable name="uri" select="./@rdf:resource"/>
	<xsl:apply-templates select="//foaf:*[@rdf:about=$uri]" mode="external-link"/>
      </xsl:when>
      <xsl:otherwise>
	<xsl:apply-templates select="./foaf:*" mode="external-link" />
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text> </xsl:text>
  </xsl:template>


  <!-- The following fields are meant to be in available in all
       languages that has an interface, thus, we need to check the
       interface language here. -->

  <xsl:template match="dct:subject">
   <xsl:choose>
      <xsl:when test="./@rdf:resource">
	<xsl:variable name="uri" select="./@rdf:resource"/>
	<a href="{$uri}{$qloc}"><xsl:value-of select="//skos:Concept[@rdf:about=$uri]/skos:prefLabel[@xml:lang=$interface-language]"/></a>
      </xsl:when>
      <xsl:when test="./skos:Concept/@rdf:about">
	<a href="{./skos:Concept/@rdf:about}{$qloc}"><xsl:value-of select="./skos:Concept/skos:prefLabel[@xml:lang=$interface-language]"/></a>
      </xsl:when>
      <xsl:otherwise>
	<span class="warning"><i18n:text key="validation.topic.notitle">Emnet mangler tittel</i18n:text></span>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:if test="position() != last()">
      <xsl:text>, </xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="sub:Audience">
    <xsl:value-of select="./rdfs:label[@xml:lang=$interface-language]"/>
  </xsl:template>

  <xsl:template match="dct:language">
    <xsl:value-of select="./lingvoj:Lingvo/rdfs:label[@xml:lang=$interface-language]"/>
  </xsl:template>

  <xsl:template match="wdr:describedBy">
    <xsl:value-of select="./rdf:Description/rdfs:label[@xml:lang=$interface-language]"/>
  </xsl:template>

  <xsl:template match="dct:format">
    <xsl:value-of select="./dct:MediaType/rdfs:label[@xml:lang=$interface-language]"/>

    <xsl:if test="position() != last()">
      <xsl:text>, </xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="foaf:Agent|foaf:Person|foaf:Group|foaf:Organization" mode="edit">
    <input type="hidden" name="uri" value="{./@rdf:about}" />

    <tr>
      <td><i18n:text key="language">Språk</i18n:text></td>
      <td><i18n:text key="name">Navn</i18n:text></td>
    </tr>
    <xsl:for-each select="./foaf:name">
      <tr>
        <td>
          <label for="foaf:Agent/foaf:name@{@xml:lang}"><xsl:value-of select="@xml:lang" /></label>
        </td>
        <td>
          <input id="foaf:Agent/foaf:name@{@xml:lang}" type="text" name="foaf:Agent/foaf:name@{@xml:lang}" size="40">
           <xsl:attribute name="value">
            <xsl:value-of select="."/>
           </xsl:attribute>
          </input>
        </td>
      </tr>
      
    </xsl:for-each>
    <tr>
      <td>
        <input id="new_lang" type="text" name="new_lang" size="5" />
      </td>
      <td>
        <input id="new_name" type="text" name="new_name" size="40" />  
      </td>
    </tr>

    <tr>
        <td>
            <label for="dct:description"><i18n:text key="description">Beskrivelse</i18n:text></label>
        </td>
        <td>
            <textarea id="dct:description" name="dct:description" rows="6" cols="40"><xsl:value-of select="./dct:description"/>...</textarea>
        </td>
    </tr>

  </xsl:template>

</xsl:stylesheet>