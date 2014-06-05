/*
Copyright [2013-2014] eBay Software Foundation

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/


/* 
Copyright 2012 eBay Software Foundation 

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

    http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License. 
 */
package com.ebay.cloud.cms.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ebay.cloud.cms.config.CMSDBConfig;
import com.ebay.cloud.cms.dal.entity.IEntity;
import com.ebay.cloud.cms.dal.entity.flatten.impl.FlattenEntityIDHelper;
import com.ebay.cloud.cms.dal.entity.flatten.impl.NewBsonEntity;
import com.ebay.cloud.cms.dal.entity.impl.BsonEntity;
import com.ebay.cloud.cms.dal.entity.impl.EntityIDHelper;
import com.ebay.cloud.cms.dal.persistence.ConsistentPolicy;
import com.ebay.cloud.cms.dal.persistence.PersistenceContext.CollectionFinder;
import com.ebay.cloud.cms.dal.persistence.flatten.impl.NewCollectionFinder;
import com.ebay.cloud.cms.dal.persistence.flatten.impl.NewDalEntityFactory;
import com.ebay.cloud.cms.dal.persistence.flatten.impl.NewDalSearchStrategy;
import com.ebay.cloud.cms.dal.persistence.flatten.impl.NewPersistenceServiceImpl;
import com.ebay.cloud.cms.dal.persistence.impl.DalEntityFactory;
import com.ebay.cloud.cms.dal.persistence.impl.DalSearchStrategy;
import com.ebay.cloud.cms.dal.persistence.impl.PersistenceService;
import com.ebay.cloud.cms.dal.persistence.impl.PersistenceService.Registration;
import com.ebay.cloud.cms.dal.persistence.impl.PersistenceServiceImpl;
import com.ebay.cloud.cms.dal.search.ISearchService;
import com.ebay.cloud.cms.dal.search.impl.SearchServiceImpl;
import com.ebay.cloud.cms.entmgr.branch.IBranch;
import com.ebay.cloud.cms.entmgr.branch.IBranchService;
import com.ebay.cloud.cms.entmgr.entity.EntityContext;
import com.ebay.cloud.cms.entmgr.service.ServiceFactory;
import com.ebay.cloud.cms.metadata.RepositoryServiceFactory;
import com.ebay.cloud.cms.metadata.model.IndexInfo;
import com.ebay.cloud.cms.metadata.model.MetaClass;
import com.ebay.cloud.cms.metadata.model.MetaField;
import com.ebay.cloud.cms.metadata.model.MetaField.CardinalityEnum;
import com.ebay.cloud.cms.metadata.model.MetaRelationship;
import com.ebay.cloud.cms.metadata.service.IMetadataService;
import com.ebay.cloud.cms.metadata.service.IRepositoryService;
import com.ebay.cloud.cms.metadata.service.MetadataContext;
import com.ebay.cloud.cms.mongo.MongoDataSource;
import com.ebay.cloud.cms.query.exception.QueryException;
import com.ebay.cloud.cms.query.service.IQueryResult;
import com.ebay.cloud.cms.query.service.IQueryService;
import com.ebay.cloud.cms.query.service.QueryContext;
import com.ebay.cloud.cms.query.service.QueryContext.QueryCursor;
import com.ebay.cloud.cms.query.service.QueryContext.SortOrder;
import com.ebay.cloud.cms.query.service.impl.QueryServiceImpl;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.MongoClient;

/**
 * @author shuachen
 * 
 *         2014-3-11
 */
public class MaxApplication {

	private static final Logger logger = LoggerFactory.getLogger(MaxApplication.class);

	private static final ObjectMapper mapper = new ObjectMapper();

	static class StatisticsData {
		String implementationName;
		String repoName;
		String branchName;
		String metaClassNameWithMaxIndexes;
		long maxIndexes;
		long repoDataSize;
		long maxDocumentSize;
		long maxIndexedArraySize;
		String maxIndexedArraySizeDocument;
		String maxDocumentSizeDocument;
		List<String> indexNames;

		public String getImplementationName() {
			return implementationName;
		}

		public void setImplementationName(String implementationName) {
			this.implementationName = implementationName;
		}

		public String getRepoName() {
			return repoName;
		}

		public void setRepoName(String repoName) {
			this.repoName = repoName;
		}

		public String getBranchName() {
			return branchName;
		}

		public void setBranchName(String branchName) {
			this.branchName = branchName;
		}

		public String getMetaClassNameWithMaxIndexes() {
			return metaClassNameWithMaxIndexes;
		}

