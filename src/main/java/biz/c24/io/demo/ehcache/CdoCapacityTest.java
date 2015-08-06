package biz.c24.io.demo.ehcache;

import biz.c24.io.api.C24;
import biz.c24.io.demo.Utils;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import org.plei.prelei_schema.xsd.LEIDirectory;

import java.io.File;

public class CdoCapacityTest implements Runnable {
    
    public static void main(String[] args) {
        new CdoCapacityTest().run();
    }
    
    public void run() {
        
        try {
        
            CacheManager manager = CacheManager.create();
            
            Cache cache = new Cache(
                    new CacheConfiguration("testCache", Integer.MAX_VALUE)
                      .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.FIFO)
                      .eternal(true)
                      .timeToLiveSeconds(0)
                      .timeToIdleSeconds(0));
            manager.addCache(cache);
            
            LEIDirectory cdo = C24.parse(LEIDirectory.class).from(new File("/lei_sample.xml"));
            
            int i;
            for(i = 0; i < Integer.MAX_VALUE; i++) {
                cache.put(new Element(i, Utils.cloneWithIdentifier(cdo.getLEIRegistrations().getLEIRegistration(0), i)));
                if(cache.getSize() != i+1) {
                    // We're full
                    break;
                }
                
                if(i % 10000 == 0) {
                    System.out.println(i);
                }
//                if(i % 100000 == 0) {
//                    System.out.println("Heap Size (in Bytes): " + cache.getStatistics().getLocalHeapSizeInBytes());
//                }
                
            }
            
            System.out.println("Capacity " + i);
            
            CacheManager.getInstance().shutdown();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

}
