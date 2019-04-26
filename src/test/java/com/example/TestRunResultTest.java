package com.example;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.google.gson.Gson;

public class TestRunResultTest {

    private static String EXAMPLE_JSON = "{\"id\":\"1556250632326-d154a616-f4e2-4c28-9697-ab3a32af8ad7\",\"testOrder\":[\"org.apache.dubbo.rpc.cluster.support.ForkingClusterInvokerTest.testClearRpcContext\",\"org.apache.dubbo.rpc.cluster.router.tag.TagRouterTest.testRoute_requestWithTag_shouldDowngrade\"],\"results\":{\"org.apache.dubbo.rpc.cluster.router.tag.TagRouterTest.testRoute_requestWithTag_shouldDowngrade\":{\"name\":\"org.apache.dubbo.rpc.cluster.router.tag.TagRouterTest.testRoute_requestWithTag_shouldDowngrade\",\"result\":\"PASS\",\"time\":0.021263325,\"stackTrace\":[]},\"org.apache.dubbo.rpc.cluster.support.ForkingClusterInvokerTest.testClearRpcContext\":{\"name\":\"org.apache.dubbo.rpc.cluster.support.ForkingClusterInvokerTest.testClearRpcContext\",\"result\":\"PASS\",\"time\":0.6207401,\"stackTrace\":[]}},\"diffs\":{}}";

    @Test
    public void testParsing() {
        Gson gson = new Gson();
        TestRunResult result = gson.fromJson(EXAMPLE_JSON, TestRunResult.class);

        System.out.println(result.getResults().keySet());

        for (String key : result.getResults().keySet()) {
            System.out.println(result.getResults().get(key).getTime());
        }
    }
}
