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

        <xsl:variable name="transcription_am">
            <xsl:for-each
                    select='$document-json/xfn:map//xfn:map[@key="transcriptions"]/xfn:array[@key="AM"]/xfn:string'>
                <xsl:sort select='text()'/>
                <xsl:if test="position()=1">
                    <xsl:value-of select="text()"/>
                </xsl:if>
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="transcription_br">
            <xsl:for-each
                    select='$document-json/xfn:map//xfn:map[@key="transcriptions"]/xfn:array[@key="BR"]/xfn:string'>
                <xsl:sort select='text()'/>
                <xsl:if test="position()=1">
                    <xsl:value-of select="text()"/>
                </xsl:if>
            </xsl:for-each>
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="$document-json/xfn:map//xfn:map[@key='translations'] != ''">
            <!--<xsl:when test="'1' != '1'">-->
                    <!-- select languages. key=language -->
                    <xsl:for-each select="$document-json/xfn:map//xfn:map[@key='translations']/xfn:map[@key]">
                        <xsl:sort select="@key"/>
                        <!-- select categories. key=category -->
                        <xsl:for-each select="xfn:map[@key]">
                            <xsl:sort select="@key"/>
                            <xsl:if test="@key != ''">
                                        -<xsl:value-of select="@key"/>-
                            </xsl:if>
                            <!--<xsl:for-each select="xfn:number[@key][not(position() &gt; $max-count)]">-->
                            <xsl:for-each select="xfn:number[@key]">
                                <xsl:sort select="text()" data-type="number" order="descending"/>
                                <xsl:if test="position() &lt;= $max-count">
                                            <xsl:value-of select = "position()" />. <xsl:value-of select="@key"/> -<xsl:value-of select="text()"/>;
                                </xsl:if>
                            </xsl:for-each>
                        </xsl:for-each>
                    </xsl:for-each>
                    <xsl:if test="$transcription_am != '' or $transcription_br != ''">
                                    <xsl:if test="$transcription_am">
                                        am: <xsl:value-of select='$transcription_am'/>
                                    </xsl:if>
                                    <xsl:if test="$transcription_br">
                                        br: <xsl:value-of select='$transcription_br'/>
                                    </xsl:if>
                    </xsl:if>
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