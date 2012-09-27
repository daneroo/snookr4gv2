
package net.snookr.db;

// Db4o
import com.db4o.*;
import com.db4o.ext.ExtDb4o;
import com.db4o.query.*;
import com.db4o.constraints.UniqueFieldValueConstraint;

import net.snookr.util.Environment;
import net.snookr.model.FSImage;
import net.snookr.model.FlickrImage;

class Database {
    //TODO make this private again...
    public ObjectContainer oc;
    Database() {
        //println "-=-=-= Open Database: ${Environment.yapFile} =-=-=-"
        Db4o.configure().generateVersionNumbers(Integer.MAX_VALUE);
        Db4o.configure().generateUUIDs(Integer.MAX_VALUE);

        Db4o.configure().add(new UniqueFieldValueConstraint(FSImage.class,"fileName"));
        Db4o.configure().objectClass(FSImage.class).objectField("fileName").indexed(true);
        Db4o.configure().objectClass(FSImage.class).objectField("md5").indexed(true);

        Db4o.configure().add(new UniqueFieldValueConstraint(FlickrImage.class,"photoid"));
        Db4o.configure().objectClass(FlickrImage.class).objectField("photoid").indexed(true);
        Db4o.configure().objectClass(FlickrImage.class).objectField("md5").indexed(true);

        oc = Db4o.openFile(Environment.yapFile);

    }

    public void close() {
        oc.close();
    }

    public void printSummary(boolean verbose) {
        // just read everything in the db
        ObjectSet result=oc.get(null);
        println ("database has "+result.size()+" objects");
        def wholeDb = [];
        while(result.hasNext()) { wholeDb << result.next() }
        Map mapByType = [:];
        for ( o in wholeDb) {
            String className = o.getClass().getName();
            int count = mapByType[className];
            mapByType[className] = (count==null)?1:(count+1);
        }
        mapByType.each() { k,v ->
            println "db has ${v} objects of type ${k}"
        }
        if (verbose) {
            for ( o in wholeDb) {
                println (""+o.getClass().getName()+" : "+o);
            }
        }
        
    }

    Object fetchUniqueByValue(Class claz,String fieldName,Object value) {
        /*
        def qbe = claz.newInstance();
        qbe[fieldName] = value;
        ObjectSet result = oc.get(qbe);

         */
        Query query=this.oc.query();
        query.constrain(claz);
        query.descend(fieldName).constrain(value);
        ObjectSet result=query.execute();

        assert result.size()<=1;

        if (result.size()>0) return result.next();
        return null;
    }
    List fetchByValue(Class claz,String fieldName,Object value) {
        /* QBE-based resultSet
        def qbe = claz.newInstance();
        qbe[fieldName] = value;
        ObjectSet result = oc.get(qbe);

         */
        // Query-constrain-based resultSet
        Query query=this.oc.query();
        query.constrain(claz);
        query.descend(fieldName).constrain(value);
        ObjectSet result=query.execute();

        List resultList=[];
        while(result.hasNext()) { resultList << result.next(); }
        return resultList;
    }
    
    int commitCounter=0;
    String save(Object persist) {
        oc.set(persist);
        commitCounter++;
        if ((commitCounter%1000)==0) oc.commit();
    }
    void delete(Object persist) {
        oc.delete(persist);
        commitCounter++;
        if ((commitCounter%1000)==0) oc.commit();
    }

    Map getMapForClassByPrimaryKey(Class claz,String fieldName) {
        ObjectSet result = oc.get(claz)
        //println "found ${result.size()} ${claz.getName()} objects";
        Map mapForClass = [:];
        while(result.hasNext()) { 
            def clazInstance = result.next(); // FSImage, or FlickrImage
            mapForClass[clazInstance[fieldName]]=clazInstance;
        }
        return mapForClass;
        
    }

}
