<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format">

    <xsl:import href="invoice-template.xsl"/>

    <xsl:template match="/">
        <fo:root font-family="Consolas">
            <fo:layout-master-set>
                <fo:simple-page-master master-name="A4" page-height="29.7cm" page-width="21cm" margin="2cm" margin-bottom="1cm">
                    <fo:region-body/>
                </fo:simple-page-master>
            </fo:layout-master-set>

            <xsl:for-each select="/invoices/invoice">
                <fo:page-sequence master-reference="A4">
                    <fo:flow flow-name="xsl-region-body" font-size="9pt">
                        <xsl:call-template name="invoice-template"/>
                    </fo:flow>
                </fo:page-sequence>
            </xsl:for-each>
        </fo:root>
    </xsl:template>
</xsl:stylesheet>
