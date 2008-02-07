<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" 
  xmlns:dct="http://purl.org/dc/terms/" 
  xmlns:foaf="http://xmlns.com/foaf/0.1/" 
  xmlns:sub="http://xmlns.computas.com/sublima#"
  xmlns:sioc="http://rdfs.org/sioc/ns#"
  xmlns:od="http://sublima.computas.com/topic/" 
  xmlns:lingvoj="http://www.lingvoj.org/ontology#"
  xmlns:wdr="http://www.w3.org/2007/05/powder#"
  xmlns="http://www.w3.org/1999/xhtml" 
  exclude-result-prefixes="rdf rdfs dct foaf sub sioc od lingvoj wdr"
  >


  <xsl:template match="foaf:Agent|foaf:Person|foaf:Group|foaf:Organization" mode="external-link">
    <xsl:choose>
      <xsl:when test="foaf:homepage and foaf:name/@xml:lang=$interface-language">
	<a href="{foaf:homepage/@rdf:resource}"><xsl:value-of select="foaf:name"/></a>
      </xsl:when>
      <xsl:otherwise>
	<xsl:value-of select="foaf:name[@xml:lang=$interface-language]"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="dct:title" mode="internal-link">
    <xsl:if test="@xml:lang=$interface-language">
      <a href="{../dct:identifier/@rdf:resource}"><xsl:value-of select="."/></a>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="dct:description">
    <xsl:if test="@xml:lang=$interface-language">
      <xsl:copy-of select="node()"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="sub:committer">
    <xsl:value-of select="./sioc:User/rdfs:label[@xml:lang=$interface-language]"/>
  </xsl:template>
 
  <xsl:template match="dct:dateAccepted|dct:dateSubmitted">
    <xsl:value-of select="substring-before(., 'T')"/>
  </xsl:template>

  <xsl:template match="dct:subject">
    <a href="{./*/@rdf:about}"><xsl:value-of select="./*/rdfs:label[@xml:lang=$interface-language]"/></a>
  </xsl:template>

  <xsl:template match="sub:Audience">
    <xsl:value-of select="./rdfs:label[@xml:lang=$interface-language]"/>
  </xsl:template>

  <xsl:template match="dct:language">
    <xsl:value-of select="./lingvoj:Lingvo/rdfs:label[@xml:lang=$interface-language]"/>
  </xsl:template>

  <!-- There are two ways to do this; either address a literal, which
       is in the model, or hardcode a value in the XSLT. In most
       cases, we want to keep the values in the database, but there
       might be exceptions. The status of the resource and the type of
       the resource might be such exceptions. In particular, in cases
       where the search interface consists of a drop-down, it has
       performance benefits and makes the query simpler and we don't
       have to search a literal, when the URI rather than the literal
       is searched for. -->

  <xsl:template match="wdr:describedBy">
    <xsl:choose>
      <xsl:when test="@rdf:resource='http://sublima.computas.com/status/approved'">
	<xsl:choose>
	  <xsl:when test="$interface-language='en'">Approved</xsl:when>
	  <xsl:when test="$interface-language='no'">Godkjent</xsl:when>
	</xsl:choose>
      </xsl:when>
      <xsl:when test="@rdf:resource='http://sublima.computas.com/status/suggested'">
	<xsl:choose>
	  <xsl:when test="$interface-language='en'">Suggested</xsl:when>
	  <xsl:when test="$interface-language='no'">Foreslått</xsl:when>
	</xsl:choose>
      </xsl:when>
	
    </xsl:choose>
  </xsl:template>

  <xsl:template match="dct:type">
    <xsl:choose>
      <xsl:when test="@rdf:resource='http://purl.org/dc/dcmitype/Text'">
	<xsl:choose>
	  <xsl:when test="$interface-language='en'">Text</xsl:when>
	  <xsl:when test="$interface-language='no'">Tekst</xsl:when>
	</xsl:choose>
      </xsl:when>
      <xsl:when test="@rdf:resource='http://purl.org/dc/dcmitype/Image'">
	<xsl:choose>
	  <xsl:when test="$interface-language='en'">Image</xsl:when>
	  <xsl:when test="$interface-language='no'">Bilde</xsl:when>
	</xsl:choose>
      </xsl:when>
	
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>