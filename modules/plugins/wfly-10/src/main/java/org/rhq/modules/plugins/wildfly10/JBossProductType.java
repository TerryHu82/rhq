/*
 * RHQ Management Platform
 * Copyright (C) 2005-2013 Red Hat, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA
 */
package org.rhq.modules.plugins.wildfly10;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Metadata describing a product based on Wildfly 10
 */
 public enum JBossProductType {

    EAP("EAP", "EAP 7", "JBoss Enterprise Application Platform 7", "JBoss EAP"),
    JDG("JDG", "JBoss JDG 7", "JBoss Data Grid 7", "Data Grid"),
    WILDFLY("WildFly", "WildFly 10", "WildFly Application Server 10", "WildFly Full");

    public final String SHORT_NAME;
    public final String NAME;
    public final String FULL_NAME;
    /** The value the server returns for the "product-name" attribute of the root resource. */
    public final String PRODUCT_NAME;

    JBossProductType(String shortName, String name, String fullName, String productName) {
        this.SHORT_NAME = shortName;
        this.NAME = name;
        this.FULL_NAME = fullName;
        this.PRODUCT_NAME = productName;
    }

    public static JBossProductType getValueByProductName(String productName) {
        for (JBossProductType productType : JBossProductType.values()) {
             if (productType.PRODUCT_NAME.equals(productName)) {
                 return productType;
             }
        }
        throw new IllegalArgumentException("No product type with product-name '" + productName + "' is known.");
    }

    public static JBossProductType getValueByShortName(String productName) {
        if(productName.equalsIgnoreCase("wildfly-full")) {
            return WILDFLY;
        }

        for (JBossProductType productType : JBossProductType.values()) {
            if (productType.SHORT_NAME.equalsIgnoreCase(productName)) {
                return productType;
            }
        }
        throw new IllegalArgumentException("No product type with product-name '" + productName + "' is known.");
    }

    /**
     * Determines the product type of a JBoss product installation.
     *
     * @param homeDir the JBoss product installation directory (e.g. /opt/wildfly-10.0.0.Final)
     *
     * @param apiVersion Api version of the domain api.
     * @return the product type
     */
    public static JBossProductType determineJBossProductType(File homeDir, String apiVersion) throws Exception {
        JBossProductType productType = null;
        File productConfFile = new File(homeDir, "bin/product.conf");
        if (productConfFile.exists()) {
            Properties productConfProps = new Properties();
            FileInputStream inputStream = new FileInputStream(productConfFile);
            try {
                productConfProps.load(inputStream);
            } catch (IOException e) {
                throw new Exception("Failed to parse " + productConfFile + ".", e);
            } finally {
                inputStream.close();
            }
            String slot = productConfProps.getProperty("slot", "").trim();
            if (slot.isEmpty()) {
                throw new Exception("'slot' property not found in " + productConfFile + ".");
            }
            // "urn:jboss:domain:<version>" parsing, EAP 7.0 = 4.0, EAP 7.1 = 5.0 and EAP 7.2 = 7.0
            try {
                Double version = Double.valueOf(apiVersion);
                if(version >= 4) {
                    productType = getValueByShortName(slot);
                }
            } catch(NumberFormatException e) {
                // This is not a supported situation for this plugin
                throw e;
            } catch(IllegalArgumentException ie) {
                // Wildfly could be newer than what we know
                productType = WILDFLY;
            }
        }

        return productType;
    }

    @Override
    public String toString() {
        return this.NAME;
    }

}
