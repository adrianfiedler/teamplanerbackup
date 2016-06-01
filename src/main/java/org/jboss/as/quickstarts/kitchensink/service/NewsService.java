/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.as.quickstarts.kitchensink.service;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import org.jboss.as.quickstarts.kitchensink.model.Einladung;
import org.jboss.as.quickstarts.kitchensink.model.News;
import org.jboss.as.quickstarts.kitchensink.model.News_;
import org.jboss.as.quickstarts.kitchensink.model.Team;
import org.jboss.as.quickstarts.kitchensink.model.Team_;
import org.jboss.as.quickstarts.kitchensink.model.Termin_;
import org.jboss.as.quickstarts.kitchensink.model.Verein;
import org.jboss.as.quickstarts.kitchensink.model.Verein_;
import org.jboss.as.quickstarts.kitchensink.model.Zusage;

// The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
public class NewsService {

	@Inject
	private Logger log;

	@Inject
	private EntityManager em;

	public void save(News news) {
		em.persist(news);
	}

	public void delete(News news) {
		em.remove(news);
	}

	public News findById(String id) {
		return em.find(News.class, id);
	}

	public List<News> findAll() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<News> criteria = cb.createQuery(News.class);
		Root<News> news = criteria.from(News.class);
		criteria.select(news).orderBy(cb.desc(news.get(News_.modificationDate)));
		List<News> results = em.createQuery(criteria).getResultList();
		return results;
	}
}
