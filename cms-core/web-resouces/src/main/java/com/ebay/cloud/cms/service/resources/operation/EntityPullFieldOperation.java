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
package com.ebay.cloud.cms.service.resources.operation;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriInfo;

import com.ebay.cloud.cms.dal.entity.IEntity;
import com.ebay.cloud.cms.entmgr.entity.EntityContext;
import com.ebay.cloud.cms.entmgr.entity.EntityContext.ModifyAction;
import com.ebay.cloud.cms.sysmgmt.server.CMSServer;

public class EntityPullFieldOperation extends EntityFieldOperation {

    public EntityPullFieldOperation(CMSServer server, String mode, HttpServletRequest request, String priority,
            String consistPolicy, String repoName, String metadata, String branch, String oid, String jsonBody,
            String fieldName, String errorMsg, UriInfo uriInfo) {
        super(server, mode, request, priority, consistPolicy, repoName, metadata, branch, oid, jsonBody, fieldName,
                errorMsg, uriInfo);
    }

    @Override
    protected boolean ignoreEmptyPayload() {
        return true;
    }

    @Override
    protected void performAction(EntityContext context, IEntity entity, IEntity queryEntity) {
        context.setModifyAction(ModifyAction.PULLFIELD);
        cmsServer.pullField(priority, queryEntity, entity, fieldName, context);
    }
}

