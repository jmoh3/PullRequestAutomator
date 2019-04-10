package com.example;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PatchTest {

    private static final String PATCH_DIRECTORY = "/Users/jackieoh/Desktop/PURE/patches/";
    private static final String OPEN_SOURCE_REPOSITORY_DIRECTORY = "/Users/jackieoh/Desktop/PURE/openSourceRepositories";

    @Test
    public void testStatusNoCleaners() {
        Patch patch = new Patch(PATCH_DIRECTORY + "com.alibaba.json.bvt.bug.Issue_717.test_for_issue.patch", "");
        assertEquals(Patch.Status.NO_CLEANERS, patch.getStatus());
    }

    @Test
    public void testStatusInlineFail() {
        Patch patch = new Patch(PATCH_DIRECTORY + "com.alibaba.json.bvt.date.DateTest_tz.test_codec.patch", "");
        assertEquals(Patch.Status.INLINE_FAIL, patch.getStatus());
    }

    @Test
    public void testStatusInlineSuccess() {
        Patch patch = new Patch(PATCH_DIRECTORY + "com.github.kevinsawicki.http.HttpRequestTest.deleteWithEscapedMappedQueryParams.patch", "");
        assertEquals(Patch.Status.INLINE_SUCCESS, patch.getStatus());
    }

    @Test
    public void parsePackageNameApache() {
        String incubatorDubboPath = "./apache.incubator-dubbo/apache.incubator-dubbo=org.apache.dubbo.rpc.protocol.dubbo.telnet.PortTelnetHandlerTest.testListAllPort/apache.incubator-dubbo_output/incubator-dubbo-dubbo-rpc-dubbo-rpc-dubbo/fixer/org.apache.dubbo.rpc.protocol.dubbo.telnet.PortTelnetHandlerTest.testListAllPort.patch.5";

        HashMap<String, String> parsedPath = Patch.parsePackageFilePath(incubatorDubboPath);

        assertEquals("testListAllPort", parsedPath.get("test"));
        assertEquals("dubbo-rpc-dubbo-rpc-dubbo", parsedPath.get("module"));
        assertEquals("apache/incubator-dubbo", parsedPath.get("slug"));
        assertEquals("org.apache.dubbo.rpc.protocol.dubbo.telnet.PortTelnetHandlerTest", parsedPath.get("filepathWithinModule"));
    }

    @Test
    public void parsePackageNameAlibaba() {
        String alibabaPath = "./alibaba.fastjson/alibaba-fastjson=com.alibaba.json.bvt.serializer.MaxBufSizeTest.test_max_buf/alibaba.fastjson_output/fastjson/fixer/com.alibaba.json.bvt.serializer.MaxBufSizeTest.test_max_buf.patch.45";

        HashMap<String, String> parsedPath = Patch.parsePackageFilePath(alibabaPath);

        assertEquals("alibaba/fastjson", parsedPath.get("slug"));
        assertEquals("fastjson", parsedPath.get("module"));
        assertEquals("test_max_buf", parsedPath.get("test"));
        assertEquals("com.alibaba.json.bvt.serializer.MaxBufSizeTest", parsedPath.get("filepathWithinModule"));
    }

    @Test
    public void testGetFileDubbo() {
        String pathToLocalRepo = "/Users/jackieoh/Desktop/PURE/incubator-dubbo";
        String module = "dubbo-rpc-dubbo-rpc-dubbo";
        String pathWithinModule = "org.apache.dubbo.rpc.protocol.dubbo.telnet.PortTelnetHandlerTest";

        Patch.findPathToFile(pathToLocalRepo, module, pathWithinModule);
    }

    @Test
    public void testGetFileAlibaba() {
        String pathToLocalRepo = "/Users/jackieoh/Desktop/PURE/fastjson";
        String alibabaPath = "./alibaba.fastjson/alibaba-fastjson=com.alibaba.json.bvt.serializer.MaxBufSizeTest.test_max_buf/alibaba.fastjson_output/fastjson/fixer/com.alibaba.json.bvt.serializer.MaxBufSizeTest.test_max_buf.patch.45";
        HashMap<String, String> parsedPath = Patch.parsePackageFilePath(alibabaPath);

        Patch.findPathToFile(pathToLocalRepo, parsedPath.get("module"), parsedPath.get("filepathWithinModule"));
    }

    @Test
    public void testPatchConstructorDubbo() {
        String incubatorDubboPathToPatch = "/Users/jackieoh/Desktop/PURE/all-patches/org.apache.dubbo.rpc.protocol.dubbo.telnet.ChangeTelnetHandlerTest.testChangeServiceNotExport.patch.77";
        String incubatorDubboLocation = "./apache.incubator-dubbo/apache.incubator-dubbo=org.apache.dubbo.rpc.protocol.dubbo.telnet.ChangeTelnetHandlerTest.testChangeServiceNotExport/apache.incubator-dubbo_output/incubator-dubbo-dubbo-rpc-dubbo-rpc-dubbo/fixer/org.apache.dubbo.rpc.protocol.dubbo.telnet.ChangeTelnetHandlerTest.testChangeServiceNotExport.patch.77";

        Patch patch = new Patch(incubatorDubboPathToPatch, incubatorDubboLocation, OPEN_SOURCE_REPOSITORY_DIRECTORY);

        assertEquals("/Users/jackieoh/Desktop/PURE/openSourceRepositories/incubator-dubbo/dubbo-rpc/dubbo-rpc-dubbo/src/test/java/org/apache/dubbo/rpc/protocol/dubbo/telnet/ChangeTelnetHandlerTest.java", patch.getPathToFile());
        assertEquals(Patch.Status.INLINE_SUCCESS, patch.getStatus());
        assertEquals("apache/incubator-dubbo", patch.getSlug());
        assertEquals("testChangeServiceNotExport", patch.getFlaky());

        assertTrue(patch.applyPatch());
        assertTrue(patch.undoPatch());
    }

    @Test
    public void testPatchConstructorFastjson() {
        String fastJsonPathToPatch = "/Users/jackieoh/Desktop/PURE/patches/com.alibaba.json.bvt.serializer.MaxBufSizeTest.test_max_buf.patch";
        String fastJsonLocation = "./alibaba.fastjson/alibaba-fastjson=com.alibaba.json.bvt.serializer.MaxBufSizeTest.test_max_buf/alibaba.fastjson_output/fastjson/fixer/com.alibaba.json.bvt.serializer.MaxBufSizeTest.test_max_buf.patch.45";

        Patch patch = new Patch(fastJsonPathToPatch, fastJsonLocation, OPEN_SOURCE_REPOSITORY_DIRECTORY);

        assertEquals("/Users/jackieoh/Desktop/PURE/openSourceRepositories/fastjson/src/test/java/com/alibaba/json/bvt/serializer/MaxBufSizeTest.java", patch.getPathToFile());
        assertEquals(Patch.Status.INLINE_FAIL, patch.getStatus());
        assertEquals("alibaba/fastjson", patch.getSlug());
        assertEquals("test_max_buf", patch.getFlaky());

        assertFalse(patch.applyPatch());
    }

    @Test
    public void testPatchConstructorFastjsonInlineSuccess() {
        String incubatorDubboPathToPatch = "/Users/jackieoh/Desktop/PURE/all-patches/com.alibaba.json.bvt.serializer.MaxBufSizeTest.test_max_buf.patch.175";
        String incubatorDubboLocation = "/alibaba.fastjson/alibaba-fastjson=com.alibaba.json.bvt.serializer.MaxBufSizeTest.test_max_buf/alibaba.fastjson_output/fastjson/fixer/com.alibaba.json.bvt.serializer.MaxBufSizeTest.test_max_buf.patch.175";

        Patch patch = new Patch(incubatorDubboPathToPatch, incubatorDubboLocation, OPEN_SOURCE_REPOSITORY_DIRECTORY);

        assertEquals("/Users/jackieoh/Desktop/PURE/openSourceRepositories/fastjson/src/test/java/com/alibaba/json/bvt/serializer/MaxBufSizeTest.java", patch.getPathToFile());
        assertEquals(Patch.Status.INLINE_SUCCESS, patch.getStatus());
        assertEquals("alibaba/fastjson", patch.getSlug());
        assertEquals("test_max_buf", patch.getFlaky());

        assertTrue(patch.applyPatch());
        assertTrue(patch.undoPatch());
    }
}
