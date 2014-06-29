#
# Endpoint where security service is deployed for authentication
#
securityServiceUrl=http://phx5qa01c-4a35.stratus.phx.qa.ebay.com:8080
#odbUrl=http://jrds.ccc.qa.ebay.com/jodbweb/services/OdbWebService?wsdl
odbUrl=http://cms.cloud.qa.ebay.com/jodbweb/services/OdbWebService?wsdl
#
# Enable Security
#
secure=true
envSuperUser=bilwu
#
# cms.open configuration
#
cms.mongoConnection=phx5qa01c-2b82.stratus.phx.qa.ebay.com,phx5qa01c-4496.stratus.phx.qa.ebay.com:37017,phx5qa01c-4496.stratus.phx.qa.ebay.com:27017
cms.serverUserName=localCMSServer
cms.calIsCalMode=false
cms.calCalConfig=cal.txt

#
# Dual Write enabled
#
dualWrite=true

# CAL configuration
cal.sendBufferSize=128000
cal.environment=cmsws_dev_envt_new
cal.poolName=cmsws_pool
cal.siteViewAddr=cal.vip.qa.ebay.com


