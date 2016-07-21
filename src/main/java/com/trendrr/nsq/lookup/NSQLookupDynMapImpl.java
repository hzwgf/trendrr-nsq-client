/**
 *
 */
package com.trendrr.nsq.lookup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trendrr.nsq.ConnectionAddress;
import com.trendrr.nsq.NSQLookup;


/**
 * Lookup implementation based on trendrr-oss DynMap
 *
 *
 * @author Dustin Norlander
 * @created Jan 23, 2013
 *
 */
public class NSQLookupDynMapImpl implements NSQLookup {

    protected static Logger log = LoggerFactory.getLogger(NSQLookupDynMapImpl.class);

    //lookup地址
    Set<String> addresses = new HashSet<String> ();


    public void addAddr(String addr, int port) {
        if (!addr.startsWith("http")) {
            addr = "http://" + addr;
        }
        addr = addr + ":" + port;
        this.addresses.add(addr);
    }

    public List<ConnectionAddress> lookup(String topic) {
        HashMap<String, ConnectionAddress> nsqds = new HashMap<String, ConnectionAddress>();
        log.debug("lookup topic:{}",new Object[]{topic});
        for(String lookupd:this.addresses){
        	List<String> temp=query(lookupd,topic);
        	for(String nsqd:temp){
        		ConnectionAddress addr=new ConnectionAddress();
        		String[] pair=nsqd.split(":");
        		addr.setHost(pair[0]);
        		addr.setPort(Integer.parseInt(pair[1]));
        		nsqds.put(nsqd, addr);
        	}
        }
        log.debug("拉取到nsqd地址列表：{}",new Object[]{nsqds});
        return new ArrayList<ConnectionAddress>(nsqds.values());
    }
    
    public List<String> query(String lookupd,String topic) {
//		http://192.168.66.204:4161/lookup?topic=order_topic
		String urlString = lookupd + "/lookup?topic=" + topic;
		URL url = null;
		try {
			url = new URL(urlString);
			InputStream is = url.openStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			return parseResponseForProducers(br);
		} catch (MalformedURLException e) {
			log.error("Malformed Lookupd URL: {}", urlString);
		} catch (IOException e) {
			log.error("Problem reading lookupd response: ", e);
		}
		return new LinkedList<String>();
	}
    
    public static List<String> parseResponseForProducers(Reader response){
		ObjectMapper mapper = new ObjectMapper();
		List<String> outputs = new ArrayList<String>();
		try {	 
//			{"status_code":200,"status_txt":"OK","data":{"channels":["channel3","channel1","channel2"],"producers":[{"remote_address":"127.0.0.1:31669","hostname":"kdt-qa4","broadcast_address":"kdt-qa4","tcp_port":4150,"http_port":4151,"version":"0.3.5"}]}}
			 JsonNode rootNode = mapper.readTree(response);
			 JsonNode producers = rootNode.path("data").path("producers");
			 Iterator<JsonNode> prodItr = producers.getElements();
			 while(prodItr.hasNext()){
				 JsonNode producer = prodItr.next();
				 String addr = producer.path("broadcast_address").getTextValue();
				 if ( addr == null ) { // We're keeping previous field compatibility, just in case
					addr = producer.path("address").getTextValue();
				 }
				 int tcpPort = producer.path("tcp_port").getIntValue();
				 outputs.add(addr + ":" + tcpPort);
			 }
		} catch (JsonParseException e) {
			log.error("Error parsing json from lookupd:", e);
		} catch (JsonMappingException e) {
			log.error("Error mapping json from lookupd:", e);
		} catch (IOException e) {
			log.error("Error reading response from lookupd:", e);
		}
		return outputs;
	}
    
//    TODO 这段代码在linux上跑不通，在mac上没问题？？？？
//    public List<ConnectionAddress> lookup(String topic) {
//    	HashMap<String, ConnectionAddress> addresses = new HashMap<String, ConnectionAddress>();
//    	log.debug("lookup topic:{}",new Object[]{topic});
//    	for (String addr : this.addresses) {
//    		
//    		DynMap mp = DynMap.instance(this.getHTML(addr + "/lookup?topic=" + topic), new DynMap());
//    		for (DynMap node : mp.getListOrEmpty(DynMap.class, "data.producers")) {
//    			String host = node.getString("broadcast_address", node.getString("address"));
//    			String key =  host + ":" + node.getInteger("tcp_port");
//    			ConnectionAddress address = new ConnectionAddress();
//    			address.setHost(host);
//    			address.setPort(node.getInteger("tcp_port"));
//    			addresses.put(key, address);
//    		}
//    		
//    	}
//    	log.debug("拉取到nsqd地址列表：{}",new Object[]{addresses});
//    	
//    	return new ArrayList<ConnectionAddress>(addresses.values());
//    }
//
//    public String getHTML(String url) {
//      URL u;
//      HttpURLConnection conn = null;
//      BufferedReader rd = null;
//      String line;
//      String result = "";
//      try {
//    	 log.debug("lookupd:{},开始拉取nsqd",new Object[]{url});
//         u = new URL(url);
//         conn = (HttpURLConnection) u.openConnection();
//         conn.setConnectTimeout(5000);
//         conn.setRequestMethod("GET");
//             rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//             while ((line = rd.readLine()) != null) {
//                result += line;
//             }
//
//          } catch (Exception e) {
//              log.error("Caught", e);
//          } finally {
//              try {
//                  if (rd != null){
//                        rd.close();
//                    }
//                } catch (Exception e) {
//                    log.error("Caught", e);
//                }
//
//                // Release memory and underlying resources on the HttpURLConnection otherwise we may run out of file descriptors and leak memory
//                if (conn != null){
//                    conn.disconnect();
//                }
//          }
//          return result;
//       }

}
