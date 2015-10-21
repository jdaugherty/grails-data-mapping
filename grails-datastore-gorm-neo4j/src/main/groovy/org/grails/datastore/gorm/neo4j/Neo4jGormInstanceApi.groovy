/*
 * Copyright 2015 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.grails.datastore.gorm.neo4j

import org.grails.datastore.gorm.GormInstanceApi
import org.grails.datastore.mapping.core.Datastore
import org.grails.datastore.mapping.dirty.checking.DirtyCheckable
import org.grails.datastore.mapping.model.config.GormProperties


/**
 * Adds instance methods specified to Neo4j
 *
 * @param < D > The domain class type
 */
class Neo4jGormInstanceApi<D> extends GormInstanceApi<D> {

    Neo4jGormInstanceApi(Class<D> persistentClass, Datastore datastore) {
        super(persistentClass, datastore)
    }

    /**
     * Allows accessing to dynamic properties with the dot operator
     *
     * @param instance The instance
     * @param name The property name
     * @return The property value
     */
    def propertyMissing(D instance, String name) {

        def unwrappedInstance = unwrappedInstance(instance)

        MetaProperty mp = unwrappedInstance.hasProperty(Neo4jGormEnhancer.UNDECLARED_PROPERTIES);
        mp ? mp.getProperty(unwrappedInstance)[name] : null
    }

    /**
     * dealing with undeclared properties must not happen on proxied instances
     * @param instance
     * @return the unwrapped instance
     */
    private D unwrappedInstance(D instance) {
        def proxyFactory = datastore.mappingContext.proxyFactory
        proxyFactory.unwrap(instance)
    }

    /**
     * Allows setting a dynamic property via the dot operator
     * @param instance The instance
     * @param name The property name
     * @param val The value
     */
    def propertyMissing(D instance, String name, val) {

        def unwrappedInstance = unwrappedInstance(instance)

        if (name == Neo4jGormEnhancer.UNDECLARED_PROPERTIES) {
            unwrappedInstance.metaClass."${Neo4jGormEnhancer.UNDECLARED_PROPERTIES}" = val
        } else {
            MetaProperty mp = unwrappedInstance.hasProperty(Neo4jGormEnhancer.UNDECLARED_PROPERTIES);
            Map undeclaredProps
            if (mp) {
                undeclaredProps = mp.getProperty(unwrappedInstance)
            } else {
                undeclaredProps = [:]
                unwrappedInstance.metaClass."${Neo4jGormEnhancer.UNDECLARED_PROPERTIES}" = undeclaredProps
            }
            (val == null) ? undeclaredProps.remove(name) : undeclaredProps.put(name, val)
            if (datastore.mappingContext.isPersistentEntity(val)) {
                val.save()
            } else if (Neo4jGormEnhancer.isCollectionWithPersistentEntities(val, datastore.mappingContext)) {
                val.each { it.save() }
            }
            if (unwrappedInstance instanceof DirtyCheckable) {
                ((DirtyCheckable)unwrappedInstance).markDirty(name)
            }
        }
    }

    /**
     * Allows subscript access to schemaless attributes.
     *
     * @param instance The instance
     * @param name The name of the field
     */
    void putAt(D instance, String name, value) {
        instance."$name" = value

    }

    /**
     * Allows subscript access to schemaless attributes.
     *
     * @param instance The instance
     * @param name The name of the field
     * @return the value
     */
    def getAt(D instance, String name) {
        instance."$name"
    }

    /**
     * perform a cypher query
     * @param queryString
     * @param params
     * @return
     */
    def cypher(instance, String queryString, Map params ) {
        params['this'] = instance.id
        ((Neo4jDatastore)datastore).graphDatabaseService.execute(queryString, params)
    }

    /**
     * perform a cypher query
     * @param queryString
     * @param params
     * @return
     */
    def cypher(instance, String queryString, List params ) {
        Map paramsMap = new LinkedHashMap()
        paramsMap.put("this", instance.id)
        int i = 0
        for(p in params) {
            paramsMap.put(String.valueOf(++i), p)
        }
        ((Neo4jDatastore)datastore).graphDatabaseService.execute(queryString, paramsMap)
    }

    /**
     * perform a cypher query
     * @param queryString
     * @return
     */
    def cypher(instance, String queryString) {
        ((Neo4jDatastore)datastore).graphDatabaseService.execute(queryString, Collections.singletonMap("this", instance.id))
    }

}