		public void setMetaClassNameWithMaxIndexes(String metaClassNameWithMaxIndexes) {
			this.metaClassNameWithMaxIndexes = metaClassNameWithMaxIndexes;
		}

		public long getMaxIndexes() {
			return maxIndexes;
		}

		public void setMaxIndexes(long maxIndexes) {
			this.maxIndexes = maxIndexes;
		}

		public long getRepoDataSize() {
			return repoDataSize;
		}

		public void setRepoDataSize(long repoDataSize) {
			this.repoDataSize = repoDataSize;
		}

		public long getMaxDocumentSize() {
			return maxDocumentSize;
		}

		public void setMaxDocumentSize(long maxDocumentSize) {
			this.maxDocumentSize = maxDocumentSize;
		}

		public long getMaxIndexedArraySize() {
			return maxIndexedArraySize;
		}

		public void setMaxIndexedArraySize(long maxIndexedArraySize) {
			this.maxIndexedArraySize = maxIndexedArraySize;
		}

		public String getMaxIndexedArraySizeDocument() {
			return maxIndexedArraySizeDocument;
		}

		public void setMaxIndexedArraySizeDocument(String maxIndexedArraySizeDocument) {
			this.maxIndexedArraySizeDocument = maxIndexedArraySizeDocument;
		}

		public String getMaxDocumentSizeDocument() {
			return maxDocumentSizeDocument;
		}

		public void setMaxDocumentSizeDocument(String maxDocumentSizeDocument) {
			this.maxDocumentSizeDocument = maxDocumentSizeDocument;
		}

		public List<String> getIndexNames() {
			return indexNames;
		}

		public void setIndexNames(List<String> indexNames) {
			this.indexNames = indexNames;
		}

	}
	
	public static class RegistrationUtils {

		private static List<PersistenceService.Registration> implementations;

		public static List<Registration> getTestDalImplemantation(
				MongoDataSource dataSource) {
			if (implementations == null) {
				Registration reg = new Registration("hierarchy",
						new PersistenceServiceImpl(dataSource), BsonEntity.class,
						DalEntityFactory.getInstance(),
						DalSearchStrategy.getInstance(),
						EntityIDHelper.getInstance(), new CollectionFinder());

				Registration fReg = new Registration("flatten",
						new NewPersistenceServiceImpl(dataSource),
						NewBsonEntity.class, NewDalEntityFactory.getInstance(),
						NewDalSearchStrategy.getInstance(),
						FlattenEntityIDHelper.getInstance(),
						new NewCollectionFinder());

				implementations = new ArrayList<PersistenceService.Registration>();
				implementations.add(reg);
				implementations.add(fReg);
			}
			return implementations;
		}
		
		public static Registration getDefaultDalImplementation(MongoDataSource dataSource) {
			if (implementations == null) {
				getTestDalImplemantation(dataSource);
			}
			return implementations.get(0);
		}

	}

	public static void main(String[] args) throws Exception {
		String connectionString = "stratuscmslvsmdbd1.db.stratus.ebay.com:27017,stratuscmslvsmdbd2.db.stratus.ebay.com:27017,stratuscmslvsmdbd3.db.stratus.ebay.com:27017";
		MongoDataSource bootStrapDs = new MongoDataSource(connectionString);
		CMSDBConfig dbConfig = new CMSDBConfig(bootStrapDs);
		MongoDataSource dataSource = new MongoDataSource(connectionString, dbConfig);
		List<PersistenceService.Registration> implementations = getTestDalImplemantation(dataSource);
		IRepositoryService repositoryService = RepositoryServiceFactory.createRepositoryService(dataSource,
				"localCMSServer");
		ISearchService searchService = new SearchServiceImpl(dataSource);
		IBranchService branchService = ServiceFactory.getBranchService(dataSource, implementations);
		IQueryService queryService = new QueryServiceImpl(repositoryService, branchService, searchService);
		String[] repoNames = { "cmsdb" };
		MongoClient client = dataSource.getMongoInstance();
		for (String repoName : repoNames) {
			printSystemLimitationStatistics(implementations, repositoryService, branchService, queryService, dbConfig,
					repoName, client);
		}
	}
	
	private static List<PersistenceService.Registration> implementations;

