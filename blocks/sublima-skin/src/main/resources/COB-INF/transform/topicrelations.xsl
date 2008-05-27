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

    <xsl:template match="c:related" mode="topicrelatededit">
        <form action="{$baseurl}/admin/emner/relasjoner/relasjon" method="POST">
            <input type="hidden" name="uri" value="{./c:relation/rdf:RDF/skos:semanticRelation/@rdf:about}"/>
            <table>
                <tr>
                    <td>
                        <label for="skos:semanticRelation/rdfs:label">Relasjonstype</label>
                    </td>
                    <td>
                      <input id="skos:semanticRelation/rdfs:label" type="text" name="skos:semanticRelation/rdfs:label" size="40" value="{./c:relation/rdf:RDF/skos:semanticRelation/rdfs:label}" /></td>
                </tr>
             
                <tr>
                    <td></td>
                    <td></td>
                </tr>
                <tr>
                    <td>
                        <input type="submit" value="Lagre relasjonstype"/>
                    </td>
                    <td>
                        <input type="reset" value="Rens skjema"/>
                    </td>
                </tr>
            </table>
        </form>

    </xsl:template>

      <xsl:template match="c:related" mode="topicrelatedtemp">
        <form action="{$baseurl}/admin/emner/relasjoner/relasjon" method="POST">
            <input type="hidden" name="uri" value="{./c:tempvalues/c:tempvalues/rdf:about}"/>
            <table>
                <tr>
                    <td>
                        <label for="skos:semanticRelation/rdfs:label">Relasjonstype</label>
                    </td>
                    <td>
                      <input id="skos:semanticRelation/rdfs:label" type="text" name="skos:semanticRelation/rdfs:label" size="40" value="{./c:tempvalues/c:tempvalues/rdfs:label}" /></td>
                </tr>

                <tr>
                    <td></td>
                    <td></td>
                </tr>
                <tr>
                    <td>
                        <input type="submit" value="Lagre relasjonstype"/>
                    </td>
                    <td>
                        <input type="reset" value="Rens skjema"/>
                    </td>
                </tr>
            </table>
        </form>

    </xsl:template>
</xsl:stylesheet>