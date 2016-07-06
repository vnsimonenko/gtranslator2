<xsl:stylesheet
        xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='3.0'
        xmlns:translation="http://apache.org/translation"
        xmlns:root="http://apache.org/root"
        xmlns:xfn="http://www.w3.org/2005/xpath-functions"
        exclude-result-prefixes="translation"
>

    <xsl:variable name="json-text" select="//translation:element"/>
    <xsl:variable name="max-count" select="//translation:element/@maxcount"/>
    <xsl:variable name="width-px" select="//translation:element/@width"/>
    <xsl:variable name="height-px" select="//translation:element/@height"/>
    <xsl:variable name="font-family" select="//translation:element/@fontfamily"/>
    <xsl:variable name="document-json" select="json-to-xml($json-text, map{'unescape': false()})"/>

    <xsl:output encoding="UTF-8" method="html"/>

    <xsl:template match="root:document">
        <!--<xsl:value-of select="$document-json/xfn:map/xfn:string[@key='source']"/>-->

        <xsl:choose>
            <xsl:when test="$document-json/xfn:map//xfn:map[@key='translations'] != ''">
                    <xsl:for-each select="$document-json/xfn:map//xfn:map[@key='translations']/xfn:map[@key]">
                        <xsl:sort select="@key"/>
                        <xsl:for-each select="xfn:map[@key]">
                            <xsl:sort select="@key"/>
                            <xsl:if test="@key != ''">
                                        <xsl:value-of select="@key"/>\n
                            </xsl:if>
                            <xsl:for-each select="xfn:number[@key]">
                                <xsl:sort select="@key"/>
                                        <xsl:value-of select="@key"/>
                            </xsl:for-each>
                        </xsl:for-each>
                    </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <!--<xsl:template-->
    <!--match="@*|*|text()|processing-instruction()">-->
    <!--<xsl:copy>-->
    <!--<xsl:apply-templates-->
    <!--select="@*|*|text()|processing-instruction()"/>-->
    <!--</xsl:copy>-->
    <!--</xsl:template>-->
</xsl:stylesheet>