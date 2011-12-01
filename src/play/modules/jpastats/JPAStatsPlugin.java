package play.modules.jpastats;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.stat.Statistics;

import play.Play;
import play.PlayPlugin;
import play.Play.Mode;
import play.db.jpa.JPA;
import play.mvc.Before;
import play.mvc.Http;
import play.mvc.Http.Response;

public class JPAStatsPlugin extends PlayPlugin {
    private static ThreadLocal<Long> databaseQueryCount = new ThreadLocal<Long>();
    private static ThreadLocal<Long> hibernateQueryCount = new ThreadLocal<Long>();
    private static ThreadLocal<Long> collectionFetchCount = new ThreadLocal<Long>();
    private static ThreadLocal<Long> entityFetchCount = new ThreadLocal<Long>();
    private static ThreadLocal<Statistics> statistics = new ThreadLocal<Statistics>();
    
    private boolean enabled;
    private boolean headers;
    
    public void onConfigurationRead() {
        enabled = Boolean.valueOf(Play.configuration.getProperty("jpastats.enabled", "true"));
        headers = Boolean.valueOf(Play.configuration.getProperty("jpastats.headers", "false"));
    }
    
    @Override
    public void beforeActionInvocation(Method m) {
        if(enabled) {
            Statistics stats = ((Session) JPA.em().getDelegate()).getSessionFactory().getStatistics();
            stats.setStatisticsEnabled(true);
            statistics.set(stats);
        }
        
        databaseQueryCount.set(currentDatabaseQueryCount());
        hibernateQueryCount.set(currentHibernateQueryCount());
        collectionFetchCount.set(currentCollectionFetchCount());
        entityFetchCount.set(currentEntityFetchCount());
    }
    
    @Override
    public void afterActionInvocation() {
        if(headers) {
            addHeadersToResponse();
        }
    }
    
    private void addHeadersToResponse() {
        List<Http.Header> headers = new ArrayList<Http.Header>(4);
        headers.add(new Http.Header("X-Database-Queries", (new Long(getDatabaseQueryCount())).toString()));
        headers.add(new Http.Header("X-Hibernate-Queries", (new Long(getHibernateQueryCount())).toString()));
        headers.add(new Http.Header("X-Collection-Fetches", (new Long(getCollectionFetchCount())).toString()));
        headers.add(new Http.Header("X-Entity-Fetches", (new Long(getEntityFetchCount())).toString()));
        
        for(Http.Header header : headers) {
            Response.current().headers.put(header.name, header);
        }
    }
    
    public static long getDatabaseQueryCount() {
        return currentDatabaseQueryCount() - databaseQueryCount.get();
    }
    
    public static long getHibernateQueryCount() {
        return currentHibernateQueryCount() - hibernateQueryCount.get();
    }
    
    public static long getCollectionFetchCount() {
        return currentCollectionFetchCount() - collectionFetchCount.get();
    }
    
    public static long getEntityFetchCount() {
        return currentEntityFetchCount() - entityFetchCount.get();
    }
    
    protected static long currentDatabaseQueryCount() {
        try {
            return statistics.get().getPrepareStatementCount();
        } catch(Exception e) {
            return 0;
        }
    }
    
    protected static long currentHibernateQueryCount() {
        try {
            return statistics.get().getQueryExecutionCount();
        } catch(Exception e) {
            return 0;
        }
    }
    
    protected static long currentCollectionFetchCount() {
        try {
            return statistics.get().getCollectionFetchCount();
        } catch(Exception e) {
            return 0;
        }
    }
    
    protected static long currentEntityFetchCount() {
        try {
            return statistics.get().getEntityFetchCount();
        } catch(Exception e) {
            return 0;
        }
    }
}
