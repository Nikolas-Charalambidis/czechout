<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:this="urn:czechout:functions"
                version="2.0"
                exclude-result-prefixes="xs this">

    <xsl:decimal-format name="czech" grouping-separator=" " decimal-separator="," />

    <xsl:function name="this:format-bigdecimal" as="xs:string">
        <xsl:param name="value"/>
        <xsl:param name="suffix"/>
        <xsl:choose>
            <xsl:when test="$suffix = '%'">
                <xsl:variable name="formattedBigDecimal" select="number($value) * 100"/>
                <xsl:value-of select="concat(format-number($formattedBigDecimal, '# ##0,##', 'czech'), ' ', $suffix)"/>
            </xsl:when>
            <xsl:when test="exists($suffix) and normalize-space($suffix) != ''">
                <xsl:variable name="formattedBigDecimal" select="format-number(number($value), '# ##0,#####', 'czech')"/>
                <xsl:value-of select="concat($formattedBigDecimal, ' ', $suffix)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="formattedBigDecimal" select="format-number(number($value), '# ##0,##', 'czech')"/>
                <xsl:value-of select="$formattedBigDecimal"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:template match="/">
        <fo:root font-family="Consolas">
            <fo:layout-master-set>
                <fo:simple-page-master master-name="A4"
                                       page-height="29.7cm"
                                       page-width="21cm"
                                       margin="2cm">
                    <fo:region-body/>
                </fo:simple-page-master>
            </fo:layout-master-set>

            <fo:page-sequence master-reference="A4">
                <fo:flow flow-name="xsl-region-body" font-size="9pt">

                    <xsl:for-each select="/invoices/invoices">

                        <fo:block font-size="24pt" font-weight="bold" margin-bottom="3pt">
                            Faktura: <xsl:value-of select="name"/>
                        </fo:block>

                        <fo:block margin-left="12px" margin-bottom="10pt">
                            Daňový doklad
                        </fo:block>

                        <fo:table width="100%" margin-top="10pt" table-layout="fixed">
                            <fo:table-column column-width="60%"/>
                            <fo:table-column column-width="40%"/>
                            <fo:table-body>
                                <fo:table-row>
                                    <fo:table-cell>
                                        <fo:block font-size="14pt" margin-bottom="3pt" font-weight="bold">Dodavatel:</fo:block>
                                        <fo:block margin-left="12px" margin-bottom="3pt"><xsl:value-of select="issuer/name"/></fo:block>
                                        <fo:block margin-left="12px" margin-bottom="3pt"><xsl:value-of select="issuer/address/street"/>&#160;<xsl:value-of select="issuer/address/houseNumber"/></fo:block>
                                        <fo:block margin-left="12px" margin-bottom="3pt"><xsl:value-of select="issuer/address/zipCode"/>&#160;<xsl:value-of select="issuer/address/district"/>,&#160;<xsl:value-of select="issuer/address/city"/></fo:block>
                                        <fo:block margin-left="12px" margin-bottom="3pt" font-weight="bold" ><xsl:value-of select="issuer/identifierType"/>:&#160;<xsl:value-of select="issuer/identifier"/></fo:block>
                                        <fo:block margin-left="12px" margin-bottom="3pt" font-weight="bold" ><xsl:value-of select="issuer/vatPrefix"/>:&#160;<xsl:value-of select="issuer/vat"/></fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell>
                                        <fo:block font-size="14pt" margin-bottom="3pt" font-weight="bold">Odběratel:</fo:block>
                                        <fo:block margin-left="12px" margin-bottom="3pt"><xsl:value-of select="recipient/name"/></fo:block>
                                        <fo:block margin-left="12px" margin-bottom="3pt"><xsl:value-of select="recipient/address/street"/>&#160;<xsl:value-of select="recipient/address/houseNumber"/></fo:block>
                                        <fo:block margin-left="12px" margin-bottom="3pt"><xsl:value-of select="recipient/address/zipCode"/>&#160;<xsl:value-of select="recipient/address/district"/>,&#160;<xsl:value-of select="recipient/address/city"/></fo:block>
                                        <fo:block margin-left="12px" margin-bottom="3pt" font-weight="bold" ><xsl:value-of select="recipient/identifierType"/>:&#160;<xsl:value-of select="recipient/identifier"/></fo:block>
                                        <fo:block margin-left="12px" margin-bottom="3pt" font-weight="bold" ><xsl:value-of select="recipient/vatPrefix"/>:&#160;<xsl:value-of select="recipient/vat"/></fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                            </fo:table-body>
                        </fo:table>

                        <fo:table width="100%" margin-top="10pt" table-layout="fixed">
                            <fo:table-column column-width="45%"/>
                            <fo:table-column column-width="15%"/>
                            <fo:table-column column-width="40%"/>
                            <fo:table-body>
                                <fo:table-row>
                                    <fo:table-cell>
                                        <fo:block font-size="14pt" margin-bottom="3pt" font-weight="bold">Platební podmínky</fo:block>
                                        <fo:block margin-left="12px" margin-bottom="3pt">Číslo účtu:</fo:block>
                                        <fo:block margin-left="12px" margin-bottom="3pt">Banka</fo:block>
                                        <fo:block margin-left="12px" margin-bottom="3pt">Variabilní symbol</fo:block>
                                        <fo:block margin-left="12px" margin-bottom="3pt">Způsob úhrady</fo:block>
                                        <fo:block margin-left="12px" margin-bottom="3pt">Datum vystavění:</fo:block>
                                        <fo:block margin-left="12px" margin-bottom="3pt">Datum uskutečnění zdanitelného plnění:</fo:block>
                                        <fo:block margin-left="12px" margin-bottom="3pt" font-weight="bold">Datum splatnosti:</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell>
                                        <fo:block font-size="14pt" margin-bottom="3pt" font-weight="bold">&#160;</fo:block>
                                        <fo:block margin-left="12px" margin-bottom="3pt"><xsl:value-of select="account/accountNumber"/></fo:block>
                                        <fo:block margin-left="12px" margin-bottom="3pt"><xsl:value-of select="account/bankName"/></fo:block>
                                        <fo:block margin-left="12px" margin-bottom="3pt"><xsl:value-of select="vs"/></fo:block>
                                        <fo:block margin-left="12px" margin-bottom="3pt"><xsl:value-of select="method/name"/></fo:block>
                                        <fo:block margin-left="12px" margin-bottom="3pt">1.1.2025</fo:block>
                                        <fo:block margin-left="12px" margin-bottom="3pt">1.1.2025</fo:block>
                                        <fo:block margin-left="12px" margin-bottom="3pt" font-weight="bold">1.1.2025</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell>
                                        <fo:block font-size="14pt" margin-bottom="3pt" font-weight="bold">QR platba:</fo:block>
                                        <fo:block margin-left="12px" >
                                            <fo:external-graphic src="../../../target/qr.svg" content-height="180px" content-width="180px" scaling="uniform"/>
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                            </fo:table-body>
                        </fo:table>

                        <fo:block margin-top="20pt" font-size="14pt" font-weight="bold" margin-bottom="5pt">
                            Fakturace za dodané služby:
                        </fo:block>

                        <fo:table width="100%" margin-top="10pt" table-layout="fixed">
                            <fo:table-column column-width="25.5%"/>
                            <fo:table-column column-width="10.5%"/>
                            <fo:table-column column-width="10.5%"/>
                            <fo:table-column column-width="7.5%"/>
                            <fo:table-column column-width="15%"/>
                            <fo:table-column column-width="15.5%"/>
                            <fo:table-column column-width="15.5%"/>

                            <fo:table-header>
                                <fo:table-row font-weight="bold">
                                    <fo:table-cell>
                                        <fo:block margin-left="12px" margin-bottom="3pt">Název položky</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell text-align="right">
                                        <fo:block margin-bottom="3pt">Množství</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell text-align="right">
                                        <fo:block margin-bottom="3pt">Cena</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell text-align="right">
                                        <fo:block margin-bottom="3pt">% DPH</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell text-align="right">
                                        <fo:block margin-bottom="3pt">Bez DPH</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell text-align="right">
                                        <fo:block margin-bottom="3pt">DPH</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell text-align="right">
                                        <fo:block margin-bottom="3pt">Včetně DPH</fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                            </fo:table-header>
                            <fo:table-body>
                                <xsl:for-each select="items">
                                    <fo:table-row>
                                        <fo:table-cell>
                                            <fo:block margin-left="12px" margin-bottom="3pt"><xsl:value-of select="name"/></fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell text-align="right">
                                            <fo:block margin-bottom="3pt"><xsl:value-of select="this:format-bigdecimal(quantity, null)"/>&#160;<xsl:value-of select="unit"/></fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell text-align="right">
                                            <fo:block margin-bottom="3pt"><xsl:value-of select="this:format-bigdecimal(unitPrice, 'Kč')"/></fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell text-align="right">
                                            <fo:block margin-bottom="3pt"><xsl:value-of select="this:format-bigdecimal(vatRate, '%')"/></fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell text-align="right">
                                            <fo:block margin-bottom="3pt"><xsl:value-of select="this:format-bigdecimal(basePrice, 'Kč')"/></fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell text-align="right">
                                            <fo:block margin-bottom="3pt"><xsl:value-of select="this:format-bigdecimal(vatPrice, 'Kč')"/></fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell text-align="right">
                                            <fo:block margin-bottom="3pt"><xsl:value-of select="this:format-bigdecimal(totalPrice, 'Kč')"/></fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                </xsl:for-each>
                            </fo:table-body>
                        </fo:table>

                        <fo:table width="100%" margin-top="10pt" table-layout="fixed">
                            <fo:table-column column-width="46.5%"/>
                            <fo:table-column column-width="7.5%"/>
                            <fo:table-column column-width="15%"/>
                            <fo:table-column column-width="15.5%"/>
                            <fo:table-column column-width="15.5%"/>
                            <fo:table-header>
                                <fo:table-row font-weight="bold">
                                    <fo:table-cell text-align="right">
                                        <fo:block margin-bottom="3pt"/>
                                    </fo:table-cell>
                                    <fo:table-cell text-align="right">
                                        <fo:block margin-bottom="3pt">Sazba</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell text-align="right">
                                        <fo:block margin-bottom="3pt">Bez DPH</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell text-align="right">
                                        <fo:block margin-bottom="3pt">DPH</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell text-align="right">
                                        <fo:block margin-bottom="3pt">Včetně DPH</fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                            </fo:table-header>
                            <fo:table-body>
                                <xsl:for-each-group select="items" group-by="vatRate">
                                    <xsl:sort select="current-grouping-key()" order="descending"/>
                                    <fo:table-row>
                                        <fo:table-cell>
                                            <fo:block margin-bottom="3pt"/>
                                        </fo:table-cell>
                                        <fo:table-cell text-align="right">
                                            <fo:block>
                                                <xsl:value-of select="this:format-bigdecimal(current-grouping-key(), '%')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell text-align="right">
                                            <fo:block margin-bottom="3pt">
                                                <xsl:value-of select="this:format-bigdecimal(sum(current-group()/basePrice), 'Kč')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell text-align="right">
                                            <fo:block margin-bottom="3pt">
                                                <xsl:value-of select="this:format-bigdecimal(sum(current-group()/vatPrice), 'Kč')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell text-align="right">
                                            <fo:block margin-bottom="3pt">
                                                <xsl:value-of select="this:format-bigdecimal(sum(current-group()/totalPrice), 'Kč')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                </xsl:for-each-group>
                            </fo:table-body>
                        </fo:table>

                        <fo:table width="100%" margin-top="10pt" table-layout="fixed">
                            <fo:table-column column-width="50%"/>
                            <fo:table-column column-width="50%"/>
                            <fo:table-body>
                                <fo:table-row>
                                    <fo:table-cell>
                                        <fo:block font-size="14pt" font-weight="bold">
                                            Celkem k úhradě:
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell>
                                        <fo:block font-size="14pt" font-weight="bold" text-align="right">
                                            <xsl:value-of select="this:format-bigdecimal(sum(items/totalPrice), 'Kč')"/>
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                            </fo:table-body>
                        </fo:table>

                        <xsl:if test="position() != last()">
                            <fo:block break-after="page"/>
                        </xsl:if>

                    </xsl:for-each>

                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>
</xsl:stylesheet>
