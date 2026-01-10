<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:this="urn:czechout:functions"
                version="2.0"
                exclude-result-prefixes="xs this">

    <xsl:param name="NOTE_BUSINESS"/>
    <xsl:param name="NOTE_VAT"/>
    <xsl:param name="DISCLAIMER"/>

    <xsl:decimal-format name="czech" grouping-separator=" " decimal-separator="," />

    <xsl:attribute-set name="label">
        <xsl:attribute name="font-size">14pt</xsl:attribute>
        <xsl:attribute name="margin-bottom">5pt</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="standard-margin">
        <xsl:attribute name="margin-left">8pt</xsl:attribute>
        <xsl:attribute name="margin-bottom">4pt</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="table-cell">
        <xsl:attribute name="padding-top">2pt</xsl:attribute>
        <xsl:attribute name="padding-bottom">2pt</xsl:attribute>
    </xsl:attribute-set>

    <xsl:function name="this:format-bigdecimal" as="xs:string">
        <xsl:param name="value"/>
        <xsl:param name="suffix"/>
        <xsl:variable name="num" select="number($value)"/>
        <xsl:choose>
            <xsl:when test="$suffix = '%'">
                <xsl:value-of select="concat(format-number($num * 100, '# ##0,##', 'czech'), ' ', $suffix)"/>
            </xsl:when>
            <xsl:when test="exists($suffix) and normalize-space($suffix) != ''">
                <xsl:value-of select="concat(format-number($num, '# ##0,#####', 'czech'), ' ', $suffix)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="format-number($num, '# ##0,##', 'czech')"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:template name="render-address">
        <xsl:param name="party"/>
        <xsl:param name="title"/>
        <fo:block xsl:use-attribute-sets="label"><xsl:value-of select="$title"/></fo:block>
        <fo:block xsl:use-attribute-sets="standard-margin">
            <xsl:value-of select="$party/name"/>
        </fo:block>
        <fo:block xsl:use-attribute-sets="standard-margin">
            <xsl:value-of select="string-join(($party/address/street, $party/address/houseNumber), ' ')"/>
        </fo:block>
        <fo:block xsl:use-attribute-sets="standard-margin">
            <xsl:value-of select="concat($party/address/zipCode, ' ', $party/address/district, ', ', $party/address/city)"/>
        </fo:block>
        <fo:block xsl:use-attribute-sets="standard-margin" font-weight="bold">
            <xsl:value-of select="concat($party/identifierType, ': ', $party/identifier)"/>
        </fo:block>
        <xsl:if test="$party/vat">
            <fo:block xsl:use-attribute-sets="standard-margin" font-weight="bold">
                <xsl:value-of select="concat($party/vatPrefix, ': ', $party/vat)"/>
            </fo:block>
        </xsl:if>
    </xsl:template>

    <xsl:template name="invoice-template">
        <fo:block font-size="24pt" margin-bottom="5pt">
            <fo:inline font-weight="bold">Faktura</fo:inline>
            <fo:inline>&#160;—&#160;</fo:inline>
            <fo:inline font-weight="bold"><xsl:value-of select="name"/></fo:inline>
        </fo:block>

        <fo:block xsl:use-attribute-sets="standard-margin" margin-bottom="20pt">Daňový doklad</fo:block>

        <fo:table margin-bottom="10pt" width="100%" table-layout="fixed">
            <fo:table-column column-width="62%"/><fo:table-column column-width="38%"/>
            <fo:table-body>
                <fo:table-row>
                    <fo:table-cell>
                        <xsl:call-template name="render-address">
                            <xsl:with-param name="party" select="issuer"/>
                            <xsl:with-param name="title" select="'Dodavatel:'"/>
                        </xsl:call-template>
                    </fo:table-cell>
                    <fo:table-cell>
                        <xsl:call-template name="render-address">
                            <xsl:with-param name="party" select="recipient"/>
                            <xsl:with-param name="title" select="'Odběratel:'"/>
                        </xsl:call-template>
                    </fo:table-cell>
                </fo:table-row>
            </fo:table-body>
        </fo:table>

        <fo:block xsl:use-attribute-sets="standard-margin"><xsl:value-of select="$NOTE_BUSINESS"/></fo:block>
        <fo:block xsl:use-attribute-sets="standard-margin" margin-bottom="20pt"><xsl:value-of select="$NOTE_VAT"/></fo:block>

        <fo:table margin-bottom="20pt" width="100%" table-layout="fixed">
            <fo:table-column column-width="42%"/>
            <fo:table-column column-width="20%"/>
            <fo:table-column column-width="38%"/>
            <fo:table-body>
                <fo:table-row>
                    <fo:table-cell>
                        <fo:block xsl:use-attribute-sets="label">Platební podmínky</fo:block>
                        <fo:block xsl:use-attribute-sets="standard-margin">Číslo účtu</fo:block>
                        <fo:block xsl:use-attribute-sets="standard-margin">Banka</fo:block>
                        <xsl:if test="vs"><fo:block xsl:use-attribute-sets="standard-margin">Variabilní symbol</fo:block></xsl:if>
                        <xsl:if test="ks"><fo:block xsl:use-attribute-sets="standard-margin">Konstantní symbol</fo:block></xsl:if>
                        <xsl:if test="ss"><fo:block xsl:use-attribute-sets="standard-margin">Specifický symbol</fo:block></xsl:if>
                        <fo:block xsl:use-attribute-sets="standard-margin">Způsob úhrady</fo:block>
                        <fo:block xsl:use-attribute-sets="standard-margin">Datum vystavění</fo:block>
                        <fo:block xsl:use-attribute-sets="standard-margin">Datum uskutečnění zdanitelného plnění</fo:block>
                        <fo:block xsl:use-attribute-sets="standard-margin" font-weight="bold">Datum splatnosti</fo:block>
                    </fo:table-cell>
                    <fo:table-cell text-align="right">
                        <fo:block xsl:use-attribute-sets="label">&#160;</fo:block>
                        <fo:block xsl:use-attribute-sets="standard-margin" margin-right="12px" ><xsl:value-of select="account/accountNumber"/>/<xsl:value-of select="account/bankCode"/></fo:block>
                        <fo:block xsl:use-attribute-sets="standard-margin" margin-right="12px" ><xsl:value-of select="account/bankName"/></fo:block>
                        <xsl:if test="vs">
                            <fo:block xsl:use-attribute-sets="standard-margin" margin-right="12px" ><xsl:value-of select="vs"/></fo:block>
                        </xsl:if>
                        <xsl:if test="ks">
                            <fo:block xsl:use-attribute-sets="standard-margin" margin-right="12px" ><xsl:value-of select="ks"/></fo:block>
                        </xsl:if>
                        <xsl:if test="ss">
                            <fo:block xsl:use-attribute-sets="standard-margin" margin-right="12px" ><xsl:value-of select="ss"/></fo:block>
                        </xsl:if>
                        <fo:block xsl:use-attribute-sets="standard-margin" margin-right="12px"><xsl:value-of select="method/name"/></fo:block>
                        <fo:block xsl:use-attribute-sets="standard-margin" margin-right="12px"><xsl:value-of select="format-date(xs:date(issueDate), '[D].[M].[Y]')"/></fo:block>
                        <fo:block xsl:use-attribute-sets="standard-margin" margin-right="12px"><xsl:value-of select="format-date(xs:date(taxDate), '[D].[M].[Y]')"/></fo:block>
                        <fo:block xsl:use-attribute-sets="standard-margin" margin-right="12px" font-weight="bold"><xsl:value-of select="format-date(xs:date(dueDate), '[D].[M].[Y]')"/></fo:block>
                    </fo:table-cell>
                    <fo:table-cell>
                        <fo:block xsl:use-attribute-sets="label" font-weight="bold">QR platba: </fo:block>
                        <fo:block margin-left="8pt">
                            <fo:instream-foreign-object content-type="image/svg+xml"
                                                        content-width="175px"
                                                        content-height="175px"
                                                        scaling="uniform">
                                <xsl:copy-of select="qrSvg/*"/>
                            </fo:instream-foreign-object>
                        </fo:block>
                    </fo:table-cell>
                </fo:table-row>
            </fo:table-body>
        </fo:table>

        <fo:block xsl:use-attribute-sets="label">Fakturace za dodané služby:</fo:block>
        <fo:table margin-bottom="15pt" width="100%" table-layout="fixed" border-bottom="1pt solid black">
            <fo:table-column column-width="22.5%"/>
            <fo:table-column column-width="10.5%"/>
            <fo:table-column column-width="12.5%"/>
            <fo:table-column column-width="7.5%"/>
            <fo:table-column column-width="15%"/>
            <fo:table-column column-width="16%"/>
            <fo:table-column column-width="16%"/>

            <fo:table-header border-bottom="1pt solid black">
                <fo:table-row font-weight="bold">
                    <fo:table-cell xsl:use-attribute-sets="table-cell"><fo:block margin-left="8pt">Název</fo:block></fo:table-cell>
                    <fo:table-cell xsl:use-attribute-sets="table-cell" text-align="right"><fo:block>Mn.</fo:block></fo:table-cell>
                    <fo:table-cell xsl:use-attribute-sets="table-cell" text-align="right"><fo:block>Cena/MJ</fo:block></fo:table-cell>
                    <fo:table-cell xsl:use-attribute-sets="table-cell" text-align="right"><fo:block>% DPH</fo:block></fo:table-cell>
                    <fo:table-cell xsl:use-attribute-sets="table-cell" text-align="right"><fo:block>Bez DPH</fo:block></fo:table-cell>
                    <fo:table-cell xsl:use-attribute-sets="table-cell" text-align="right"><fo:block>DPH</fo:block></fo:table-cell>
                    <fo:table-cell xsl:use-attribute-sets="table-cell" text-align="right"><fo:block>Celkem</fo:block></fo:table-cell>
                </fo:table-row>
            </fo:table-header>
            <fo:table-body>
                <xsl:for-each select="items">
                    <fo:table-row border-bottom="1pt solid #EEEEEE">
                        <fo:table-cell xsl:use-attribute-sets="table-cell">
                            <fo:block margin-left="8pt"><xsl:value-of select="name"/></fo:block>
                        </fo:table-cell>
                        <fo:table-cell xsl:use-attribute-sets="table-cell" text-align="right">
                            <fo:block><xsl:value-of select="this:format-bigdecimal(quantity, null)"/>&#160;<xsl:value-of select="unit"/></fo:block>
                        </fo:table-cell>
                        <fo:table-cell xsl:use-attribute-sets="table-cell" text-align="right">
                            <fo:block><xsl:value-of select="this:format-bigdecimal(unitPrice, 'Kč')"/></fo:block>
                        </fo:table-cell>
                        <fo:table-cell xsl:use-attribute-sets="table-cell" text-align="right">
                            <fo:block><xsl:value-of select="this:format-bigdecimal(vatRate, '%')"/></fo:block>
                        </fo:table-cell>
                        <fo:table-cell xsl:use-attribute-sets="table-cell" text-align="right">
                            <fo:block><xsl:value-of select="this:format-bigdecimal(basePrice, 'Kč')"/></fo:block>
                        </fo:table-cell>
                        <fo:table-cell xsl:use-attribute-sets="table-cell" text-align="right">
                            <fo:block><xsl:value-of select="this:format-bigdecimal(vatPrice, 'Kč')"/></fo:block>
                        </fo:table-cell>
                        <fo:table-cell xsl:use-attribute-sets="table-cell" text-align="right">
                            <fo:block><xsl:value-of select="this:format-bigdecimal(totalPrice, 'Kč')"/></fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </xsl:for-each>
            </fo:table-body>
        </fo:table>

        <fo:table margin-bottom="30pt" width="100%" table-layout="fixed">
            <fo:table-column column-width="45.5%"/>
            <fo:table-column column-width="7.5%"/>
            <fo:table-column column-width="15%"/>
            <fo:table-column column-width="16%"/>
            <fo:table-column column-width="16%"/>
            <fo:table-header>
                <fo:table-row font-weight="bold">
                    <fo:table-cell xsl:use-attribute-sets="table-cell" text-align="right">
                        <fo:block/>
                    </fo:table-cell>
                    <fo:table-cell xsl:use-attribute-sets="table-cell" text-align="right" border-bottom="1pt solid black">
                        <fo:block>% DPH</fo:block>
                    </fo:table-cell>
                    <fo:table-cell xsl:use-attribute-sets="table-cell" text-align="right" border-bottom="1pt solid black">
                        <fo:block>Bez DPH</fo:block>
                    </fo:table-cell>
                    <fo:table-cell xsl:use-attribute-sets="table-cell" text-align="right" border-bottom="1pt solid black">
                        <fo:block>DPH</fo:block>
                    </fo:table-cell>
                    <fo:table-cell xsl:use-attribute-sets="table-cell" text-align="right" border-bottom="1pt solid black">
                        <fo:block>Včetně DPH</fo:block>
                    </fo:table-cell>
                </fo:table-row>
            </fo:table-header>
            <fo:table-body>
                <xsl:for-each-group select="items" group-by="vatRate">
                    <xsl:sort select="current-grouping-key()" order="descending"/>
                    <fo:table-row>
                        <fo:table-cell><fo:block/></fo:table-cell>
                        <fo:table-cell xsl:use-attribute-sets="table-cell" border-bottom="1pt solid #EEEEEE" text-align="right"><fo:block><xsl:value-of select="this:format-bigdecimal(current-grouping-key(), '%')"/></fo:block></fo:table-cell>
                        <fo:table-cell xsl:use-attribute-sets="table-cell" border-bottom="1pt solid #EEEEEE" text-align="right"><fo:block><xsl:value-of select="this:format-bigdecimal(sum(current-group()/basePrice), 'Kč')"/></fo:block></fo:table-cell>
                        <fo:table-cell xsl:use-attribute-sets="table-cell" border-bottom="1pt solid #EEEEEE" text-align="right"><fo:block><xsl:value-of select="this:format-bigdecimal(sum(current-group()/vatPrice), 'Kč')"/></fo:block></fo:table-cell>
                        <fo:table-cell xsl:use-attribute-sets="table-cell" border-bottom="1pt solid #EEEEEE" text-align="right"><fo:block><xsl:value-of select="this:format-bigdecimal(sum(current-group()/totalPrice), 'Kč')"/></fo:block></fo:table-cell>
                    </fo:table-row>
                </xsl:for-each-group>
            </fo:table-body>
        </fo:table>

        <fo:table margin-bottom="10pt" width="100%" table-layout="fixed" border-top="2pt solid black">
            <fo:table-column column-width="50%"/><fo:table-column column-width="50%"/>
            <fo:table-body>
                <fo:table-row>
                    <fo:table-cell font-size="16pt" font-weight="bold" padding-top="8pt">
                        <fo:block>Celkem k úhradě</fo:block>
                    </fo:table-cell>
                    <fo:table-cell font-size="16pt" font-weight="bold" padding-top="8pt" text-align="right">
                        <fo:block>
                            <xsl:value-of select="this:format-bigdecimal(sum(items/totalPrice), 'Kč')"/>
                        </fo:block>
                    </fo:table-cell>
                </fo:table-row>
            </fo:table-body>
        </fo:table>

        <fo:block xsl:use-attribute-sets="standard-margin"><xsl:value-of select="$DISCLAIMER"/></fo:block>

        <xsl:if test="position() != last()"><fo:block break-after="page"/></xsl:if>
    </xsl:template>
</xsl:stylesheet>
