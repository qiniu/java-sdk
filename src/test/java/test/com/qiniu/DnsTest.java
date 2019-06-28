package test.com.qiniu;

import com.qiniu.common.Zone;
import com.qiniu.http.Dns;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import qiniu.happydns.DnsClient;
import qiniu.happydns.Domain;
import qiniu.happydns.IResolver;
import qiniu.happydns.local.Resolver;
import qiniu.happydns.local.SystemDnsServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// example
public class DnsTest {
    private Configuration config;
    private UploadManager uploadManager;

    public void setUp() throws UnknownHostException {
        IResolver[] resolvers = new IResolver[2];
        resolvers[0] = SystemDnsServer.defaultResolver();
        resolvers[1] = new Resolver(InetAddress.getByName("119.29.29.29"));
        final DnsClient dnsClient = new DnsClient(resolvers);

        config = new Configuration();
        config.zone = Zone.zone0();
        config.dns = new Dns() {
            @Override
            public List<InetAddress> lookup(String hostname) throws UnknownHostException {
                InetAddress[] ips;
                try {
                    Domain domain = new Domain(hostname, true, true);
                    ips = dnsClient.queryInetAddress(domain);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new UnknownHostException(e.getMessage());
                }
                if (ips == null) {
                    throw new UnknownHostException(hostname + " resolve failed.");
                }
                List<InetAddress> l = new ArrayList<>(ips.length);
                Collections.addAll(l, ips);
                return l;
            }
        };

        uploadManager = new UploadManager(config);
    }

    //@Test
    public void testSome() {
        // uploadManager.xxxxx
    }
}
