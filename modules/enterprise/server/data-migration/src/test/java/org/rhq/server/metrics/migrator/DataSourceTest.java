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

package org.rhq.server.metrics.migrator;

import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.ejb.Ejb3Configuration;

import org.rhq.server.metrics.migrator.datasources.ExistingDataBulkExportSource;
import org.rhq.server.metrics.migrator.datasources.ExistingPostgresDataBulkExportSource;

/**
 * @author Thomas Segismont
 */


public class DataSourceTest {

    //ExistingPostgresDataBulkExport
    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        Logger.getLogger("org.rhq").setLevel(Level.DEBUG);
        EntityManagerFactory entityManagerFactory = null;
        EntityManager entityManager = null;
        ExistingDataBulkExportSource source = null;
        try {
            entityManagerFactory = createEntityManager();
            entityManager = entityManagerFactory.createEntityManager();
            source = new ExistingPostgresDataBulkExportSource(entityManager,
                "SELECT  schedule_id, time_stamp, value, minvalue, maxvalue FROM RHQ_MEASUREMENT_DATA_NUM_1D");
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            source.initialize();
            int rowIndex = 0;
            int maxResults = 30000;
            for (;;) {
                List<Object[]> existingData = source.getData(rowIndex, maxResults);
                if (existingData.size() < maxResults) {
                    break;
                } else {
                    rowIndex += maxResults;
                }
            }
            stopWatch.stop();
            System.out.println("Execution: " + stopWatch);
        } finally {
            if (source != null) {
                source.close();
            }
            if (entityManager != null) {
                entityManager.close();
            }
            if (entityManagerFactory != null) {
                entityManagerFactory.close();
            }
        }
    }

    private static EntityManagerFactory createEntityManager() throws Exception {
        Properties properties = new Properties();
        properties.put("javax.persistence.provider", "org.hibernate.ejb.HibernatePersistence");
        properties.put("hibernate.connection.username", "rhqadmin");
        properties.put("hibernate.connection.password", "rhqadmin");
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        String driverClassName = "org.postgresql.Driver";
        try {
            //Required to preload the driver manually.
            //Without this the driver load will fail due to the packaging.
            Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            throw new Exception("Postgres SQL Driver class could not be loaded. Missing class: " + driverClassName);
        }
        properties.put("hibernate.driver_class", driverClassName);
        properties.put("hibernate.connection.url", "jdbc:postgresql://localhost:5432/rhq");
        Ejb3Configuration configuration = new Ejb3Configuration();
        configuration.setProperties(properties);
        return configuration.buildEntityManagerFactory();
    }

    //ExistingDataJPABulkExportSource

    public static void main2(String[] args) throws Exception {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        Logger.getLogger("org.rhq").setLevel(Level.DEBUG);
        EntityManagerFactory entityManagerFactory = null;
        EntityManager entityManager = null;
        ExistingDataBulkExportSource source = null;
        try {
            entityManagerFactory = createEntityManager();
            entityManager = entityManagerFactory.createEntityManager();
            source = new ExistingPostgresDataBulkExportSource(entityManager,
                "SELECT  schedule_id, time_stamp, value, minvalue, maxvalue FROM RHQ_MEASUREMENT_DATA_NUM_1D");
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            source.initialize();
            int rowIndex = 0;
            int maxResults = 30000;
            for (;;) {
                List<Object[]> existingData = source.getData(rowIndex, maxResults);
                if (existingData.size() < maxResults) {
                    break;
                } else {
                    rowIndex += maxResults;
                }
            }
            stopWatch.stop();
            System.out.println("Execution: " + stopWatch);
        } finally {
            if (source != null) {
                source.close();
            }
            if (entityManager != null) {
                entityManager.close();
            }
            if (entityManagerFactory != null) {
                entityManagerFactory.close();
            }
        }
    }

    private static EntityManagerFactory createEntityManager2() throws Exception {
        Properties properties = new Properties();
        properties.put("javax.persistence.provider", "org.hibernate.ejb.HibernatePersistence");
        properties.put("hibernate.connection.username", "rhqadmin");
        properties.put("hibernate.connection.password", "rhqadmin");
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        String driverClassName = "org.postgresql.Driver";
        try {
            //Required to preload the driver manually.
            //Without this the driver load will fail due to the packaging.
            Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            throw new Exception("Postgres SQL Driver class could not be loaded. Missing class: " + driverClassName);
        }
        properties.put("hibernate.driver_class", driverClassName);
        properties.put("hibernate.connection.url", "jdbc:postgresql://localhost:5432/rhqdev");
        Ejb3Configuration configuration = new Ejb3Configuration();
        configuration.setProperties(properties);
        return configuration.buildEntityManagerFactory();
    }

}