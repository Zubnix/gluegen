package com.jogamp.common.net;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jogamp.common.util.IOUtil;

public class AssetURLConnectionRegisteredTest extends AssetURLConnectionBase {
    
    @BeforeClass
    public static void assetRegistration() throws Exception {
        try {
            System.err.println("******* Asset URL Stream Handler Registration: PRE");
            Assert.assertTrue("GenericURLStreamHandlerFactory.register() failed", AssetURLContext.registerHandler(AssetURLConnectionRegisteredTest.class.getClassLoader()));
            Assert.assertNotNull(AssetURLContext.getRegisteredHandler());
            System.err.println("******* Asset URL Stream Handler Registration: POST");
        } catch (Exception e) {
            setTestSupported(false);
            throw e;
        }        
    }
    
    @Test
    public void assetRegisteredURLConnection_RT() throws IOException {
        testAssetConnection(createAssetURLConnection(test_asset_rt_url), test_asset_rt_entry);
    }
    
    @Test
    public void assetRegisteredURLConnection_Test() throws IOException {
        testAssetConnection(createAssetURLConnection(test_asset_test1_url), test_asset_test1_entry);
    }
    
    @Test
    public void assetRegisteredIOUtilGetResourceRel1_RT() throws IOException, URISyntaxException {
        final URLConnection urlConn0 = IOUtil.getResource(test_asset_test2a_url, this.getClass().getClassLoader());
        Assert.assertNotNull(urlConn0);
        Assert.assertEquals(test_asset_test2a_url, urlConn0.getURL().toExternalForm());
        testAssetConnection(urlConn0, test_asset_test2_entry);
        
        final URI uri1 = IOUtil.getRelativeOf(urlConn0.getURL().toURI(), test_asset_test3_rel);
        Assert.assertNotNull(uri1);
        Assert.assertEquals(test_asset_test3a_url, uri1.toString());
        testAssetConnection(uri1.toURL().openConnection(), test_asset_test3_entry);
        
        final URI uri2 = IOUtil.getRelativeOf(urlConn0.getURL().toURI(), test_asset_test4_rel);
        Assert.assertNotNull(uri2);
        Assert.assertEquals(test_asset_test4a_url, uri2.toString());
        testAssetConnection(uri2.toURL().openConnection(), test_asset_test4_entry);
    }
        
    @Test
    public void assetRegisteredIOUtilGetResourceRel2_RT() throws IOException, URISyntaxException {
        final URLConnection urlConn0 = IOUtil.getResource(test_asset_test2b_url, this.getClass().getClassLoader());
        Assert.assertNotNull(urlConn0);
        Assert.assertEquals(test_asset_test2b_url, urlConn0.getURL().toExternalForm());
        testAssetConnection(urlConn0, test_asset_test2_entry);
        
        final URI uri1 = IOUtil.getRelativeOf(urlConn0.getURL().toURI(), test_asset_test3_rel);
        Assert.assertNotNull(uri1);
        Assert.assertEquals(test_asset_test3b_url, uri1.toString());
        testAssetConnection(uri1.toURL().openConnection(), test_asset_test3_entry);
        
        final URI uri2 = IOUtil.getRelativeOf(urlConn0.getURL().toURI(), test_asset_test4_rel);
        Assert.assertNotNull(uri2);
        Assert.assertEquals(test_asset_test4b_url, uri2.toString());
        testAssetConnection(uri2.toURL().openConnection(), test_asset_test4_entry);        
    }
    
    URLConnection createAssetURLConnection(String path) throws IOException {
        URL url = AssetURLContext.createURL(path);
        URLConnection c = url.openConnection();
        System.err.println("createAssetURL: "+path+" -> url: "+url+" -> conn: "+c+" / connURL "+(null!=c?c.getURL():null));
        return c;        
        
    }
    
    public static void main(String args[]) throws IOException {
        String tstname = AssetURLConnectionRegisteredTest.class.getName();
        org.junit.runner.JUnitCore.main(tstname);
    }    
}
