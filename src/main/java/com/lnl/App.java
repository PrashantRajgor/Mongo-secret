package com.lnl;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	
    	String uri = "mongodb://" + "LnL02" + ":" + "test1" + "@"
				+ "3.16.46.133" + ":27017/test";
    	
    	MongoClientURI mongoUri = new MongoClientURI(uri);
    	MongoClient mongo = new MongoClient(mongoUri);
		// check readWrite status
		MongoDatabase db = mongo.getDatabase("test");
		// code issue to check create operation through new user

		db.createCollection("employeeTest");
		System.out.println("done");
		
		
		MongoCollection collection = db.getCollection("employeeTest", BasicDBObject.class);
		collection.insertOne(new BasicDBObject("key", "value"));
		System.out.println("Data inserted with new users");

		List<BasicDBObject> documents = (List<BasicDBObject>) collection.find().into(new ArrayList<BasicDBObject>());
		for (BasicDBObject document : documents) {
			if (!document.get("key").equals("value")) {
				System.out.println("Fail to test newly created users,data retrival failed,reverting all operations!!!");
				mongo.close();
				return;
			}
		}
		
		
		
		/*
		 * MongoClient mongo = new MongoClient("3.134.116.147", 27017); DB db =
		 * mongo.getDB("test");
		 * 
		 * DBCollection col = db.getCollection("employee");
		 * System.out.println(col.findOne());
		 */
    }
}
