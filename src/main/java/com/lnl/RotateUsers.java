package com.lnl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetRandomPasswordRequest;
import com.amazonaws.services.secretsmanager.model.GetRandomPasswordResult;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.PutSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.PutSecretValueResult;
import com.amazonaws.services.secretsmanager.model.UpdateSecretVersionStageRequest;
import com.amazonaws.services.secretsmanager.model.UpdateSecretVersionStageResult;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lnl.entity.Event;
import com.lnl.entity.SecretValues;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class RotateUsers implements RequestHandler<Map<Object, String>, String> {

	String secretName = "mongo-user";// "mongodb-creds";
	ObjectMapper mapper = new ObjectMapper();
	LambdaLogger logger;
	String region = "us-east-2";
	MongoClient mongo /* = new MongoClient("localhost", 27017) */;

	@Override
	public String handleRequest(Map<Object, String> event, Context context) {
		logger = context.getLogger();
		logger.log("inside handleReuqest");
		AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard().withRegion(region).build();

		String step = event.get("Step");
		String token = event.get("ClientRequestToken");
		String arn = event.get("SecretId");

		logger.log("Events  " + event + "");

		switch (step) {
		case "createSecret":
			logger.log("creating secret");
			try {
				createSecret(client, arn, token);
				logger.log("Secret creation completed");
			} catch (Exception e) {
				e.printStackTrace();
			}

			 break;
		case "setSecret":
			logger.log("setting secret");
			try {
				setSecret(client, arn);
				logger.log("Secret setup completed");
			} catch (Exception e) {
				e.printStackTrace();
			}
			 break;
		case "testSecret":
			logger.log("testing secret");
			try {
				testSecret(client, arn);
				logger.log("Secret testing completed");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 break;
		case "finishSecret":
			logger.log("changing label of secret");
			finishSecret(client, arn, token);
			logger.log("New secret updated");

			break;

		}

		return null;
	}

	static int i = 1;

	private void createSecret(AWSSecretsManager client, String arn, String token)
			throws JsonParseException, JsonMappingException, IOException {
		GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(secretName);
		GetSecretValueResult getSecretValueResult = client.getSecretValue(getSecretValueRequest);
		SecretValues values = mapper.readValue(getSecretValueResult.getSecretString(), SecretValues.class);

		// create new password
		/*
		 * GetRandomPasswordRequest getRandomPasswordRequest = new
		 * GetRandomPasswordRequest(); getRandomPasswordRequest.setPasswordLength(32L);
		 * getRandomPasswordRequest.setExcludePunctuation(true); GetRandomPasswordResult
		 * randomPasswordResult = client.getRandomPassword(getRandomPasswordRequest);
		 * String randomPassword = randomPasswordResult.getRandomPassword();
		 */
		String randomPassword = "test1";
		//logger.log("Existing data " + getSecretValueResult.getSecretString());

		//logger.log("Overriding new password");
		values.setPassword(randomPassword);
		//logger.log("Update test data " + mapper.writeValueAsString(values));
		List<String> versionStage = new ArrayList<String>();
		versionStage.add("AWSPENDING");
		// creating new secret with random password
		PutSecretValueRequest putSecretValueRequest = new PutSecretValueRequest();
		// putSecretValueRequest.setClientRequestToken(token);
		putSecretValueRequest.setSecretString(mapper.writeValueAsString(values));
		putSecretValueRequest.setVersionStages(versionStage);
		putSecretValueRequest.setSecretId(arn);
		putSecretValueRequest.setClientRequestToken(token);
		/* PutSecretValueResult data = */client.putSecretValue(putSecretValueRequest);
		logger.log("Secret value created");
	}

	private void setSecret(AWSSecretsManager client, String arn)
			throws JsonParseException, JsonMappingException, IOException {
		GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withVersionStage("AWSPENDING")
				.withSecretId(arn);
		GetSecretValueResult getSecretValueResult = client.getSecretValue(getSecretValueRequest);
		SecretValues newSecreteValue = mapper.readValue(getSecretValueResult.getSecretString(), SecretValues.class);

		logger.log("Pending secret value " + newSecreteValue);
		// get master details
		GetSecretValueRequest getMasterSecretValueRequest = new GetSecretValueRequest().withVersionStage("AWSCURRENT")
				.withSecretId(newSecreteValue.getMasterarn());
		GetSecretValueResult getMasterSecretValueResult = client.getSecretValue(getMasterSecretValueRequest);
		SecretValues masterSecreteValue = mapper.readValue(getMasterSecretValueResult.getSecretString(),
				SecretValues.class);
		logger.log("mongo creds " + masterSecreteValue.getUsername());
		String uri = "mongodb://" + masterSecreteValue.getUsername() + ":" + masterSecreteValue.getPassword() + "@"
				+ newSecreteValue.getIpaddress() + ":27017";
		MongoClientURI mongoUri = new MongoClientURI(uri);
		mongo = new MongoClient(mongoUri);
		//logger.log("Mongoclient created");

		createMongoUsers(newSecreteValue.getUsername(), newSecreteValue.getPassword());

		//logger.log("In this step need to configure new secret in db or other application ");

	}

	private void createMongoUsers(String username, String password) {
		logger.log("creating new users");
		String dbName = "test";
		// mongo = new MongoClient("10.30.1.113", 27017);
		MongoDatabase db = mongo.getDatabase(dbName);
		BasicDBObject createUserCommand = new BasicDBObject("createUser", username).append("pwd", password).append(
				"roles", Collections.singletonList(new BasicDBObject("role", "readWrite").append("db", dbName)));
		
		//db.updateUser("root",  {pwd: "abcde12345"} );
		BasicDBObject createUserCommand1 = new BasicDBObject("updateUser", username).append("pwd", password);
		db.runCommand(createUserCommand1);
		//logger.log("created new user,closing connection");

		mongo.close();
	}

	private void testSecret(AWSSecretsManager client, String arn)
			throws JsonParseException, JsonMappingException, IOException {
		GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withVersionStage("AWSPENDING")
				.withSecretId(arn);
		GetSecretValueResult getSecretValueResult = client.getSecretValue(getSecretValueRequest);
		SecretValues newSecreteValue = mapper.readValue(getSecretValueResult.getSecretString(), SecretValues.class);
		logger.log("Testing new user");

		String uri = "mongodb://" + newSecreteValue.getUsername() + ":" + newSecreteValue.getPassword() + "@"
				+ newSecreteValue.getIpaddress() + ":27017/test";
		MongoClientURI mongoUri = new MongoClientURI(uri);
		mongo = new MongoClient(mongoUri);
		// check readWrite status
		MongoDatabase db = mongo.getDatabase("test");
		// code issue to check create operation through new user

		db.createCollection("employeeTest");
		MongoCollection collection = db.getCollection("employeeTest", BasicDBObject.class);
		collection.insertOne(new BasicDBObject("key", "value"));
		logger.log("Data inserted with new users");

		List<BasicDBObject> documents = (List<BasicDBObject>) collection.find().into(new ArrayList<BasicDBObject>());
		for (BasicDBObject document : documents) {
			if (!document.get("key").equals("value")) {
				logger.log("Fail to test newly created users,data retrival failed,reverting all operations!!!");
				mongo.close();
				return;
			}
		}
		collection.drop();
		mongo.close();
		//logger.log("Test connection with updated secrets");

	}

	private void finishSecret(AWSSecretsManager client, String arn, String token) {
		logger.log("Final step to update version of new secret from pending to current");
		GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest();
		getSecretValueRequest.setSecretId(arn);
		getSecretValueRequest.setVersionStage("AWSCURRENT");
		GetSecretValueResult getSecretValueResult = client.getSecretValue(getSecretValueRequest);
		String versionId = getSecretValueResult.getVersionId();
		logger.log(getSecretValueResult.getSecretString());
		logger.log("version id :"+versionId);
		UpdateSecretVersionStageRequest updateSecretVersionStageRequest = new UpdateSecretVersionStageRequest();
		updateSecretVersionStageRequest.setSecretId(arn);
		updateSecretVersionStageRequest.setMoveToVersionId(token);
		updateSecretVersionStageRequest.setRemoveFromVersionId(versionId);
		updateSecretVersionStageRequest.setVersionStage("AWSCURRENT");
		UpdateSecretVersionStageResult updatedResult = client.updateSecretVersionStage(updateSecretVersionStageRequest);
		logger.log("update completed");
	}
}
