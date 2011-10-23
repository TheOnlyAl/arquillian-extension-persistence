/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.persistence;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.persistence.configuration.PersistenceConfiguration;
import org.jboss.arquillian.persistence.event.TransactionFinished;
import org.jboss.arquillian.persistence.event.TransactionStarted;
import org.jboss.arquillian.persistence.metadata.MetadataProvider;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;

public class TransactionalWrapper
{

   private static final String USER_TRANSACTION_JNDI_NAME = "java:comp/UserTransaction";

   @Inject @SuiteScoped
   private Instance<PersistenceConfiguration> configuration;

   private UserTransaction obtainTransaction()
   {
      try
      {
         final InitialContext context = new InitialContext();
         return (UserTransaction) context.lookup(USER_TRANSACTION_JNDI_NAME);
      }
      catch (NamingException e)
      {
         throw new RuntimeException("Failed obtaining transaction.");
      }
   }
   
   public void beforeTest(@Observes TransactionStarted transactionStarted) throws Exception
   {
      MetadataProvider metadataProvider = new MetadataProvider(transactionStarted, configuration.get());
      if (!metadataProvider.isTransactional())
      {
         return;
      }
      obtainTransaction().begin();
   }
   
   public void afterTest(@Observes TransactionFinished transactionFinished) throws Exception
   {
      MetadataProvider metadataProvider = new MetadataProvider(transactionFinished, configuration.get());

      TransactionMode mode = metadataProvider.getTransactionalMode();
      if (TransactionMode.COMMIT.equals(mode))
      {
         obtainTransaction().commit();
      }
      else
      {
         obtainTransaction().rollback();
      }
   }
   
   
}
