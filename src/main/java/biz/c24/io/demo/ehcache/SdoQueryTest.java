package biz.c24.io.demo.ehcache;

import biz.c24.io.api.C24;
import biz.c24.io.demo.Utils;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.SearchAttribute;
import net.sf.ehcache.config.Searchable;
import net.sf.ehcache.search.Attribute;
import net.sf.ehcache.search.Results;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import java.io.File;

public class SdoQueryTest implements Runnable {

    public static void main(String[] args) {
        new SdoQueryTest().run();
    }

    public void run() {

        try {

            CacheManager manager = CacheManager.create();

            CacheConfiguration cacheConfig = new CacheConfiguration("testCache", Integer.MAX_VALUE)
                    .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.FIFO)
                    .eternal(true)
                    .timeToLiveSeconds(0)
                    .timeToIdleSeconds(0);

            Searchable searchable = new Searchable();
            cacheConfig.addSearchable(searchable);

            searchable.addSearchAttribute(new SearchAttribute().name("legalEntityIdentifier").expression("value.getLegalEntityIdentifier()"));

            Cache cache = new Cache(cacheConfig);
            manager.addCache(cache);


            org.plei.prelei_schema.xsd.sdo.LEIDirectory sdo = C24.parse(org.plei.prelei_schema.xsd.sdo.LEIDirectory.class).from(new File("/lei_sample.xml"));

            int i;
            for(i = 0; i < 1000000; i++) {
                cache.put(new Element(i, Utils.cloneSdoWithIdentifier(sdo.getLEIRegistrations().getLEIRegistration(0), i)));
                if(cache.getSize() != i+1) {
                    // We're full
                    break;
                }

                if(i % 10000 == 0) {
                    System.out.println(i);
                }

            }

            if(i < 1000000) {
                throw new RuntimeException("Cached overflowed");
            }

            Attribute<String> identifier = cache.getSearchAttribute("legalEntityIdentifier");

            for(i = 0; i < 10; i++) {
                Results results = cache.createQuery().includeValues().addCriteria(identifier.eq("00000000000000000999")).execute();
                if(results.size() != 1) {
                    throw new RuntimeException();
                }
            }

            long time = System.currentTimeMillis();
            for(i = 0; i < 10; i++) {
                Results results = cache.createQuery().includeValues().addCriteria(identifier.eq("0000000000000000099" + i)).execute();
                if(results.size() != 1) {
                    throw new RuntimeException();
                }
            }

            time = System.currentTimeMillis() - time;
            System.out.println("10 in " + time);

            CacheManager.getInstance().shutdown();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

}
