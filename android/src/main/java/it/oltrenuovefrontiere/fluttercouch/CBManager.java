package it.oltrenuovefrontiere.fluttercouch;

import com.couchbase.lite.BasicAuthenticator;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Document;
import com.couchbase.lite.Endpoint;
import com.couchbase.lite.LogDomain;
import com.couchbase.lite.LogLevel;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Replicator;
import com.couchbase.lite.ReplicatorConfiguration;
import com.couchbase.lite.SessionAuthenticator;
import com.couchbase.lite.URLEndpoint;
import com.couchbase.litecore.C4Replicator;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CBManager {
    private static final CBManager mInstance = new CBManager();
    private HashMap<String, Database> mDatabase = new HashMap<>();
    private ReplicatorConfiguration mReplConfig;
    private Replicator mReplicator;
    private String defaultDatabase = "defaultDatabase";

    private CBManager() {
    }

    public static CBManager getInstance() {
        return mInstance;
    }

    public Database getDatabase() {
        return mDatabase.get(defaultDatabase);
    }

    public Database getDatabase(String name) {
        if (mDatabase.containsKey(name)) {
            return mDatabase.get(name);
        }
        return null;
    }

    public String saveDocument(Map<String, Object> _map) throws CouchbaseLiteException {
        MutableDocument mutableDoc = new MutableDocument(_map);
        mDatabase.get(defaultDatabase).save(mutableDoc);
        return mutableDoc.getId();
    }

    public String saveDocumentWithId(String _id, Map<String, Object> _map) throws CouchbaseLiteException {
        MutableDocument mutableDoc = new MutableDocument(_id, _map);
        mDatabase.get(defaultDatabase).save(mutableDoc);
        return mutableDoc.getId();
    }

    public Map<String, Object> getDocumentWithId(String _id) throws CouchbaseLiteException {
        Database defaultDb = getDatabase();
        HashMap<String, Object> resultMap = new HashMap<String, Object>();
        if (defaultDatabase != null) {
            try {
                Document document = defaultDb.getDocument(_id);
                if (document != null) {
                    resultMap.put("doc", document.toMap());
                    resultMap.put("id", _id);
                } else {
                    resultMap.put("doc", null);
                    resultMap.put("id", _id);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resultMap;
    }

    public void initDatabaseWithName(String _name) throws CouchbaseLiteException {
        DatabaseConfiguration config = new DatabaseConfiguration(FluttercouchPlugin.context);
        if (!mDatabase.containsKey(_name)) {
            defaultDatabase = _name;
            // Database.setLogLevel(LogDomain.REPLICATOR, LogLevel.VERBOSE);
            mDatabase.put(_name, new Database(_name, config));
        }
    }

    public String setReplicatorEndpoint(String _endpoint) throws URISyntaxException {
        Endpoint targetEndpoint = new URLEndpoint(new URI(_endpoint));
        mReplConfig = new ReplicatorConfiguration(mDatabase.get(defaultDatabase), targetEndpoint);
        return mReplConfig.getTarget().toString();
    }

    public String setReplicatorType(String _type) {
        ReplicatorConfiguration.ReplicatorType settedType = ReplicatorConfiguration.ReplicatorType.PULL;
        switch (_type) {
            case "PUSH":
                settedType = ReplicatorConfiguration.ReplicatorType.PUSH;
                break;
            case "PULL":
                settedType = ReplicatorConfiguration.ReplicatorType.PULL;
                break;
            case "PUSH_AND_PULL":
                settedType = ReplicatorConfiguration.ReplicatorType.PUSH_AND_PULL;
                break;
        }
        mReplConfig.setReplicatorType(settedType);
        return settedType.toString();
    }

    public List<String> setReplicatorChannels(List<String> channels){
        if(mReplConfig == null) throw new NullPointerException("No Replicator Configuration");
        mReplConfig.setChannels(channels);
        return mReplConfig.getChannels();
    }

    public List<String> setDocumentIDs(List<String> documentIDs){
        if(mReplConfig == null) throw new NullPointerException("No Replicator Configuration");
        mReplConfig.setDocumentIDs(documentIDs);
        return mReplConfig.getDocumentIDs();
    }

    public Map<String,String> setHeaders(Map<String,String> headers){
        if(mReplConfig == null) throw new NullPointerException("No Replicator Configuration");
        mReplConfig.setHeaders(headers);
        return mReplConfig.getHeaders();
    }

    public byte[] setPinnedServerCertificate(byte[] pinnedServerCertificate){
        if(mReplConfig == null) throw new NullPointerException("No Replicator Configuration");
        mReplConfig.setPinnedServerCertificate(pinnedServerCertificate);
        return mReplConfig.getPinnedServerCertificate();
    }

    public void setPushFilter(String value, String type){
        if(mReplConfig == null) throw new NullPointerException("No Replicator Configuration");
        mReplConfig.setPushFilter((document, flags) -> value.equals(document.getString(type)));
    }

    public void setPullFilter(String value, String type){
        if(mReplConfig == null) throw new NullPointerException("No Replicator Configuration");
        mReplConfig.setPullFilter((document, flags) -> value.equals(document.getString(type)));
    }

    public String setReplicatorBasicAuthentication(Map<String, String> _auth) throws Exception {
        if (_auth.containsKey("username") && _auth.containsKey("password")) {
            mReplConfig.setAuthenticator(new BasicAuthenticator(_auth.get("username"), _auth.get("password")));
        } else {
            throw new Exception();
        }
        return mReplConfig.getAuthenticator().toString();
    }

    public String setReplicatorSessionAuthentication(String sessionID) throws Exception {
        if (sessionID != null) {
            mReplConfig.setAuthenticator(new SessionAuthenticator(sessionID));
        } else {
            throw new Exception();
        }
        return mReplConfig.getAuthenticator().toString();
    }

    public boolean setReplicatorContinuous(boolean _continuous) {
        mReplConfig.setContinuous(_continuous);
        return mReplConfig.isContinuous();
    }

    public void initReplicator() {
        mReplicator = new Replicator(mReplConfig);
    }

    public void startReplicator() {
        mReplicator.start();
    }

    public void stopReplicator() {
        mReplicator.stop();
        mReplicator = null;
    }

    public Replicator getReplicator() {
        return mReplicator;
    }
}
