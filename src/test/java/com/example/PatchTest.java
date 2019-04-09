package com.example;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PatchTest {

    private static final String patchDirectory = "/Users/jackieoh/Desktop/PURE/patches/";

    @Test
    public void testStatusNoCleaners() {
        Patch patch = new Patch(patchDirectory + "com.alibaba.json.bvt.bug.Issue_717.test_for_issue.patch", "");
        assertEquals(Patch.Status.NO_CLEANERS, patch.getStatus());
    }

    @Test
    public void testStatusInlineFail() {
        Patch patch = new Patch(patchDirectory + "com.alibaba.json.bvt.date.DateTest_tz.test_codec.patch", "");
        assertEquals(Patch.Status.INLINE_FAIL, patch.getStatus());
    }

    @Test
    public void testStatusInlineSuccess() {
        Patch patch = new Patch(patchDirectory + "com.github.kevinsawicki.http.HttpRequestTest.deleteWithEscapedMappedQueryParams.patch", "");
        assertEquals(Patch.Status.INLINE_SUCCESS, patch.getStatus());
    }

    @Test
    public void testApplyPatchSuccess() {
        Patch patch = new Patch("/Users/jackieoh/IdeaProjects/adventure/src/com/example/GithubPRPatch.patch",  "/Users/jackieoh/IdeaProjects/adventure/src/com/example/GithubPullRequestTest.java");
        assertTrue(patch.applyPatch());
        assertTrue(patch.undoPatch());
    }

    @Test
    public void parsePackageNameApache() {
        String incubatorDubboPath = "./apache.incubator-dubbo/apache.incubator-dubbo=org.apache.dubbo.rpc.protocol.dubbo.telnet.PortTelnetHandlerTest.testListAllPort/apache.incubator-dubbo_output/incubator-dubbo-dubbo-rpc-dubbo-rpc-dubbo/fixer/org.apache.dubbo.rpc.protocol.dubbo.telnet.PortTelnetHandlerTest.testListAllPort.patch.5";

        HashMap<String, String> parsedPath = Patch.parsePackageFilePath(incubatorDubboPath);

        assertEquals("testListAllPort", parsedPath.get("test"));
        assertEquals("dubbo-rpc-dubbo-rpc-dubbo", parsedPath.get("module"));
        assertEquals("apache/incubator-dubbo", parsedPath.get("slug"));
        assertEquals("org.apache.dubbo.rpc.protocol.dubbo.telnet.PortTelnetHandlerTest", parsedPath.get("filepathWithinModule"));

//        System.out.println(parsedPath.get("module") + "/src/main/test" + parsedPath.get("filepathWithinModule").replace("\\.", "/"));
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
    public void testSecondPatchConstructor() {
        String incubatorDubboPathToPatch = "/Users/jackieoh/Desktop/PURE/patches/org.apache.dubbo.rpc.protocol.dubbo.telnet.ChangeTelnetHandlerTest.testChangeServiceNotExport.patch";
        String incubatorDubboLocation = "./apache.incubator-dubbo/apache.incubator-dubbo=org.apache.dubbo.rpc.protocol.dubbo.telnet.ChangeTelnetHandlerTest.testChangeServiceNotExport/apache.incubator-dubbo_output/incubator-dubbo-dubbo-rpc-dubbo-rpc-dubbo/fixer/org.apache.dubbo.rpc.protocol.dubbo.telnet.ChangeTelnetHandlerTest.testChangeServiceNotExport.patch.55";
        String pathToLocalRepo = "/Users/jackieoh/Desktop/PURE/incubator-dubbo";

        Patch patch = new Patch(incubatorDubboPathToPatch, incubatorDubboLocation, pathToLocalRepo);

        assertEquals("/Users/jackieoh/Desktop/PURE/incubator-dubbo/dubbo-rpc/dubbo-rpc-dubbo/src/test/java/org/apache/dubbo/rpc/protocol/dubbo/telnet/ChangeTelnetHandlerTest.java", patch.getPathToFile());
        assertEquals(Patch.Status.INLINE_SUCCESS, patch.getStatus());
        assertEquals("apache/incubator-dubbo", patch.getSlug());
        assertEquals("testChangeServiceNotExport", patch.getFlaky());

        assertTrue(patch.applyPatch());
        assertTrue(patch.undoPatch());
    }
}
