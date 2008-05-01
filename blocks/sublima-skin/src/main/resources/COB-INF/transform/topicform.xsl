<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
        xmlns:lingvoj="http://www.lingvoj.org/ontology#"
        xmlns:wdr="http://www.w3.org/2007/05/powder#"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
        xmlns:c="http://xmlns.computas.com/cocoon"
        xmlns:skos="http://www.w3.org/2004/02/skos/core#"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:dct="http://purl.org/dc/terms/"
        xmlns:sub="http://xmlns.computas.com/sublima#"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:sparql="http://www.w3.org/2005/sparql-results#"
        xmlns="http://www.w3.org/1999/xhtml"
        version="1.0">
  <xsl:import href="rdfxml2xhtml-deflist.xsl"/>
  <xsl:param name="baseurl"/>
    <xsl:param name="interface-language">no</xsl:param>

    <xsl:template match="c:topic" mode="topicedit">

        <form name="new_resource" action="{$baseurl}/admin/emner/emne" method="POST">
            <input type="hidden" name="uri" value="{./c:topicdetails/rdf:RDF/sub:Resource/dct:subject/skos:Concept/@rdf:about}"/>
            <table>
                <tr>
                    <td>
                        <label for="dct:subject/skos:Concept/rdfs:label">Tittel</label>
                    </td>
                    <td>
                        <input id="dct:subject/skos:Concept/rdfs:label" type="text" name="dct:subject/skos:Concept/rdfs:label" size="40" value="{./c:topicdetails/rdf:RDF/sub:Resource/dct:subject/skos:Concept/rdfs:label}"/>
                    </td>
                </tr>
              </table>
              <p>Relaterte emner</p>
              <table>
                <tr>
                    <td>
                        <label for="dct:subject/skos:Concept/skos:broader/rdf:resource">Bredere</label>
                    </td>
                    <td>
                        <select id="dct:subject/skos:Concept/skos:broader/rdf:resource" name="dct:subject/skos:Concept/skos:broader/rdf:resource" multiple="multiple">
                            <xsl:for-each select="./c:alltopics/rdf:RDF/skos:Concept">
                                <xsl:sort select="./rdfs:label"/>
                                <xsl:choose>
                                    <xsl:when test="./@rdf:about = /c:page/c:content/c:topic/c:topicdetails/rdf:RDF/sub:Resource/dct:subject/skos:Concept/skos:broader/@rdf:resource">
                                        <option value="{./@rdf:about}" selected="selected">
                                            <xsl:value-of select="./rdfs:label"/>
                                        </option>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <option value="{./@rdf:about}">
                                            <xsl:value-of select="./rdfs:label"/>
                                        </option>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:for-each>
                        </select>
                    </td>
                </tr>

                <tr>
                    <td></td>
                    <td></td>
                </tr>
                <tr>
                    <td>
                        <input type="submit" value="Lagre emne"/>
                    </td>
                    <td>
                        <input type="reset" value="Rens skjema"/>
                    </td>
                </tr>
            </table>
        </form>

    </xsl:template>

    <xsl:template match="c:topic" mode="topictemp">

        <form name="new_resource" action="{$baseurl}/admin/emner/emne" method="POST">
            <input type="hidden" name="uri" value="{./c:topicdetails/rdf:RDF/sub:Resource/dct:subject/skos:Concept/@rdf:about}"/>
            <table>
                <tr>
                    <td>
                        <label for="dct:subject/skos:Concept/rdfs:label">Tittel</label>
                    </td>
                    <td>
                        <input id="dct:subject/skos:Concept/rdfs:label" type="text" name="dct:subject/skos:Concept/rdfs:label" size="40" value="{./c:tempvalues/c:tempvalues/rdfs:label}"/>
                    </td>
                </tr>
              </table>
              <p>Relaterte emner</p>
              <table>
                <tr>
                    <td>
                        <label for="dct:subject/skos:Concept/skos:broader/rdf:resource">Bredere</label>
                    </td>
                    <td>
                        <select id="dct:subject/skos:Concept/skos:broader/rdf:resource" name="dct:subject/skos:Concept/skos:broader/rdf:resource" multiple="multiple">
                            <xsl:for-each select="./c:alltopics/rdf:RDF/skos:Concept">
                                <xsl:sort select="./rdfs:label"/>
                                <xsl:choose>
                                    <xsl:when test="./@rdf:about = /c:page/c:content/c:topic/c:tempvalues/c:tempvalues/skos:broader/@rdf:resource">
                                        <option value="{./@rdf:about}" selected="selected">
                                            <xsl:value-of select="./rdfs:label"/>
                                        </option>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <option value="{./@rdf:about}">
                                            <xsl:value-of select="./rdfs:label"/>
                                        </option>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:for-each>
                        </select>
                    </td>
                </tr>

                <tr>
                    <td></td>
                    <td></td>
                </tr>
                <tr>
                    <td>
                        <input type="submit" value="Lagre emne"/>
                    </td>
                    <td>
                        <input type="reset" value="Rens skjema"/>
                    </td>
                </tr>
            </table>
        </form>

    </xsl:template>
</xsl:stylesheet>