	public static List<Registration> getTestDalImplemantation(
			MongoDataSource dataSource) {
		if (implementations == null) {
			Registration reg = new Registration("hierarchy",
					new PersistenceServiceImpl(dataSource), BsonEntity.class,
					DalEntityFactory.getInstance(),
					DalSearchStrategy.getInstance(),
					EntityIDHelper.getInstance(), new CollectionFinder());

			Registration fReg = new Registration("flatten",
					new NewPersistenceServiceImpl(dataSource),
					NewBsonEntity.class, NewDalEntityFactory.getInstance(),
					NewDalSearchStrategy.getInstance(),
					FlattenEntityIDHelper.getInstance(),
					new NewCollectionFinder());

			implementations = new ArrayList<PersistenceService.Registration>();
			implementations.add(reg);
			implementations.add(fReg);
		}
		return implementations;
	}

	private static void printSystemLimitationStatistics(List<PersistenceService.Registration> implementations,
			IRepositoryService repositoryService, IBranchService branchService, IQueryService queryService,
			CMSDBConfig dbConfig, String repoName, MongoClient client) throws Exception {
		long repoDataSize = 0;
		DB db = client.getDB(repoName);
		if (db != null) {
			CommandResult cr = db.getStats();
			if (cr != null) {
				repoDataSize = ((Number) cr.get("dataSize")).longValue();
			}
		}
		List<StatisticsData> datas = new ArrayList<StatisticsData>();
		for (PersistenceService.Registration implementation : implementations) {
			EntityContext entityContext = new EntityContext();
			entityContext.setSourceIp("127.0.0.1");
			entityContext.setModifier("maxApplication");
			entityContext.setDbConfig(dbConfig);
			entityContext.setRegistration(implementation);
			entityContext.setFetchFieldProperty(true);
			try {
				List<IBranch> branches = branchService.getMainBranches(repoName, entityContext);
				for (IBranch branch : branches) {
					String branchName = branch.getId();
					if (!branchName.equals("main")) {
						continue;
					}
					IMetadataService metaService = repositoryService.getRepository(repoName).getMetadataService();
					List<MetaClass> metaClasses = metaService.getMetaClasses(new MetadataContext());
					long maxIndexes = 0;
					String metaClassNameWithMaxIndexes = "";
					for (MetaClass metaClass : metaClasses) {
						Collection<IndexInfo> indexes = metaClass.getIndexes();
						int currentIndexes = indexes.size();
						if (maxIndexes < currentIndexes) {
							maxIndexes = currentIndexes;
							metaClassNameWithMaxIndexes = metaClass.getName();
						}
					}
					MetaClass maxIndexesMetaClass = metaService.getMetaClass(metaClassNameWithMaxIndexes);
					Collection<IndexInfo> indexes = maxIndexesMetaClass.getIndexes();
					List<String> indexNames = new ArrayList<String>();
					for (IndexInfo index : indexes) {
						indexNames.add(index.getIndexName());
					}

					long maxIndexedArraySize = 0;
					long maxDocumentSize = 0;
					String maxIndexedArraySizeDocument = "";
					String maxDocumentSizeDocument = "";
					for (MetaClass metaClass : metaClasses) {
						// if (!metaClass.getName().equals("VCluster")) {
						// continue;
						// }

						Set<String> keySet = new HashSet<String>();
						indexes = metaClass.getIndexes();
						for (IndexInfo index : indexes) {
							List<String> keys = index.getKeyList();
							for (String key : keys) {
								if (keySet.contains(key)) {
									continue;
								} else {
									keySet.add(key);
								}
								StringBuffer sb = new StringBuffer();
								sb.append(metaClass.getName());
								String[] keyParts = key.split("\\.");
								String sortOnField = null;
								if (keyParts.length > 1) {
									MetaRelationship metaRelationship = (MetaRelationship) metaClass
											.getFieldByName(keyParts[0]);
									MetaClass refMetaClass = metaRelationship.getRefMetaClass();
									for (int i = 1; i < keyParts.length - 1; i++) {
										metaRelationship = (MetaRelationship) refMetaClass.getFieldByName(keyParts[i]);
										refMetaClass = metaRelationship.getRefMetaClass();
									}
									MetaField metaField = refMetaClass.getFieldByName(keyParts[keyParts.length - 1]);
									if (!CardinalityEnum.Many.equals(metaField.getCardinality())) {
										continue;
									}
									for (int i = 0; i < keyParts.length - 1; i++) {
										sb.append(".").append(keyParts[i]);
									}
									sortOnField = keyParts[keyParts.length - 1] + "._length";
									sb.append("{@").append(keyParts[keyParts.length - 1]).append(".$_length}");
								} else {
									MetaField metaField = metaClass.getFieldByName(key);
									if (!CardinalityEnum.Many.equals(metaField.getCardinality())) {
										continue;
									}
									sortOnField = key + "._length";
									sb.append("{@").append(key).append(".$_length}");
								}
								String query = sb.toString();

								QueryContext queryContext = new QueryContext(repoName, branchName);
								queryContext.setConsistentPolicy(ConsistentPolicy.NEAREST);
								queryContext.setRegistration(implementation);
								queryContext.setAllowFullTableScan(true);
								queryContext.setDbConfig(dbConfig);
								queryContext.addSortOrder(SortOrder.desc);
								int[] limits = { 1 };
								queryContext.setLimits(limits);
								queryContext.addSortOn(sortOnField);

								IQueryResult queryResult = null;
								List<IEntity> entities = new ArrayList<IEntity>();
								try {
									queryResult = queryService.query(query, queryContext);
									entities = queryResult.getEntities();
								} catch (QueryException qe) {
									logger.error(qe.getMessage(), qe);
									continue;
								}
								if (!entities.isEmpty()) {
									IEntity entity = entities.get(0);
									JsonNode jsonNode = mapper.readTree(entity.toString());
									if (jsonNode.has(sortOnField)) {
										long currentIndexedArraySize = jsonNode.get(sortOnField).asLong(0L);
										if (maxIndexedArraySize < currentIndexedArraySize) {
											maxIndexedArraySize = currentIndexedArraySize;
											maxIndexedArraySizeDocument = entity.getType() + "/" + entity.getId() + "/"
													+ sortOnField;
										}
									}
								}
							}
						}

						// max document size
						IQueryResult queryResult = null;
						QueryContext queryContext = new QueryContext(repoName, branchName);
						queryContext.setConsistentPolicy(ConsistentPolicy.NEAREST);
						queryContext.setRegistration(implementation);
						queryContext.setAllowFullTableScan(true);
						queryContext.setDbConfig(dbConfig);
						queryContext.setCountOnly(false);
						QueryCursor queryCursor = null;
						do {
							if (null != queryCursor) {
								int[] skips = queryCursor.getSkips();
								int[] limits = queryCursor.getLimits();
								queryContext.setSkips(skips);
								queryContext.setLimits(limits);
							}
							queryResult = queryService.query(metaClass.getName(), queryContext);
							List<IEntity> entities = queryResult.getEntities();
							for (IEntity entity : entities) {
								long currentDocumentSize = entity.getEntitySize();
								if (maxDocumentSize < currentDocumentSize) {
									maxDocumentSize = currentDocumentSize;
									maxDocumentSizeDocument = metaClass.getName() + "/" + entity.getId();
								}
							}
							queryCursor = queryResult.getNextCursor();
						} while (null != queryResult && queryResult.hasMoreResults());
					}
					StatisticsData data = new StatisticsData();
					data.setBranchName(branchName);
					data.setImplementationName(implementation.registrationId);
					data.setIndexNames(indexNames);
					data.setMaxDocumentSize(maxDocumentSize);
					data.setMaxDocumentSizeDocument(maxDocumentSizeDocument);
					data.setMaxIndexedArraySize(maxIndexedArraySize);
					data.setMaxIndexedArraySizeDocument(maxIndexedArraySizeDocument);
					data.setMaxIndexes(maxIndexes);
					data.setMetaClassNameWithMaxIndexes(metaClassNameWithMaxIndexes);
					data.setRepoDataSize(repoDataSize);
					data.setRepoName(repoName);
					datas.add(data);
				}
			} catch (RuntimeException ex) {
				ex.printStackTrace();
				continue;
			}
		}
		
		for (StatisticsData data : datas) {
			logger.error("");
			logger.error("");
			logger.error("==========================");
			logger.error(">>>>> implementation: " + data.getImplementationName() + "\trepoName: " + data.getRepoName()
					+ "\tbranchName: " + data.getBranchName());
			logger.error(">>>>> repository data size: " + data.getRepoDataSize());
			logger.error(">>>>> maxIndexes: " + data.getMaxIndexes() + "\tmetaClassNameWithMaxIndexes: "
					+ data.getMetaClassNameWithMaxIndexes() + "\tindexNames: " + StringUtils.join(data.getIndexNames(), ","));
			logger.error(">>>>> maxDocumentSize: " + data.getMaxDocumentSize() + "\tentity: " + data.getMaxDocumentSizeDocument());
			logger.error(">>>>> maxIndexedArraySize: " + data.getMaxIndexedArraySize() + "\tentity: "
					+ data.getMaxIndexedArraySizeDocument());
		}
	}
}
